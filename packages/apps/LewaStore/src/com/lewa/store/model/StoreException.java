package com.lewa.store.model;

import android.os.Handler;
import android.os.Message;

public class StoreException extends AbsException{
	
	private Handler mHandler;	
	private String errMsg;
	private Message message;
	
	@Override
	public void setHandler(Handler handler) {
		// TODO Auto-generated method stub
		this.mHandler=handler;
	}

	@Override
	public void obtainMessage() {
		// TODO Auto-generated method stub
		this.message= Message.obtain();
	}

	@Override
	public void sendMessage(int what, Object obj) {
		// TODO Auto-generated method stub
		message.what=what;
		message.obj=obj;
        mHandler.sendMessage(message);
	}	
	
	@Override
	public void setErrorMsg(String error) {
		// TODO Auto-generated method stub
		this.errMsg=error;
	}
}
