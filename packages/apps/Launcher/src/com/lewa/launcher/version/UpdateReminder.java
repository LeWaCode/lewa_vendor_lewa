package com.lewa.launcher.version;

import java.util.Calendar;
import java.util.Timer;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class UpdateReminder {
	public static boolean isAutoUpdate = true;
	public final int saturday = 7;
	public final int remindDay = saturday;
	public final int hour = 19;
	public final int min = 0;
	public final int sec = 0;
	public Context context;
	public Timer timer;
	public boolean runFlag;
	public UpdateReminder(Context context) {
		this.context = context;
	}
	public void scheduleStart() {
        Calendar cal = this.getScheuleStartTime(Calendar.getInstance());
		Calendar nextWeekCal = cal;
		nextWeekCal.set(Calendar.WEEK_OF_YEAR, cal.get(Calendar.WEEK_OF_YEAR));
		Intent intent = new Intent();
		intent.setClassName(context,AlarmReceiver.class.getName());
		intent.setAction(Intent.ACTION_SYNC);
		PendingIntent pendingIntent = PendingIntent
				.getBroadcast(context, 0,
						intent, 0);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, this.getScheuleStartTime(Calendar.getInstance())
				.getTimeInMillis(), getWeekDuration(), pendingIntent);

	}

	void runAutoUpdate() {
		if (!UpdateReminder.isAutoUpdate) {
			return;
		}
		new VersionUpdate((Activity) context).autoUpdate();
	}

	public Calendar getScheuleStartTime(Calendar startCal) {
		Calendar scheduleBegin = startCal;

		Calendar scheduleDateTime = thisWeekPoint();
		if (scheduleBegin.after(scheduleDateTime)) {
			scheduleBegin.set(Calendar.DAY_OF_WEEK, this.remindDay);
			scheduleBegin.set(Calendar.WEEK_OF_YEAR,
					scheduleBegin.get(Calendar.WEEK_OF_YEAR) + 1);
			scheduleBegin.set(Calendar.HOUR_OF_DAY, this.hour);
		} else if (scheduleBegin.before(scheduleDateTime)) {
			scheduleBegin = scheduleDateTime;
		} else if (scheduleBegin.equals(scheduleDateTime)) {
			scheduleBegin.set(Calendar.MINUTE,
					scheduleBegin.get(Calendar.MINUTE + 1));
			scheduleBegin = this.getScheuleStartTime(scheduleBegin);
		}
		return scheduleBegin;
	}

	public static long getWeekDuration() {
		Calendar scheduleBegin = Calendar.getInstance();
		Calendar scheduleEnd = scheduleBegin;
		scheduleEnd.set(Calendar.DAY_OF_WEEK, scheduleBegin.get(Calendar.DAY_OF_WEEK)+7);
		return scheduleEnd.getTimeInMillis()-scheduleBegin.getTimeInMillis();
	}
	
	public Calendar thisWeekPoint() {
		Calendar saturday7Pm = Calendar.getInstance();
		saturday7Pm.set(Calendar.DAY_OF_WEEK, saturday);
		saturday7Pm.set(Calendar.HOUR_OF_DAY, hour);
		saturday7Pm.set(Calendar.MINUTE, min);
		saturday7Pm.set(Calendar.SECOND, sec);
		return saturday7Pm;
	}
}
