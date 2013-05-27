package com.lewa.PIM.mms.slideshow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.webkit.MimeTypeMap;
import android.widget.ListView;

import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.lewa.PIM.R;
import com.lewa.PIM.mms.model.AudioModel;
import com.lewa.PIM.mms.model.ImageModel;
import com.lewa.PIM.mms.model.SlideModel;
import com.lewa.PIM.mms.model.SlideshowModel;
import com.lewa.PIM.mms.model.TextModel;
import com.lewa.PIM.mms.model.VideoModel;
import com.lewa.PIM.mms.slideshow.MmsSlideShowAdapter;
import com.lewa.PIM.mms.ui.MessagingPreferenceActivity;

public class MmsSlideShowListActivity extends Activity{
	
	private static final String TAG = "MmsSlideShowListActivity";	
    
	private ListView mListView;
	
	private Context mSlideShowContext;
	private MmsSlideShowAdapter mSlideShowAdapter;
	private SlideshowModel mModel;
	private MediaPlayer mAudioPlayer;
	
	public static int MMS_SLIDE_AUDIO_IDLE = 0;
	public static int MMS_SLIDE_AUDIO_PLAY = 1;
	public static int MMS_SLIDE_AUDIO_PAUSE = 2;
	public static int MMS_SLIDE_AUDIO_STOP = 3;
	
	private int mAudioPlayState = MMS_SLIDE_AUDIO_IDLE;
	private int mMmsId;
	
