package com.lewa.search.bean;

import android.graphics.drawable.Drawable;

/**
 * This class defines a view class for setting infomation.
 * @author		wangfan
 * @version	2012.07.04
 */

public class SettingFileInfo extends FileInfo{
	
	//this sequenceShow defines which attributes should be showed on the screen
	//in this case, the view has to get a TextView to show "title", a TextView to show "text",
	//		and a ImageView to show "thumbnail"
	static 
	{  
		sequenceShow =  new String[] { "title", "text", "thumbnail" };
	}
	
	//each setting item has its' unique id
	private int id;
	//records packageName and actionName for file opening operation
	private String actionName;
	private String packageName;
	
	public SettingFileInfo(int id, String title, String text, Drawable thumbnail, String actionName, String packageName,
			int type) {
		
		super(title, text, thumbnail, type);
		// TODO Auto-generated constructor stub
		
		this.id = id;
		this.actionName = actionName;
		this.packageName = packageName;
		
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
