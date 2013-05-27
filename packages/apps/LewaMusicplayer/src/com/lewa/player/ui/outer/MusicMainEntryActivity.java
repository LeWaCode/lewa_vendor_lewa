package com.lewa.player.ui.outer;

import java.util.ArrayList;

import com.lewa.os.ui.ViewPagerIndicatorActivity;
import com.lewa.player.ExitApplication;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MusicSetting;
import com.lewa.player.MusicUtils;
import com.lewa.player.MusicUtils.ServiceToken;
import com.lewa.player.R;
import com.lewa.player.SleepModeManager;
import com.lewa.player.online.DownLoadAllPicsAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ui.MusicFolderActivity;
import com.lewa.player.ui.NowPlayingController;
import com.lewa.player.ui.SearchLocalSongsActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MusicMainEntryActivity extends ViewPagerIndicatorActivity implements MusicUtils.Defs{
	
    private static final int ID_MUSIC_NO_SONGS_CONTENT = 100;  
    private static final int ID_DIALOG_NO_NETWORK = 0;
    private static final int ID_DIALOG_NOT_WIFI = 1;
    private static final int ID_DIALOG_DOWNLOAD = 2;
    private static final int ID_DIALOG_CONFIRM_CANCEL = 3;
    private static final int ID_DIALOG_DOWNLOAD_RESULT = 4;
    
    private final int SUB_ACTIVITY_COUNT = 5;
    private ArtistBrowserActivity artistActivity;
	private PlaylistBrowserActivity PlaylistActivity;
	private AlbumBrowserActivity ablumActivity;
	private Bitmap bitmapbg;
	IMediaPlaybackService mService;
	private NowPlayingController actionbar;
	ServiceToken mToken;
	ProgressDialog mProgress;
	private ContentResolver mResolver;
	private Context mContext;
	private Bundle metaData = null;
	private String trackingKey = "";
	public static final String GA_TRACKING_KEY = "ga-tracking-key";
	private LinearLayout mLinear;
	private AlertDialog mDownloadDialog;
	private static int mAlbumCount;
	private static int mArtistCount;
	private DownLoadAllPicsAsync mDownImgAsync;
	private boolean mIsDownloadStop;
	private AlertDialog mDownloadFinishDialog;
	private boolean mIsDownloadFinish;
	private static final String PREFS_NAME = "player.lewa.com";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		boolean fisrtTimeOpen = settings.getBoolean("firstTimeOpen", true);
		if(fisrtTimeOpen && MusicUtils.mHasSongs){
			Builder builder = new Builder(MusicMainEntryActivity.this);
			builder.setMessage(R.string.first_dialog_message);
			builder.setTitle(R.string.first_dialog_title);
			builder.setPositiveButton(R.string.dialog_positive, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent();
	                intent.setClass(MusicMainEntryActivity.this, MusicFolderActivity.class);
	                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(intent);
				}
			});
			builder.setNegativeButton(R.string.dialog_negative, new OnClickListener() {
			@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			//builder
			//builder.
//			Dialog dialog = builder.create();
//			Window dialogWindow = dialog.getWindow();
//			WindowManager.LayoutParams lp = dialogWindow.getAttributes();
//			dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
//			lp.x = 100; // ??¦Ë??X???
//	        lp.y = 120; // ??¦Ë??Y???
//	        dialogWindow.setAttributes(lp);
//	        dialog.show();
			builder.create().show();
			
			SharedPreferences.Editor editor = settings.edit(); 
			editor.putBoolean("firstTimeOpen", false);
			editor.commit();
		}
		
		// TODO Auto-generated method stub
		if (MusicUtils.ifHasSongs(this) == false) {
		    MusicUtils.mHasSongs = false;
		}
		
        ArrayList<StartParameter> aClasses = new ArrayList<StartParameter>();
//        Intent genreIntent = new Intent();
//        genreIntent.putExtra("delayloadcontent", true);
//        aClasses.add(new StartParameter(GenreBrowserActivity.class,
//                genreIntent, R.string.title_outer_genre));

//        Intent alltrackIntent = new Intent();
//        alltrackIntent.putExtra("delayloadcontent", true);
//        aClasses.add(new StartParameter(AllTrackBrowserActivity.class, null,
//                R.string.title_outer_alltrack));
        
        Intent artistIntent = new Intent();
        artistIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(ArtistBrowserActivity.class,
                artistIntent, R.string.title_outer_artist));

        Intent playlistIntent = new Intent();
        playlistIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(PlaylistBrowserActivity.class, null,
                R.string.title_outer_playlist));
        
        Intent albumIntent = new Intent();
        albumIntent.putExtra("delayloadcontent", true);
        aClasses.add(new StartParameter(AlbumBrowserActivity.class,
                albumIntent, R.string.title_outer_album));
		
		setupFlingParm(aClasses, R.layout.mainentry, R.id.indicator_outer,
				R.id.pager_outer);		
		setDisplayScreen(1);
		setIfMusic(true);
		
		mToken = MusicUtils.bindToService(this, osc);
		if (mToken == null) {
			
		}
		super.onCreate(savedInstanceState);
        
		artistActivity = (ArtistBrowserActivity) super.getItemActivity(0);
        PlaylistActivity = (PlaylistBrowserActivity) super.getItemActivity(1);
        ablumActivity = (AlbumBrowserActivity) super.getItemActivity(2);
        actionbar = (NowPlayingController)this.findViewById(R.id.actionbar);
        
        mLinear = (LinearLayout)findViewById(R.id.linear_page);
        mLinear.post(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                int id = R.drawable.playlist_default;
                MusicUtils.setDefaultBackground(mContext, mLinear, id);
                Bitmap back = MusicUtils.getDefaultBg(mContext, id);
                setGridView(back);
            }
        });     
        mResolver = getContentResolver();
        mContext = this;

        registerProviderStatusObserver();
        
        IntentFilter f = new IntentFilter();
        f.addAction(OnlineLoader.UPDATEBG);
        f.addAction(OnlineLoader.GET_PIC_ACTION);
        f.addAction(OnlineLoader.STOPDOWNLOAD);
        f.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        f.addAction(MusicUtils.ACTION_DELETEITEM);
        registerReceiver(updateui, new IntentFilter(f));        
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        registerReceiver(mScanListener, intentFilter);
        registerReceiver(ScanSdFilesReceiver, intentFilter);
        
        checkHasSongs(); 
        setupNoSongsView();
        
        ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
        OnlineLoader.setContext(this);
	}
	
	private void registerProviderStatusObserver() {
        mResolver.registerContentObserver(Uri.parse("content://media"),
                true, mProviderStatusObserver);
    }

    private void unregisterProviderStatusObserver() {
        mResolver.unregisterContentObserver(mProviderStatusObserver);
    }
    
    private ContentObserver mProviderStatusObserver = new ContentObserver(new Handler()) {

        @Override
        public void onChange(boolean selfChange) {
            checkHasSongs();
        }
    };
    
    private void checkHasSongs() {
        if (MusicUtils.getAllSongs(mContext) == null) {
            MusicUtils.mHasSongs = false;
        } else {
            MusicUtils.mHasSongs = true;
        }
        //setupNoSongsView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
	    super.onNewIntent(intent);
        setDisplayScreen(1);
        Bundle bundle = intent.getExtras();
        if(bundle != null && bundle.getBoolean("folder")) {
            mProgress = ProgressDialog.show(this, getString(R.string.synchron_title), 
                    getString(R.string.synchron_message), true);
            onCreate(bundle);
        }        
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		actionbar.destroyNowplaying();
		//PlaylistActivity.onDestroy();
		this.unregisterReceiver(updateui);
		this.unregisterReceiver(mScanListener);
		MusicUtils.unbindFromService(mToken);
		unregisterProviderStatusObserver();
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String name = this.getResources().getString(R.string.title_actionnow);
//		setupNoSongsView();
		
        mLinear.post(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                int id = R.drawable.playlist_default;
                MusicUtils.setDefaultBackground(mContext, mLinear, id);
                Bitmap back = MusicUtils.getDefaultBg(mContext, id);
                setGridView(back);
                
                if(mIsDownloadFinish){
                    if(mDownloadFinishDialog != null){
                        mDownloadFinishDialog.dismiss();
                        mDownloadFinishDialog = null;
                        mIsDownloadFinish = false;
                    }
                }        
                if (mDownloadFinishDialog != null && !mDownloadFinishDialog.isShowing()) {                    
                    mDownloadFinishDialog.show();
                }
            }
        });
                
        PlaylistActivity.onResume();        
	}

	public void setArtistBackground(Bitmap back) {
		if (back == null) return;
		OnlineLoader.setContext(this);
	//	MusicUtils.setBackground(getWindow().getDecorView(), back);
		MusicUtils.setBackground(mLinear, back);
	}
	
	public void setGridView(Bitmap back) {
		//if(back == null)return;
		PlaylistActivity.setGridViewBack(back);		
	}
	
	private ServiceConnection osc = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			mService = IMediaPlaybackService.Stub.asInterface(obj);
			actionbar.setMediaService(mService);
			
	        IntentFilter f = new IntentFilter();
	        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
	        f.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
	        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
	        f.addDataScheme("file");
			registerReceiver(mScanListener, f);
		}
		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};

	BroadcastReceiver updateui = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent i) {
			// TODO Auto-generated method stub
		    if(mProgress != null && mProgress.isShowing()){
                mProgress.cancel();
                mProgress = null;
                try {
                    if(mService.getQueuePosition() >= 0) {
                        Toast.makeText(mContext, R.string.nowplaying_filter, Toast.LENGTH_SHORT).show();
                    }
                    mService.removeTracks(0, MusicUtils.getAllSongsInDB(mContext).length);
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
			if(OnlineLoader.UPDATEBG.equals(i.getAction())) {
				
				Parcelable p = i.getParcelableExtra("backg");
			//	bitmapbg = MusicUtils.getDefaultBg(mContext, R.drawable.playlist_default);//(Bitmap)p;
			//	if(i.getFlags() == 2){					
			//		setArtistBackground(bitmapbg);
					bitmapbg = (Bitmap)p;
					setGridView(bitmapbg);
			//	} 
//				else {
//					setArtistBackground(bitmapbg);
//				}
			}
			if(OnlineLoader.GET_PIC_ACTION.equals(i.getAction())) {
			    if(i.getFlags() == OnlineLoader.ALBUMDOWNLOAD) {
			        mAlbumCount = i.getIntExtra("count", 0);
			    }
			    if(i.getFlags() == OnlineLoader.ARTISTDOWNLOAD) {
			        mArtistCount = i.getIntExtra("count", 0);
                }
			    int currentCount = mAlbumCount + mArtistCount;
		//	    mCurrentView.setText(currentCount + "");
			    
			    if(mDownloadDialog != null) {
			        mDownloadDialog.setMessage(getString(R.string.downloading) + currentCount);	
			    }
			}
			if(OnlineLoader.STOPDOWNLOAD.equals(i.getAction())) {
			    if(mDownImgAsync != null && !mIsDownloadStop) {
			        //after album download, do artist download
                     if(i.getFlags() == OnlineLoader.ALBUMDOWNLOAD) {
                        artistActivity.getAllArtistPics();
                        mDownImgAsync = artistActivity.getDownloadAsync();
                           
                        int count = i.getIntExtra("count", mAlbumCount);
                        if(count != mAlbumCount) {
                            mAlbumCount = count;
                        }
                        return;
                    }
                }			    
			        
			    if(mDownloadDialog != null) {
                    mDownloadDialog.dismiss();
			    }                    
                    
                int count = i.getIntExtra("count", mArtistCount);
                if (!mIsDownloadStop && count != mArtistCount) {
                    mArtistCount = count;
                }
                int totalCount = mAlbumCount + mArtistCount;
                if(totalCount > 0) {
                    onCreate(i.getExtras());
                }
                
                //show result dialog
                showDialog(ID_DIALOG_DOWNLOAD_RESULT);
                if(mDownloadFinishDialog != null){
                    mDownloadFinishDialog.show();
                }
                    
                mDownImgAsync = null;
                mAlbumCount = 0;
                mArtistCount = 0;
                mIsDownloadStop = false;
			}
			if(ConnectivityManager.CONNECTIVITY_ACTION.equals(i.getAction())) {			    
                boolean isConnection = OnlineLoader.isWiFiActive(MusicMainEntryActivity.this)
                        || OnlineLoader.IsConnection();
                if(!isConnection && !mIsDownloadStop) {
                    if(mDownImgAsync != null) {
                        mDownImgAsync.setStopFlag(true);
                    }
                    mIsDownloadStop = true;
                }
			}
			if(MusicUtils.ACTION_DELETEITEM.equals(i.getAction())) {
			    onCreate(i.getExtras());
			}
		}
	};
	
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
                    || Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                MusicUtils.setSpinnerState(MusicMainEntryActivity.this);
            }
            if(mDownloadDialog != null) {
                mDownloadDialog.dismiss();
                mDownloadDialog = null;
            }
            if(mDownImgAsync != null) {
                mDownImgAsync = null;
            }
            mIsDownloadStop = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, SLEEP, 0, R.string.sleep_start).setIcon(R.drawable.sleep_mode);
        menu.add(0, GET_PIC, 0, R.string.download_menu).setIcon(R.drawable.get_pic);
        menu.add(0, REFRESH, 0, R.string.refresh).setIcon(R.drawable.refresh);
