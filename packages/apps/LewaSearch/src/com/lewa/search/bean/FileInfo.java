package com.lewa.search.bean;

import java.util.Map;

import android.graphics.drawable.Drawable;

import com.lewa.search.decorator.Decorator;
/**
 * This class defines an abstract view class for file infomation.
 * This class should have all of the attributes showed in the view.
 * All of other file infomation class have to extend this class.
 * @author		wangfan
 * @version	2012.07.04
 */

public abstract class FileInfo{
	
	//this sequenceShow defines which attributes should be showed on the screen
	//each item who extends this FileInfo has to define its' own sequenceShow
	public static String[] sequenceShow;
	
	//title, text, and thumbnail should show in each item
	private String title;
	private String text;
	
	private Drawable thumbnail;

	//each kind of file has its' unique fileType
	private int fileType;

	//this map records the attribute name and the decorator for this attribute
	public Map<String, Decorator> decorators;

	public FileInfo(String title, String text, Drawable thumbnail, int fileType) {
		super();
		this.title = title;
		this.text = text;
		this.thumbnail = thumbnail;
		this.fileType = fileType;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Drawable getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(Drawable thumbnail) {
		this.thumbnail = thumbnail;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}
}
