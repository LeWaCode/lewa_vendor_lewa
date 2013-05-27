package com.lewa.spm.utils;

import com.lewa.spm.device.DevStatus;
import com.lewa.spm.element.ConsumeValue;

import android.content.Context;
import android.os.PowerManager;

public class CalcUtils {

	Context mContext;
	private static CalcUtils _instance = null;

	private CalcUtils(Context paramContext) {
		this.mContext = paramContext;
	}

	public static CalcUtils getInstance(Context paramContext) {
		if (_instance == null)
			_instance = new CalcUtils(paramContext);
		return _instance;
	}

	/**
	 * Idel Time
	 * 
	 * @param mCurRunningMode
	 * @return
	 */
	public double Idle(int mCurRunningMode) {
		double d = getCurrentExtraPowerConsume(mCurRunningMode) / 2.0D;
		return ConsumeValue.PowerPoolmAh / (ConsumeValue.IdleWeight + d);
	}

	/**
	 * Music Time
	 * 
	 * @param mCurRunningMode
	 * @return
	 */
	public double Audio(int mCurRunningMode) {
		return ConsumeValue.PowerPoolmAh
				/ (ConsumeValue.Audio + getCurrentExtraPowerConsume(mCurRunningMode));
	}

	/**
	 * Call Time
	 * 
	 * @param mCurRunningMode
	 * @return
	 */
	public double Dialing(int mCurRunningMode) {
		return ConsumeValue.PowerPoolmAh
				/ (ConsumeValue.Dailing + getCurrentExtraPowerConsume(mCurRunningMode));
	}

	/**
	 * Web Time
	 * 
	 * @param mCurRunningMode
	 * @return
	 */
	public double Browsing(int mCurRunningMode) {
		return ConsumeValue.PowerPoolmAh
				/ (ConsumeValue.Browsing + (getCurrentExtraPowerConsume(mCurRunningMode) + getCurrentBackLightExtra()));
	}

	/**
	 * Video Time
	 * 
	 * @param mCurRunningMode
	 * @return
	 */
	public double Video(int mCurRunningMode) {
		return ConsumeValue.PowerPoolmAh
				/ (ConsumeValue.Video + getCurrentExtraPowerConsume(mCurRunningMode));
	}

	/**
	 * Life Time
	 * 
	 * @param mCurRunningMode
	 * @return
	 */
	public double Lift(int mCurRunningMode) {
		return ConsumeValue.PowerPoolmAh
				/ getCurrentExtraPowerConsume(mCurRunningMode);
	}

	/**
	 * TODO some bug here
	 * 
	 * @return
	 */
	private double getCurrentBackLightExtra() {
		// PreferenceStatus localPreferenceStatus =
		// PreferenceStatus.getInstance(paramContext);
		double d;
		// if (localPreferenceStatus.isScreenAuto(paramContext))
		d = 2.277D;
		// else
		// d = ConsumeValue.ScreenLightFull *
		// localPreferenceStatus.curScreenBrightness(paramContext) / 255.0D;
		return d;
	}

	/**
	 * 
	 * @param mContext
	 * @param mCurRunningMode
	 * @return
	 */
	private double getCurrentExtraPowerConsume(int mCurRunningMode) {

		double d = ConsumeValue.Suspend;
		switch (mCurRunningMode) {

		case ConsumeValue.MODE_NORMAL:
			d = getStandbyPowerConsume(d);
			break;
		case ConsumeValue.MODE_AIR:
			d = getSleepPowerConsume(d);
			break;
		case ConsumeValue.MODE_OPT_NORMAL:
			d = getOptNormalPowerConsume(d);
			break;
		case ConsumeValue.MODE_OPT_SUPER:
			d = getOptSuperPowerConsume(d);
			break;
		default:// Reserved
			break;
		}
		return d;
	}

	/**
	 * GPRS WLAN BT BRIGHT 30% TIMEOUT 15S GPS FELLBACK
	 * 
	 * self defined mode
	 * 
	 * @param d
	 * @return
	 */
	private double getStandbyPowerConsume(double d) {
		if (DevStatus.getInstance(mContext).getMobileDataStatus())
			d += ConsumeValue.DataWeight;// 1.7D;		
		if (DevStatus.getInstance(mContext).getWifiStatus())
			d += ConsumeValue.WifiopenWeight;// 1.408333333333333D;
		if (DevStatus.getInstance(mContext).getBTStatus())
			d += ConsumeValue.BlueToothWeight;// 0.6666666666666666D;
		if (DevStatus.getInstance(mContext).getGpsStatus())
			d += ConsumeValue.GpsWeight;// 2.576666666666667D;
		if (DevStatus.getInstance(mContext).getHapticFb())// Like GPRS
			d += ConsumeValue.HapticWeight;// 3.333333333333334D;
		d += ConsumeValue.CPUWeight;// 1.7D;
		d += ConsumeValue.Read;
		return d;
	}

