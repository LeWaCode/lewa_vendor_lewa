package com.lewa.spm.util;

import java.util.Date;

import android.content.Context;

import com.lewa.spm.R;

/**
 * Contains utility functions for formatting elapsed time and consumed bytes
 */
public class TimeUtils {
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int SECONDS_PER_HOUR = 60 * 60;
    private static final int SECONDS_PER_DAY = 24 * 60 * 60;

    /**
     * Returns elapsed time for the given millis, in the following format:
     * 2d 5h 40m 29s
     * @param context the application context
     * @param millis the elapsed time in milli seconds
     * @return the formatted elapsed time
     */
    public static String formatElapsedTime(Context context, double millis) {
        StringBuilder sb = new StringBuilder();
        int seconds = (int) Math.floor(millis / 1000);

        int days = 0, hours = 0, minutes = 0;
        if (seconds > SECONDS_PER_DAY) {
            days = seconds / SECONDS_PER_DAY;
            seconds -= days * SECONDS_PER_DAY;
        }
        if (seconds > SECONDS_PER_HOUR) {
            hours = seconds / SECONDS_PER_HOUR;
            seconds -= hours * SECONDS_PER_HOUR;
        }
        if (seconds > SECONDS_PER_MINUTE) {
            minutes = seconds / SECONDS_PER_MINUTE;
            seconds -= minutes * SECONDS_PER_MINUTE;
        }
        if (days > 0) {
        	sb.append(days + context.getString(R.string.spm_date) + hours + context.getString(R.string.spm_hour) + minutes + context.getString(R.string.spm_minute) + seconds + context.getString(R.string.spm_seconds));
        } else if (hours > 0) {
            sb.append(hours + context.getString(R.string.spm_hour) + minutes + context.getString(R.string.spm_minute) + seconds + context.getString(R.string.spm_seconds));
        } else if (minutes > 0) {
            sb.append(minutes + context.getString(R.string.spm_minute) + seconds + context.getString(R.string.spm_seconds));
        } else {
            sb.append(seconds + context.getString(R.string.spm_seconds));
        }
        return sb.toString();
    }

    /**
     * Formats data size in KB, MB, from the given bytes.
     * @param context the application context
     * @param bytes data size in bytes
     * @return the formatted size such as 4.52 MB or 245 KB or 332 bytes
     */
    public static String formatBytes(Context context, double bytes) {
        // TODO: I18N
        if (bytes > 1000 * 1000) {
            return String.format("%.2f MB", ((int) (bytes / 1000)) / 1000f);
        } else if (bytes > 1024) {
            return String.format("%.2f KB", ((int) (bytes / 10)) / 100f);
        } else {
            return String.format("%d bytes", (int) bytes);
        }
    }
    
    /**
     * calculate the D-value of the life times
     * @param context
     * @param fromValue
     * @param toValue
     * @param level
     * @return
     */
    
    public static String calcLifeChangeDiff(Context context, double p1, double p2, int level){
		return toTimeFormatDif(context,
            CalcUtils.getInstance(context).getHoursFromTime(p1, 1), 
            CalcUtils.getInstance(context).getHoursFromTime(p2, 1),
            CalcUtils.getInstance(context).getMinutesFromString(p1, 1),
            CalcUtils.getInstance(context).getMinutesFromString(p2, 1));
	}
    
    
    /**
	 * transfer the diff time of battery life time
	 * @param toHour
	 * @param fromHour
	 * @param toMinute
	 * @param fromMinute
	 * @return
	 */
	public static String toTimeFormatDif(Context context, int hour1, int hour2, int minute1, int minute2){
		int hourDif = 0;
		int minDif = 0;
		hourDif = hour1 - hour2;
		if (hourDif == 0){
			minDif = minute1 - minute2;
		}else if (hourDif > 0){
			if (minute1 < minute2){
				minDif = 60 - minute2 + minute1;
				hourDif --;
			}else if (minute1 > minute2){
				minDif = minute1 - minute1;
			}
		}else if (hourDif < 0){
			if (minute1 < minute2){
				minDif = minute2 - minute1;
			}else if (minute1 > minute2){
				minDif = 60 - minute1 + minute2;
				hourDif ++;
			}
		}
		if(hourDif > 0){
			return "+" + hourDif + context.getString(R.string.spm_hour) + minDif + context.getString(R.string.spm_minute);
		}else if (hourDif < 0){
			return hourDif + context.getString(R.string.spm_hour) + minDif + context.getString(R.string.spm_minute);
		}else if (hourDif == 0){
			if(minDif > 0){
				return "+" + minDif + context.getString(R.string.spm_minute);
			}else if (minDif < 0){
				return minDif + context.getString(R.string.spm_minute);
			}else {
				return "0";
			}
		}
		return null;
	}
	
	public static String transferTime (Context context, long time){
		String timeTemp ="";
		long hour = 0;
		long min = 0;
        // if charging time < 5min then charging time = 5min
        if(time < 300){
            time = 300;
        }
		hour = time / 3600;
		min = (time - (hour *3600)) / 60;
		if (hour > 0){
			timeTemp = timeTemp + hour + context.getString(R.string.spm_hour);
		}
		if (min > 0){
			timeTemp = timeTemp + min + context.getString(R.string.spm_minute);
		}
		return timeTemp;
	}

    public static String getCurrentTime(){
		Date currentDate = new Date();
		String currentTime = null;
		int hour = currentDate.getHours(); 
		int minute = currentDate.getMinutes();
		if (hour < 10){
			currentTime = "0" + hour;
		}else {
			currentTime = "" + hour;
		}
		if (minute < 10){
			currentTime = currentTime + Constants.INTEL_TIME_SIGN + "0" + minute;
		}else {
			currentTime = currentTime + Constants.INTEL_TIME_SIGN + minute;
		}
		return currentTime;
	}

}

