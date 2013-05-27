package com.lewa.search.bean;

import android.graphics.drawable.Drawable;

/**
 * This class defines a view class for special web search buttons.
 * This programe treat search buttons as normal searched file items,as for the unity of the interface.
 * @author		wangfan
 * @version	2012.07.04
 */

public class WebSearchFileInfo extends FileInfo{
	
	//this sequenceShow defines which attributes should be showed on the screen
	//in this case, the view has to get a TextView to show "title", a TextView to show "text",
	//		and a ImageView to show "thumbnail"
	static 
	{  
		sequenceShow =  new String[] { "title", "text", "thumbnail" };
	}
	
	//there are two kinds of webSearch:web info search and  web app search
	private int webSearchType;
	//records the key in web search
	private String key;
	
	public WebSearchFileInfo(String title, String text, Drawable thumbnail,
			int webSearchType, String key, int fileType) {
		super(title, text, thumbnail, fileType);
		// TODO Auto-generated constructor stub
		
		this.webSearchType = webSearchType;
		this.key = key;
	}

	public int getWebSearchType() {
		return webSearchType;
	}

	public void setWebSearchType(int webSearchType) {
		this.webSearchType = webSearchType;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
