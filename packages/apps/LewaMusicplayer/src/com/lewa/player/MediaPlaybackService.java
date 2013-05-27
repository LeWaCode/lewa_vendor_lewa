package com.lewa.player;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import com.lewa.player.helper.AsyncMusicPlayer;
import com.lewa.player.online.LocalAsync;
import com.lewa.player.ui.MediaAppWidgetProvider;
import com.lewa.player.ui.MusicEQActivity;
import com.lewa.player.ui.NowPlayingController;

import android.annotation.Widget;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.Equalizer;
import android.net.Uri;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

/**
 * Provides "background" audio playback capabilities, allowing the
 * user to switch between activities without stopping playback.
 */
public class MediaPlaybackService extends Service {
    /** used to specify whether enqueue() should start playing
     * the new list of files right away, next or once all the currently
     * queued files have been played
     */
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int PLAYBACKSERVICE_STATUS = 1;
    
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;

    public static final String PLAYSTATE_CHANGED = "com.lewa.player.playstatechanged";
    public static final String META_CHANGED = "com.lewa.player.metachanged";
    public static final String QUEUE_CHANGED = "com.lewa.player.queuechanged";

    public static final String SERVICECMD = "com.lewa.player.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";    

    public static final String TOGGLEPAUSE_ACTION = "com.lewa.player.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.lewa.player.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.lewa.player.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.lewa.player.musicservicecommand.next";
    public static final String HEADPLUG = "android.intent.action.HEADSET_PLUG";
    public static final String SHAKE = "com.lewa.player.musicservicecommand.shake";
    public static final String SLEEP = "com.lewa.player.musicservicecommand.sleep";
    public static final String UPDATEID3INFO = "com.lewa.player.updateid3info";

    public static final int TRACK_ENDED = 1;
    public static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FADEIN = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEOUTOPEN = 6;
    private static final int FADEOUTPAUSE = 7;
    private static final int FADEOUTCLOSE = 8;
    
    private static final int VOLUME_CLEARED_FADEIN = 0;
    private static final int MAX_HISTORY_SIZE = 100;
    
	public static AsyncMusicPlayer mPlayer; 
    private String mFileToPlay;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mMediaMountedCount = 0;
    private long [] mAutoShuffleList = null;
    private long [] mPlayList = null;
    private int mPlayListLen = 0;
    private Vector<Integer> mHistory = new Vector<Integer>(MAX_HISTORY_SIZE);
    private Cursor mCursor;
    private int mPlayPos = -1;
    private int mPrePlayPos = -1;
    private static final String LOGTAG = "MediaPlaybackService";
    private final Shuffler mRand = new Shuffler();
    private int mOpenFailedCounter = 0;
    String[] mCursorCols = new String[] {
            "audio._id AS _id",             // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX below
            MediaStore.Audio.Media.BOOKMARK    // index must match BOOKMARKCOLIDX below
    };
    private final static int IDCOLIDX = 0;
    private final static int PODCASTCOLIDX = 8;
    private final static int BOOKMARKCOLIDX = 9;
    private BroadcastReceiver mUnmountReceiver = null;
    private WakeLock mWakeLock;
    private int mServiceStartId = -1;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mQuietMode = false;
    private AudioManager mAudioManager;
    private boolean mQueueIsSaveable = true;
    // used to track what type of audio focus loss caused the playback to pause
    private boolean mPausedByTransientLossOfFocus = false;
    private int fadeouttype;

    private SharedPreferences mPreferences;
    // We use this to distinguish between different cards when saving/restoring playlists.
    // This will have to change if we want to support multiple simultaneous cards.
    private int mCardId;
    
    private MediaAppWidgetProvider mAppWidgetProvider = MediaAppWidgetProvider.getInstance();
    
    // interval after which we stop the service when idle
    private static final int IDLE_DELAY = 60000;
    
    ShakeListener mShaker;
    private static final int REFRESH = 1;
    
    private Equalizer mEqualizer;
    
    short[] levels = new short[5];
  
    //jczou #8167
    private boolean isModifiedINFO = false;
    private static boolean isSearchArt = true;
    
    private Handler mMediaplayerHandler = new Handler() {
        float mCurrentVolume = 1.0f;
        
        @Override
        public void handleMessage(Message msg) {
            //MusicUtils.debugLog("mMediaplayerHandler.handleMessage " + msg.what);
            switch (msg.what) {
            	case VOLUME_CLEARED_FADEIN:
            		mCurrentVolume = 0f;
            		if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
            		break;
                case FADEIN:
                    mCurrentVolume += 0.01f;
                    if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEIN, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    if (mIsSupposedToBePlaying) {
                        next(true);
                    } else {
                        // the server died when we were idle, so just
                        // reopen the same song (it will start again
                        // from the beginning though when the user
                        // restarts)
                        openCurrent();
                    }
                    break;
                case TRACK_ENDED:
                    if (mRepeatMode == REPEAT_CURRENT) {
                        seek(0);
                        play();
                    } else {
                        next(false);
                    }
                    PlayTimesAdd();
                    break;
                case RELEASE_WAKELOCK:
                    mWakeLock.release();
                    break;

                case FOCUSCHANGE:
                    // This code is here so we can better synchronize it with the code that
                    // handles fade-in
                    // AudioFocus is a new feature: focus updates are made verbose on purpose
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_LOSS");
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_GAIN");
                            if(!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mPlayer.setVolume(mCurrentVolume);
                                play(); // also queues a fade-in
                            }
                            break;
                        default:
                            Log.e(LOGTAG, "Unknown audio focus change code");
                    }
                    break;
                    
                case FADEOUTOPEN:
                    mCurrentVolume -= 0.01f;
                    if (mCurrentVolume > 0.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEOUTOPEN, 10);
                    } else {                        
                        mCurrentVolume = 0.0f;
                        nextAfterFadeOut();
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                    
                case FADEOUTPAUSE:

                    mCurrentVolume -= 0.01f;
                    if (mCurrentVolume > 0.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEOUTPAUSE, 10);
                    } else {                        
                        mCurrentVolume = 0.0f;
                        pauseAfterFadeOut();
                        
                    }
                    mPlayer.setVolume(mCurrentVolume);
                	break;
                	
                case FADEOUTCLOSE:

                    mCurrentVolume -= 0.01f;
                    if (mCurrentVolume > 0.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEOUTCLOSE, 10);
						mPlayer.setVolume(mCurrentVolume);
                    } else {                        
                        mCurrentVolume = 0.0f;
						mPlayer.setVolume(mCurrentVolume);
                        ExitApplication exit = (ExitApplication)getApplicationContext();    
                        exit.finishAll(); 
						stopSelf(mServiceStartId);
                        android.os.Process.killProcess(android.os.Process.myPid());                         
                    }
                    
