package com.lewa.pond.auth;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.LewaUser;
import com.lewa.core.util.LewaUtils;
import com.lewa.pond.R;
import com.lewa.pond.auth.LewaConnectionAlertDialog.AlertDialogListener;
import com.lewa.pond.utils.CustomProgressDailog;
import com.lewa.pond.utils.Login;
import com.lewa.pond.utils.StrUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity
 * 
 * @author vchoy
 * 
 */
public class LewaLoginActivity extends Activity implements
		android.view.View.OnClickListener, OnDismissListener,
		AlertDialogListener {

	public static final String TAG = "LewaLoginActivity";

	public static final String JSI_INTERFACE_INTENT = "com.lewa.pond.jsi";

	private static final int DO_SUCCESS_PROMPT = 0x001;
	private static final int DO_FAILURE_MESSAGE = 0x010;
	private static final int USER_INPUT_VALID = 0x001;
	private static final int USER_INPUT_INVALID = 0x000;
	
	private static final int USER_ADDED_ACCOUNT_SUCCESS_CODE=1;
	private static final int USER_ADDED_ACCOUNT_FAILED_CODE=2;

	public static final String KEY_OK = "ok";
	public static final String KEY_ERRORS = "errors";
	public static final String KEY_IMEI = "imei";
	public static final String KEY_SPCONFIG="thirdPartySp";
	public static final String KEY_SPNAME="ro.config.wlnum";

	public Context context = this;
	public LewaUser user;
	public ServiceConnection connection;
	public LewaAuthService lewaAuthService;
	private TelephonyManager teleManager;
	private LewaLoginJavascriptInterface jsi;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;

	public String emailAddress = "";
	public String emailDomain = "";
	private EditText email = null;
	private int emailViewId = -1;
	private boolean bound = false;
	private boolean defsLoaded = false;
	private boolean loginPageOpened = false;

	private int postConfirmEmailRunnableWhat;
	private int postResetPasswordRunnableWhat;

	private EditText nameET;
	private EditText passwordET;
	private TextView forgetPwdTV;
	private TextView reSendEmailTV;
	private Button submitBtn;
	private Button regBtn;

	private BroadcastReceiver bcr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(APIDefinitions.ACTION_DEFINITIONS_LOADED)) {
				Log.d(TAG, "definitions loaded!");
				defsLoaded = true;
			} else if (action
					.equals(APIDefinitions.ACTION_DEFINITIONS_NOT_LOADED)) {
				Toast.makeText(context,
						getString(R.string.error_internet_connectivity),
						Toast.LENGTH_LONG);
				finish();
			} else if (action.equals("access-token-set")) {
				finish();
				intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
			} else if (action.equals(JSI_INTERFACE_INTENT)) {
				String jsiAction = intent.getStringExtra("action");
				String jsiParams = intent.getStringExtra("params");
				if (jsiAction.equals("forgot-password")) {
					resetPassword("");
				} else if (jsiAction.equals("resend-activation-email")) {
					resendConfirmationEmail("");
				}
			} else if (action.equals(LewaAuthService.ACTION_AUTH_SERVICE_BOUND)) {
				Log.d(TAG, "service bound!");
				bound = true;
				lewaAuthService.loadDefinitions();
				LewaUser user = LewaUser.getInstance(getApplicationContext());
				if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED_BUT_NOT_VERIFIED) {
					Log.d(TAG, "user registered, but not verified");
					Toast.makeText(LewaLoginActivity.this,
							getString(R.string.dialog_check_your_email),
							Toast.LENGTH_LONG).show();
					lewaAuthService.getRemoteAccessToken();
				}
				openLoginPage();
			} else if (action
					.equals(LewaLoginJavascriptInterface.EVENT_JS_READY)) {
				loginPageOpened = true;
			} else if (action.equals(APIClient.EVENT_FAILED_DUE_TO_ERROR)) {
				try {
					Bundle extras = intent.getExtras();
					Log.d(TAG, extras.toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};

	private void initPages() {
		this.nameET = (EditText) findViewById(R.id.account_email_address);
		this.passwordET = (EditText) findViewById(R.id.account_password);
		this.forgetPwdTV = (TextView) findViewById(R.id.forget_pwd_tv);
		this.reSendEmailTV = (TextView) findViewById(R.id.resend_email_tv);
		this.submitBtn = (Button) findViewById(R.id.account_btn_submit);
		this.regBtn = (Button) findViewById(R.id.account_btn_register);
		this.submitBtn.setOnClickListener(this);
		this.regBtn.setOnClickListener(this);
		this.forgetPwdTV.setOnClickListener(this);
		this.reSendEmailTV.setOnClickListener(this);
	}

	private boolean checkForm(String name, String pwd) {
		if (name.equals("") || name == "" || name == null) {
			nameET.requestFocus();
			nameET.setError(getString(R.string.error_email_is_not_null));
			return false;
		} else {
			nameET.setError(null);
		}
		if (!StrUtils.isEmail(name)) {
			nameET.requestFocus();
			nameET.setError(getString(R.string.error_email_invalid));
			return false;
		} else {
			nameET.setError(null);
		}
		if (!LewaUtils.validatePassword(pwd)) {
			passwordET.requestFocus();
			passwordET
					.setError(getString(R.string.dialog_enter_valid_password));
			return false;
		} else {
			passwordET.setError(null);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		int vid = v.getId();
		int connectionCode = LewaUtils.checkNetworkInfo(this);
		switch (vid) {
		case R.id.account_btn_submit:
			if (connectionCode != 0) {
				try {
					String email = nameET.getText().toString().trim();
					String password = passwordET.getText().toString().trim();
					if (this.checkForm(email, password)) {
						this.doJob(email, password);
					} else {
						Log.e(TAG, "check form error");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Toast.makeText(this,
						getString(R.string.error_internet_connectivity),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.account_btn_register:
			if (connectionCode != 0) {
				Intent intent = new Intent(LewaLoginActivity.this,
						LewaSignupActivity.class);
				startActivity(intent);
			} else {
				Toast.makeText(this,
						getString(R.string.error_internet_connectivity),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.forget_pwd_tv:
			resetPassword("");
			break;
		case R.id.resend_email_tv:
			resendConfirmationEmail("");
			break;
		default:
			break;
		}
	}
	
	public void putSpValue(String key, String value) {
		editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public String getStringValue(String key) {
		return sp.getString(key, null);
	}

	private CustomProgressDailog dialog;

	private void doJob(final String email, final String password) {
		teleManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		final Login login = new Login(LewaLoginActivity.this);
		login.setLoginParams(email, password, teleManager.getDeviceId());

		dialog = new CustomProgressDailog(LewaLoginActivity.this);
		dialog.setProperties(null, getString(R.string.progress_dialog_logining));
		dialog.show();

		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String response = login.attemptLogin();
					JSONObject jsonObject = login.parseLoginData(response);
					if (jsonObject.has(KEY_OK) && jsonObject.getBoolean(KEY_OK)) {
						if (null != jsi) {
							String uid=jsonObject.getString("uid");
							jsi.setUserID(uid);
							jsi.setClientID(jsonObject.getString("cid"));
							jsi.setClientKey(jsonObject.getString("key"));
							jsi.setUserSecret(jsonObject.getString("secret"));
							jsi.setUserEmail(jsonObject.getString("email"));
							jsi.setAccessToken(jsonObject.getString("access_token"));
							putSpValue(KEY_SPNAME, uid);
						}
					} else {
						// Log.e(TAG, "login failed");
						Message msg = Message.obtain();
						msg.what = USER_ADDED_ACCOUNT_FAILED_CODE;
						finishHandler.sendMessage(msg);
						return;
					}
					Thread.sleep(2000);
				} catch (JSONException e1) {
					e1.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}

				Message msg = Message.obtain();
				msg.what =USER_ADDED_ACCOUNT_SUCCESS_CODE;
				finishHandler.sendMessage(msg);
			}
		}).start();
	}

	public Handler finishHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == USER_ADDED_ACCOUNT_SUCCESS_CODE) {
				try {
					if (null != dialog) {
						dialog.dismiss();
					}
					finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (msg.what == USER_ADDED_ACCOUNT_FAILED_CODE) {
				if (null != dialog) {
					dialog.dismiss();
				}
				LewaConnectionAlertDialog alertDailog = new LewaConnectionAlertDialog(
						LewaLoginActivity.this);
				alertDailog.prepareBuilder(
						getString(R.string.alert_dialog_login_failed),
						getString(R.string.alert_dialog_login_failed_result),
						null, getString(R.string.label_back));
				alertDailog.showAlertDialog(null);
			}
		}

	};

	@Override
	public void OnHasConnectionListener() {
		this.load();
	}

	@Override
	public void OnAbortedDialogListener() {
		this.finish();
	}

	@Override
	public void OnCancelConnectionRetryListener() {
		this.finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.login);
		this.user = LewaUser.getInstance(getApplicationContext());
		this.jsi = new LewaLoginJavascriptInterface(this);
		sp=context.getSharedPreferences(KEY_SPCONFIG, Context.MODE_WORLD_READABLE);
		editor=sp.edit();
	}

	@Override
	public void onResume() {
		runOnUiThread(new Runnable() {
			public void run() {
				lock(false);
			}
		});
		super.onResume();
	}

	@Override
	public void onPause() {
		this.unregisterReceiver(this.bcr);
		this.unbindService();
		if (null != dialog) {
			dialog.dismiss();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.gc();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK)
			this.finish();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		this.setResult(RESULT_OK);
		this.user.save(TAG + " - onDismiss");
		this.finish();
	}

	LewaConnectionAlertDialog helper;

	public void lock(boolean force) {
		helper = new LewaConnectionAlertDialog(this, this);
		helper.prepareBuilder(getString(R.string.label_internet_connection),
				getString(R.string.error_internet_connectivity),
				getString(R.string.label_retry), getString(R.string.label_back));
		helper.loopDialog(force);
	}

	public void load() {
		Log.d(TAG, "loading...");
		IntentFilter filter = new IntentFilter();
		filter.addAction(LewaAuthService.ACTION_AUTH_SERVICE_BOUND);
		filter.addAction(APIDefinitions.ACTION_DEFINITIONS_LOADED);
		filter.addAction(APIDefinitions.ACTION_DEFINITIONS_NOT_LOADED);
		filter.addAction("access-token-set");
		filter.addAction(JSI_INTERFACE_INTENT);
		filter.addAction(LewaLoginJavascriptInterface.EVENT_JS_READY);
		filter.addAction(APIClient.EVENT_FAILED_DUE_TO_ERROR);
		this.registerReceiver(bcr, filter);
		this.bindService();
	}

	ProgressDialog progressDialog;

	public void openLoginPage() {
		Log.d(TAG, "openLoginPage()");
		this.initPages();
	}

	public void openLoginAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(getString(R.string.dialog_logged_in));
		alert.setCancelable(true);
		alert.setTitle(getString(R.string.label_login));
		alert.setPositiveButton(getString(R.string.label_close),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (dialog != null)
							dialog.dismiss();
						finish();
					}
				});
		alert.show();
	}

	/**
	 * Opens reset password prompt (AlertDialog)
	 * 
	 * @param error
	 *            String (seeds the error message) TODO: change to strings.xml
	 */
	private void resetPassword(String error) {

		final EditText email = new EditText(this);
		email.setTag("email-input");
		email.setHint(getString(R.string.label_email_address));
		email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		if (!error.equals("")) {
			email.setError(error);
		}
		this.emailViewId = email.getId();

		final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setView(email);
		dialogBuilder.setTitle(getString(R.string.label_reset_password));
		dialogBuilder.setMessage(String
				.format(getString(R.string.dialog_reset_password_email)));
		dialogBuilder.setCancelable(true);
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				emailAddress = email.getText().toString();
				email.invalidate();
				dialog.cancel();
				emailDomain = LewaUtils.validateEmail(emailAddress, true);
				Log.d(TAG, "emailAddress: " + emailAddress);
				Message message = Message.obtain();
				if (emailDomain != "" && !emailDomain.equals("")) {
					message.what = USER_INPUT_VALID;
				} else {
					message.what = USER_INPUT_INVALID;
				}
				resetPasswordHandler.sendMessageDelayed(message, 500); // adding
																		// delay
																		// will
																		// hopefully
																		// ensure
																		// that
																		// alertdialog
																		// closed
																		// and
																		// progress
																		// dialog
																		// shown
			}
		};
		dialogBuilder.setPositiveButton(getString(R.string.label_ok),
				onClickListener);
		dialogBuilder.show();
	}

	/**
	 * Depending on the validity of email, routes session to proper flow (allow
	 * reset/loop dialog with error message) This handler is prior to resetting
	 * the password (step 1)
	 */
	Handler resetPasswordHandler = new Handler() {
		public void handleMessage(Message message) {
			if (message.what == USER_INPUT_VALID) {
				Log.d(TAG, "sending reset password email");
				sendResetPasswordEmail(emailAddress);
				return;
			}
			resetPassword(getString(R.string.error_email_invalid)); // loop
		}
	};

	class SendResetPasswordEmailHandler extends Handler {
		private ProgressDialog dialog;

		public SendResetPasswordEmailHandler(ProgressDialog dialog) {
			this.dialog = dialog;
		}

		public void handleMessage(final Message message) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (dialog.isShowing())
						dialog.dismiss();
				}
			});
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					APIResponse response = (APIResponse) message.obj;
					JSONObject responseJSON = response.getResponseJSON();
					try {
						APIException.sniffResponse(responseJSON);
						postResetPasswordRunnableWhat = DO_SUCCESS_PROMPT;
					} catch (APIException e) {
						postResetPasswordRunnableWhat = DO_FAILURE_MESSAGE;
					}
					runOnUiThread(postResetPasswordRunnable);
				}
			});
			thread.start();
		}
	}

	/**
	 * This is after a password has been reset... shows the "go to invox"
	 * alertdialog (step 3)
	 */
	Runnable postResetPasswordRunnable = new Runnable() {
		@Override
		public void run() {
			if (postResetPasswordRunnableWhat == DO_SUCCESS_PROMPT) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(
						context);
				builder.setTitle(R.string.label_reset_password);
				builder.setMessage(R.string.dialog_password_reset_success);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.label_go_to_inbox,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								String url = String.format("http://mail.%s",
										emailDomain);
								Intent i = new Intent(Intent.ACTION_VIEW);
								i.setData(Uri.parse(url));
								startActivityForResult(i, 1);
							}
						});
				builder.setNegativeButton(R.string.label_later,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});
				builder.show();
				return;
			}
			Toast.makeText(LewaLoginActivity.this,
					getString(R.string.error_internet_connectivity),
					Toast.LENGTH_LONG).show();
		}
	};

	/**
	 * TODO: change Toast messages to another AlertDialog (i do not know how to
	 * do this, i hate Android UI)
	 * 
	 * @param emailAddress
	 * @param emailDomain
	 */
	private void sendResetPasswordEmail(final String emailAddress) {
		final ProgressDialog resetPasswordProgressDialog = new ProgressDialog(
				context);
		resetPasswordProgressDialog
				.setTitle(getString(R.string.label_reset_password));
		resetPasswordProgressDialog
				.setMessage(getString(R.string.dialog_resetting_password));
		resetPasswordProgressDialog.setIndeterminate(true);
		resetPasswordProgressDialog.setCancelable(true);
		resetPasswordProgressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});
		resetPasswordProgressDialog.show();
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				try {
					lewaAuthService.resetPassword(
							new SendResetPasswordEmailHandler(
									resetPasswordProgressDialog), emailAddress);
				} catch (APIException e) {
					Log.e(TAG, "api exception not caught"); // can never get
															// here
				} catch (IOException e) {
					Log.e(TAG, "io exception not caught"); // can never get here
				}
			}
		});
		thread.start();
	}

	private void resendConfirmationEmail(String error) {

		final LewaUser user = LewaUser.getInstance(getApplicationContext());
		final EditText email = new EditText(getApplicationContext());
		email.setTag("email-input");
		email.setHint(getString(R.string.label_email_address));
		email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		email.setText(user.getEmailAddressNew());
		if (!error.equals("")) {
			email.setError(error);
		}
		this.emailViewId = email.getId();
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(getString(R.string.label_email_verification));
		alert.setMessage(getString(R.string.dialog_resend_verification_email));
		alert.setView(email);
		alert.setCancelable(true);
		alert.setPositiveButton(getString(R.string.label_ok),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						email.invalidate();
						dialog.cancel();
						String emailAddress = email.getEditableText()
								.toString().trim().toLowerCase();
						String emailDomain = LewaUtils.validateEmail(
								emailAddress, true);
						Message message = Message.obtain();
						if (emailDomain != "") {
							user.setEmailAddress(emailAddress);
							message.what = USER_INPUT_VALID;
						} else {
							message.what = USER_INPUT_INVALID;
						}
						confirmEmailHandler.sendMessageDelayed(message, 1500);
					}
				});
		alert.show();
	}

	private void prepareConfirmationEmail(final LewaUser user) {

		final EditText email = new EditText(getApplicationContext());
		email.setTag("email-input");
		email.setHint(getString(R.string.label_email_address));
		email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
		String newEmailAddress = email.getEditableText().toString()
				.toLowerCase().trim();
		if (newEmailAddress.equals(""))
			newEmailAddress = user.getEmailAddressNew();
		if (!newEmailAddress.equals("")
				&& !newEmailAddress.equals(user.getEmailAddressNew())) {
			user.setEmailAddress(newEmailAddress);
			user.setEmailAddressNew(newEmailAddress);
		}
		final ProgressDialog resendVerificationProgressDialog = ProgressDialog
				.show(LewaLoginActivity.this,
						getString(R.string.label_email_verification),
						String.format(
								"%s: %s",
								getString(R.string.dialog_resend_verification_email),
								user.getEmailAddress()), true, true,
						new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								dialog.dismiss();
							}
						});
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				lewaAuthService.resendActivationEmail(
						new SendConfirmEmailHandler(
								resendVerificationProgressDialog), user);
			}
		});
		thread.start();
	}

	/**
	 * After a user enters their email address (for re-sending confirmation
	 * email), this handler routes the user to the next flow in the process,
	 * prior to actually contacting the server (step 1)
	 */
	Handler confirmEmailHandler = new Handler() {
		public void handleMessage(Message message) {
			if (message.what == USER_INPUT_VALID) {
				Log.d(TAG, "sending confirmation email");
				prepareConfirmationEmail(user);
			} else {
				Log.d(TAG, "looping confirmation email");
				resendConfirmationEmail(getString(R.string.error_email_invalid)); // loop
			}
		}
	};

	/**
	 * Handles the raw API response from the server for re-sending of the
	 * verification emails (step 2)
	 */
	class SendConfirmEmailHandler extends Handler {
		private ProgressDialog dialog;

		public SendConfirmEmailHandler(ProgressDialog dialog) {
			this.dialog = dialog;
		}

		public void handleMessage(final Message message) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (dialog.isShowing())
						dialog.dismiss();
					if (message.what == APIClient.WHAT_FAILURE) {
						APIException exception = (APIException) message.obj;
						Log.e(TAG, String.format("error code: %s, message: ",
								exception.getErrorCode(),
								exception.getErrorDescription()));
						postConfirmEmailRunnableWhat = DO_FAILURE_MESSAGE;
						Toast.makeText(
								LewaLoginActivity.this,
								getString(R.string.error_internet_connectivity),
								Toast.LENGTH_LONG).show();
						return;
					}
					APIResponse response = (APIResponse) message.obj;
					JSONObject responseJSON = response.getResponseJSON();
					try {
						APIException.sniffResponse(responseJSON);
						postConfirmEmailRunnableWhat = DO_SUCCESS_PROMPT;
					} catch (APIException e) {
						postConfirmEmailRunnableWhat = DO_FAILURE_MESSAGE;
					}
					postSendConfirmEmailRunnable.run();
				}
			});
		}
	}

	/**
	 * Handles the alertdialog shown to the user after successsfully re-sending
	 * their verification email (step 1);
	 */
	Runnable postSendConfirmEmailRunnable = new Runnable() {
		@Override
		public void run() {
			AlertDialog.Builder alert = new AlertDialog.Builder(
					LewaLoginActivity.this);
			alert.setTitle(R.string.label_almost_done);
			alert.setMessage(R.string.dialog_check_your_registration_email);
			alert.setCancelable(false);
			alert.setPositiveButton(R.string.label_go_to_inbox,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							String url = String.format("http://mail.%s",
									emailDomain);
							Intent i = new Intent(Intent.ACTION_VIEW);
							i.setData(Uri.parse(url));
							startActivityForResult(i, 1);
						}
					});
			alert.setNegativeButton(R.string.label_later,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			if (postConfirmEmailRunnableWhat == DO_SUCCESS_PROMPT) {
				Log.d(TAG, "show the success prompt dialog");
				alert.show();
				return;
			}
			Log.w(TAG, "not successful");
			Toast.makeText(LewaLoginActivity.this,
					getString(R.string.error_internet_connectivity),
					Toast.LENGTH_LONG).show();
		}
	};

	protected void bindService() {
		connection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className,
					IBinder service) {
				lewaAuthService = ((LewaAuthService.LewaAuthBinder) service)
						.getService();
			}

			public void onServiceDisconnected(ComponentName className) {
				Log.d(TAG, "service disco");
				bound = false;
				lewaAuthService = null;
			}
		};
		if (!bound)
			bindService(new Intent(this, LewaAuthService.class), connection,
					Context.BIND_AUTO_CREATE);
	}

	protected void unbindService() {
		if (lewaAuthService != null && bound) {
			Log.d(TAG, "lewaAuthService not null, continue");
			lewaAuthService.stopSelf();
			unbindService(connection);
		}
		bound = false;
	}
}