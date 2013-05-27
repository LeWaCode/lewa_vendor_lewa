package com.lewa.player.helper;

import java.io.IOException;

import com.lewa.player.MediaPlaybackService;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;

public class AsyncMusicPlayer {
	
	        private MediaPlayer mMediaPlayer = new MediaPlayer();
	        private Handler mHandler;
	        private boolean mIsInitialized = false;
	        private PowerManager.WakeLock mWakeLock;
	        private Context mContext;

	        public AsyncMusicPlayer(Context context) {
	        	mContext = context;
	           // mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
	        }

	        private void acquireWakeLock()
	        {
	          if (this.mWakeLock == null)
	            return;
	          this.mWakeLock.acquire();
	        }
	        
	        private void releaseWakeLock()
	        {
	          if (this.mWakeLock == null)
	            return;
	          this.mWakeLock.release();
	        }
	        
	       public void setDataSource(String path) {
	            try {
	                mMediaPlayer.reset();
	                mMediaPlayer.setOnPreparedListener(null);
	                if (path.startsWith("content://")) {
	                    mMediaPlayer.setDataSource(mContext, Uri.parse(path));
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
/*	            Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
	            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
	            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
	            sendBroadcast(i);*/
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
	            	acquireWakeLock();
	                mHandler.sendEmptyMessage(MediaPlaybackService.TRACK_ENDED);
	                mHandler.sendEmptyMessage(MediaPlaybackService.RELEASE_WAKELOCK);
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
	                    //mMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
	                    //mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
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
	    
	
}
