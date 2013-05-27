package com.lewa.player.ui;

import com.lewa.player.ExitApplication;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.ui.outer.MusicMainEntryActivity;
import com.lewa.player.ui.view.MediaPlaybackView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MediaPlaybackActivity extends Activity implements MusicUtils.Defs, View.OnClickListener{

	private MediaPlaybackView mv;
	private TextView artist;
	private TextView trackname;
	private TextView nextTrack1;
	private TextView nextTrack2;
	private TextView nextTrack3;
	private TextView nextTrack4;
	private ImageView mRepeatButton;
	private ImageView mShuffleButton;
	private ImageView albumImageView;
	private ImageView mEQsetting;
	private ImageView mBackButton;
	private IMediaPlaybackService mService;
	private CurrentPlaylistActivity cll;
	private int repeatMode = 0;
	private int randomMode = 0;
	private Context mContext;
	public static SharedPreferences mPlaysettings;
	private boolean mIsShuffle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mv = new MediaPlaybackView(getApplicationContext());
		artist = (TextView) mv.findViewById(R.id.artistNameText);
		trackname = (TextView) mv.findViewById(R.id.songNameText);
//		nextTrack1 = (TextView) mv.findViewById(R.id.hisSongName1);
//		nextTrack2 = (TextView) mv.findViewById(R.id.hisSongName2);
//		nextTrack3 = (TextView) mv.findViewById(R.id.hisSongName3);
//		nextTrack4 = (TextView) mv.findViewById(R.id.hisSongName4);
//		albumImageView = (ImageView) mv.findViewById(R.id.albumimage);
		mRepeatButton = (ImageView) mv.findViewById(R.id.repeatSwitch);
//		mShuffleButton = (ImageView) mv.findViewById(R.id.randomSwitch);
		mEQsetting = (ImageView) mv.findViewById(R.id.eqSetting);
		mBackButton = (ImageView) mv.findViewById(R.id.backtomain);
		mPlaysettings = getSharedPreferences("Music", 0);
		
		mContext = this;
//		mRepeatButton.setOnTouchListener(tl);
	//	mShuffleButton.setOnTouchListener(tl);
//		mEQsetting.setOnTouchListener(tl);
		
		mRepeatButton.setOnClickListener(this);
	    //  mShuffleButton.setOnTouchListener(tl);
	    mEQsetting.setOnClickListener(this);
		mBackButton.setOnClickListener(this);
		this.setContentView(mv);
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}

