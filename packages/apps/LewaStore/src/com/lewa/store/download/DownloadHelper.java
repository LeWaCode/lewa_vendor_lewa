package com.lewa.store.download;

import com.lewa.core.base.APIClient;
import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIRequest;
import com.lewa.core.base.APISecurityHelper;
import com.lewa.core.base.LewaComponentCache;
import com.lewa.core.base.LewaContentDAO;
import com.lewa.core.base.LewaUser;
import com.lewa.core.base.StorePackage;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class DownloadHelper {
	
	public static final String TAG = "DownloadHelper";
	
	private Context context;
	private LewaComponentCache cache;
	
	public DownloadHelper(Context context) {
		this.context = context;
		this.cache = new LewaComponentCache(this.context);
	}
	
	/**
	 * 
	 * @param packageIdInt	123
	 * @return
	 */
	public String getStoreItemURL(int packageIdInt) {
		APIClient client = (APIClient) cache.getComponent(LewaComponentCache.COMPONENT_CLIENT);
		APIRequest request = client.getNewRequest();
		APIDefinitions defs = (APIDefinitions) cache.getComponent(LewaComponentCache.COMPONENT_DEFINITIONS);
		String url = defs.getApiURL(APIDefinitions.KEY_URI_STORE_DOWNLOAD, false);
		StorePackage item = this.getPackageById(packageIdInt);
		url = String.format(url, packageIdInt, item.getType());
		Bundle params = new Bundle();
		LewaUser user = LewaUser.getInstance(this.context);
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
//		Log.e(TAG,"makeSignature()="+APISecurityHelper.makeSignature(params));
		request.setURL(url);
		request.setParams(params);
		String fullUrl = request.getFullURL();
//		Log.e(TAG,"fullUrl="+fullUrl);
		return fullUrl;
	}
	
	/**
	 * 
	 * @param packageIdInt	123
	 * @return
	 */
	public String getStorePackageURL(int packageIdInt) {
		APIClient client = (APIClient) cache.getComponent(LewaComponentCache.COMPONENT_CLIENT);
		APIRequest request = client.getNewRequest();
		APIDefinitions defs = (APIDefinitions) cache.getComponent(LewaComponentCache.COMPONENT_DEFINITIONS);
		String url = defs.getApiURL(APIDefinitions.KEY_URI_STORE_DOWNLOAD, false);
		StorePackage sPackage = new StorePackage((LewaContentDAO) cache.getComponent(LewaComponentCache.COMPONENT_DAO));
		url = String.format(url, packageIdInt, sPackage.getType());
		Bundle params = new Bundle();
		LewaUser user = LewaUser.getInstance(this.context);
		params.putString(APIClient.PARAM_CID, user.getClientID());
		params.putString(APIClient.PARAM_ACCESS_TOKEN, user.getAccessToken());
		params.putString(APIClient.PARAM_SIGNATURE, APISecurityHelper.makeSignature(params));
		request.setURL(url);
		request.setParams(params);
		String fullUrl = request.getFullURL();
		return fullUrl;
	}
	
	private StorePackage getPackageById(int packageIdInt) {
		LewaContentDAO dao = (LewaContentDAO) this.cache.getComponent(LewaComponentCache.COMPONENT_DAO);
		StorePackage item = new StorePackage(dao);
		String itemString = dao.getData(LewaContentDAO.DATA_SOURCE_CACHE, String.format(LewaContentDAO.FIELD_STORE_PACKAGE, packageIdInt));
		item.setProperties(itemString);
		return item;
	}
}
