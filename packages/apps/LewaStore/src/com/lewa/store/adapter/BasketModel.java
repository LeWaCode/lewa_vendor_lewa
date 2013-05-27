package com.lewa.store.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.store.model.AppInfo;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;

import android.content.Context;
import android.content.Intent;

/**
 * 处理Basket逻辑
 * 
 * @author ypzhu
 * 
 */
public class BasketModel {

	private Context context = null;
	private List<AppInfo> cloudList = null;

	public List<HashMap<String, Object>> systemAppPackagesMap = null;
	public PkgManager pkg = null;
	private Map<String, Integer> systemMap = null;

	public BasketModel(Context c, PkgManager p) {
		this.context = c;
		this.pkg = p;
		this.systemAppPackagesMap = pkg.getAppPackages();
	}

	public BasketModel(Context c, List<AppInfo> list, PkgManager p) {
		this.context = c;
		this.cloudList = list;
		this.pkg = p;
		this.systemAppPackagesMap = pkg.getAppPackages();
	}

	public Map<String, Integer> initSystemMap() {
		systemMap = new HashMap<String, Integer>();
		Map<String, Object> map = null;
		int length = systemAppPackagesMap.size();
		for (int i = 0; i < length; i++) {
			map = systemAppPackagesMap.get(i);
			String pkgName = map.get("packageName").toString();
			int versioncode = Integer.parseInt(map.get("appVersionCode")
					.toString());
			systemMap.put(pkgName, versioncode);
		}
		return systemMap;
	}

	public Map<String, Object> getSingleSystemApp(String pkgName) {
		Map<String, Object> map = null;
		int length = systemAppPackagesMap.size();
		for (int i = 0; i < length; i++) {
			map = systemAppPackagesMap.get(i);
			if (map.get("packageName").toString().equals(pkgName)) {
				return map;
			}
		}
		return null;
	}

	public Map<String, Object> getSingleSystemApp(String pkgName,
			List<HashMap<String, Object>> newSystemAppPackagesList) {
		Map<String, Object> map = null;
		int length = newSystemAppPackagesList.size();
		for (int i = 0; i < length; i++) {
			map = newSystemAppPackagesList.get(i);
			if (map.get("packageName").toString().equals(pkgName)) {
				return map;
			}
		}
		return null;
	}

	public void installAll() {
		if (null != cloudList && cloudList.size() > 0) {
			Intent intent = null;
			for (AppInfo info : cloudList) {
				intent = new Intent();
				intent.setAction(Constants.DOWNLOAD_ACTION);
				intent.putExtra("appName", info.getAppName());
				intent.putExtra("url", info.getUrl());// download url
				intent.putExtra("packageIdInt", info.getAppId());
				intent.putExtra("localfile",
						Constants.DOWNLOAD_SDPATH + info.getAppName());
				context.startService(intent);
			}
		}
	}
}
