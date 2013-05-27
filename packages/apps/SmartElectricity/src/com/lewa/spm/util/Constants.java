package com.lewa.spm.util;

public class Constants {

	//save file name
	public static final String SHARED_PREFERENCE_NAME="com.lewa.spm_preferences";
    
	public static final int SPM_MODE_OUT_ID = -1;//default mode
	public static final int SPM_MODE_LONG_ID = 0;//long idle mode
	public static final int SPM_MODE_ALARM_ID = 1;//interval mode
    //mode settings file
    public static final String  DEV_SETTINGS_STORE_OUT= "dev_settings_store_out";//defalt mode
	public static final String  DEV_SETTINGS_STORE_LONG = "dev_settings_store_long";//
	public static final String  DEV_SETTINGS_STORE_INTERVAL ="dev_settings_store_interval";//

    //mode device file
    public static final String  DEV_STATUS_STORE_OUT= "dev_status_store_out";//defalt mode
	public static final String  DEV_STATUS_STORE_LONG = "dev_status_store_long";//
	public static final String  DEV_STATUS_STORE_INTERVAL ="dev_status_store_interval";//

    /** none Power source  */
    public static final int BATTERY_PLUGGED_NONE = 0;
    /** Power source is an AC charger. */
    public static final int BATTERY_PLUGGED_AC = 1;
    /** Power source is a USB port. */
    public static final int BATTERY_PLUGGED_USB = 2;
    //keys of whether the battery been saved
    public static final String APPLICATION_INITED = "application_inited";

     //keys of whether intelligent mode is checked

    public static final String INTTELLIGENT_MODE_CHECKED = "inttelligent_mode_checked";

    public static final String LONG_MODE_SWITCH_TIME_DIFFERENCE = "long_s_t_d";


    

    
	
	public static final String SP_INTENT_MONTAGE_TIME = "com.lewa.spm.MontageTime";
	public static final String SPM_MONTAGE_TIME = "MontageTime";

	
	public static final String SPM_POWER_PLUGGED_TYPE = "power_plugged_type";
	public static final String SPM_POWER_STATE_DISCHARGING = "DISCHARGING";
	
	public static String INTEL_TIME_DEF_FROM = "23:00";		//intelligent time default from value	
	public static String INTEL_TIME_DEF_TO = "07:00";		//intelligent time default to value

	
	public static String INTEL_POWER_VALUE_SIGN = "%";		//the sign of the low power
	public static String INTEL_TIME_SIGN = ":";		//the sign of the low power
	public static String SPM_LIFE_DIFF_TXT_SHOW = "hint";		//the sign of the low power
	public static String MODE_OUT = "USER_OUT_MODE"; // long_stand_by_mode
	
	
	public static final String KEY_BEDTIME = "sleep_sp";
	public static final String KEY_BEDTIME_SETTIME = "set_time";
	public static final String KEY_BEDTIME_STARTTIME = "start_time";
	public static final String KEY_BEDTIME_ENDTIME = "end_time";
    public static final String KEY_BEDTIME_TIME_FRAME_MODE = "time_frame_mode";
	public static final String KEY_MODE_LONG_CHECK = "spm_mode_long";
	public static final String KEY_BATTERY_CALIBRATION = "spm_battery_calibration";
	public static final String KEY_SET_LONG_MODE_PARA = "set_spm_mode_standby";
	public static final String KEY_SET_ALARM_MODE_PARA = "set_spm_mode_alarm";
	
	public static final String STR_WIFI = "spm_dev_wifi";
	public static final String STR_GPS = "spm_dev_gps";
	public static final String STR_BLUETOOTH = "spm_dev_bluetooth";
	public static final String STR_LOCKSCREEN = "spm_dev_time_out";
	public static final String STR_BRIGHTNESS = "spm_dev_brghtiness";
	public static final String STR_BRIGHTNESS_VALUE = "spm_dev_brghtiness_value";
	public static final String STR_MOBILE = "spm_dev_data";
	public static final String STR_HAPTIC = "spm_dev_haptic";
	public static final String STR_AIRPLANE = "spm_dev_airplane";
	public static final String STR_SYNC = "spm_dev_auto_sync";

	public static final String STR_MODE_TYPE_NAME = "mode_type_name";
	public static final String STR_TIME_IS_IN_OR_NOT = "time_is_in_or_not";// if it is in sleepmode
	public static final String STR_TIME_BEFORE_MODE_TYPE_NAME = "before_time_mode_type_name";
	public static final String STR_SWITCH_BEFORE_MODE_TYPE_NAME = "before_switch_mode_type_name";
	public static final String STR_POWER_TYPE_NAME = "power_mode_type_name";
	public static final String STR_POWER_LEVEL = "level";
    public static final String STR_CHARGE_STATUS = "charge_status";
	public static final String STR_POWER_PLUGGED = "plugged";
	public static final String SPM_NOTIFICATION = "notificationt";
    public static final String SPM_ALMOST_FULL_START_TIME = "almost_full_start_time";
    public static final String SPM_TIME_SETTTING_SAVE = "spm_time_setting_save";
    public static final String SPM_EXCUTE_FINISH_NAME = "spm_excute_finish_name";
    public static final String SPM_EXCUTE_FINISH_ACTION = "spm_excute_finish_action";
    public static final String SPM_DEVS_SWITTCH_FINISH_ACTION = "spm_dev_switch_finish_action";

