package com.lewa.labi.impl;

import android.content.Context;
import android.content.Intent;

import com.lewa.labi.intf.ConcreateTask;

public class LabiTask extends ConcreateTask {
	public final static String ACTION_SYNC = "com.gozap.android.labi.intent.SYNC";
	public final static String ACTION_RECOVERY = "com.gozap.android.labi.intent.RESTORE";
	public final static String ACTION_SYNCMODE = "com.gozap.android.labi.intent.SYNC_MODE";

	@Override
    public void invokeSync(Context context) {
        Intent syncIntent = new Intent(ACTION_SYNC);
        syncIntent.putExtra("taskID" , this.getId()) ;
        context.startService(syncIntent);
	}

	@Override
    public void invokeRecvy(Context context) {
        Intent restoreIntent = new Intent(ACTION_RECOVERY);
        restoreIntent.putExtra("taskID" , this.getId()) ;
        context.startService(restoreIntent);
	}
	
	@Override
    public void onSyncModeChanged() {
		
	}
}
