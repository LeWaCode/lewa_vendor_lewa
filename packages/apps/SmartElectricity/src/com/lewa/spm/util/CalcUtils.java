package com.lewa.spm.util;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.os.BatteryStats.Uid;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.telephony.SignalStrength;
import android.util.SparseArray;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import com.lewa.spm.mode.ModeSettings;
import com.lewa.spm.mode.ModeDevStatus;
import com.lewa.spm.util.Constants;
import com.lewa.spm.control.InquirState;
import com.lewa.spm.control.SwitchState;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import android.util.Log;

public class CalcUtils {

    private static final String TAG = "CalcUtils";
    
	Context mContext;
    BatteryStatsImpl mStatsImpl;
    IBatteryStats mBatteryInfo;
    
	private static CalcUtils _instance = null;
	
	private CalcUtils(Context ctx) {
		mContext = ctx;
        mBatteryInfo = IBatteryStats.Stub.asInterface(
                ServiceManager.getService("batteryinfo"));
        load();
	}

	public static CalcUtils getInstance(Context paramContext) {
		if (_instance == null)
			_instance = new CalcUtils(paramContext);
		return _instance;
	}

    private void load() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStatsImpl = com.android.internal.os.BatteryStatsImpl.CREATOR
                    .createFromParcel(parcel);
            mStatsImpl.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException:", e);
        }
    }
    
    private int getBatteryCapacity(){
        return 2050;
    }
    int getIdleConsume(){
        return 20+3*4;
    }
    int getFlyConsume(){
        return  7;
    }
    int getWifiOnConsume(){
        return  1;
    }
    int getWifiActiveConsume(){
        return  150;
    }
    int getBluetoothOnConsume(){
        return  1;
    }
    int getBluetoothActiveConsume(){
        return  1;
    }
    int getGPSActiveConsume(){
        return  55;
    }
    int getHapticActiveConsume(){
        return  150;
    }
    int getScreenConsume(){
        return 200;
    }
    int getTotalLevelSummary(){
        return 160;
    }
    int getDataConsume(){
        return 150;
    }
    private double getSavingPowerConsume(int mode) {
            ModeSettings ms=new ModeSettings(mContext,mode);
            ms.getSettings();
            InquirState iqr=new InquirState(mContext,mode);
            long uSecTime = SystemClock.elapsedRealtime() * 1000;
            long batteryTime=mStatsImpl.computeBatteryRealtime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
            int batteryCapacity=getBatteryCapacity();
            int idleConsume=getIdleConsume();
            int flyConsume=getFlyConsume();
            double allConsume=idleConsume;

            Log.i(TAG,"getSavingPowerConsume batteryTime="+batteryTime);
            Log.i(TAG,"getSavingPowerConsume allConsume init="+allConsume); 
			if ((ms.dataSetted==false)&&iqr.mobileDataState()){
                
                //estimation of 5M data everyday
			    allConsume+=1280.0/86400.0*getDataConsume();
			}
            Log.i(TAG,"getSavingPowerConsume allConsume data="+allConsume); 
			if ((ms.wifiSetted==false)&&iqr.wifiState()){
                long wifiontime=mStatsImpl.getWifiOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
                Log.i(TAG,"getSavingPowerConsume wifiontime="+wifiontime);
				allConsume+=getWifiOnConsume();
                //estimation of max 2G data everyday and average 300K every second
                allConsume+=((2.0*1024.0*1024.0/300.0)/86400.0)*getWifiActiveConsume();
            }
            Log.i(TAG,"getSavingPowerConsume allConsume wifi="+allConsume); 
			if ((ms.bluetoothSetted==false)&&iqr.bluetoothState()){
                long btontime=mStatsImpl.getBluetoothOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
                Log.i(TAG,"getSavingPowerConsume btontime="+btontime);
                //allConsume+=btontime/batteryTime*getBluetoothActiveConsume();
                allConsume+=getBluetoothOnConsume();
                allConsume+=getBluetoothActiveConsume();
			}
            Log.i(TAG,"getSavingPowerConsume allConsume bt="+allConsume); 
			if ((ms.gpsSetted==false)&&iqr.gpsState()){
                allConsume+=1280.0/86400.0*getGPSActiveConsume();
				
			}
            Log.i(TAG,"getSavingPowerConsume allConsume gps="+allConsume); 
			if ((ms.hapticSetted==false)&&iqr.hapticState()){
				allConsume+=1/1000.0*getHapticActiveConsume();
			}
            Log.i(TAG,"getSavingPowerConsume allConsume haptic="+allConsume); 
			if ((ms.autoSyncSetted==false)&&iqr.syncState()){
			    allConsume+=1280.0/86400.0*getDataConsume()/5;
			}
            Log.i(TAG,"getSavingPowerConsume allConsume autosync="+allConsume); 
            //
            long screenOntime=mStatsImpl.getScreenOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
            
			if (ms.brightnessSetted){ 
				allConsume+=2/24.0*(getScreenConsume()+getTotalLevelSummary()*iqr.lightOfScreenValue()/225.0);
			}else{
			   int screenLevel=iqr.lightOfScreenValue()<ms.brightnessValue?iqr.lightOfScreenValue():ms.brightnessValue;
			   allConsume+=2/24.0*(getScreenConsume()+getTotalLevelSummary()*screenLevel/225.0);
			}
            Log.i(TAG,"getSavingPowerConsume allConsume bright="+allConsume); 
			if (ms.flyModeSetted){
                long phoneOntime=mStatsImpl.getPhoneOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
                Log.i(TAG,"getSavingPowerConsume phoneOntime="+phoneOntime);
				allConsume-=(batteryTime-phoneOntime)/batteryTime*(getIdleConsume()-getFlyConsume());
			}
            Log.i(TAG,"getSavingPowerConsume allConsume="+allConsume);
		    return batteryCapacity/allConsume*60;
	}

    private double getUserPowerConsume() {
        ModeDevStatus mds=new ModeDevStatus(mContext,Constants.SPM_MODE_OUT_ID);
        mds.getStatus();
        InquirState iqr=new InquirState(mContext,Constants.SPM_MODE_OUT_ID);
        long uSecTime = SystemClock.elapsedRealtime() * 1000;
        long batteryTime=mStatsImpl.computeBatteryRealtime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
        int batteryCapacity=getBatteryCapacity();
        int idleConsume=getIdleConsume();
        int flyConsume=getFlyConsume();
        double allConsume=idleConsume;
        Log.i(TAG,"getUserPowerConsume allConsume init="+allConsume); 
        Log.i(TAG,"getUserPowerConsume batteryTime="+batteryTime);
		if (mds.dataOn){
			//estimation of 5M data everyday
			allConsume+=(1280.0/86400.0)*getDataConsume();
		}
        Log.i(TAG,"getUserPowerConsume allConsume data="+allConsume); 
		if (mds.wifiOn){
			long wifiontime=mStatsImpl.getWifiOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
            Log.i(TAG,"getUserPowerConsume wifiontime="+wifiontime);
			allConsume+=getWifiOnConsume();
            //estimation of max 2G data everyday and average 300K every second
            allConsume+=(((2.0*1024.0*1024.0)/300.0)/86400.0)*getWifiActiveConsume();
		}
        Log.i(TAG,"getUserPowerConsume allConsume wifi="+allConsume); 
		if (mds.bluetoothOn){
			long btontime=mStatsImpl.getBluetoothOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
            Log.i(TAG,"getUserPowerConsume btontime="+btontime);
            //allConsume+=btontime/batteryTime*getBluetoothActiveConsume();
            allConsume+=getBluetoothOnConsume();
            allConsume+=getBluetoothActiveConsume();
		}
        Log.i(TAG,"getUserPowerConsume allConsume bt="+allConsume); 
		if (mds.gpsOn){
			 allConsume+=(1280.0/86400.0)*getGPSActiveConsume();
		}
        Log.i(TAG,"getUserPowerConsume allConsume gps="+allConsume); 
		if (mds.hapticOn){
			allConsume+=1/1000.0*getHapticActiveConsume();
		}
        Log.i(TAG,"getUserPowerConsume allConsume haptic="+allConsume); 
		if (mds.autoSyncOn){
			//estimation 1M data need to sync at most
			allConsume+=1280.0/86400.0*getDataConsume()/5;
		}
        Log.i(TAG,"getUserPowerConsume allConsume autosync="+allConsume); 
        long screenOntime=mStatsImpl.getScreenOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
        Log.i(TAG,"getUserPowerConsume screenOntime="+screenOntime);
		if (mds.brightnessOn){
			allConsume+=2.0/24.0*(getScreenConsume()+getTotalLevelSummary()*iqr.lightOfScreenValue()/225.0);
		}else{
			allConsume+=2.0/24.0*(getScreenConsume()+getTotalLevelSummary()*mds.brightnessValue/225.0);
		}
        Log.i(TAG,"getUserPowerConsume allConsume bright="+allConsume); 
		if (mds.flyModeOn){
            long phoneOntime=mStatsImpl.getPhoneOnTime(uSecTime,BatteryStats.STATS_SINCE_UNPLUGGED);
            Log.i(TAG,"getUserPowerConsume phoneOntime="+phoneOntime);
			allConsume-=(batteryTime-phoneOntime)/batteryTime*(getIdleConsume()-getFlyConsume());
		}
        Log.i(TAG,"getUserPowerConsume allConsume="+allConsume);
		return batteryCapacity/allConsume*60;
	}


    public double Lift(int mode) {
            if(Constants.SPM_MODE_OUT_ID==mode){
                return getUserPowerConsume();
            }else{
                 return getSavingPowerConsume(mode);
            }

	}
	
	
	public int getHoursFromTime(double time, double battLevel) {
		return ((int) Math.round(time * battLevel)) / 60;
	}

	public int getMinutesFromString(double time, double battLevel) {
		return ((int) Math.round(time * battLevel)) % 60;
	}
	
}