//	public void setAlbumImage(Bitmap obj) {
//		albumImageView.setImageBitmap(obj);
//		//albumImageView.getDrawable().setDither(true);
//	}

	public void setMediaService(IMediaPlaybackService service) {
		mService = service;
	}

	public void setCurListActivity(CurrentPlaylistActivity cl) {
		cll = cl;
	}
	
	public void updateRepeatButtonImg() {
	    try {
            repeatMode = mPlaysettings.getInt("repeatmode", MediaPlaybackService.REPEAT_ALL);
            randomMode = mPlaysettings.getInt("shufflemode", MediaPlaybackService.SHUFFLE_NONE);
                        
            if (MediaPlaybackService.SHUFFLE_NORMAL == randomMode ||
                    MediaPlaybackService.SHUFFLE_AUTO == randomMode) {
                mRepeatButton.setImageResource(R.drawable.shuffle_on);
                repeatMode = MediaPlaybackService.REPEAT_NONE;
                return;
            }
            if (MediaPlaybackService.REPEAT_CURRENT == repeatMode) {
                mRepeatButton.setImageResource(R.drawable.repeat_one);
            } else if (MediaPlaybackService.REPEAT_ALL == repeatMode){
                mRepeatButton.setImageResource(R.drawable.repeat_all);
            }
            randomMode = MediaPlaybackService.SHUFFLE_NONE;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	OnTouchListener tl = new OnTouchListener(){

        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            // TODO Auto-generated method stub
            if(v.getId() == R.id.repeatSwitch) {
                if(repeatMode == MediaPlaybackService.REPEAT_ALL){
                    repeatMode = MediaPlaybackService.REPEAT_CURRENT;
                    randomMode = MediaPlaybackService.SHUFFLE_NONE;
                    mRepeatButton.setImageResource(R.drawable.repeat_one);
                    Toast.makeText(mContext, R.string.repeat_mode_one, Toast.LENGTH_SHORT).show();
                    try {
                        mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
                        mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                } else if(repeatMode == MediaPlaybackService.REPEAT_CURRENT) {
                    repeatMode = MediaPlaybackService.REPEAT_NONE;
                    randomMode = MediaPlaybackService.SHUFFLE_AUTO;
                    mRepeatButton.setImageResource(R.drawable.shuffle_on);
                    Toast.makeText(mContext, R.string.shuffle_mode_on, 2000).show();
                    try {
                        mService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
                        mService.setRepeatMode(MediaPlaybackService.REPEAT_NONE);
                        if(cll != null) {
                            cll.updateNowplayingCursor();
                        }
                        mIsShuffle = true;
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } else if(randomMode == MediaPlaybackService.SHUFFLE_AUTO || 
                        randomMode == MediaPlaybackService.SHUFFLE_NORMAL) {
                    randomMode = MediaPlaybackService.SHUFFLE_NONE;
                    repeatMode = MediaPlaybackService.REPEAT_ALL;
                    mRepeatButton.setImageResource(R.drawable.repeat_all);
                    Toast.makeText(mContext, R.string.repeat_mode_all, Toast.LENGTH_SHORT).show();
                    try {
                        mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                        mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } else if(v.getId() == R.id.eqSetting) {
                Intent intent = new Intent();
                intent.setClass(mContext, MusicEQActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
            return false;
        }

    };

	public void updateTrackInfoColor(){
		if(repeatMode == MediaPlaybackService.REPEAT_CURRENT) {
			nextTrack1.setTextColor(Color.DKGRAY);
			nextTrack2.setTextColor(Color.DKGRAY);
			nextTrack3.setTextColor(Color.DKGRAY);
			nextTrack4.setTextColor(Color.DKGRAY);
		} else {
			nextTrack1.setTextColor(Color.GRAY);
			nextTrack2.setTextColor(Color.GRAY);
			nextTrack3.setTextColor(Color.GRAY);
			nextTrack4.setTextColor(Color.GRAY);
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	public void updateTrackInfo(String artistName, String trackName) {    //nextTrack[]

        /*int index = trackName.indexOf('-');
        if (index > 0) {
            if (artistName.equals("unknown"))
                artistName = trackName.substring(0, index);
            trackName = trackName.substring(index + 1);
            if (trackName.startsWith(new String(" ")))
                trackName = trackName.substring(1);
        }*/

        artist.setText(artistName);
        trackname.setText(trackName);

//        updateTrackNext();
	}

	public void updateTrackNext() {
	    String [] nextTrack = new String[4];
        nextTrack = MusicUtils.getTrackNameNext(mContext);

		if(nextTrack[0] != null)
		nextTrack1.setText(nextTrack[0]);
		else
			nextTrack1.setText("");
		if(nextTrack[1] != null)
		nextTrack2.setText(nextTrack[1]);
		else
			nextTrack2.setText("");
		if(nextTrack[2] != null)
		nextTrack3.setText(nextTrack[2]);
		else
			nextTrack3.setText("");
		if(nextTrack[3] != null)
		nextTrack4.setText(nextTrack[3]);
		else
			nextTrack4.setText("");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        //menu.add(0, GOTO_START, 0, R.string.goto_start).setIcon(R.drawable.library);
//        menu.add(0, SHARE_LIST, 0, R.string.share_list).setIcon(R.drawable.);
		menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
		menu.add(0, DELETE_ITEM, 0, R.string.delete_item).setIcon(R.drawable.delete);
		menu.add(0, MODIFY_ID3, 0, R.string.memu_modifyID3).setIcon(R.drawable.edit_id3);
		SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
        MusicUtils.makePlaylistMenu(this, sub);
        sub.setIcon(R.drawable.add_to_playlist); 
//        menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu).setIcon(R.drawable.set_as_ringtone);
        menu.add(0, EQ_SETTING, 0, R.string.eq_setting).setIcon(R.drawable.eq_menu);
        menu.add(0, SETTINGS, 0, R.string.settings).setIcon(R.drawable.setting);

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if(PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0) == 0) {
            menu.getItem(0).setTitle(R.string.sleep_start);
        } else {
            menu.getItem(0).setTitle(R.string.sleep_close);
        }        
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        try {
            switch (item.getItemId()) {
                case GOTO_START:
                    intent = new Intent();
                    intent.setClass(this, MusicMainEntryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case SETTINGS:
                    Intent musicPreferencesIntent = new Intent().setClass(this, MusicSetting.class);
                    musicPreferencesIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(musicPreferencesIntent);
                    break;
                case USE_AS_RINGTONE: {
                    // Set the system setting to make this the current ringtone
                    if (mService != null) {
                        MusicUtils.setRingtone(this, mService.getAudioId());
                    }
                    return true;
                }
                case NEW_PLAYLIST: {
                    long[] list = new long[] {mService.getAudioId()};
                    MusicUtils.addToNewPlaylist(this, list, NEW_PLAYLIST);
                    return true;
                }

                case PLAYLIST_SELECTED: {
                    long [] list = new long[1];
                    list[0] = MusicUtils.getCurrentAudioId();
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(this, list, playlist);
                    return true;
                }

                case DELETE_ITEM: {
                    if (mService != null) {
                        long [] list = new long[1];
                        list[0] = MusicUtils.getCurrentAudioId();
                        String f;
                        if (android.os.Environment.isExternalStorageRemovable()) {
                            f = getString(R.string.delete_song_desc, mService.getTrackName());
                        } else {
                            f = getString(R.string.delete_song_desc_nosdcard, mService.getTrackName());
                        }
                        MusicUtils.deleteItems(this, f, list);
                    }
                    return true;
                }
                
                case SLEEP: {
                    new SleepModeManager(this);
                    return true;
                }
                case MODIFY_ID3: {
                	Intent id3Intent = new Intent();
                	id3Intent.setClass(mContext, ModifyTrackID3Activity.class);
                	id3Intent.putExtra("TrackId", MusicUtils.getCurrentAudioId());
					id3Intent.putExtra("album", MusicUtils.getAlbumName(mContext, MusicUtils.getCurrentAlbumId()));
                	id3Intent.putExtra("artist", artist.getText().toString());
                	id3Intent.putExtra("track", trackname.getText().toString());
                	id3Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                	this.startActivity(id3Intent);
                	return true;
                }
                case EQ_SETTING: {
                    intent = new Intent();
                    intent.setClass(mContext, MusicEQActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return true;
                }
            }
        } catch (RemoteException ex) {
        }
        return super.onOptionsItemSelected(item);
    }
    public int getRandomMode() {
        return randomMode;
    }
    public boolean getIsShuffle() {
        return mIsShuffle;
    }
    
    public void setIsShuffle(boolean isShuffle) {
        mIsShuffle = isShuffle;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
        if(v.getId() == R.id.repeatSwitch) {
            if(repeatMode == MediaPlaybackService.REPEAT_ALL){
                repeatMode = MediaPlaybackService.REPEAT_CURRENT;
                randomMode = MediaPlaybackService.SHUFFLE_NONE;
                mRepeatButton.setImageResource(R.drawable.repeat_one_selector);
                Toast.makeText(mContext, R.string.repeat_mode_one, Toast.LENGTH_SHORT).show();
                try {
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            } else if(repeatMode == MediaPlaybackService.REPEAT_CURRENT) {
                repeatMode = MediaPlaybackService.REPEAT_NONE;
                randomMode = MediaPlaybackService.SHUFFLE_AUTO;
                mRepeatButton.setImageResource(R.drawable.shuffle_on_selector);
                Toast.makeText(mContext, R.string.shuffle_mode_on, 2000).show();
                try {
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_NONE);
                    if(cll != null) {
                        cll.updateNowplayingCursor();
                    }
                    mIsShuffle = true;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else if(randomMode == MediaPlaybackService.SHUFFLE_AUTO || 
                    randomMode == MediaPlaybackService.SHUFFLE_NORMAL) {
                randomMode = MediaPlaybackService.SHUFFLE_NONE;
                repeatMode = MediaPlaybackService.REPEAT_ALL;
                mRepeatButton.setImageResource(R.drawable.repeat_all_selector);
                Toast.makeText(mContext, R.string.repeat_mode_all, Toast.LENGTH_SHORT).show();
                try {
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } else if(v.getId() == R.id.eqSetting) {
            Intent intent = new Intent();
            intent.setClass(mContext, MusicEQActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else if(v.getId() == R.id.backtomain) {
            Intent intent = new Intent();
            intent.setClass(this, MusicMainEntryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
