package com.lewa.store.model;

import android.os.Handler;

public abstract class AbsException {

	public abstract void setHandler(Handler handler);
	public abstract void setErrorMsg(String error);
	public abstract void sendMessage(int what,Object obj);
	public abstract void obtainMessage();
}
