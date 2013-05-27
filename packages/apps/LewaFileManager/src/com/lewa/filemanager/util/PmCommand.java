/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.util;

import android.content.Context;
import android.content.pm.IPackageManager;
import android.os.ServiceManager;
import dalvik.system.PathClassLoader;

/**
 *
 * @author Administrator
 */
public class PmCommand {

    public static final String SetInstallLocation = "setInstallLocation";
    public static final int InstallLocation_System_Decide = 0;
    public static final int InstallLocation_Internal = 1;
    public static final int InstallLocation_External = 2;
 public static int installLocation = -1;
    public static void exec(int location, Context context) {
        try {
            IPackageManager mPm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            mPm.setInstallLocation(location);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
