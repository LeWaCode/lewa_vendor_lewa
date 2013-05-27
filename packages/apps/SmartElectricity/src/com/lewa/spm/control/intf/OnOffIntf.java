package com.lewa.spm.control.intf;

public interface OnOffIntf {
	public void change(boolean closeOrOpen);	
	public void change();
	public abstract boolean isOnOff();
}
