package com.lewa.store.model;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.util.Log;

public class AppInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String TAG = "AppInfo";
	private int appId;
	private String appName;
	private String appAuthor;
	private Bitmap appLogo;
	private String Description;
	private String screenShot;
	private int downloadTimes;// 下载次数
	private String url;// 下载地址
	private String appVersion;// 版本
	private String appVersionCode;
	private String appLogoUrl;
	private String packageName;
	private String access;//0,guest 可以下载;1,注册用户可以下载,2,私有用户才可下载
	private int appSize;//大小,单位字节
	private String appCategory;

	public AppInfo(){
		
	}

	public AppInfo(int app_id, String app_name, String app_author,
			String app_version, String app_versioncode, String app_packagename,
			String app_description, String app_url, String app_logourl,String app_access,int size,String category) {
		this.appId = app_id;
		this.appName = app_name;
		this.appAuthor = app_author;
		this.appVersion = app_version;
		this.appVersionCode = app_versioncode;
		this.packageName = app_packagename;
		this.Description = app_description;
		this.url = app_url;
		this.appLogoUrl = app_logourl;
		this.access=app_access;
		this.appSize=size;
		this.appCategory=category;
	}
	
	public void stat() {
		Log.d(TAG, String.format(
			"id: %s, name: %s, author: %s, version: %s, version_code: %s, package_name: %s, description: %s, url: %s",
			this.appId, this.appName, this.appAuthor, this.appVersion, this.appVersionCode, this.packageName, this.Description, this.url
		));
	}
	
	public String getAppCategory() {
		return appCategory;
	}

	public void setAppCategory(String appCategory) {
		this.appCategory = appCategory;
	}

	public int getAppSize() {
		return appSize;
	}

	public void setAppSize(int appSize) {
		this.appSize = appSize;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getAppVersionCode() {
		return appVersionCode;
	}

	public void setAppVersionCode(String appVersionCode) {
		this.appVersionCode = appVersionCode;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getAppLogoUrl() {
		return appLogoUrl;
	}

	public void setAppLogoUrl(String appLogoUrl) {
		this.appLogoUrl = appLogoUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppAuthor() {
		return appAuthor;
	}

	public void setAppAuthor(String appAuthor) {
		this.appAuthor = appAuthor;
	}

	public Bitmap getAppLogo() {
		return appLogo;
	}

	public void setAppLogo(Bitmap appLogo) {
		this.appLogo = appLogo;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	public String getScreenShot() {
		return screenShot;
	}

	public void setScreenShot(String screenShot) {
		this.screenShot = screenShot;
	}

	public int getDownloadTimes() {
		return downloadTimes;
	}

	public void setDownloadTimes(int downloadTimes) {
		this.downloadTimes = downloadTimes;
	}

	@Override
	public String toString() {
		return "AppInfo [appId=" + appId + ", appName=" + appName
				+ ", appAuthor=" + appAuthor + ", appLogo=" + appLogo
				+ ", Description=" + Description + ", screenShot=" + screenShot
				+ ", downloadTimes=" + downloadTimes + ", url=" + url
				+ ", appVersion=" + appVersion + ", appVersionCode="
				+ appVersionCode + ", appLogoUrl=" + appLogoUrl
				+ ", packageName=" + packageName + ", access=" + access + "]";
	}
}
