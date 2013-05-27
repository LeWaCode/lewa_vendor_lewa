package com.lewa.face.preview.slide.base;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import com.lewa.face.R;
import com.lewa.face.app.ThemeApplication;
import com.lewa.face.download.DownloadBase;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.preview.slide.adapter.OnlineSlideAdapter;
import com.lewa.face.util.ThemeActions;
import com.lewa.face.util.ThemeConstants;
import com.lewa.face.util.ThemeUtil;

public abstract class OnlineSlideBase extends SlideBaseActivity{

    private String TAG = OnlineSlideBase.class.getSimpleName();
    
    private AlertDialog alertDialog = null;
    private TextView mThemeDownload = null;

    private boolean isDownloading = false;
    
    private DownloadBase downloadBase = null;
    
    DControlThread dcontrolThread = null;
    private int sleepSpan=1000; // 线程的休眠时间 , add by zjyu ,2012.5.15

    protected OnlineSlideAdapter onlineSlideAdapter = null;
    
    @Override
    protected void initOtherViews() {
        findViewById(R.id.theme_bottom_bar_local).setVisibility(View.GONE);
        
        SharedPreferences sharedPreferences = mContext.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE);
        long containPkg = sharedPreferences.getLong(themeBase.getPkg(),-1);
        
        mThemeDownload = (TextView) findViewById(R.id.theme_download);
        
        dcontrolThread = new DControlThread();
        if(containPkg == ThemeConstants.DOWNLOADED){
            mThemeDownload.setText(R.string.theme_downloaded);
            mThemeDownload.setClickable(false);
        }else if(containPkg == ThemeConstants.DOWNLOADING){
            mThemeDownload.setText(R.string.theme_downloading);
            mThemeDownload.setClickable(false);
        }else{
            mThemeDownload.setOnClickListener(this);
        }
        
        
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ThemeActions.DOWNLOAD_THEME_OVER);
        registerReceiver(downloadOver, intentFilter);
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.theme_check_info:
        {
           /* Intent intent = new Intent();
            intent.setClass(mContext, ThemeOnlineModel.class);
            intent.putExtra("themeBase", themeBase);
            startActivity(intent);*/
            break;
        }
        case R.id.theme_download:
        {
            if(!isDownloading){
                isDownloading = true;
                
                /**
                 * SDCard no space
                 */
                if(!ThemeUtil.sdcardHasSpace(20)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.theme_sd_nospace_title);
                    builder.setMessage(R.string.theme_sd_nospace_msg);
                    builder.setNegativeButton(R.string.theme_back,new DialogInterface.OnClickListener() {
                     
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             //do nothing
                             isDownloading = false;
                             alertDialog.dismiss();
                         }
                    });
                    builder.setPositiveButton(R.string.theme_sd_clear, new DialogInterface.OnClickListener() {
                         
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             Intent fileManager = new Intent();
                             isDownloading = false;
                             fileManager.setAction(ThemeActions.START_FILEMANAGER_ACTION);
                             startActivity(fileManager);
                         }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }
                
                /**
                 * the network is not connect
                 */
                if(!ThemeUtil.isNetWorkEnable(mContext)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.theme_network_error_title);
                    builder.setMessage(R.string.theme_network_error_msg);
                    builder.setNegativeButton(R.string.theme_back,new DialogInterface.OnClickListener() {
                     
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             //do nothing
                             isDownloading = false;
                             alertDialog.dismiss();
                         }
                    });
                    builder.setPositiveButton(R.string.theme_network_set, new DialogInterface.OnClickListener() {
                         
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             isDownloading = false;
                             Intent intent = new Intent();
                             intent.setAction("android.settings.SETTINGS");
                             startActivity(intent);
                         }
                    });
                    alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }
                
                /**
                 * the network is not wifi
                 */
                if(!ThemeUtil.getNetworkType(mContext).equals("wifi")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.theme_network_remind_title);
                    builder.setMessage(R.string.theme_network_remind_msg);
                    builder.setNegativeButton(R.string.theme_back,new DialogInterface.OnClickListener() {
                     
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         //do nothing
                         isDownloading = false;
                         alertDialog.dismiss();
                     }
                 });
                    builder.setPositiveButton(R.string.theme_ignore, new DialogInterface.OnClickListener() {
                     
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         /**
                          * 忽略gprs网络，直接下载
                          */
                         download();
                     }
                 });
                    alertDialog = builder.create();
                    alertDialog.show();
                    return;
                }
                /**
                 * 如果以上条件都满足，直接下载
                 */
                download();
                
            }
            break;
        }
        default:
            break;
        }
        
    }

    
    private void download(){
        
        SharedPreferences.Editor editor = mContext.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
        editor.putLong(themeBase.getPkg(), ThemeConstants.DOWNLOADING);
        editor.commit();
        
        mThemeDownload.setText(R.string.theme_downloading);
        mThemeDownload.setClickable(false);
        
//        downloadBase.execute("");
        ThemeUtil.baselist.add(themeBase);
        if (ThemeUtil.startFlag) {
            ThemeUtil.startFlag = false;
            ThemeUtil.threadcontrolFlag = true;
            dcontrolThread.start();
        }
    }
    
    /**
     * 获得下载器
     * @param themeBase
     * @param context
     * @return
     */
    protected abstract DownloadBase getDownloadBase(ThemeBase themeBase,Context context);
    
    @Override
    protected ThemeBase initThemeBase(Intent intent) {
        themeBase = (ThemeBase) intent.getSerializableExtra(ThemeConstants.THEMEBASE); 
        return themeBase;
    }
    
     @Override
     protected void onDestroy() {
         ThemeApplication.activities.remove(this);
         unregisterReceiver(downloadOver);
         onlineSlideAdapter.onDestroy();
         super.onDestroy();
     }
     
     private BroadcastReceiver downloadOver = new BroadcastReceiver() {
         
         @Override
         public void onReceive(Context context, Intent intent) {
             String action = intent.getAction();
             if(action.equals(ThemeActions.DOWNLOAD_THEME_OVER)){
                 isDownloading = false;
                 mThemeDownload.setText(R.string.theme_downloaded);
                 mThemeDownload.setClickable(false);
             }
             
         }
     };
     
     @Override
     public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
         
         if(key.equals(themeBase.getPkg())){
             long flag = sharedPreferences.getLong(key, -1);
             if(flag == -1){
                 return;
             }
             if(flag == ThemeConstants.DOWNLOADFAIL){
                 isDownloading = false;
                 mThemeDownload.setText(R.string.theme_download);
                 return;
             }
             if(flag == ThemeConstants.DOWNLOADED){
                 isDownloading = false;
                 mThemeDownload.setText(R.string.theme_downloaded);
                 return;
             }
         }
     }

//Begin, 控制线程依次执, add by zjyu 
    class DControlThread extends Thread {

        @Override
        public void run() {
            while (ThemeUtil.threadcontrolFlag) {
                if (!ThemeUtil.baselist.isEmpty() ) {
                    synchronized (ThemeUtil.dcontrolFlag) {
                     if ("true".equals(ThemeUtil.dcontrolFlag)) {
                        ThemeUtil.dcontrolFlag = "false";
                        downloadBase = getDownloadBase(ThemeUtil.baselist.get(0),mContext);
                        downloadBase.execute("");
                        ThemeUtil.baselist.remove(0);
                    }
                    
                    }
                try {
                    DControlThread.sleep(sleepSpan);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                }else{//队列为空，停掉线程，并让线程可以再次开启
                    ThemeUtil.threadcontrolFlag = false;
                    ThemeUtil.startFlag = true;
                }
        }

    }

    }
//End
}
