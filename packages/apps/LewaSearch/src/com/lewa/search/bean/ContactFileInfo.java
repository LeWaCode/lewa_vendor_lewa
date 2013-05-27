package com.lewa.search.bean;

import android.graphics.drawable.Drawable;

/**
 * This class defines a view class for contact infomation.
 * @author		wangfan
 * @version	2012.07.04
 */
public class ContactFileInfo extends FileInfo{

	//this sequenceShow defines which attributes should be showed on the screen
	//in this case, the view has to get a TextView to show "title", a TextView to show "text",
	//		and a ImageView to show "thumbnail"
	static 
	{  
		sequenceShow =  new String[] { "title", "text", "thumbnail" }; 
	}
	
	//records contactId for each item, contactId is unique
	private String contactId;
	
	public ContactFileInfo(String contactId, String title, String text, Drawable thumbnail, int fileType) {
		
		// TODO Auto-generated constructor stub
		super(title, text, thumbnail, fileType);
		this.contactId = contactId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	
}
