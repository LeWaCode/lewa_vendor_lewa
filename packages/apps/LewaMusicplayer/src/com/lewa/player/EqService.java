package com.lewa.player;

import java.util.HashMap;

import com.lewa.player.ui.MusicEQActivity;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.Equalizer;
import android.os.IBinder;
import android.util.Log;


/**
 * <p>
 * This calls listen to two kinds of events:
 * <ol>
 * <li>headset plug / unplug events. The android framework only delivers
 *     these events to a running process, so there must be a service listening
 *     to them.
 * <li>preference update events.
 * </ol>
 * <p>
 * When new events arrive, they are pushed into the audio stack
 * using AudioManager.setParameters().
 * 
 * @author alankila
 */
public class EqService extends Service {

	protected static final String TAG = EqService.class.getSimpleName();
	
	public static final String NAME = "com.lewa.player.helper.EqService";
	
	private Equalizer mEqualizer;
	
	HashMap<Short, Short> levels = new HashMap<Short, Short>();


	/**
	 * Update audio parameters when preferences have been updated.
	 */
    private final BroadcastReceiver preferenceUpdateReceiver = new BroadcastReceiver() {
		@SuppressWarnings("deprecation")
        @Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "Preferences updated.");
			levels.clear();
			levels = (HashMap<Short, Short>) intent.getExtra("levles");
			updateDsp();
		}
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Starting service.");

		mEqualizer = new Equalizer(0, 0);
		mEqualizer.setEnabled(true);
		
		startForeground(0, new Notification());

		registerReceiver(preferenceUpdateReceiver, new IntentFilter(MusicEQActivity.ACTION_UPDATE_EQ));
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(preferenceUpdateReceiver);
		stopForeground(true);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected void updateDsp() {
	    mEqualizer.setEnabled(true);
		int len = levels.size();

		for (short i = 0; i < len; i ++) {
			mEqualizer.setBandLevel(i, levels.get(i));
		}
	}
}
