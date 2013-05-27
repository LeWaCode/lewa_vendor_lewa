package com.lewa.player.ui;

import java.io.File;


import com.lewa.player.ExitApplication;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.online.SearchLRC;
import com.lewa.player.ui.outer.MusicMainEntryActivity;
import com.lewa.player.ui.view.Lyric.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ScrollView;
import android.widget.TextView;

public class LyricActivity extends Activity implements MusicUtils.Defs {

	
	public LyricView lyv;
	private Lyric mLyric;
	private ScrollView sv;
	private PlayListItem currentLrc;
	private boolean hasLrc = false;
	private long currentLRCId;
	private String curTrackName;
    public int ScreenDisertyDpi = 0;
    private IMediaPlaybackService mService;
    private String curArtistName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
        DisplayMetrics dm= new DisplayMetrics();
        
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ScreenDisertyDpi =  dm.densityDpi;
		super.onCreate(savedInstanceState);
	
		IntentFilter filter = new IntentFilter();
		filter.addAction(MediaPlaybackService.META_CHANGED);
		filter.addAction(OnlineLoader.UPDATELRC);
		this.registerReceiver(receiver, filter);
		if(lyv != null){
			this.setContentView(sv);
		}
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub		
		super.onSaveInstanceState(outState);
	}


	BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(intent.getAction().equals(MediaPlaybackService.META_CHANGED)) {

				//setLyric(intent.getStringExtra("track"), intent.getLongExtra("id", -1), intent.getStringExtra("artist"));
				
			}else if(intent.getAction().equals(OnlineLoader.UPDATELRC)) {
				int stat = intent.getIntExtra("downStat", -1);
				if(stat >= 0) {
					updateDownLRC(intent.getLongExtra("id", -1));
				}else {
					setLyricNOlrc(1);
				}
				
			}
		}
		
	};
	
	public void updateDownLRC(long trackid) {
		if(currentLRCId == trackid) {
			setLyric(curTrackName, trackid, null);
		}
	}
	
	public void setLyricActivity(String mTrackName, String artistName,
			long songid) {
		// TODO Auto-generated method stub
		setLyric(mTrackName, songid, artistName);		
	}	

	public void setLyric(String trackName, long songid, String ArtistName) {
		if(songid > 0 && trackName != null) {
			currentLRCId = songid;
			/*int index = trackName.indexOf('-');
	        if (index > 0) {
	            if (ArtistName.equals("unknown"))
	                ArtistName = trackName.substring(0, index);
	            trackName = trackName.substring(index + 1);
	            if (trackName.startsWith(new String(" ")))
	                trackName = trackName.substring(1);
	        }*/
			curTrackName = trackName;
			curArtistName = ArtistName;
			File lrcfile = null;
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{   
				
			    String sdCardDir = Environment.getExternalStorageDirectory() + SearchLRC.LRC_PATH;
			    //modify by zhaolei,120327,for lrc save
    			lrcfile = new File(sdCardDir + trackName);   //String.valueOf(songid)
    			//end
    			if(lrcfile.exists()) {
    				setLrcFile(trackName, lrcfile);
    			}else if((lrcfile = setLocalLrc(songid)) != null){
    				setLrcFile(trackName, lrcfile);
    			}else {
    				OnlineLoader.getSongLrc(trackName, ArtistName, songid);
    				//lyv.setmLyric(mLyric);
    				setLyricNOlrc(0);
    			}
			}

		}
		
	}
	
	private void setLrcFile(String trackName, File lrcfile) {
		currentLrc = new PlayListItem(trackName, null, 0L, true);
		long totalTime = 0;
        try {
            totalTime = MusicUtils.sService.duration();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		mLyric = new Lyric(lrcfile, currentLrc, totalTime);
		if(mLyric.list.size()<3){
			setLyricNOlrc(1);
		}else{
			setLyricView();
		}
		
	}
	
	private File setLocalLrc(long trackId) {
		String trackPath = MusicUtils.getSongPath(this, trackId);
		if(trackPath == null) {
			return null;
		}
		String LrcPath = trackPath.substring(0, trackPath.lastIndexOf("."));
		LrcPath = LrcPath + ".lrc";
		File lrcfile = new File(LrcPath);
		if(lrcfile.exists()) {
			return lrcfile;
		}else {
			return null;
		}

	}
	
    private void setLyricNOlrc(int ifnolrc) {
		// TODO Auto-generated method stub
        TextView tv = new TextView(this);
        int resourceId = R.string.loadlrc;
        if(ifnolrc == 1){
        	resourceId = R.string.lrc_down_notfound;
        } 
        if(OnlineLoader.downArtorLrc(1) == 1) {
            if (!((OnlineLoader.IsConnection()) || (OnlineLoader.isWiFiActive(this)))){
                resourceId = R.string.no_network;
            }
        }else {
            resourceId = R.string.download_lrc_off_hint;
        }
        

        tv.setText(resourceId);
        tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        tv.setGravity(Gravity.CENTER);
        setContentView(tv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }


	public void setLyricView() {
		
		lyv = new LyricView(this, ScreenDisertyDpi);
		
		lyv.setmLyric(mLyric);
		lyv.setSentencelist(mLyric.list);
		lyv.setBackgroundDrawable(null);
		if(ScreenDisertyDpi > 160) {
			setContentView(lyv, new LayoutParams(480,600));	
			lyv.setPadding(0, 130, 0, 0);
		} else {
			setContentView(lyv, new LayoutParams(320,350));	
			lyv.setPadding(0, 120, 0, 0);
		}

		//lyv.setHeight(340);
		lyv.setNotCurrentPaintColor(0xFFADADAD);
		lyv.setCurrentPaintColor(Color.WHITE);
		//lyv.setLrcTextSize(20);
		lyv.setTexttypeface(Typeface.DEFAULT);
		lyv.setBrackgroundcolor(0);
		lyv.setBackgroundDrawable(null);
		lyv.setService(mService);
		hasLrc = true;
	}
	
	public void UpdateDuration(long duration) {
		if(hasLrc){
			lyv.updateIndex(duration);
		}

	}
	
	public void postUpdate() {
		if(hasLrc){
			mHandler.post(mUpdateResults);
		}

	}

	public Handler mHandler = new Handler();
	Runnable mUpdateResults = new Runnable() {
		public void run() {
			lyv.invalidate();
		}
	};
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(receiver);
	}
	
	public void setMediaService(IMediaPlaybackService service) {
        mService = service;
    }

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
      //menu.add(0, GOTO_START, 0, R.string.goto_start).setIcon(R.drawable.library);
//      menu.add(0, SHARE_LIST, 0, R.string.share_list).setIcon(R.drawable.);
      menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
      menu.add(0, DELETE_ITEM, 0, R.string.delete_item).setIcon(R.drawable.delete);
      menu.add(0, MODIFY_ID3, 0, R.string.memu_modifyID3).setIcon(R.drawable.edit_id3);
      SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0, R.string.add_to_playlist);
      MusicUtils.makePlaylistMenu(this, sub);
      sub.setIcon(R.drawable.add_to_playlist); 
//      menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu).setIcon(R.drawable.set_as_ringtone);
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
                            f = getString(R.string.delete_song_desc, curTrackName);
                        } else {
                            f = getString(R.string.delete_song_desc_nosdcard, curTrackName);
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
                    id3Intent.setClass(this, ModifyTrackID3Activity.class);
                    id3Intent.putExtra("TrackId", MusicUtils.getCurrentAudioId());
                    id3Intent.putExtra("album", MusicUtils.getAlbumName(this, MusicUtils.getCurrentAlbumId()));
                    id3Intent.putExtra("artist", curArtistName);
                    id3Intent.putExtra("track", curTrackName);
                    id3Intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    this.startActivity(id3Intent);
                    return true;
                }
                
                case EQ_SETTING: {
                    intent = new Intent();
                    intent.setClass(this, MusicEQActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    this.startActivity(intent);
                    return true;
                }
            }
        } catch (RemoteException ex) {
        }
        return super.onOptionsItemSelected(item);
    }


}
