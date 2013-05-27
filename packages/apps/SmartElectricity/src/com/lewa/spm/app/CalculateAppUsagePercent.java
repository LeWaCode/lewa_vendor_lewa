package com.lewa.spm.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.internal.os.PowerProfile;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.app.IBatteryStats;
import android.content.Context;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.BatteryStats.Uid;
import android.os.Parcel;
import android.util.Log;
import android.util.SparseArray;

import com.lewa.spm.util.Constants;
import com.lewa.spm.util.TimeUtils;

public class CalculateAppUsagePercent {
	IBatteryStats mBatteryInfo;
	BatteryStatsImpl mStats;
	GetRunningApp appRunning;
	private PowerProfile mPowerProfile;

	/** Queue for fetching name and icon for an application */
	private ArrayList<String> mRequestQueue = new ArrayList<String>();
	private final List<BatterySipper> mUsageList = new ArrayList<BatterySipper>();
	List<HashMap<String, Object>> mAppUsageList;
	HashMap<String, Object> mAppUsagemap;

	private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;
	public static final int WIFI_UID = 1010;
	public static final int BLUETOOTH_GID = 2000;

	private double mMaxPower = 1;
	private double mTotalPower;
	private double mWifiPower;
	private double mBluetoothPower;
	public String mTimeOutOfFullPower = null;

	// How much the apps together have left WIFI running.
	private long mAppWifiRunning;

	Context mContext;

	public CalculateAppUsagePercent(Context mContext) {
		super();
		this.mContext = mContext;
		mPowerProfile = new PowerProfile(mContext);
		appRunning = new GetRunningApp(mContext);
		// mRequestQueue = (ArrayList<String>) appRunning.;
		mRequestQueue = null;
	}

	public Object onRetainNonConfigurationInstance() {
		return mStats;
	}

