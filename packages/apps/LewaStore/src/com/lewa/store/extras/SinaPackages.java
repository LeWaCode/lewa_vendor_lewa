package com.lewa.store.extras;

import android.util.Log;

import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;

public class SinaPackages {

	private static String TAG = SinaPackages.class.getSimpleName();

	public final static String PUBLIC_SINA_PACKAGENAME = "com.sina.weibo";
	public final static String LEWA_SINA_PACKAGENAME = "com.sina.mfweibo";

	public SinaPackages() {
	};

	public static boolean uninstallWeibo() {
		boolean flag = false;
		String str = "Success";
		String[] args = { "pm", "uninstall", PUBLIC_SINA_PACKAGENAME };
		String identifier = PkgManager.runPackageCommand(args);
		if (identifier.indexOf(str) != -1) {
			flag = true;
			Log.d(TAG, "uninstall weibo success");
		}
		return flag;
	}

	public static void run(String beInstallPkgName) {
		if (beInstallPkgName.equals(SinaPackages.LEWA_SINA_PACKAGENAME)) {
			Log.d(TAG, "is sina weibo");
			if (LaunchApp.isInstallApp(SinaPackages.PUBLIC_SINA_PACKAGENAME)) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						Log.d(TAG, "del weibo");
						uninstallWeibo();
					}
				}).start();
			}
		}
	}
}