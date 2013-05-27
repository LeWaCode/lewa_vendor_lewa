package com.lewa.spm.utils;

import java.util.Date;

import com.lewa.spm.device.BatteryInfo;
import com.lewa.spm.device.DevStatus;
import com.lewa.spm.device.SwitchManager;
import com.lewa.spm.element.ConsumeValue;

import android.content.Context;
import android.util.Log;

public class ModeUtils {

	private String TAG = "ModeUtils";
	
	Context mContext;
	private final String timeSplider = ":";
	private static ModeUtils _instance = null;
	
	private ModeUtils(Context paramContext) {
		this.mContext = paramContext;
	}

	public static ModeUtils getInstance(Context paramContext) {
		if (_instance == null)
			_instance = new ModeUtils(paramContext);
		return _instance;
	}
	
	/**
	 * TODO should be include preference save???
	 * 	MODE_NULL = 0;enterPreMode
	 *	MODE_AIR = 1;
	 *	MODE_NORMAL = 2;	
	 *	MODE_INTEL_TIME = 3;
	 *	MODE_INTEL_POWER = 4;
	 * @param modeNum
	 */
	public void enterMode(int modeNum){
		switch(modeNum){
			case ConsumeValue.MODE_NULL :
				//MODE_NULL,(Reserved)
				enterPreMode();
				break;
			case ConsumeValue.MODE_AIR ://MODE_AIR
				enterAirMode();
				break;
			case ConsumeValue.MODE_NORMAL ://MODE_NORMAL
				enterNormalMode();
				break;
			case ConsumeValue.MODE_INTEL_TIME ://MODE_INTEL_TIME
				enterTimeMode();		
				break;
			case ConsumeValue.MODE_INTEL_POWER ://MODE_INTEL_POWER
				enterPowerMode();
				break;
			case ConsumeValue.MODE_OPT_NORMAL :
				enterOptNormalMode();
				break;
			case ConsumeValue.MODE_OPT_SUPER :
				enterOptSuperMode();
				break;
			default :
				break;
		}
	}
	
	//TODO should close device
	private void enterPreMode() {
		Log.i(TAG, "enterPreMode()");
	}
	
	private void enterNormalMode(){
		Log.i(TAG, "enterNormalMode()");		
	}
	
	private void enterAirMode(){
		Log.i(TAG, "enterAirMode()");
		SwitchManager.getInstance(mContext).mobileDataSwitch(false);
		SwitchManager.getInstance(mContext).wifiSwitch(false);
		SwitchManager.getInstance(mContext).btSwitch(false);
		SwitchManager.getInstance(mContext).brightSwitch(77);
		SwitchManager.getInstance(mContext).timeoutSwitch(15000);
		SwitchManager.getInstance(mContext).gpsSwitch(false);
		SwitchManager.getInstance(mContext).setHapticFb(false);
	}
	
	private void enterTimeMode(){
		Log.i(TAG, "enterTimeMode()");
	}
	
	private void enterPowerMode(){
		Log.i(TAG, "enterPowerMode()");
	}
	
	private void enterOptNormalMode(){
		Log.i(TAG, "enterOptNormalMode()");
		SwitchManager.getInstance(mContext).mobileDataSwitch(true);
		SwitchManager.getInstance(mContext).wifiSwitch(false);
		SwitchManager.getInstance(mContext).btSwitch(false);
		SwitchManager.getInstance(mContext).setAutoBrightness(true);
		SwitchManager.getInstance(mContext).timeoutSwitch(60000);
		SwitchManager.getInstance(mContext).gpsSwitch(false);
		SwitchManager.getInstance(mContext).setHapticFb(true);
	}
	
	private void enterOptSuperMode(){
		Log.i(TAG, "enterOptSuperMode()");
		SwitchManager.getInstance(mContext).mobileDataSwitch(false);
		SwitchManager.getInstance(mContext).wifiSwitch(false);
		SwitchManager.getInstance(mContext).btSwitch(false);
		SwitchManager.getInstance(mContext).brightSwitch(77);
		SwitchManager.getInstance(mContext).setAutoBrightness(false);
		SwitchManager.getInstance(mContext).timeoutSwitch(30000);
		SwitchManager.getInstance(mContext).gpsSwitch(false);
		SwitchManager.getInstance(mContext).setHapticFb(false);
	}
	
