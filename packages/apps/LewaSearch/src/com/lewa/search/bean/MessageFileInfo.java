package com.lewa.search.bean;

import android.graphics.drawable.Drawable;

/**
 * This class defines a view class for message infomation.
 * @author		wangfan
 * @version	2012.07.04
 */

public class MessageFileInfo extends FileInfo{
	
	//this sequenceShow defines which attributes should be showed on the screen
	//in this case, the view has to get a TextView to show "title", a TextView to show "text",
	//		and a ImageView to show "thumbnail"
	static 
	{  
		sequenceShow =  new String[] { "title", "text", "thumbnail" };
	}
	
	//messageId, threadId and number are necessary in locating the message
	private String messageId;
	private String threadId;
	private String number;

	public MessageFileInfo(String messageId, String title, String text, Drawable thumbnail, 
			String threadId, String number, int type) {
		// TODO Auto-generated constructor stub
		super(title, text, thumbnail, type);
		this.messageId = messageId;
		this.threadId = threadId;
		this.number = number;
		
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
}
