/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.util;

import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.ds.database.MimeSrc;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.app.filemanager.ui.CountActivity;

import com.lewa.app.filemanager.ui.PathActivity;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class StatusCheckUtil {

    public static boolean isSDAvailable;
    public static int mediaUpdating = 0;
    public static boolean mediaScanning;
    public static MediaScannerBroadcast broadcastRec;

    public static boolean isSDCardAvailable() {
        isSDAvailable = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
        return isSDAvailable;
    }

    public static void setCommonActivity(CommonActivity commonActivity) {
        if (StatusCheckUtil.broadcastRec != null) {
            StatusCheckUtil.broadcastRec.setCommonActivity(commonActivity);
        }
    }
    int type = -1;

    public static final class MediaScannerBroadcast extends BroadcastReceiver {

        boolean restart;
        File imagepath;
        Intent switchIntent;
        CommonActivity activity;
		public static String scanCat;

        public void setCommonActivity(CommonActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Logs.i("test", "intent.getAction() -- " + intent.getAction());
            if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                Logs.i("------------------ receive message 111");
				CountActivity.categoryActivity.setScanBarHide(true);
                MimeSrc.recountCategoryNum(context);
                if (CountActivity.categoryActivity != null) {
					if (CountActivity.categoryActivity.navTool.isAtTop()) {
						CountActivity.categoryActivity.homeViewHolder
								.dataChanged();
						CountActivity.categoryActivity.homeViewHolder.rebind();
					} else {
                    CountActivity.categoryActivity.viewHolder.refresh();
                }
				}
                if (mediaScanning) {
					if (StatusCheckUtil.MediaScannerBroadcast.scanCat != null) {
						Toast.makeText(
								context,
								context.getString(R.string.scan_finish_toast)
										+ scanCat, Toast.LENGTH_SHORT).show();
					}
                    mediaScanning = false;
					scanCat = null;
                    return;
                }
                StatusCheckUtil.mediaUpdating--;
                if (mediaUpdating == 0 && OperationUtil.operationRunnableOver) {
                    Message message = new Message();
                    message.what = Constants.OperationContants.FINISH_OPERATION;
                    message.obj = OperationUtil.operatingDialog;
                    activity.handler.sendMessage(message);
                } else if (mediaUpdating < 0) {
                    mediaUpdating = 0;
                }
				if (CountActivity.categoryActivity != null) {
					CountActivity.categoryActivity.viewHolder.refresh();
				}
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || intent.getAction().equals(Intent.ACTION_MEDIA_REMOVED)//各种未挂载状态
					|| intent.getAction().equals(
							Intent.ACTION_MEDIA_BAD_REMOVAL)
                    || intent.getAction().equals(Intent.ACTION_MEDIA_SHARED)
					|| intent.getAction().equals(
							Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)) {

				StatusCheckUtil.srcardStateResolve(true,
						CountActivity.categoryActivity);
				StatusCheckUtil.srcardStateResolve(true,
						PathActivity.activityInstance);
            } else if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
                CountActivity.categoryActivity.finished = true;
                CountActivity.categoryActivity.recovery();
                PathActivity.codePerformed = false;
                PathActivity.activityInstance.recovery();
            }
        }
    };
    static IntentFilter intentFilter;
    static Context ctx;

    public static IntentFilter getSDcardIntentListener() {
        if (intentFilter != null) {
            return intentFilter;
        }
        intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        return intentFilter;
    }

	public static void registerSDcardIntentListener(Context context,
			BroadcastReceiver broadcastRec) {
        context.registerReceiver(broadcastRec, getSDcardIntentListener());
        ctx = context;
    }

	public static void unregisterSDcardIntentListener(Context context,
			BroadcastReceiver broadcastRec) {
        ctx.unregisterReceiver(broadcastRec);
    }
    public static View no_file_layout;

    public static boolean srcardStateResolve() {
        return !StatusCheckUtil.isSDCardAvailable();
    }

    public static boolean srcardStateResolve(boolean initUI, Activity activity) {
        if (!StatusCheckUtil.isSDCardAvailable()) {
            if (initUI) {
				no_file_layout = LayoutInflater.from(activity).inflate(
						R.layout.no_file_layout, null);
				((TextView) no_file_layout.findViewById(R.id.nofile_text))
						.setText(R.string.sdcard_unavailable);
                no_file_layout.setVisibility(View.VISIBLE);
                activity.setTheme(android.R.style.Theme);
                activity.setContentView(no_file_layout);
            }
            return true;
        } else {
            no_file_layout = null;
            return false;
        }
    }
}
