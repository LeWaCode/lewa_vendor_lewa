package com.lewa.pond.auth;

import java.io.IOException;
import java.util.HashMap;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.LewaUser;
import com.lewa.core.util.LewaUtils;
import com.lewa.pond.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressWarnings("unused")
public class LewaAccountActivity extends Activity implements OnCancelListener, OnClickListener, com.lewa.pond.auth.LewaConnectionAlertDialog.AlertDialogListener {
	
	public static final String TAG = "LewaAccountActivity";
	
//	private TextView email = null;
	private EditText password = null;
	private EditText passwordConfirm = null;
	private AlertDialog alertDialog = null;
	private int initial_delay = 1000;
	private int delay = 0;
	private boolean accessGranted = false;
	private static boolean defsLoaded = false;
	
	@Override
	public void OnHasConnectionListener() {
		this.load();
	}
	
	@Override
	public void OnAbortedDialogListener() {
		this.finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		
		IntentFilter filter = new IntentFilter();
		filter.addAction("definitions-loaded");
		filter.addAction(LewaAuthService.ACTION_AUTH_SERVICE_BOUND);
		this.registerReceiver(bcr, filter);
        
		LewaUser user = LewaUser.getInstance(this);
		if (user.getRegistrationState() != LewaUser.USER_IS_REGISTERED) {
			// send user to login screen
			Intent intent = new Intent(this,LewaLoginActivity.class);
			startActivity(intent);
			finish();
		} else {
			// let user access account page (must enter password first)
			this.lock(false);
		}
	}
	
	/**
	 * Checks if user has connection and show the alertdialog. If the user does not have a connection,
	 * the alert dialog will keep looping, until:
	 * 
	 * 1) if user has an internet connection
	 * 2) if user presses "Back" (cancel)
	 */
	public void lock(boolean force) {
		LewaConnectionAlertDialog helper = new LewaConnectionAlertDialog(this, this);
		helper.prepareBuilder(
			getString(R.string.label_internet_connection),
			getString(R.string.error_internet_connectivity),
			getString(R.string.label_retry),
			getString(R.string.label_back)
		);
		helper.loopDialog(force);
	}
	
