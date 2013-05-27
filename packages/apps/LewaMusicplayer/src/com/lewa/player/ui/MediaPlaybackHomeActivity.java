package com.lewa.player.ui;

import java.util.ArrayList;

import com.lewa.os.ui.ViewPagerIndicatorActivity;
import com.lewa.player.ExitApplication;
import com.lewa.player.IMediaPlaybackService;
import com.lewa.player.MediaPlaybackService;
import com.lewa.player.MusicUtils;
import com.lewa.player.MusicUtils.ServiceToken;
import com.lewa.player.R;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.online.OnlineLoader;
import com.lewa.player.ui.outer.PlaylistBrowserActivity;
import com.lewa.player.ui.view.LyricViewv;
import com.lewa.player.ui.view.MediaPlaybackView;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MediaPlaybackHomeActivity extends ViewPagerIndicatorActivity 
    implements MusicUtils.Defs {

	private IMediaPlaybackService mService = null;

	private long mLastSeekEventTime;
    private ImageView mPrevButton;
    private ImageView mPauseButton;
    private ImageView mNextButton;
	private ServiceToken mToken;
	private boolean mFromTouch = false;
	private SeekBar playingSeek = null;
	private long mPosOverride = -1;
	private long mDuration;
	private boolean paused;
	private CurrentPlaylistActivity cl;
	private MediaPlaybackActivity mp;
	private LyricActivity lrcActivity;
	private Context mContext;
	private TextView mTotalTime;
	private TextView mCurrentTime;
	private LocalAsync labg;
    private Worker mAlbumArtWorker;
//    private AlbumArtHandler mAlbumArtHandler;

	private static final int REFRESH = 1;
	private static final int QUIT = 2;
	private static final int GET_ALBUM_ART = 3;
	private static final int ALBUM_ART_DECODED = 4;
	public static final String UPDATEPLAING = "com.lewa.launcher.outer.update";	

	private boolean randomAll = false;
	private RelativeLayout mMediaPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		/*
		 * mv = new MediaPlaybackView(this); lv = new LyricView(this); cl = new
		 * CurrentPlaylistView(this,"nowplaying"); ArrayList<StartParameterView>
		 * aClasses = new ArrayList<StartParameterView>(); aClasses.add(new
		 * StartParameterView(cl, R.string.tilte_playinglist)); aClasses.add(new
		 * StartParameterView(mv, R.string.title_mediaview)); aClasses.add(new
		 * StartParameterView(lv, R.string.title_lyricview));
		 */

		ArrayList<StartParameter> aClasses = new ArrayList<StartParameter>();
		aClasses.add(new StartParameter(CurrentPlaylistActivity.class, null,
				R.string.tilte_playinglist));
		aClasses.add(new StartParameter(MediaPlaybackActivity.class, null,
				R.string.title_mediaview));
		aClasses.add(new StartParameter(LyricActivity.class, null,
				R.string.title_lyricview));

		mContext = this;

		setupFlingParm(aClasses, R.layout.mediaplaying_home, R.id.indicator,
				R.id.pager);
		setDisplayScreen(1);
		setIfMusic(true);
		super.onCreate(savedInstanceState);

		playingSeek = (SeekBar) findViewById(R.id.playingseekbar);
		playingSeek.setMax(1000);
		playingSeek.setOnSeekBarChangeListener(mSeekListener);		

		mPrevButton = (ImageView) findViewById(R.id.previous);
		mPauseButton = (ImageView) findViewById(R.id.play);
		mNextButton = (ImageView) findViewById(R.id.next);
		mTotalTime = (TextView) findViewById(R.id.totaltime);
		mCurrentTime = (TextView) findViewById(R.id.currenttime);
		
        mAlbumArtWorker = new Worker("album art worker");
//        mAlbumArtHandler = new AlbumArtHandler(mAlbumArtWorker.getLooper());
		
		mPrevButton.setOnClickListener(mPrevListener);
		mPauseButton.setOnClickListener(mPauseListener);
		mNextButton.setOnClickListener(mNextListener);
		
		cl = (CurrentPlaylistActivity) super.getItemActivity(0);
		mp = (MediaPlaybackActivity) super.getItemActivity(1);
		lrcActivity = (LyricActivity) super.getItemActivity(2);
		mp.setCurListActivity(cl);
		randomAll = getIntent().getBooleanExtra("isRandomAll", false);
		
//		mRainbowLine = (ImageView) mp.findViewById(R.id.rainbowline);
//		startService(new Intent(EqService.NAME));
		
		mMediaPager = (RelativeLayout)findViewById(R.id.meida_pager);
		mMediaPager.post(new Runnable() {
            
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Bitmap back = MusicUtils.getDefaultBg(mContext, 0);
                MusicUtils.setBackground(mMediaPager, back);            }
        });
		
		ExitApplication exit = (ExitApplication) getApplication();  
        exit.addActivity(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		paused = false;
		mToken = MusicUtils.bindToService(this, osc);
		if (mToken == null) {
			// something went wrong
			mHandler.sendEmptyMessage(QUIT);
		}
		
		IntentFilter f = new IntentFilter();
		f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
		f.addAction(MediaPlaybackService.META_CHANGED);
		f.addAction(OnlineLoader.UPDATEBG);
		registerReceiver(mStatusListener, new IntentFilter(f));
		long next = refreshNow();
		queueNextRefresh(next);
	}

    @Override
    protected void onResume() {
        super.onResume();
        int ifBacklight = MusicUtils.getIntPref(this, "backlight", 0);
        if (ifBacklight == 1)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        int ifBacklight = MusicUtils.getIntPref(this, "backlight", 0);
        if (ifBacklight == 1)
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onStop() {
        paused = true;
        mHandler.removeMessages(REFRESH);
        unregisterReceiver(mStatusListener);
        MusicUtils.unbindFromService(mToken);
        mService = null;
        super.onStop();
    }
	
	@Override
	protected void onDestroy() {
		mAlbumArtWorker.quit();
		
//		cl.closeCursor();
		cl.finish();
		cl = null;
		lrcActivity.finish();
		mp.finish();
//		if(labg != null) {
//            labg.restorePreArtistName();
//        }
		super.onDestroy();
	}

	private ServiceConnection osc = new ServiceConnection() {
		public void onServiceConnected(ComponentName classname, IBinder obj) {
			mService = IMediaPlaybackService.Stub.asInterface(obj);
			if(randomAll && !MusicUtils.isMusicLoaded()) {

//				MusicUtils.clearQueue();
//				long list[] = MusicUtils.getAllSongs(mContext);
//				if(list != null) {
//					MusicUtils.addToCurrentPlaylist(mContext,
//							list);
//				}
				try {
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
                    mService.play();
                } catch (RemoteException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
			}
					
			mp.setMediaService(mService);
			mp.updateRepeatButtonImg();  
			lrcActivity.setMediaService(mService);
			if (mService != null)
				cl.initAdatper();
			startPlayback();
			/*
			 * Intent intent = new Intent();
			 * //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * intent.setClass(mContext,CurrentPlaylistActivity.class);
			 * startActivity(intent);
			 */

			try {
				// Assume something is playing when the service says it is,
				// but also if the audio ID is valid but the service is paused.
			    
			    if(MediaPlaybackService.SHUFFLE_NORMAL == mp.getRandomMode()) {
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_AUTO);
                    if(cl != null) {
                        cl.updateNowplayingCursor();
                    }
                } 
				if (mService.getAudioId() >= 0 || mService.isPlaying()
						|| mService.getPath() != null) {
					// something is playing now, we're done.
					/*
					 * mRepeatButton.setVisibility(View.VISIBLE);
					 * mShuffleButton.setVisibility(View.VISIBLE);
					 * mQueueButton.setVisibility(View.VISIBLE);
					 * setRepeatButtonImage(); setShuffleButtonImage();
					 * */
					 setPauseButtonImage();
					 
					return;
				}
			} catch (RemoteException ex) {
			}
			// Service is dead or not playing anything. If we got here as part
			// of a "play this file" Intent, exit. Otherwise go to the Music
			// app start screen.
			// if (getIntent().getData() == null) {

			// }*/
			// finish();
		}

		public void onServiceDisconnected(ComponentName classname) {
			mService = null;
		}
	};
	
	
	private View.OnClickListener mPauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			doPauseResume();
		}
	};

	private View.OnClickListener mPrevListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (mService == null)
				return;
			try {
				//if (mService.position() < 2000) {
					mService.prev();
/*				} else {
					mService.seek(0);
					mService.play();
				}*/
			} catch (RemoteException ex) {
			}
		}
	};

	private View.OnClickListener mNextListener = new View.OnClickListener() {
		public void onClick(View v) {
			if (mService == null)
				return;
			try {
				mService.next();
			} catch (RemoteException ex) {
			}
		}
	};
	
    private void doPauseResume() {
        try {
            if(mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
                refreshNow();
                setPauseButtonImage();
            }
        } catch (RemoteException ex) {
        }
    }

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			mLastSeekEventTime = 0;
			mFromTouch = true;
		}

		public void onProgressChanged(SeekBar bar, int progress,
				boolean fromuser) {
			if (!fromuser || (mService == null))
				return;
			long now = SystemClock.elapsedRealtime();
			if ((now - mLastSeekEventTime) > 250) {
				mLastSeekEventTime = now;
				mPosOverride = mDuration * progress / 1000;
				try {
					mService.seek(mPosOverride);
				} catch (RemoteException ex) {
				}

				// trackball event, allow progress updates
				if (!mFromTouch) {
					refreshNow();
					mPosOverride = -1;
				}
			}
		}

		public void onStopTrackingTouch(SeekBar bar) {
			mPosOverride = -1;
			mFromTouch = false;
		}
	};

	private void startPlayback() {

		if (mService == null)
			return;
		Intent intent = getIntent();
		String filename = "";
		Uri uri = intent.getData();
		if (uri != null && uri.toString().length() > 0) {
			// If this is a file:// URI, just use the path directly instead
			// of going through the open-from-filedescriptor codepath.
			String scheme = uri.getScheme();
			if ("file".equals(scheme)) {
				filename = uri.getPath();
			} else {
				filename = uri.toString();
			}
			try {
				mService.stop();
				mService.openFile(filename);
				mService.play();
				setIntent(new Intent());
			} catch (Exception ex) {
				Log.d("MediaPlaybackActivity", "couldn't start playback: " + ex);
			}
		}

		updateTrackInfo();
		long next = refreshNow();
		queueNextRefresh(next);
	}
	
	private void updateTrackInfo() {
        if (mService == null) {
            return;
        }
        try {
            String path = mService.getPath();
            if (path == null) {
                finish();
                return;
            }
            
            long songid = mService.getAudioId(); 
            
            if(mp != null) {
                if(!mp.getIsShuffle()) {
                    setMediaHomeBackGround(songid);
                } else {
                    mp.setIsShuffle(false);
                }                
            }
/*            if (songid < 0 && path.toLowerCase().startsWith("http://")) {
                // Once we can get album art and meta data from MediaPlayer, we
                // can show that info again when streaming.
                ((View) mArtistName.getParent()).setVisibility(View.INVISIBLE);
                ((View) mAlbumName.getParent()).setVisibility(View.INVISIBLE);
                mAlbum.setVisibility(View.GONE);
                mTrackName.setText(path);
                mAlbumArtHandler.removeMessages(GET_ALBUM_ART);
                mAlbumArtHandler.obtainMessage(GET_ALBUM_ART, new AlbumSongIdWrapper(-1, -1)).sendToTarget();
            } else {*/
                String artistName = mService.getArtistName();
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = getResources().getString(R.string.unknown_artist_name);
                }
                //mArtistName.setText(artistName);
                String albumName = mService.getAlbumName();
                long albumid = mService.getAlbumId();
                if (MediaStore.UNKNOWN_STRING.equals(albumName)) {
                    albumName = getResources().getString(R.string.unknown_album_name);
                    albumid = -1;
                }
   //             String [] next = new String[4];
  //              next = MusicUtils.getTrackNameNext(mContext);
                String mTrackName = mService.getTrackName();
                lrcActivity.setLyricActivity(mTrackName, artistName, songid);
                mp.updateTrackInfo(artistName, mTrackName);
//                mAlbumArtHandler.removeMessages(GET_ALBUM_ART);
//                mAlbumArtHandler.obtainMessage(GET_ALBUM_ART, new AlbumSongIdWrapper(albumid, songid)).sendToTarget();
                

            mDuration = mService.duration();
            mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
        } catch (RemoteException ex) {
            finish();
        }
    }

	private long refreshNow() {
		if (mService == null)
			return 500;
		try {
			long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
			long remaining = 1000 - (pos % 1000);
			if ((pos >= 0) && (mDuration > 0)) {
				mCurrentTime.setText(MusicUtils.makeTimeString(this, pos /
				 1000));

				if (mService.isPlaying()) {
					mCurrentTime.setVisibility(View.VISIBLE);
				} else {

					remaining = 500;
				}

				playingSeek.setProgress((int) (1000 * pos / mDuration));
			} else {
				mCurrentTime.setText("--:--");
				playingSeek.setProgress(1000);
			}
			// return the number of milliseconds until the next full second, so
			// the counter can be updated at just the right time
			return 100;
		} catch (RemoteException ex) {
		}
		return 100;
	}

	private void queueNextRefresh(long delay) {
		if (!paused) {
			Message msg = mHandler.obtainMessage(REFRESH);
			mHandler.removeMessages(REFRESH);
			mHandler.sendMessageDelayed(msg, delay);
		}
	}
	
    private static class AlbumSongIdWrapper {
        public long albumid;
        public long songid;
        AlbumSongIdWrapper(long aid, long sid) {
            albumid = aid;
            songid = sid;
        }
    }
	
