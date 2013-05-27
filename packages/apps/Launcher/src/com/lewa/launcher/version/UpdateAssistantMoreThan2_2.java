package com.lewa.launcher.version;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.lewa.launcher.R;
import com.lewa.launcher.version.DialogUtil.DialogAbstract;

public class UpdateAssistantMoreThan2_2 {
    public static Activity activity = null;
    public final static int DOWNLOAD_FINISH_FLAG = 1;
    public final static int ALREADY_UP_TO_DATE_FLAG = 2;
    public static long lewaSetupId = -1L;
    public static Thread updateThread;

    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            final long id = lewaSetupId;
            switch (msg.what) {
            case DOWNLOAD_FINISH_FLAG:
                DialogAbstract abs = new DialogUtil.DialogAbstract();
                abs.activity = activity;
				abs.title = activity.getString(R.string.word_sureCancelTheDownloading);
				abs.iconId = R.drawable.lewahome;
				abs.positiveButtonText = activity.getString(R.string.cancel);
				abs.negativeButtonText = activity.getString(R.string.word_continue);
                abs.positiveButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DownloadManager dm = (DownloadManager) UpdateAssistantMoreThan2_2.activity
                                .getSystemService(Context.DOWNLOAD_SERVICE);
                        dm.remove(id);
                    }

                };
                abs.negativeButtonClickListener = DialogUtil
                        .getNewCancelOption(activity);
                DialogUtil.showChoiceDialog(abs);
            }
        }

    };

    public UpdateAssistantMoreThan2_2(Activity activity) {
        UpdateAssistantMoreThan2_2.activity = activity;
    }

    public void Entrance_Check() {
        if (isNetworkAvailable(UpdateAssistantMoreThan2_2.activity) == false) {
            new AlertDialog.Builder(UpdateAssistantMoreThan2_2.activity)
                    .setTitle(activity.getString(R.string.no_available_network))
                    .setIcon(R.drawable.lewahome)
                    .setNeutralButton(activity.getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            }).show();
            return;
        }
        try {
            hint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean seeHasRunningTask() {
        if (UpdateAssistantMoreThan2_2.lewaSetupId != -1l) {
            return true;
        }
        return false;
    }

    private static boolean isNetworkAvailable(Context ctx) {
        try {
            ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            return (info != null && info.isConnected());
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    public void update(Activity activity) throws NameNotFoundException {

        final String appUrl = VersionUtil.getUrl();
        VersionUpdate.notificationManager.cancel(VersionUpdate.Flag_AutoUpdate);

        if (appUrl == null)
            return;

        try {
            downloadTheFile(appUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void hint() throws Exception {
        boolean bResult = VersionUtil.checkNew(activity);
        if (bResult) {

            @SuppressWarnings("unused")
            AlertDialog alert = new AlertDialog.Builder(
                    UpdateAssistantMoreThan2_2.activity)
                    .setTitle("New Version Found !")
                    .setMessage(
                            "Update " + VersionUtil.getLocalVersion() + " to "
                                    + VersionUtil.getRemoteVersion() + "?")
                    .setIcon(R.drawable.lewahome)
                    .setPositiveButton("Update",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    try {
                                        if (seeHasRunningTask()) {
                                            return;
                                        }
                                        update(activity);
                                    } catch (NameNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                    .setNegativeButton("Later",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                }
                            }).show();
        } else {

            new AlertDialog.Builder(UpdateAssistantMoreThan2_2.activity)
                    .setTitle("already up-to-date")
                    .setIcon(R.drawable.lewahome)
                    .setNeutralButton("OK",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }

                            }).show();

        }

    }

    private void downloadTheFile(String strPath) throws Exception {
        DownloadManager dm = (DownloadManager) activity
                .getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(strPath));
        request.setDestinationInExternalFilesDir(activity,
                Environment.DIRECTORY_DOWNLOADS, VersionUtil.getApkName());
        request.setMimeType("application/vnd.android.package-archive");
        request.setShowRunningNotification(true);
        request.setTitle(activity.getString(R.string.application_name));
        request.setVisibleInDownloadsUi(true);
        lewaSetupId = dm.enqueue(request);
    }
}
