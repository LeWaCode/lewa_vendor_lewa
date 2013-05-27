package com.lewa.PIM.mms.ui;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import com.lewa.PIM.R;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImsVideoAttachmentView extends LinearLayout implements SlideViewInterface{
	private ImageView mImageView;
	private static final String TAG = "ImsVideoAttachmentView";
	
	public ImsVideoAttachmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ImsVideoAttachmentView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
    @Override
    protected void onFinishInflate() {
        mImageView = (ImageView) findViewById(R.id.video_thumbnail);
    }
    
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVisibility(boolean visible) {
		setVisibility(visible ? View.VISIBLE : View.GONE);			
	}

	@Override
	public void setImage(String name, Bitmap bitmap) {
        try {
            if (null == bitmap) {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            }
            mImageView.setImageBitmap(bitmap);
        } catch (java.lang.OutOfMemoryError e) {
            Log.e(TAG, "setImage: out of memory: ", e);
        }
	}
	
//	public void setImage(ContentResolver cr, Uri uri){
//		
//        try {
//        	Bitmap bitmap = null;
//        	BitmapFactory.Options options = new BitmapFactory.Options();  
//        	options.inDither = false;
//        	options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//        	Cursor cursor = cr.query(uri,new String[] { MediaStore.Video.Media._ID }, null, null, null);
//        	if (cursor == null || cursor.getCount() == 0) {  
//                return ;  
//            }
//        	cursor.moveToFirst();
//        	String videoId = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
//        	if (videoId == null) {  
//                return;  
//            }
//        	cursor.close();
//        	long videoIdLong = Long.parseLong(videoId);
//			bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, videoIdLong,Images.Thumbnails.MICRO_KIND, options);
//            mImageView.setImageBitmap(bitmap);
//
//        } catch (Exception e) {
//            Log.e(TAG, "setImage: out of memory: ", e);
//        }	
//	}
	
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
