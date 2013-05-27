package com.lewa.pond.auth;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.json.JSONObject;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.LewaUser;
import com.lewa.core.util.LewaUtils;
import com.lewa.core.util.ScreenDensityHelper;
import com.lewa.pond.R;
import com.lewa.pond.auth.LewaConnectionAlertDialog.AlertDialogListener;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * This activity
 * 
 * @author vchoy
 * 
 */
public class LewaLoginActivity extends Activity implements OnTouchListener, OnDismissListener, AlertDialogListener {

	public static final String TAG = "LewaLoginActivity";

	public static final String JSI_INTERFACE_INTENT = "com.lewa.pond.jsi";
	
	private static final int DIALOG_DISMISS_THRESHOLD = 80;
	private static final int DO_SUCCESS_PROMPT = 0x001;
	private static final int DO_FAILURE_MESSAGE = 0x010;
	private static final int USER_INPUT_VALID = 0x001;
	private static final int USER_INPUT_INVALID = 0x000;
	
	public Context context = this;
	public LewaUser user;
	public ServiceConnection connection;
	public LewaAuthService lewaAuthService;
	
	private LewaLoginJavascriptInterface jsi = null;
	public String emailAddress = "";
	public String emailDomain = "";
	private EditText email = null;
	private int emailViewId = -1;
	private boolean bound = false;
	private boolean defsLoaded = false;
	private boolean loginPageOpened = false;
	
	private int postConfirmEmailRunnableWhat;
	private int postResetPasswordRunnableWhat;
	
