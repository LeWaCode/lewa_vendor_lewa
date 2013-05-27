package com.lewa.PIM.mms.MsgPopup;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class MsgReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
    	/*ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    	ComponentName cn = am.getRunningTasks(1).get(0).topActivity;   
    	SharedPreferences sp = context.getSharedPreferences("smsdialogvalue", Context.MODE_WORLD_READABLE);
    	String settingRemind = sp.getString("smsdialogvalue", "true");
    	if (settingRemind.equals("false"))  {
    		return ;
    	}
        if(cn.getClassName().equals("com.lewa.PIM.mms.ui.ComposeMessageActivity")){
        	return;
        }*/
        SharedPreferences mmsActionSp = context.getSharedPreferences("mmsaction",Context.MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor=mmsActionSp.edit();
        if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")){
            DestktopMessageActivity.setActionString("android.provider.Telephony.WAP_PUSH_RECEIVED");
        	//editor.putString("action", "android.provider.Telephony.WAP_PUSH_RECEIVED");
        }else if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            //DestktopMessageActivity.setActionString("android.provider.Telephony.SMS_RECEIVED");
            //editor.putString("action", "android.provider.Telephony.SMS_RECEIVED");
    	}else if (intent.getAction().equals("com.lewa.PIM.IM.MESSAGE_RECEIVED")){
            DestktopMessageActivity.setActionString("com.lewa.PIM.IM.MESSAGE_RECEIVED");
    	}
        else {
    	    DestktopMessageActivity.setActionString(null);
        }
        
    	//editor.commit();
        /*Intent i = new Intent(context, DestktopMessageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i); */
    	
    } 
}