	/**
	 * 
	 * Check the device should enter time intelligent mode
	 * @return whether device will run in this mode,call only when power mode is set.
	 */
	public boolean checkIntelTime(){
		
		Date curDate = new Date();
		int curHours = curDate.getHours();
		int curMinutes = curDate.getMinutes();
		
		String fromStr = PrefUtils.getInstance(mContext).getIntelTimeFrom();
		String toStr = PrefUtils.getInstance(mContext).getIntelTimeTo();
		if(fromStr.contains(timeSplider)){
			int fromIntHours = Integer.parseInt(fromStr.split(timeSplider)[0]);
			int fromIntMinutes= Integer.parseInt(fromStr.split(timeSplider)[1]);
			int toIntHours = Integer.parseInt(toStr.split(timeSplider)[0]);
			int toIntMinutes= Integer.parseInt(toStr.split(timeSplider)[1]);
			if(fromIntHours<toIntHours){//same day
				if(curHours>fromIntHours && curHours<toIntHours){
					return true;
				}
				else if(curHours == fromIntHours && curMinutes >= fromIntMinutes){
					return true;//
				}
				else if(curHours == toIntHours && curMinutes < toIntMinutes){
					return true;
				}
				else{
					return false;
				}
			}else if(fromIntHours>toIntHours){//two days
				if(curHours<fromIntHours && curHours>toIntHours){
					return false;
				}
				else if(curHours == fromIntHours && curMinutes >= fromIntMinutes){
					return true;//
				}
				else if(curHours == toIntHours && curMinutes < toIntMinutes){
					return true;
				}
				else{
					return true;
				}
			}else{//fromIntHours==toIntHours
				//same day and same hour may be
				if(fromIntMinutes<toIntMinutes && curMinutes>=fromIntMinutes && curMinutes<toIntMinutes){
					return true;
				}
				else{
					return false;
				}
			}
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * Check the device should enter power intelligent mode
	 * @return whether device will run in this mode,call only when power mode is set.
	 * 
	 */
	public boolean checkIntelPower(){
		
		int prefPower = PrefUtils.getInstance(mContext).getIntelLowPower();
		int curPower = Integer.parseInt(BatteryInfo.getInformation(BatteryInfo.battPresent));
		
		return (curPower<=prefPower);
	}

	//normal:data_on,wlan_off,bt_off,bright_auto,timeout_1m,gps_off,touch_on	
	public boolean maskNormalModeList(int devId){
		boolean returnValue = false;
		switch(devId){
			case ConsumeValue.OPT_DATA_ID:
				returnValue = !DevStatus.getInstance(mContext).getMobileDataStatus();
				break;
			case ConsumeValue.OPT_WLAN_ID:
				returnValue = DevStatus.getInstance(mContext).getWifiStatus();
				break;
			case ConsumeValue.OPT_BLUETOOTH_ID:
				returnValue = DevStatus.getInstance(mContext).getBTStatus();
				break;
			case ConsumeValue.OPT_BRIGHT_ID:
				returnValue = !DevStatus.getInstance(mContext).isAutoBrightness();
				break;
			case ConsumeValue.OPT_TIMEOUT_ID:
				returnValue = !(DevStatus.getInstance(mContext).getTimeoutStatus()==60000);
				break;
			case ConsumeValue.OPT_GPS_ID:
				returnValue = DevStatus.getInstance(mContext).getGpsStatus();
				break;
			case ConsumeValue.OPT_TOUCH_ID:
				returnValue = !DevStatus.getInstance(mContext).getHapticFb();
				break;			
			default:
				break;
		}
		return returnValue;
	}
	
	//super:data_off,wlan_off,bt_off,bright_30,timeout_30s,gps_off,touch_off
	public boolean maskSuperModeList(int devId){
		boolean returnValue = false;
		switch(devId){
			case ConsumeValue.OPT_DATA_ID:
				returnValue = DevStatus.getInstance(mContext).getMobileDataStatus();
				break;
			case ConsumeValue.OPT_WLAN_ID:
				returnValue = DevStatus.getInstance(mContext).getWifiStatus();
				break;
			case ConsumeValue.OPT_BLUETOOTH_ID:
				returnValue = DevStatus.getInstance(mContext).getBTStatus();
				break;
			case ConsumeValue.OPT_BRIGHT_ID:
				if(DevStatus.getInstance(mContext).isAutoBrightness())
					returnValue = true;
				else
					returnValue = !(DevStatus.getInstance(mContext).getBrightStatus()==77);
				break;
			case ConsumeValue.OPT_TIMEOUT_ID:
				returnValue = !(DevStatus.getInstance(mContext).getTimeoutStatus()==30000);
				break;
			case ConsumeValue.OPT_GPS_ID:
				returnValue = DevStatus.getInstance(mContext).getGpsStatus();
				break;
			case ConsumeValue.OPT_TOUCH_ID:
				returnValue = DevStatus.getInstance(mContext).getHapticFb();
				break;
			default:
				break;
		}
		return returnValue;
		
	}
}
