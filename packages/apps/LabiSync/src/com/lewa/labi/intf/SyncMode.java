package com.lewa.labi.intf;

import com.lewa.labi.impl.SyncModeImpl;

public abstract class SyncMode {
	public static final int COMPANION_ID_LABI = 0;
	private static SyncMode impl;
	
	public static SyncMode getInstance() {
		if(impl == null) {
			impl = new SyncModeImpl();
		}
		return impl;
	}
	public abstract boolean isAuto(int companionid);

	public abstract void setMode(int companionId, boolean isAuto);
}
