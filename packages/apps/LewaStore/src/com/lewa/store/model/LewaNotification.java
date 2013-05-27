package com.lewa.store.model;

import com.lewa.store.R;
import com.lewa.store.nav.MainActivity;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.StrUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class LewaNotification {

	private Context context;
	private NotificationManager nm;

	private Intent intent;
	private PendingIntent pendIntent;
	private Notification notification;

	public LewaNotification(Context c) {
		this.context = c;
		nm = (NotificationManager) this.context
				.getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
   /**
     * 物理存储异常
     * @param 
     * 下载的app名字
     */
    public void notifyStorageException(String msg) {
        Notification notification = new Notification(R.drawable.notification_update_prompt,context.getString(R.string.notification_name_update_msg),
                System.currentTimeMillis());
        notification.icon=R.drawable.notification_update_prompt;
        notification.defaults |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,R.string.app_name,intent, PendingIntent.FLAG_ONE_SHOT);
        notification.setLatestEventInfo(context,context.getString(R.string.storage_exception_title),msg, pendingIntent);
        nm.notify(Constants.STORAGE_EXCEPIION_NOTIFICATION_ID, notification);
    }

	/**
	 * 下载安装成功
	 * @param appName
	 * 下载的app名字
	 */
	public void notifyDownloadSuccess(String appName) {
		Notification notification = new Notification(R.drawable.notification_download_ok,context.getString(R.string.notification_name),
				System.currentTimeMillis());
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent=new Intent(context,MainActivity.getInstance().getClass());
		intent.putExtra("gotomanager",1);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);// 该Intent使得当用户点击该通知后发出这个Intent,如果要以该Intent启动一个Activity，一定要设置
													// Intent.FLAG_ACTIVITY_NEW_TASK
													// 标记
		// 点击通知后的Intent,还是在当前界面
		PendingIntent pendingIntent = PendingIntent.getActivity(context,R.string.app_name, intent, PendingIntent.FLAG_ONE_SHOT);
		String appname=appName.replace(".apk","").trim();
		// 设置通知信息
		notification.setLatestEventInfo(context, appname, appname+context.getString(R.string.notification_install_sucess), pendingIntent);
		nm.notify(Constants.NOTIFICATION_DOWNLOAD_SUCESS_ID, notification);
	}
	
	/**
	 * 下载失败
	 * @param appName
	 *            下载的app名字
	 */
	public void notifyDownloadFailed(String appName) {
		Notification notification = new Notification(R.drawable.notification_download_failed,this.context.getString(R.string.notification_name),
				System.currentTimeMillis());
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent=new Intent(this.context,MainActivity.getInstance().getClass());
		intent.putExtra("gotomanager",1);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this.context,
				R.string.app_name, intent, PendingIntent.FLAG_ONE_SHOT);
		String appname=appName.replace(".apk","").trim();
		notification.setLatestEventInfo(this.context,appname,appname+this.context.getString(R.string.notification_download_failed), pendingIntent);
		nm.notify(Constants.NOTIFICATION_DOWNLOAD_FAILED_ID, notification);
	}
	
	/**
	 * 下载失败
	 * @param appName
	 *            下载的app名字
	 */
	public void notifyDownloadFailedResult(String appName,String result) {
		Notification notification = new Notification(R.drawable.notification_download_failed,this.context.getString(R.string.notification_name),
				System.currentTimeMillis());
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent=new Intent(this.context,MainActivity.getInstance().getClass());
		intent.putExtra("gotomanager",1);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this.context,
				R.string.app_name, intent, PendingIntent.FLAG_ONE_SHOT);
		String appname=appName.replace(".apk","").trim();
		notification.setLatestEventInfo(this.context,appname,result+","+appname+context.getString(R.string.notification_download_failed), pendingIntent);
		nm.notify(Constants.NOTIFICATION_DOWNLOAD_FAILED_ID, notification);
	}
	
	/**
	 * 安装失败
	 * @param appName
	 * @param result
	 */
	public void notifyInstallFailedResult(String appName,String result) {
		Notification notification = new Notification(R.drawable.notification_download_failed,this.context.getString(R.string.notification_name),
				System.currentTimeMillis());
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		PendingIntent pendingIntent = PendingIntent.getActivity(this.context,
				R.string.app_name, intent, PendingIntent.FLAG_ONE_SHOT);
		String appname=appName.replace(".apk","").trim();
		notification.setLatestEventInfo(this.context,appname,result+","+context.getString(R.string.notification_install_failed), pendingIntent);
		nm.notify(StrUtils.getRandomNumbers(), notification);
	}
	
	//push
	public void notifyUpdateMessage() {
		Notification notification = new Notification(R.drawable.notification_update_prompt,context.getString(R.string.notification_name_update_msg),
				System.currentTimeMillis());
		notification.icon=R.drawable.notification_update_prompt;
		notification.defaults |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent=new Intent(context,MainActivity.getInstance().getClass());
		intent.putExtra("gotomanager",1);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(context,R.string.app_name, intent, PendingIntent.FLAG_ONE_SHOT);
		notification.setLatestEventInfo(context,context.getString(R.string.notification_name_update_msg_title),context.getString(R.string.notification_name_update_msg_content), pendingIntent);
		nm.notify(Constants.NOTIFICATION_UPDATE_MESSAGE_ID, notification);
	}
	
	public void deleteNotification(int id){
		try {
			 nm.cancel(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 
	public void deleteAllNotification(){
		try {
			nm.cancelAll();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
