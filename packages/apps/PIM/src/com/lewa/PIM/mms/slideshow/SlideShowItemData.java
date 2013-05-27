package com.lewa.PIM.mms.slideshow;

import android.graphics.Bitmap;
import android.net.Uri;

public class SlideShowItemData{
	
	public String subject;
	public String textBody;
	public Bitmap showBitmap;
	public Uri audioUri;
	public String filePath;
	
	public boolean ismIsText() {
		return mIsText;
	}

	public void setmIsText(boolean mIsText) {
		this.mIsText = mIsText;
	}

	public boolean ismIsVideo() {
		return mIsVideo;
	}

	public void setmIsVideo(boolean mIsVideo) {
		this.mIsVideo = mIsVideo;
	}

	public boolean ismIsImage() {
		return mIsImage;
	}

	public void setmIsImage(boolean mIsImage) {
		this.mIsImage = mIsImage;
	}

	public boolean ismIsAoudio() {
		return mIsAoudio;
	}

	public void setmIsAoudio(boolean mIsAoudio) {
		this.mIsAoudio = mIsAoudio;
	}

	private boolean mIsText;
	private boolean mIsVideo;
	private boolean mIsImage;
	private boolean mIsAoudio;
	
	public SlideShowItemData(){
		subject = "";
		textBody = "";
		showBitmap = null;
	}
	
	public SlideShowItemData(String sub, String body, Bitmap bitmap){
		subject = sub;
		textBody = body;
		showBitmap = bitmap;
	}
}
