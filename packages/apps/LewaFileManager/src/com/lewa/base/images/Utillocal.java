/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base.images;

import android.R.drawable;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import com.lewa.base.Logs;
import com.lewa.filemanager.beans.AppInfoData;
import com.lewa.filemanager.beans.FileInfo;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 *
 * @author Administrator
 */
public class Utillocal {

    static BitmapFactory.Options options = new BitmapFactory.Options();
    public static int thumbnailWidth = 38;
    public static int thumbnailHeight = 38;

    public static Drawable getApkIcon(Context context, String path) {
        AppInfoData appInfo;
        appInfo = getApkInfo(context, new File(path));
        if (appInfo == null || appInfo.icon == null) {
            return FileInfo.iconPackage;
        }
        return appInfo.icon;
    }

    /**
     * 获取未安装的apk信息
     * 
     * @param ctx
     * @param apkPath
     * @return
     */
    public static AppInfoData getApkInfo(Context ctx, File apkFile) {
        AppInfoData appInfoData;
        String PATH_PackageParser = "android.content.pm.PackageParser";
        String PATH_AssetManager = "android.content.res.AssetManager";
        try {
            // 反射得到pkgParserCls对象并实例化,有参数
            Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
            Class<?>[] typeArgs = {String.class};
            Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = {apkFile.getPath()};
            Object pkgParser = pkgParserCt.newInstance(valueArgs);

            // 从pkgParserCls类得到parsePackage方法
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();// 这个是与显示有关的, 这边使用默认
            typeArgs = new Class<?>[]{File.class, String.class,
                DisplayMetrics.class, int.class};
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
                    "parsePackage", typeArgs);

            valueArgs = new Object[]{apkFile, apkFile.getPath(), metrics, 0};

            // 执行pkgParser_parsePackageMtd方法并返回
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
                    valueArgs);

            // 从返回的对象得到名为"applicationInfo"的字段对象
            if (pkgParserPkg == null) {
                Logs.e("", "============ 1 ");
                return null;
            }
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
                    "applicationInfo");

            // 从对象"pkgParserPkg"得到字段"appInfoFld"的值
            if (appInfoFld.get(pkgParserPkg) == null) {
                Logs.i("", "============ 1 ");
                return null;
            }
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);

            // 反射得到assetMagCls对象并实例化,无参
            Class<?> assetMagCls = Class.forName(PATH_AssetManager);
            Object assetMag = assetMagCls.newInstance();
            // 从assetMagCls类得到addAssetPath方法
            typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
                    "addAssetPath", typeArgs);
            valueArgs = new Object[1];
            valueArgs[0] = apkFile.getPath();
            // 执行assetMag_addAssetPathMtd方法
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

            // 得到Resources对象并实例化,有参数
            Resources res = ctx.getResources();
            typeArgs = new Class[3];
            typeArgs[0] = assetMag.getClass();
            typeArgs[1] = res.getDisplayMetrics().getClass();
            typeArgs[2] = res.getConfiguration().getClass();
            Constructor<Resources> resCt = Resources.class.getConstructor(typeArgs);
            valueArgs = new Object[3];
            valueArgs[0] = assetMag;
            valueArgs[1] = res.getDisplayMetrics();
            valueArgs[2] = res.getConfiguration();
            res = (Resources) resCt.newInstance(valueArgs);
            CharSequence label = null;
            if (info.labelRes != 0) {
                label = res.getText(info.labelRes);
            }
            // 读取apk文件的信息
            appInfoData = new AppInfoData();
            appInfoData.name = label.toString();
            if (info != null) {
                if (info.icon != 0) {// 图片存在，则读取相关信息
                    Drawable icon = res.getDrawable(info.icon);// 图标
                    appInfoData.setAppicon(icon);
                }
            } else {
                Logs.e("", "============ else ");

                return null;
            }
//            String pkgname = pkgParserPkg.getClass().getDeclaredMethod("getName").invoke(pkgParserPkg).toString();
//            appInfoData.setApppackage(pkgname);
            return appInfoData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    public static Bitmap getImage(String path, Bitmap argBitmap, Context ctx) {
        try {
            options.inJustDecodeBounds = true;
            options.outWidth = 160;
            options.outHeight = 232;
            options.inSampleSize = 2;

            BitmapFactory.decodeFile(path, options);
            if (options.outWidth > 0 && options.outHeight > 0) {
                // Now see how much we need to scale it down.
                int widthFactor = (options.outWidth + thumbnailWidth - 1)
                        / thumbnailWidth;
                int heightFactor = (options.outHeight + thumbnailHeight - 1)
                        / thumbnailHeight;

                widthFactor = Math.max(widthFactor, heightFactor);
                widthFactor = Math.max(widthFactor, 1);

                // Now turn it into a power of two.
                if (widthFactor > 1) {
                    if ((widthFactor & (widthFactor - 1)) != 0) {
                        while ((widthFactor & (widthFactor - 1)) != 0) {
                            widthFactor &= widthFactor - 1;
                        }

                        widthFactor <<= 1;
                    }
                }

                options.inSampleSize = widthFactor;
                options.inJustDecodeBounds = false;

                Bitmap bitmap = BitmapFactory.decodeFile(path,
                        options);
                if (bitmap == null) {
                    return argBitmap;
                }
                float scaleWidth = ((float) thumbnailWidth) / bitmap.getWidth();
                float scaleHeight = ((float) thumbnailHeight) / bitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postScale(scaleWidth, scaleHeight);
                Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                return resizedBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return argBitmap;
    }
}
