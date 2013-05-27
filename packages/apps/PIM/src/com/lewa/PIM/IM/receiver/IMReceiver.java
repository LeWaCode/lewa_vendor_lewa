package com.lewa.PIM.IM.receiver;

import java.io.IOException;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;

/**
 * Handle incoming SMSes.  Just dispatches the work off to a Service.
 */
public abstract class IMReceiver extends BroadcastReceiver {
    static final Object mStartingServiceSync = new Object();
    static PowerManager.WakeLock mStartingService;


    @Override
    public void onReceive(Context context, Intent intent) { 
	    onReceiveWithPrivilege(context, intent, false);
    }

    
    protected abstract void onReceiveWithPrivilege(Context context, Intent intent, boolean privileged);
    /**
     * Start the service to process the current event notifications, acquiring
     * the wake lock before returning to ensure that the service will run.
     */
    public static void beginStartingService(Context context, Intent intent) {
        synchronized (mStartingServiceSync) {
            if (mStartingService == null) {
                PowerManager pm =
                    (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                mStartingService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "StartingAlertService");
                mStartingService.setReferenceCounted(false);
            }
            mStartingService.acquire();
            //Log.d("IMReceiver","beginStartingService");
            context.startService(intent);
        }
    }

    /**
     * Called back by the service when it has finished processing notifications,
     * releasing the wake lock if the service is now stopping.
     */
    public static void finishStartingService(Service service, int startId) {
        synchronized (mStartingServiceSync) {
            if (mStartingService != null) {
                if (service.stopSelfResult(startId)) {
                	//Log.d("IMReceiver","finishStartingService");
                    mStartingService.release();
                }
            }
        }
    }
}
