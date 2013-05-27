package com.lewa.store.receiver;

import com.lewa.store.R;
import com.lewa.store.nav.MainActivity;
import com.lewa.store.utils.Constants;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateMessageReceiver extends BroadcastReceiver{
	
	String TAG=UpdateMessageReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
        if("com.lewa.pond.push".equals(intent.getAction())) {        	
        	String title = intent.getStringExtra("lewaApplicationMessageTitle");
            String msg = intent.getStringExtra("lewaApplicationMessage");
            String appId = intent.getStringExtra("lewaApplicationId");

            if("com.lewa.store".equals(appId)) {
	        	// handle message
                Log.e(TAG, "received push message");
                notifyUpdateMessage(context);
            }
        } 
    }
	
	public void notifyUpdateMessage(Context context) {
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon,context.getString(R.string.notification_name_update_msg),
				System.currentTimeMillis());
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
}