	/**
	 * air mode,we closed all device.
	 * 
	 * @param d
	 * @return
	 */
	private double getSleepPowerConsume(double d) {
		// if (DevStatus.getInstance(mContext).getBTStatus())
		// d += ConsumeValue.BlueToothWeight;// 0.6666666666666666D;
		// if (DevStatus.getInstance(mContext).getSyncStatus())
		// d += ConsumeValue.SyncWeight;// 3.333333333333334D;
		// if (DevStatus.getInstance(mContext).getGpsStatus())
		// d += ConsumeValue.GpsWeight;// 2.576666666666667D;
		// if (DevStatus.getInstance(mContext).getWifiStatus())
		// d += ConsumeValue.WifiopenWeight;// 1.408333333333333D;
		// if (!DevStatus.getInstance(mContext).getGprsStatus())
		d += ConsumeValue.CPUWeight;// 1.7D;
		d += ConsumeValue.Read;
		return d;
	}
	
	private double getOptNormalPowerConsume(double d){
		d += ConsumeValue.DataWeight;// 1.7D;
		d += ConsumeValue.HapticWeight;// 3.333333333333334D;
		d += ConsumeValue.CPUWeight;// 1.7D;
		d += ConsumeValue.Read;
		return d;
	}
	
	private double getOptSuperPowerConsume(double d){
		d += ConsumeValue.CPUWeight;// 1.7D;
		d += ConsumeValue.Read;
		return d;
	}

	public boolean isScreenOn() {
		return ((PowerManager) mContext.getSystemService("power")).isScreenOn();
	}

	/**
	 * 
	 * double d4 = XMUtils.WebBrowsing(this, bool1); arrayOfString[2] =
	 * (XMUtils.toTimeFormatHours(Math.abs(d4 - XMUtils.WebBrowsing(this,
	 * bool4)), d1) + ")"); StringBuilder localStringBuilder4 = new
	 * StringBuilder("(").append(str);
	 * 
	 * @param paramDouble1
	 * @param battLevel
	 *            Battery percent.
	 * @return
	 */
	public String toTimeFormatHours(double paramDouble1, double battLevel) {

		double d = paramDouble1 * battLevel;

		double dValue = 0D;
		String str = "";

		if (d > 6.0D) {
			dValue = Double.valueOf(d / 60.0D);
			str = String.format("%.1f", dValue) + "Hours";
		} else {
			dValue = Double.valueOf(d);
			str = String.format("%.1f", dValue) + "Minutes";
		}

		return str;
	}

	/**
	 * import Minutes which calculate. arrayOfString[0] =
	 * XMUtils.toTimeFotmat(XMUtils.Dialing(this, modeNum), battLevel);
	 * 
	 * @param paramDouble1
	 *            CalcUtils.Dialing(this, bool) etc. it is minutes.
	 * @param battLevel
	 *            Battery percent.
	 * @return
	 */
	public String toTimeFotmat(double paramDouble1, double battLevel) {
		int i = (int) Math.round(paramDouble1 * battLevel);

		String str = "";

		if (i > 60)
			str = i / 60 + "Hours" + i % 60 + "Minutes";
		else
			str = i + "Minutes";

		return str;
	}

	public int getHoursFromTime(double paramDouble1, double battLevel) {
		return ((int) Math.round(paramDouble1 * battLevel)) / 60;
	}

	public int getMinutesFromString(double paramDouble1, double battLevel) {
		return ((int) Math.round(paramDouble1 * battLevel)) % 60;
	}

	/**
	 * (Reserved Method)
	 * 
	 * @param mFromMode
	 *            this value from ConsumeValue there.
	 * @param mToMode
	 *            this is also from ConsumeValue for add or del.
	 * @return int for title change
	 */
	public int getValueBetween(int mFromMode, int mToMode) {
		return (mToMode - mFromMode) * 35;
	}

}