	public List<HashMap<String, Object>> refreshStats() {
		mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager
				.getService("batteryinfo"));
		if (mStats == null) {
			load();
		}
		mMaxPower = 0;
		mTotalPower = 0;
		mWifiPower = 0;
		mBluetoothPower = 0;
		mAppWifiRunning = 0;
		mUsageList.clear();
		mAppUsageList = new ArrayList<HashMap<String, Object>>();
		if (mAppUsageList.size() != 0) {
			mAppUsageList.clear();
		}
		processAppUsage();
		processMiscUsage();
		Collections.sort(mUsageList);
		for (BatterySipper sipper : mUsageList) {
			HashMap<String, Object>  appUsagemap= new HashMap<String, Object>();
			double percentOfTotal = ((sipper.getSortValue() / mTotalPower) * 100);
			sipper.percent = percentOfTotal;
			appUsagemap.put(String.valueOf(sipper.uidObj.getUid()),
					percentOfTotal);
			mAppUsageList.add(appUsagemap);
		}
		return mAppUsageList;
	}

	private void processAppUsage() {
		SensorManager sensorManager = (SensorManager) mContext
				.getSystemService(Context.SENSOR_SERVICE);
		final int which = mStatsType;
		final int speedSteps = mPowerProfile.getNumSpeedSteps();
		final double[] powerCpuNormal = new double[speedSteps];
		final long[] cpuSpeedStepTimes = new long[speedSteps];
		for (int p = 0; p < speedSteps; p++) {
			powerCpuNormal[p] = mPowerProfile.getAveragePower(
					PowerProfile.POWER_CPU_ACTIVE, p);
		}
		final double averageCostPerByte = getAverageDataCost();
		long uSecTime = mStats.computeBatteryRealtime(
				SystemClock.elapsedRealtime() * 1000, mStatsType);
		updateStatsPeriod(uSecTime);
		mTimeOutOfFullPower = TimeUtils.formatElapsedTime(mContext,
				uSecTime / 1000);// calculate the time of Out of Pull Power;
		SparseArray<? extends Uid> uidStats = mStats.getUidStats();
		final int NU = uidStats.size();
		for (int iu = 0; iu < NU; iu++) {
			Uid u = uidStats.valueAt(iu);
			double power = 0;
			double highestDrain = 0;
			String packageWithHighestDrain = null;
			Map<String, ? extends BatteryStats.Uid.Proc> processStats = u
					.getProcessStats();
			long cpuTime = 0;
			long cpuFgTime = 0;
			long wakelockTime = 0;
			long gpsTime = 0;
			if (processStats.size() > 0) {
				// Process CPU time
				for (Map.Entry<String, ? extends BatteryStats.Uid.Proc> ent : processStats
						.entrySet()) {
					Uid.Proc ps = ent.getValue();
					final long userTime = ps.getUserTime(which);
					final long systemTime = ps.getSystemTime(which);
					final long foregroundTime = ps.getForegroundTime(which);
					cpuFgTime += foregroundTime * 10; // convert to millis
					final long tmpCpuTime = (userTime + systemTime) * 10; // convert
																			// to
																			// millis
					int totalTimeAtSpeeds = 0;
					// Get the total first
					for (int step = 0; step < speedSteps; step++) {
						cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(
								step, which);
						totalTimeAtSpeeds += cpuSpeedStepTimes[step];
					}
					if (totalTimeAtSpeeds == 0)
						totalTimeAtSpeeds = 1;
					// Then compute the ratio of time spent at each speed
					double processPower = 0;
					for (int step = 0; step < speedSteps; step++) {
						double ratio = (double) cpuSpeedStepTimes[step]
								/ totalTimeAtSpeeds;
						processPower += ratio * tmpCpuTime
								* powerCpuNormal[step];
					}
					cpuTime += tmpCpuTime;
					power += processPower;
					if (packageWithHighestDrain == null
							|| packageWithHighestDrain.startsWith("*")) {
						highestDrain = processPower;
						packageWithHighestDrain = ent.getKey();
					} else if (highestDrain < processPower
							&& !ent.getKey().startsWith("*")) {
						highestDrain = processPower;
						packageWithHighestDrain = ent.getKey();
					}
				}
			}
			if (cpuFgTime > cpuTime) {
				cpuTime = cpuFgTime; // Statistics may not have been gathered
										// yet.
			}
			power /= 1000;

			// Process wake lock usage
			Map<String, ? extends BatteryStats.Uid.Wakelock> wakelockStats = u
					.getWakelockStats();
			for (Map.Entry<String, ? extends BatteryStats.Uid.Wakelock> wakelockEntry : wakelockStats
					.entrySet()) {
				Uid.Wakelock wakelock = wakelockEntry.getValue();
				// Only care about partial wake locks since full wake locks
				// are canceled when the user turns the screen off.
				BatteryStats.Timer timer = wakelock
						.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
				if (timer != null) {
					wakelockTime += timer.getTotalTimeLocked(uSecTime, which);
				}
			}
			wakelockTime /= 1000; // convert to millis

			// Add cost of holding a wake lock
			power += (wakelockTime * mPowerProfile
					.getAveragePower(PowerProfile.POWER_CPU_AWAKE)) / 1000;

			// Add cost of data traffic
			long tcpBytesReceived = u.getTcpBytesReceived(mStatsType);
			long tcpBytesSent = u.getTcpBytesSent(mStatsType);
			power += (tcpBytesReceived + tcpBytesSent) * averageCostPerByte;

			// Add cost of keeping WIFI running.
			long wifiRunningTimeMs = u.getWifiRunningTime(uSecTime, which) / 1000;
			mAppWifiRunning += wifiRunningTimeMs;
			power += (wifiRunningTimeMs * mPowerProfile
					.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;

			// Process Sensor usage
			Map<Integer, ? extends BatteryStats.Uid.Sensor> sensorStats = u
					.getSensorStats();
			for (Map.Entry<Integer, ? extends BatteryStats.Uid.Sensor> sensorEntry : sensorStats
					.entrySet()) {
				Uid.Sensor sensor = sensorEntry.getValue();
				int sensorType = sensor.getHandle();
				BatteryStats.Timer timer = sensor.getSensorTime();
				long sensorTime = timer.getTotalTimeLocked(uSecTime, which) / 1000;
				double multiplier = 0;
				switch (sensorType) {
				case Uid.Sensor.GPS:
					multiplier = mPowerProfile
							.getAveragePower(PowerProfile.POWER_GPS_ON);
					gpsTime = sensorTime;
					break;
				default:
					android.hardware.Sensor sensorData = sensorManager
							.getDefaultSensor(sensorType);
					if (sensorData != null) {
						multiplier = sensorData.getPower();
					}
				}
				power += (multiplier * sensorTime) / 1000;
			}

			// Add the app to the list if it is consuming power
			BatterySipper app = new BatterySipper(mContext, mRequestQueue,
					null, packageWithHighestDrain, 0, u, new double[] { power });
			app.cpuTime = cpuTime;
			app.gpsTime = gpsTime;
			app.wifiRunningTime = wifiRunningTimeMs;
			app.cpuFgTime = cpuFgTime;
			app.wakeLockTime = wakelockTime;
			app.tcpBytesReceived = tcpBytesReceived;
			app.tcpBytesSent = tcpBytesSent;
			if (u.getUid() == WIFI_UID) {
			} else if (u.getUid() == BLUETOOTH_GID) {
			} else {
				mUsageList.add(app);
			}
			if (u.getUid() == WIFI_UID) {
				mWifiPower += power;
			} else if (u.getUid() == BLUETOOTH_GID) {
				mBluetoothPower += power;
			} else {
				if (power > mMaxPower)
					mMaxPower = power;
				mTotalPower += power;
			}
		}
	}

	private void addScreenUsage(long uSecNow) {
		double power = 0;
		long screenOnTimeMs = mStats.getScreenOnTime(uSecNow, mStatsType) / 1000;
		power += screenOnTimeMs
				* mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
		final double screenFullPower = mPowerProfile
				.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
		for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
			double screenBinPower = screenFullPower * (i + 0.5f)
					/ BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
			long brightnessTime = mStats.getScreenBrightnessTime(i, uSecNow,
					mStatsType) / 1000;
			power += screenBinPower * brightnessTime;
		}
		power /= 1000; // To seconds
		if (power > mMaxPower)
			mMaxPower = power;
		mTotalPower += power;
	}

	// private void addPhoneUsage(long uSecNow) {
	// long phoneOnTimeMs = mStats.getPhoneOnTime(uSecNow, mStatsType) / 1000;
	// double phoneOnPower =
	// mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE)
	// * phoneOnTimeMs / 1000;
	// if (phoneOnPower > mMaxPower) mMaxPower = phoneOnPower;
	// mTotalPower += phoneOnPower;
	// }
	//
	// private void addRadioUsage(long uSecNow) {
	// double power = 0;
	// final int BINS = BatteryStats.NUM_SIGNAL_STRENGTH_BINS;
	// long signalTimeMs = 0;
	// for (int i = 0; i < BINS; i++) {
	// long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, uSecNow,
	// mStatsType) / 1000;
	// power += strengthTimeMs / 1000
	// * mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i);
	// signalTimeMs += strengthTimeMs;
	// }
	// long scanningTimeMs = mStats.getPhoneSignalScanningTime(uSecNow,
	// mStatsType) / 1000;
	// power += scanningTimeMs / 1000 * mPowerProfile.getAveragePower(
	// PowerProfile.POWER_RADIO_SCANNING);
	// if (power > mMaxPower) mMaxPower = power;
	// mTotalPower += power;
	// }

	private void addWiFiUsage(long uSecNow) {
		long onTimeMs = mStats.getWifiOnTime(uSecNow, mStatsType) / 1000;
		long runningTimeMs = mStats.getGlobalWifiRunningTime(uSecNow,
				mStatsType) / 1000;
		runningTimeMs -= mAppWifiRunning;
		if (runningTimeMs < 0)
			runningTimeMs = 0;
		double wifiPower = (onTimeMs * 0 /* TODO */
				* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON) + runningTimeMs
				* mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON)) / 1000;
		double power = wifiPower + mWifiPower;
		if (power > mMaxPower)
			mMaxPower = power;
		mTotalPower += power;
	}

	private void addIdleUsage(long uSecNow) {
		long idleTimeMs = (uSecNow - mStats
				.getScreenOnTime(uSecNow, mStatsType)) / 1000;
		double idlePower = (idleTimeMs * mPowerProfile
				.getAveragePower(PowerProfile.POWER_CPU_IDLE)) / 1000;
		if (idlePower > mMaxPower)
			mMaxPower = idlePower;
		mTotalPower += idlePower;
	}

	private void addBluetoothUsage(long uSecNow) {
		long btOnTimeMs = mStats.getBluetoothOnTime(uSecNow, mStatsType) / 1000;
		double btPower = btOnTimeMs
				* mPowerProfile
						.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
				/ 1000;
		int btPingCount = mStats.getBluetoothPingCount();
		btPower += (btPingCount * mPowerProfile
				.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD)) / 1000;
		double power = btPower + mBluetoothPower;
		if (power > mMaxPower)
			mMaxPower = power;
		mTotalPower += power;
	}

	private double getAverageDataCost() {
		final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
										// system
		final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from
										// system
		final double WIFI_POWER = mPowerProfile
				.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600;
		final double MOBILE_POWER = mPowerProfile
				.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600;
		final long mobileData = mStats.getMobileTcpBytesReceived(mStatsType)
				+ mStats.getMobileTcpBytesSent(mStatsType);
		final long wifiData = mStats.getTotalTcpBytesReceived(mStatsType)
				+ mStats.getTotalTcpBytesSent(mStatsType) - mobileData;
		final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000;
		final long mobileBps = radioDataUptimeMs != 0 ? mobileData * 8 * 1000
				/ radioDataUptimeMs : MOBILE_BPS;

		double mobileCostPerByte = MOBILE_POWER / (mobileBps / 8);
		double wifiCostPerByte = WIFI_POWER / (WIFI_BPS / 8);
		if (wifiData + mobileData != 0) {
			return (mobileCostPerByte * mobileData + wifiCostPerByte * wifiData)
					/ (mobileData + wifiData);
		} else {
			return 0;
		}
	}

	private void processMiscUsage() {
		final int which = mStatsType;
		long uSecTime = SystemClock.elapsedRealtime() * 1000;
		final long uSecNow = mStats.computeBatteryRealtime(uSecTime, which);
		final long timeSinceUnplugged = uSecNow;
		addScreenUsage(uSecNow);
		addWiFiUsage(uSecNow);
		// addRadioUsage(uSecNow);
		// addPhoneUsage(uSecNow);
		addBluetoothUsage(uSecNow);
		addIdleUsage(uSecNow); // Not including cellular idle power
	}

	private void load() {
		try {
			byte[] data = mBatteryInfo.getStatistics();
			Parcel parcel = Parcel.obtain();
			parcel.unmarshall(data, 0, data.length);
			parcel.setDataPosition(0);
			mStats = com.android.internal.os.BatteryStatsImpl.CREATOR
					.createFromParcel(parcel);
			mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
		} catch (RemoteException e) {
			Log.e("Tt", "RemoteException:", e);
		}
	}

	static final int MSG_UPDATE_NAME_ICON = 1;

	// Handler mHandler = new Handler() {
	//
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case MSG_UPDATE_NAME_ICON:
	// BatterySipper bs = (BatterySipper) msg.obj;
	// break;
	// }
	// super.handleMessage(msg);
	// }
	// };

	public String updateStatsPeriod(long duration) {
		String durationString = TimeUtils.formatElapsedTime(mContext,
				duration / 1000);
		Intent mIntent = new Intent(Constants.SP_INTENT_MONTAGE_TIME);
		mIntent.putExtra(Constants.SPM_MONTAGE_TIME, durationString);
		mContext.sendBroadcast(mIntent);
		return durationString;
	}

	public void release() {
		mUsageList.clear();
		if (mAppUsageList != null) {
			for (HashMap<String, Object> hashMap : mAppUsageList) {
				hashMap.clear();
				hashMap = null;
			}
			mAppUsageList.clear();
			mAppUsageList = null;
		}
	}
}
