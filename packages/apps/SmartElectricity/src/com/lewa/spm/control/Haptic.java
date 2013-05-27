/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.spm.control;

import android.content.Context;
import android.provider.Settings;

public class Haptic extends SwitchControl {

	public Haptic(int mode , Context mContext) {
		super(mode, mContext);
	}

	public void change(boolean closeOrOpen) {
		Settings.System.putInt(mContext.getContentResolver(),
				Settings.System.HAPTIC_FEEDBACK_ENABLED, closeOrOpen ? 1 : 0);
	}

	public boolean isOnOff() {
		return (Settings.System.getInt(mContext.getContentResolver(),
				Settings.System.HAPTIC_FEEDBACK_ENABLED, 0) == 1);
	}
}
