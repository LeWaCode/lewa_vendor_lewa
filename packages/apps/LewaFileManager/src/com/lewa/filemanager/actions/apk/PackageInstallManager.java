/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.apk;

import com.lewa.app.filemanager.R;
import android.app.Activity;
import android.content.BroadcastReceiver;
import com.lewa.base.NotifyUtil;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageManager;
import com.lewa.filemanager.config.RuntimeArg;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.app.filemanager.ui.CountActivity;
import com.lewa.app.filemanager.ui.SlideActivity;
import com.lewa.app.filemanager.ui.PathActivity;
import com.lewa.filemanager.beans.AppInfoData;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.base.images.Utillocal;
import com.lewa.filemanager.beans.ReportInfo;
import java.io.File;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class PackageInstallManager extends IPackageInstallObserver.Stub {

    public static List<String> toInstallPaths;
    private static PackageInstallManager instance;
    InstallHelper pi;
    NotifyUtil notifyUtil;
    private Context context;
    int faliureNum = 0;
    int successNum = 0;
    int unhandled = 0;
    Intent backIntent;
    String apkpath;
    String intallingAppLabel;
    public static final String STR_FAIL_APP_LABEL = "failureAppLabels";

    public static void createPackageManagerInstance(InstallHelper pi, Context context) {
        if (instance == null) {
            instance = new PackageInstallManager(pi, context);
        }
    }

    public static PackageInstallManager getInstance() {
        return instance;
    }

    public void endWhileAppAlive() {
        ReportOfInstall.prepareReportText(context);
        popInstallReport();
        NotifyUtil.cancel(context, NotifyUtil.ID_INSTALLED);
        RuntimeArg.isInInstall = false;
    }

    public void postJobWhenExiting(int headText) {
        if (RuntimeArg.isInInstall) {
            PackageInstallManager.getInstance().end(headText);
        }
    }
    public void end(Integer resultAbsId) {
        notifyOverResult(resultAbsId);
        clear();
        NotifyHelper.cancelApkNOtification();
        RuntimeArg.isInInstall = false;
    }

    public String getApkLabel(String apkpath) {
        if(apkpath==null){
            return null;
        }
        File apk = new File(apkpath);
        AppInfoData appdata = Utillocal.getApkInfo(context, apk);
        String intallingAppLabel;
        if (appdata == null) {
            intallingAppLabel = apk.getName();
        } else {
            intallingAppLabel = appdata.name;
        }
        return intallingAppLabel;
    }

    public int getFaliureNum() {
        return faliureNum;
    }

    public int getSuccessNum() {
        return successNum;
    }

    public int getUnhandled() {
        return unhandled;
    }

    public void clearHistory() {
        ReportOfInstall.clear();
    }

    public String constructProgress(int current, int total) {
        return " (" + current + "/" + total + ") ";
    }

    protected PackageInstallManager(InstallHelper pi, Context context) {
        this.pi = pi;
        notifyUtil = new NotifyUtil(context);
        this.context = context;
        backIntent = new Intent(Constants.InvokedPath.ACTION_INSTALL_PATH);
        backIntent.addCategory("android.intent.category.DEFAULT");
    }

    public void notifyOverResult(int resultAbsId) {
        Intent intent = null;
        if (resultAbsId == R.string.install_stoped_by_app_exit) {
            intent = new Intent("com.lewa.filemgr.count_start");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
        } else {
            if (SlideActivity.fileActivityInstance == null) {
            } else {
                intent = new Intent("com.lewa.filemgr.file_start");
            }
            intent.addCategory(Intent.CATEGORY_DEFAULT);
        }
        notifyAllInstalled(notifyUtil, resultAbsId, successNum, faliureNum, pi.getTotalInstallSum(), context, intent);


    }

    public void notifyOverResult() {
        notifyOverResult(R.string.totalInstallFinishedHint);
    }

    public void packageInstalled(String packageName, int returnCode) {
        if (RuntimeArg.shouldStopInstall) {
            RuntimeArg.shouldStopInstall = false;
            return;
        }
        Logs.i("failureAppLabels ---- packageInstalled "+ packageName+" "+ returnCode);
        if (pi.getCurrInstallPath() == null) {
            throw new RuntimeException("currently don't install any apks");
        }
        String appLabel = getApkLabel(apkpath);
        boolean isFailed = false;
        ApplicationInfo appInfo;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
//            appLabel = context.getPackageManager().getApplicationLabel(appInfo).toString();
            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                ReportOfInstall.failed.add(new ReportInfo(apkpath, appLabel));
                faliureNum++;
                isFailed = true;
                Logs.i("failureAppLabels ---- isFailed" + appLabel + " " + packageName);
            }

        } catch (Exception ex) {
            if (!ReportOfInstall.failed.contains(apkpath)) {
                ReportOfInstall.failed.add(new ReportInfo(apkpath, appLabel));
                faliureNum++;
                isFailed = true;
            }
        }
        if (!isFailed) {
            successNum++;
            ReportOfInstall.success.add(new ReportInfo(apkpath, appLabel));
        }
        int isSuccess = !isFailed ? R.string.appInstallSuccessHint : R.string.appInstallFailureHint;

        Logs.i("", "finishedAllInstall ---- apkpath " + apkpath);
        if (tryNextInstall(appLabel, isSuccess, isFailed)) {
            return;
        }
    }

    public boolean tryNextInstall(String appLabel, int isSuccess, boolean isFailed) {
        apkpath = pi.getIteratorNext(pi.getPaths());
        Logs.i("", "finishedAllInstall ---- apkpath " + apkpath);
        if (!pi.hasNext(apkpath)) {
            endWhileAppAlive();
        } else {
            String intallingAppLabel = getApkLabel(apkpath);
            if (intallingAppLabel == null) {
                return true;
            }
            tellThatLatestInstallationDone(notifyUtil, intallingAppLabel, appLabel, isSuccess, !isFailed, pi.getCurrInstallIdx(), pi.getTotalInstallSum());
        }
        return false;
    }

    public String tackleErrorApk(String apkpath) {
        String intallingAppLabel = null;
        while (((!apkpath.equals("end")) && (intallingAppLabel = getApkLabel(apkpath)).equals(new File(apkpath).getName()))) {
            apkpath = pi.getIteratorNext(pi.getPaths());
            faliureNum++;
        }
        Logs.i("invalid " + apkpath);
        if (apkpath.equals("invalid")) {
            notifyOverResult();
            clear();
            return null;
        }
        return intallingAppLabel;
    }

    public void beginInstall(List<String> paths) {
        RuntimeArg.isInInstall = true;
        PackageInstallManager.getInstance().clearHistory();
        PackageInstallManager.getInstance().clear();
        String apkpath = pi.getIteratorNext(paths);
        intallingAppLabel = getApkLabel(apkpath);
        intallingAppLabel = intallingAppLabel==null|| intallingAppLabel.equals(new File(apkpath).getName())?context.getString(R.string.unknown):intallingAppLabel;
        if (pi.hasNext(apkpath)) {
            beginAllInstall(notifyUtil, paths.size(), intallingAppLabel);
        }
    }

    private void beginAllInstall(NotifyUtil notifyUtil, Integer totalAppNum, String intallingAppLabel) {
        notifyInstallId(notifyUtil, context.getString(R.string.appInstallStartTicker), context.getString(R.string.installing) + " [" + intallingAppLabel + "] " + constructProgress(1, totalAppNum), context.getString(R.string.nowInstallingForYou), backIntent, SlideActivity.fileActivityInstance, Activity.class);
    }

    private void tellThatLatestInstallationDone(NotifyUtil notifyUtil, String intallingAppLabel, String processedAppLabel, int resultLabel, boolean isInstallSuccess, Integer current, Integer totalAppNum) {
        String ticker = processedAppLabel + " " + context.getString(resultLabel);
        String title = context.getString(R.string.installing) + " [" + intallingAppLabel + "] " + constructProgress(current, totalAppNum);
        String msg = processedAppLabel + " " + (isInstallSuccess ? context.getString(R.string.already) : "") + context.getString(resultLabel);
        notifyInstallId(notifyUtil, ticker, title, msg, backIntent, SlideActivity.fileActivityInstance, Activity.class);
    }

    public static void notifyAllInstalled(NotifyUtil notifyUtil, int resulrAbstractId, Integer successNum, Integer failureNum, Integer totalNum, Context context, Intent backIntent) {
        Logs.i("-----------" + successNum + " " + failureNum + " " + totalNum);
        String successStr = context.getString(R.string.success) + " " + (successNum) + " ";
        String failureStr = context.getString(R.string.failure) + " " + (totalNum - successNum) + " ";
//        String unhandledStr = context.getString(R.string.unhandled) + " " + (totalNum - successNum - failureNum) + " ";
        String totalStr = context.getString(R.string.toInstallSum) + " " + (totalNum);
        String installOverHint = context.getString(resulrAbstractId);
        String messageStr = successStr + " " + failureStr + " "
                //                + unhandledStr + " " 
                + totalStr;
        String resultAbs = context.getString(resulrAbstractId);
//        if (failureNum > 0) {
//            backIntent.putStringArrayListExtra(STR_FAIL_APP_LABEL, (ArrayList<String>) ReportOfInstall.failed);
//        }
        NotifyUtil.cancel(context, NotifyUtil.ID_INSTALLED);
        Intent intent = new Intent(ApkNotifyBroadcast.ACTION_APK_NOTIFY_CANCEL);
        notifyInstallId(notifyUtil, resultAbs + "  " + successStr + failureStr /*+ unhandledStr */ + totalStr, installOverHint, messageStr, intent, SlideActivity.fileActivityInstance, BroadcastReceiver.class);
    }

    private static void notifyInstallId(NotifyUtil notifyUtil, String tickerText, String title, String downWords, Intent intent, Context activity, Class targetFlag) {
        notifyUtil.notify(NotifyUtil.ID_INSTALLED, R.drawable.com_android_folder, tickerText, title, downWords, intent, activity, targetFlag);
    }

    public void clear() {
        faliureNum = 0;
        successNum = 0;
        unhandled = 0;
        this.pi.clear();
    }

    private void popInstallReport() {

        int frameid = SlideActivity.paramActivity.currFrameId;
        CommonActivity ui = null;
        if (frameid == SlideActivity.TAB_INDEX_CATEGORY) {
            ui = CountActivity.categoryActivity;
        } else if (frameid == SlideActivity.TAB_INDEX_SDCARD) {
            ui = PathActivity.activityInstance;
        }
        ui.handler.sendEmptyMessage(Constants.OperationContants.ONINSTALL_REPORT);

    }
}
