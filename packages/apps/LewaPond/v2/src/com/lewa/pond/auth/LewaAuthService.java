package com.lewa.pond.auth;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.APISecurityHelper;
import com.lewa.core.base.DeviceInfo;
import com.lewa.core.base.LewaContentDAO;
import com.lewa.core.base.LewaUser;
import com.lewa.core.threads.AccessTokenThread;
import com.lewa.core.threads.IThread;
import com.lewa.core.util.ACounter;
import com.lewa.core.util.ExponentialCounter;
import com.lewa.core.util.FibonacciCounter;
import com.lewa.core.util.LewaUtils;
import com.lewa.pond.R;
import com.lewa.pond.account.LewaAccountAuthenticatorActivity;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * A local service, which allows clients to invoke methods on this service directly, or via broadcasts. Handles
 * all internal data processing involved for registration and login
 * 
 * @author Victor Choy (c) LewaTek
 * 
 */
public class LewaAuthService extends Service {
	
	public static final String TAG = "LewaAuthService";
	
	public static final String ACTION_AUTH_SERVICE_BOUND = "com.lewa.pond.auth.LewaAuthService.service_bound";
	public static final String ACTION_ACCESS_TOKEN_SET = "com.lewa.pond.auth.LewaAuthService.access_token_set";
	public static final String LEWA_AUTH_EVENT = "com.lewa.auth.event";	// never change the string value...
	
	public static final String NOTIFICATION_PRE_REG = "pre-registration";
	public static final String NOTIFICATION_POST_REG = "post-registration";
	public static final int N_REGISTRATION_UNCONFIRMED_ID = 1;
	public static final int N_REGISTRATION_CONFIRMED_ID = 2;
	
	public static final int MAX_RETRIES_PASSWORD_RESET = 6;
	public static final int MAX_RETRIES_RESEND_VERIFICATION = 6;
	
	protected IBinder binder = null;
	protected APIClient client = null;
	protected APIDefinitions defs = null;
	protected APISecurityHelper security = null;
	protected LewaContentDAO pdm = null;
	protected LewaAuthServiceBroadcastReceiver bcr = null;
	protected Bundle metaData = null;
	protected DeviceInfo device = null;
	protected NotificationManager notificationManager = null;
	protected PendingIntent contentIntent = null;
	protected Notification notification = null;
	
	protected HashMap<String,com.lewa.core.threads.IThread> threads = null;
	
	
	protected HashMap<String,Integer> notificationIds = new HashMap<String,Integer>();
	
	@Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
	
	public class LewaAuthBinder extends Binder {
        public LewaAuthService getService() {
        	Intent intent = new Intent();
        	intent.setAction(ACTION_AUTH_SERVICE_BOUND);
        	sendBroadcast(intent);
            return LewaAuthService.this;
        }
    }
	
