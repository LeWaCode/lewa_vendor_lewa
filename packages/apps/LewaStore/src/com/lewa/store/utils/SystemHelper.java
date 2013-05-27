package com.lewa.store.utils;

import com.lewa.store.activity.ManageActivity;
import com.lewa.store.extras.SinaPackages;
import com.lewa.store.pkg.LaunchApp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class SystemHelper {

	private static String TAG = SystemHelper.class.getSimpleName();

	private Context mContext;
    private ActivityManager activityManager;

	public SystemHelper(Context c) {
		this.mContext = c;
		activityManager = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
	}

	public void stopStoreService() {
		try {
			// if (ManageActivity.downloadings.size() == 0 &&
			// ManageActivity.downloaders.size()==0) {
			if (ManageActivity.downloadings.size() == 0) {// 下载失败的，全部移除
				Intent intent = new Intent();
				intent.setAction(Constants.DOWNLOAD_ACTION);
				mContext.stopService(intent);
				Log.i(TAG, "send signal to stop service");
			} else {
				Log.e(TAG, "there have undone download task,can not stop!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "stop service err!,msg==" + e.getMessage());
		}
	}

	public void stopStoreProcess() {
		try {
			if (ManageActivity.downloadings.size() == 0) {
				Log.d(TAG, "stopStoreProcess,pid=" + android.os.Process.myPid());
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Handler stopHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what==Constants.HandlerWhatFlagStopProcess){
				try {
					Log.d(TAG,"kill process in handler");
					stopStoreProcess();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	};
	
	public void beforeStop(){
		if (LaunchApp.isInstallApp(SinaPackages.LEWA_SINA_PACKAGENAME)) {
			Log.d(TAG, "is lewa weibo");
			if (LaunchApp.isInstallApp(SinaPackages.PUBLIC_SINA_PACKAGENAME)) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						Looper.prepare();
						Log.d(TAG, "also have pub weibo");	
						try {
							if(SinaPackages.uninstallWeibo()){
								Log.d(TAG,"beforeStop,uninstall ok");
								Message message = Message.obtain();
								message.what = Constants.HandlerWhatFlagStopProcess;
								stopHandler.sendMessage(message);
							}else{
								Log.d(TAG,"beforeStop uninstall error");
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}else{
				Log.d(TAG,"not install pub weibo");
				stopStoreProcess();				
			}
		}else{
			Log.d(TAG,"not install lewa weibo");
			stopStoreProcess();
		}
	}

	public boolean isDownloading() {
		boolean flag = false;
		if (ManageActivity.downloadings.size() != 0) {
			flag = true;
		}
		return flag;
	}

	public void displayBriefMemory() {		
		ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
		activityManager.getMemoryInfo(info);
		Log.i(TAG, "system remain memory:" + (info.availMem >> 10) + "kb");
		Log.i(TAG, "is low memory：" + info.lowMemory);
//		Log.i(TAG, "当系统剩余内存低于" + info.threshold+" 时就看成低内存运行");
	}
}
