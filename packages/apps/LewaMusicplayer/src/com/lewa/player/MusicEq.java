package com.lewa.player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.lewa.player.helper.AsyncMusicPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.EnvironmentalReverb;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.util.Log;



public class MusicEq implements Runnable{

	
	public Handler mMediaHandler;
	public Thread mEqThread;
	public AsyncMusicPlayer mMediaPlayer;
	public HashMap<Short, Short> mlevels;
	public Context mContext;
	
	public MusicEq(){
		mEqThread = new Thread(this);
		mEqThread.start();
	}
	
	public void destroy(){
		if(mEqThread != null) {
			mEqThread.interrupt();
			mEqThread.destroy();
		}
	}
	
	public void setHandler(Handler mediaHandler){
		mMediaHandler = mediaHandler;
	}
	
	public void setMediaPlayer(Context context, AsyncMusicPlayer MediaPlayer){
		mMediaPlayer = MediaPlayer;
		mContext = context;
	}
	
	public void setEQ(HashMap<Short, Short> levels){
		mlevels = levels;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true) {
			try {
			if(mlevels != null) {
				equalize(mContext, mMediaPlayer, mlevels, true);
			}
			Thread.sleep(1000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}
	
	public void equalize(Context mContext, AsyncMusicPlayer mMediaPlayer, HashMap<Short, Short> levels, boolean enabled) {
		
		String TAG = "prototype_equalize";
		int sessionId = mMediaPlayer.getAudioSessionId();
		//Log.d(TAG, String.format("media player session id: %s", sessionId));
		Equalizer eq = new Equalizer(0, sessionId);
		if (!enabled) {
			eq.setEnabled(false);
			return;
		}
		boolean eqEnabled = eq.getEnabled();
		if (!eqEnabled) {
			//Log.d(TAG, "enabling EQ");
			eq.setEnabled(!eqEnabled);
		}
		
		Set<java.util.Map.Entry<Short, Short>> set = levels.entrySet();
		Iterator<java.util.Map.Entry<Short, Short>> iter = set.iterator();
		while (iter.hasNext()) {
			java.util.Map.Entry<Short, Short> entry = iter.next();
			short band = entry.getKey();
			short level = entry.getValue();
			//Log.d(TAG, String.format("got setting for band: %d, level: %s", band, level));
			eq.setBandLevel(band, level);
		}
			
		for (short eq_i = 0; eq_i < eq.getNumberOfBands(); eq_i++) {
			short eq_range[] = eq.getBandLevelRange();
			//Log.d(TAG, String.format("band: %d, level: %d, range: %d -> %d", eq_i, eq.getBandLevel(eq_i), eq_range[0], eq_range[1]));
		}
		
	}
	
	public void bassBoost(Context mContext, MediaPlayer mMediaPlayer, boolean enabled) {
		String TAG = "prototype_bassBoost";
		int sessionId = mMediaPlayer.getAudioSessionId();
		BassBoost bb = new BassBoost(0, sessionId);
		if (!enabled) {
			bb.setEnabled(enabled);
			return;
		}
		boolean bbEnabled = bb.getEnabled();
		Log.d(TAG, String.format("bb enabled? %s", bbEnabled));
		if (!bbEnabled) {
			Log.d(TAG, "enabling Bass Boost");
			bb.setEnabled(!bbEnabled);
		}
		/*if (bb.getStrengthSupported())
			bb.setStrength((short) BB_LEVEL_LOW);*/
		Log.d(TAG, String.format("bass boost set to: %d", bb.getRoundedStrength()));
		
	}
	
	/**
	 * Changes the "reverb" (echo/surround) effect.
	 * 
	 * @param mContext
	 * @param mMediaPlayer
	 * @param enabled
	 */
	public void reverberate(Context mContext, MediaPlayer mMediaPlayer, boolean enabled) {
		/*
		 * Source: http://www.java2s.com/Open-Source/Android/android-core/platform-frameworks-base/android/media/audiofx/EnvironmentalReverb.java.htm
		 * 
		 * setDecayHFRatio(short 100-2000) -		The high frequency decay rate (relative to low freq). Lower values = faster high freq. decay
		 * setDecayTime(short 100-20000) -			The length of the total "echo" effect. Higher the value, the louder each echo is
		 * setDensity(short 0-1000) -				Lower the value, the more "hollow" the sound
		 * setRoomLevel(short -9000-0) -			Sets master volume of "reverb" effect
		 * setDiffusion(short 0-1000) - 			Lower value is more "chucky" echo effect
		 * setReflectionsLevel(short -9000-1000) - 	Initial volume of early "reflections"
		 * setReflectionsDelay(int 0-300) -			Delay until first "reflection" is heard
		 * setReverbLevel(short -9000-2000) -		Overall volume of late echoes
		 * setReverbDelay(int 0-100) -				Delay from first "reflection" and echo
		 */
			String TAG = "prototype_reverb";
			int sessionId = mMediaPlayer.getAudioSessionId();
			EnvironmentalReverb rv = new EnvironmentalReverb(0, sessionId);
			
			if (!enabled) {
				rv.setEnabled(enabled);
				return;
			}
			boolean rvEnabled = rv.getEnabled();
			Log.d(TAG, String.format("rv enabled? %s", rvEnabled));
			if (!rvEnabled) {
				Log.d(TAG, "enabling reverb");
				rv.setEnabled(!rvEnabled);
			}
			// TODO: Instead of arbitrary values, change to constants and "presets" (??????)
			//rv.setDecayHFRatio((short) 1000);
			rv.setDecayTime(3000);
			//rv.setDensity((short) 300);
			//rv.setRoomLevel((short) -2000);
			//rv.setDiffusion((short) 100);
			//rv.setReflectionsLevel((short) -4500);
			//rv.setReflectionsDelay(300);
			rv.setReverbLevel((short) -5000);
			rv.setReverbDelay(100);
			reverbStat(rv);
			mMediaPlayer.attachAuxEffect(rv.getId());
		
	}
	
	public void reverbStat(EnvironmentalReverb rv) {
		String TAG = "prototype_reverbStat";
		short decayHFRatio = rv.getDecayHFRatio();
		int decayTime = rv.getDecayTime();
		short density = rv.getDensity();
		short diffusion = rv.getDiffusion();
		int reflectionsDelay = rv.getReflectionsDelay();
		short reflectionsLevel = rv.getReflectionsLevel();
		int reverbDelay = rv.getReverbDelay();
		short reverbLevel = rv.getReverbLevel();
		short roomHFLevel = rv.getRoomHFLevel();
		short roomLevel = rv.getRoomLevel();
		Log.d(TAG, String.format(
			"decay hf ratio: %s, decay time: %s, density: %s, diffusion: %s, reflections delay: %s, reflections level: %s, reverb delay: %s, reverb level: %s, room hf level: %s, room level: %s",
			decayHFRatio,
			decayTime,
			density,
			diffusion,
			reflectionsDelay,
			reflectionsLevel,
			reverbDelay,
			reverbLevel,
			roomHFLevel,
			roomLevel
		));
	}



}
