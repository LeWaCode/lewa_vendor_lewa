package com.lewa.store.pkg;

import java.io.File;
import java.util.List;

import com.lewa.store.model.AppInfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;

/**
 * @author 启动另外一个应用
 */
public class LaunchApp {
	
	/** 判断是否安装或覆盖安装的类型 */
	private static final int NOTINSTALL = 0; // 未安装
	private static final int INSTALLED = 1; // 已安装且为新版本
	private static final int OLDVERSION = 2; // 已安装但为旧版本
    Context context;
    
    public LaunchApp(Context c){
    	this.context=c;
    }
    
	/**
	 * @param pkg	pakage name
	 * @param installMode	0 -- go market  1 download in client
	 * @param appPath	download url
	 * @param fileName	saved name
	 * launch app from phone ,if not exsit ,download it from market or our server
	 */
	public void launchApp(String pkg,int installMode,String appPath,String fileName) {
		if (isInstallApp(pkg)) {// 启动目标应用
			// 获取目标应用安装包的Intent
			Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkg);
			context.startActivity(intent);
		} else {// 安装目标应用
			if (installMode == 0) {
//				context.mViewManager.mClientJsInterface.goMarket(pkg);
			}else {
//				context.mViewManager.mClientJsInterface.autoUpgrade(appPath, fileName);
			}
		}
	}

	/**
	 * 判断应用是否安装或者是否为最新版本
	 * 
	 * @param pkg
	 *                目标应用安装后的包名
	 * @param versionCode
	 *                指定的应用版本号
	 * @return 安装的类型
	 */
	private int isLastestApp(String pkg, int versionCode) {
		if (isInstallApp(pkg)) {
			// 获取系统中安装的所有应用包名集合
			List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
			int size=packages.size();
			for (int i = 0; i < size; i++) {
				PackageInfo packageInfo = packages.get(i);
				// 找出指定的应用
				if (pkg.equals(packageInfo.packageName)) {
					if (packageInfo.versionCode >= versionCode) {
						return INSTALLED;
					} else {
						return OLDVERSION;
					}
				}
			}
		}
		return NOTINSTALL;
	}
	
	/**
	 * 检测是否需要更新
	 * @param ai
	 * @param sVersionCode
	 * @return
	 */	
	public static boolean isUpdateApp(AppInfo ai, int sVersionCode) {
		boolean flag=false;
		if(Integer.parseInt(ai.getAppVersionCode())>sVersionCode){
			flag=true;
		}
		return flag;
	}
	
	/**
	 * 检测是否安装
	 * @param pkg	pakage name
	 * @return
	 * whether the app is installed or not
	 */
	public static boolean isInstallApp(String pkg) {
		return new File("/data/data/" + pkg).exists();
	}
	
	/**
	 * 是否安装谷歌应用
	 * @param filename
	 * @return
	 */
	public static boolean isInstallGooglePackages(String filename){
	    if(null!=filename && !filename.equals("")){
	        return new File("/system/app/"+filename.trim()).exists();
	    }else{
	        return false;
	    }	   
	}
}
