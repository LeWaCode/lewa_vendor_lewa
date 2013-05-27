package com.lewa.spm.charging;

import com.lewa.spm.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

public class ChargingAnimation {
	AnimationDrawable chargingAnimationDrawable;
	Drawable batteryDrawable;
	Context mContext;

	public ChargingAnimation(Context ctx) {
		mContext = ctx;
	}

	public AnimationDrawable getAnimationBaseOnLevel(int level) {
		if (level < 10) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging);
		} else if ((level >= 10) && (level < 20)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_10);
		} else if ((level >= 20) && (level < 30)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_20);
		} else if ((level >= 30) && (level < 40)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_30);
		} else if ((level >= 40) && (level < 50)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_40);
		} else if ((level >= 50) && (level < 60)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_50);
		} else if ((level >= 60) && (level < 70)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_60);
		} else if ((level >= 70) && (level < 80)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_70);
		} else if ((level >= 80) && (level < 90)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_80);
		} else if ((level >= 90) && (level < 100)) {
			chargingAnimationDrawable = (AnimationDrawable) mContext
					.getResources().getDrawable(R.anim.battery_charging_90);
		}
		return chargingAnimationDrawable;
	}

	public Drawable getDrawableBasedOnLevel(int level) {
		if (level < 5) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_0_bg);
		} else if ((level >= 5) && (level < 10)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_10_red_bg);
		} else if ((level >= 10) && (level <= 20)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_20_red_bg);
		} else if ((level > 20) && (level < 35)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_30_bg);
		} else if ((level >= 35) && (level < 45)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_40_bg);
		} else if ((level >= 45) && (level < 55)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_50_bg);
		} else if ((level >= 55) && (level < 65)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_60_bg);
		} else if ((level >= 65) && (level < 75)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_70_bg);
		} else if ((level >= 75) && (level < 85)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_80_bg);
		} else if ((level >= 85) && (level < 98)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_90_bg);
		} else if ((level >= 98) && (level <= 100)) {
			batteryDrawable = mContext.getResources().getDrawable(
					R.drawable.spm_battery_charging_100_bg);
		}
		return batteryDrawable;
	}

}
