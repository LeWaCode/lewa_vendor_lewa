package com.lewa.pond.auth;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.lewa.core.base.LewaUser;
import com.lewa.core.util.LewaUtils;


/**
 * Android-Javascript binding
 * 
 * @author vchoy
 * 
 */
public class LewaLoginJavascriptInterface {

	public static final String TAG = "LewaLoginJavascriptInterface";

	public static final String EVENT_JS_READY = "com.lewa.pond.auth.LewaLoginJavascriptInterface.EVENT_JS_READY";
	
	private LewaLoginActivity lewaLoginActivity;
	
	public LewaLoginJavascriptInterface(LewaLoginActivity lewaLoginActivity) {
		this.lewaLoginActivity = lewaLoginActivity;
	}
	
	public void log(String message) {
		Log.i(TAG, message);
	}

	public String getRegistrationState() {
		int state = lewaLoginActivity.user.getRegistrationState();
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
		return stringState;
	}
	
	public String getSignupParameters() {
		return "";
	}
	
	public String getLoginParameters() {
		String params = "";
		if (lewaLoginActivity.lewaAuthService != null) {
			Bundle bundleParams = lewaLoginActivity.lewaAuthService.getDeviceInfo();
			JSONObject jsonParams = LewaUtils.bundleToJSON(bundleParams);
			params = jsonParams.toString();
		}
		return params;
	}
	
	public void hideDialog() {
		lewaLoginActivity.sendBroadcast(new Intent(EVENT_JS_READY));
	}

	public void setUserID(String userID) {
		Log.d(TAG, "got user ID " + userID);
		lewaLoginActivity.lewaAuthService.setUserID(userID);
		lewaLoginActivity.user.setUserID(userID);
	}
	
	public void setClientID(String clientID) {
		Log.d(TAG, "got client ID " + clientID);
		lewaLoginActivity.lewaAuthService.setClientID(clientID);
		lewaLoginActivity.user.setClientID(clientID);
	}

	public void setUserSecret(String userSecret) {
		lewaLoginActivity.user.setSecretToken(userSecret);
	}

	public void setAccessToken(String accessToken) {
		lewaLoginActivity.lewaAuthService.setAccessToken(accessToken);
	}

	public void setRegToken(String regToken) {
		lewaLoginActivity.lewaAuthService.setRegToken(regToken);
		lewaLoginActivity.user.setRegistrationToken(regToken);
	}

	public void setClientKey(String clientKey) {
		lewaLoginActivity.lewaAuthService.setThirdPartyToken(clientKey);
		lewaLoginActivity.user.setThirdPartyToken(clientKey);
	}

	public void setUserEmail(String userEmail) {
		lewaLoginActivity.user.setEmailAddressNew(userEmail);
	}

	public void raiseIntent(String action) {
		Intent intent = new Intent();
		intent.setAction(LewaLoginActivity.JSI_INTERFACE_INTENT);
		intent.putExtra("action", action);
		lewaLoginActivity.sendBroadcast(intent);
	}

	public void raiseIntent(String action, String params) {
		Intent intent = new Intent();
		intent.setAction(LewaLoginActivity.JSI_INTERFACE_INTENT);
		intent.putExtra("action", action);
		intent.putExtra("params", params);
		lewaLoginActivity.sendBroadcast(intent);
	}

	/**
	 * @param activity
	 */
	public void navigate(String activity) {
		if (activity.contains("register")) {
			lewaLoginActivity.startActivityForResult(new Intent(lewaLoginActivity, LewaSignupActivity.class), 0);
		}
	}
}