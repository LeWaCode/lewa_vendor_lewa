package com.lewa.store.items;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.APISecurityHelper;
import com.lewa.core.base.DeviceInfo;
import com.lewa.core.base.LewaComponentCache;
import com.lewa.core.base.LewaContentDAO;
import com.lewa.core.base.LewaUser;
import com.lewa.core.base.StorePackage;
import com.lewa.store.push.LewaPushMessageInterface;
import com.lewa.store.push.LewaPushMessageInterface.LewaPushMessageSubscriberInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Checks for updates against remote store server
 * 
 * @author vchoy
 *
 */
public class LewaStoreUpdatesChecker {

	public static final String TAG = "LewaStoreUpdatesChecker";
	
	private Context context = null;
	private OnUpdatesResponseListener listener = null;
	private LewaComponentCache components = null;
	private ResponseHandler responseHandler = new ResponseHandler();
	
	public LewaStoreUpdatesChecker(Context context) {
		
		this.context = context;
		this.components = LewaComponentCache.getInstance(context);
	}
	
	public void setListener(OnUpdatesResponseListener listener) {
		
		// push message interface will also use the postbacks so that store objects only need this one helper class
		this.listener = listener;
	}
	
	public void getUpdates(String[] fqns, String[] versionCodes) throws InputMismatchException, OnUpdatesResponseListenerNotSetException {
		
		Log.d(TAG, "loading updates");
		
		// prepare components
		LewaUser user = LewaUser.getInstance(context);
		APIClient client = (APIClient) this.components.getComponent(LewaComponentCache.COMPONENT_CLIENT);
		APIDefinitions defs = (APIDefinitions) this.components.getComponent(LewaComponentCache.COMPONENT_DEFINITIONS);
		String url = defs.getApiURL(APIDefinitions.KEY_URI_STORE_PACKAGE_VERSIONS_LIST, false);
		APIRequest request = client.getNewRequest();
		DeviceInfo device = (DeviceInfo) this.components.getComponent(LewaComponentCache.COMPONENT_DEVICE);
		
		// do some checks
		if (this.listener == null) throw new OnUpdatesResponseListenerNotSetException();
		if (fqns.length != versionCodes.length) throw new InputMismatchException(fqns, versionCodes);
		
		// build the json-encoded package list
		JSONArray jsonArray = new JSONArray();
		int i = 0;
		while (i < fqns.length) {
			JSONObject node = new JSONObject();
			try {
				node.put("fqn", fqns[i]);
				node.put("vc", versionCodes[i]);
				jsonArray.put(node);
			} catch (JSONException e) {
				Log.w(TAG, String.format("failed to set data for node with data - fqn: %s -> version code: %s", fqns[i], versionCodes[i]));
			}
			i++;
		}
		
		// package the request data
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_IMEI, device.getDeviceInfo().getString(DeviceInfo.KEY_IMEI));		
		params.putString(APIClient.PARAM_SERIALIZED_PACKAGES, jsonArray.toString());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		
		// do the request
		request.setParams(params);
		request.setURL(url);
		request.setRequestId(APIDefinitions.KEY_URI_STORE_PACKAGE_VERSIONS_LIST);
		client.makeThreadedGetRequest(request, this.responseHandler);
	}
	
	public interface OnUpdatesResponseListener {
		public void onFetchUpdatesSuccess(StorePackage[] storePackages);
		public void onFetchUpdatesFailure(APIException e);
		public void onFetchSystemMessage(String message);
	}
	
	private void postbackUpdates(JSONObject json) {
		LewaContentDAO dao = (LewaContentDAO) this.components.getComponent(LewaComponentCache.COMPONENT_DAO);
		StorePackage[] packages = null;
		try {
			if (json.has(APIClient.PAYLOAD_PACKAGES)) {
				JSONArray packagesArray = json.getJSONArray(APIClient.PAYLOAD_PACKAGES);
				int packageCount = packagesArray.length();
				packages = new StorePackage[packageCount];
				int i = 0;
				while (i < packageCount) {
					JSONObject jsonPackage = packagesArray.getJSONObject(i);
					StorePackage sPackage = new StorePackage(dao);
					sPackage.setProperties(jsonPackage.toString());
					// dont save, unless the packages will be extracted from LewaContentDAO
					//String echo = sPackage.save();
					packages[i] = sPackage;
					i++;
				}
			}
		} catch (JSONException e) {
			Log.w(TAG, "postbackResult() - unexpected structured response");
			this.listener.onFetchUpdatesFailure(new APIException(e));
		}
		this.listener.onFetchUpdatesSuccess(packages);
	}
	
	private void postbackResultException(APIException e) {
		this.listener.onFetchUpdatesFailure(e);
	}
	
	public class ResponseHandler extends Handler {
		public static final String TAG = "LewaStoreUpdatesChecker.ResponseHandler";
		public void handleMessage(Message message) {
			Log.d(TAG, ">>>>>>> handling message");
			APIResponse response = (APIResponse) message.obj;
			JSONObject json = response.getResponseJSON();
			try {
				APIException.sniffResponse(json);
			} catch (APIException e) {
				Log.e(TAG, "APIException: " + response.getRequestId());
				e.setRawResponse(response.getResponse());
				postbackResultException(e);
				return;
			}
			if (json.has(APIClient.PAYLOAD_PACKAGES)) {
				Log.d(TAG, "packages in payload");
				postbackUpdates(json);
				return;
			}
		}
	}
	
	public class InputMismatchException extends Exception {
		private static final long serialVersionUID = 1L;
		public static final String TAG = "InputMismatchException";
		
		private String message = "";
		
		public InputMismatchException(String[] fqns, String[] versionCodes) {
			this.message = String.format(
				"FQN count: %d, version code count: %d\nNumber of values are mismatched", 
				fqns.length, versionCodes.length
			);
		}
		public String getMessage() {
			return this.message;
		}
	}
	
	public class OnUpdatesResponseListenerNotSetException extends Exception {
		private static final long serialVersionUID = 1L;
		public static final String TAG = "InputMismatchException";
		
		private String message = "OnUpdatesResponseListener not set. Set the listener prior to getUpdates()";
		
		public OnUpdatesResponseListenerNotSetException() {}
		public String getMessage() {
			return this.message;
		}
	}
}
