package com.lewa.pond.account;

import com.lewa.pond.R;
import com.lewa.core.base.LewaUser;
import com.lewa.core.util.LewaUtils;
import com.lewa.pond.auth.LewaConnectionAlertDialog;
import com.lewa.pond.auth.LewaLoginActivity;
import com.lewa.pond.auth.LewaConnectionAlertDialog.AlertDialogListener;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class LewaAccountAuthenticatorActivity extends AccountAuthenticatorActivity implements AlertDialogListener, OnCancelListener {
	
	public static final String TAG = "LewaAccountAuthenticatorActivity";
	
	public static final String EXTRAS_ACTION = "action";
	public static final String EXTRAS_ADD_ACCOUNT = "add-lewa-account";
	public static final String EXTRAS_DENY_ACCOUNT = "deny-lewa-account";
	public static final String EXTRAS_INTENT_ACTION = "intent-action";
	public static final String EXTRAS_INTENT_ACTION_CSTORE = "com.lewa.store.activity.MainActivity.start";
	
	private Intent intent = null;
//	private ProgressDialog pDialog = null;
	protected boolean bound = false;
	
	@Override
	public void OnHasConnectionListener() {
		this.load();
	}
	
	@Override
	public void OnAbortedDialogListener() {
		this.finish();
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		this.finish();
	}
	
	public void lock(boolean force) {
		LewaConnectionAlertDialog helper = new LewaConnectionAlertDialog(this, this);
		/*helper.prepareBuilder(
			getString(R.string.label_internet_connection),
			getString(R.string.error_internet_connectivity),
			getString(R.string.label_retry),
			getString(R.string.label_back)
		);*/
		helper.loopDialog(force);
	}
	
	public void load() {
/*		this.pDialog = ProgressDialog.show(this, "", getString(R.string.dialog_loading), true, true);
		this.pDialog.setOnCancelListener(this);*/
		this.delegate();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "binding LewaAuthService");
		this.lock(false);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		intent = null;
//		pDialog = null;
	}

	private void delegate() {
		Log.d(TAG, "delegate() invoked");
//		if (this.pDialog != null && this.pDialog.isShowing()) this.pDialog.dismiss();
		this.intent = this.getIntent();
		String what = intent.getStringExtra(EXTRAS_ACTION);
		Log.d(TAG, "do what? " + what);
		if (what != null && what.equals(EXTRAS_DENY_ACCOUNT)) {
			Toast.makeText(getApplicationContext(), this.getString(R.string.dialog_already_registered), Toast.LENGTH_LONG).show();
		} else if (what != null && what.equals(EXTRAS_ADD_ACCOUNT)) {
			//intent = new Intent();
			//intent.setAction(EXTRAS_INTENT_ACTION_CSTORE);
			//Log.d(TAG, intent.getAction());
			Log.d(TAG,"delegate,addAccount");
			this.addAccount();
			//this.startActivity(intent);
			
		} else {
			LewaUser user = LewaUser.getInstance(getApplicationContext());
			if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
				Log.d(TAG, "user is registered, opening preferences...");
				
				//redirect to store?
				//if (intent.hasExtra(EXTRAS_INTENT_ACTION) && intent.getStringExtra(EXTRAS_INTENT_ACTION).equals(EXTRAS_INTENT_ACTION_CSTORE)) {
					//intent = new Intent();
					//intent.setAction(EXTRAS_INTENT_ACTION_CSTORE);
				//} else {
					intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
					intent.putExtra("name", user.getXEmail());
					intent.putExtra("type", "com.lewa.pond");
					intent.putExtra("manageAccountsCategory", "com.lewa.pond.account.preferences");
					intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {"com.lewa.pond"});
				//}
			} else {
				Log.d(TAG, "go to login...");
				intent = new Intent(getApplicationContext(), LewaLoginActivity.class);
			}
			Log.d(TAG, String.format("starting activity with action %s", intent.getAction()));
			this.startActivity(intent);
		}
		Log.d(TAG, "all tasks completed, finishing activity...");
		this.finish();
	}
	
	public void addAccount() {
		
		Log.d(TAG, "addAccount()");
		LewaUser user = LewaUser.getInstance(getApplicationContext());
        if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
			String xEmailAddress = LewaUtils.replaceUserNameInEmail(user.getEmailAddress());
			if (xEmailAddress.equals("")) {
				xEmailAddress = "Lewa User";	// fail safe, so that it does not lock out the user (cannot add account, since email is blank)
			}
			String accountType = getString(R.string.ACCOUNT_TYPE);
			Account account = new Account(xEmailAddress, accountType.trim());
			Bundle userData = new Bundle();
			userData.putString("SERVER", xEmailAddress);
			AccountManager am = AccountManager.get(getApplicationContext());
			Account[] previousAccounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
			Log.d(TAG, String.format("number of accounts: %d", previousAccounts.length));
			if (previousAccounts.length > 0) {
				am.removeAccount(previousAccounts[0], null, null);
			}
			if (am.addAccountExplicitly(account, user.getSecretToken(), userData)) {				
				/*Bundle result = new Bundle();
				result.putString(AccountManager.KEY_ACCOUNT_NAME, xEmailAddress);
				result.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.ACCOUNT_TYPE));
				setAccountAuthenticatorResult(result);
				Log.d(TAG, String.format("set account data for: %s, account type: %s", xEmailAddress, accountType));*/				
			}
        } else {
			Log.d(TAG, "user not authenticated yet, skip");
		}
	}

	@Override
	public void OnCancelConnectionRetryListener() {
		this.finish();
	}
}