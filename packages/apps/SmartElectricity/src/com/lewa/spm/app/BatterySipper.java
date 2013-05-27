package com.lewa.spm.app;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.BatteryStats.Uid;

import java.util.ArrayList;


public class BatterySipper implements Comparable<BatterySipper> {
    final Context mContext;
//    final HashMap<String,UidToDetail> mUidCache = new HashMap<String,UidToDetail>();
    final ArrayList<String> mRequestQueue;
    final Handler mHandler;
    Drawable icon;
    int iconId; // For passing to the detail screen.
    public Uid uidObj;
    double value;
    double[] values;
    long usageTime;
    long cpuTime;
    long gpsTime;
    long wifiRunningTime;
    long cpuFgTime;
    long wakeLockTime;
    long tcpBytesReceived;
    long tcpBytesSent;
    public double percent;
//    double noCoveragePercent;
    String name;
//    String defaultPackageName;

    static class UidToDetail {
        String name;
//        String packageName;
        Drawable icon;
    }

    BatterySipper(Context context, ArrayList<String> requestQueue,
            Handler handler, String label,int iconId, Uid uid, double[] values) {
        mContext = context;
        mRequestQueue = requestQueue;
        mHandler = handler;
        this.values = values;
        name = label;
        if (iconId > 0) {
            icon = mContext.getResources().getDrawable(iconId);
        }
        if (values != null) value = values[0];
//        if ((label == null || iconId == 0) && uid != null) {
//            getQuickNameIconForUid(uid);
//        }
        uidObj = uid;
    }

    public double getSortValue() {
        return value;
    }

    double[] getValues() {
        return values;
    }

    Drawable getIcon() {
        return icon;
    }

    public int compareTo(BatterySipper other) {
        // Return the flipped value because we want the items in descending order
        return (int) (other.getSortValue() - getSortValue());
    }

//    void getQuickNameIconForUid(Uid uidObj) {
//        final int uid = uidObj.getUid();
//        final String uidString = Integer.toString(uid);
//        if (mUidCache.containsKey(uidString)) {
//            UidToDetail utd = mUidCache.get(uidString);
//            defaultPackageName = utd.packageName;
//            name = utd.name;
//            icon = utd.icon;
//            return;
//        }
//        PackageManager pm = mContext.getPackageManager();
//        final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
//        String[] packages = pm.getPackagesForUid(uid);
//        icon = pm.getDefaultActivityIcon();
//        if (packages == null) {
//            name = Integer.toString(uid);
//            return;
//        } else {
////            name = packages[0];
//        }
//        synchronized (mRequestQueue) {
//        	
//            mRequestQueue.add(name);
//        }
//    }

    /**
     * Sets name and icon
     * @param uid Uid of the application
     */
//    void getNameIcon() {
//        PackageManager pm = mContext.getPackageManager();
//        final int uid = uidObj.getUid();
//        final Drawable defaultActivityIcon = pm.getDefaultActivityIcon();
//        String[] packages = pm.getPackagesForUid(uid);
//        if (packages == null) {
//            name = Integer.toString(uid);
//            return;
//        }
//
//        String[] packageLabels = new String[packages.length];
//        System.arraycopy(packages, 0, packageLabels, 0, packages.length);
//
//        int preferredIndex = -1;
//        // Convert package names to user-facing labels where possible
//        for (int i = 0; i < packageLabels.length; i++) {
//            // Check if package matches preferred package
//            if (packageLabels[i].equals(name)) preferredIndex = i;
//            try {
//                ApplicationInfo ai = pm.getApplicationInfo(packageLabels[i], 0);
//                CharSequence label = ai.loadLabel(pm);
//                if (label != null) {
//                    packageLabels[i] = label.toString();
//                }
//                if (ai.icon != 0) {
//                    defaultPackageName = packages[i];
//                    icon = ai.loadIcon(pm);
//                    break;
//                }
//            } catch (NameNotFoundException e) {
//            }
//        }
//        if (icon == null) icon = defaultActivityIcon;
//
//        if (packageLabels.length == 1) {
//            name = packageLabels[0];
//        } else {
//            // Look for an official name for this UID.
//            for (String pkgName : packages) {
//                try {
//                    final PackageInfo pi = pm.getPackageInfo(pkgName, 0);
//                    if (pi.sharedUserLabel != 0) {
//                        final CharSequence nm = pm.getText(pkgName,
//                                pi.sharedUserLabel, pi.applicationInfo);
//                        if (nm != null) {
//                            name = nm.toString();
//                            if (pi.applicationInfo.icon != 0) {
//                                defaultPackageName = pkgName;
//                                icon = pi.applicationInfo.loadIcon(pm);
//                            }
//                            break;
//                        }
//                    }
//                } catch (PackageManager.NameNotFoundException e) {
//                }
//            }
//        }
//        final String uidString = Integer.toString(uidObj.getUid());
//        UidToDetail utd = new UidToDetail();
//        utd.name = name;
//        utd.icon = icon;
//        utd.packageName = defaultPackageName;
//        mUidCache.put(uidString, utd);
//    }
}