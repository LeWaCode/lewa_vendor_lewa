

package com.android.deskclock;


import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.preference.PreferenceManager;
import android.widget.Toast;


/**
 * The AlarmNotifationReceiver.java is added for lockscreen alarm,when select snooze
 * @author fulianwu
 *
 */
public class AlarmNotifationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("com.lewa.action.AlarmNotifation")){
            int alarmId = intent.getIntExtra(Alarms.ALARM_ID, 0);
            snooze(context, alarmId);
        }
        
    }
    
    // Attempt to snooze this alert.
    private void snooze(Context context,int alarmId) {
        final String snooze =
                PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsActivity.KEY_ALARM_SNOOZE, "10");
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis()
                + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(context, alarmId,
                snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = context.getString(R.string.default_label);
        label = context.getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(context, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_ID, alarmId);
        PendingIntent broadcast = PendingIntent.getBroadcast(context, alarmId, cancelSnooze, 0);
        NotificationManager nm = getNotificationManager(context);
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                label, 0);
        n.setLatestEventInfo(context, label,
                context.getString(R.string.alarm_notify_snooze_text,
                    Alarms.formatTime(context, c)), broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL
                | Notification.FLAG_ONGOING_EVENT;
        nm.notify(alarmId, n);

        String displayTime = context.getString(R.string.alarm_alert_snooze_set,
                snoozeMinutes);
        // Intentionally log the snooze time for debugging.
        Log.v(displayTime);

        // Display the snooze minutes in a toast.
        Toast.makeText(context, displayTime,
                Toast.LENGTH_LONG).show();
        context.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        
    }
    
    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
}
