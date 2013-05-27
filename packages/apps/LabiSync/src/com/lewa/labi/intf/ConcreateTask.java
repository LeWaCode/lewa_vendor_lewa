package com.lewa.labi.intf;

import java.util.Random;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

public abstract class ConcreateTask extends AbsTask implements Cloneable{
	private static ConcreateTask task;
	protected Object taskTypeContact;
	protected Object taskTypeSMS;
	protected Object taskTypeCallLog;
	protected Object taskTypeCalendar;
	protected Object taskTypeNotifyModeIsAuto;
	private Object taskType = -1;
	private Integer taskid = -1;
	private Handler mHandler;

	public ConcreateTask newInstance() throws Exception {
		return task.getClass().newInstance();
	}

	public static <T extends ConcreateTask> ConcreateTask getFactory(Class<T> clazz) {
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

	public Object getTaskType() {
	    return taskType;
	}

	public void setTaskTypeContact(Object taskTypeContact) {
		this.taskTypeContact = taskTypeContact;
	}

	public Object getContactTaskType() {
	    return taskTypeContact;
	}

	public void setTaskTypeSMS(Object taskTypeSMS) {
		this.taskTypeSMS = taskTypeSMS;
	}

	public Object getSMSTaskType() {
	    return taskTypeSMS;
	}

	public void setTaskTypeCallLog(Object taskTypeCallLog) {
		this.taskTypeCallLog = taskTypeCallLog;
	}

	public Object getCalllogTaskType() {
	    return taskTypeCallLog;
	}

	public void setTaskTypeCalendar(Object taskTypeCalendar) {
		this.taskTypeCalendar = taskTypeCalendar;
	}

	public Object getCalendarTaskType() {
	    return taskTypeCalendar;
	}

	public void setTaskTypeModeIsAuto(Object taskTypeModeIsAuto) {
		this.taskTypeNotifyModeIsAuto = taskTypeModeIsAuto;
	}

	public Object getTaskTypeModeIsAuto() {
	    return taskTypeNotifyModeIsAuto;
	}

	public void setResult(Context context, Result result) {
	     Message msg = mHandler.obtainMessage(0, result);
         mHandler.sendMessage(msg);
	}
	
	public void setHandler(Handler handler) {
	    mHandler = handler;
	}
	
	public Integer getId() {
	    if (taskid == -1) {
	        Random r = new Random();
	        taskid = r.nextInt(8999)+1000;
	    }
	    return taskid;
	}

	public abstract void invokeSync(Context context);
	
	public abstract void invokeRecvy(Context context);
	
	public abstract void onSyncModeChanged();
}
