/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.apk;

import android.content.Context;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.beans.ReportInfo;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class ReportOfInstall {

    public static List<ReportInfo> success = new ArrayList<ReportInfo>();
    public static List<ReportInfo> untackled = new ArrayList<ReportInfo>();
    public static List<ReportInfo> failed = new ArrayList<ReportInfo>();
    public static String successStr = "";
    public static String failureStr = "";
    public static String unhandledStr = "";
    public static String totalStr = "";
    public static String messageStr = "";

    public static void clear() {
        success.clear();
        untackled.clear();
        failed.clear();
        successStr = "";
        failureStr = "";
        unhandledStr = "";
        totalStr = "";
        messageStr = "";
    }

    public static void prepareReportText(Context ctx) {
        prepareReportText(ctx, PackageInstallManager.getInstance().successNum, PackageInstallManager.getInstance().faliureNum, PackageInstallManager.toInstallPaths.size());
    }

    public static void prepareReportText(Context context, Integer successNum, Integer failureNum, Integer totalNum) {
        successStr = context.getString(R.string.success) + " " + (successNum) + " ";
        failureStr = context.getString(R.string.failure) + " " + (failureNum) + " ";
        totalStr = context.getString(R.string.toInstallSum) + " " + (totalNum);
        messageStr = successStr + " " + failureStr +  " " + totalStr;
    }
}
