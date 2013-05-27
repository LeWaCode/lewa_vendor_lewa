package com.lewa.labi.intf;

import android.content.Context;
import android.os.Handler;

public abstract class AbsTask {
	public static final String Task_ID = "taskID";
	private static AbsTask task;

	public static <T extends AbsTask> AbsTask getFactory(Class<T> clazz) {
	    if (task == null) {
	        try {
	            task = clazz.newInstance();
	        } catch (IllegalAccessException e) {
	            e.printStackTrace();
	        } catch (InstantiationException e) {
	            e.printStackTrace();
	        }
	    }
		return task;
	}

	public abstract Object getTaskType() ;

	public abstract void setTaskTypeContact(Object taskTypeContact);

	public abstract Object getContactTaskType();

	public abstract void setTaskTypeSMS(Object taskTypeSMS);

	public abstract Object getSMSTaskType();

	public abstract void setTaskTypeCallLog(Object taskTypeCallLog) ;

	public abstract Object getCalllogTaskType();

	public abstract void setTaskTypeCalendar(Object taskTypeCalendar);

	public abstract Object getCalendarTaskType();

	public abstract void setTaskTypeModeIsAuto(Object taskTypeModeIsAuto);

	public abstract Object getTaskTypeModeIsAuto() ;

	public abstract void invokeSync(Context context);

	public abstract void invokeRecvy(Context context);

	public abstract void onSyncModeChanged();
	
	public abstract void setResult(Context context, Result result);
	
	public abstract void setHandler(Handler handler);

	public abstract Integer getId();
}
