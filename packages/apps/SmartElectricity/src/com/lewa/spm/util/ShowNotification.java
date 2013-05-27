//package com.lewa.spm.util;
//
//import com.lewa.spm.R;
//import com.lewa.spm.activity.SPMActivity;
//
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.Context;
//import android.content.Intent;
//import android.widget.RemoteViews;
//
//public class ShowNotification {
//	 public static void showNotification(Context mContext, int level, String time , int lowPower, String startTime, String endTime) {
//		 SharedStorageKeyValuePair saveValue = new SharedStorageKeyValuePair(mContext);
//		 Notification notification = null;
//	     RemoteViews contentView = null;
//	     Intent notificationIntent = new Intent(mContext, SPMActivity.class);
//	     notificationIntent.putExtra(Constants.SPM_NOTIFICATION, 2);
//	     NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
//		 if (startTime.compareTo(endTime) > 0){// the bedTimeEnd is tomorrow's time
//				if (time.compareTo(startTime) >= 0){
//					notification = new Notification( R.drawable.spm_lewa_icon, "", System.currentTimeMillis());
//		            contentView = contentViewAdd(mContext, mContext.getString(R.string.spm_notification_time));
//		            notification.contentView = contentView;
//		            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
//		            notification.contentIntent = contentIntent;
//		            mNotificationManager.notify(R.layout.spm_notification, notification);
//		            saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_TIME_COUNT, 1);
//				}
//			}else if (time.compareTo(startTime) * (time.compareTo(endTime)) <= 0){//(currentTime.compareTo(bedTimeStart) >= 0) && (currentTime.compareTo(bedTimeEnd) <= 0)
//				notification = new Notification(R.drawable.spm_lewa_icon, "", System.currentTimeMillis());
//		            contentView = contentViewAdd(mContext, mContext.getString(R.string.spm_notification_time));
//		            notification.contentView = contentView;
//		            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
//		            notification.contentIntent = contentIntent;
//		            mNotificationManager.notify(R.layout.spm_notification, notification);
//		            saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_TIME_COUNT, 1);
//			}else {
//				mNotificationManager.cancel(R.layout.spm_notification);
//			}
//	        
//	        if (level <= lowPower){
//	        	notification = new Notification(R.drawable.spm_lewa_icon, "", System.currentTimeMillis());
//	            contentView = contentViewAdd(mContext,  mContext.getString(R.string.spm_notification_low_power));
//	            notification.contentView = contentView;
//	            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
//	            notification.contentIntent = contentIntent;
//	            mNotificationManager.notify(R.layout.spm_notification, notification);
//	            saveValue.saveIntToSharedpreference(Constants.SHARED_PREFERENCE_NAME, Constants.SPM_NOTIFICATION_LOW_POWER_COUNT, 1);
//	        }else{
//	        	mNotificationManager.cancel(R.layout.spm_notification);
//	        }
//	    }
//	 
//	 private static RemoteViews contentViewAdd(Context context , String content) {
//	        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.spm_notification);
//	        contentView.setImageViewResource(R.id.spm_notification_img, R.drawable.spm_lewa_icon);
//	        contentView.setTextViewText(R.id.spm_notification_title, context.getString(R.string.app_name));
//	        contentView.setTextViewText(R.id.spm_notification_content, content);
//	        return contentView;
//	    }
//}
