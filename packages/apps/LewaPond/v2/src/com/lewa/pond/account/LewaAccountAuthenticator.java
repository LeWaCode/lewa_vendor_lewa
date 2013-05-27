package com.lewa.pond.account;

import java.io.IOException;

import com.lewa.core.base.APIException;
import com.lewa.core.base.LewaUser;
import com.lewa.pond.R;
import com.lewa.pond.auth.LewaAuthService;
import com.lewa.pond.auth.LewaLoginActivity;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * 这是一个验证器，会异步调用这个类
 * 使用这个类，必须写相应的Service
 * @author ypzhu@lewatek.com
 *
 */
@SuppressWarnings("unused")
public class LewaAccountAuthenticator extends AbstractAccountAuthenticator {
	
	private String TAG = "LewaAccountAuthenticator";
	private Context	context;
	private SharedPreferences sp;
	private SharedPreferences.Editor editor;	
	private String NOTICE_PUSH_ACTION="com.lewa.pond.notice.push.msg";
	
	public LewaAccountAuthenticator(Context context) {
		super(context);
		this.context = context;
		LewaUser user = LewaUser.getInstance(context);
		AccountManager am = AccountManager.get(context);
		Account[] previousAccounts = am.getAccountsByType(context.getString(R.string.ACCOUNT_TYPE));
		if (previousAccounts.length == 0 && user.getRegistrationState() != LewaUser.USER_IS_NOT_AUTHENTICATED) {
			user.delete(TAG);
		}
	}
	
	public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
		LewaUser user = LewaUser.getInstance(context);
		AccountManager am = AccountManager.get(context);
		Account[] previousAccounts = am.getAccountsByType(context.getString(R.string.ACCOUNT_TYPE));
		if (previousAccounts.length > 0) {
			int state = user.getRegistrationState();
			String stringState = "";
			switch (state) {
			case LewaUser.USER_IS_REGISTERED:
				stringState = "registered";
				break;
			case LewaUser.USER_IS_REGISTERED_BUT_NOT_VERIFIED:
				stringState = "almost-registered";
				break;
			case LewaUser.USER_IS_NOT_AUTHENTICATED:
				stringState = "not-registered";
				break;
			}
			if (state == LewaUser.USER_IS_REGISTERED || state == LewaUser.USER_IS_REGISTERED_BUT_NOT_VERIFIED) {
				Intent intent = new Intent(context, LewaAccountAuthenticatorActivity.class);
				intent.putExtra(LewaAccountAuthenticatorActivity.EXTRAS_ACTION, LewaAccountAuthenticatorActivity.EXTRAS_DENY_ACCOUNT);
		        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				Log.d(TAG,"deny-lewa-account");
				return new Bundle();
			}
			user.delete(TAG + " - addAccount()");
			user.save(TAG);
			am.removeAccount(previousAccounts[0], null, null);
		}
		return this.returnParcelable(response);
	}
	
	@Override
	public Bundle getAccountRemovalAllowed(AccountAuthenticatorResponse response, Account account) throws NetworkErrorException {
	    Bundle result = super.getAccountRemovalAllowed(response, account);
	    if (result != null && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT) && !result.containsKey(AccountManager.KEY_INTENT)) {
	        final boolean removalAllowed = result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT);
	        if (removalAllowed) {
	        	Log.d(TAG, "removing account");
	        	LewaUser user = LewaUser.getInstance(context);
	    		sp=context.getSharedPreferences(LewaLoginActivity.KEY_SPCONFIG, Context.MODE_WORLD_READABLE);
	    		editor=sp.edit();
	    		putSpValue(LewaLoginActivity.KEY_SPNAME,"");
	        	user.delete(TAG);
	        	
	        	Intent intent=new Intent(NOTICE_PUSH_ACTION);
	        	context.sendBroadcast(intent);
	        }
	    }
	    return result;
	}
	
	public String getStringValue(String key) {
		return sp.getString(key, null);
	}
	
	public void putSpValue(String key, String value) {
		editor = sp.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public Bundle returnParcelable(AccountAuthenticatorResponse response) {
		Bundle ret = new Bundle();
		Intent intent = new Intent(context, LewaAccountAuthenticatorActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		ret.putParcelable(AccountManager.KEY_INTENT, intent);
		return ret;
	}
	
	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
		Log.e(TAG, ".confirmCredentials");
		return null;
	}


	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
	{
		Log.e(TAG, ".editProperties");
		return null;
	}


	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions) throws NetworkErrorException
	{
		Log.e(TAG, ".getAuthToken");
		return null;
	}


	@Override
	public String getAuthTokenLabel(String authTokenType)
	{
		Log.e(TAG, ".getAuthTokenLabel");
		return null;
	}


	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException
	{
		Log.e(TAG, ".hasFeatures");
		return null;
	}


	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle loginOptions)
	{
		Log.e(TAG, ".updateCredentials");
		return null;
	}
}
