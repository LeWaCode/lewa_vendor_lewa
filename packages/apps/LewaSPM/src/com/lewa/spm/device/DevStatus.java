package com.lewa.spm.device;

import java.util.HashMap;
import java.util.Map;

import com.lewa.spm.R;
import com.lewa.spm.utils.PrefUtils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;
import android.widget.Toast;


public class DevStatus {
	
	private static DevStatus _instance = null;
	private static Context mContext;
	
	@SuppressWarnings("static-access")
	private DevStatus(Context mContext) {
		this.mContext = mContext;		
	}

	public static DevStatus getInstance(Context mContext) {
		if (_instance == null) {
			_instance = new DevStatus(mContext);
		}
		return _instance;
	}

	/**
	 * 0~255
	 * @return
	 */
	public int getBrightStatus(){
	    int nowBrightnessValue = 10;
        try {
			nowBrightnessValue = android.provider.Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}	    
	    return nowBrightnessValue;		
	}
		 
	public boolean isAutoBrightness() {
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(mContext.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }
	
	public int getTimeoutStatus(){
		return Settings.System.getInt(mContext.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,30000);
	}
	
	public boolean getBTStatus() {
		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter!=null){
			return mBluetoothAdapter.isEnabled();
		}else{
			return false;
		}
	}

	public boolean getWifiStatus() {		
		WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);	

		if(mWifiManager!=null){
			return mWifiManager.isWifiEnabled();
		}else{
			return false;
		}
	}

	public boolean getMobileDataStatus() {
        ConnectivityManager cm = (ConnectivityManager) mContext
        		.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getMobileDataEnabled();
	}

	public boolean getGpsStatus() {
		LocationManager myLocationManager = (LocationManager )mContext.getSystemService(Context.LOCATION_SERVICE);
		return myLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	public boolean getSyncStatus() {
		return ContentResolver.getMasterSyncAutomatically();
	}
	
	public boolean getVibrateStatus(){
		AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		
		return (mAudioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER)
				==AudioManager.VIBRATE_SETTING_ON);
	}
	
	public boolean getHapticFb(){
		return (Settings.System.getInt(mContext.getContentResolver() , Settings.System.HAPTIC_FEEDBACK_ENABLED,0)==1);	
	}
	
	public boolean isScreenOn(Context paramContext) {
		return ((PowerManager) paramContext.getSystemService("power")).isScreenOn();
	}
	
	public Map<String,String> getAllDeviceStatus(){
		String mArray[] = PrefUtils.MAP_KEY_ARRAY;		
		Map<String,String> mMap = new HashMap<String,String>();
		
		for(int j = 0;j<mArray.length;j++){
			
			if(mArray[j].equals(PrefUtils.MAP_KEY_DATA)){
				mMap.put(mArray[j], String.valueOf(getMobileDataStatus()));
			}else if(mArray[j].equals(PrefUtils.MAP_KEY_WLAN)){
				mMap.put(mArray[j], String.valueOf(getWifiStatus()));
			}else if(mArray[j].equals(PrefUtils.MAP_KEY_BLUETOOTH)){
				mMap.put(mArray[j], String.valueOf(getBTStatus()));
			}else if(mArray[j].equals(PrefUtils.MAP_KEY_BRIGHT)){
				if(this.isAutoBrightness()) {
					mMap.put(mArray[j], mContext.getString(R.string.spm_auto));
				} else {
					mMap.put(mArray[j], String.valueOf(getBrightStatus()));
				}
			}else if(mArray[j].equals(PrefUtils.MAP_KEY_TIMEOUT)){
				mMap.put(mArray[j], String.valueOf(this.getTimeoutStatus()));
			}else if(mArray[j].equals(PrefUtils.MAP_KEY_GPS)){
				mMap.put(mArray[j], String.valueOf(getGpsStatus()));
			}else if(mArray[j].equals(PrefUtils.MAP_KEY_TOUCH)){
				mMap.put(mArray[j], String.valueOf(this.getHapticFb()));
			}			
		}
		return mMap;		
	}	
}
