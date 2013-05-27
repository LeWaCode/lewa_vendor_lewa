package com.lewa.pond.auth;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.lewa.core.base.APISecurityHelper;
import com.lewa.core.base.LewaUser;
import com.lewa.core.util.LewaUtils;

/**
 * Android-Javascript binding
 * 
 * TODO: Abstract, since both LewaLoginActivity and LewaSignupActivity duplicate code
 * 
 * @author vchoy
 *
 */
public class LewaSignupJavascriptInterface {
	
	public static final String TAG = "LewaSignupJavascriptInterface";
    
	public static final String EVENT_JS_READY = "com.lewa.pond.auth.LewaSignupJavascriptInterface.EVENT_JS_READY";
	
	private LewaSignupActivity lewaSignupActivity;
	private String temporarySecretToken = "";
	
    public LewaSignupJavascriptInterface(LewaSignupActivity lewaSignupActivity) {
    	this.lewaSignupActivity = lewaSignupActivity;
    }

    public String getRegistrationState() {
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
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
		Log.d(TAG, "getRegistrationState: " + stringState);
		return stringState;
	}
    
    /**
     * Signature and secret required to discourage non-Lewa clients from registration
     * 
     * @return String (JSON)
     */
    public String getSignupParameters() {
    	String signature = "";
    	temporarySecretToken = lewaSignupActivity.lewaAuthService.makeNewSecretToken();
    	Bundle bundleParams = lewaSignupActivity.lewaAuthService.getDeviceInfo();
    	bundleParams.putString("secret", temporarySecretToken);
    	signature = APISecurityHelper.makeSignature(bundleParams);
    	bundleParams.putString("signature", signature);
    	JSONObject jsonParams = LewaUtils.bundleToJSON(bundleParams);
    	String params = jsonParams.toString();
    	return params;
    }
    
    public String getLoginParameters() {
    	return "";
    }
    
    public void setUserEmail(String userEmail) {
    	Log.d(TAG, "e: " + userEmail);
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
    	user.setEmailAddressNew(userEmail);
    }
    
    public String setRegToken(String regToken) {
    	Log.d(TAG, "r: " + regToken);
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
    	lewaSignupActivity.openRegConfirm();
    	String echo = lewaSignupActivity.lewaAuthService.setRegToken(regToken);
    	lewaSignupActivity.lewaAuthService.setNewSecretToken(temporarySecretToken);
    	user.setRegistrationToken(regToken);
    	lewaSignupActivity.lewaAuthService.getRemoteAccessToken();
    	return echo;
    }
    
    public String setAccessToken(String accessToken) {
    	Log.d(TAG, "a: " + accessToken);
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
    	String echo = lewaSignupActivity.lewaAuthService.setAccessToken(accessToken);
    	user.setAccessToken(accessToken);
    	return echo;
    }
    
    public void setUserID(String userID) {
    	Log.d(TAG, "u: " + userID);
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
    	lewaSignupActivity.lewaAuthService.setUserID(userID);
		user.setUserID(userID);
	}
    
    public String setClientID(String clientID) {
    	Log.d(TAG, "c: " + clientID);
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
    	lewaSignupActivity.lewaAuthService.setClientID(clientID);
    	user.setClientID(clientID);
    	String echo = lewaSignupActivity.lewaAuthService.setClientID(clientID);
    	return echo;
    }
    
    public void setClientKey(String clientKey) {
    	Log.d(TAG, "client: " + clientKey);
    	LewaUser user = LewaUser.getInstance(lewaSignupActivity);
    	user.setThirdPartyToken(clientKey);
    	lewaSignupActivity.lewaAuthService.setThirdPartyToken(clientKey);
    }
    
    public void setUserSecret(String userSecret) {
    	//
    }
    
    public void hideDialog() {
    	lewaSignupActivity.sendBroadcast(new Intent(EVENT_JS_READY));
    }
    
    public void log(String message) {
    	Log.i("JS -> ", message);
    }
    
    public void raiseIntent(String action) {
		//
	}

	public void raiseIntent(String action, String params) {
		//
	}
    
    /**
     * @param activity
     */
    public void navigate(String activity) {}
}