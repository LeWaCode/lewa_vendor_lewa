package com.lewa.labi.impl;

import java.util.HashMap;

import com.lewa.labi.intf.Result;

public class ResultImpl implements Result {
	private int type = -1;

	public ResultImpl(int type) {
		super();
		
		this.type = type;
	}

	private HashMap<Object, Object> items = new HashMap<Object, Object>();

	public boolean addItem(Object name, Object value) {
	    items.put(name, value);
	    return true;
	}
	
	public Object getItem(Object name) {
	    return items.get(name);
	}
	
	public int getType() {
		return type;
	}

	public void clearAll() {
		items.clear();
	}
}
