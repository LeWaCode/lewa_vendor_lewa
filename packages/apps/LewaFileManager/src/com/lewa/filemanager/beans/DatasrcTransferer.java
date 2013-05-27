/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.beans;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.funcgroup.AppSrc;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.filemanager.beans.FileUtil;
import com.lewa.base.Logs;
import java.util.Date;

/**
 *
 * @author chenliang
 */
public class DatasrcTransferer {

    public static <T extends FileInfo> T transf(Context context, Cursor cursor, Class<? extends FileInfo> clazz) {
        if (cursor != null && cursor.isClosed()) {
            return null;
        }
        FileInfo fileInfo = null;
        try {
            fileInfo = clazz.newInstance();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        fileInfo.setPath(cursor.getString(cursor.getColumnIndex("_data")));
        fileInfo.setName(FileUtil.getName(fileInfo.getPath()));
        fileInfo.setLeng(cursor.getLong(cursor.getColumnIndex("_size")));
        fileInfo.setLastModified(DateFormat.format("yyyy-MM-dd kk:mm:ss",
                new Date(cursor.getLong(cursor.getColumnIndex("date_modified")) * 1000)).toString());
        fileInfo.setIconRes(-1);
        fileInfo.setIsDir(Boolean.FALSE);

        fileInfo.setSizeText(FileUtil.formatSize(fileInfo.getLeng()));
        fileInfo.setType(FileUtil.getRealExtension(fileInfo.getName()));
        if (clazz == MusicInfo.class) {
            preserMusicInfo(cursor, fileInfo, context);
        }
        if (clazz == ApkInfo.class) {
            if (ApkInfoUtil.preserApkInfo(fileInfo, context)) {
                return (T) fileInfo;
            }
        }
        return (T) fileInfo;
    }

    public static void preserMusicInfo(Cursor cursor, FileInfo fileInfo, Context context) {
        String album = cursor.getString(cursor.getColumnIndex("album"));
        ((MusicInfo) fileInfo).album = album == null || album.equals("unknown") ? context.getString(R.string.music_unknown) : album;
        String tmp = FileUtil.getParent(fileInfo.getPath());
        tmp = tmp.substring(tmp.lastIndexOf("/") + 1);
        Logs.i("--------------------------" + ((MusicInfo) fileInfo).album + "---" + tmp);
        if (tmp.equals(((MusicInfo) fileInfo).album)) {
            ((MusicInfo) fileInfo).album = context.getString(R.string.music_unknown);
        }
        ((MusicInfo) fileInfo).album = "- " + ((MusicInfo) fileInfo).album;
        String artist = cursor.getString(cursor.getColumnIndex("artist"));
        ((MusicInfo) fileInfo).artist = artist == null || artist.equals("unknown") ? context.getString(R.string.music_unknown) : artist;
        ((MusicInfo) fileInfo).thumbnail = cursor.getString(cursor.getColumnIndex("thumbnail"));
        ((MusicInfo) fileInfo).album_id = cursor.getInt(cursor.getColumnIndex("album_id"));
        ((MusicInfo) fileInfo).album_artist_id = cursor.getInt(cursor.getColumnIndex("album_artist_id"));
    }
}