	/**
	 * Allow the user to enter their My Account page (starts with an AlertDialog with password EditText)
	 */
	public void load() {
		Log.d(TAG, "loading...");
		this.bindService();
		LewaUser user = LewaUser.getInstance(this);
		int state = user.getRegistrationState();
		if (state == LewaUser.USER_IS_NOT_AUTHENTICATED || state == LewaUser.USER_IS_REGISTERED_BUT_NOT_VERIFIED) {
			Intent intent = new Intent(this, LewaLoginActivity.class);
			startActivityForResult(intent, 1);
		}
		else if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
			if (!accessGranted) {
				if(LewaUtils.checkNetworkInfo(this)!=0){
					this.promptPassword("");
				}else{
					Log.d(TAG,"no internet");
					Toast.makeText(LewaAccountActivity.this,getString(R.string.error_internet_connectivity),Toast.LENGTH_SHORT).show();
					finish();				    
				}				
			}
		}
	}
	
	private BroadcastReceiver bcr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "action: "+action);
			if (action.equals(APIDefinitions.ACTION_DEFINITIONS_LOADED)) {
				defsLoaded = true;
			}
			else if (action.equals(LewaAuthService.ACTION_AUTH_SERVICE_BOUND)) {
				lewaAuthService.loadDefinitions();
			}
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()");
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
//		this.email = null;
		this.password = null;
		this.passwordConfirm = null;
		if (this.alertDialog != null) {
			this.alertDialog.dismiss();
			this.alertDialog = null;
		}
		this.unbindService();
		this.unregisterReceiver(this.bcr);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        this.setResult((accessGranted) ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        finish();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		this.finish();
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.account_cancel_changes) {
			this.finish();
		}
		else if (v.getId() == R.id.account_submit_changes) {
			
			String tempPassword = password.getEditableText().toString().trim();
			String tempPasswordConfirm = passwordConfirm.getEditableText().toString().trim();
			
			ProgressDialog progressDialog = ProgressDialog.show(this, getString(R.string.label_my_account), getString(R.string.dialog_saving_changes), true, true);
			
			LewaUser user = (LewaUser) v.getTag();
//			user.setEmailAddressNew(email.getEditableText().toString().toLowerCase().trim());
			user.setPasswordNew(tempPassword);
			
			if (user.getEmailAddress().equals(user.getEmailAddressNew()) && user.getPasswordOriginal().equals(user.getPasswordNew())) {
				progressDialog.dismiss();
				Toast.makeText(this, this.getString(R.string.dialog_account_info_not_changed), Toast.LENGTH_LONG).show();
				return;
			}
			
			String emailOK = LewaUtils.validateEmail(user.getEmailAddressNew(), false);
			boolean passwordOK = (LewaUtils.validatePassword(user.getPasswordNew()) && tempPassword.equals(tempPasswordConfirm)) ? true : false;
			boolean emailChanged = (user.getEmailAddress().equals(user.getEmailAddressNew())) ? false : true;
			String emailError = "";
			String passwordError = "";
			boolean updatedAccountInfo = false;
			
			Log.d(TAG, String.format("email ok? %s, password ok? ", emailOK, passwordOK));
			if (emailOK.equals("") || !passwordOK) {
				if (emailOK.equals("")) {
					emailError = getString(R.string.error_email_invalid);
				}
				if (!passwordOK) {
					passwordError = getString(R.string.error_password_invalid);
				}
				progressDialog.dismiss();
				this.setAccountPage(user, emailError, passwordError);
				return;
			}
			
			try {
				Log.d(TAG, "updating account info...");
				updatedAccountInfo = this.lewaAuthService.updateAccountInfo(user);
				Log.d(TAG, "updated account info");
			} catch (APIException e) {
				Log.d(TAG, "APIException");
				
				String code = e.getErrorCode();
				String message = e.getErrorDescription();
				Log.i(TAG, String.format("code: %s, message: %s", code, message));
				
				HashMap<String,HashMap<String,String>> causes = e.getErrorCauses();
				
				if (causes.containsKey(APIClient.PARAM_EMAIL_NEW)) {
					emailError = getString(R.string.error_already_registered);
				}
				if (causes.containsKey(APIClient.PARAM_PASSWORD_NEW)) {
					passwordError = getString(R.string.error_password_invalid);
				}
				progressDialog.dismiss();
				this.setAccountPage(user, emailError, passwordError);
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			progressDialog.dismiss();
			user.setEmailAddress(user.getEmailAddressNew());
			user.setPasswordOriginal(user.getPasswordNew());
			this.createDialog(user, updatedAccountInfo, emailChanged);
		}
	}
	
	private void createDialog(final LewaUser user, boolean updatedAccountInfo, boolean checkEmailPrompt) {
		
		String alertMessage = "";
		if (checkEmailPrompt) alertMessage = getString(R.string.dialog_check_your_email_change);
		else alertMessage = getString(R.string.dialog_change_password_success);
		this.setResult(Activity.RESULT_OK);
		
//		this.email.setText(user.getEmailAddress());
		this.password.setText(user.getPasswordOriginal());
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.label_my_account));
		builder.setMessage(alertMessage);
		builder.setCancelable(true);
		builder.setOnCancelListener(this);
		builder.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});
		alertDialog = builder.show();
	}
	
	private void setAccountPage(LewaUser user, String emailError, String passwordError) {
		this.delay = this.initial_delay;
		this.setContentView(R.layout.account);
		user.setEmailAddressNew(user.getEmailAddress());
		user.setPasswordNew(user.getPasswordOriginal());
		
//		this.email = (TextView) this.findViewById(R.id.account_email_address);
//		if (!emailError.equals("")) {
//			this.email.setText(user.getEmailAddressNew());
//			this.email.setError(emailError);
//		} else {
//			this.email.setText(user.getEmailAddress());
//		}
		this.password = (EditText) this.findViewById(R.id.account_password);
		if (!passwordError.equals("")) 
//		{
//			this.password.setText(user.getPasswordNew());
			this.password.setError(passwordError);
//		} else {
//			this.password.setText(user.getPasswordOriginal());
//		}
		this.passwordConfirm = (EditText) this.findViewById(R.id.account_password_confirm);
		if (!passwordError.equals("")) 
//		{
//			this.passwordConfirm.setText(user.getPasswordNew());
			this.passwordConfirm.setError(passwordError);
//		} else {
//			this.passwordConfirm.setText(user.getPasswordOriginal());
//		}
		
		Button saveButton = (Button) this.findViewById(R.id.account_submit_changes);
		saveButton.setOnClickListener(this);
		saveButton.setTag(user);
		Button cancelButton = (Button) this.findViewById(R.id.account_cancel_changes);
		cancelButton.setOnClickListener(this);
		cancelButton.setTag(user);
	}
	
	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			if (promptProgressDialog.isShowing()) promptProgressDialog.dismiss();
			LewaUser user = (LewaUser) message.obj;
			if (user.getEmailAddress() != "") {
				setAccountPage(user, "", "");
				return;
			}
			promptPassword(getString(R.string.error_password_incorrect));
		}
	};
	
	private ProgressDialog promptProgressDialog = null;
	private void promptPassword(String errorMessage) {
		Log.d(TAG, "promptPassword()");
		
		final LewaUser user = LewaUser.getInstance(this);
		final EditText password = new EditText(this);
		password.setTag("password-input");
		password.setInputType(0x81);
		password.setHint(getString(R.string.label_password));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.label_my_account));
		builder.setMessage(getString(R.string.dialog_enter_password));
		
		// does not match spec, but leave commented out if we change // password.setTransformationMethod(PasswordTransformationMethod.getInstance());
		if (errorMessage != "") password.setError(errorMessage);
		builder.setView(password);
		builder.setCancelable(true);
		builder.setOnCancelListener(this);
		builder.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				user.setPasswordOriginal(password.getEditableText().toString().trim());
				String emailAddress = "";
				try {
					emailAddress = checkPassword(user);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				user.setEmailAddress(emailAddress);
				Message message = handler.obtainMessage(1, user);
				// TODO: maybe this is not a good idea, since server throttle maybe better?
				delay += (delay * 0.5);
				handler.sendMessageDelayed(message, delay);
				promptProgressDialog = ProgressDialog.show(LewaAccountActivity.this, getString(R.string.label_my_account), getString(R.string.dialog_loading));
				if (delay == 0) {
					delay += initial_delay;
				}

			}
		});
		alertDialog = builder.create();
		alertDialog.show();
	}
	
	/**
	 * Ask Service to contact Server to check password validity. If valid, server should respond
	 * with the user's email address (so that we can show the email address in "My Account"
	 * 
	 * @param user
	 * @return
	 * @throws IOException 
	 */
	private String checkPassword(LewaUser user) throws IOException {
		String email = "";
		try {
			email = this.lewaAuthService.checkPassword(user);
		} catch (APIException e) {
			Log.e(TAG, "checkPassword() APIException");
		}
		return email;
	}
	
	private LewaAuthService lewaAuthService;
	protected boolean bound = false;
	
	private ServiceConnection connection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	lewaAuthService = ((LewaAuthService.LewaAuthBinder) service).getService();
	    }
	    public void onServiceDisconnected(ComponentName className) {
	    	lewaAuthService = null;
	    }
	};

	protected void bindService() {
	    bindService(new Intent(getApplicationContext(), LewaAuthService.class), connection, Context.BIND_AUTO_CREATE);
	    bound = true;
	}

	protected void unbindService() {
		if (bound) {
			Log.d(TAG, "bound, unbinding");
			if (lewaAuthService != null) {
				Log.d(TAG, "lewaAuthService not null, continue");
				unbindService(connection);
			} else Log.d(TAG, "lewaAuthService already null, abort");
			return;
		}
		Log.d(TAG, "unbound, aborting unbind");
	}

	@Override
	public void OnCancelConnectionRetryListener() {
		this.finish();
	}
}
