package com.lewa.search.bean;

import android.graphics.drawable.Drawable;

/**
 * This class defines a view class for application infomation.
 * @author		wangfan
 * @version	2012.07.04
 */

public class AppFileInfo extends FileInfo{
	
	//this sequenceShow defines which attributes should be showed on the screen
	//in this case, the view has to get a TextView to show "title", a TextView to show "text",
	//		and a ImageView to show "thumbnail"
	static 
	{  
		sequenceShow =  new String[] { "title", "text", "thumbnail" };
	}
	
	//records packageName for each app
	private String packageName;
	//two kinds of applications:user app and system app
	private int appType;
	
	public AppFileInfo(String title, String text, Drawable thumbnail, String packageName,
			int appType, int fileType) {
		super(title, text, thumbnail, fileType);
		// TODO Auto-generated constructor stub
		
		this.packageName = packageName;
		this.appType = appType;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

}