	private ArrayList<String> mTempFileList;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mms_slide_show_list);
		
		mSlideShowContext = this;
		mTempFileList = new ArrayList<String>();
		mListView = (ListView)findViewById(R.id.slide_list);		
		mSlideShowAdapter = new MmsSlideShowAdapter(mSlideShowContext);
		mListView.setAdapter(mSlideShowAdapter);
        Intent intent = getIntent();
        Uri msg = intent.getData();
        mMmsId = Integer.parseInt(msg.getLastPathSegment());
        
        try {
        	mModel = SlideshowModel.createFromMessageUri(this, msg);
		} catch (MmsException e) {
            Log.e(TAG, "Cannot present the slide show.", e);
            finish();
            return;
		}
		initSlideShowData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopAudio();		
		mSlideShowAdapter.cleanData();
		removeAllTempFile();
		if (!mModel.isEmpty()) {
			mModel.clear();			
		}
	}
	
	private void initSlideShowData(){
		int count = mModel.size();
		
		for (int i = 0; i < count; i++) {
			SlideShowItemData data = new SlideShowItemData();	
			SlideModel model = mModel.get(i);
			if (model.hasText()) {
				TextModel textmodel = model.getText();
				data.subject = textmodel.getText().replaceAll("\r", "");
				data.setmIsText(true);
			}
			
			if (model.hasAudio()){
				AudioModel audiomodel = model.getAudio();
				setAudio(audiomodel.getUri());
				data.filePath = setAudioFilePath();
				mTempFileList.add(data.filePath);
				data.setmIsAoudio(true);
				data.audioUri = audiomodel.getUri();
			}
			
			if (model.hasVideo()) {
				//VideoModel videoModel = model.getVideo();				
				data.filePath = setVideo();
				mTempFileList.add(data.filePath);
				data.setmIsVideo(true);
			}
			
			if (model.hasImage()) {
				ImageModel imageModel = model.getImage();
				data.showBitmap = imageModel.getBitmap();
				data.filePath = setImage(imageModel.getUri());
				mTempFileList.add(data.filePath);
				data.setmIsImage(true);
			}
			mSlideShowAdapter.addSlidShowItemData(data);
		}
		mSlideShowAdapter.notifyDataSetChanged();
	}	
	
	public int getAudioState(){
			return mAudioPlayState;
	}
	
    public void startAudio(Uri audio) {
    	
        if (mAudioPlayer != null) {
            mAudioPlayer.start();
            mAudioPlayState = MMS_SLIDE_AUDIO_PLAY;
        }else {
            mAudioPlayer = new MediaPlayer();            
            try {
				mAudioPlayer.setDataSource(mSlideShowContext, audio);
				mAudioPlayer.prepare();
				mAudioPlayer.start();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            mAudioPlayState = MMS_SLIDE_AUDIO_PLAY;
		}        
    }
    
    public void stopAudio() {
        if (mAudioPlayer != null) {
            mAudioPlayer.stop();
            mAudioPlayer.release();
            mAudioPlayer = null;
            mAudioPlayState = MMS_SLIDE_AUDIO_IDLE;
        }
    }

    public void pauseAudio() {
        if (mAudioPlayer != null) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.pause();
                mAudioPlayState = MMS_SLIDE_AUDIO_PAUSE;
            }
        }
    }
    
    public boolean isAudioPlaying(){
    	if (mAudioPlayer != null) {
			return mAudioPlayer.isPlaying();
		}else {
			return false;
		}
    }
    
    public int getAudioDuration(){
    	if (mAudioPlayer != null) {
			return mAudioPlayer.getDuration();
		}
    	return -1;
    }
    
    public int getAudioCurrentPosition(){
    	if (mAudioPlayer != null) {
			return mAudioPlayer.getCurrentPosition();
		}
    	return -1;
    }
    
    public void setAudio(Uri audio) {
        if (audio == null) {
            throw new IllegalArgumentException("Audio URI may not be null.");
        }
        mAudioPlayState = MMS_SLIDE_AUDIO_IDLE;
        if (mAudioPlayer != null) {
            mAudioPlayer.reset();
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
        try {
            mAudioPlayer = new MediaPlayer();
            mAudioPlayer.setDataSource(mSlideShowContext, audio);
            mAudioPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Unexpected IOException.", e);
            mAudioPlayer.release();
            mAudioPlayer = null;
        }
    }
    
    public String setVideo(){
        PduBody body = null;
        String path = null;
		try {
			body = SlideshowModel.getPduBody(mSlideShowContext, ContentUris.withAppendedId(Mms.CONTENT_URI, mMmsId));
		} catch (MmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (body == null) {
            return path;
        }
        
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (ContentType.isVideoType(type)) {
                // All parts (but there's probably only a single one) have to be successful
                // for a valid result.
            	path = copyPart(part, Long.toHexString(mMmsId)); 
            	break;
            }
        }        
        return path;
    }
    
    private String setAudioFilePath(){
        PduBody body = null;
        String path = null;
		try {
			body = SlideshowModel.getPduBody(mSlideShowContext, ContentUris.withAppendedId(Mms.CONTENT_URI, mMmsId));
		} catch (MmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (body == null) {
            return path;
        }
        
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (ContentType.isAudioType(type)) {
                // All parts (but there's probably only a single one) have to be successful
                // for a valid result.
            	path = copyPart(part, Long.toHexString(mMmsId)); 
            	break;					
            }
        }        
        return path;
    }
    
    private String setImage(Uri uri){
        PduBody body = null;
        String path = null;
		try {
			body = SlideshowModel.getPduBody(mSlideShowContext, ContentUris.withAppendedId(Mms.CONTENT_URI, mMmsId));
		} catch (MmsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (body == null) {
            return path;
        }
        
        int partNum = body.getPartsNum();
        for(int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            String type = new String(part.getContentType());
            if (ContentType.isImageType(type)) {
                // All parts (but there's probably only a single one) have to be successful
                // for a valid result.
            	if (part.getDataUri().compareTo(uri) == 0) {
                	path = copyPart(part, Long.toHexString(mMmsId)); 
                	break;					
				}
            }
        }        
        return path;
    }
    
    private String copyPart(PduPart part, String fallback) {
        Uri uri = part.getDataUri();
        String path = null;
        InputStream input = null;
        FileOutputStream fout = null;
        try {
            input = mSlideShowContext.getContentResolver().openInputStream(uri);
            if (input instanceof FileInputStream) {
                FileInputStream fin = (FileInputStream) input;

                byte[] location = part.getName();
                if (location == null) {
                    location = part.getFilename();
                }
                if (location == null) {
                    location = part.getContentLocation();
                }

                String fileName;
                if (location == null) {
                    // Use fallback name.
                    fileName = fallback;
                } else {
                    fileName = new String(location);
                }
                // Depending on the location, there may be an
                // extension already on the name or not
                // Get Shared Preferences and User Defined Save Location for MMS
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String dir = Environment.getExternalStorageDirectory() + "/"
                                + prefs.getString(MessagingPreferenceActivity.MMS_SAVE_LOCATION, "download")  + "/";
                String extension;
                int index;
                if ((index = fileName.indexOf(".")) == -1) {
                    String type = new String(part.getContentType());
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                }
                path = dir + fileName + "." + extension;
                File file = new File(path);

                // make sure the path is valid and directories created for this file.
                File parentFile = file.getParentFile();
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    Log.e(TAG, "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                    return null;
                }

                fout = new FileOutputStream(file);

                byte[] buffer = new byte[8000];
                int size = 0;
                while ((size=fin.read(buffer)) != -1) {
                    fout.write(buffer, 0, size);
                }

                // Notify other applications listening to scanner events
                // that a media file has been added to the sd card
//                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
//                        Uri.fromFile(file)));
            }
        } catch (IOException e) {
            // Ignore
            Log.e(TAG, "IOException caught while opening or reading stream", e);
            return path;
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return path;
                }
            }
            if (null != fout) {
                try {
                    fout.close();
                } catch (IOException e) {
                    // Ignore
                    Log.e(TAG, "IOException caught while closing stream", e);
                    return path;
                }
            }
        }
        return path;
    }    
    
    private void removeAllTempFile(){
    	int count = mTempFileList.size();
    	for (int i = 0; i < count; i++) {
    		try {
        		File delFile=new File(mTempFileList.get(i));
        		if (delFile.exists()) {
    				delFile.delete();
    			}				
			} catch (Exception e) {
				Log.e(TAG, "delete file error");
			}
		}
    	mTempFileList.clear();
    }    
}
