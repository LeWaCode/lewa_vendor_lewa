/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.config;

import android.os.Build;
import com.lewa.filemanager.ds.sdcard.TypeFilter;

/**
 *
 * @author Administrator
 */
public class Config {

    public static Float image_filter_size = 0F;
    public static final int LOAD_CONST_START = 2;
    public static final int LOAD_CONST = 20;
    public static final int LIMIT = 500;
    public static boolean SDCARD_HIDE_OPTION = true;
    public static boolean ACCOUNT_HIDE_OPTION = true;
    public static boolean isLewaRom = Build.DISPLAY.toLowerCase().startsWith("lewa");
    public static boolean is4_0Lower = (Build.VERSION.SDK_INT < 14); //for media other type in database. 
    public static boolean hideInstallHints = false;
    public static final int UI_SCHEMA_UI2_0 = 1;
    
    public static int uiSChema = UI_SCHEMA_UI2_0;

    public static int getHiddenOption(boolean toHide) {
        return toHide ? TypeFilter.FILTER_REMOVE_HIDDEN : -1;
    }
}
