/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.service;

import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.Process;
import android.view.View;
import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.ds.uri.NavigationConstants;
import com.lewa.filemanager.ds.sdcard.TypeFilter;
import com.lewa.app.filemanager.ui.CountActivity;
import com.lewa.base.Logs;
import com.lewa.filemanager.config.Constants;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class ScanHelper {

    static Thread scanThread;
    static boolean isRunning;
    static String runningScanCategory;

    public static void scan(final String scanCategory, final Context context) {
        String finishText;
        if (isRunning) {
            Toast.makeText(context, context.getString(R.string.scaning, runningScanCategory), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            runningScanCategory = CountActivity.categoryActivity.navTool.navEntity.peek().displayname;
        } catch (Exception e) {
            return;
        }
        String home = context.getString(R.string.home);
        CountActivity.categoryActivity.setScanBarHide(false);
        CountActivity.categoryActivity.hideScanBar(View.VISIBLE);
        if (!runningScanCategory.equals(home)) {
            Toast.makeText(context, context.getString(R.string.scaning_toast, runningScanCategory), Toast.LENGTH_SHORT).show();
        }
        Logs.i("-------------->>" + runningScanCategory);
        if (runningScanCategory.equals(home)) {
            finishText = context.getString(R.string.scan_finish_all_toast);
        } else {
            finishText = context.getString(R.string.scan_finish_toast) + runningScanCategory;
            Logs.i("-------------->> finishText" + finishText);
        }
        final String finalFinishText = finishText;
        Logs.i("-------------->> finalFinishText" + finalFinishText);
        scanThread = new HandlerThread("", Process.THREAD_PRIORITY_BACKGROUND) {

            @Override
            public void run() {
                scanCategory(scanCategory, context, finalFinishText);
                isRunning = false;
            }
        };
        scanThread.start();
        isRunning = true;
    }

    public static void scanCategory(String scanCategory, Context context, String toastFinish) {
        DataSychroInMemory dataSychroInMemory = new DataSychroInMemory();
        CountFilter allFilter = new CountFilter(dataSychroInMemory, context, new Integer[]{TypeFilter.FILTER_BOTH_DIR_FILE, Config.getHiddenOption(Config.ACCOUNT_HIDE_OPTION)}, Constants.HIDDEN_EXCLUDED, null, scanCategory);
        File file = new File(NavigationConstants.SDCARD_PATH);
        file.listFiles(allFilter);
        dataSychroInMemory.clearNoUseData(context);
        Intent intent = new Intent(ScanReceiver.ACTION_RECEIVER_SCAN);
        Logs.i("", "-------------->>>" + scanCategory);
        if (scanCategory != null && !scanCategory.trim().equals("")) {
            intent.putExtra(ScanReceiver.SCAN_FINISHED_TOAST, toastFinish);
//            Toast.makeText(context, toastFinish, Toast.LENGTH_LONG).show();
        }
        intent.putExtra(ScanReceiver.SCAN_FINISHED, true);
        context.sendBroadcast(intent);
    }
}
