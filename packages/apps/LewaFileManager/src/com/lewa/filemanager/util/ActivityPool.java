/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.lewa.app.filemanager.R;
import com.lewa.base.Logs;
import com.lewa.base.SharedPreferenceUtil;
import com.lewa.filemanager.ds.database.service.FileScanService;
import com.lewa.filemanager.actions.apk.PackageInstallManager;
import com.lewa.app.filemanager.ui.CommonActivity;
import com.lewa.app.filemanager.ui.SlideActivity;
import com.lewa.filemanager.actions.apk.NotifyHelper;
import com.lewa.filemanager.config.Constants;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Administrator
 */
public class ActivityPool {

    public Set<CommonActivity> activityList = new HashSet<CommonActivity>();
    private static ActivityPool instance;

    protected ActivityPool() {
    }

    public static ActivityPool getInstance() {
        if (null == instance) {
            instance = new ActivityPool();
        }
        return instance;
    }
    //添加Activity到容器中

    public void addActivity(CommonActivity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    public void refresh() {
        refresh(null);
    }

    public void sortrefresh() {
        sortrefresh(null);
    }

    public void refresh(CommonActivity commonActivity) {
        for (CommonActivity activity : activityList) {
            if (activity != commonActivity) {
                activity.refresh();
            }
        }
    }

    public void sortrefresh(CommonActivity commonActivity) {
        for (CommonActivity activity : activityList) {
            Logs.i("--------- activity " + activity);
            if (activity != commonActivity) {
                activity.sortRefresh();
            }
        }
    }

    public void invokeMethod(String methodName, CommonActivity exclude, Class[] classes, Object[] values) {
        for (CommonActivity activity : activityList) {
            Logs.i("--------- activity " + activity);
            if (activity != exclude) {
                try {
                    activity.getClass().getMethod(methodName, classes).invoke(activity, values);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void exit() {
        PackageInstallManager.getInstance().postJobWhenExiting(R.string.install_stoped_by_app_exit);        
        SharedPreferenceUtil.putValue(SlideActivity.fileActivityInstance, Constants.SharedPrefernce.RememberedCategory, Constants.SharedPrefernce.KEY_CATEGORY, "");
        SlideActivity.fileActivityInstance.stopService(new Intent(((Context) SlideActivity.fileActivityInstance), FileScanService.class));
        for (Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);
    }
}
