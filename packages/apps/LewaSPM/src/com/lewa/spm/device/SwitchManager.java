package com.lewa.spm.device;

import java.util.HashMap;
import java.util.Map;

import com.lewa.spm.R;
import com.lewa.spm.SPMActivity;
import com.lewa.spm.utils.PrefUtils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.WindowManager;

import android.os.IPowerManager;
import android.os.Power;
import android.os.ServiceManager;
import android.os.RemoteException;

public class SwitchManager {

	private Context mContext;
	
	private static SwitchManager _instance = null;
	
	/**
	 * 
	 * @param mContext
	 */
	public SwitchManager(Context mContext) {
		this.mContext = mContext;
	}

	public static SwitchManager getInstance(Context paramContext) {
		if (_instance == null)
			_instance = new SwitchManager(paramContext);
		return _instance;
	}
	
	/**
	 * 
	 * @param brightValue
	 * 		0~255 int value
	 */
	public boolean brightSwitch(int brightValue) {
		
		try {			
			
            IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager.getService("power"));
            
            if (power != null) {
                
            	Log.d("SwitchManager", "brightValue=" + brightValue);
            	
                power.setBacklightBrightness(brightValue);
                
                Settings.System.putInt(mContext.getContentResolver() , Settings.System.SCREEN_BRIGHTNESS, brightValue);
                
            }
        } catch (RemoteException e) {
            Log.d("SwitchManager", "toggleBrightness: " + e.toString());
        }		
		
		return true;
	}

	/**
	 * 
	 * @param isAuto
	 */
	public void setAutoBrightness(boolean isAuto){
		if(isAuto){
			 Settings.System.putInt(mContext.getContentResolver(),
	             Settings.System.SCREEN_BRIGHTNESS_MODE,
	                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		}else{
			Settings.System.putInt(mContext.getContentResolver(),
	                Settings.System.SCREEN_BRIGHTNESS_MODE,
	                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);	
		}
		Uri uri = android.provider.Settings.System.getUriFor(android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE);
		mContext.getContentResolver().notifyChange(uri, null);
	}
	
	public boolean timeoutSwitch(int mInt) {
		return Settings.System.putInt(mContext.getContentResolver(),
				Settings.System.SCREEN_OFF_TIMEOUT, mInt);
	}

	/**
	 * 
	 * @param mBoolean
	 * @return
	 */
	public boolean btSwitch(boolean mBoolean) {
		return mBoolean ? BluetoothAdapter.getDefaultAdapter().enable()
				: BluetoothAdapter.getDefaultAdapter().disable();
	}

	/**
	 * 
	 * @param mBoolean
	 */
	public void gpsSwitch(boolean mBoolean) {
		Secure.setLocationProviderEnabled(mContext.getContentResolver(),
				LocationManager.GPS_PROVIDER, mBoolean);
	}

	/**
	 * 
	 * @param mBoolean
	 */
	public void syncSwitch(boolean mBoolean) {
		ContentResolver.setMasterSyncAutomatically(mBoolean);
	}
	
	public void mobileDataSwitch(boolean mBoolean) {
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);        
        cm.setMobileDataEnabled(mBoolean);        
	}

	/**
	 * 
	 * @param mBoolean
	 */
	public void wifiSwitch(boolean mBoolean) {
		((WifiManager) this.mContext.getSystemService("wifi"))
				.setWifiEnabled(mBoolean);
	}
	
	/**
	 * 
	 * @param mBoolean
	 */
	public void vibrateSwitch(boolean mBoolean){
		AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		
		mAudioManager.setVibrateSetting( AudioManager.VIBRATE_TYPE_RINGER , mBoolean ? 1 : 0 );
	}
	
	/**
	 * 
	 * @param hapticFbOn
	 * @return
	 */
	public boolean setHapticFb(boolean hapticFbOn){
		 return Settings.System.putInt(mContext.getContentResolver(), Settings.System.HAPTIC_FEEDBACK_ENABLED,
				 hapticFbOn ? 1 : 0);
	}
	
	/**
	 * 
	 * @param airOn
	 */
	public void setAirSwitch(boolean airOn){		
    	Settings.System.putString(mContext.getContentResolver(),Settings.System.AIRPLANE_MODE_ON, "0");
    	Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    	mContext.sendBroadcast(intent);		
	}

	public void setAllDeviceStatus(Map<String,String> mMap){
		
		if(mMap==null)
			return;
		
		String mArray[] = PrefUtils.MAP_KEY_ARRAY;
		
		for(int j = 0;j<mArray.length;j++){
			
			if(mArray[j].equals(PrefUtils.MAP_KEY_DATA)){
				mobileDataSwitch(Boolean.valueOf(mMap.get(mArray[j])));
			} else if(mArray[j].equals(PrefUtils.MAP_KEY_WLAN)){
				wifiSwitch(Boolean.valueOf(mMap.get(mArray[j])));
			} else if(mArray[j].equals(PrefUtils.MAP_KEY_BLUETOOTH)){
				btSwitch(Boolean.valueOf(mMap.get(mArray[j])));				
			} else if(mArray[j].equals(PrefUtils.MAP_KEY_BRIGHT)){				
				if(mMap.get(mArray[j]).equals(mContext.getString(R.string.spm_auto)))//TODO bug here when change language,it run way.
					setAutoBrightness(true);
				else
					brightSwitch(Integer.valueOf(mMap.get(mArray[j])));
			} else if(mArray[j].equals(PrefUtils.MAP_KEY_TIMEOUT)){				
				timeoutSwitch(Integer.valueOf(mMap.get(mArray[j])));				
			} else if(mArray[j].equals(PrefUtils.MAP_KEY_GPS)){
				gpsSwitch(Boolean.valueOf(mMap.get(mArray[j])));				
			} else if(mArray[j].equals(PrefUtils.MAP_KEY_TOUCH)){
				setHapticFb(Boolean.valueOf(mMap.get(mArray[j])));
			}			
		}		
	}
}
