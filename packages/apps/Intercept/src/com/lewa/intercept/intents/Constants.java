package com.lewa.intercept.intents;

public class Constants {

    public static final boolean DBUG = false;

    public static final String TAG = "InterceptApp";

    public static final String SHARE_PREFERENCE_NAME = "com.lewa.intercept_preferences";
    // block rule choice value (checked KEY)
    public static final int BLOCK_MODE_BLACKLIST = 1;
    public static final int BLOCK_MODE_ALLNUM = 2;
    public static final int BLOCK_MODE_OUT_OF_WHITELIST = 3;
    public static final int BLOCK_MODE_EXCEPT_CONTACT = 4;
    public static final int BLOCK_MODE_SMART = 5;

    public static final int BLOCK_TYPE_NUMBER_CALL = 1;
    public static final int BLOCK_TYPE_NUMBER_MSG = 2;
    public static final int BLOCK_TYPE_NUMBER_DEFAULT = 3;

    public static final int DEFAULT_BLOCK_PRIVACY = 0;

    public static final int BLOCK_TYPE_DEFAULT = 0;
    public static final int BLOCK_TYPE_BLACK = 1;
    public static final int BLOCK_TYPE_WHITE = 2;

    public static final int BLOCK_SWITCH_ON_INT = 0;
    public static final int BLOCK_SWITCH_OFF_INT = 1;

    public static final int BLOCK_INTERCEPT_ON_INT = 0;
    public static final int BLOCK_INTERCEPT_OFF_INT = 1;

    public static final boolean BLOCK_SWITCH_ON_BOOLEAN = true;
    public static final boolean BLOCK_SWITCH_OFF_BOOLEAN = false;

    public static final boolean BLOCK_INTERCEPT_ON_BOOLEAN = true;
    public static final boolean BLOCK_INTERCEPT_OFF_BOOLEAN = false;

    public static final String STARTTIME = "07:00";
    public static final String ENDTIME = "23:00";

    public static final String KEY_INTERCEPT_NOTIFY = "isInterceptNotify";
    public static final String KEY_INTERCEPT = "isIntercept";
    public static final String KEY_SWITCH = "isSwitch";
    public static final String KEY_ONERING = "oneringIntercept";
    // public static final String KEY_INTERUPTSET = "isInterupt";

    public static final String KEY_BLOCK_MODE = "blockMode";

    public static final String KEY_START_TIME = "startTime";
    public static final String KEY_END_TIME = "endTime";

    public static final String KEY_START_HOUR = "startHour";
    public static final String KEY_START_MINUTE = "startMunite";
    public static final String KEY_END_HOUR = "endHour";
    public static final String KEY_END_MINUTE = "endMunite";

    public static final String KEY_BLOCK_IS_ALL_DAY = "isAllDay";

    public static final String KEY_BLOCK_SET_TIME = "timeSetting";
    
    public static int SIMCARD_INFO = 0;
    public static final int GEMINI_SIM_1 = 0;
    public static final int GEMINI_SIM_2 = 1;
    
    public static final int INTERCEPT_SMS  = 0;

    public static final int INTERCEPT_CALL = 1;
}
