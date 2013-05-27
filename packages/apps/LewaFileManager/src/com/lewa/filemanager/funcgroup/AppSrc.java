/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.funcgroup;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author chenliang
 */
public class AppSrc {
    private static Map<String,String> packagesVersion = new TreeMap<String,String>(new Comparator<String>(){

        public int compare(String arg0, String arg1) {
            if (arg0 == null) {
                return 0;
            }
            return arg0.compareTo(arg1);
        }
        
    });
    public static void initInstalledApps(Context context) {
        packagesVersion.clear();
        PackageManager appPm = context.getPackageManager();
        List<PackageInfo> packages = appPm.getInstalledPackages(0);
        for (PackageInfo packageInfo : packages) {
            packagesVersion.put(packageInfo.packageName,packageInfo.versionName);
        }
    }

    public static Map<String, String> getPackagesVersion() {
        return packagesVersion;
    }

    
}
