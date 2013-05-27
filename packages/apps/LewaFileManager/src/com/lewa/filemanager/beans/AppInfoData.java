package com.lewa.filemanager.beans;

import android.graphics.drawable.Drawable;

public class AppInfoData {

    public Drawable icon;
    public String name;
    public String pkgName;
    public String versionName;
    public String versionCode;

    public void setAppicon(Drawable icon) {
        this.icon = icon;
    }
}
