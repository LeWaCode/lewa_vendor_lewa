package com.lewa.face.download;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.lewa.face.PkgMain;
import com.lewa.face.R;
import com.lewa.face.pojos.ThemeBase;
import com.lewa.face.util.ThemeConstants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.RemoteViews;

public abstract class DownloadBase extends AsyncTask<String, Integer, Boolean>{
    
    protected ThemeBase themeBase;
    protected Context context;
    protected String name;
    
    private NotificationManager nm;
    private Notification downloadingNotification;
    private RemoteViews remoteViews;
    private PendingIntent remotePendingIntent;
    
    private int fileSize = -1;
    private int completeSize = -1;
    protected String backCompName;
    
    public DownloadBase(ThemeBase themeBase,Context context){
        this.themeBase = themeBase;
        this.context = context;
        this.name = themeBase.getPkg();
    }
    
    public void setBackCompName(String backCompName) {
        this.backCompName = backCompName;
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        
        initNotifyDownloading();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // TODO Auto-generated method stub
        super.onProgressUpdate(values[0]);
        
        remoteViews.setProgressBar(R.id.notification_progress, fileSize, values[0], false);
        int per = values[0] * 100 /fileSize;
        remoteViews.setTextViewText(R.id.notification_appname,name);
        remoteViews.setTextViewText(R.id.text_download, context.getString(R.string.percent, String.valueOf(per)));
        
        downloadingNotification.contentView = remoteViews;
        downloadingNotification.contentIntent = remotePendingIntent;
        nm.notify(ThemeConstants.DOWNLOAD_NOTIFICATION_ID, downloadingNotification);
        /**
         * 下载完成后的提示音
         */
        if(per == 100){
            notifyDownloadSuccess(name);
        }
        
    }

    @Override
    protected Boolean doInBackground(String... arg0) {
        File target = targetFile();
        File parent = target.getParentFile();
        if(!parent.exists()){
            parent.mkdirs();
        }
        boolean success = false;
        try {
            success = download(target, downloadUrl());
        } catch (IOException e) {
            SharedPreferences.Editor editor = context.getSharedPreferences("DOWNLOADED", Context.MODE_PRIVATE).edit();
            editor.putLong(themeBase.getPkg(), ThemeConstants.DOWNLOADFAIL);
            editor.commit();
        }
        if(success){
            downloadSuccess();
        }else {
            nm.cancel(ThemeConstants.DOWNLOAD_NOTIFICATION_ID);
        }
        return success;
    }

    /**
     * 返回true则表明下载成功，否则失败
     * @param targetFile
     * @param urlPath
     * @return
     */
    @SuppressWarnings({ "finally", "static-access" })
    private boolean download(File targetFile, String urlPath) throws IOException {
        InputStream inStream = null;
        FileOutputStream fos = null;
        HttpURLConnection conn = null;
        
        try {
            
            URL url = new URL(urlPath);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);
            
            /**
             * 服务器端文件的长度，即大小
             */
            fileSize = conn.getContentLength();
            
            if (conn.getResponseCode() == 200) {
                inStream = conn.getInputStream();
                if(!targetFile.exists()){
                	targetFile.getParentFile().mkdirs();
                	targetFile.createNewFile();
                }
                fos = new FileOutputStream(targetFile);
                byte[] buffer = new byte[4096];
                int len = 0;
                int times = 0;
                while ((len = inStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    completeSize += len;
                    if (times == 0 || ((int) (completeSize * 100 / fileSize) - 10 > times)) {
                        times += 10;
                        publishProgress(completeSize);
                    }
                    
                    Thread.currentThread().sleep(10);
                }
                publishProgress(fileSize);
                fos.flush();
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                    inStream = null;
                }
                if (fos != null) {
                    fos.close();
                    fos = null;
                }
                
                if(conn != null){
                    conn.disconnect();
                    conn = null;
                }
                
                if(targetFile.length() != fileSize){
                    if(targetFile.exists()){
                        /**
                         * download fail
                         */
                        FileUtils.forceDelete(targetFile);
                    }
                    return false;
                }else {
                    /**
                     * download success
                     */
                    return true;
                }
                
            } catch (IOException e) {
                throw e;
            }
        }
    }
    
    private void initNotifyDownloading() {
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        downloadingNotification = new Notification();
        downloadingNotification.icon = R.drawable.theme_online_notifation;
        
        remoteViews = new RemoteViews(context.getPackageName(),R.layout.theme_notification_download_layout);
        remoteViews.setImageViewResource(R.id.image_download,R.drawable.theme_notification_downloading);//设置图片
        remoteViews.setProgressBar(R.id.notification_progress, 100, 0, false);
        //返回记忆
        //start, fixed for bug 9740 by yuzhijian
//        Intent intent = new Intent(ThemeConstants.GoToInvokeLWT.ACTION_INVOKE_LWT);
//        intent.putExtra(ThemeConstants.GoToInvokeLWT.FLAG_KEY_INVOKE_LWT, ThemeConstants.GoToInvokeLWT.FLAG_INVOKE_LWT);
//        intent.putExtra(ThemeConstants.GoToInvokeLWT.INVOKE_LWT_FILE_FIELD, targetFile().getAbsolutePath());
//        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        remotePendingIntent = PendingIntent.getActivity(context, R.string.app_name, null,  PendingIntent.FLAG_ONE_SHOT);
        //end
    }
    
    /**
     * 下载主题成功
     * @param themeName 下载的主题名字
     */
    private void notifyDownloadSuccess(String themeName) {
        
        nm.cancel(ThemeConstants.DOWNLOAD_NOTIFICATION_ID);
        
        // p1:通知的图标 p2:通知的状态栏显示的提示 p3:通知显示的时间
        Notification notification = new Notification(R.drawable.theme_notification_download_ok,"",
                System.currentTimeMillis());
        notification.defaults |= Notification.FLAG_AUTO_CANCEL;// 当用户点击
                                                                // Clear之后，能够清除该通知。
        notification.defaults |= Notification.DEFAULT_SOUND;// 默认声音
        notification.defaults |= Notification.DEFAULT_VIBRATE;// 默认振动
        notification.flags |= Notification.FLAG_AUTO_CANCEL;//

        Intent intent = new Intent(ThemeConstants.GoToInvokeLWT.ACTION_INVOKE_LWT);
        intent.putExtra(ThemeConstants.GoToInvokeLWT.FLAG_KEY_INVOKE_LWT, ThemeConstants.GoToInvokeLWT.FLAG_INVOKE_LWT);
        intent.putExtra(ThemeConstants.GoToInvokeLWT.INVOKE_LWT_FILE_FIELD, targetFile().getAbsolutePath());
        intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        // 点击通知后的Intent,还是在当前界面
        PendingIntent pendingIntent = PendingIntent.getActivity(context,R.string.app_name, intent, PendingIntent.FLAG_ONE_SHOT);
        String name = themeName.replace(".lwt","").trim();
        // 设置通知信息
        notification.setLatestEventInfo(context, name, name+context.getString(R.string.download_success), pendingIntent);
        // 发送通知
        nm.notify(0, notification);
    }
    
    protected abstract File targetFile();
    
    protected abstract String downloadUrl();
    
    protected abstract void downloadSuccess();


}