/*    public class AlbumArtHandler extends Handler {
        private long mAlbumId = -1;
        
        public AlbumArtHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg)
        {
            long albumid = ((AlbumSongIdWrapper) msg.obj).albumid;
            long songid = ((AlbumSongIdWrapper) msg.obj).songid;
            if (msg.what == GET_ALBUM_ART && (mAlbumId != albumid || albumid < 0)) {
                // while decoding the new image, show the default album art
//                Message numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
//                mHandler.removeMessages(ALBUM_ART_DECODED);
//                mHandler.sendMessageDelayed(numsg, 300);
                Bitmap bm = MusicUtils.getArtwork(mContext, songid, albumid);
                Bitmap topbm = bm;
                if (bm == null) {
                    bm = MusicUtils.getArtwork(mContext, songid, -1);
                    albumid = -1; 
                }
                updateTopAlbumBitmap(topbm);                
//                if (bm != null) {
//                    numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, bm);
//                    mHandler.removeMessages(ALBUM_ART_DECODED);
//                    mHandler.sendMessage(numsg);
//                }
                mAlbumId = albumid;
            }
        }
    }
*/    
    public void setMediaHomeBackGround(long songid) {
    	labg = new LocalAsync(this);
    	labg.setArtistImg(songid);
    }
    
    public void updateTopAlbumBitmap(Bitmap bm) {
    	if(bm == null) {
        	Intent intent = new Intent();
        	intent.setAction(NowPlayingController.UPDATE_NOWPLAYINGALBUM);
        	intent.putExtra("ifbmnull", 1);
        	this.sendBroadcast(intent);
    	} else {
        	Intent intent = new Intent();
        	intent.setAction(NowPlayingController.UPDATE_NOWPLAYINGALBUM);
        	intent.putExtra("albumbitmap", bm);
        	this.sendBroadcast(intent);
    	}
    }
    
    public void updateAllSongsBitmap(Bitmap bm) {
        Intent intent = new Intent();
        intent.setAction(PlaylistBrowserActivity.UPDATE_ALLSONGSIMG);
        intent.putExtra("artistbitmap", bm);
        sendBroadcast(intent);
    }

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ALBUM_ART_DECODED:
				//mAlbum.setImageBitmap((Bitmap)msg.obj);
				// mAlbum.getDrawable().setDither(true);
