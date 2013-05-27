package com.lewa.launcher.version;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Administrator
 */
public class ScheduleReminder {
	
	public static boolean isAutoUpdate = true;
    public final int saturday = 7;
    public final int remindDay = saturday;
    public final int hour = 19;
    public final int min = 0;
    public final int sec = 0;
    public Context context;
    public Timer timer;
    
    
	public ScheduleReminder() {
		super();
    	timer = new Timer(true);
	}

	public TimerTask getTimerTaskInstance() {
		return new TimerTask(){
			@Override
			public void run() {
				Looper.prepare();
				scheduleNext();
				Looper.loop();
			}
    		
    	};
	}

	public ScheduleReminder(Context context){
		this();
		this.context = context;
	}
	/*
	 * generate the next schedule time according now and schedule point
	 */
    public Date getScheuleStartTime() {
        Calendar scheduleBegin = Calendar.getInstance();
        

        Calendar scheduleDateTime = genScheduledPoint();
        if(scheduleBegin.after(scheduleDateTime)){
			scheduleBegin.set(Calendar.DAY_OF_WEEK,this.remindDay);
			scheduleBegin.set(Calendar.WEEK_OF_YEAR,scheduleBegin.get(Calendar.WEEK_OF_YEAR)+1);
			scheduleBegin.set(Calendar.HOUR_OF_DAY,this.hour);
        }
        else{
        scheduleBegin = scheduleDateTime;
        }
            return scheduleBegin.getTime();
    }	

	public Calendar genScheduledPoint() {
		Calendar saturday7Pm = Calendar.getInstance();

		saturday7Pm.set(Calendar.DAY_OF_WEEK, saturday);
        saturday7Pm.set(Calendar.HOUR_OF_DAY, hour);
        saturday7Pm.set(Calendar.MINUTE, min);
        saturday7Pm.set(Calendar.SECOND, sec);
        return saturday7Pm;
	}
	private void scheduleNext() {
    	runAutoUpdate();
    	timer.schedule(this.getTimerTaskInstance(), this.getScheuleStartTime());
    }
    
    private void runAutoUpdate() {
    	if(!ScheduleReminder.isAutoUpdate){
    		return;
    	}
    	new VersionUpdate((Activity)context).autoUpdate();
    }
    
    public void scheduleStart() {
    	Date date = this.getScheuleStartTime();
    	timer.schedule(this.getTimerTaskInstance(), date);
    }

}
