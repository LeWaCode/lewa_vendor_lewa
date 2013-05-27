/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.service;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import com.lewa.app.filemanager.ui.CountActivity;
import com.lewa.base.Logs;

/**
 *
 * @author chenliang
 */
public class ScanReceiver extends BroadcastReceiver {

    private Activity countActivity;
    public static final String ACTION_RECEIVER_SCAN = "com.lewa.app.filemanager.receiveScanSignal";
    public static final String ACTION_SCANNING = "com.lewa.app.filemanager.scanning";
    public static final String SCAN_FINISHED = "scanFinished";
    public static final String SCAN_FINISHED_TOAST = "scanFinishedToast";
    public static final String SCAN_TYPE = "scan_type";

    public ScanReceiver(Activity countActivity) {
        this.countActivity = countActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED) || intent.getAction().equals(ACTION_RECEIVER_SCAN)) {
            Logs.i("receiveScanSignal--------->");
            if (CountActivity.categoryActivity != null) {

                CountActivity.categoryActivity.refresh();
                if (intent.getBooleanExtra(SCAN_FINISHED, false)) {
                    Logs.i("receiveScanSignal---------> scan finished");
                    CountActivity.categoryActivity.setScanBarHide(true);
                    CountActivity.categoryActivity.hideScanBar(View.GONE);
                    String toast = intent.getStringExtra(SCAN_FINISHED_TOAST);
                    countActivity.stopService(new Intent(context, FileScanService.class));
                    Logs.i("", "toast =======================================> " + toast);
                    if (toast != null && !toast.trim().equals("")) {
                        if (CountActivity.categoryActivity.navTool.navEntity.size() > 1) {
                            Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        }
    }
}
