package com.lewa.labi.intf;

public interface Result {
	public static final int TYPE_SYNC = 0;
	public static final int TYPE_RECVY = 1;
	public boolean addItem(Object name, Object value);
	public Object getItem(Object name);
	public int getType();
	public void clearAll();
}
