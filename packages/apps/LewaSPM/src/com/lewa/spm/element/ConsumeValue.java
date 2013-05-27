package com.lewa.spm.element;

public class ConsumeValue {

	public static final double PowerPoolmAh = 5600.0D;
	
	public static final double Suspend = 1.143333333333333D;	
	public static final double Audio = 1.833333333333333D;
	public static final double Video = 2.5D;
	public static final double Game = 3.0D;
	public static final double Read = 1.75D;
	public static final double Browsing = 0.5333333333333333D;	
	public static final double Dailing = 17.5D;
	
	public static final double IdleWeight = 1.143333333333333D;
	public static final double GpsWeight = 2.576666666666667D;
	public static final double BlueToothWeight = 0.6666666666666666D;
	public static final double HapticWeight = 3.333333333333334D;
	public static final double TrafficWeight = 1.0D;
	public static final double WifiopenWeight = 1.408333333333333D;
	public static final double CPUWeight = 1.0D;
	public static final double DataWeight = 2.0D;	
	
	public static final double AutoLightRate = 0.33D;
	public static final int BenchmarkSwitchNormal = 2;
	public static final int BenchmarkSwitchSave = 5;
	
	public static final double MobileDataWeight = 1.7D;	
	
	public static final double ScreenLightFull = 6.9D;
	
	public static final double estimateDiscountTrafficrating = 0.95D;
	
	public static final int MODE_NULL = 0;
	public static final int MODE_AIR = 1;
	public static final int MODE_NORMAL = 2;	
	public static final int MODE_INTEL_TIME = 3;
	public static final int MODE_INTEL_POWER = 4;
	public static final int MODE_OPT_NORMAL = 5;
	public static final int MODE_OPT_SUPER = 6;
	
	public static final String ACTION_UPDATE_UI_ENERGY = "android.intent.action.updateuiforenergy";
	public static final String ACTION_UPDATE_UI_INTELS_TIME = "android.intent.action.updateuiforintelstime";
	public static final String ACTION_UPDATE_UI_INTELS_POWER = "android.intent.action.updateuiforintelspower";
	
	public static final String PARAM_ENERGY_UPDATE = "param_energy_update";
	
	public static final String PARAM_INTEL_TIME_START = "param_intel_time_start";	
	public static final String PARAM_INTEL_POWER_START = "param_intel_power_start";
	
	public static final int CLEAR_BIT = 0;
	public static final int SET_BIT = 1;
	
	public static final int OPT_DATA_ID = 0;
	public static final int OPT_WLAN_ID = 1;
	public static final int OPT_BLUETOOTH_ID = 2;
	public static final int OPT_BRIGHT_ID = 3;
	public static final int OPT_TIMEOUT_ID = 4;
	public static final int OPT_GPS_ID = 5;
	public static final int OPT_TOUCH_ID = 6;
	
}
