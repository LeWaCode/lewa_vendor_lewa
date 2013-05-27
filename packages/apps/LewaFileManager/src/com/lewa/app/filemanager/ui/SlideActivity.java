package com.lewa.app.filemanager.ui;

import android.view.View;
import java.util.ArrayList;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.widget.TabHost;
import android.widget.TextView;

import com.lewa.app.filemanager.R;
import com.lewa.filemanager.config.LogConf;
import com.lewa.filemanager.funcgroup.AppSrc;
import com.lewa.filemanager.ds.database.SQLManager;
import com.lewa.filemanager.actions.OperationUtil;
import com.lewa.filemanager.actions.apk.InstallHelper;
import com.lewa.filemanager.actions.apk.PackageInstallManager;
import com.lewa.base.NotifyUtil;
import com.lewa.filemanager.beans.ApkInfo;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.DensityUtil;
import com.lewa.base.Logs;
import com.lewa.filemanager.actions.apk.ApkNotifyBroadcast;
import com.lewa.filemanager.actions.apk.NotifyHelper;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.config.RuntimeArg;
import com.lewa.filemanager.ds.database.service.ScanReceiver;
import com.lewa.filemanager.ds.uri.NavigationConstants;
import com.lewa.filemanager.util.LWTReceiver;
import com.lewa.filemanager.util.PackageReceiver;
import com.lewa.filemanager.util.StatusCheckUtil;
import com.lewa.filemanager.util.StatusCheckUtil.MediaScannerBroadcast;
import com.lewa.os.ui.ViewPagerIndicator.OnPagerSlidingListener;
import com.lewa.os.ui.ViewPagerIndicatorActivity.StartParameter;
import java.util.Timer;
import java.util.TimerTask;

public class SlideActivity extends FileActivity {

    FileActivity activity;

    public SlideActivity(FileActivity activity) {
        this.activity = activity;
    }

