package com.lewa.face.util;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * The monitor is for sdcard status,just for mounted or unmounted
 * @author fulw
 *
 */
public class ThemeSDCardMonitor implements ThemeConstants{
    
    private Context mContext;
    private SDCardStatusListener mSdCardStatusListener;
    protected SDCardReceiver mSDCardReceiver;
    
    public ThemeSDCardMonitor(Context context){
        mContext = context;
        mSDCardReceiver = new SDCardReceiver();
    }
    
    public void addListener(SDCardStatusListener sdCardStatusListener){
        
        mSdCardStatusListener = sdCardStatusListener;
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        
        mContext.registerReceiver(mSDCardReceiver, intentFilter);
    }
    
    public void removeListener(){
        mContext.unregisterReceiver(mSDCardReceiver);
        mSdCardStatusListener = null;
    }
    
    private class SDCardReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            if(mSdCardStatusListener == null){
                return;
            }
            
            String action = intent.getAction();
            if(Intent.ACTION_MEDIA_MOUNTED.equals(action)){
                mSdCardStatusListener.onStatusChanged(MEDIA_MOUNTED);
            }else {
                mSdCardStatusListener.onStatusChanged(MEDIA_UNMOUNTED);
            }
        }
        
    }

    public interface SDCardStatusListener{
        
        public abstract void onStatusChanged(int sdcardStatus);
    }
    
}
