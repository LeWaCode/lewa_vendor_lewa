package com.lewa.spm.control;

import com.lewa.spm.control.intf.OnOffIntf;
import com.lewa.spm.control.intf.ValueIntf;

import android.content.Context;

public abstract class SelectControl extends ControlDev implements OnOffIntf, ValueIntf {
	public SelectControl(int mode) {
		super(mode);
	}

	public SelectControl(int mode, Context mContext) {
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
