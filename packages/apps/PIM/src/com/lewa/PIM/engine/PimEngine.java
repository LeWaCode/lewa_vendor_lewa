package com.lewa.PIM.engine;

//import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.SystemProperties;
import android.text.TextUtils;

import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.calllog.data.MissedCallLogInfo;
import com.lewa.PIM.dialpad.data.DialpadItem;
import com.lewa.PIM.util.CommonMethod;

public abstract class PimEngine {
    private static PimEngine sInstance;
    
    private static String[] sEmergencyNumbers;
    private static boolean  sEmergencyNumbersRequired = false;

    public static enum DataEvent {
        LOAD_CONTACTS,
        LOAD_CALLLOGS,
        LOAD_DIALPADS,
        LOAD_MISSED_LOGS,
        CONTACTS_CHANGED,
        ROSTERDATA_CHANGED
    }

    public static PimEngine getInstance(Context context) {
        if (null == sInstance) {
            String classPackage = "com.lewa.PIM.engine.PimEngineBase";
            try {
                CommonMethod.initSpecialPhoneNameMap(context);
                sInstance = (PimEngine )Class.forName(classPackage).asSubclass(PimEngine.class).newInstance();
                Context appContext = context.getApplicationContext();
                sInstance.setContext(appContext);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        return sInstance;
    }

    public static String[] getEmergencyNumbers() {
        if (!sEmergencyNumbersRequired) {
            sEmergencyNumbersRequired = true;
            String numbers = SystemProperties.get("ril.ecclist");
            if (TextUtils.isEmpty(numbers)) {
                numbers = SystemProperties.get("ro.ril.ecclist");
            }

            if (!TextUtils.isEmpty(numbers)) {
                if (!numbers.contains("110")) {
                    numbers.concat(",110");
                }
                if (!numbers.contains("120")) {
                    numbers.concat(",120");
                }
            }
            
            if (!TextUtils.isEmpty(numbers)) {
                sEmergencyNumbers = numbers.split(",");
            }
        }

        return sEmergencyNumbers;
    }

    public abstract void setContext(Context context);
    public abstract void addDataListenner(PimEngine.DataEventListener listener);
    public abstract void removeDataListenner(PimEngine.DataEventListener listener);
    public abstract void deleteCallLog(long logId);
    public abstract void deleteCallLogs(long[] logIds);
    public abstract void deleteCallLogs(ArrayList<Long> arrayLogIds);
    public abstract void setCallLogsRead();
    public abstract boolean isLoadCallLogsInProgress();

    public abstract List<CallLogGroup> loadCallLogs(boolean load);
    public abstract List<DialpadItem> loadDialpadItems(boolean load);
    public abstract void reloadCallLogsAndContacts();
    public abstract HashMap<Long, MissedCallLogInfo> loadMissedCallLogInfo(boolean forceQuery);

    public abstract void notifyDataEvent(PimEngine.DataEvent event, int state);

    public static abstract interface DataEventListener {
        public static final int LOAD_DATA_DONE = 2;
        public abstract void onDataEvent(PimEngine.DataEvent event, int state);
    }
}
