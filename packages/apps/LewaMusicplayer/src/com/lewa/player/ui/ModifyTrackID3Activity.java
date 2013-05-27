package com.lewa.player.ui;

import com.lewa.player.ExitApplication;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.common.CorrectTrackID3;

import android.app.Activity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ModifyTrackID3Activity extends Activity {
	
	public EditText mETArtist;
	public EditText mETTrack;
	public EditText mETAlbum;
	public Button mBTSave;
	public Button mBTCancel;
	public String mPath;
	public long mSongid = -1;
	public Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.modify_id3);
		
		mContext = this;
		mETTrack = (EditText) findViewById(R.id.editText1);
		mETArtist = (EditText) findViewById(R.id.editText2);
		mETAlbum = (EditText) findViewById(R.id.editText3);
		
		mBTSave = (Button) findViewById(R.id.button1);
		mBTCancel = (Button) findViewById(R.id.button2);
		
		mBTSave.setOnClickListener(listener);
		mBTCancel.setOnClickListener(listener);
		Intent mIntent = this.getIntent();
		if(mIntent != null){
		    mETTrack.setText(mIntent.getStringExtra("track"));
		    
		    String artist = mIntent.getStringExtra("artist");
		    String album = mIntent.getStringExtra("album");
		    
		    if(MediaStore.UNKNOWN_STRING.equals(artist)) {
		        artist = getResources().getString(R.string.unknown_artist_name);
            }            
            if(MediaStore.UNKNOWN_STRING.equals(album)) {
                album = getResources().getString(R.string.unknown_album_name);
            }
			mETArtist.setText(artist);
			mETAlbum.setText(album);
		}		

		String path = mIntent.getStringExtra("path");
		long songid = mIntent.getLongExtra("TrackId", -1);
		if(path == null && songid > 0) {
			path = MusicUtils.getSongPath(this, songid);
			mSongid = songid;
		}
		if(path != null){
			mPath = path;
		}
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}
	
	OnClickListener listener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId() == R.id.button1) {
				String[] TagInfo = new String[3];
				TagInfo[0] = mETTrack.getText().toString().trim();
				TagInfo[1] = mETArtist.getText().toString().trim();
				TagInfo[2] = mETAlbum.getText().toString().trim();
				if(TagInfo[0].equals("")) {
					Toast.makeText(mContext, mContext.getText(R.string.id3_empty), 1000).show();
					return;
				}
				CorrectTrackID3.setIntagedInfo(mContext,TagInfo, mPath, mSongid);				
				
				Intent in = new Intent();
				in.setAction(MediaPlaybackService.UPDATEID3INFO);
				mContext.sendBroadcast(in);
				hiddenSoftInput();

				finish();
			}else {
				finish();
			}
		}
	};
	
	public void hiddenSoftInput() {
		if(mETArtist != null && mETTrack != null && mETAlbum != null) {
			InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.hideSoftInputFromWindow(mETArtist.getWindowToken(), 0);
			inputMethodManager.hideSoftInputFromWindow(mETTrack.getWindowToken(), 0);
			inputMethodManager.hideSoftInputFromWindow(mETAlbum.getWindowToken(), 0);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}	

}
