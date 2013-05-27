package com.lewa.store.download;

import java.io.Serializable;

/**
 * 记录进度条信息
 * @author ypzhu
 * @email  richfuns@gmail.com
 * 
 */
public class LoadInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public int fileSize;
	private int complete;
	private String urlstring;
	
	public LoadInfo(int fileSize, int complete, String urlstring) {
		this.fileSize = fileSize;
		this.complete = complete;
		this.urlstring = urlstring;
	}

	public LoadInfo() {
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getComplete() {
		return complete;
	}

	public void setComplete(int complete) {
		this.complete = complete;
	}

	public String getUrlstring() {
		return urlstring;
	}

	public void setUrlstring(String urlstring) {
		this.urlstring = urlstring;
	}

	@Override
	public String toString() {
		return "LoadInfo [fileSize=" + fileSize + ", complete=" + complete
				+ ", urlstring=" + urlstring + "]";
	}	
}
