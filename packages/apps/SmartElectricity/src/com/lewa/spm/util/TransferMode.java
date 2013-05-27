package com.lewa.spm.util;

import com.lewa.spm.R;

import android.content.Context;

public class TransferMode {
	/**
	 * based on the mode, transfer the mode constant to mode String (R.string......)
	 * @param type
	 * @return
	 */
	public static String consTransferMode(Context context, int typeId){
		String modeGetString = null;
		switch (typeId) {
		case Constants.SPM_MODE_OUT_ID:
			modeGetString = context.getResources().getString(R.string.spm_mode_ordinary);
			break;
		case Constants.SPM_MODE_LONG_ID:
			modeGetString = context.getResources().getString(R.string.spm_mode_standby);
			break;
		case Constants.SPM_MODE_ALARM_ID:
			modeGetString = context.getResources().getString(R.string.spm_alarm_clock);
			break;

		default:
			break;
		}
		return modeGetString;
	}
}
