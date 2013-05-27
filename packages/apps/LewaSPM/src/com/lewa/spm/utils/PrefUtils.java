package com.lewa.spm.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefUtils {

	// the content include:current mode,intel mode parameter,

	public final String TAG = "PrefUtils";
	
	public static String PREFS_NAME = "com.lewa.spm";		//preference tag

	public static String NO_VALUE = "NULLVAL";				//no value in preference
	
	public static String CUR_MODE = "CURMODE";				//current mode
	public static String PRE_MODE = "PREMODE";				//previous mode
	
	public static String OLD_CONSUME = "OLDCONSUME";		//old consume value
	public static int OLD_CONSUME_DEF = -1;
	
	public static String INTEL_MODE_TIME_ON = "INTIMEON";	//intelligent time mode
	public static String INTEL_TIME_FROM = "INTELTIMEFROM";	//intelligent from time
	public static String INTEL_TIME_DEF_FROM = "23:00";		//intelligent time default from value	
	public static String INTEL_TIME_TO = "INTELTIMETO";		//intelligent to time
	public static String INTEL_TIME_DEF_TO = "7:00";		//intelligent time default to value
	public static String INTEL_TIME_MODE = "INTELTIMEMODE"; //intelligent time mode which change to
	public static String INTEL_TIME_DEF_MODE = "1";			//intelligent time mode default value
	
	public static String INTEL_MODE_POWER_ON = "INPOWERON";	//intelligent power mode
	public static String INTEL_POWER_LOW = "INTELPOWERLOW";	//intelligent power lowest value	
	public static int INTEL_POWER_DEF = 20;					//intelligent power default value
	public static String INTEL_POWER_MODE = "INTELPOWERMODE";//intelligent power mode which change to
	public static String INTEL_POWER_DEF_MODE = "2";		//intelligent power mode default value	
	
	public static String INTEL_TIME_BIT = "INTELTIMEBIT";	//intelligent time reset bit
	public static int INTEL_TIME_BIT_DEF = 0;				//intelligent time reset default bit
	public static String INTEL_POWER_BIT = "INTELPOWERBIT";	//intelligent power reset bit
	public static int INTEL_POWER_BIT_DEF = 0;				//intelligent power reset default bit
	
	public static String ALERT_TIME_CHECK = "ALERTTIMECHECK";//intelligent time alert choise
	public static boolean ALERT_TIME_CHECK_DEF = false;		//intelligent time alert default choise
	public static String ALERT_POWER_CHECK = "ALERTPOWERCHECK";//intelligent power alert choise
	public static boolean ALERT_POWER_CHECK_DEF = false;	//intelligent power alert choise
	
	public static String MAP_KEY_STORED = "MAPKEYSTORED";
	public static Map<String,String> MAP_KEY_STORED_DEF = null;
	public static String MAP_KEY_DATA = "MAPKEYDATA";
	public static String MAP_KEY_WLAN = "MAPKEYWLAN";
	public static String MAP_KEY_BLUETOOTH = "MAPKEYBLUETOOTH";
	public static String MAP_KEY_BRIGHT = "MAPKEYBRIGHT";
	public static String MAP_KEY_TIMEOUT = "MAPKEYTIMEOUT";
	public static String MAP_KEY_GPS = "MAPKEYGPS";
	public static String MAP_KEY_TOUCH = "MAPKEYTOUCH";
	public static String MAP_KEY_ARRAY[] = {MAP_KEY_DATA,MAP_KEY_WLAN,MAP_KEY_BLUETOOTH,MAP_KEY_BRIGHT,
		MAP_KEY_TIMEOUT,MAP_KEY_GPS,MAP_KEY_TOUCH};
	private static String MAP_KEY_SPLIDER = ":";
	
	public static String OPT_BUTTON_STATE = "OPTBUTTONSTATE";
	public static boolean OPT_BUTTON_STATE_DEF = true;
	
	Context mContext;
	
	private static PrefUtils _instance = null;
	
	private SharedPreferences sPreferences;
	private SharedPreferences.Editor editor;

	private PrefUtils(Context paramContext) {
		this.mContext = paramContext;

		this.sPreferences = paramContext.getSharedPreferences(PREFS_NAME,
				Context.MODE_WORLD_WRITEABLE);
		this.editor = this.sPreferences.edit();
	}

	public static PrefUtils getInstance(Context paramContext) {
		if (_instance == null)
			_instance = new PrefUtils(paramContext);
		return _instance;
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	private String getMyPreference(String key) {
		return sPreferences.getString(key, NO_VALUE);
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void setMyPreference(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * 
	 * @return integer value
	 *  
	 */
	public int getCurRunningMode(){
		String tempStr = getMyPreference(CUR_MODE);
		if(tempStr.equals(NO_VALUE))
			return 0;
		else
			return Integer.valueOf(tempStr);
	}
	
	/**
	 * 
	 * @param mMode
	 */
	public void setCurRunningMode(int mMode){
		setMyPreference(CUR_MODE,Integer.toString(mMode));
	}
	
	/**
	 * 
	 * @return integer value
	 *  
	 */
	public int getPreRunningMode(){
		String tempStr = getMyPreference(PRE_MODE);
		if(tempStr.equals(NO_VALUE))
			return 0;
		else
			return Integer.valueOf(tempStr);
	}
	
	/**
	 * 
	 * @param mMode
	 */
	public void setPreRunningMode(int mMode){
		setMyPreference(PRE_MODE,Integer.toString(mMode));
	}
	
	/**
	 * 	
	 * @param modeName
	 * COMMON_MODE_ON,
	 * INTEL_MODE_TIME_ON,
	 * INTEL_MODE_POWER_ON
	 * @return
	 */
	public boolean getModeOn(String modeName){
		String tempStr = getMyPreference(modeName);		
		if(tempStr.equals(NO_VALUE))
			return false;
		else
			return tempStr.equals("true");
			
	}
	
	/**
	 * 
	 * @param modeName
	 * @param mBool
	 */
	public void setModeOn(String modeName,boolean mBool){
		setMyPreference(modeName,Boolean.toString(mBool));
	}
	
	public String getIntelTimeFrom(){
		String tempStr = getMyPreference(INTEL_TIME_FROM);
		if(tempStr.equals(NO_VALUE))
			return INTEL_TIME_DEF_FROM;
		else
			return tempStr;
	}
	
	public int getIntelTimeMillisFromNow(String fromPref){
		//now 2:10 from 2:00
		String strArray[] = fromPref.split(":");
		int fromHour = Integer.parseInt(strArray[0]);
		int fromMinute = Integer.parseInt(strArray[1]);
		
		Date curDate = new Date();
		int curHours = curDate.getHours();
		int curMinutes = curDate.getMinutes();
		
		int curMillis = curHours * 3600 * 1000 + curMinutes * 60 * 1000;
		
		int fromMillis = 0;
		if((curHours*60+curMinutes)>(fromHour*60+fromMinute))
			fromMillis = (fromHour + 24) * 3600 * 1000 + fromMinute * 60 * 1000;						
		else
			fromMillis = fromHour * 3600 * 1000 + fromMinute * 60 * 1000;
		
		return (fromMillis - curMillis);
	}
	
	public void setIntelTimeFrom(String formatedTime){
		setMyPreference(INTEL_TIME_FROM,formatedTime);
	}

	public String getIntelTimeTo(){
		String tempStr = getMyPreference(INTEL_TIME_TO);
		if(tempStr.equals(NO_VALUE))
			return INTEL_TIME_DEF_TO;
		else
			return tempStr;
	}
	
	public void setIntelTimeTo(String formatedTime){
		setMyPreference(INTEL_TIME_TO,formatedTime);
	}
	
	public void setIntelTimeMode(int modeNum){
		setMyPreference(INTEL_TIME_MODE,String.valueOf(modeNum));
	}
	
	public int getIntelTimeMode() {
		String tempStr = getMyPreference(INTEL_TIME_MODE);
		if(tempStr.equals(NO_VALUE))
			return Integer.valueOf(INTEL_TIME_DEF_MODE);
		else
			return Integer.valueOf(tempStr);
	}
	
	public void setIntelPowerMode(int modeNum){
		setMyPreference(INTEL_POWER_MODE,String.valueOf(modeNum));
	}
	
	public int getIntelPowerMode() {
		String tempStr = getMyPreference(INTEL_POWER_MODE);
		if(tempStr.equals(NO_VALUE))
			return Integer.valueOf(INTEL_POWER_DEF_MODE);
		else
			return Integer.valueOf(tempStr);
	}	
	
	/**
	 * 
	 * @return the smart power mode benchmark value
	 * if the value isn't exist,return INTEL_POWER_DEF;//20
	 */
	public int getIntelLowPower(){
		String tempStr = getMyPreference(INTEL_POWER_LOW);
		if(tempStr.equals(NO_VALUE))
			return INTEL_POWER_DEF;
		else
			return Integer.parseInt(tempStr);
	}
	
	/**
	 * 
	 * @param percent
	 */
	public void setIntelLowPower(int percent){
		setMyPreference(INTEL_POWER_LOW,Integer.toString(percent));
	}
	
	public void setOldConsumeValue(int value){
		setMyPreference(OLD_CONSUME,Integer.toString(value));
	}
	
	public int getOldConsumeValue(){
		String tempStr = getMyPreference(OLD_CONSUME);
		if(tempStr.equals(NO_VALUE))
			return OLD_CONSUME_DEF;
		else
			return Integer.parseInt(tempStr);
	}
	
	public void setIntelTimeBit(int value){
		setMyPreference(INTEL_TIME_BIT,Integer.toString(value));
	}
	
	public int getIntelTimeBit(){
		String tempStr = getMyPreference(INTEL_TIME_BIT);
		if(tempStr.equals(NO_VALUE))
			return INTEL_TIME_BIT_DEF;
		else
			return Integer.parseInt(tempStr);
	}
	
	public void setIntelPowerBit(int value){
		setMyPreference(INTEL_POWER_BIT,Integer.toString(value));
	}
	
	public int getIntelPowerBit(){
		String tempStr = getMyPreference(INTEL_POWER_BIT);
		if(tempStr.equals(NO_VALUE))
			return INTEL_POWER_BIT_DEF;
		else
			return Integer.parseInt(tempStr);
	}
	
	public void setIntelTimeAlertCheck(boolean value){
		setMyPreference(ALERT_TIME_CHECK , Boolean.toString(value));
	}
	
	public boolean getIntelTimeAlertCheck(){
		String tempStr = getMyPreference(ALERT_TIME_CHECK);
		if(tempStr.equals(NO_VALUE))
			return ALERT_TIME_CHECK_DEF;
		else
			return Boolean.parseBoolean(tempStr);
	}
	
	public void setIntelPowerAlertCheck(boolean value){
		setMyPreference(ALERT_POWER_CHECK , Boolean.toString(value));
	}
	
	public boolean getIntelPowerAlertCheck(){
		String tempStr = getMyPreference(ALERT_POWER_CHECK);
		if(tempStr.equals(NO_VALUE))
			return ALERT_POWER_CHECK_DEF;
		else
			return Boolean.parseBoolean(tempStr);
	}
	
	public void setIntelTimePreDevStateList(Map<String,String> mMap){
		String mStr = "";
		for(int i=0;i<mMap.size();i++){
			mStr += mMap.get(MAP_KEY_ARRAY[i]).toString();
			if(i!=mMap.size()-1)
				mStr += MAP_KEY_SPLIDER;//not add splider for the end.
		}
		setMyPreference(MAP_KEY_STORED,mStr);
	}
	
	public Map<String,String> getIntelTimePreDevStateList(){
		
		Map<String,String> mMap = new HashMap<String,String>();
		
		String tempStr = getMyPreference(MAP_KEY_STORED);
		
		if(tempStr.equals(NO_VALUE)){
			mMap = MAP_KEY_STORED_DEF;
		}else{
			String tempArr[] = tempStr.split(MAP_KEY_SPLIDER);
			for(int j = 0;j<tempArr.length;j++){
				mMap.put(MAP_KEY_ARRAY[j], tempArr[j]);
			}			
		}
		return mMap;
	}
	
	public boolean getOptBtnNormalState(){
		String tempStr = getMyPreference(OPT_BUTTON_STATE);
		if(tempStr.equals(NO_VALUE))
			return OPT_BUTTON_STATE_DEF;
		else
			return Boolean.parseBoolean(tempStr);
	}
	
	public void setOptBtnNormalState(boolean value){
		setMyPreference(OPT_BUTTON_STATE ,Boolean.toString(value));
	}
}
