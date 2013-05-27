package com.lewa.store.model;

import java.io.File;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lewa.store.R;
import com.lewa.store.activity.ManageActivity;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;

public class AppListModel {

    private String TAG = AppListModel.class.getSimpleName();

    private String localApkDir = null;
    
    private Context context;

    public AppListModel(Context c) {
    	this.context=c;
        this.localApkDir = Constants.DATA_DIR + File.separator;
    }

    public String getAppName(View v) {
        RelativeLayout layout = (RelativeLayout) v.getParent();
        String appName = ((TextView) layout.findViewById(R.id.app_name))
                .getText().toString();
//        Log.e(TAG, "appName==" + appName);
        return appName.trim();
    }

    public boolean isExistApkFile(String appName) {
        boolean a = new File(localApkDir + appName + Constants.APK_FILE_SUFFIX)
                .exists();
//        Log.e(TAG,"file path=="+localApkDir+appName+Constants.APK_FILE_SUFFIX);
//        Log.e(TAG, "isExistApkFile==" + a);
        return a;
    }

    public boolean installApkFromSD(String appName) {
    	PkgManager.getAppInstallLocation(this.context);
        return PkgManager.isInstalledSucess(localApkDir + appName+ Constants.APK_FILE_SUFFIX);
    }
    
    //获得当前队列中下载个数
    public int getDownloadingNumbers(){
        return ManageActivity.downloaders.size();
    }
}