                	break;

                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            //MusicUtils.debugLog("mIntentReceiver.onReceive " + action + " / " + cmd);
            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                next(true);
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
                prev();
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDSTOP.equals(cmd)) {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            } else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
                // Someone asked us to refresh a set of specific widgets, probably
                // because they were just added.
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetProvider.performUpdate(MediaPlaybackService.this, appWidgetIds);
            } else if (SHAKE.equals(action)) {
                doShake();
            } else if(SLEEP.equals(action)) {
               SleepModeManager.setSleepTime(context, 0);
               mMediaplayerHandler.sendEmptyMessage(FADEOUTCLOSE);                 
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                if(mShaker != null) {
                    mShaker.pause();
                    mShaker = null;
                }
                doShake();
            } else if(HEADPLUG.equals(action)) {
            	if(intent.getIntExtra("state", 1) == 0){
            		pause();
            	}
            }else if(MediaPlaybackService.UPDATEID3INFO.equals(action)) {
                isModifiedINFO = true;
                mCursor = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursorCols, "_id=" + String.valueOf(mPlayList[mPlayPos]), null, null);
                mCursor.moveToFirst();
                if(isPlaying()) {
                    updateNotification();
                }
                notifyChange(META_CHANGED);
            	
            }
        }
    };
    
    //jczou#6815
    
    //jczou
    BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        
        private boolean isAlarmPause = false;
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            System.out.println(action);
            if(isPlaying() && action.equals("com.android.deskclock.START_ALARM")){
                //TODO
                pause();
                isAlarmPause = true;
            }
            else if(isAlarmPause && (action.equals("com.android.deskclock.ALARM_DONE") || action.equals("alarm_killed"))){
                //TODO
                play();
                isAlarmPause = false;
            }
        }
    };
    
    private void doShake() {
        Context context = getApplicationContext();
        int isShake = MusicUtils.getIntPref(context, "shake", 0);
        if(isShake == 1) {
            if(mShaker == null) {
                mShaker = new ShakeListener(this);
            }
        } else {
            if(mShaker != null) {
                mShaker.pause();
                mShaker = null;
            }
        }
    }

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    public MediaPlaybackService() {
    }

    protected void PlayTimesAdd() {
		// TODO Auto-generated method stub
        MusicUtils.songPlayTimesPlus(this);
		
	}

	@Override
    public void onCreate() {
        super.onCreate();

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName()));
        
        mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
       // mCardId = MusicUtils.getCardId(this); //from musicUtils
        mCardId = FileUtils.getFatVolumeId(Environment.getExternalStorageDirectory().getPath());
        
        registerExternalStorageListener();

        // Needs to be done in this thread, since otherwise ApplicationContext.getPowerManager() crashes.
		//AsyncMusicPlayer localmediaplayer = new AsyncMusicPlayer(this);
		mPlayer = new AsyncMusicPlayer(this);
        mPlayer.setHandler(mMediaplayerHandler);

        reloadQueue();
        
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SERVICECMD);
        commandFilter.addAction(TOGGLEPAUSE_ACTION);
        commandFilter.addAction(PAUSE_ACTION);
        commandFilter.addAction(NEXT_ACTION);
        commandFilter.addAction(PREVIOUS_ACTION);
        commandFilter.addAction(SHAKE);
        commandFilter.addAction(SLEEP);
		commandFilter.addAction(HEADPLUG);
        commandFilter.addAction(Intent.ACTION_SCREEN_OFF);
        commandFilter.addAction(UPDATEID3INFO);
        commandFilter.addAction("downartist");
        commandFilter.addAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        registerReceiver(mIntentReceiver, commandFilter);
        
        IntentFilter alarmFilter = new IntentFilter();
        alarmFilter.addAction("alarm_killed");
        alarmFilter.addAction("com.android.deskclock.ALARM_DONE");
        alarmFilter.addAction("com.android.deskclock.START_ALARM");
        registerReceiver(mAlarmReceiver, alarmFilter);
        
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // If the service was idle, but got killed before it stopped itself, the
        // system will relaunch it. Make sure it gets stopped again in that case.
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);        

        setRepeatMode(MediaPlaybackService.REPEAT_ALL);
