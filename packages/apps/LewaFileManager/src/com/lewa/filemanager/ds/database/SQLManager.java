/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.ds.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import com.lewa.filemanager.config.Config;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Administrator
 */
public class SQLManager {

    public static String tablename = "files";
    public static final String unknown = "WEI ZHI";
    public static String querySql = "distinct m._data _data, m.album_artist_id album_artist_id,m.album_id album_id,m._size _size,m.date_modified date_modified,_display_name,"
            + (Config.isLewaRom ? "case when i.artist_sort_key like '<unknown>' then '" + unknown + "' else upper(i.artist_sort_key) end artist_sort_key," : "")
            + "g.name genre,case when b.album like '<unknown>' then 'unknown'  else b.album end album,case when i.artist is null then 'unknown' when i.artist like '<unknown>' then 'unknown'  else i.artist end artist,t._data thumbnail from audio_meta m left join (select p.audio_id _id,s.name name from audio_genres_map p left join audio_genres s on (s._id = p.genre_id)) g on (m._id = g._id) left join album_art t on (m.album_id=t.album_id ) left join artists i on (m.artist_id=i.artist_id) left join albums b on (m.album_id = b.album_id)";
    public static String createFileSql = "CREATE TABLE " + tablename + " (_id INTEGER PRIMARY KEY AUTOINCREMENT,_data TEXT,_size INTEGER,mime_type TEXTfile_name_ext TEXT,date_modified INTEGER,title TEXT)";
    public static String createFileIdxSql = "CREATE INDEX path_index ON " + tablename + "(_data)";
    public static String otherfile = "/data/data/com.lewa.app.filemanager/databases/files";
    public static File dbFile = new File(otherfile);
    public static File dbDir = new File("/data/data/com.lewa.app.filemanager/databases");
    public static Uri FilesUri = Uri.parse("content://com.lewa.app.filemanager/files");
    public static SQLiteDatabase sdb;

    public static void createFileDB() {
        if (!Config.isLewaRom) {
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }
            if (!dbFile.exists()) {
                try {
                    dbFile.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            sdb = SQLiteDatabase.openOrCreateDatabase(otherfile, null);
            Cursor otherfile = sdb.query("sqlite_master", new String[]{"count(1)"}, "type=? and name =?", new String[]{"table", tablename}, null, null, null);
            if (otherfile == null || otherfile.getCount() == 0) {
                sdb.execSQL(createFileSql);
                sdb.execSQL(createFileIdxSql);
            }
            otherfile.close();
            sdb.close();
        }
    }

    public static Cursor queryMusic(Context context, String orderby) {
        return context.getContentResolver().query(Audio.Media.EXTERNAL_CONTENT_URI, new String[]{querySql + " order by " + orderby + "--"}, null, null, null);
    }

    public static String getSearchPart_Media_Sql(String keyWords, String orderby) {
        return "i._data, i.title,i._size,i.date_modified from (select _data, _display_name title,_size,date_modified from audio_meta where _display_name like '%" + keyWords + "%'  union select _data, _display_name title,_size,date_modified  from video where  _display_name like '%" + keyWords + "%'  union select _data, _display_name title,_size,date_modified  from images  where  _display_name like '%" + keyWords + "%' ) i" + orderby;
    }

    public static String getSearchPart_Files_Sql(String keyWords, String orderby) {
        return "_data, title,_size,date_modified  from files  where  title like '%" + keyWords + "%' " + orderby;
    }

    public static String getImagesQuerySql(Float sizemorethan, String orderbyfield) {
        return "_data,title,_size,date_modified from images where _size > " + sizemorethan * 1024 + " order by " + orderbyfield;
    }
    

    public static boolean apkFlagExists(Context ctx){
        return ifFieldExists("files", "apk_condition_install",ctx);
    }
    

    public static Boolean ifFieldExists(String table, String field,Context ctx) {
        Cursor c = ctx.getContentResolver().query(MediaArgs.otherUri, new String[]{" sql from sqlite_master where tbl_name='" + table + "'--"}, null, null, null);
        if(c.getCount()>0){
            c.moveToNext();
            return c.getString(0).contains(field);
        }
        return null;
    }

}