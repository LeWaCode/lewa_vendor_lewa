package com.lewa.spm.mode;

import java.util.Calendar;

import com.lewa.spm.util.Constants;
import com.lewa.spm.util.TimeUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PowSavingAlarm {
	private static final boolean DEBUG = true;

	/**
	 * set startTime alarm
	 */

	private static String getCalendarString(Calendar c) {
		String message = String.format("%1$04d/%2$02d/%3$02d %4$02d:%5$02d",
				c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1,
				c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE));

		return message;
	}

	public static void setStartTimeAlarm(Context ctx, String startTime) {
		String currentTime = TimeUtils.getCurrentTime();
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, c.get(Calendar.YEAR));
		c.set(Calendar.MONTH, c.get(Calendar.MONTH));
		if (currentTime.equals(startTime)) {
			c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		} else if (currentTime.compareTo(startTime) > 0) {
			c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH) + 1);
		} else {
			c.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));
		}
		c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime.substring(0, 2)));
		c.set(Calendar.MINUTE, Integer.parseInt(startTime.substring(3, 5)));
		c.set(Calendar.SECOND, 0);
		if (DEBUG)
			Log.i("tt", ":) set bed time start alarm:" + getCalendarString(c));
		Intent intent = new Intent(Constants.SPM_INTENT_ACTION_START_ALARM);
		intent.putExtra(Constants.SPM_INTENT_ACTION_START_ALARM_EXTRA,
				Constants.SPM_INTENT_ACTION_START_ALARM_EXTRA);
		PendingIntent startPendingIntent = PendingIntent.getBroadcast(ctx, 0,
				intent, 0);
		AlarmManager alarmManager = (AlarmManager) ctx
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
				24 * 60 * 60 * 1000, startPendingIntent);
	}

	/**
	 * set endTime alarm
	 */
	public static void setEndTimeAlarm(Context mContext, String endTime) {
		String currentTime = TimeUtils.getCurrentTime();
		Calendar calEnd = Calendar.getInstance();
		calEnd.set(Calendar.YEAR, calEnd.get(Calendar.YEAR));
		calEnd.set(Calendar.MONTH, calEnd.get(Calendar.MONTH));
		if (currentTime.equals(endTime)) {
			calEnd.set(Calendar.DAY_OF_MONTH, calEnd.get(Calendar.DAY_OF_MONTH));
		} else if (currentTime.compareTo(endTime) > 0) {
			calEnd.set(Calendar.DAY_OF_MONTH,
					calEnd.get(Calendar.DAY_OF_MONTH) + 1);
		} else {
			calEnd.set(Calendar.DAY_OF_MONTH, calEnd.get(Calendar.DAY_OF_MONTH));
		}

		calEnd.set(Calendar.HOUR_OF_DAY,
				Integer.parseInt(endTime.substring(0, 2)));
		calEnd.set(Calendar.MINUTE, Integer.parseInt(endTime.substring(3, 5)));
		calEnd.set(Calendar.SECOND, 0);
		if (DEBUG)
			Log.i("tt", ":) set bed time end alarm:"
					+ getCalendarString(calEnd));
		Intent endIntent = new Intent(Constants.SPM_INTENT_ACTION_END_ALARM);
		endIntent.putExtra(Constants.SPM_INTENT_ACTION_END_ALARM_EXTRA,
				Constants.SPM_INTENT_ACTION_END_ALARM_EXTRA);
		PendingIntent endPendingIntent = PendingIntent.getBroadcast(mContext,
				1, endIntent, 0);
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager
				.setRepeating(AlarmManager.RTC_WAKEUP,
						calEnd.getTimeInMillis(), 24 * 60 * 60 * 1000,
						endPendingIntent);
	}

	public static void cancelEndAlarm(Context mContext) {
		if (DEBUG)
			Log.i("tt", ":) cancel bed time end alarm.");
		Intent endIntent = new Intent(Constants.SPM_INTENT_ACTION_END_ALARM);
		endIntent.putExtra(Constants.SPM_INTENT_ACTION_END_ALARM_EXTRA,
				Constants.SPM_INTENT_ACTION_END_ALARM_EXTRA);
		PendingIntent endPendingIntent = PendingIntent.getBroadcast(mContext,
				1, endIntent, 0);
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(endPendingIntent);
	}

	public static void cancelStartAlarm(Context mContext) {
		if (DEBUG)
			Log.i("tt", ":) cancel bed time start alarm.");
		Intent startIntent = new Intent(Constants.SPM_INTENT_ACTION_START_ALARM);
		startIntent.putExtra(Constants.SPM_INTENT_ACTION_START_ALARM_EXTRA,
				             Constants.SPM_INTENT_ACTION_START_ALARM_EXTRA);
		PendingIntent startPendingIntent = PendingIntent.getBroadcast(mContext,
				0, startIntent, 0);
		AlarmManager alarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(startPendingIntent);
	}

}
