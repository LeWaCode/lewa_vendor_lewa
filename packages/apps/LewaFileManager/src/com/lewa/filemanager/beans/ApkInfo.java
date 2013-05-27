/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.beans;

import android.content.Context;
import android.util.DisplayMetrics;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.base.Logs;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 *
 * @author chenliang
 */
public class ApkInfo extends FileInfo {

    public String versionName;
    public String versionNameStr = DISPLAY_VERSION_CODE;
    public String versionCondition;
    public String packageName;
    public static String VERSION_UNINTALLED;
    public static String VERSION_UPDATETODATE;
    public static String VERSION_UPGRADE;
    public static String VERSION_INTALLED;
    public static String DISPLAY_VERSION_CODE;
    public static String DISPLAY_UNKNOWN;
    public boolean isBuilt;

    public ApkInfo(String path) {
        this.setLeng(new File(path).length());
        this.setSizeText(FileUtil.formatSize(this.getLeng()));
        this.setName(FileUtil.getName(path));
    }

    public static void init(Context context) {
        VERSION_UNINTALLED = context.getString(R.string.uninstalled);
        VERSION_UPDATETODATE = context.getString(R.string.up_to_date);
        VERSION_UPGRADE = context.getString(R.string.upgrade);
        VERSION_INTALLED = context.getString(R.string.installed);
        DISPLAY_VERSION_CODE = context.getString(R.string.version);
        DISPLAY_UNKNOWN = context.getString(R.string.unknown);
    }

    public ApkInfo() {
    }

    public boolean setApkInfo(Context ctx) {
        if (getPath() == null) {
            throw new IllegalStateException("should initialize path in ApkInfo");
        }
        return setApkInfo(ctx, new File(this.getPath()));
    }

    public ApkInfo(File file, Context context) {
        super(file, context);
    }

    public boolean setApkInfo(Context ctx, File apkFile) {
        if (isBuilt) {
            return true;
        }
        isBuilt = true;
        String PATH_PackageParser = "android.content.pm.PackageParser";
        try {
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = {String.class};
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = {apkFile.getPath()};
            Object pkgParser = pkgParserCt.newInstance(valueArgs);

            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            typeArgs = new Class<?>[]{File.class, String.class,
                DisplayMetrics.class, int.class};
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
                    "parsePackage", typeArgs);

            valueArgs = new Object[]{apkFile, apkFile.getPath(), metrics, 0};

            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
                    valueArgs);
            this.packageName = pkgParserPkg.getClass().getField("packageName").get(pkgParserPkg).toString();
            this.versionName = pkgParserPkg.getClass().getField("mVersionName").get(pkgParserPkg).toString();
            this.versionNameStr = DISPLAY_VERSION_CODE + " " + versionName;
        } catch (Exception e) {
            this.versionNameStr = DISPLAY_VERSION_CODE + " " + DISPLAY_UNKNOWN;
            return false;
        }
        return true;
    }

    public static class ApkVersionName implements Comparable<ApkVersionName> {

        public String versionName;

        public ApkVersionName(String versionName) {
            this.versionName = versionName != null ? versionName.trim() : null;
        }

        public int compareTo(ApkVersionName another) {

            if (this.versionName.trim().equals(another.versionName)) {
                return 0;
            }
            String zeros = "";
            String versionname = "";
            String another_name = "";
            boolean bool = versionName.length() - another.versionName.length() > 0 ? true : false;
            for (int i = 0; i < this.versionName.length(); i++) {
                char ch = this.versionName.charAt(i);
                if (Character.isDigit(ch)) {
                    versionname += new String(new Character(ch).toString());
                }
            }
            for (int i = 0; i < another.versionName.length(); i++) {
                char ch = another.versionName.charAt(i);
                if (Character.isDigit(ch)) {
                    another_name += new String(new Character(ch).toString());
                }
            }

            int balance = versionname.length() - another_name.length();
            for (int i = 0; i < Math.abs(balance); i++) {
                zeros += "0";
            }
            if (balance > 0) {
                another_name += zeros;
            } else {
                versionname += zeros;
            }
            Logs.i("", "v=== " + another_name + " " + versionname);
            return Integer.parseInt(versionname) - Integer.parseInt(another_name);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof FileInfo) {
            return super.getPath().equals(((FileInfo) obj).getPath());
        }
        return false;
    }
}
