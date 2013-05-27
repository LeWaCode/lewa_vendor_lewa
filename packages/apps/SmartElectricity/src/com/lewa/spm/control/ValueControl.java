package com.lewa.spm.control;

import com.lewa.spm.control.intf.ValueIntf;

import android.content.Context;

public abstract class ValueControl extends ControlDev implements ValueIntf{

	public ValueControl(int mode, Context mContext) {
		super(mode, mContext);
	}

	public abstract void adjust(int value) ;

}
