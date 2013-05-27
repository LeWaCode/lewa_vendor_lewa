package com.lewa.PIM.mms.slideshow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.lewa.PIM.R;
import com.lewa.PIM.mms.ui.ComposeMessageActivity;
import com.lewa.PIM.mms.ui.MessageItem;
import com.lewa.PIM.mms.ui.MessagingPreferenceActivity;
import com.lewa.PIM.mms.ui.MsgLinkify;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Video;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MmsSlideShowItem extends LinearLayout{
	
	private TextView mTitle;
	private ImageView mContactImage;
	private TextView mTextBody;
	private RelativeLayout mContactImageLayout;
	private ImageView mPlaySlideshowButton;
	private LinearLayout mContactImageParent;
	
	private Context mContext;
	private Handler mhandler;
	private SlideShowItemData mData;
	
	private long mSampleStart = 0;
	private static final int MENU_COPY_TO_SDCARD  = 0;
	 
	 
	public MmsSlideShowItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MmsSlideShowItem(Context context) {
		super(context);
	}
	
	public void setActivity(Context context){
		mContext = context;		
	}
	
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitle = (TextView)findViewById(R.id.title);
        mContactImage = (ImageView)findViewById(R.id.contact_image);
        mTextBody = (TextView)findViewById(R.id.text_body);
        mContactImageLayout = (RelativeLayout)findViewById(R.id.contact_image_layout);
        mPlaySlideshowButton = (ImageView)findViewById(R.id.play_slideshow_button);
        mContactImageParent = (LinearLayout)findViewById(R.id.contact_image_parent);
    }
    
    public void bind(SlideShowItemData itemData){
    	
    	mData = itemData;
    	
    	if (!TextUtils.isEmpty(itemData.subject)) {
        	mTitle.setText(itemData.subject);			
    		MsgLinkify.addLinks(mTitle, 0x000f);
		}else {
			mTitle.setText("");
		}
    	
    	if (!TextUtils.isEmpty(itemData.textBody)) {
        	mTextBody.setText(itemData.textBody);			
    		MsgLinkify.addLinks(mTextBody, 0x000f);
		}else {
			mTextBody.setText("");
		}
    	
    	if (itemData.ismIsImage()) {    		
    		setImage(itemData.showBitmap);
		}else {
			mContactImage.setVisibility(View.GONE);
		}
    	
    	if (itemData.ismIsVideo()) {
			setVideo();
			mPlaySlideshowButton.setVisibility(View.VISIBLE);
		}else {
			mPlaySlideshowButton.setVisibility(View.GONE);
		}
    	
    	if (itemData.ismIsAoudio()) {
			setAoudio();
		}
    }   
    
    private void setVideo(){
    	Bitmap image = ThumbnailUtils.createVideoThumbnail(mData.filePath, Video.Thumbnails.MINI_KIND);
    	
    	if (image == null) {
    		image = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_video);
		}
    	
    	mContactImage.setImageBitmap(image);
    	mContactImage.setVisibility(View.VISIBLE);
    	
    	mContactImageLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mediaIntent = new Intent(Intent.ACTION_VIEW);
				Uri uri = Uri.parse(mData.filePath);
				String type = "video/*";
				mediaIntent.setDataAndType(uri, type);
				mContext.startActivity(mediaIntent);					
			}
		});
    	
    	mContactImageParent.setOnCreateContextMenuListener(mRecipientsMenuCreateListener);
    }
    
    private void setImage(Bitmap bitmap){
    	
    	Bitmap imageBitmap = bitmap;
    	if (imageBitmap == null) {
            bitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.ic_missing_thumbnail_picture);
		}
		mContactImage.setImageBitmap(imageBitmap);    	
		mContactImage.setVisibility(View.VISIBLE);
		
		mContactImageLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				File fp = new File(mData.filePath);
				Intent intent = new Intent();
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(fp), "image/*");
				mContext.startActivity(intent);				
			}
		});			
		
		mContactImageParent.setOnCreateContextMenuListener(mRecipientsMenuCreateListener);
    }
    
    private void setAoudio(){
    	mContactImageLayout.setBackgroundResource(R.drawable.skinclassic_color_list_top_bot);
    	mContactImage.setBackgroundResource(R.drawable.yms_album_thumb);
    	MmsSlideShowListActivity activity = (MmsSlideShowListActivity)mContext;
    	mhandler = new Handler();
    	
		if (activity.getAudioState() == activity.MMS_SLIDE_AUDIO_IDLE ||
			activity.getAudioState() == activity.MMS_SLIDE_AUDIO_PAUSE) {
			mContactImage.setImageResource(R.drawable.play);
		}else {
			mContactImage.setImageResource(R.drawable.pause);
		}
		mContactImage.setVisibility(View.VISIBLE);
		
    	mContactImageLayout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MmsSlideShowListActivity activity = (MmsSlideShowListActivity)mContext;
				
				if (activity.getAudioState() == activity.MMS_SLIDE_AUDIO_PLAY) {
					activity.pauseAudio();
					mContactImage.setImageResource(R.drawable.play);
				}else {
					activity.startAudio(mData.audioUri);
			    	mhandler.postDelayed(mRunnable, 300);
			    	mSampleStart = System.currentTimeMillis();
					mContactImage.setImageResource(R.drawable.pause);					
				}
			}
		});
    	mContactImageParent.setOnCreateContextMenuListener(mRecipientsMenuCreateListener);
    }
    
    Runnable mRunnable = new Runnable(){
 	   @Override
 	   public void run() {
 		  MmsSlideShowListActivity activity = (MmsSlideShowListActivity)mContext;
 		  
 		  long time = System.currentTimeMillis();
 		  
 		  int playTime = (int)(time - mSampleStart);
 		  if (playTime >= activity.getAudioDuration()) {
 			  mhandler.removeCallbacks(mRunnable);
 			  activity.stopAudio();
 			  mContactImage.setImageResource(R.drawable.play);
 		  }else {
 			  mhandler.postDelayed(this, 300);										
 		  }
	   }
     };
     
     private final OnCreateContextMenuListener mRecipientsMenuCreateListener = new OnCreateContextMenuListener() {
 		
 		@Override
 		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo info) {
 			MsgListMenuClickListener l = new MsgListMenuClickListener();
            menu.add(0, MENU_COPY_TO_SDCARD, 0, R.string.copy_to_sdcard)
            .setOnMenuItemClickListener(l);			
 		}
 	};
 	
 	private final class MsgListMenuClickListener implements MenuItem.OnMenuItemClickListener{

		@Override
		public boolean onMenuItemClick(MenuItem item) {
            int resId = 0;
            resId = imsCopyMedia(mData.filePath) ? R.string.copy_to_sdcard_success :
                R.string.copy_to_sdcard_fail;												
            Toast.makeText(mContext, resId, Toast.LENGTH_SHORT).show();
            return true;
		} 		
 	}
 	
    private boolean imsCopyMedia(String path){
    	boolean ret = false;
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String dir = Environment.getExternalStorageDirectory() + "/"
        + prefs.getString(MessagingPreferenceActivity.MMS_SAVE_LOCATION, "download")  + "/";        
        
        try {
    		    		
    		File parentFile = new File(dir);
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                Log.e("MmsSlideShowItem", "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                return false;
            }
    		String [] list = path.split("/");
    		String fileName = list[list.length - 1];
    		String filePath = dir + "temp_" + fileName;    		    		
    		
    		FileInputStream fosfrom = new FileInputStream(path);
    		FileOutputStream fosto = new FileOutputStream(filePath);
    		
    		int c;
    		byte bt[] = new byte[1024];
    		while ((c = fosfrom.read(bt)) > 0) {
    			fosto.write(bt, 0, c);
    		}
    		
    		fosfrom.close();
    		fosto.close();
    		ret = true;
    		
		} catch (Exception e) {
			Log.e("readfile", e.getMessage());
		}	
		return ret;
    }
 }