    public static final String SPM_INTENT_LONG_STATUS_EXTRA="sync_long_status";

    public static final String SPM_INTENT_ACTION_START_ALARM="com.lewa.spm_action_start_alarm";
    public static final String SPM_INTENT_ACTION_START_ALARM_EXTRA="start_alarm";
    public static final String SPM_INTENT_ACTION_END_ALARM="com.lewa.spm_action_end_alarm";
    public static final String SPM_INTENT_ACTION_END_ALARM_EXTRA="end_alarm"; 
    public static final String SPM_ENTRY_INTILLI_MODE_ON_TIME="enteryintilligentmodeontime";


    
	
	public static final String SPM_BATTERY_STATUS_AC_CHARGING_SP_NAME = "battery_ac_charging_info";// save battery is charging or not charging and target(first)
	public static final String SPM_BATTERY_STATUS_USB_CHARGING_SP_NAME = "battery_usb_charging_info";// save battery is charging or not charging and target(first)
	public static final String SPM_BATTERY_STATUS_AC_CHARGING_COUNT_SP_NAME = "battery_ac_charging_count_info";// save battery is charging or not charging and target(first)
	public static final String SPM_BATTERY_STATUS_USB_CHARGING_COUNT_SP_NAME = "battery_usb_charging_count_info";// save battery is charging or not charging and target(first)
	public static final String SPM_BATTERY_STATUS_TARGET = "status";// is to save battery status's name;
	public static final String SPM_DEV_CHANGED = "changed";

	
	//version info
	public static final String SPM_SHARED_PREFERENCE_VERSION_NAME = "version";
	public static final int SPM_SHARED_PREFERENCE_VERSION_NUM = 3;
	
	public static final int BEDTIME_STARTTIME = 0;
	public static final int BEDTIME_ENDTIME = 1;
	public static final int BEDTIME_TIME_FRAME_MODE = 2;
	public static final int BEDTIME_OUTSIDE_TIME_MODE = 3;
	public static final int LOW_POWER_SET_VALUE = 4;
	public static final int BELOW_LOW_POWER_MODE = 5;
	public static int INTEL_POWER_DEF = 20;					//intelligent power default value
	public static final int MINIMUM_BACKLIGHT = 30;
	public static final int MAXIMUM_BACKLIGHT = 225;
	public static final int STR_TIME_BEFORE_MODE_TYPE_ID = 1;
	public static final int STR_SWITCH_BEFORE_MODE_TYPE_ID = 0;
	public static final int STR_NOTIFICATION_SWITCH_OPEN_ID = 1;
	public static final int STR_NOTIFICATION_SWITCH_CLOSE_ID = 0;
	
    //userdefine activity 
    public static final String  USER_DEFINED_EXTRA_NAME ="name";
    public static final String  USER_DEFINED_EXTRA_POSITION ="position";






    /**
     * Wi-Fi is currently being disabled. The state will change to {@link #WIFI_STATE_DISABLED} if
     * it finishes successfully.
     * 
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_DISABLING = 0;
    /**
     * Wi-Fi is disabled.
     * 
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_DISABLED = 1;
    /**
     * Wi-Fi is currently being enabled. The state will change to {@link #WIFI_STATE_ENABLED} if
     * it finishes successfully.
     * 
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_ENABLING = 2;
    /**
     * Wi-Fi is enabled.
     * 
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_ENABLED = 3;
    /**
     * Wi-Fi is in an unknown state. This state will occur when an error happens while enabling
     * or disabling.
     * 
     * @see #WIFI_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_STATE_UNKNOWN = 4;




    /**
     * Indicates the local Bluetooth adapter is off.
     */
    public static final int STATE_OFF = 10;
    /**
     * Indicates the local Bluetooth adapter is turning on. However local
     * clients should wait for {@link #STATE_ON} before attempting to
     * use the adapter.
     */
    public static final int STATE_TURNING_ON = 11;
    /**
     * Indicates the local Bluetooth adapter is on, and ready for use.
     */
    public static final int STATE_ON = 12;
    /**
     * Indicates the local Bluetooth adapter is turning off. Local clients
     * should immediately attempt graceful disconnection of any remote links.
     */
    public static final int STATE_TURNING_OFF = 13;







    public static final int BREAK_CAUSE_NONE=-1;
    public static final int BREAK_CAUSE_FLYMODE=0;
    public static final int BREAK_CAUSE_WIFI=1;
    public static final int BREAK_CAUSE_GPS=2;
    public static final int BREAK_CAUSE_BLUETOOTH=3;
    public static final int BREAK_CAUSE_DATA=4;
    public static final int BREAK_CAUSE_HAPITIC=5;
    public static final int BREAK_CAUSE_AUTOSYNC=6;



    //ADDED BY luokairong s
    public static final String POWERSAVING_ACTION_NOTIFY_ON="powersaving_action_notify_on";
    public static final String POWERSAVING_DEV_TYPE="dev_type";
    
    public static final int DEV_AIRPLANE = 1;
    public static final int DEV_BLUETOOTH =2;
    public static final int DEV_GPS = 3;
    public static final int DEV_DATA = 4;
    public static final int DEV_SYNC = 5;
    public static final int DEV_WIFI = 6;

    //ADDED BY luokairong e


    
}

