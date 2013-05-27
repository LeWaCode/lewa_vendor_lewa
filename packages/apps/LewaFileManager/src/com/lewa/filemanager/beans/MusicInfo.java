/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.beans;

import android.content.Context;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.beans.FileInfo;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class MusicInfo extends FileInfo {

    public static String album_text;
    public static String artist_text;
    public String album;
    public String artist;
    public String genre;
    public String thumbnail;
    public Integer album_id;
    public Integer album_artist_id;
    public boolean isBuilt;
    public String line = " - ";
    public MusicInfo() {
        album = album_text;
        artist = artist_text;
    }

    public MusicInfo(File file, Context context, Boolean duplicatedFlag) {
        super(file, context, duplicatedFlag);
    }

    public MusicInfo(File file, Context context) {
        super(file, context);
    }

    public static void init(Context context) {
        album_text = context.getString(R.string.music_album) + ": ";
        artist_text = context.getString(R.string.music_artist) + ": ";
    }

    public void buildMusic(int album_id, int album_artist_id) {
        if (!isBuilt) {
            this.album_id = album_id;
            this.album_artist_id = album_artist_id;
        }
    }
}