//       setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
        
        doShake();
        
        try{
            mEqualizer = new Equalizer(0, getAudioSessionId());
            mEqualizer.setEnabled(true); 
        }catch(Exception ex) {
            Log.e(LOGTAG, " ex = " + ex);
        }
        registerReceiver(preferenceUpdateReceiver, new IntentFilter(MusicEQActivity.ACTION_UPDATE_EQ));
        
        SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
        short l1 = (short) music_settings.getInt("LowerEQ", 0);
        short l2 = (short) music_settings.getInt("LowEQ", 0);
        short l3 = (short) music_settings.getInt("MiddleEQ", 0);
        short l4 = (short) music_settings.getInt("HighEQ", 0);
        short l5 = (short) music_settings.getInt("HigherEQ", 0);
        
        levels[0] = l1;
        levels[1] = l2;
        levels[2] = l3;
        levels[3] = l4;
        levels[4] = l5;
        
        updateDsp();
        notifyChange(META_CHANGED);
    }

    @Override
    public void onDestroy() {
        // Check that we're not being destroyed while something is still playing.
        if (isPlaying()) {
            Log.e(LOGTAG, "Service being destroyed while still playing.");
        }
        // release all MediaPlayer resources, including the native player and wakelocks
        Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(i);
        mPlayer.release();
        mPlayer = null;

        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        
        // make sure there aren't any other messages coming
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mMediaplayerHandler.removeCallbacksAndMessages(null);

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        unregisterReceiver(mAlarmReceiver);
        unregisterReceiver(mIntentReceiver);
        unregisterReceiver(preferenceUpdateReceiver);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        mWakeLock.release();
        if(mShaker != null) {
            mShaker.pause();
            mShaker = null;
        }
        super.onDestroy();
    }
    
    private final char hexdigits [] = new char [] {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    private void saveQueue(boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }

        Editor ed = mPreferences.edit();
        //long start = System.currentTimeMillis();
        if (full) {
            StringBuilder q = new StringBuilder();
            
            // The current playlist is saved as a list of "reverse hexadecimal"
            // numbers, which we can generate faster than normal decimal or
            // hexadecimal numbers, which in turn allows us to save the playlist
            // more often without worrying too much about performance.
            // (saving the full state takes about 40 ms under no-load conditions
            // on the phone)
            int len = mPlayListLen;
            for (int i = 0; i < len; i++) {
                long n = mPlayList[i];
                if (n < 0) {
                    continue;
                } else if (n == 0) {
                    q.append("0;");
                } else {
                    while (n != 0) {
                        int digit = (int)(n & 0xf);
                        n >>>= 4;
                        q.append(hexdigits[digit]);
                    }
                    q.append(";");
                }
            }
            //Log.i("@@@@ service", "created queue string in " + (System.currentTimeMillis() - start) + " ms");
            ed.putString("queue", q.toString());
            ed.putInt("cardid", mCardId);
            if (mShuffleMode != SHUFFLE_NONE) {
                // In shuffle mode we need to save the history too
                len = mHistory.size();
                q.setLength(0);
                for (int i = 0; i < len; i++) {
                    int n = mHistory.get(i);
                    if (n == 0) {
                        q.append("0;");
                    } else {
                        while (n != 0) {
                            int digit = (n & 0xf);
                            n >>>= 4;
                            q.append(hexdigits[digit]);
                        }
                        q.append(";");
                    }
                }
                ed.putString("history", q.toString());
            }
        }
        ed.putInt("curpos", mPlayPos);
        if (mPlayer.isInitialized()) {
            ed.putLong("seekpos", mPlayer.position());
        }
        ed.putInt("repeatmode", mRepeatMode);
        ed.putInt("shufflemode", mShuffleMode);
        //SharedPreferencesCompat.apply(ed);
        ed.commit();

        //Log.i("@@@@ service", "saved state in " + (System.currentTimeMillis() - start) + " ms");
    }

    private void reloadQueue() {
        String q = null;
        
        boolean newstyle = false;
        int id = mCardId;
        if (mPreferences.contains("cardid")) {
            newstyle = true;
            id = mPreferences.getInt("cardid", ~mCardId);
        }
        if (id == mCardId) {
            // Only restore the saved playlist if the card is still
            // the same one as when the playlist was saved
            q = mPreferences.getString("queue", "");
        }
        int qlen = q != null ? q.length() : 0;
        if (qlen > 1) {
            //Log.i("@@@@ service", "loaded queue: " + q);
            int plen = 0;
            int n = 0;
            int shift = 0;
            for (int i = 0; i < qlen; i++) {
                char c = q.charAt(i);
                if (c == ';') {
                    ensurePlayListCapacity(plen + 1);
                    mPlayList[plen] = n;
                    plen++;
                    n = 0;
                    shift = 0;
                } else {
                    if (c >= '0' && c <= '9') {
                        n += ((c - '0') << shift);
                    } else if (c >= 'a' && c <= 'f') {
                        n += ((10 + c - 'a') << shift);
                    } else {
                        // bogus playlist data
                        plen = 0;
                        break;
                    }
                    shift += 4;
                }
            }
            mPlayListLen = plen;

            int pos = mPreferences.getInt("curpos", 0);
            if (pos < 0 || pos >= mPlayListLen) {
                // The saved playlist is bogus, discard it
                mPlayListLen = 0;
                return;
            }
            mPlayPos = pos;
            
            // When reloadQueue is called in response to a card-insertion,
            // we might not be able to query the media provider right away.
            // To deal with this, try querying for the current file, and if
            // that fails, wait a while and try again. If that too fails,
            // assume there is a problem and don't restore the state.
            Cursor crsr = MusicUtils.query(this,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String [] {"_id"}, "_id=" + mPlayList[mPlayPos] , null, null);
            if (crsr == null || crsr.getCount() == 0) {
                // wait a bit and try again
                SystemClock.sleep(3000);
                crsr = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursorCols, "_id=" + mPlayList[mPlayPos] , null, null);
            }
            if (crsr != null) {
                crsr.close();
            }

            // Make sure we don't auto-skip to the next song, since that
            // also starts playback. What could happen in that case is:
            // - music is paused
            // - go to UMS and delete some files, including the currently playing one
            // - come back from UMS
            // (time passes)
            // - music app is killed for some reason (out of memory)
            // - music service is restarted, service restores state, doesn't find
            //   the "current" file, goes to the next and: playback starts on its
            //   own, potentially at some random inconvenient time.
            mOpenFailedCounter = 20;
            mQuietMode = true;
            openCurrent();
            mQuietMode = false;
            if (!mPlayer.isInitialized()) {
                // couldn't restore the saved state
                mPlayListLen = 0;
                return;
            }
            
            long seekpos = mPreferences.getLong("seekpos", 0);
            seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);
            Log.d(LOGTAG, "restored queue, currently at position "
                    + position() + "/" + duration()
                    + " (requested " + seekpos + ")");
            
            int repmode = mPreferences.getInt("repeatmode", REPEAT_ALL);
            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }
            mRepeatMode = repmode;

            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }
            if (shufmode != SHUFFLE_NONE) {
                // in shuffle mode we need to restore the history too
                q = mPreferences.getString("history", "");
                qlen = q != null ? q.length() : 0;
                if (qlen > 1) {
                    plen = 0;
                    n = 0;
                    shift = 0;
                    mHistory.clear();
                    for (int i = 0; i < qlen; i++) {
                        char c = q.charAt(i);
                        if (c == ';') {
                            if (n >= mPlayListLen) {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            mHistory.add(n);
                            n = 0;
                            shift = 0;
                        } else {
                            if (c >= '0' && c <= '9') {
                                n += ((c - '0') << shift);
                            } else if (c >= 'a' && c <= 'f') {
                                n += ((10 + c - 'a') << shift);
                            } else {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            shift += 4;
                        }
                    }
                }
            }
            if (shufmode == SHUFFLE_AUTO) {
                if (! makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shufmode;
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        if (intent != null) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            //MusicUtils.debugLog("onStartCommand " + action + " / " + cmd);

            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                next(true);
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
//                if (position() < 2000) {
//                    prev();
//                } else {
//                    seek(0);
//                    play();
//                }
                prev();
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDSTOP.equals(cmd)) {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            }
        }
        
        // make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);        
        
        return  START_NOT_STICKY; //START_STICKY; 
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;

        // Take a snapshot of the current playlist
        saveQueue(true);

        if (isPlaying() || mPausedByTransientLossOfFocus) {
            // something is currently playing, or will be playing once 
            // an in-progress action requesting audio focus ends, so don't stop the service now.
            return true;
        }
        
        // If there is a playlist but playback is paused, then wait a while
        // before stopping the service, so that pause/resume isn't slow.
        // Also delay stopping the service if we're transitioning between tracks.
        if (mPlayListLen > 0  || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }
        
        // No active playlist, OK to stop the service right now
        stopSelf(mServiceStartId);
        return true;
    }
    
    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
            if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
                    || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }
            // save the queue again, because it might have changed
            // since the user exited the music app (because of
            // party-shuffle or because the play-position changed)
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    };

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     *
     * @param storagePath path to mount point for the removed media
     */
    public void closeExternalStorageFiles(String storagePath) {
        // stop playback and clean up if the SD card is going to be unmounted.
        stop(true);
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications.
     * The intent will call closeExternalStorageFiles() if the external media
     * is going to be ejected, so applications can clean up any files they have open.
     */
    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action.equals(Intent.ACTION_MEDIA_EJECT) ||
                            action.equals(Intent.ACTION_MEDIA_SHARED)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        //mCardId = MusicUtils.getCardId(MediaPlaybackService.this);
                        mCardId = FileUtils.getFatVolumeId(Environment.getExternalStorageDirectory().getPath());
                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(QUEUE_CHANGED);
                        notifyChange(META_CHANGED);
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addAction(Intent.ACTION_MEDIA_SHARED);
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    /**
     * Notify the change-receivers that something has changed.
     * The intent that is sent contains the following data
     * for the currently playing track:
     * "id" - Integer: the database row ID
     * "artist" - String: the name of the artist
     * "album" - String: the name of the album
     * "track" - String: the name of the track
     * The intent has an action that is one of
     * "com.android.music.metachanged"
     * "com.android.music.queuechanged",
     * "com.android.music.playbackcomplete"
     * "com.android.music.playstatechanged"
     * respectively indicating that a new track has
     * started playing, that the playback queue has
     * changed, that playback has stopped because
     * the last file in the list has been played,
     * or that the play-state changed (paused/resumed).
     */
    private void notifyChange(String what) {
        
        Intent i = new Intent(what);
        i.putExtra("id", Long.valueOf(getAudioId()));
        i.putExtra("artist", getArtistName());
        i.putExtra("album",getAlbumName());
        i.putExtra("track", getTrackName());
        i.putExtra("playing", isPlaying());
        sendBroadcast(i);
        
        if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
        } else {
            saveQueue(false);
        }
        
        // Share this notification directly with our widgets
        mAppWidgetProvider.notifyChange(this, what);
    }

    private void ensurePlayListCapacity(int size) {
        if (mPlayList == null || size > mPlayList.length) {
            // reallocate at 2x requested size so we don't
            // need to grow and copy the array for every
            // insert
            long [] newlist = new long[size * 2];
            int len = mPlayList != null ? mPlayList.length : mPlayListLen;
            for (int i = 0; i < len; i++) {
                newlist[i] = mPlayList[i];
            }
            mPlayList = newlist;
        }
        // FIXME: shrink the array when the needed size is much smaller
        // than the allocated size
    }
    
    // insert the list of songs at the specified position in the playlist
    private void addToPlayList(long [] list, int position) {
        int addlen = list.length;
        if (position < 0) { // overwrite
            mPlayListLen = 0;
            position = 0;
        }
        ensurePlayListCapacity(mPlayListLen + addlen);
        if (position > mPlayListLen) {
            position = mPlayListLen;
        }
        
        // move part of list after insertion point
        int tailsize = mPlayListLen - position;
        for (int i = tailsize ; i > 0 ; i--) {
            mPlayList[position + i] = mPlayList[position + i - addlen]; 
        }
        
        // copy list into playlist
        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }
        mPlayListLen += addlen;
        if (mPlayListLen == 0) {
            mCursor.close();
            mCursor = null;
            notifyChange(META_CHANGED);
        }
    }
    
    /**
     * Appends a list of tracks to the current playlist.
     * If nothing is playing currently, playback will be started at
     * the first track.
     * If the action is NOW, playback will switch to the first of
     * the new tracks immediately.
     * @param list The list of tracks to append.
     * @param action NOW, NEXT or LAST
     */
    public void enqueue(long [] list, int action) {
        synchronized(this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                // action == LAST || action == NOW || mPlayPos + 1 == mPlayListLen
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.length;
                    openCurrent();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }
            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrent();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Replaces the current playlist with a new list,
     * and prepares for starting playback at the specified
     * position in the list, or a random position if the
     * specified position is 0.
     * @param list The new list of tracks.
     */
    public void open(long [] list, int position) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            long oldId = getAudioId();
            int listlength = list.length;
            boolean newlist = true;
            if (mPlayListLen == listlength) {
                // possible fast path: list might be the same
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlayList[i]) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
            	//this.removeTracks(0, Integer.MAX_VALUE);
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            int oldpos = mPlayPos;
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mRand.nextInt(mPlayListLen);
            }
            mHistory.clear();

            
            
            
            
            
            
            
            
            saveBookmarkIfNeeded();
            
            //openCurrent();
            //if (oldId != getAudioId()) {
            //    notifyChange(META_CHANGED);
            //}
            SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            if(a == 1){
            	mMediaplayerHandler.sendEmptyMessage(FADEOUTOPEN);
            }else{
            	nextAfterFadeOut();
            }
            
        }
    }
    
    /**
     * Moves the item at index1 to index2.
     * @param index1
     * @param index2
     */
    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
                long tmp = mPlayList[index1];
                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i+1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                        mPlayPos--;
                }
            } else if (index2 < index1) {
                long tmp = mPlayList[index1];
                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i-1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                        mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    /**
     * Returns the current play list
     * @return An array of integers containing the IDs of the tracks in the play list
     */
    public long [] getQueue() {
        synchronized (this) {
            int len = mPlayListLen;
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlayList[i];
            }
            return list;
        }
    }

    private void openCurrent() {
        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

            if (mPlayListLen == 0) {
                return;
            }
            openCurrent1();
        }
    }
    
    private void openCurrent1(){
    	stop(false);

        String id = String.valueOf(mPlayList[mPlayPos]);
        
        mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mCursorCols, "_id=" + id , null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
            open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
            // go to bookmark if needed
/*               if (isPodcast()) {
                long bookmark = getBookmark();
                // Start playing a little bit before the bookmark,
                // so it's easier to get back in to the narrative.
                seek(bookmark - 5000);
            }*/
        }
    }

    /**
     * Opens the specified file and readies it for playback.
     *
     * @param path The full path of the file to be opened.
     */
    public void open(String path) {
        if(!MusicUtils.mHasSongs) {
            return;
        }
        synchronized (this) {
            if (path == null) {
                return;
            }
            
            // if mCursor is null, try to associate path with a database cursor
            if (mCursor == null) {

                ContentResolver resolver = getContentResolver();
                Uri uri;
                String where;
                String selectionArgs[];
                if (path.startsWith("content://media/")) {
                    uri = Uri.parse(path);
                    where = null;
                    selectionArgs = null;
                } else {
                   uri = MediaStore.Audio.Media.getContentUriForPath(path);
                   where = MediaStore.Audio.Media.DATA + "=?";
                   selectionArgs = new String[] { path };
                }
                
                try {
                    mCursor = resolver.query(uri, mCursorCols, where, selectionArgs, null);
                    if  (mCursor != null) {
                        if (mCursor.getCount() == 0) {
                            mCursor.close();
                            mCursor = null;
                        } else {
                            mCursor.moveToNext();
                            ensurePlayListCapacity(1);
                            mPlayListLen = 1;
                            mPlayList[0] = mCursor.getLong(IDCOLIDX);
                            mPlayPos = 0;
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (!mPlayer.isInitialized()) {
                stop(true);
                if (mOpenFailedCounter++ < 10 &&  mPlayListLen > 1) {
                    // beware: this ends up being recursive because next() calls open() again.
                    if (mOpenFailedCounter != 0) {
                        // need to make sure we only shows this once
                        mOpenFailedCounter = 0;
                        if (!mQuietMode) {
                            long id = mPlayList[mPlayPos];
                            String name = String.valueOf(MusicUtils.getSongName(getApplicationContext(), id)[0]);
                            String message = getString(R.string.playcurrent_failed, name);
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                        Log.d(LOGTAG, "Failed to open file for playback");
                    }
                    next(false);
                }
                
            } else {
                mOpenFailedCounter = 0;
            }
        }
    }

    //jczou #8167
    public static boolean isSearchArt(){
        return isSearchArt;
    }
    /**
     * Starts playback of a previously opened file.
     */
    public void play() {
        //jczou #8167
        isSearchArt = !(!isModifiedINFO && mPlayPos == mPrePlayPos);
        isModifiedINFO = false;
        
        mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this.getPackageName(),
                MediaButtonIntentReceiver.class.getName()));

        if (mPlayer.isInitialized()) {
            // if we are at the end of the song, go to the next song first
            long duration = mPlayer.duration();
            if (mRepeatMode != REPEAT_CURRENT && duration > 2000 &&
                mPlayer.position() >= duration - 2000) {
                next(true);
            }
            
            //mMediaplayerHandler.sendEmptyMessage(FADEIN);
            SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            if(a == 1){
            	mPlayer.setVolume(0);
                mMediaplayerHandler.sendEmptyMessage(VOLUME_CLEARED_FADEIN);
            }else{
            	mPlayer.setVolume(1.0f);
            }
            
            mPlayer.start();
            
            // make sure we fade in, in case a previous fadein was stopped because
            // of another focus loss
            //mMediaplayerHandler.
            
            
          
            updateNotification();          
            
            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }

        } else if (mPlayListLen <= 0) {
            // This is mostly so that if you press 'play' on a bluetooth headset
            // without every having played anything before, it will still play
            // something.
            if(!MusicUtils.mHasSongs 
                    || !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                notifyChange(META_CHANGED);
                return;
            }
            setShuffleMode(SHUFFLE_AUTO);
//            openCurrent();
            play();
        }
       queueNextRefresh(1000);
    }
    
    private void updateNotification() {
            Notification status = new Notification(R.drawable.ic_music_notification, "", 
                    System.currentTimeMillis());
            String contentTitle = getTrackName();
            String contentText = getArtistName();
            if (contentText == null
                    || contentText.equals(MediaStore.UNKNOWN_STRING)) {
                contentText = getString(R.string.unknown_artist_name);
            }
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent("com.lewa.player.PLAYBACK_VIEWER")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
            status.setLatestEventInfo(getApplicationContext(), contentTitle,
                    contentText, contentIntent);
            status.flags |= Notification.FLAG_ONGOING_EVENT;
            status.icon = R.drawable.play_status;
            status.tickerText = contentTitle;
            startForeground(PLAYBACKSERVICE_STATUS, status);
    }
    
    private void queueNextRefresh(long delay) {
        Message msg = mHandler.obtainMessage(REFRESH);
        mHandler.removeMessages(REFRESH);
        mHandler.sendMessageDelayed(msg, delay);
    }
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if(msg.what == REFRESH) {
                if(mAppWidgetProvider != null) {
                    mAppWidgetProvider.performUpdate(MediaPlaybackService.this, null);
                }
                if(isPlaying()) {
                    queueNextRefresh(1000);
                }
            }
        }        
    };
    
    private void stop(boolean remove_status_icon) {
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }
        mFileToPlay = null;
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        if (remove_status_icon) {
            gotoIdleState();
        } else {
            stopForeground(false);
        }
        if (remove_status_icon) {
            mIsSupposedToBePlaying = false;
        }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        stop(true);
    }

    /**
     * Pauses playback (call play() to resume)
     */
    public void pause() {
        synchronized(this) {
            mMediaplayerHandler.removeMessages(FADEIN);
//            mMediaplayerHandler.removeMessages(FADEOUTSTOP);
            if (isPlaying()) {
                
                saveBookmarkIfNeeded();
                SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
                int a = music_settings.getInt("isFade", 0);
                if(a == 1){
                	mMediaplayerHandler.sendEmptyMessage(FADEOUTPAUSE);
                }else{
                	pauseAfterFadeOut();
                }
                //pauseAfterFadeOut();
            }
        }
    }
    
    private void pauseAfterFadeOut(){

        mPlayer.pause();
        gotoIdleState();
        mIsSupposedToBePlaying = false;
        notifyChange(PLAYSTATE_CHANGED);
    }

    /** Returns whether something is currently playing
     *
     * @return true if something is playing (or will be playing shortly, in case
     * we're currently transitioning between tracks), false if not.
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /*
      Desired behavior for prev/next/shuffle:

      - NEXT will move to the next track in the list when not shuffling, and to
        a track randomly picked from the not-yet-played tracks when shuffling.
        If all tracks have already been played, pick from the full set, but
        avoid picking the previously played track if possible.
      - when shuffling, PREV will go to the previously played track. Hitting PREV
        again will go to the track played before that, etc. When the start of the
        history has been reached, PREV is a no-op.
        When not shuffling, PREV will go to the sequentially previous track (the
        difference with the shuffle-case is mainly that when not shuffling, the
        user can back up to tracks that are not in the history).

        Example:
        When playing an album with 10 tracks from the start, and enabling shuffle
        while playing track 5, the remaining tracks (6-10) will be shuffled, e.g.
        the final play order might be 1-2-3-4-5-8-10-6-9-7.
        When hitting 'prev' 8 times while playing track 7 in this example, the
        user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next',
        a random track will be picked again. If at any time user disables shuffling
        the next/previous track will be picked in sequential order again.
     */

    public void prev() {
        synchronized (this) {
        	if(mShuffleMode != SHUFFLE_NONE){
        		int hsize = mHistory.size();
                if (hsize == 0) {
                    // prev is a no-op
                    //return;
                	mPlayPos = mRand.nextInt(mPlayListLen);
                }else{
                	Integer pos = mHistory.remove(hsize - 1);
                	mPlayPos = pos.intValue();
                }
        	}
        	/*else if (mShuffleMode == SHUFFLE_NORMAL) {
                // go to previously-played track and remove it from the history
                int histsize = mHistory.size();
                if (histsize == 0) {
                    // prev is a no-op
                    return;
                }
                Integer pos = mHistory.remove(histsize - 1);
                mPlayPos = pos.intValue();
            } */else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = mPlayListLen - 1;
                }
            }
            saveBookmarkIfNeeded();
            //stop(false);
            //openCurrent();
            //play();
            //notifyChange(META_CHANGED);

            SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            //Log.i("isFade",a+"");
            //this.fadeouttype = FADEOUTNEXT;
            if(a == 1){
            	mMediaplayerHandler.sendEmptyMessage(FADEOUTOPEN);
            }else{
            	nextAfterFadeOut();
            }
        }
    }

    public void next(boolean force) {
        synchronized (this) {
            if (mPlayListLen <= 0) {
                Log.d(LOGTAG, "No play queue");
                return;
            }

/*            if (mShuffleMode == SHUFFLE_NORMAL) {
                // Pick random next track from the not-yet-played ones
                // TODO: make it work right after adding/removing items in the queue.

                // Store the current file in the history, but keep the history at a
                // reasonable size
                if (mPlayPos >= 0) {
                    mHistory.add(mPlayPos);
                }
                if (mHistory.size() > MAX_HISTORY_SIZE) {
                    mHistory.removeElementAt(0);
                }

                int numTracks = mPlayListLen;
                int[] tracks = new int[numTracks];
                for (int i=0;i < numTracks; i++) {
                    tracks[i] = i;
                }

                int numHistory = mHistory.size();
                int numUnplayed = numTracks;
                for (int i=0;i < numHistory; i++) {
                    int idx = mHistory.get(i).intValue();
                    if (idx < numTracks && tracks[idx] >= 0) {
                        numUnplayed--;
                        tracks[idx] = -1;
                    }
                }

                // 'numUnplayed' now indicates how many tracks have not yet
                // been played, and 'tracks' contains the indices of those
                // tracks.
                if (numUnplayed <=0) {
                    // everything's already been played
                    if (mRepeatMode == REPEAT_ALL || force) {
                        //pick from full set
                        numUnplayed = numTracks;
                        for (int i=0;i < numTracks; i++) {
                            tracks[i] = i;
                        }
                    } else {
                        // all done
                        gotoIdleState();
                        if (mIsSupposedToBePlaying) {
                            mIsSupposedToBePlaying = false;
                            notifyChange(PLAYSTATE_CHANGED);
                        }
                        return;
                    }
                }
                int skip = mRand.nextInt(numUnplayed);
                int cnt = -1;
                while (true) {
                    while (tracks[++cnt] < 0)
                        ;
                    skip--;
                    if (skip < 0) {
                        break;
                    }
                }
                mPlayPos = cnt;
            } else 
                if (mShuffleMode == SHUFFLE_AUTO) {
                //doAutoShuffleUpdate();
                mPlayPos++;
            } else { */
            if(mShuffleMode != SHUFFLE_NONE){
            	//int a = SHUFFLE_NONE;
            	//int b = SHUFFLE_NORMAL;
            	mHistory.add(mPlayPos);
            	mPlayPos = mRand.nextInt(mPlayListLen);
            }else{
                if (mPlayPos >= mPlayListLen - 1) {
                    // we're at the end of the list
                    if (mRepeatMode == REPEAT_NONE && !force) {
                        // all done
                        gotoIdleState();
                        mIsSupposedToBePlaying = false;
                        notifyChange(PLAYSTATE_CHANGED);
                        return;
                    } else if (mRepeatMode == REPEAT_ALL || force) {
                        mPlayPos = 0;
                    }
                } else {
                    mPlayPos++;
                }
     //       }
            }
            saveBookmarkIfNeeded();
            SharedPreferences music_settings = this.getSharedPreferences("Music_setting", 0);
            int a = music_settings.getInt("isFade", 0);
            //Log.i("isFade",a+"");
            //this.fadeouttype = FADEOUTNEXT;
            if(a == 1){
            	mMediaplayerHandler.sendEmptyMessage(FADEOUTOPEN);
            }else{
            	nextAfterFadeOut();
            }
            
        }
    }
    
    private void nextAfterFadeOut(){
    	stop(false);
        openCurrent();
        play();
        notifyChange(META_CHANGED);
        Intent intent = new Intent();
        intent.setAction(NowPlayingController.UPDATE_NOWPLAYINGALBUM);
//        intent.putExtra("albumbitmap", bm);
        sendBroadcast(intent);
    }
    
    private void gotoIdleState() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        stopForeground(true);
    }
    
    private void saveBookmarkIfNeeded() {
        try {
            if (isPodcast()) {
                long pos = position();
                long bookmark = getBookmark();
                long duration = duration();
                if ((pos < bookmark && (pos + 10000) > bookmark) ||
                        (pos > bookmark && (pos - 10000) < bookmark)) {
                    // The existing bookmark is close to the current
                    // position, so don't update it.
                    return;
                }
                if (pos < 15000 || (pos + 10000) > duration) {
                    // if we're near the start or end, clear the bookmark
                    pos = 0;
                }
                
                // write 'pos' to the bookmark field
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.BOOKMARK, pos);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursor.getLong(IDCOLIDX));
                getContentResolver().update(uri, values, null, null);
            }
        } catch (SQLiteException ex) {
        }
    }

    // Make sure there are at least 5 items after the currently playing item
    // and no more than 10 items before.
    private void doAutoShuffleUpdate() {
        boolean notify = false;

        // remove old entries
        if (mPlayPos > 10) {
            removeTracks(0, mAutoShuffleList.length);
            notify = true;
        }
        mHistory.clear();
        // add new entries if needed
        
        long first = getAudioId();
        if(first < 0) {
            mAutoShuffleList = MusicUtils.getAllSongs(getApplicationContext());
            mPlayList = MusicUtils.getAllSongs(getApplicationContext());
            if(mPlayList == null || mPlayList != null && mPlayList.length == 0) {
                return;
            }
            first = mAutoShuffleList[mRand.nextInt(mPlayList.length)];
        }
        //Log.i("mAutoShuffleList",mAutoShuffleList.length+"");
        //Log.i("mPlayListLen",mPlayList.length+"");
        mPlayListLen = mAutoShuffleList.length;
        mPlayList = new long[mPlayListLen];
        for(int i=0;i<mPlayListLen;i++){
        	mPlayList[i] = mAutoShuffleList[i];
        }
        /*mPlayList[0] = first;
        mPlayListLen++;
        
        int to_add = mAutoShuffleList.length ;//- //(mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
        
        while(mPlayListLen < to_add){//for (int i = 0; i <= to_add + 1; i++) {
            // pick something at random from the list

            int lookback = mHistory.size();
            int idx = -1;
            while(true) {
                idx = mRand.nextInt(mAutoShuffleList.length);
                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }
                //lookback /= 2;
            }
            if( mAutoShuffleList[idx] == mPlayList[0]){                
                continue;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            ensurePlayListCapacity(mPlayListLen + 1);
            mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
            notify = true;
        }*/
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    // check that the specified idx is not in the history (but only look at at
    // most lookbacksize entries in the history)
    private boolean wasRecentlyUsed(int idx, int lookbacksize) {

        // early exit to prevent infinite loops in case idx == mPlayPos
        if (lookbacksize == 0) {
            return false;
        }

        int histsize = mHistory.size();
        if (histsize < lookbacksize) {
            Log.d(LOGTAG, "lookback too big");
            lookbacksize = histsize;
        }
        int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            long entry = mHistory.get(i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    // A simple variation of Random that makes sure that the
    // value it returns is not equal to the value it returned
    // previously, unless the interval is 1.
    private static class Shuffler {
        private int mPrevious;
        private Random mRandom = new Random();
        public int nextInt(int interval) {
            if(interval==0)
                return -1;
            int ret;
            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);
            mPrevious = ret;
            return ret;
        }
    };

    private boolean makeAutoShuffleList() {
        ContentResolver res = getContentResolver();
        Cursor c = null;
        try {
            /*c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.IS_MUSIC + "=1",
                    null, null);
            if (c == null || c.getCount() == 0) {
                return false;
            }
            int len = c.getCount();
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                c.moveToNext();
                list[i] = c.getLong(0);
            }*/
        	long [] list = this.getQueue();
        	//list = this.getQueue();
            mAutoShuffleList = list;
            return true;
        } catch (RuntimeException ex) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return false;
    }
    
    /**
     * Removes the range of tracks specified from the play list. If a file within the range is
     * the file currently being played, playback will move to the next file after the
     * range. 
     * @param first The first file to be removed
     * @param last The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(int first, int last) {
        int numremoved = removeTracksInternal(first, last);
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }
    
    private int removeTracksInternal(int first, int last) {
        synchronized (this) {
            if (last < first) return 0;
            if (first < 0) first = 0;
            if (last >= mPlayListLen) last = mPlayListLen - 1;

            boolean gotonext = false;
            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                gotonext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= (last - first + 1);
            }
            int num = mPlayListLen - last - 1;
            for (int i = 0; i < num; i++) {
                mPlayList[first + i] = mPlayList[last + 1 + i];
            }
            mPlayListLen -= last - first + 1;
            
            if (gotonext) {
                if (mPlayListLen == 0) {
                    stop(true);
                    mPlayPos = -1;
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                } else {
                    if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }
                    boolean wasPlaying = isPlaying();
                    stop(false);
                    openCurrent();
                    if (wasPlaying) {
                        play();
                    }
                }
                notifyChange(META_CHANGED);
            }
            return last - first + 1;
        }
    }
    
    /**
     * Removes all instances of the track with the given id
     * from the playlist.
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    
    public int removeTrack(long id) {
        int numremoved = 0;
        synchronized (this) {
            for (int i = 0; i < mPlayListLen; i++) {
                if (mPlayList[i] == id) {
                    numremoved += removeTracksInternal(i, i);
                    i--;
                }
            }
        }
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }
    
    public void setShuffleMode(int shufflemode) {
        synchronized(this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }
            mShuffleMode = shufflemode;
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    if(mPlayPos == -1){
                    	mPlayPos = mRand.nextInt(mPlayListLen);
                    }
                    //mPlayPos = 0; 
                    //Log.i("mPlayPos",""+mPlayPos);
                    if(position() < 0) {
                        openCurrent();
                    }
                    //if(isPlaying()) {                        
                        //play();
                    //}
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    // failed to build a list of files to shuffle
                    mShuffleMode = SHUFFLE_NONE;
                }
            }
            saveQueue(false);
        }
    }
    public int getShuffleMode() {
        return mShuffleMode;
    }
    
    public void setRepeatMode(int repeatmode) {
        synchronized(this) {
            mRepeatMode = repeatmode;
            saveQueue(false);
        }
    }
    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    /**
     * Returns the path of the currently playing file, or null if
     * no file is currently playing.
     */
    public String getPath() {
        return mFileToPlay;
    }
    
    /**
     * Returns the rowid of the currently playing file, or -1 if
     * no file is currently playing.
     */
    public long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0 && mPlayer.isInitialized()) {
                return mPlayList[mPlayPos];
            }
        }
        return -1;
    }
    
    /**
     * Returns the position in the queue 
     * @return the position in the queue
     */
    public int getQueuePosition() {
        synchronized(this) {
            return mPlayPos;
        }
    }
    
    /**
     * Starts playing the track at the given position in the queue.
     * @param pos The position in the queue of the track that will be played.
     */
    public void setQueuePosition(int pos) {
        synchronized(this) {
            stop(false);
            mPlayPos = pos;
            openCurrent();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    public String getArtistName() {
        synchronized(this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
    
    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }

    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }

    public String[] getTrackNameNext() {
    	String [] TrackNameNext = new String[4];
    	synchronized (this) {
			if(mCursor == null || mCursor.getCount() == 0) {
    			return null;
    		}
    		if(mCursor.getCount() >=4) {
    			for(int i=0; i<4; i++)
    			TrackNameNext[i] = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
    			mCursor.moveToNext();
    			
    		}
    	}
		return TrackNameNext;
    	
    }
    
    private boolean isPodcast() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return false;
            }
            return (mCursor.getInt(PODCASTCOLIDX) > 0);
        }
    }
    
    private long getBookmark() {
        synchronized (this) {
            if (mCursor == null || mCursor.getCount() == 0) {
                return 0;
            }
            return mCursor.getLong(BOOKMARKCOLIDX);
        }
    }
    
    /**
     * Returns the duration of the file in milliseconds.
     * Currently this method returns -1 for the duration of MIDI files.
     */
    public long duration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }
        return -1;
    }

    /**
     * Returns the current playback position in milliseconds
     */
    public long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }
        return -1;
    }

    /**
     * Seeks to the position specified.
     *
     * @param pos The position to seek to, in milliseconds
     */
    public long seek(long pos) {
        if (mPlayer.isInitialized()) {
            if (pos < 0) pos = 0;
            if (pos > mPlayer.duration()) pos = mPlayer.duration();
            return mPlayer.seek(pos);
        }
        return -1;
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId: the audio session ID.
     */
    public void setAudioSessionId(int sessionId) {
        synchronized (this) {
            mPlayer.setAudioSessionId(sessionId);
        }
    }

    /**
     * Returns the audio session ID.
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }
    
    private final BroadcastReceiver preferenceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            levels = new short[5];
            String [] eqResult =  (intent.getStringExtra("levles")).split(";");
            for(int i = 0; i < 5; i++) {
                if(eqResult.length == 5) {
                    levels[i] = (short) (Float.valueOf(eqResult[i]) * 1);
                } else {
                    levels[i] = 0;
                }
            }
            updateDsp();
        }
    };
    
    protected void updateDsp() {
        mEqualizer.setEnabled(true);
        int len = levels.length;

        for (short i = 0; i < len; i ++) {
            mEqualizer.setBandLevel(i, levels[i]);
        }
    }

/*   MultiPlayer 
 * 	 *//**
     * Provides a unified interface for dealing with midi files and
     * other media files.
     *//*
    private class MultiPlayer {
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Handler mHandler;
        private boolean mIsInitialized = false;

        public MultiPlayer() {
            mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSource(String path) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(MediaPlaybackService.this, Uri.parse(path));
                } else {
                    mMediaPlayer.setDataSource(path);
                }
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
            } catch (IOException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            } catch (IllegalArgumentException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            }
            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnErrorListener(errorListener);
            Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
            sendBroadcast(i);
            mIsInitialized = true;
        }
        
        public boolean isInitialized() {
            return mIsInitialized;
        }

        public void start() {
            //MusicUtils.debugLog(new Exception("MultiPlayer.start called"));
            mMediaPlayer.start();
        }

        public void stop() {
            mMediaPlayer.reset();
            mIsInitialized = false;
        }

        *//**
         * You CANNOT use this player anymore after calling release()
         *//*
        public void release() {
            stop();
            mMediaPlayer.release();
        }
        
        public void pause() {
            mMediaPlayer.pause();
        }
        
        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                // Acquire a temporary wakelock, since when we return from
                // this callback the MediaPlayer will release its wakelock
                // and allow the device to go to sleep.
                // This temporary wakelock is released when the RELEASE_WAKELOCK
                // message is processed, but just in case, put a timeout on it.
                mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        };

        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mIsInitialized = false;
                    mMediaPlayer.release();
                    // Creating a new MediaPlayer and settings its wakemode does not
                    // require the media service, so it's OK to do this now, while the
                    // service is still being restarted
                    mMediaPlayer = new MediaPlayer(); 
                    mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                default:
                    Log.d("MultiPlayer", "Error: " + what + "," + extra);
                    break;
                }
                return false;
           }
        };

        public long duration() {
            return mMediaPlayer.getDuration();
        }

        public long position() {
            return mMediaPlayer.getCurrentPosition();
        }

        public long seek(long whereto) {
            mMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        public void setVolume(float vol) {
            mMediaPlayer.setVolume(vol, vol);
        }

        public void setAudioSessionId(int sessionId) {
            mMediaPlayer.setAudioSessionId(sessionId);
        }

        public int getAudioSessionId() {
            return mMediaPlayer.getAudioSessionId();
        }
    }*/

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still
     * has a remote reference to the stub.
     */
    static class ServiceStub extends IMediaPlaybackService.Stub {
        WeakReference<MediaPlaybackService> mService;
        
        ServiceStub(MediaPlaybackService service) {
            mService = new WeakReference<MediaPlaybackService>(service);
        }

        public void openFile(String path){
            if(mService.get() == null) {
                return;
            }
            mService.get().open(path);
        }
        public void open(long [] list, int position) {
            if(mService.get() == null) {
                return;
            }
            mService.get().open(list, position);
        }
        public int getQueuePosition() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getQueuePosition();
        }
        public void setQueuePosition(int index) {
            if(mService.get() == null) {
                return;
            }
            mService.get().setQueuePosition(index);
        }
        public boolean isPlaying() {
            if(mService.get() == null) {
                return false;
            }
            return mService.get().isPlaying();
        }
        public void stop() {
            if(mService.get() == null) {
                return;
            }
            mService.get().stop();
        }
        public void pause() {
            if(mService.get() == null) {
                return;
            }
            mService.get().pause();
        }
        public void play() {
            if(mService.get() == null) {
                return;
            }
            mService.get().play();
        }
        public void prev() {
            if(mService.get() == null) {
                return;
            }
            mService.get().prev();
        }
        public void next() {
            if(mService.get() == null) {
                return;
            }
            mService.get().next(true);
        }
        public String getTrackName() {
            if(mService.get() == null) {
                return "";
            }
            return mService.get().getTrackName();
        }
        public String getAlbumName() {
            if(mService.get() == null) {
                return "";
            }
            return mService.get().getAlbumName();
        }
        public long getAlbumId() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getAlbumId();
        }
        public String getArtistName() {
            if(mService.get() == null) {
                return "";
            }
            return mService.get().getArtistName();
        }
        public long getArtistId() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getArtistId();
        }
        public void enqueue(long [] list , int action) {
            if(mService.get() == null) {
                return;
            }
            mService.get().enqueue(list, action);
        }
        public long [] getQueue() {
            if(mService.get() == null) {
                return null;
            }
            return mService.get().getQueue();
        }
        public void moveQueueItem(int from, int to) {
            if(mService.get() == null) {
                return;
            }
            mService.get().moveQueueItem(from, to);
        }
        public String getPath() {
            if(mService.get() == null) {
                return "";
            }
            return mService.get().getPath();
        }
        public long getAudioId() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getAudioId();
        }
        public long position() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().position();
        }
        public long duration() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().duration();
        }
        public long seek(long pos) {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().seek(pos);
        }
        public void setShuffleMode(int shufflemode) {
            if(mService.get() == null) {
                return;
            }
            mService.get().setShuffleMode(shufflemode);
        }
        public int getShuffleMode() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getShuffleMode();
        }
        public int removeTracks(int first, int last) {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().removeTracks(first, last);
        }
        public int removeTrack(long id) {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().removeTrack(id);
        }
        public void setRepeatMode(int repeatmode) {
            if(mService.get() == null) {
                return;
            }
            mService.get().setRepeatMode(repeatmode);
        }
        public int getRepeatMode() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getRepeatMode();
        }

        public int getMediaMountedCount() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getMediaMountedCount();
        }
        public int getAudioSessionId() {
            if(mService.get() == null) {
                return -1;
            }
            return mService.get().getAudioSessionId();
        }
    }



    private final IBinder mBinder = new ServiceStub(this);
}