//				mp.setAlbumImage((Bitmap)msg.obj);
				//if((Bitmap)msg.obj != null)
				//MusicUtils.setBackground(getWindow().getDecorView(), (Bitmap)msg.obj);
				break;

			case REFRESH:
				try {
					if(lrcActivity != null && mService != null) {
						lrcActivity.UpdateDuration(mService.position());
						lrcActivity.postUpdate();
					}

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long next = refreshNow();
				queueNextRefresh(next);
				break;

			case QUIT:
				// This can be moved back to onCreate once the bug that prevents
				// Dialogs from being started from onCreate/onResume is fixed.
				new AlertDialog.Builder(MediaPlaybackHomeActivity.this)
				// .setTitle(R.string.service_start_error_title)
				// .setMessage(R.string.service_start_error_msg)
						.setPositiveButton("error",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										finish();
									}
								}).setCancelable(false).show();
				break;

			default:
				break;
			}
		}
	};

	private void setPauseButtonImage() {
		try {
			if (mService != null && mService.isPlaying()) {
				mPauseButton
						.setBackgroundResource(R.drawable.pause_selector);
			//	mRainbowLine.setImageResource(R.drawable.rainbowline);
			} else {
				mPauseButton.setBackgroundResource(R.drawable.play_selector);
			//	mRainbowLine.setImageResource(R.drawable.blackwhiteline);
			}
		} catch (RemoteException ex) {
		}
	}
	
	protected void updateNowPlayingbar() {
		// TODO Auto-generated method stub
		String mTrackName = null;
		try {
			mTrackName = mService.getTrackName();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Intent bcast = new Intent();
        bcast.setAction(UPDATEPLAING);
        bcast.putExtra("songName", mTrackName);
        mContext.sendBroadcast(bcast);
	}

	private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(MediaPlaybackService.META_CHANGED)) {
				// redraw the artist/title info and
				// set new max for progress bar
				updateTrackInfo();
				//updateNowPlayingbar();
				setPauseButtonImage();
				queueNextRefresh(1);				
				
			} else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
				setPauseButtonImage();
			} else if (action.equals(OnlineLoader.UPDATEBG)) {
				Parcelable p = intent.getParcelableExtra("backg");
				Bitmap b = (Bitmap)p;
				if(b != null) {
					MusicUtils.setBackground(mMediaPager, b);
//					updateAllSongsBitmap(b);
				}
			}
		}
	};
	
    
    private static class Worker implements Runnable {
        private final Object mLock = new Object();
        private Looper mLooper;
        
        /**
         * Creates a worker thread with the given name. The thread
         * then runs a {@link android.os.Looper}.
         * @param name A name for the new thread
         */
        Worker(String name) {
            Thread t = new Thread(null, this, name);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            synchronized (mLock) {
                while (mLooper == null) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        
        public Looper getLooper() {
            return mLooper;
        }
        
        public void run() {
            synchronized (mLock) {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mLock.notifyAll();
            }
            Looper.loop();
        }
        
        public void quit() {
            mLooper.quit();
        }
    } 
    
    @Override
    public void onUserInteraction() {
        // TODO Auto-generated method stub
        super.onUserInteraction();
        if(cl.getIsOrderChanged()) {
//            mp.updateTrackNext();
            cl.setIsOrderChanged(false);
        }
    }
}
