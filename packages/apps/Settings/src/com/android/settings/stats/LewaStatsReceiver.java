package com.android.settings.stats;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.settings.R;

public class LewaStatsReceiver extends BroadcastReceiver {   
    private static final String PREF_NAME = "LewaStats";
    
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            if (isFirstBoot(ctx)) {
                promptUser(ctx);
            }
        }
    }
    
    private boolean isFirstBoot(Context context) {
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        boolean firstboot = settings.getBoolean("firstboot", true);
        return firstboot;
    }

    private void promptUser(Context context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification n = new Notification(R.drawable.icon,
                context.getString(R.string.notification_ticker), System.currentTimeMillis());
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", "com.android.settings.stats.StatsActivity");
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);
        n.setLatestEventInfo(context,
                context.getString(R.string.notification_title),
                context.getString(R.string.notification_desc), pi);
        nm.notify(1, n);
    }

}
