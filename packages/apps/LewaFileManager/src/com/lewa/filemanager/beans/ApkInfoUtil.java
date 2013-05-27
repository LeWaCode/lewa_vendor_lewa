/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.beans;

import android.content.Context;
import com.lewa.filemanager.funcgroup.AppSrc;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.filemanager.cpnt.adapter.CountAdapter;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.filemanager.funcgroup.AppSrc;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chenliang
 */
public class ApkInfoUtil {
    public static boolean bindFlag;
    public static <T extends FileInfo> boolean preserApkInfo(FileInfo fileInfo, Context context) {
        ApkInfo apkInfo = (ApkInfo) fileInfo;
        if (!apkInfo.isBuilt) {
            if (!apkInfo.setApkInfo(context)) {
                apkInfo.versionCondition = ApkInfo.DISPLAY_UNKNOWN;
                apkInfo.versionName = ApkInfo.DISPLAY_UNKNOWN;
                return true;
            }
            String anotherVersionName = AppSrc.getPackagesVersion().get(apkInfo.packageName);
            int result;
            if (anotherVersionName != null) {
//                Logs.i("--- apk compare " + apkInfo.name + " " + apkInfo.versionName + " " + anotherVersionName);
//                result = new ApkInfo.ApkVersionName(apkInfo.versionName).compareTo(new ApkInfo.ApkVersionName(anotherVersionName));
//                if (result > 0) {
//                    apkInfo.versionCondition = ApkInfo.VERSION_UPGRADE;
//                } else if (result < 0) {
                    apkInfo.versionCondition = ApkInfo.VERSION_INTALLED;
//                } else if (result == 0) {
//                    apkInfo.versionCondition = ApkInfo.VERSION_UPDATETODATE;
//                }
            } else {
                apkInfo.versionCondition = ApkInfo.VERSION_UNINTALLED;
            }
        }
        return false;
    }

    public static void refreshInfo(final CountAdapter adapter, final CommonActivity activity) {

        new Thread() {

            @Override
            public void run() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ApkInfoUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
                Iterator<Integer> positions = new HashSet<Integer>(adapter.items.keySet()).iterator();
                Integer i = -1;
                while (positions.hasNext()) {
                    Logs.i("----i i" + i);
                    i = positions.next();
                    preserApkInfo((ApkInfo) (adapter.items.get(i)), activity);
                }
                activity.handler.sendEmptyMessage(Constants.OperationContants.REFRESH_APKINFO);
            }
        }.start();
    }
}
