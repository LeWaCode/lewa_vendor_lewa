package com.lewa.PIM.sim;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

public final class SimCard {
    public static final int SINGLE_MODE   = 1;
    public static final int MULTIPLE_MODE = 2;

    public static final int NORMAL_PLATFORM = 1;
    public static final int MTK_PLATFORM    = 2;
    public static final int QC_PLATFORM     = 3;

    public static final int GEMINI_SIM_1 = 0;
    public static final int GEMINI_SIM_2 = 1;

    private static final String MTK_FEATURE_CLASS_NAME = "com.mediatek.featureoption.FeatureOption"; //should according to MTK
    private static final String MTK_FEATURE_FIELD_NAME = "MTK_GEMINI_SUPPORT";

    private static int sPhoneMode = 0;
    private static int sPhonePlatform = 0;
    
    public static int getPhoneMode() {
        if (0 == sPhoneMode) {
            sPhoneMode = SINGLE_MODE;
            sPhonePlatform = NORMAL_PLATFORM;
            try {
                Class<?> classType = Class.forName(MTK_FEATURE_CLASS_NAME);
                sPhonePlatform = MTK_PLATFORM;
                try {
                    Field field = classType.getField(MTK_FEATURE_FIELD_NAME);
                    boolean supportGemini = field.getBoolean(null);
                    if (supportGemini) {
                        sPhoneMode = MULTIPLE_MODE;
                    }
                }
                catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
            Log.i("SimCard", "getPhoneMode: mode=" + sPhoneMode + " platform=" + sPhonePlatform);
        }
        
        return sPhoneMode;
    }

    public static int getPhonePlatform() {
        if (0 == sPhonePlatform) {
            sPhoneMode = SINGLE_MODE;
            sPhonePlatform = NORMAL_PLATFORM;
            try {
                Class<?> classType = Class.forName(MTK_FEATURE_CLASS_NAME);
                sPhonePlatform = MTK_PLATFORM;
                try {
                    Field field = classType.getField(MTK_FEATURE_FIELD_NAME);
                    boolean supportGemini = field.getBoolean(null);
                    if (supportGemini) {
                        sPhoneMode = MULTIPLE_MODE;
                    }
                }
                catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
            Log.i("SimCard", "getPhonePlatform: mode=" + sPhoneMode + " platform=" + sPhonePlatform);
        }
        
        return sPhonePlatform;
    }

    public static boolean hasIccCard(Context context, int simId) {
        if (0 == sPhoneMode) {
            getPhoneMode();
        }

        TelephonyManager telMgr = (TelephonyManager )context.getSystemService(Context.TELEPHONY_SERVICE);
        if (SINGLE_MODE == sPhoneMode) {
            return telMgr.hasIccCard();
        }
        else {
            boolean hasCard = false;
            Method m = null;
            try {
                m = TelephonyManager.class.getMethod("hasIccCardGemini", int.class);
                hasCard = ((Boolean ) m.invoke(telMgr, simId)).booleanValue();
            }
            catch (Exception e) {
                e.printStackTrace();
                //hasCard = telMgr.hasIccCard();
            }

            Log.i("SimCard", "hasIccCard: sim" + simId + " hasCard=" + hasCard);
            return hasCard;
        }
    }

    public static int getSimState(Context context, int simId) {
        if (0 == sPhoneMode) {
            getPhoneMode();
        }

        TelephonyManager telMgr = (TelephonyManager )context.getSystemService(Context.TELEPHONY_SERVICE);
        if (SINGLE_MODE == sPhoneMode) {
            return telMgr.getSimState();
        }
        else {
            int simState = TelephonyManager.SIM_STATE_UNKNOWN;
            Method m = null;
            try {
                m = TelephonyManager.class.getMethod("getSimStateGemini", int.class);
                simState = ((Integer )m.invoke(telMgr, simId)).intValue();
            }
            catch (Exception e) {
                e.printStackTrace();
                //simState = telMgr.getSimState();
            }

            Log.i("SimCard", "getSimState: sim" + simId + " simState=" + simState);
            return simState;
        }
    }
}