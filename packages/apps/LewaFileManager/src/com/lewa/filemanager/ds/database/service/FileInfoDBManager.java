/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import com.lewa.filemanager.config.Config;
import com.lewa.filemanager.ds.util.ContentValuesUtil;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.base.images.MimeUtil;
import com.lewa.filemanager.config.Constants;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class FileInfoDBManager {

    public static Uri ensureUri(File file) {
        String mime = MimeUtil.parseMime(file);
        if (mime.equals(Constants.CateContants.CATE_IMAGES)) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mime.equals(Constants.CateContants.CATE_MUSIC)) {
            return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (mime.equals(Constants.CateContants.CATE_VIDEO)) {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            if (file.getName().trim().toLowerCase().endsWith(Constants.LOWERCASE_APK)) {
                return MediaArgs.otherUri;
            } else if (mime.startsWith(Constants.MIMEContants.MIME_APPLICATION) || mime.startsWith(Constants.MIMEContants.MIME_TEXT)) {
                return MediaArgs.otherUri;
            } else {
                return null;
            }
        }
    }

    public static void doDBSync(File file, Context context) {
        Uri uri = ensureUri(file);
        if (uri == null) {
            return;
        }
        boolean ifExists = query(context, uri, file.getAbsolutePath());
        if (ifExists) {
            update(context, uri, file.getAbsolutePath(), ContentValuesUtil.constructContentValues(file, uri, null));
        } else {
            insert(context, uri, ContentValuesUtil.constructContentValues(file, uri, null));
        }
    }

    public static void delete(Context context, Uri uri, String data) {
        context.getContentResolver().delete(uri, MediaArgs.PATH + " like ?", new String[]{data});
    }

    public static void del(Context context, Uri uri, String sql) {
        context.getContentResolver().delete(uri, sql + "--", null);
    }

    public static void update(Context context, Uri uri, String data, ContentValues cv) {
        context.getContentResolver().update(uri, cv, MediaArgs.PATH + " like ?", new String[]{data});
    }

    public static boolean query(Context context, Uri uri, String data) {
        return context.getContentResolver().query(uri, new String[]{"1"}, MediaArgs.PATH + " like ?", new String[]{data}, null).getCount() > 0;
    }

    public static void delete(Context context, Uri uri, int data) {
        try {
            context.getContentResolver().delete(uri, "_id = ?", new String[]{data + ""});
        } catch (Exception e) {
        }
    }

    public static boolean doDBSync(File file, Uri uri, Context context, boolean ifExists, Integer id, String ext) {
        if (ifExists) {
            update(context, uri, id, ContentValuesUtil.constructContentValues(file, uri, ext));
        } else {
            return insert(context, uri, ContentValuesUtil.constructContentValues(file, uri, ext));
        }
        return false;
    }

    public static boolean insert(Context context, Uri uri, ContentValues cv) {
        context.getContentResolver().insert(uri, cv);
        return true;
    }

    public static void update(Context context, Uri uri, int data, ContentValues cv) {
        context.getContentResolver().update(uri, cv, "_id = ?", new String[]{data + ""});
    }

    public static void upd(Context context, Uri uri, String sql, ContentValues cv) {
        context.getContentResolver().update(uri, cv, sql + "--", null);
    }
}
