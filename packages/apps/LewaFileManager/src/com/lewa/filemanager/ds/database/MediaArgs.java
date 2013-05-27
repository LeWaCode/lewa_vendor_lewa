/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.lewa.filemanager.config.Config;

/**
 *
 * @author chenliang
 */
public class MediaArgs {

    public static final String PATH = MediaStore.Audio.Media.DATA;
    public static final String TITLE = MediaStore.Audio.Media.TITLE;
    public static final String SIZE = MediaStore.Audio.Media.SIZE;
    public static final String MODIFIED = MediaStore.Audio.Media.DATE_MODIFIED;
    public static final String MIMETYPE = MediaStore.Audio.Media.MIME_TYPE;
    public static final String SUMSIZE = "sum(_size)";
    public static final String COUNT = "count(distinct " + PATH + ")";
    public static final String APK_MIME = "application/vnd.android.package-archive";
    public static final String THEME_MIME = "lewa/theme";
    public static final String PACKAGE_MIME = "application/vnd.android.package-archive";
    public static Uri otherUri;

	static {
    	otherUri = Uri.parse("content://media/external/file");
    	if(Config.is4_0Lower&&!Config.isLewaRom){
    		otherUri = SQLManager.FilesUri;
    	}
    }
    public static final String[] LISTPROJECTION = new String[]{
        PATH, TITLE, SIZE, MODIFIED
    };
    public static final String[] CATEGORYPROJECTION = new String[]{
        COUNT, SUMSIZE
    };

    public static final String countWhereOnMimeLike(boolean like) {
        return MIMETYPE + " " + (like ? "" : "not ") + "like ?";
    }

    public static final String whereOnMimeLike(boolean like) {
        String isMimeNull = (like ? "" : MIMETYPE + " is not null"
                //                + " and " + MIMETYPE + " not like 'application/zip'"
                + " and " + MIMETYPE + " not like 'application/vnd.android.package-archive'"
                + " and " + MIMETYPE + " not like 'lewa/theme'");
        String where = " " + (like ? MIMETYPE + " like ?" : isMimeNull);
        return where;
    }

    public static final String countWhereOnMimesNull() {
        return MIMETYPE + " is null";
    }

    public static Cursor countQuery4NoMedia(Context context, Uri uri, boolean like, String mimeValue) {
        return context.getContentResolver().query(uri, MediaArgs.CATEGORYPROJECTION, MediaArgs.whereOnMimeLike(like), like ? new String[]{mimeValue} : null, null);
    }

    public static Cursor listQuery4NoMedia(Context context, Uri address, boolean like, String mimeValue, String order) {
        return context.getContentResolver().query(address, LISTPROJECTION, whereOnMimeLike(like), like ? new String[]{mimeValue} : null, order);
    }

    public static Cursor query4NoMedia(Context context, Uri address, String[] projection, boolean like, String order, String... mimeValues) {
        String countWhereOnMimeLike = countWhereOnMimeLike(like);
        if (!like) {
            for (int i = 1; i < mimeValues.length; i++) {
                countWhereOnMimeLike += " and " + countWhereOnMimeLike(like);
            }
        }
        return context.getContentResolver().query(address, projection, "(" + countWhereOnMimeLike + getIsMimeNull(like) + ") and " + MediaArgs.PATH + " is not null", mimeValues, order);
    }

    public static final String getIsMimeNull(boolean isNotNull) {
        return isNotNull ? "" : " or (" + MIMETYPE + " is null )";
    }
}
