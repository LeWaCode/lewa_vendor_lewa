package com.lewa.store.items;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.APISecurityHelper;
import com.lewa.core.base.LewaComponentCache;
import com.lewa.core.base.LewaContentDAO;
import com.lewa.core.base.LewaUser;
import com.lewa.core.base.StorePackage;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LewaBasketHelper {
	
	public static final String TAG = "LewaBasketHelper";
	
	public static final int RESULT_OK = 1;
	public static final int RESULT_FAIL = -1;
	
	private Context context = null;
	private LewaComponentCache components = null;
	private OnBasketResponseListener listener;
	private ResponseHandler responseHandler = new ResponseHandler();
	
	public LewaBasketHelper(Context context) {
		this.context = context;
		this.components = new LewaComponentCache(context);
	}
	
	public void setBasketResponseListener(OnBasketResponseListener listener) {
		this.listener = listener;
	}
	
	public void setPackageAsDownloaded(int packageIdInt) {
		
		Log.d(TAG, String.format("setting package %d as downloaded", packageIdInt));
		
		LewaUser user = LewaUser.getInstance(context);
		APIClient client = (APIClient) this.components.getComponent(LewaComponentCache.COMPONENT_CLIENT);
		APIDefinitions defs = (APIDefinitions) this.components.getComponent(LewaComponentCache.COMPONENT_DEFINITIONS);
		String url = defs.getApiURL(APIDefinitions.KEY_URI_STORE_PACKAGE_DOWNLOAD_COMPLETED, false);
		APIRequest request = client.getNewRequest();
		
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		
		//Log.d(TAG, String.format("%s => %s", APIClient.PARAM_CID, params.getString(APIClient.PARAM_CID)));
		//Log.d(TAG, String.format("%s => %s", APIClient.PARAM_ACCESS_TOKEN, params.getString(APIClient.PARAM_ACCESS_TOKEN)));
		//Log.d(TAG, String.format("%s => %s", APIClient.PARAM_SIGNATURE, params.getString(APIClient.PARAM_SIGNATURE)));
		
		url = String.format(url, packageIdInt);	// url requires package id in structure
		request.setRequestId(APIDefinitions.KEY_URI_STORE_PACKAGE_DOWNLOAD_COMPLETED);
		request.setParams(params);
		request.setURL(url);
		client.makeThreadedTypedRequest(request, APIClient.REQUEST_TYPE_POST, this.responseHandler);
	}
	
	public void getBasket() {
		
		Log.d(TAG, "loading basket");
		
		// prepare components
		LewaUser user = LewaUser.getInstance(context);
		APIClient client = (APIClient) this.components.getComponent(LewaComponentCache.COMPONENT_CLIENT);
		APIDefinitions defs = (APIDefinitions) this.components.getComponent(LewaComponentCache.COMPONENT_DEFINITIONS);
		String url = defs.getApiURL(APIDefinitions.KEY_URI_STORE_PACKAGE_DOWNLOAD_LIST, false);
		APIRequest request = client.getNewRequest();
		
		// do the request
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		
		//Log.d(TAG, String.format("%s => %s", APIClient.PARAM_CID, params.getString(APIClient.PARAM_CID)));
		//Log.d(TAG, String.format("%s => %s", APIClient.PARAM_ACCESS_TOKEN, params.getString(APIClient.PARAM_ACCESS_TOKEN)));
		//Log.d(TAG, String.format("%s => %s", APIClient.PARAM_SIGNATURE, params.getString(APIClient.PARAM_SIGNATURE)));
		
		request.setParams(params);
		request.setURL(url);
		request.setRequestId(APIDefinitions.KEY_URI_STORE_PACKAGE_DOWNLOAD_LIST);
		client.makeThreadedGetRequest(request, this.responseHandler);
	}
	
	private void postbackResultException(APIException e) {
		this.listener.onBasketPackageDownloadCompletionFailure(e);
	}
	
	/**
	 * prepares the raw json data, prior to passing the items back to front-end
	 * 
	 * @param json
	 */
	private void postbackBasket(JSONObject json) {
		LewaContentDAO dao = (LewaContentDAO) this.components.getComponent(LewaComponentCache.COMPONENT_DAO);
		APIException error = null;
		StorePackage[] packages = null;
		boolean parsingError = false;
		JSONArray jsonArray = null;
		try {
			jsonArray = json.getJSONArray(APIClient.PAYLOAD_PACKAGES);
		} catch (JSONException e) {
			error = new APIException(e);
			parsingError = true;
		}
		if (jsonArray != null) {
			int length = jsonArray.length();
			Log.d(TAG, String.format("parsing %d packages", length));
			packages = new StorePackage[length];
			//this.initializeDatabase();
			int size=jsonArray.length();
			for (int i = 0; i < size; i++) {
				try {
					/*
					JSONObject jsonPackage = jsonArray.getJSONObject(i);
					long packageId = jsonPackage.getLong("id");
					StorePackage item = record.newEntity(StorePackage.class);
					List<StorePackage> results = item.findByColumn(StorePackage.class, "pkg", Long.toString(packageId));
					if (!results.isEmpty()) {
						Log.d(TAG, "using existing item");
						item = results.get(0);
					} else {
						Log.d(TAG, "new item");
						item.setProperties(jsonPackage.toString());
						item.save();
					}
					packages[i] = item;
					Log.d(TAG, String.format("saved store item %d", packageId));
					*/
					JSONObject jsonPackage = jsonArray.getJSONObject(i);
					//long packageId = jsonPackage.getLong("id");
					StorePackage sPackage = new StorePackage(dao);
					sPackage.setProperties(jsonPackage.toString());
					String echo = sPackage.save();
					//Log.d(TAG, "saved: " + echo);
					packages[i] = sPackage;
				} catch (JSONException e) {
					Log.w(TAG, "store package problems");
					error = new APIException(e);
					parsingError = true;
				} //catch (ActiveRecordException e) {
					//Log.e(TAG, "failed to save package to db");
					//parsingError = true;
				//}
			}
			//record.close();
		}
		if (parsingError) {
			this.listener.onBasketListFailure(error);
			return;
		}
		Log.d(TAG, String.format(">>>>>>>>>>>>>> returning %d packages", packages.length));
		this.listener.onBasketListSuccess(packages);
	}
	
	public void postbackBasketException(APIException e) {
		this.listener.onBasketListFailure(e);
	}
	
	/**
	 * implementable interface for response data... contains all of the callbacks needed to exchange http data
	 * 
	 * @author vchoy
	 *
	 */
	public interface OnBasketResponseListener {
		public void onBasketListSuccess(StorePackage[] storePackages);
		public void onBasketListFailure(APIException e);
		public void onBasketPackageDownloadCompletionSuccess(boolean completed);
		public void onBasketPackageDownloadCompletionFailure(APIException e);
	}
	
	/**
	 * response handler for threaded http requests
	 * 
	 * @author vchoy
	 */
	public class ResponseHandler extends Handler {
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
				postbackBasket(json);
				return;
			}
		}
	}
	
	/*
	private static final String DB_NAME = "store_packages";
	private static final int DB_VERSION = 1;
	private ActiveRecordBase record;
	private void initializeDatabase() {
		DatabaseBuilder builder = new DatabaseBuilder(DB_NAME);
		builder.addClass(StorePackage.class);
		Database.setBuilder(builder);
		try {
			record = ActiveRecordBase.open(context, DB_NAME, DB_VERSION);
		} catch (ActiveRecordException e) {
			throw new RuntimeException("failed to initialize ActiveRecord");
		}
	}
	
	public ActiveRecordBase activeRecord() {
		return record;
	}
	*/
}
