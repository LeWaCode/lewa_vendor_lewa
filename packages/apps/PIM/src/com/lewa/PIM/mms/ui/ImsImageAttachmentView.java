package com.lewa.PIM.mms.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import com.lewa.PIM.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImsImageAttachmentView extends LinearLayout implements SlideViewInterface{
    private ImageView mImageView;
    private static final String TAG = "ImsImageAttachmentView";

	public ImsImageAttachmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ImsImageAttachmentView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    protected void onFinishInflate() {
        mImageView = (ImageView) findViewById(R.id.ims_image_content);
    }
    
	@Override
	public void reset() {
		mImageView.setImageDrawable(null);		
	}

	@Override
	public void setVisibility(boolean visible) {
		setVisibility(visible ? View.VISIBLE : View.GONE);		
	}

	@Override
	public void setImage(String name, Bitmap bitmap) {
        try {
        	
        	if (!TextUtils.isEmpty(name)) {
				setImage(name);
				return;
			}
        	
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            mImageView.setImageBitmap(bitmap);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }		
	}
	
	public void setImage(String path){
		
        try {
			FileInputStream fosfrom = new FileInputStream(path);
			BitmapDrawable girl = new BitmapDrawable(fosfrom);
			Bitmap bitmap = girl.getBitmap();
			Bitmap mYlBitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 100, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
			bitmap.recycle();	
            mImageView.setImageBitmap(mYlBitmap);
            fosfrom.close();
        } catch (Exception e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }		
	}

	@Override
	public void setImageRegionFit(String fit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageVisibility(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVideo(String name, Uri video) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVideoVisibility(boolean visible) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startVideo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopVideo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseVideo() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seekVideo(int seekTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAudio(Uri audio, String name, Map<String, ?> extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startAudio() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopAudio() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseAudio() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void seekAudio(int seekTo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setText(String name, String text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTextVisibility(boolean visible) {
		// TODO Auto-generated method stub
		
	}

}
