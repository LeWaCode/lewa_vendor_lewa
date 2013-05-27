package com.lewa.intercept.intents;

public class InterceptIntents {
    public static final String LEWA_SMS_RECEIVED_ACTION = "android.provider.Telephony.LEWA_SMS_RECEIVED";

    public static final String BLOCK_CLASSFY_ACTION = "com.lewa.intercept.action.BLOCK_CLASSFY";

    // added by chenhengheng,2011-12-09,add lewa's 360 receviced
    public static final String LEWA_INTERCEPT_INSERTBLACK2CACHE_ACTION
            = "android.provider.lewa.intercept.insertBlack2Cache";

    public static final String LEWA_INTERCEPT_INSERTWHITE2CACHE_ACTION
            = "android.provider.lewa.intercept.insertWhite2Cache";

    public static final String LEWA_INTERCEPT_UPATEBLACKINCACHE_ACTION
            = "android.provider.lewa.intercept.updateBlackInCache";
    
    public static final String LEWA_INTERCEPT_UPATEWHITEINCACHE_ACTION
            = "android.provider.lewa.intercept.updateWhiteInCache";

    public static final String LEWA_INTERCEPT_DELETEBLACKFROMCACHE_ACTION
            = "android.provider.lewa.intercept.deleteBlackfromCache";

    public static final String LEWA_INTERCEPT_DELETEALLBLACKFROMCACHE_ACTION
            = "android.provider.lewa.intercept.deleteAllBlackfromCache";

     public static final String LEWA_INTERCEPT_DELETEWHITEFROMCACHE_ACTION
            = "android.provider.lewa.intercept.deleteWhitefromCache";

    public static final String LEWA_INTERCEPT_DELETEALLWHITEFROMCACHE_ACTION
            = "android.provider.lewa.intercept.deleteAllWhitefromCache";

    private static final String LEWA_INTERCEPT_UPDATE_SMART_LIB_RIGHTNOW
            = "android.provider.lewa.intercept.updateSmartLibRightNow";

    private static final String LEWA_INTERCEPT_UPDATE_SMART_LIB
            = "android.provider.lewa.intercept.updateSmartLib";

    public static final String LEWA_INTERCEPT_NOTIFICATION_ACTION
            = "android.provider.lewa.intercept.notification";

    public static final String LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION
            = "android.provider.lewa.intercept.notification.classfy";

    public static final String LEWA_INTERCEPT_SAVE_MODE_ACTION 
            = "android.provider.lewa.intercept.SAVE_MODE";    

}