	private BroadcastReceiver bcr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(APIDefinitions.ACTION_DEFINITIONS_LOADED)) {
				Log.d(TAG, "definitions loaded!");
				defsLoaded = true;
			} else if (action.equals(APIDefinitions.ACTION_DEFINITIONS_NOT_LOADED)) {
				Toast.makeText(context, getString(R.string.error_internet_connectivity), Toast.LENGTH_LONG);
				finish();
			} else if (action.equals("access-token-set")) {
				intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
				finish();
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
					Toast.makeText(LewaLoginActivity.this, getString(R.string.dialog_check_your_email), Toast.LENGTH_LONG).show();
					lewaAuthService.getRemoteAccessToken();
				}
				openLoginPage();
			} else if (action.equals(LewaLoginJavascriptInterface.EVENT_JS_READY)) {
				loginPageOpened = true;
			} else if (action.equals(APIClient.EVENT_FAILED_DUE_TO_ERROR)) {
				Bundle extras = intent.getExtras();
				Log.d(TAG, extras.toString());
			}
		}
	};
	
	@Override
	public void OnHasConnectionListener() { this.load(); }
	
	@Override
	public void OnAbortedDialogListener() { this.finish(); }
	
	@Override
	public void OnCancelConnectionRetryListener() { this.finish(); }
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.loading_splash_screen);
		this.jsi = new LewaLoginJavascriptInterface(this);
		this.user = LewaUser.getInstance(getApplicationContext());
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		runOnUiThread(new Runnable() {
			public void run() {
				lock(false);
				progressDialog = ProgressDialog.show(context, "", getString(R.string.dialog_loading), true, true);
				progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						finish();
					}
				});
			}
		});
		super.onResume();
	}
	
	@Override
	public void onPause() {
		if (progressDialog.isShowing()) progressDialog.dismiss();
		this.unregisterReceiver(this.bcr);
		this.unbindService();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == Activity.RESULT_OK) this.finish();
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		this.setResult(RESULT_OK);
		this.user.save(TAG + " - onDismiss");
		this.finish();
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (v.getId() == this.emailViewId) {
			EditText editText = (EditText) v;
			if (editText.getError().length() > 0) {
				editText.setError("");
			}
		} return false;
	}
	
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
		this.setContentView(R.layout.login);
		LinearLayout root = (LinearLayout) this.findViewById(R.id.login_linear_layout);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		final WebChromeClient chrome = new WebChromeClient() {
			public void onProgressChanged(final WebView view, final int progress) {
				Log.d(TAG, String.format("progress: %d", progress));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (progressDialog == null) return;
						progressDialog.setProgress(progress);
						if (progress == 100 && loginPageOpened == false) {
							setContentView(R.layout.loading_splash_screen);
							Toast.makeText(context, getString(R.string.error_internet_connectivity), Toast.LENGTH_LONG).show();
						}
						if (progress > DIALOG_DISMISS_THRESHOLD && progressDialog.isShowing()) {
							progressDialog.dismiss();
						}
					}
				});
			}
		};
		final APIDefinitions defs = lewaAuthService.getDefinitionsInstance();
		final WebView webView = new WebView(this);
		root.addView(webView);
		webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
		webView.setBackgroundColor(R.color.color_black);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setAppCacheEnabled(false);
		webView.addJavascriptInterface(jsi, "r2d2c3po");
		webView.setWebChromeClient(chrome);
		webView.setVerticalScrollBarEnabled(true);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.setLayoutParams(params);
		webView.requestFocus(WebView.FOCUS_DOWN);
		webView.setOnTouchListener(new WebView.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN:
	                case MotionEvent.ACTION_UP:
	                    if (!v.hasFocus()) { v.requestFocus(); } break;
	            } return false;
	        }
	    });
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				String url = defs.getApiURL(APIDefinitions.KEY_URL_LOGIN, true);
				Locale locale = Locale.getDefault();
				String initialScale = ScreenDensityHelper.getInitialScaleString(getApplicationContext());
				String densityDpi = ScreenDensityHelper.getDensityDpiString(getApplicationContext());
				url = String.format("%s?lang=%s&density_dpi=%s&initial_scale=%s", url, locale.toString(), densityDpi, initialScale);
				webView.loadUrl(url);
			}
		});
		thread.start();
	}

	public void openLoginAlert() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setMessage(getString(R.string.dialog_logged_in));
		alert.setCancelable(true);
		alert.setTitle(getString(R.string.label_login));
		alert.setPositiveButton(getString(R.string.label_close), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dialog != null) dialog.dismiss();
				finish();
			}
		});
		alert.show();
	}
	
	/**
	 * Opens reset password prompt (AlertDialog)
	 * 
	 * @param error		String	(seeds the error message)
	 * TODO: change to strings.xml
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
		dialogBuilder.setMessage(String.format(getString(R.string.dialog_reset_password_email)));
		dialogBuilder.setCancelable(true);
		DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton)  {
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
				resetPasswordHandler.sendMessageDelayed(message, 500);		// adding delay will hopefully ensure that alertdialog closed and progress dialog shown
			}
		};
		dialogBuilder.setPositiveButton(getString(R.string.label_ok), onClickListener);
		dialogBuilder.show();
	}
	
	/**
	 * Depending on the validity of email, routes session to proper flow (allow reset/loop dialog with error message)
	 * This handler is prior to resetting the password (step 1)
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
					if (dialog.isShowing()) dialog.dismiss();	
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
	 * This is after a password has been reset... shows the "go to invox" alertdialog (step 3)
	 */
	Runnable postResetPasswordRunnable = new Runnable() {
		@Override
		public void run() {
			if (postResetPasswordRunnableWhat == DO_SUCCESS_PROMPT) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle(R.string.label_reset_password);
				builder.setMessage(R.string.dialog_password_reset_success);
				builder.setCancelable(false);
				builder.setPositiveButton(R.string.label_go_to_inbox, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						String url = String.format("http://mail.%s", emailDomain);
						Intent i = new Intent(Intent.ACTION_VIEW);
						i.setData(Uri.parse(url));
						startActivityForResult(i, 1);
					}
				});
				builder.setNegativeButton(R.string.label_later, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
				builder.show();
				return;
			}
			Toast.makeText(LewaLoginActivity.this, getString(R.string.error_internet_connectivity), Toast.LENGTH_LONG).show();
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
		final ProgressDialog resetPasswordProgressDialog = new ProgressDialog(context);
		resetPasswordProgressDialog.setTitle(getString(R.string.label_reset_password));
		resetPasswordProgressDialog.setMessage(getString(R.string.dialog_resetting_password));
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
					lewaAuthService.resetPassword(new SendResetPasswordEmailHandler(resetPasswordProgressDialog), emailAddress);
				} catch (APIException e) {
					Log.e(TAG, "api exception not caught");	// can never get here
				} catch (IOException e) {
					Log.e(TAG, "io exception not caught");	// can never get here
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
		alert.setPositiveButton(getString(R.string.label_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				email.invalidate();
				dialog.cancel();
				String emailAddress = email.getEditableText().toString().trim().toLowerCase();
				String emailDomain = LewaUtils.validateEmail(emailAddress, true);
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
		String newEmailAddress = email.getEditableText().toString().toLowerCase().trim();
		if (newEmailAddress.equals("")) newEmailAddress = user.getEmailAddressNew();
		if (!newEmailAddress.equals("") && !newEmailAddress.equals(user.getEmailAddressNew())) {
			user.setEmailAddress(newEmailAddress);
			user.setEmailAddressNew(newEmailAddress);
		}
		final ProgressDialog resendVerificationProgressDialog = ProgressDialog.show(
			LewaLoginActivity.this, 
			getString(R.string.label_email_verification), 
			String.format("%s: %s", getString(R.string.dialog_resend_verification_email), user.getEmailAddress()), 
			true, 
			true, 
			new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					dialog.dismiss();
				}
			}
		);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				lewaAuthService.resendActivationEmail(new SendConfirmEmailHandler(resendVerificationProgressDialog), user);
			}
		});
		thread.start();
	}
	
	/**
	 * After a user enters their email address (for re-sending confirmation email), this handler
	 * routes the user to the next flow in the process, prior to actually contacting the
	 * server (step 1)
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
	 * Handles the raw API response from the server for re-sending of the verification emails (step 2)
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
					if (dialog.isShowing()) dialog.dismiss();
					if (message.what == APIClient.WHAT_FAILURE) {
						APIException exception = (APIException) message.obj;
						Log.e(TAG, String.format("error code: %s, message: ", exception.getErrorCode(), exception.getErrorDescription()));
						postConfirmEmailRunnableWhat = DO_FAILURE_MESSAGE;
						Toast.makeText(LewaLoginActivity.this, getString(R.string.error_internet_connectivity), Toast.LENGTH_LONG).show();
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
	 * Handles the alertdialog shown to the user after successsfully re-sending their verification email (step 1);
	 */
	Runnable postSendConfirmEmailRunnable = new Runnable() {
		@Override
		public void run() {
			AlertDialog.Builder alert = new AlertDialog.Builder(LewaLoginActivity.this);
			alert.setTitle(R.string.label_almost_done);
			alert.setMessage(R.string.dialog_check_your_registration_email);
			alert.setCancelable(false);
			alert.setPositiveButton(R.string.label_go_to_inbox, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					String url = String.format("http://mail.%s", emailDomain);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivityForResult(i, 1);
				}
			});
			alert.setNegativeButton(R.string.label_later, new OnClickListener() {
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
			Toast.makeText(LewaLoginActivity.this, getString(R.string.error_internet_connectivity), Toast.LENGTH_LONG).show();
		}
	};
	
	protected void bindService() {
		connection = new ServiceConnection() {
			public void onServiceConnected(ComponentName className, IBinder service) {
				lewaAuthService = ((LewaAuthService.LewaAuthBinder) service).getService();
			}
			public void onServiceDisconnected(ComponentName className) {
				Log.d(TAG, "service disco");
				bound = false;
				lewaAuthService = null;
			}
		};
		if (!bound) bindService(new Intent(this, LewaAuthService.class), connection, Context.BIND_AUTO_CREATE);
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
