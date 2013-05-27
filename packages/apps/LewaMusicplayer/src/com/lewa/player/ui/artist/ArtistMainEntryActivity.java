package com.lewa.player.ui.artist;

import java.util.ArrayList;

import com.lewa.os.ui.ViewPagerIndicatorActivity;
import com.lewa.player.ExitApplication;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.NowPlayingController;
import com.lewa.player.ui.SearchLocalSongsActivity;
import com.lewa.player.ui.outer.AlbumBrowserActivity;
import com.lewa.player.ui.outer.AllTrackBrowserActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

public class ArtistMainEntryActivity extends ViewPagerIndicatorActivity 
                        implements MusicUtils.Defs, View.OnTouchListener{

	
	private String artist = null;
	LocalAsync labg;
	Bitmap bitmapbg;
	private NowPlayingController Artistactionbar;
	private LinearLayout mLinear;
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		if(this.getIntent() != null) {
			artist = this.getIntent().getStringExtra("artist");
		}
		ArrayList<StartParameter> aClasses = new ArrayList<StartParameter>();
		Intent albumIntent = new Intent();
		albumIntent.putExtra("artist", artist);
		
		aClasses.add(new StartParameter(AllTrackBrowserActivity.class, albumIntent,
				R.string.title_artist_alltrack));
		aClasses.add(new StartParameter(AlbumBrowserActivity.class, albumIntent,
                R.string.title_artist_album));
		setupFlingParm(aClasses, R.layout.mainentry, R.id.indicator_outer,
				R.id.pager_outer);
		setDisplayScreen(0);
		setIfMusic(true);
		
		super.onCreate(savedInstanceState);	
		
		mContext = this;
		
//		ImageView backImg = (ImageView)findViewById(R.id.nowplayingimage);
//        backImg.setImageDrawable(null);
//        backImg.setImageResource(R.drawable.top_play);  //(R.drawable.top_back);
//        backImg.setOnTouchListener(this);
		
		Artistactionbar = (NowPlayingController) findViewById(R.id.actionbar);
		if(MusicUtils.sService != null) {
		    Artistactionbar.setMediaService(MusicUtils.sService);
        }
//		Artistactionbar.findViewById(R.id.nowplayingText).setOnTouchListener(this);
//        if(MusicUtils.sService != null) {
//            Artistactionbar.setMediaService(MusicUtils.sService);
//        }
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);		
	}	
	
	private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(ArtistMainEntryActivity.this);
			}
		}		
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
	    if(Artistactionbar != null) {
	        Artistactionbar.destroyNowplaying();
	        Artistactionbar = null;
	    }
//	    if(labg != null) {
//            labg.restorePreArtistName();
//        }
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
//        menu.add(0, FOLDER, 0, R.string.folder).setIcon(R.drawable.folder);
        menu.add(0, SEARCH, 0, R.string.search).setIcon(R.drawable.search);
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
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case SEARCH: {
                Intent intent = new Intent();
                intent.setClass(this, SearchLocalSongsActivity.class);
                startActivity(intent);
                return true;
            }
            case SETTINGS: {
                Intent musicPreferencesIntent = new Intent().setClass(this, MusicSetting.class);
                startActivity(musicPreferencesIntent);
                return true;
            }
            case FOLDER: {
                Intent intent = new Intent();
                intent.setClass(this, MusicFolderActivity.class);
                startActivity(intent);
                return true;
            }
            case SLEEP: {
                new SleepModeManager(this);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // TODO Auto-generated method stub
   //     finish();
        return false;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mLinear = (LinearLayout)findViewById(R.id.linear_page);
        mLinear.post(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                int id = R.drawable.playlist_default;
                MusicUtils.setDefaultBackground(mContext, mLinear, id);
            }
        });
    }
    
}
