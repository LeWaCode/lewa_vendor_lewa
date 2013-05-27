/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.util;

import com.lewa.base.images.FileTypeInfo;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import com.lewa.filemanager.ds.database.MediaArgs;
import com.lewa.base.images.MimeUtil;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.filemanager.beans.MusicInfo;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class ContentValuesUtil {

    static ContentValues contentvalues = new ContentValues();

    public static synchronized ContentValues constructContentValues(File file, FileTypeInfo typeInfo) {
        contentvalues.clear();
        contentvalues.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        if (typeInfo == null || (!(typeInfo.category.equals(Constants.CateContants.CATE_THEME) || typeInfo.category.equals(Constants.CateContants.CATE_DOCS) || typeInfo.category.equals(Constants.CateContants.CATE_PACKAGE)))) {
            contentvalues.put(MediaStore.Audio.Media.DISPLAY_NAME, file.getName());
        }
        contentvalues.put(MediaStore.Audio.Media.TITLE, FileUtil.getNameTitle(file.getAbsolutePath()));
        contentvalues.put(MediaStore.Audio.Media.SIZE, file.length());
        contentvalues.put(MediaStore.Audio.Media.DATE_MODIFIED, file.lastModified() / 1000);
        contentvalues.put(MediaStore.Audio.Media.MIME_TYPE, typeInfo.mime);
        return contentvalues;
    }

    public static synchronized ContentValues constructContentValues(FileInfo fileinfo, FileTypeInfo typeInfo) {
        constructContentValues(fileinfo.getFile(), typeInfo);
        if (fileinfo instanceof MusicInfo) {
            contentvalues.put("album_id", ((MusicInfo) fileinfo).album_id);
            Logs.i("", "------------ aa " + ((MusicInfo) fileinfo).album_id);
            contentvalues.put("album_artist_id", ((MusicInfo) fileinfo).album_artist_id);
        }
        return contentvalues;
    }

    public static synchronized ContentValues constructContentValues(File file, Uri uri, String ext) {

        contentvalues.clear();
        contentvalues.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        if (uri == null || (!(uri.equals(MediaArgs.otherUri)))) {
            contentvalues.put(MediaStore.Audio.Media.DISPLAY_NAME, file.getName());
        }
        contentvalues.put(MediaStore.Audio.Media.TITLE, FileUtil.getNameTitle(file.getAbsolutePath()));
        contentvalues.put(MediaStore.Audio.Media.SIZE, file.length());
        contentvalues.put(MediaStore.Audio.Media.DATE_MODIFIED, file.lastModified() / 1000);
        contentvalues.put(MediaStore.Audio.Media.MIME_TYPE, "lwt".equalsIgnoreCase(ext) ? MediaArgs.THEME_MIME : MimeUtil.parseWholeMime(ext));
        return contentvalues;
    }
}
