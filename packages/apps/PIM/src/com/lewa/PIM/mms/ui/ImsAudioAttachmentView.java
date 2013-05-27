package com.lewa.PIM.mms.ui;

import java.util.Map;

import com.google.android.mms.ContentType;
import com.lewa.PIM.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImsAudioAttachmentView extends LinearLayout implements SlideViewInterface{
    private TextView mNameView;
    private TextView mAlbumView;
    private TextView mArtistView;
    private TextView mErrorMsgView;
    private Button 	 mPlayButton;
    private Handler  mhandler = null;
    private Recorder mRecorder = null;
    
	public ImsAudioAttachmentView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ImsAudioAttachmentView(Context context) {
		super(context);
	}
	
    @Override
    protected void onFinishInflate() {
        mNameView = (TextView) findViewById(R.id.audio_name);
        mAlbumView = (TextView) findViewById(R.id.album_name);
        mArtistView = (TextView) findViewById(R.id.artist_name);
        mErrorMsgView = (TextView) findViewById(R.id.audio_error_msg);
        mPlayButton = (Button) findViewById(R.id.ims_play_audio_button);
        mhandler = new Handler();
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
		// TODO Auto-generated method stub
		
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
		mNameView.setText(name);		
	}

	@Override
	public void startAudio() {
		// TODO Auto-generated method stub
		
	}
	
	public void startAudio(String path, Recorder recorder){
		if (TextUtils.isEmpty(path) || recorder == null) {
			return;
		}
		mRecorder = recorder;
		
		if (recorder.state() == recorder.IDLE_STATE) {
			mPlayButton.setText(R.string.compose_stop_audio_text);
			recorder.play(path);  
			if (mhandler == null) {
				mhandler = new Handler();
			}
			mhandler.postDelayed(mRunnable, 300);
		}else if (recorder.state() == recorder.PLAYING_STATE){
			stopAudio();   			
			mRecorder = null;
		}
	}
	
    Runnable mRunnable=new Runnable(){
 	   @Override
 	   public void run() {
		   if (mRecorder != null && mRecorder.state() == mRecorder.IDLE_STATE) {
			   mPlayButton.setText(R.string.play);
			   mRecorder = null;
		   }else {
			   mhandler.postDelayed(this, 300);
		   }	
 	    }
     };
     
	@Override
	public void stopAudio() {
		if (mRecorder != null && mRecorder.state() == mRecorder.PLAYING_STATE) {
			mPlayButton.setText(R.string.play);
			mRecorder.stop();
			mhandler.removeCallbacks(mRunnable);
		}		
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
