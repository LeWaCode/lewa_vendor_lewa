/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.spm.control;

import android.content.ContentResolver;

public class AutoSync extends SwitchControl{

	public AutoSync(int mode) {
		super(mode);
	}


	public void change(boolean closeOrOpen) {
		ContentResolver.setMasterSyncAutomatically(closeOrOpen);
	}

	public boolean isOnOff() {
		return ContentResolver.getMasterSyncAutomatically();
	}

}
