package com.lewa.player.provider;

import java.io.File;

import com.lewa.player.MusicUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class PlayerDBHelper extends SQLiteOpenHelper {


	  public static final String DATABASE_NAME = "com_lewa_player.db";
	  private static final int DATABASE_VERSION = 4;
	  private static final int MAX_FAVOURITE_COUNT = 9;

	  final Context mContext;
	  final boolean mInternal;
	  int playtimes = 0;

      public PlayerDBHelper(Context context, String name, boolean internal) {
          super(context, name, null, DATABASE_VERSION);
          mContext = context;
          mInternal = internal;
      }



    @Override
    public void onCreate(SQLiteDatabase db) {
    	// TODO Auto-generated method stub
    	db.execSQL("create table if not exists playlist_audio_map("
    			+ "song_id integer primary key,"
    			+ "name varchar,"
    			+ "playlist,"
    			+ "date_added,"
    			+ "isonline,"
    			+ "artist,"
    			+ "play_times integer)");

    	db.execSQL("create table if not exists select_folder("
    			+ "id integer primary key,"
    			+ "path varchar,"
    			+ "play_times integer)");

    	db.execSQL("create table if not exists folder("
    	        + "_id integer primary key autoincrement,"
    	        + "folder_path varchar)");
        try {
            //String filePath = Environment.getExternalStorageDirectory()+"/LEWA/music/";
            //Runtime.getRuntime().exec("rm -rf " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int read_times(int id) {
        int ret = 0;
        try {
            SQLiteDatabase ldb = this.getReadableDatabase();
            String sname = MusicUtils.getSongName(mContext, id)[0].toString();
            Cursor c = ldb.query("playlist_audio_map", new String[]{"play_times"}, "name LIKE" + "'" + sname + "'", null, null, null, null);
              //ldb.execSQL("select play_times from playlist_audio_map where song_id =" + String.valueOf(id));
            //ldb.insert("playlist_audio_map", "song_id", values);
            if(c !=null) {
            	c.moveToFirst();
            	if(c.getCount() > 0) {
            		ret = c.getInt(0);
            	} else {
            		ret = -1;
            	}
            }
            c.close();
            c = null;
            ldb.close();
        } catch (SQLException e) {

        }
        return ret;
    }

    public String[] getDBFavouriteList() {
		String [] ret = null;
        try {
    	    SQLiteDatabase ldb = this.getReadableDatabase();
            Cursor c = ldb.query("playlist_audio_map", new String[]{"name"}, "play_times!=-1", null, null, null, "play_times desc");
            if(c != null && c.getCount() > 0) {
            	c.moveToFirst();
            	int count = c.getCount() >= MAX_FAVOURITE_COUNT ? MAX_FAVOURITE_COUNT : c.getCount();
            	ret = new String[count];
            	for(int i=0; i<count; i++) {
            		ret[i] = c.getString(0);
            		c.moveToNext();
            	}
            }
            c.close();
            c = null;
            ldb.close();
        } catch (SQLException e) {

        }

        return ret;

    }

    public void deleteDBFavoriteList(String name) {

        try {
            SQLiteDatabase ldb = this.getWritableDatabase();
            ldb.delete("playlist_audio_map", "name in " + "(" + name + ")", null);
            ldb.close();
        } catch (SQLException e) {
        }
    }

    public void times_plus(int id) {
        int songid = read_times(id);
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            if(songid < 0) {
            	songid = 0;
            	values.put("song_id", id);
            	values.put("name", MusicUtils.getSongName(mContext, id)[0].toString());
            values.put("play_times", songid);
            db.insert("playlist_audio_map", "song_id", values);
            }else {
            	songid = songid +1;
            	values.put("song_id", id);
            values.put("play_times", songid);
            db.update("playlist_audio_map", values, "song_id=" + id, null);
            }
            values.clear();
            db.close();
        } catch (SQLException e) {

        }
    }

    public void updateDBFolder(String[] path) {
        try {
            SQLiteDatabase ldb = this.getWritableDatabase();

            ldb.delete("folder", null, null);

            ContentValues values = new ContentValues();
            int size = path.length;
            for(int i = 0; i < size; i++) {
                values.put("folder_path", path[i]);
                ldb.insert("folder", "folder_path", values);
            }
            values.clear();
            ldb.close();
        } catch (SQLException e) {

        }
    }

    public String[] getDBFolder() {
        String[] paths = new String[0];
        try {
            SQLiteDatabase ldb = this.getReadableDatabase();
            Cursor c = ldb.query("folder", new String[]{"folder_path"}, null, null, null, null, null);
            if(c != null && c.getCount() > 0) {
                c.moveToFirst();
                paths = new String[c.getCount()];
                for(int i=0; i<c.getCount(); i++) {
                    paths[i] = c.getString(0);
                    c.moveToNext();
                }
            }
            c.close();
            c = null;
            ldb.close();
        } catch (SQLException e) {

        }

        return paths;
    }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		if(oldVersion == 3) {
			db.execSQL("DROP TABLE IF EXISTS playlist_audio_map");
	    	db.execSQL("create table if not exists playlist_audio_map("
	    			+ "song_id integer primary key,"
	    			+ "name varchar,"
	    			+ "playlist,"
	    			+ "date_added,"
	    			+ "isonline,"
	    			+ "artist,"
	    			+ "play_times integer)");
		}
	}

}
