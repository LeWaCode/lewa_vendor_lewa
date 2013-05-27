package com.lewa.providers.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class PhoneLocationSQLiteHelper extends SQLiteOpenHelper{
    private static final String DB_NAME = "location.db";
    private static final int DB_VERSION = 10;
    private static final String dbPath = "/data/data/com.lewa.providers.location/databases";
    private static final String dbPathName = dbPath + "/" + DB_NAME;
	private static int countDbCopy = 0;
    private Context context;
    
    public PhoneLocationSQLiteHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        
        this.context = context;
        checkDBIsExist();
    }

    @Override
    public synchronized SQLiteDatabase getWritableDatabase() {
       SQLiteDatabase db = null;
        try {
            db = super.getWritableDatabase();
        } catch (SQLiteException e) {
            db = SQLiteDatabase.openDatabase(dbPathName, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        return db;
    }
     
    @Override
    public synchronized SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = null;
        try {
            db = super.getReadableDatabase();
        } catch (SQLiteException e) {
            db = SQLiteDatabase.openDatabase(dbPathName, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        return db;
    }
        
    @Override
    public void onCreate(SQLiteDatabase db) {
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        deleteDB();
	checkDBIsExist();
    }
    
    public void deleteDB() {
        File dbFile = new File(dbPathName);
        
        if (countDbCopy < 3 && dbFile.exists())
        {
            dbFile.delete(); 
        }
    }
    
    private void checkDBIsExist(){
       File dbDir = new File(dbPath);
        if(!dbDir.exists()){
            dbDir.mkdir();
        }
        
        File db = new File(dbPathName);
        if(countDbCopy < 3 && !db.exists()){
            try {
				countDbCopy++;
                InputStream inputStream = context.getResources().openRawResource(R.raw.location);
                FileOutputStream fos = new FileOutputStream(dbPathName);
                byte[] buffer = new byte[8192];
                int temp = 0;
                try {
                    while ((temp = inputStream.read(buffer)) > 0) {
                        fos.write(buffer, 0, temp);
                    }
                    fos.close();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
