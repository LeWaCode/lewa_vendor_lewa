/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.actions.apk;

import java.io.File;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageParser;
import android.net.Uri;
import android.util.DisplayMetrics;
import com.lewa.app.filemanager.R;
import com.lewa.base.Logs;
import com.lewa.filemanager.beans.AppInfoData;
import com.lewa.filemanager.beans.ReportInfo;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallHelper {

    final static int SUCCEEDED = 1;
    final static int FAILED = 0;
    private Context mContext;
    private ApplicationInfo mAppInfo;
    private List<String> paths;
    private Iterator<String> itStr;
    private String currInstallPath;
    private int currInstallIdx;

    public int getCurrInstallIdx() {
        return currInstallIdx + 1;
    }

    public void clear() {
        paths = null;
        itStr = null;
    }

    public String getCurrInstallPath() {
        return currInstallPath;
    }

    public Integer getTotalInstallSum() {
        return this.paths.size();
    }

    public List<String> getPaths() {
        return paths;
    }

    public InstallHelper(Context context) {
        mContext = context;
    }

    public void install(String path, String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }

    public String getIteratorNext(List<String> paths) {
        if (this.paths == null) {
            this.paths = paths;
            itStr = paths.iterator();
        }
        if (itStr.hasNext()) {
            this.currInstallPath = itStr.next();
            currInstallIdx = paths.indexOf(currInstallPath);
            Logs.i(" ---------- currInstallPath " + currInstallPath);
            return currInstallPath;
        }
        return null;
    }

    public boolean hasNext(String iteratorNext) {
        if (iteratorNext == null || iteratorNext.equals("end")) {
            return false;
        }
        Logs.i("-----------pathpath 111");
        tryInstall(iteratorNext);
        return true;
    }

    public void tryInstall(String path) {
        PackageInstallManager.getInstance().apkpath=path;
        int installFlags = 0;
        Uri mPackageURI = Uri.fromFile(new File(path));
        PackageParser.Package mPkgInfo = null;
        String packageName = null;
        PackageManager pm = null;
        Object appLabel = "";
        AppInfoData data = new AppInfoData();
        try {
            mPkgInfo = getPackageInfo(mPackageURI);
            mAppInfo = mPkgInfo.applicationInfo;
        } catch (Exception e) {
//            ReportOfInstall.failed.add(new ReportInfo(path, mContext.getString(R.string.unknown)));
//            PackageInstallManager.getInstance().faliureNum++;
//            PackageInstallManager.getInstance().tryNextInstall("", -1, true);
//            return;
        }
        try {
            if ((mAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
                Logs.i("-----------pathpath 1 " + path);
                ReportOfInstall.failed.add(new ReportInfo(path, appLabel.toString()));
                PackageInstallManager.getInstance().faliureNum++;
                PackageInstallManager.getInstance().tryNextInstall("", -1, true);
                return;
            }
            Logs.i("-----------pathpath 0 1 " + path);
            if (mPkgInfo == null) {
                Logs.i("-----------pathpath 2 " + path);
                PackageInstallManager.getInstance().faliureNum++;
                ReportOfInstall.failed.add(new ReportInfo(path, mContext.getString(R.string.unknown)));
                PackageInstallManager.getInstance().tryNextInstall(new File(path).getName(), R.string.appInstallFailureHint, true);
                return;
            }
            Logs.i("-----------pathpath 0 2 " + path);
            packageName = mAppInfo.packageName;
            pm = mContext.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName,
                    PackageManager.GET_UNINSTALLED_PACKAGES);
            if (pi != null) {
                try {
                    installFlags |= ((Integer) (PackageManager.class.getDeclaredField("INSTALL_REPLACE_EXISTING").get(PackageManager.class))).intValue();
                } catch (Exception ex) {
                    Logger.getLogger(InstallHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
//            Logs.i("-----------pathpath 1 " + path);
//            ReportOfInstall.failed.add(new ReportInfo(path, ""));
//            PackageInstallManager.getInstance().faliureNum++;
//            PackageInstallManager.getInstance().tryNextInstall("", -1, true);
//            return;
        }
        try {
            if ((installFlags & ((Integer) (PackageManager.class.getDeclaredField("INSTALL_REPLACE_EXISTING").get(PackageManager.class))).intValue()) != 0) {
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            PackageManager.class.getMethod("installPackage", new Class[]{Uri.class, android.content.pm.IPackageInstallObserver.class, int.class, String.class}).invoke(pm, new Object[]{mPackageURI, PackageInstallManager.getInstance(), installFlags,
                        packageName});
        } catch (Exception ex) {
            Logs.i("-----------pathpath 3 " + path);
                PackageInstallManager.getInstance().faliureNum++;
                ReportOfInstall.failed.add(new ReportInfo(path, mContext.getString(R.string.unknown)));
                PackageInstallManager.getInstance().tryNextInstall(new File(path).getName(), R.string.appInstallFailureHint, true);
        }
    }

    private class PackageDeleteObserver extends IPackageDeleteObserver.Stub {

        public void packageDeleted(boolean succeeded) {
        }
    }

    public void uninstall(String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
                packageURI);
        mContext.startActivity(uninstallIntent);
    }

    public void uninstallBatch(String packageName) {
        PackageDeleteObserver observer = new PackageDeleteObserver();
        try {
            PackageManager.class.getMethod("deletePackage", new Class[]{String.class, android.content.pm.IPackageDeleteObserver.class, int.class}).invoke(mContext.getPackageManager(), new Object[]{packageName, observer, 0});
        } catch (Exception ex) {
            Logger.getLogger(InstallHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /* 
     *  Utility method to get package information for a given packageURI        
     */
    public PackageParser.Package getPackageInfo(Uri packageURI) {
        final String archiveFilePath = packageURI.getPath();
        PackageParser packageParser = new PackageParser(archiveFilePath);
        File sourceFile = new File(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        PackageParser.Package pkg = packageParser.parsePackage(sourceFile,
                archiveFilePath, metrics, 0);
// Nuke the parser reference.
        packageParser = null;
        return pkg;
    }
}
