package com.lewa.launcher.version;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.webkit.URLUtil;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.lewa.launcher.ApplicationsAdapter;
import com.lewa.launcher.R;
import com.lewa.launcher.version.DialogUtil.DialogAbstract;

public class VersionUpdate {
    public static String configPath = "/mnt/sdcard" + "/Download" + "/";
    public static final String remSharedPrefenceNanme = "settingRem.txt";
    private static final String TAG = "AutoUpdate";
    private final static int DOWNLOAD_FINISH_FLAG = 1;
    public static NotificationManager notificationManager;
    public final static int Flag_Download = 0;
    public final static int Flag_AutoUpdate = 1;
    private static final String STATUS_SDCARD = "STATUS_SDCARD";
    private static final String STATUS_NETWORK = "STATUS_NETWORK";
    private static final String STATUS_AVAILABLE_SPACE = "STATUS_AVAILABLE_SPACE";
    public Activity activity = null;
    public static String currentTempFilePath;
    private String fileExtendedName = "";
    private String fileName = "";
    private Intent startIntent;
    private ProgressDialog dialog;
    private Notification notification = new Notification();

    public static boolean lewaSetupRunning;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case DOWNLOAD_FINISH_FLAG:
                if (dialog != null) {
                    dialog.dismiss();
                    dialog.cancel();
                }
                startInstall(new File(currentTempFilePath));
                putRemInfomation();
                lewaSetupRunning = false;
            }
        }

    };
    private final String mVerLib = "VerLib";
    private final String CURR_VER_KEY = "ver";

    public static void putRemInfomation() {
        try {
            File dir = new File(configPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir.getAbsoluteFile() + "/"
                    + remSharedPrefenceNanme);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);
            os.write(VersionUpdate.currentTempFilePath.getBytes());
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Boolean> status_updatePreparation = new HashMap<String, Boolean>();
    private long downloadFileSizeInM = 8;

    private boolean check() {
        boolean sdcardMounte;
        boolean status_network = recordStatusPreparation_Update(
                VersionUpdate.STATUS_NETWORK,
                // false
                this.checkNetworkAvailable(false));
        boolean status_sdcard = recordStatusPreparation_Update(
                VersionUpdate.STATUS_SDCARD,
                (sdcardMounte = android.os.Environment
                        .getExternalStorageState().equals(
                                android.os.Environment.MEDIA_MOUNTED)));
        boolean status_freeSize = recordStatusPreparation_Update(
                VersionUpdate.STATUS_AVAILABLE_SPACE, sdcardMounte
                        && SDCardFreeSize() > downloadFileSizeInM);
        return status_network && status_sdcard && status_freeSize;
    }

    public long SDCardFreeSize() {
        String sDcString = android.os.Environment.getExternalStorageState();
        if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
            File pathFile = android.os.Environment
                    .getExternalStorageDirectory();
            android.os.StatFs statfs = new android.os.StatFs(pathFile.getPath());
            long nBlocSize = statfs.getBlockSize();
            long nAvailaBlock = statfs.getAvailableBlocks();
            long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;
            return nSDFreeSize;
        }
        return 0L;
    }

    public boolean recordStatusPreparation_Update(String statusKeyInTheMap,
            boolean boolean_condition_preupdate_ok) {
        boolean status_cond = false;
        if (boolean_condition_preupdate_ok) {
            status_cond = true;
        }
        status_updatePreparation.put(statusKeyInTheMap, status_cond);
        return status_cond;
    }

    public void hintIfUpdate() {

        Boolean bResult = false;

        if (!check()) {
            popStatusDialog(activity.getString(R.string.prepareUpdate));

            return;
        }
        try {
            bResult = VersionUtil.checkNew(activity);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        if (bResult == null) {
            alertPreservingServer();
            return;
        }
        if (bResult) {
            String note = VersionUtil.getNote();
            DialogAbstract abs = new DialogUtil.DialogAbstract();
            abs.activity = activity;
            abs.title = activity.getString(R.string.newVersionFound) + " : "
                    + VersionUtil.getRemoteVersion();
            abs.iconId = R.drawable.lewahome;
            abs.message = note;
            abs.positiveButtonText = activity.getString(R.string.now_download);
            abs.negativeButtonText = activity.getString(R.string.word_later);
            abs.positiveButtonClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        if (lewaSetupRunning) {
                            return;
                        }
                        updateCore();
                    } catch (NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            };
            abs.negativeButtonClickListener = DialogUtil
                    .getNewCancelOption(activity);
            DialogUtil.showChoiceDialog(abs);
        } else {

            new AlertDialog.Builder(this.activity)
                    .setTitle(activity.getString(R.string.word_alreadyNew))
                    .setIcon(R.drawable.lewahome)
                    .setNeutralButton(activity.getString(R.string.ok),
                            DialogUtil.getNewCancelOption(activity)).show();

        }

    }

    public void popStatusDialog(String title) {
        DialogAbstract abs = new DialogUtil.DialogAbstract();
        abs.activity = activity;
        abs.title = title;
        abs.iconId = R.drawable.lewahome;
        TableLayout tableLayout = new TableLayout(activity);

        configureRow(tableLayout, R.string.sdcard, VersionUpdate.STATUS_SDCARD);
        configureRow(tableLayout, R.string.net, VersionUpdate.STATUS_NETWORK);
        configureRow(tableLayout, R.string.freespace,
                VersionUpdate.STATUS_AVAILABLE_SPACE);
        abs.view = tableLayout;
        abs.neutralButtonText = activity.getString(R.string.ok);
        abs.neutralButtonClickListener = DialogUtil.getNewCancelOption(
                activity, false);
        DialogUtil.showNeutralDialog(abs);
    }

    public void configureRow(TableLayout tableLayout, int nameid,
            String statusChars) {
        boolean bStatus = status_updatePreparation.get(statusChars);
        TableRow tablerow = new TableRow(activity);
        TextView tv_name = new TextView(activity);
        tv_name.setText("       " + activity.getString(nameid));
        tablerow.addView(tv_name);

        TextView tv = new TextView(activity);
        tv.setText("              ");
        tablerow.addView(tv);

        TextView tv_status = new TextView(activity);
        tv_status.setText(bStatus ? activity.getString(R.string.passed)
                : activity.getString(R.string.unavailable));
        tv_status.setTextColor(bStatus ? Color.GREEN : Color.RED);
        tablerow.addView(tv_status);
        tv_name.setWidth(130);
        tv_name.setGravity(Gravity.RIGHT);
        tv_status.setPadding(10, 0, 0, 0);

        tableLayout.addView(tablerow);
    }

    public void updateCore() throws NameNotFoundException {
        String osVn = android.os.Build.VERSION.SDK;
        if (osVn.startsWith("8") || osVn.startsWith("7")) {
            try {
                update(activity);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        } else if (osVn.startsWith("10")) {
            try {
                new UpdateAssistantMoreThan2_2(activity).update(activity);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    public void alertPreservingServer() {
        DialogAbstract abs = new DialogUtil.DialogAbstract();
        abs.activity = activity;
        abs.iconId = R.drawable.lewahome;
        abs.message = activity.getString(R.string.word_server_preserving);
        abs.neutralButtonText = activity.getString(R.string.ok);
        abs.neutralButtonClickListener = DialogUtil
                .getNewCancelOption(activity);
        DialogUtil.showNeutralDialog(abs);
        return;
    }

    public void alert_insert_sdcard_needed() {
        DialogAbstract abs = new DialogUtil.DialogAbstract();
        abs.activity = activity;
        abs.iconId = R.drawable.lewahome;
        abs.message = activity.getString(R.string.insert_sdcard_needed);
        abs.neutralButtonText = activity.getString(R.string.ok);
        abs.neutralButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
                if (!activity
                        .getClass()
                        .getName()
                        .equals(ApplicationsAdapter.mLauncher.getClass()
                                .getName())) {
                    activity.finish();
                }
            }

        };
        DialogUtil.showNeutralDialog(abs);
        return;
    }

    public VersionUpdate(Activity activity) {
        this.activity = activity;

        notificationManager = (NotificationManager) activity
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void maunalUpdate() {
        hintIfUpdate();
    }

    public boolean checkNetworkAvailable(boolean hint) {
        if (!isNetworkAvailable(this.activity)) {
            if (hint) {

                new AlertDialog.Builder(this.activity)
                        .setTitle(
                                activity.getString(R.string.lewa_weather_no_network_message))
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
            }
            return false;
        }
        return true;
    }

    public void autoUpdate() {
        if (!checkNetworkAvailable(false)) {
            return;
        }
        hintOKUpdate();
    }

    private void hintOKUpdate() {
        boolean bResult = false;
        try {
            bResult = VersionUtil.checkNew(activity);
        } catch (Exception e) {
            if (VersionUtil.INFO_SERVER_CLOSED.equals(e.getMessage())) {
                return;
            }
            e.printStackTrace();
        }
        if (bResult) {
            Intent intent = new Intent(activity, UpdateActivity.class);
            intent.setClassName(activity,
                    "com.lewa.launcher.version.UpdateActivity");
            intent.setData(Uri.parse(VersionUtil.getUrl()));

            upToNotification(Flag_AutoUpdate, R.drawable.lewahome,
                    R.string.newVersionavAvailable, R.string.lewaDesktop,
                    R.string.newVersionFound, intent, activity);

        }
    }

    public void askStartNow() {
        DialogAbstract abs = new DialogUtil.DialogAbstract();
        abs.activity = activity;
        abs.iconId = R.drawable.lewahome;
        abs.message = activity.getString(R.string.word_now_update) + "?";
        abs.positiveButtonText = activity.getString(R.string.ok);
        abs.negativeButtonText = activity.getString(R.string.word_later);
        abs.positiveButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeSure();

            }
        };
        abs.negativeButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                dialog.cancel();
                activity.finish();
                VersionUpdate.notificationManager
                        .cancel(VersionUpdate.Flag_AutoUpdate);
            }
        };
        DialogUtil.showChoiceDialog(abs);

    }

    public void makeSure() {
        DialogAbstract abs = new DialogUtil.DialogAbstract();
        abs.activity = activity;
        abs.title = activity.getString(R.string.word_update);
        abs.iconId = R.drawable.lewahome;
        abs.message = activity.getString(R.string.word_update)
                + VersionUtil.getLocalVersion()
                + activity.getString(R.string.word_to)
                + VersionUtil.getRemoteVersion();
        abs.neutralButtonText = activity.getString(R.string.ok);

        abs.neutralButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    updateCore();
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        };
        DialogUtil.showNeutralDialog(abs);
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

    public void update(final Activity activity) throws NameNotFoundException {
        final String appUrl = VersionUtil.getUrl();

        Runnable run = new Runnable() {

            @Override
            public void run() {
                Looper.prepare();
                if (appUrl == null) {
                    return;
                }
                lewaSetupRunning = true;
                downloadTheFile(appUrl);
                notifyHandler();
                Looper.loop();
            }

            public void notifyHandler() {
                Message message = new Message();
                message.what = DOWNLOAD_FINISH_FLAG;
                handler.sendMessage(message);
            }
        };
        new Thread(run).start();
        showWaitDialog();
    }

    public void upToNotification(int notifyid, int iconid, int tickerTextId,
            int titleid, int downWordsid, Intent intent, Activity activity) {
        notification.icon = iconid;
        notification.tickerText = activity.getString(tickerTextId);
        notification.setLatestEventInfo(activity.getApplicationContext(),
                activity.getString(titleid), activity.getString(downWordsid),
                PendingIntent.getActivity(activity, 0, intent, 0));
        notificationManager.notify(notifyid, notification);
    }

    public void showWaitDialog() {
        dialog = new ProgressDialog(activity);
        dialog.setMessage(activity.getString(R.string.waiting_for_update));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
    }

    private void downloadTheFile(final String strPath) {
        if (strPath == null || strPath.equals("")) {
            throw new IllegalStateException("getting url is empty");
        }
        fileExtendedName = strPath.substring(strPath.lastIndexOf(".") + 1,
                strPath.length()).toLowerCase();
        fileName = strPath.substring(strPath.lastIndexOf("/") + 1,
                strPath.lastIndexOf("."));

        try {
            doDownloadTheFile(strPath);
            VersionUpdate.notificationManager
                    .cancel(VersionUpdate.Flag_AutoUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doDownloadTheFile(String strPath) throws Exception {
        File dir = new File(configPath);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                configPath = "/sdcard/Download/";
                dir = new File(configPath);
            }
        }
        if (URLUtil.isNetworkUrl(strPath)) {
            try {
                URL myURL = new URL(strPath.replace("\n", ""));
                URLConnection conn = myURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                if (is == null) {
                    throw new RuntimeException("stream is null");
                }
                File myTempFile = new File(configPath + fileName + "."
                        + fileExtendedName.replace("\n", ""));
                currentTempFilePath = myTempFile.getAbsolutePath();
                if (myTempFile.exists()) {
                    return;
                }
                myTempFile.createNewFile();
                final int buffSize = 128;
                FileOutputStream fos = new FileOutputStream(myTempFile);
                byte buf[] = new byte[buffSize];
                do {
                    int numread = is.read(buf);
                    if (numread <= 0) {
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (true);
                is.close();
            } catch (Exception ex) {
                Log.e(TAG, "getDataSource() error: " + ex.getMessage(), ex);
            }
        }
    }

    private void startInstall(File f) {
        startIntent = new Intent();
        startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startIntent.setAction(android.content.Intent.ACTION_VIEW);
        String type = getMIMEType(f);
        startIntent.setDataAndType(Uri.fromFile(f), type);
        activity.startActivity(startIntent);

    }

    private String getMIMEType(File f) {
        String type = "";
        String fName = f.getName();
        String end = fName
                .substring(fName.lastIndexOf(".") + 1, fName.length())
                .toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
                || end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
            type = "audio";
        } else if (end.equals("3gp") || end.equals("mp4")) {
            type = "video";
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            type = "image";
        } else if (end.equals("apk")) {
            type = "application/vnd.android.package-archive";
        } else {
            type = "*";
        }
        if (end.equals("apk")) {
        } else {
            type += "/*";
        }
        return type;
    }

    public static void removeLeavingInstallingApk() {
        File removeTxt = new File(VersionUpdate.configPath
                + VersionUpdate.remSharedPrefenceNanme);
        if (!removeTxt.exists()) {
            return;
        }
        String lineTxt = null;
        try {
            lineTxt = new BufferedReader(new InputStreamReader(
                    new BufferedInputStream(new FileInputStream(removeTxt))))
                    .readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (lineTxt == null) {
            return;
        }
        File file = new File(lineTxt);
        if (file.exists()) {
            file.delete();
        }
        removeTxt.delete();
    }

    public void compareVersionInSharedPreferene() {

        String currVer = activity.getString(R.string.db_version);
        SharedPreferences sp = activity.getSharedPreferences(mVerLib,
                Context.MODE_PRIVATE);
        String lastVer = sp.getString(CURR_VER_KEY, null);
        if (lastVer == null || !lastVer.equals(currVer)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(CURR_VER_KEY, currVer);
            editor.commit();
        }
    }

    public static void removeLeft(String path) {
        File file = new File(path);
        if (file.exists()) {

            file.delete();
            return;
        }
    }
}
