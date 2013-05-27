package com.lewa.search.bean;

import android.graphics.drawable.Drawable;

/**
 * This class defines a view class for normal files.
 * @author		wangfan
 * @version	2012.07.04
 */

public class NormalFileInfo extends FileInfo{

	//this sequenceShow defines which attributes should be showed on the screen
	//in this case, the view has to get a TextView to show "title", a TextView to show "text",
	//		and a ImageView to show "thumbnail"
	static 
	{  
		sequenceShow =  new String[] { "title", "text", "thumbnail" };
	}
	
	//records filePath for file opening operation
	private String filePath;
	
	public NormalFileInfo(String title, String text, Drawable thumbnail, String filePath,
			int fileType) {
		super(title, text, thumbnail, fileType);
		// TODO Auto-generated constructor stub
		
		this.filePath = filePath;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

}
