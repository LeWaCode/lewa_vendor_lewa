/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.face.loadimage;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;

/**
 *
 * @author Administrator
 */
public class Utillocal {

    static BitmapFactory.Options options = new BitmapFactory.Options();
    public static int thumbnailWidth = 160;
    public static int thumbnailHeight = 232;

    public static Drawable getApkIcon(Context context, String path) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            try {
                return pm.getApplicationIcon(appInfo);
            } catch (OutOfMemoryError e) {
            }
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

    public static Bitmap getImage(String path, Bitmap argBitmap,Context ctx) {
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
