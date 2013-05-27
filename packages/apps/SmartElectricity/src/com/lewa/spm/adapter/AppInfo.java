package com.lewa.spm.adapter;

import android.graphics.drawable.Drawable;

public class AppInfo implements Comparable<AppInfo>{
	public Drawable icon;
	public String label;
	public String packageName;
	public Double powerUsage;
	public Integer uid;

    public int compareTo(AppInfo another) {
        return new Double(another.powerUsage-this.powerUsage).intValue();
    }
}
