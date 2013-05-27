/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.uri;

import android.content.Context;
import com.lewa.app.filemanager.R;

/**
 *
 * @author Administrator
 */
public class NavigationConstants {

    public static String SDCARD;
    public static String CATEGORYHOME;
    public static final String SDCARD_PATH = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    public static String PRIVACYHOME;

    public static void init(Context activity) {
        SDCARD = activity.getString(R.string.sdcard);
        CATEGORYHOME = activity.getString(R.string.home);
        PRIVACYHOME = activity.getString(R.string.privacy_home);
    }
}