	/**
	 * The broadcast receiver for this service
	 * 
	 */
	public class LewaAuthServiceBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
	        //Bundle bundle = intent.getExtras();
			if (action.equals(APIClient.ACTION_RETURN_ACCESS_TOKEN)) {
				setAccessToken(intent.getExtras().getString(APIClient.PARAM_ACCESS_TOKEN));
				createNotification(N_REGISTRATION_CONFIRMED_ID, context.getString(R.string.dialog_logged_in), "");
			    Log.d(TAG,"have access token.");
			}
			else if (action.equals(LEWA_AUTH_EVENT)) {
				Log.d(TAG, String.format("registration event check... received broadcast OK (%s)", LEWA_AUTH_EVENT));
			}
		}
	}
	
	/**
	 * This method was to trigger post-login/post-registration hooks for all 3rd-party client applications (ie. Hudee Butterfly, Labi, etc.)
	 * It is no longer needed, since now, we can create additional apps, which listen for Broadcasts with action: LEWA_AUTH_EVENT
	 * 
	 * @deprecated
	 * 
	 * TODO: safe to remove?
	 */
	public void runThirdPartyProcesses() {
		//this.thirdPartyManager.connect(pdm.getData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_CLIENT_ID));
	}
	
	/**
	 * 
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		threads = new HashMap<String,IThread>();
		binder = new LewaAuthBinder();
		bcr = new LewaAuthServiceBroadcastReceiver();
		device = new DeviceInfo(this);
		pdm = LewaContentDAO.getInstance(this);
		try {
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(this.getPackageName(), PackageManager.GET_META_DATA);
			metaData = applicationInfo.metaData;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		security = APISecurityHelper.getInstance(pdm, metaData);
		client = APIClient.getInstance(this);
		defs = APIDefinitions.getInstance(client, pdm, metaData);
		IntentFilter filter = new IntentFilter();
		// filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);		// no longer needed, since we are not intercepting SMS's
		filter.addAction(APIClient.ACTION_RETURN_ACCESS_TOKEN);
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.addAction(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
		filter.addAction(LEWA_AUTH_EVENT);
        notificationIds.put(NOTIFICATION_PRE_REG, N_REGISTRATION_UNCONFIRMED_ID);
        notificationIds.put(NOTIFICATION_POST_REG, N_REGISTRATION_CONFIRMED_ID);
        this.registerReceiver(bcr, filter);
	}
	
	/**
	 * 
	 */
	@Override
	public void onDestroy() {
		this.unregisterReceiver(bcr);
		this.bcr = null;
		this.client = null;
		this.defs = null;
		this.device = null;
		this.metaData = null;
		this.notification = null;
		this.notificationManager = null;
		this.pdm = null;
		this.security = null;
		this.threads = null;
		this.binder = null;
		super.onDestroy();
		System.gc();
	}
	
	/**
	 * Loads remote API endpoints (URL)
	 */
	public void loadDefinitions() {
		if(LewaUtils.checkNetworkInfo(this)!=0){
			APIRequest request = new APIRequest(security);
			defs.getDefinitions(request, false);
		}else{
			Log.d(TAG,"no internet");
		}
	}
	
	/**
	 * Creates a notification update in notification status bar
	 * 
	 * @param notificationID	int
	 * @param title				String
	 * @param message			String
	 */
	public void createNotification(int notificationId, String title, String message) {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(notificationId);
		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		Intent intent = null;
		// this user is not confirmed... click-through action is to open mail
		if (notificationId == N_REGISTRATION_UNCONFIRMED_ID) {
			LewaUser user = LewaUser.getInstance(getApplicationContext());
			String emailDomain = LewaUtils.extractEmailAddressDomain(user.getEmailAddressNew());
			intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://mail.%s", emailDomain)));
			
		}
		// user is logged in, click-through action if accounts & sync
		else if (notificationId == N_REGISTRATION_CONFIRMED_ID) {
			intent = new Intent(this, LewaAccountAuthenticatorActivity.class);	// TODO: change this! maybe using context.class is better... somehow, figure out a better way
		}
		contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notification = new Notification(icon, getString(R.string.app_name), when);
		notification.setLatestEventInfo(this, title, message, contentIntent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(notificationId, notification);
	}
	
	/**
	 * Kills a previously-created status bar notification
	 * 
	 * @param notificationType	String
	 */
	public void removeNotification(String notificationType) {
		if (this.notificationIds.containsKey(notificationType)) {
			int notificationId = this.notificationIds.get(notificationType);
			if (notificationManager == null) notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notificationManager.cancel(notificationId);
		}
	}
	
	/**
	 * Helper method which returns an instance of APIDefinitions class
	 * 
	 * @return defs	APIDefinitions
	 */
	public APIDefinitions getDefinitionsInstance() {
		return defs;
	}
	
	/**
	 * sets the permanent access token in store
	 * 
	 * TODO: research alternate ways to store this key somewhere
	 * 
	 * @param accessToken	String
	 * @return echo			String
	 */
	public String setAccessToken(String accessToken) {
		LewaUser user = LewaUser.getInstance(this);
		user.setAccessToken(accessToken);
		user.setRegistrationToken("");
		user.save(TAG);
		String echo = user.getAccessToken();
		if (threads.containsKey(AccessTokenThread.TAG))	// check to avoid NullPointerException. This can happen if setting access token from login
			threads.get(AccessTokenThread.TAG).terminate(TAG + ".setAccessToken()");
		Intent authIntent = new Intent();
		authIntent.setAction(LEWA_AUTH_EVENT);
		authIntent.putExtra(APIClient.PARAM_CID, user.getClientID());
        sendBroadcast(authIntent);
		removeNotification(NOTIFICATION_PRE_REG);
		Intent accessIntent = new Intent();
		accessIntent.setAction("access-token-set");
		sendBroadcast(accessIntent);
		runThirdPartyProcesses();
		addAndroidAccount();
		return echo;
	}
	
	/**
	 * Fetches the client's access token from the remote server (given that the client has a valid
	 * "reg_token", which is used to swap for a valid "access_token")
	 * 
	 */
	public void getRemoteAccessToken() {
		Log.d(TAG, "getRemoteAccessToken()");
		try {
			if (threads.containsKey(AccessTokenThread.TAG))	{
				Log.d(TAG, "removing old access token thread");
				threads.get(AccessTokenThread.TAG).terminate(TAG + ".getRemoteAccessToken()");
				threads.remove(AccessTokenThread.TAG);
			} else {
				Log.d(TAG, "could not find old access token thread");
			}
			AccessTokenThread thread = new AccessTokenThread(this, device, metaData, pdm, defs, client);
			ACounter counter = null;
			int backoffMin = metaData.getInt(APIClient.METADATA_BACKOFF_MIN_MS);
			int backoffMax = metaData.getInt(APIClient.METADATA_BACKOFF_MAX_MS);
			if (metaData.getString("api-counter").equals("fib")) counter = new FibonacciCounter(backoffMin, backoffMax);
			else if (metaData.getString("api-counter").equals("exp")) counter = new ExponentialCounter(backoffMin, backoffMax);
			thread.setCounter(counter);
			thread.enable();
			thread.start();
			threads.put(AccessTokenThread.TAG, thread);
			Log.d(TAG, "set new thread");
		} catch (IOException e) {
			Intent intent = new Intent();
			intent.setAction(APIClient.ERROR_INTERNET_CONNECTIVITY);
			sendBroadcast(intent);
		}
		this.createNotification(N_REGISTRATION_UNCONFIRMED_ID, getString(R.string.label_almost_done), getString(R.string.dialog_check_your_email));
	}
	
	/**
	 * Authenticates user's password (input), to allow them to perform secure operations, such
	 * as access "My Account", make purchases, etc.
	 * 
	 * TODO: refactor... put behind a threaded request
	 * 
	 * @param user		LewaUser
	 * @return email	String
	 * @throws IOException
	 * @throws APIException 
	 */
	public String checkPassword(LewaUser user) throws APIException, IOException {
		String email = "";
		JSONObject jsonResponse = null;
		APIRequest request = client.getNewRequest();
		APIResponse response = client.getNewResponse();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_PASSWORD, user.getPasswordOriginal());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(defs.getApiURL(APIDefinitions.KEY_URI_CHECK_PASSWORD, true));	// TODO: change to https later!
		request.setParams(params);
		try {
			response = client.makeGetRequest(request);
			jsonResponse = response.getResponseJSON();
			APIException.sniffResponse(jsonResponse);
			if (jsonResponse.has(APIClient.PARAM_OK)) {
				if (jsonResponse.getBoolean(APIClient.PARAM_OK) == true && jsonResponse.has(APIClient.PAYLOAD_USER)) {
					JSONObject jsonUser = jsonResponse.getJSONObject(APIClient.PAYLOAD_USER);
					email = jsonUser.getString(APIClient.PAYLOAD_USER_EMAIL);
				}
			}
		} catch (JSONException e) {
			throw new APIException(e);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return email;
	}
	
	/**
	 * Sends an activation/confirmation email
	 * 
	 * TODO: refactor... put behind a threaded request
	 * 
	 * @param email			string
	 * @return jsonResponse	JSONObject
	 * @throws IOException 
	 */
	public JSONObject sendActivationEmail(String email) throws IOException {
		JSONObject jsonResponse = null;
		APIRequest request = client.getNewRequest();
		APIResponse response = client.getNewResponse();
		Bundle params = new Bundle();
		String cid = this.pdm.getData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_CLIENT_ID);
		params.putString(APIClient.PARAM_CID, cid);
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(defs.getApiURL(APIDefinitions.KEY_URI_ACCESS_TOKEN, false));	// TODO: change to https later!
		request.setParams(params);
		try {
			response = client.makeGetRequest(request);
		} catch (IOException e) {
			e.printStackTrace();
		}
		jsonResponse = response.getResponseJSON();
		return jsonResponse;
	}
	
	/**
	 * Allows a user to have our servers send another confirmation email
	 * 
	 * @param user		LewaUser
	 * @return resent		boolean
	 * @throws IOException
	 * @throws APIException 
	 */
	public boolean resendActivationEmail(Handler handler, LewaUser user) {
		boolean resent = true;
		String url = this.defs.getApiURL(APIDefinitions.KEY_URI_RESEND_CONFIRMATION_EMAIL, false);
		APIRequest request = client.getNewRequest();
		APIResponse response = client.getNewResponse();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_EMAIL, user.getEmailAddress());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(url);
		request.setParams(params);
		request.setRequestId("resendActivationEmail");
		client.makeThreadedTypedRequest(request, APIClient.REQUEST_TYPE_PUT, handler, MAX_RETRIES_RESEND_VERIFICATION);
		return resent;
	}
	
	/**
	 * sends an reset password email (device not linked yet)
	 * 
	 * @param handler		Handler
	 * @param email			string
	 * @return reset		boolean
	 * @throws IOException 
	 * @throws APIException 
	 */
	public boolean resetPassword(Handler handler, String email) throws IOException, APIException {
		boolean reset = true;
		String url = this.defs.getApiURL(APIDefinitions.KEY_URI_RESEND_PASSWORD, false);
		APIRequest request = client.getNewRequest();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_EMAIL, email.toLowerCase().trim().toLowerCase());
		request.setURL(url);
		request.setParams(params);
		request.setRequestId("resetPassword");
		client.makeThreadedTypedRequest(request, APIClient.REQUEST_TYPE_PUT, handler, MAX_RETRIES_PASSWORD_RESET);
		return reset;
	}
	
	/**
	 * Allows for changing of a user's email address
	 * 
	 * Required API call data points: cid, email, new_email, password, signature
	 * 
	 * @param user				LewaUser
	 * @return changedEmail		boolean
	 * @throws APIException 
	 * @throws IOException 
	 */
	public boolean changeEmail(LewaUser user) throws APIException, IOException {
		boolean changedEmail = false;
		String url = this.defs.getApiURL(APIDefinitions.KEY_URI_CHANGE_EMAIL, true);
		JSONObject jsonResponse = null;
		APIRequest request = client.getNewRequest();
		APIResponse response = client.getNewResponse();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_EMAIL, user.getEmailAddress());
		params.putString(APIClient.PARAM_EMAIL_NEW, user.getEmailAddressNew());
		params.putString(APIClient.PARAM_PASSWORD, user.getPasswordOriginal());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(url);
		request.setParams(params);
		response = client.makeTypedRequest(request, APIClient.REQUEST_TYPE_PUT);
		jsonResponse = response.getResponseJSON();
		APIException.sniffResponse(jsonResponse);
		if (jsonResponse.has(APIClient.PAYLOAD_USER)) {
			JSONObject jsonUser;
			try {
				jsonUser = jsonResponse.getJSONObject(APIClient.PAYLOAD_USER);
				user.setEmailAddress(jsonUser.getString(APIClient.PAYLOAD_USER_EMAIL));
			} catch (JSONException e) {
				throw new APIException(e);
			}
			if (user.getEmailAddress().equals(user.getEmailAddressNew())) {
				changedEmail = true;
			}
		}
		return changedEmail;
	}
	
	/**
	 * Allows changing of a user's password
	 * 
	 * @param user				LewaUser
	 * @return changedPassword	boolean
	 * @throws IOException 
	 */
	public boolean changePassword(LewaUser user) throws IOException {
		boolean changedPassword = false;
		String url = this.defs.getApiURL(APIDefinitions.KEY_URI_CHANGE_PASSWORD, true);
		JSONObject jsonResponse = null;
		APIRequest request = client.getNewRequest();
		APIResponse response = client.getNewResponse();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_PASSWORD, user.getPasswordOriginal());
		params.putString(APIClient.PARAM_PASSWORD_NEW, user.getPasswordNew());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(url);
		request.setParams(params);
		response = client.makeTypedRequest(request, APIClient.REQUEST_TYPE_PUT);
		jsonResponse = response.getResponseJSON();
		if (jsonResponse.has(APIClient.PAYLOAD_USER)) {
			changedPassword = true;
		}
		return changedPassword;
	}
	
	/**
	 * Allows for changing of account data
	 * 
	 * @param user					LewaUser
	 * @return updatedAccountInfo	boolean
	 * @throws APIException
	 * @throws IOException
	 */
	public boolean updateAccountInfo(LewaUser user) throws APIException, IOException {
		boolean updatedAccountInfo = false;
		String url = this.defs.getApiURL(APIDefinitions.KEY_URI_UPDATE_ACCOUNT_INFO, true);
		JSONObject jsonResponse = null;
		APIRequest request = client.getNewRequest();
		APIResponse response = client.getNewResponse();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_EMAIL, user.getEmailAddress());
		params.putString(APIClient.PARAM_EMAIL_NEW, user.getEmailAddressNew());
		params.putString(APIClient.PARAM_PASSWORD, user.getPasswordOriginal());
		params.putString(APIClient.PARAM_PASSWORD_NEW, user.getPasswordNew());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(url);
		request.setParams(params);
		response = client.makeTypedRequest(request, APIClient.REQUEST_TYPE_PUT);
		jsonResponse = response.getResponseJSON();
		APIException.sniffResponse(jsonResponse);
		if (jsonResponse.has(APIClient.PAYLOAD_USER)) {
			updatedAccountInfo = true;
		}
		return updatedAccountInfo;
	}
	
	/**
	 * Programmatically creates an Android account using AccountManager
	 * 
	 */
	public void addAndroidAccount() {
		Log.d(TAG, "addAccount()");
		LewaUser user = LewaUser.getInstance(this);
        String xEmailAddress = LewaUtils.replaceUserNameInEmail(user.getEmailAddress());
		if (xEmailAddress.equals("")) {
			xEmailAddress = "Lewa User";	// fail safe, so that it does not lock out the user (cannot add account, since email is blank)
		}
		String accountType = getString(R.string.ACCOUNT_TYPE);
		Account account = new Account(xEmailAddress, accountType.trim());
		Bundle userData = new Bundle();
		userData.putString("SERVER", xEmailAddress);
		AccountManager am = AccountManager.get(this);
		Account[] previousAccounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
		Log.d(TAG, String.format("number of accounts: %d", previousAccounts.length));
		if (previousAccounts.length > 0) {
			am.removeAccount(previousAccounts[0], null, null);
		}
		if(am.addAccountExplicitly(account, user.getSecretToken(), userData)){
			/*Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, xEmailAddress);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.ACCOUNT_TYPE));
			setAccountAuthenticatorResult(result);
			Log.d(TAG, String.format("set account data for: %s, account type: %s", xEmailAddress, accountType));*/
		}
	}
	
	/**
	 * setter for userId
	 * 
	 * @param userID	String
	 */
	public String setUserID(String userID) {
		pdm.setData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_USER_ID, userID);
		return userID;
	}
	
	/**
	 * setter for clientID
	 * 
	 * @param clientID	String
	 */
	public String setClientID(String clientID) {
		pdm.setData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_CLIENT_ID, clientID.toString());
		return clientID;
	}
	
	/**
	 * getter for clientID
	 * 
	 * @return clientID	String
	 */
	public String getClientID() {
		String clientID = pdm.getData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_CLIENT_ID);
		return clientID;
	}
	
	/**
	 * setter for registration token (a temporary token used to exchange for a permanent "access token" after
	 * the user has verified his/her email address (clicking on the email link)
	 * 
	 * @param regToken	String
	 * @return echo		String
	 */
	public String setRegToken(String regToken) {
		pdm.setData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_REG_TOKEN, regToken);
		String echo = pdm.getData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_REG_TOKEN);
		Intent intent = new Intent();
		intent.setAction("stored-reg-token");
		this.sendBroadcast(intent);
		return echo;
	}
	
	/**
	 * getter for registration token
	 * 
	 * @return regToken	String
	 */
	public String getRegToken() {
		String regToken = pdm.getData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_REG_TOKEN);
		return regToken;
	}
	
	/**
	 * setter for new secret token (secret token is used to create a request signature and is known only by the server and client)
	 * 
	 * @param newSecret	String
	 */
	public void setNewSecretToken(String newSecret) {
		security.saveNewSecretToken(newSecret);
	}
	
	/**
	 * creates a new secret token (but does not save it to DB, since it would overwrite an existing secret token)
	 * 
	 * @return secret	String
	 */
	public String makeNewSecretToken() {
		String secret = security.makeNewSecretToken();
		return secret;
	}
	
	/**
	 * returns a bundle containing device information
	 * 
	 * @return deviceInfo	Bundle
	 */
	public Bundle getDeviceInfo() {
		Bundle deviceInfo = device.getDeviceInfo();
		return deviceInfo;
	}
	
	/**
	 * sets a 3rd-party token, which LewaPond's servers create, to allow 3rd-party providers to access our resources (UUID)
	 * 
	 * @param thirdPartyToken	String
	 */
	public void setThirdPartyToken(String thirdPartyToken) {
		pdm.setData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_3RD_PARTY_TOKEN, thirdPartyToken);
	}
	
	/**
	 * getter for 3rd-party token
	 * 
	 * @return String
	 */
	public String getThirdPartyToken() {
		return pdm.getData(LewaContentDAO.DATA_SOURCE_DATABASE, LewaContentDAO.FIELD_3RD_PARTY_TOKEN);
	}
}
