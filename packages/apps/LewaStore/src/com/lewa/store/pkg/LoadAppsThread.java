package com.lewa.store.pkg;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class LoadAppsThread implements Runnable {

	Context context;
	private List<PackageInfo> packageInfos;
	private List<PackageInfo> userPackageInfos;

	public LoadAppsThread(Context c) {
		this.context = c;
	}

	private void getAppsPackageInfos() {
		packageInfos = context.getPackageManager().getInstalledPackages(
				PackageManager.GET_UNINSTALLED_PACKAGES);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		getAppsPackageInfos();
		// 得到手机上安装的程序。
		userPackageInfos = new ArrayList<PackageInfo>();
		int size=packageInfos.size();
		for (int i = 0; i < size; i++) {
			PackageInfo temp = packageInfos.get(i);
			boolean isUserApp = false;
			ApplicationInfo appInfo = temp.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
				// 表示是系统程序，但用户更新过，也算是用户安装的程序
				isUserApp = true;
			} else if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
				// 一定是用户安装的程序
				isUserApp = true;
			}
			if (isUserApp) {
				userPackageInfos.add(temp);
			}
		}
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
