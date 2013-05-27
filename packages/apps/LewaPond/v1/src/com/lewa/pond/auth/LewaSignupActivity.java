package com.lewa.pond.auth;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import org.json.JSONObject;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APISecurityHelper;
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
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Handles the "traditional" registration process (email, password, captcha).
 * 
 * @author vchoy
 *
 */
public class LewaSignupActivity extends Activity implements OnCancelListener, OnDismissListener, AlertDialogListener {
	
	public static final String TAG = "LewaSignupActivity";
	private static final int DIALOG_DISMISS_THRESHOLD = 80;
	
	protected Context context = this;
	public LewaAuthService lewaAuthService;
	protected boolean bound = false;
	
	private boolean defsLoaded = false;
	private boolean regPageLoaded = false;
	
	@Override
	public void OnHasConnectionListener() {
		Log.d(TAG, "OnHasConnectionListener");
		this.load();
	}
	
	@Override
	public void OnAbortedDialogListener() {
		Log.d(TAG, "OnAbortedDialogListener");
		this.finish();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate()");
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "onResume()");
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
		filter.addAction(APIClient.ERROR_INTERNET_CONNECTIVITY);
		filter.addAction(LewaAuthService.ACTION_AUTH_SERVICE_BOUND);
		filter.addAction(APIDefinitions.ACTION_DEFINITIONS_LOADED);
		filter.addAction(APIDefinitions.ACTION_DEFINITIONS_NOT_LOADED);
		filter.addAction(LewaSignupJavascriptInterface.EVENT_JS_READY);
		filter.addAction("stored-reg-token");
        this.registerReceiver(bcr, filter);
		this.bindService();
	}
	
	public BroadcastReceiver bcr = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(APIClient.ERROR_INTERNET_CONNECTIVITY) || action.equals("definitions-not-loaded")) {
				Toast.makeText(context, getString(R.string.error_internet_connectivity), Toast.LENGTH_LONG);
				finish();
			}
			else if (action.equals(APIDefinitions.ACTION_DEFINITIONS_LOADED)) {
				defsLoaded = true;
			}
			else if (action.equals(APIDefinitions.ACTION_DEFINITIONS_NOT_LOADED)) {
				// show an alert dialog. show message depending on error
			}
			else if (action.equals(LewaAuthService.ACTION_AUTH_SERVICE_BOUND)) {
				openRegPage();
			}
			else if (action.equals(LewaSignupJavascriptInterface.EVENT_JS_READY)) {
				regPageLoaded = true;
			}
		}
	};
	
	@Override
	public void onCancel(DialogInterface dialog) {
		Log.d(TAG, "onCancel()");
		this.finish();
	}
	
	@Override
	public void onDismiss(DialogInterface dialog) {
		Log.d(TAG, "onDismiss()");
		this.setResult(RESULT_OK);
		this.finish();
	}
	
	ProgressDialog progressDialog = null;
	protected void openRegPage() {
		Log.d(TAG, "openRegPage()");
		this.setContentView(R.layout.register);
		final WebChromeClient chrome = new WebChromeClient() {
			public void onProgressChanged(final WebView view, final int progress) {
				Log.d(TAG, String.format("progress: %d", progress));
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (progressDialog == null) return;
						progressDialog.setProgress(progress);
						if (progress == 100 && regPageLoaded == false) {
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
		APIDefinitions defs = lewaAuthService.getDefinitionsInstance();
		LewaSignupJavascriptInterface jsi = new LewaSignupJavascriptInterface(this);
		WebView webView = new WebView(this);
		WebSettings webSettings = webView.getSettings();
		LinearLayout root = (LinearLayout) this.findViewById(R.id.register_linear_layout);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		webView.setLayoutParams(params);
		root.addView(webView);
        webView.setScrollBarStyle(WebView.SCROLLBARS_INSIDE_OVERLAY);
        webView.setBackgroundColor(R.color.color_black);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setAppCacheEnabled(false);
		webView.addJavascriptInterface(jsi, "r2d2c3po");
		webView.setWebChromeClient(chrome);
		webView.setVerticalScrollBarEnabled(true);
		webView.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
		webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webView.requestFocus(WebView.FOCUS_DOWN);
		webView.setOnTouchListener(new WebView.OnTouchListener() {
	        @Override
	        public boolean onTouch(View v, MotionEvent event) {
	            switch (event.getAction()) {
	                case MotionEvent.ACTION_DOWN:
	                case MotionEvent.ACTION_UP:
	                    if (!v.hasFocus()) { v.requestFocus(); } break;
	            }
	            return false;
	        }
	    });
		String url = defs.getApiURL(APIDefinitions.KEY_URL_SIGNUP, true);
		Locale locale = Locale.getDefault();
		String initialScale = ScreenDensityHelper.getInitialScaleString(getApplicationContext());
		String densityDpi = ScreenDensityHelper.getDensityDpiString(getApplicationContext());
		url = String.format("%s?lang=%s&density_dpi=%s&initial_scale=%s", url, locale.toString(), densityDpi, initialScale);
		webView.loadUrl(url);
	}
	
	protected void openRegConfirm() {
		Log.d(TAG, "openRegConfirm()");
		AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
		aBuilder.setTitle(getString(R.string.label_register));
		aBuilder.setMessage(getString(R.string.dialog_check_your_registration_email)).setCancelable(false);
		aBuilder.setPositiveButton(getString(R.string.label_go_to_inbox), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				LewaUser user = LewaUser.getInstance(LewaSignupActivity.this);
				String emailDomain = LewaUtils.extractEmailAddressDomain(user.getEmailAddressNew());
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://mail.%s", emailDomain))));
				setResult(Activity.RESULT_OK);
				finish();
			}
		});
		aBuilder.setNegativeButton(getString(R.string.label_later), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
				intent.putExtra("manageAccountsCategory", "com.lewa.pond.account.preferences");
				intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {"com.lewa.pond"});
				finish();
				startActivity(intent);
			}
		});
		aBuilder.show().setOnDismissListener(this);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, String.format("activity finished! requestCode: %d, resultCode: %d, intent: %s", requestCode, resultCode, (intent != null) ? intent.toString() : ""));
        if (resultCode == RESULT_OK) this.finish();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		if (progressDialog.isShowing()) progressDialog.dismiss();
		super.onPause();
	}
	
	@Override
	public void onStop() {
		Log.d(TAG, "onStop()");
		super.onStop();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy()");
		this.unregisterReceiver(this.bcr);
		this.unbindService();
		System.gc();
		super.onDestroy();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// nothing
	}
	
	private ServiceConnection connection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	lewaAuthService = ((LewaAuthService.LewaAuthBinder) service).getService();
	    }

	    public void onServiceDisconnected(ComponentName className) {
	    	lewaAuthService = null;
	    }
	};

	protected void bindService() {
		Log.d(TAG, "bindService()");
	    bindService(new Intent(getApplicationContext(), LewaAuthService.class), connection, Context.BIND_AUTO_CREATE);
	    bound = true;
	}

	protected void unbindService() {
		if (bound) {
			Log.d(TAG, "bound, unbinding");
			if (lewaAuthService != null) {
				Log.d(TAG, "lewaAuthService not null, continue");
				unbindService(connection);
				lewaAuthService.stopSelf();
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
