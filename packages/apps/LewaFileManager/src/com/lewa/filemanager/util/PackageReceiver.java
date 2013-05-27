/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.util;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lewa.filemanager.funcgroup.AppSrc;
import com.lewa.app.filemanager.ui.CountActivity;

/**
 *
 * @author chenliang
 */
public class PackageReceiver extends BroadcastReceiver {

    private Activity countActivity;

    public PackageReceiver(Activity countActivity) {
        this.countActivity = countActivity;
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
            AppSrc.initInstalledApps(ctx);
            if (CountActivity.categoryActivity != null) {
                CountActivity.categoryActivity.viewHolder.refresh();
            }
        }
    }
}
