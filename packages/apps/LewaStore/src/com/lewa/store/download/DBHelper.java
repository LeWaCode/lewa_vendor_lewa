package com.lewa.store.download;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	
	private String TAG=DBHelper.class.getSimpleName();
	
	private static final int DBVERSION= 3;
	private final static String DBNAME="download.db";
	
	public DBHelper(Context context) {
		super(context, DBNAME, null, DBVERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS download_info(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer, app_id integer, "
				+ "start_pos integer, end_pos integer,max_size integer,compelete_size integer,url char,status integer)");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS app_info(_id integer PRIMARY KEY AUTOINCREMENT, app_id integer,app_name char,"
				+"app_author char,app_version char,app_versioncode integer,app_packagename char,"
				+"app_description char,app_url char,app_logourl char,app_access char,app_size integer,app_category char"
				+ ")");
		db.execSQL("CREATE TABLE IF NOT EXISTS icon_info(_id integer PRIMARY KEY AUTOINCREMENT,packagename char,"
				+ "app_url char,app_logourl char,icon text)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.e("CStoreDB", "upgrade from version " + oldVersion + " to version " + newVersion);
            if (oldVersion == 1) {
            	Log.d(TAG,"oldVersion==1");
				db.execSQL("DROP TABLE IF  EXISTS download_info ");
				db.execSQL("create table download_info(_id integer PRIMARY KEY AUTOINCREMENT, thread_id integer,app_id integer, "
						+ "start_pos integer, end_pos integer,max_size integer,compelete_size integer,url char,status integer)");
                oldVersion++;
            }
            if(oldVersion==2){
            	Log.d(TAG,"oldVersion==2");
            	db.execSQL("DROP TABLE IF EXISTS app_info ");
            	db.execSQL("CREATE TABLE IF NOT EXISTS app_info(_id integer PRIMARY KEY AUTOINCREMENT, app_id integer,app_name char,"
        				+"app_author char,app_version char,app_versioncode integer,app_packagename char,"
        				+"app_description char,app_url char,app_logourl char,app_access char,app_size integer,app_category char"
        				+ ")");
                oldVersion++;
            }
	}
}
