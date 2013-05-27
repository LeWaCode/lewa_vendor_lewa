/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.apk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 * @author chenliang
 */
public class ApkNotifyBroadcast extends BroadcastReceiver {

    public static final String ACTION_APK_NOTIFY_CANCEL = "com.lewa.filemanager.apk_notify_cancel";

    @Override
    public void onReceive(Context cntx, Intent intent) {
        if (intent.getAction().equals(ACTION_APK_NOTIFY_CANCEL)) {
            NotifyHelper.cancelApkNOtification();
        }

    }
}
