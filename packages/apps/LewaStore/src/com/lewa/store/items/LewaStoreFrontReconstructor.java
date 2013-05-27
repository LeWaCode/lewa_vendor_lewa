package com.lewa.store.items;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APIResponse;
import com.lewa.core.base.APISecurityHelper;
import com.lewa.core.base.LewaContentDAO;
import com.lewa.core.base.StorePackage;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * DAO for LewaStoreFront. Will get/set store front data from/into database, remote server, etc.
 * 
 * @author vchoy
 *
 */
public class LewaStoreFrontReconstructor {
	
	public static final String TAG = "LewaStoreFrontReconstructor";
	
	protected Context context;
	private LewaContentDAO dao;
	private APIClient client;
	private Bundle metaData;
	private APISecurityHelper sec;
	private APIDefinitions defs;
	private LewaStoreFrontInterface listener;
	
	private String lastLocalHash = "";
	private String lastRemoteHash = "";
	private boolean lastHashesMatched = false;
	private boolean lastDataParsed = false;
	
	public interface LewaStoreFrontInterface {
		public void onReconstructSuccess(StorePackage[] items);
		public void onReconstructFailure(APIException exception);
	}
	
	public LewaStoreFrontReconstructor(Context context) throws IOException{
		try {
			this.metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e1) {
			//
		}
		this.context = context;
		this.dao = LewaContentDAO.getInstance(context);
		this.sec = APISecurityHelper.getInstance(dao, metaData);
		this.client = APIClient.getInstance(context);
		this.defs = APIDefinitions.getInstance(client, dao, metaData);
		APIRequest request = new APIRequest(sec);
		defs.getDefinitions(request, false);
	}
	
	/**
	 * Populates memory with StoreFront data from either remote, cache, or persistent db
	 * 
	 * @throws APIException
	 * @throws IOException
	 */
	public void reconstruct(LewaStoreFrontInterface listener) throws APIException, IOException {
		this.listener = listener;
		String localStoreFrontHash = dao.getData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_STORE_FRONT_HASH);		
		APIRequest request = client.getNewRequest();
		Bundle params = new Bundle();
		params.putString(APIClient.PARAM_HASH, localStoreFrontHash);
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(defs.getApiURL(APIDefinitions.KEY_URI_STORE_FRONT, false));
		request.setParams(params);
		client.makeThreadedGetRequest(request, handler);
	}
	
	private StorePackage[] parseItems(JSONArray jsonItems) {
		StorePackage[] items = new StorePackage[jsonItems.length()];
		int length=jsonItems.length();
		for (int i = 0; i < length; i++) {
			try {
				StorePackage item = new StorePackage(dao);
				JSONObject jsonItem = jsonItems.getJSONObject(i);
				item.setProperties(jsonItem.toString());
				String echo = item.save();
				//Log.d(TAG, "echoing: " + echo);
				items[i] = item;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return items;
	}
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message message) {
			StorePackage[] items = null;
			String remoteStoreFrontHash = "";
			String localStoreFrontString = dao.getData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_STORE_FRONT_DATA);
			String localStoreFrontHash = dao.getData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_STORE_FRONT_HASH);
			if (localStoreFrontString == null) {
				localStoreFrontString = "";	// TODO: fixme. Sorry about this part... very ugly
			}
			if (localStoreFrontHash == null) {
				localStoreFrontHash = "";
			}
			
			JSONArray localStoreFrontJSON = null;
			
			APIResponse response = (APIResponse) message.obj;
			JSONObject responseJSON = response.getResponseJSON();
			try {
				APIException.sniffResponse(responseJSON);
			} catch (APIException e) {
				listener.onReconstructFailure(e);
				return;
			}
			try {
				remoteStoreFrontHash = responseJSON.getString(APIClient.PARAM_HASH);
			} catch (JSONException e) {
				Log.e(TAG, String.format("store front hash key: %s does not exist in response root node... big problem", APIClient.PARAM_HASH));
				APIException ae = new APIException(e);
				listener.onReconstructFailure(ae);
				return;
			}
			
			// make local and remote hashes available as class vars
			lastLocalHash = localStoreFrontHash;
			lastRemoteHash = remoteStoreFrontHash;
			
			boolean hashMatch = lastHashesMatched = localStoreFrontHash.equals(remoteStoreFrontHash);
			boolean shouldParse = lastDataParsed = localStoreFrontHash.equals("") || !hashMatch;
			Log.d(TAG, String.format("hashMatch? %s, shouldParse? %s", hashMatch, shouldParse));
			
			if (shouldParse) {
				try {
					JSONArray jsonItems = responseJSON.getJSONArray(APIClient.PARAM_ITEMS);
					items = parseItems(jsonItems);
					dao.setData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_STORE_FRONT_DATA, responseJSON.getJSONArray(APIClient.PARAM_ITEMS).toString());
					dao.setData(LewaContentDAO.DATA_SOURCE_CACHE, LewaContentDAO.FIELD_STORE_FRONT_HASH, remoteStoreFrontHash);
				} catch (JSONException e) {
					//
				}
			} else {
				if (localStoreFrontString == "") {
					throw new RuntimeException();
				}
				try {
					Log.d(TAG, "parse data from cache");
					localStoreFrontJSON = new JSONArray(localStoreFrontString);
					items = parseItems(localStoreFrontJSON);
				} catch (JSONException e) {}
			}
			listener.onReconstructSuccess(items);
		}
	};
	
	/**
	 * the last server hash that the client knows
	 * @return lastLocalHash
	 */
	public String getLastLocalHash() { return this.lastLocalHash; }
	
	/**
	 * the last hash that the server returned to this client
	 * @return lastRemoteHash
	 */
	public String getLastRemoteHash() { return this.lastRemoteHash; }
	
	/**
	 * a check to determine whether the last store front items request matched on hashes
	 * @return lastHashesMatched
	 */
	public boolean getLastHashedMatched() { return this.lastHashesMatched; }
	
	/**
	 * a check to determine whether the client parsed data returned in response
	 * @return
	 */
	public boolean getLastDataParsed() { return this.lastDataParsed; }
}
