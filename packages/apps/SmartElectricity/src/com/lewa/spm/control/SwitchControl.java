package com.lewa.spm.control;

import com.lewa.spm.control.intf.OnOffIntf;

import android.content.Context;

public abstract class SwitchControl extends ControlDev implements OnOffIntf{

	public SwitchControl(int mode) {
		super(mode);
	}

	public SwitchControl(int mode, Context mContext) {
		super(mode, mContext);
	}
	
	
	public void change() {
		if(isOnOff()){
			change(false);
		}else{
			change(true);
		}
	}

}
