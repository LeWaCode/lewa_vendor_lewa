package com.lewatek.swapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.provider.Settings;

public class SwapperReceiver extends BroadcastReceiver {

    private static final String TAG = "SwapperReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        int howSwap = SwapperCommands.readHowSwap(context, settings);
        int swapSize = SwapperCommands.readSwapSize(context, settings, howSwap);

        Boolean autoRun = settings.getBoolean("autorun", false);

        // 0 is close ,1 is open
        boolean isSdcardSwapOpen = Settings.System.getInt(context.getContentResolver(),
                Settings.System.IS_SDCARDSWAP_OPEN, 0) == 1;
        
        boolean isRebootRun = Settings.System.getInt(context.getContentResolver(),
                Settings.System.IS_REBOOT_RUN, 1) == 1;

        if (SwapperCommands.DBUG) {
            Log.d(TAG, "isRebootRun" + isRebootRun);
            Log.d(TAG, intent.getAction());
        }

        SwapperCommands sc = new SwapperCommands(context);

        if (SwapperCommands.DBUG) {
            Log.i(TAG, "autorun:" + autoRun);
        }

        if (howSwap == SwapperCommands.INVALID_METHOD) {
            // return;
        }
        if (SwapperCommands.DBUG) {
            Log.i(TAG, "howSwap:" + howSwap);
        }
        if(howSwap == SwapperCommands.SDCARDFILE_CLOSE_METHOD || howSwap == SwapperCommands.PARTITION_CLOSE_METHOD){
            return;
        }

        if (intent.getAction().equals(Intent.ACTION_MEDIA_EJECT)) {
            if (SwapperCommands.DBUG) {
                Log.d(TAG, "Starting safe unmount");
                Log.d(TAG, "isSdcardSwapOpen:" + isSdcardSwapOpen);
            }
            
            Log.d(TAG, "android.os.Environment.getExternalStorageState():"+android.os.Environment.getExternalStorageState());
            if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_BAD_REMOVAL)){
                if (SwapperCommands.DBUG) {
                    Log.d(TAG, "MEDIA_BAD_REMOVAL");
                }
                sc.swapOff(howSwap, true);
            }else {
                Log.d(TAG, "isSdcardSwapOpen");
                if(isSdcardSwapOpen){
                    sc.swapOff(howSwap, true);
                }
            }
            
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {//boot mount or sdcard mount
            if (SwapperCommands.DBUG) {
                Log.d(TAG, "android.os.Environment.getExternalStorageState():"+android.os.Environment.getExternalStorageState());
                Log.d(TAG, "isSdcardSwapOpen:" + isSdcardSwapOpen);
                Log.d(TAG, "isRebootRun:" + isRebootRun);
            }
            
            if (!isSdcardSwapOpen && isRebootRun) {
                return;
            }
            if (SwapperCommands.DBUG) {
                Log.d(TAG, "return ACTION_MEDIA_MOUNTED");
            }

            (new SwapOnThread(sc, howSwap, swapSize)).start();
            
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (SwapperCommands.DBUG) {
                Log.i(TAG, "autoRun ACTION_BOOT_COMPLETED:" + autoRun);
                Log.i(TAG, "howswap ACTION_BOOT_COMPLETED:" + howSwap);
            }

            if (!autoRun) {
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.IS_SDCARDSWAP_OPEN, 0);
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.IS_REBOOT_RUN, 0);
                return;
            }else {
                 Settings.System.putInt(context.getContentResolver(),
                        Settings.System.IS_REBOOT_RUN, 1);
            }

            if (SwapperCommands.DBUG) {
                Log.d(TAG, "return ACTION_BOOT_COMPLETED");
            }
            
            if (howSwap == SwapperCommands.SYSTEMFILE_METHOD
                    || howSwap == SwapperCommands.PARTITION_METHOD) {
                sc.swapOn(true, howSwap, swapSize);
            } else if (howSwap == SwapperCommands.SDCARDFILE_METHOD) {
                Settings.System.putInt(context.getContentResolver(),
                        Settings.System.IS_SDCARDSWAP_OPEN, 1);
            }
            
        } else if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)) {
            if (!autoRun) {
                howSwap = SwapperCommands.INVALID_METHOD;
                Editor editor = settings.edit();
                editor.putString("swapSize", swapSize + "");
                editor.putString("howswap", howSwap + "");
                editor.commit();
                return;
            }
        }

    }

    class SwapOnThread extends Thread {
        SwapperCommands sc = null;
        int howSwap = 3;
        int swapSize = 32;

        public SwapOnThread(SwapperCommands _sc, int _howSwap, int _swapSize) {
            sc = _sc;
            howSwap = _howSwap;
            swapSize = _swapSize;
        }

        public void run() {
            try {
                sleep(5000); // magic number
                if (sc != null) {
                    sc.swapOn(false, howSwap, swapSize);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
