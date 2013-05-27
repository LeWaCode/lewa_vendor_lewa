package com.lewa.filemanager.ds.database.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.lewa.filemanager.ds.database.SQLManager;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static String DATABASE_NAME = "files.db";
	public static int version = 1;
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, version);
		// TODO Auto-generated constructor stub
	}
	
    @Override  
    public void onOpen(SQLiteDatabase db) {  
        super.onOpen(db);  
    }
	@Override
	public void onCreate(SQLiteDatabase db) {
//		String s = "CREATE TABLE \"mytable\"( [_id] int PRIMARY KEY ,[title] varchar(100) ,[body] varchar(10) ,[name] varchar(100) ) ";
		db.execSQL(SQLManager.createFileSql);
                db.execSQL(SQLManager.createFileIdxSql);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);
		onCreate(db);

	}  

}