    public void endJob() {
        RuntimeArg.shouldStopInstall = true;
        PackageInstallManager.getInstance().postJobWhenExiting(R.string.install_stoped_by_app_exit);
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                NotifyHelper.cancelApkNOtification();
            }
        }, 2000);
    }

    public void postCreate() throws Exception{
        new Thread() {

            @Override
            public void run() {

                AppSrc.initInstalledApps(fileActivityInstance);
                PackageInstallManager.createPackageManagerInstance(new InstallHelper(fileActivityInstance), fileActivityInstance);
                OperationUtil.initStrCnst(fileActivityInstance);
                FileInfo.init(fileActivityInstance);
                ApkInfo.init(fileActivityInstance);
                StatusCheckUtil.broadcastRec = new MediaScannerBroadcast();
                StatusCheckUtil.registerSDcardIntentListener(fileActivityInstance, StatusCheckUtil.broadcastRec);
                NotifyUtil.cancel(fileActivityInstance, NotifyUtil.ID_INSTALLED);
            }
        }.start();
        fileActivityInstance.setOnTriggerPagerChange(onPagerSlidingListener);
        titleHeight = fileActivityInstance.findViewById(R.id.titlebar).getLayoutParams().height + fileActivityInstance.findViewById(R.id.indicator).getLayoutParams().height;
        if (distributeFCFeed || launchPathActivity) {
            fileActivityInstance.findViewById(R.id.searchBtn).setVisibility(View.GONE);
            fileActivityInstance.findViewById(R.id.searchBtnLeftLine).setVisibility(View.GONE);

        }
        int px = 0;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
        switch (DensityUtil.getDensity(fileActivityInstance)) {
            case DensityUtil.WVGA:
                px = 33;
                break;
            case DensityUtil.HVGA:
                px = 22;
                break;
        }
        ((TextView) fileActivityInstance.findViewById(R.id.titleText)).setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        fileActivityInstance.findViewById(R.id.searchBtn).setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                Intent intent = new Intent(fileActivityInstance, SearchActivity.class);
                intent.putExtra("isNew", true);
                fileActivityInstance.startActivity(intent);
            }
        });
    }
    boolean distributeFCFeed = false;
    boolean launchPathActivity = false;

    public void setEntryActivity(FileActivity activity) {
        this.fileActivityInstance = activity;
    }
    public int titleHeight;
    public static SlideActivity paramActivity;
    public static FileActivity fileActivityInstance;
    public int isInOperation = -1;
    public TabHost mTabHost;
    public TextView sdcardView;
    // /** Called when the activity is first created. */
    public static final int TAB_INDEX_CATEGORY = 0;
    public static final int TAB_INDEX_SDCARD = 1;
    public int mDetailType;
    public static boolean firstTimeLaunch = true;
    public int currFrameId;
    public OnPagerSlidingListener onPagerSlidingListener = new OnPagerSlidingListener() {

        public void onChangePagerTrigger(int id) {
            currFrameId = id;
            if (PathActivity.activityInstance != null) {
                PathActivity.activityInstance.firstTimeStartup(PathActivity.activityInstance.ACCESS_ORIGINAL, false, 0);

            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    ScanReceiver scanReceiver;
    PackageReceiver packageReceiver;
    public static ApkNotifyBroadcast apkNotifyBroadcast;
    public LWTReceiver lwtReceiver;

    public void preCreate(final FileActivity context) throws Exception{
        
        paramActivity = this;
        SQLManager.createFileDB();
        new Thread() {

            @Override
            public void run() {
                super.run();
                Logs.setLevel(LogConf.LOG_LEVEL);
                NavigationConstants.init(activity);
                if (scanReceiver == null) {

                    scanReceiver = new ScanReceiver(context);
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
                    filter.addAction(ScanReceiver.ACTION_RECEIVER_SCAN);
                    filter.addAction(ScanReceiver.ACTION_SCANNING);
                    context.registerReceiver(scanReceiver, filter);

                    apkNotifyBroadcast = new ApkNotifyBroadcast();
                    IntentFilter filterCancelApkNotify = new IntentFilter();
                    filterCancelApkNotify.addAction(ApkNotifyBroadcast.ACTION_APK_NOTIFY_CANCEL);
                    context.registerReceiver(apkNotifyBroadcast, filterCancelApkNotify);

                    packageReceiver = new PackageReceiver(context);
                    IntentFilter pfilter = new IntentFilter();
                    pfilter.addAction(Intent.ACTION_PACKAGE_ADDED);
                    pfilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
                    pfilter.addAction(Constants.GoToInvokeLWT.ACTION_DELETE_LWT);
                    pfilter.addDataScheme("package");
                    context.registerReceiver(packageReceiver, pfilter);
                    lwtReceiver = new LWTReceiver(context);
                    IntentFilter lwtfilter = new IntentFilter();
                    lwtfilter.addAction(Constants.GoToInvokeLWT.ACTION_DELETE_LWT);
                    context.registerReceiver(lwtReceiver, lwtfilter);
                }
            }
        }.start();
        setSwipeFeature(context);
    }

    public void setSwipeFeature(FileActivity activity) {
        activity.setContentView(R.layout.main);
        
        Intent intent = activity.getIntent();
        String action = intent.getAction();
        if ("com.lewa.filemgr.count_start".equals(action)) {
            distributeFCFeed = true;
        }
        if (Constants.InvokedPath.ACTION_INVOKED_PATH.equals(action)) {
            launchPathActivity = true;
        }
        ArrayList<StartParameter> aClasses = new ArrayList<StartParameter>();

        if (!launchPathActivity) {
            Intent countIntent;
            if (distributeFCFeed) {
                countIntent = intent;
            } else {
                countIntent = new Intent();
            }
            countIntent.putExtra("delayloadcontent", true);
            aClasses.add(new StartParameter(distributeFCFeed ? InnerCallActivity.class : CountActivity.class, countIntent, R.string.tab_category));
        }

        if (!distributeFCFeed) {
            Intent sdcardIntent;
            if (launchPathActivity) {
                sdcardIntent = intent;
            } else {
                sdcardIntent = new Intent();
            }
            sdcardIntent.putExtra("delayloadcontent", true);
            aClasses.add(new StartParameter(PathActivity.class, sdcardIntent, R.string.tab_sdcardlist));
        }
        //        Intent privacyIntent = new Intent();
//        privacyIntent.putExtra("delayloadcontent", true);
//        aClasses.add(new StartParameter(PrivacyActivity.class, privacyIntent, R.string.tab_privacy));

        activity.setupFlingParm(aClasses, R.layout.main, R.id.indicator, R.id.pager);
//        openIntent(getIntent());
    }


    public void onEventDestroy() {
        endJob();
        if (scanReceiver != null) {
            fileActivityInstance.unregisterReceiver(scanReceiver);
            fileActivityInstance.unregisterReceiver(packageReceiver);
            fileActivityInstance.unregisterReceiver(apkNotifyBroadcast);
            activity.unregisterReceiver(lwtReceiver);
            StatusCheckUtil.unregisterSDcardIntentListener(this, StatusCheckUtil.broadcastRec);
        }
    }

    public void cancelApkNotification() {
        NotifyUtil.cancel(fileActivityInstance, NotifyUtil.ID_INSTALLED);
    }
}
