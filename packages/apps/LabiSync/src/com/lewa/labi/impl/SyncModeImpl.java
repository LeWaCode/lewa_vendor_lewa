package com.lewa.labi.impl;

import java.util.HashMap;
import java.util.Map;

import com.lewa.labi.intf.CompanionNotFoundException;
import com.lewa.labi.intf.SyncMode;

public  class SyncModeImpl extends SyncMode{
	public static Map<Integer, Boolean> thirdPartySyncMode = new HashMap<Integer, Boolean>();

	static {
		thirdPartySyncMode.put(COMPANION_ID_LABI, false);
	}

	public  boolean isAuto(int companionid) {
		if (!thirdPartySyncMode.containsKey(companionid)) {
			throw new CompanionNotFoundException("please check the input value or initialized value because param: "+companionid+" is invalid.");
		}
		return thirdPartySyncMode.get(companionid);
	}
	
	public  void setMode(int companionId,boolean isAuto) {
		thirdPartySyncMode.put(companionId, isAuto);
	}
}
