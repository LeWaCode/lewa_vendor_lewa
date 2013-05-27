/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.spm.control;

import android.content.Context;

public class ControlDev {

	public Context mContext;
	private int mMode;

    
	public ControlDev(int mode) {
		mMode= mode;
	}

	public ControlDev(int mode, Context ctx) {
		mMode= mode;
		mContext = ctx;
	}

}
