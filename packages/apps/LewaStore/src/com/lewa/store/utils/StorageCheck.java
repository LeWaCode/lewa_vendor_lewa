package com.lewa.store.utils;

import java.io.File;

import com.lewa.store.R;
import com.lewa.store.model.LewaNotification;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import static com.lewa.store.utils.Constants.LEWA_DIR;
import static com.lewa.store.utils.Constants.LEWA_CSTORE_DIR;
import static com.lewa.store.utils.Constants.ERROR_LOG_DIR;
import static com.lewa.store.utils.Constants.STORAGE_STATUS_LOW;
import static com.lewa.store.utils.Constants.STORAGE_STATUS_NONE;
import static com.lewa.store.utils.Constants.STORAGE_STATUS_OK;
import static com.lewa.store.utils.Constants.LOW_STORAGE_THRESHOLD;
import static com.lewa.store.utils.Constants.NO_STORAGE_ERROR;

public class StorageCheck {

	// 是否有sdcard
	public static boolean isSdcardExist() {
		return Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState()) ? true : false;
	}

	//检查APP创建工作目录
	public static int checkAppDirsAndMkdirs() {
		int status = getStorageStatus();
		if (status == STORAGE_STATUS_NONE) {//无存储卡
			return STORAGE_STATUS_NONE;
		} else if (status == LOW_STORAGE_THRESHOLD) {//空间太少
			return STORAGE_STATUS_LOW;
		} else {
			String[] appDirs = new String[] { LEWA_DIR, LEWA_CSTORE_DIR,
					ERROR_LOG_DIR };

			File file = null;
			for (String dir : appDirs) {
				file = new File(dir);
				if (!file.exists() || !file.isDirectory()) {
					file.mkdirs();
				}
			}
			return STORAGE_STATUS_OK;
		}
	}

	//获得存储状态
	public static int getStorageStatus() {
		long remaining = getAvailableStorage();
		if (remaining == NO_STORAGE_ERROR) {//没存储卡
			return STORAGE_STATUS_NONE;
		}
		return remaining < LOW_STORAGE_THRESHOLD ? STORAGE_STATUS_LOW
				: STORAGE_STATUS_OK;
	}

	// 获得sdcard可用存储空间
	public static long getAvailableStorage() {
		if (!hasStorage(true)) {
			return NO_STORAGE_ERROR;
		} else {
			StatFs stat = new StatFs(Constants.EXTERNAL_STORAGE_DIR);
			return (long) stat.getAvailableBlocks()
					* (long) stat.getBlockSize();
		}
	}

	//目录是否可写
	public static boolean hasStorage(boolean requireWriteAccess) {
		if (isSdcardExist()) {
			if (requireWriteAccess) {
				return checkFSWritable();//可写
			} else {
				return true;
			}
		}
		return false;
	}

	//检测目录是否可写
	private static boolean checkFSWritable() {
		String dirName = Constants.LEWA_DIR;
		File dir = new File(dirName);
		if (!dir.isDirectory() && !dir.mkdirs())
			return false;
		return dir.canWrite();
	}
	
	// 获取当前android可用内存大小
	public static long getAvailableMemory(Context context)
    {   
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        System.out.println(mi.availMem + "，" + mi.lowMemory);//mi.availMem; //当前系统的可用内存
//        return Formatter.formatFileSize(context, mi.availMem);
        return mi.availMem;
    }

	//是否低内存
	public static boolean isLowMemory(Context context){
	    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        am.getMemoryInfo(mi);
        return mi.lowMemory;
	} 
	
	//检测物理存储
	public static boolean checkPhysicalStorage(Context context,long size,LewaNotification notification){
        boolean flag=true;
        long filesize=(long) (size);//经过验证，安装后应用的大小范围是原来app大小的(1.2~2.5倍之间)
        String errorMsg="";
        if(!StorageCheck.isSdcardExist()){
            //无sd卡
            flag=false;
            errorMsg=context.getString(R.string.storage_exception_nosdcard);
            notification.notifyStorageException(errorMsg);
        }else if(StorageCheck.isLowMemory(context)){
            //内存过低
            flag=false;
            errorMsg=context.getString(R.string.storage_exception_lowmemory);
            notification.notifyStorageException(errorMsg);
        }/*else if(StorageCheck.getAvailableStorage() <= filesize){
            //sd卡空间不足
            flag=false;
            errorMsg=context.getString(R.string.storage_exception_sdcard_lowmemory);
            notification.notifyStorageException(errorMsg);
        }else if(StorageCheck.getAvailableStorage() <= filesize && StorageCheck.getAvailableMemory(context) <= filesize){
            //存储空间不足
            flag=false;
            errorMsg=context.getString(R.string.storage_exception_no_enough_memory);
            notification.notifyStorageException(errorMsg);
        }*/
        return flag;
    }
}