//        menu.add(0, FOLDER, 0, R.string.folder).setIcon(R.drawable.folder);
        menu.add(0, SEARCH, 0, R.string.search).setIcon(R.drawable.search);
        menu.add(0, SETTINGS, 0, R.string.settings).setIcon(R.drawable.setting);
        menu.add(0, EXIT, 0, R.string.music_exit).setIcon(R.drawable.exit);        
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        if(!IsCanUseSdCard()) {
            menu.getItem(0).setEnabled(false);
            menu.getItem(1).setEnabled(false);
            menu.getItem(2).setEnabled(false);
            menu.getItem(3).setEnabled(false);
        } else {
            menu.getItem(0).setEnabled(true);
            menu.getItem(1).setEnabled(true);
            menu.getItem(2).setEnabled(true);
            menu.getItem(3).setEnabled(true);
        }        

        if(PreferenceManager.getDefaultSharedPreferences(this).getInt("sleep_mode_time", 0) == 0) {
            menu.getItem(0).setTitle(R.string.sleep_start);
        } else {
            menu.getItem(0).setTitle(R.string.sleep_close);
        }
        
        return true;
    }

    public static boolean IsCanUseSdCard() { 
        try { 
            return Environment.getExternalStorageState().equals( 
                    Environment.MEDIA_MOUNTED); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } 
        return false; 
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case SEARCH: {
                Intent intent = new Intent();
                intent.setClass(this, SearchLocalSongsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            case SETTINGS: {
                showCustomConfig();
                return true;
            }
            case FOLDER: {
                Intent intent = new Intent();
                intent.setClass(this, MusicFolderActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            }
            case SLEEP: {
                new SleepModeManager(this);
                return true;
            }
            case EXIT: {
                SleepModeManager.setSleepTime(mContext, 0);
                SleepModeManager.deleteSleepTime(mContext);
                try {
                    if (mService != null && mService.isPlaying()) {
                        mService.pause();
                    }
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                Intent intent = new Intent(Intent.ACTION_MAIN);   
                intent.addCategory(Intent.CATEGORY_HOME);   
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                
                startActivity(intent);   
                android.os.Process.killProcess(android.os.Process.myPid());
                MusicUtils.unbindFromService(mToken);
                return true;
            }            
            case GET_PIC: {
                doPicDownload();                
                return true;
            }
            case REFRESH: {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, 
                        Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                mProgress = ProgressDialog.show(mContext, getString(R.string.synchron_title), 
                        getString(R.string.synchron_message), true);
                return true;
            }
        }
        return false;
    }
    
    private BroadcastReceiver ScanSdFilesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                
            }
            if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                if(mProgress != null && mProgress.isShowing()) {
                    mProgress.dismiss();
                    mProgress = null;
                }
                artistActivity.setAdapter(null);
                artistActivity.initAdapter();
                PlaylistActivity.resetPlaylistView();
                ablumActivity.setAdapter(null);
                ablumActivity.initAdapter();
                setDisplayScreen(1);
            }
        }
    }; 
    
    private void doPicDownload() {
        if(OnlineLoader.IsConnection()) {
            //2g/3g
            showDialog(ID_DIALOG_NOT_WIFI);
        } else if(OnlineLoader.isWiFiActive(this)) {
            //wifi            
            showDialog(ID_DIALOG_DOWNLOAD);            
        } else {
            //no network
            showDialog(ID_DIALOG_NO_NETWORK);
        }
    }
    
    private void showCustomConfig() {
        Intent musicPreferencesIntent = new Intent().setClass(this, MusicSetting.class);
        startActivity(musicPreferencesIntent);
    }
    
    public void setupNoSongsView() {
        
        PlaylistActivity.setupNoSongsView();
        actionbar.setNoSongPlayingView();
        
        for (int i = 0; i < SUB_ACTIVITY_COUNT; i++) {
            if (i == 1)
                continue;
            
            Activity activity = super.getItemActivity(i);
            if (activity != null) {
                View view = activity.findViewById(ID_MUSIC_NO_SONGS_CONTENT);;
                if (MusicUtils.mHasSongs == false) {
                    if (view != null) {
                        view.setVisibility(View.VISIBLE);
                    } else {
                        view = activity.getLayoutInflater().inflate(R.layout.music_no_songs, ((ListActivity)activity).getListView(), false);    
                        view.setId(ID_MUSIC_NO_SONGS_CONTENT);
                        activity.addContentView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                    }
                    TextView txtView = (TextView)view.findViewById(R.id.text_no_songs);
                    String status = Environment.getExternalStorageState();
                    if ( !(status.equals(Environment.MEDIA_MOUNTED))) {
                        txtView.setText(R.string.nosd);
                    } else {
                        txtView.setText(R.string.no_songs);
                    }
                } else {
                    if (view != null)
                        view.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        // TODO Auto-generated method stub
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        String message = "";
        
        switch(id) {
            case ID_DIALOG_NO_NETWORK :
                message = getString(R.string.no_network_tip);
                break;
            case ID_DIALOG_NOT_WIFI :
                message = getString(R.string.download_tip);
                break;
            case ID_DIALOG_DOWNLOAD:
                int count = mArtistCount + mAlbumCount;
                message = getString(R.string.downloading) + count;            
//                if(mDownImgAsync == null) {
//                    ablumActivity.getAllAlbumPics();
//                    mDownImgAsync = ablumActivity.getDownloadAsync();
//                }
                break;
            case ID_DIALOG_CONFIRM_CANCEL: {
                message = getString(R.string.download_break_hint);
                break;
            }
            case ID_DIALOG_DOWNLOAD_RESULT: {
                String f = getString(R.string.download_finish);
                String finishTip;
                if(mIsDownloadStop) {
                    finishTip = getString(R.string.getpicbreak);
                } else {
                    finishTip = getString(R.string.getpicfinish);
                }
                int totalCount = mArtistCount + mAlbumCount;
                message = String.format(f, finishTip, totalCount, mArtistCount, mAlbumCount);
            }
        }
        builder.setTitle(R.string.download_menu);
        builder.setMessage(message);
        
        int positiveName = R.string.ok;
        if(id == ID_DIALOG_NO_NETWORK) {
            positiveName = R.string.set_network;
        }
        if(id != ID_DIALOG_DOWNLOAD && id != ID_DIALOG_DOWNLOAD_RESULT) {
            builder.setPositiveButton(positiveName, new OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    dialog.dismiss();
                    switch(id){
                        case ID_DIALOG_NO_NETWORK: {
                            //skip to set network
                            Intent intent = new Intent("android.settings.SETTINGS");
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mContext.startActivity(intent);
                            break;
                        }
                        
                        case ID_DIALOG_NOT_WIFI: {
                            //show downloading
                            showDialog(ID_DIALOG_DOWNLOAD);
//                            OnlineLoader.setContext(mContext);
//                            ablumActivity.getAllAlbumPics();
                            break;
                        }
                        
                        case ID_DIALOG_CONFIRM_CANCEL: {
                            //stop downloading,set stop flag
                            if(mDownImgAsync != null) {
                                mDownImgAsync.setStopFlag(true);
                            }
                            mIsDownloadStop = true;
                            dialog.dismiss();
                            break;
                        }
                    }
                }
            });
        }
        
        int negativeName = R.string.cancel;
        if(id == ID_DIALOG_DOWNLOAD_RESULT) {
            negativeName = R.string.ok;
        }
        builder.setNegativeButton(negativeName, new OnClickListener() {
            
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                dialog.dismiss();
                switch(id) {
                    case ID_DIALOG_CONFIRM_CANCEL: {
                        showDialog(ID_DIALOG_DOWNLOAD);
                        break;
                    }
                    case ID_DIALOG_DOWNLOAD_RESULT: {
                        mDownloadFinishDialog = null;
                        break;
                    }
                    case ID_DIALOG_DOWNLOAD : {
                        showDialog(ID_DIALOG_CONFIRM_CANCEL);
                        break;
                    } 
                }                
            }
        });
        
        dialog = builder.create();
        
        if(id == ID_DIALOG_DOWNLOAD) {
            mDownloadDialog = (AlertDialog) dialog;
        }
        if(id == ID_DIALOG_DOWNLOAD_RESULT) {
            mDownloadFinishDialog = (AlertDialog) dialog;
        }
        
        dialog.show();
        return dialog;        
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        // TODO Auto-generated method stub
        String message = "";
        
        switch(id) {
            case ID_DIALOG_NO_NETWORK :
                message = getString(R.string.no_network_tip);
                break;
            case ID_DIALOG_NOT_WIFI :
                message = getString(R.string.download_tip);
                break;
            case ID_DIALOG_DOWNLOAD:
                int count = mArtistCount + mAlbumCount;
                message = getString(R.string.downloading) + count;            
                if(mDownImgAsync == null) {
                    ablumActivity.getAllAlbumPics();
                    mDownImgAsync = ablumActivity.getDownloadAsync();
                }             
                break;
            case ID_DIALOG_CONFIRM_CANCEL: {
                message = getString(R.string.download_break_hint);
                break;
            }
            case ID_DIALOG_DOWNLOAD_RESULT: {
                String f = getString(R.string.download_finish);
                String finishTip;
                if(mIsDownloadStop) {
                    finishTip = getString(R.string.getpicbreak);
                } else {
                    finishTip = getString(R.string.getpicfinish);
                }
                int totalCount = mArtistCount + mAlbumCount;
                message = String.format(f, finishTip, totalCount, mArtistCount, mAlbumCount);
                break;
            }
        }
        ((AlertDialog)dialog).setMessage(message);
        dialog.setTitle(R.string.download_menu);
    }
    
}
