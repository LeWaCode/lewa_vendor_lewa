package com.android.phone;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.InterceptConstants;
import android.net.Uri;
import android.util.Log;

public class InterceptProvider extends ContentProvider {

    private DatabaseHelper dbHelper;
    private static final UriMatcher sMatcher;

    static {
        sMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sMatcher.addURI(InterceptConstants.AUTOHORITY,InterceptConstants.TABLENAME, InterceptConstants.ITEM);
        sMatcher.addURI(InterceptConstants.AUTOHORITY, InterceptConstants.TABLENAME+"/#", InterceptConstants.ITEM_ID);
        sMatcher.addURI(InterceptConstants.AUTOHORITY,InterceptConstants.DND_TABLENAME, InterceptConstants.DND_ITEM);
        sMatcher.addURI(InterceptConstants.AUTOHORITY,InterceptConstants.MSG_TABLENAME, InterceptConstants.MSG_HISTORY_ITEM);
        sMatcher.addURI(InterceptConstants.AUTOHORITY,InterceptConstants.MSG_TABLENAME+"/#", InterceptConstants.MSG_HISTORY_ITEM_ID);
        sMatcher.addURI(InterceptConstants.AUTOHORITY,InterceptConstants.CALL_TABLENAME, InterceptConstants.CALL_HISTORY_ITEM);
        sMatcher.addURI(InterceptConstants.AUTOHORITY,InterceptConstants.CALL_TABLENAME+"/#", InterceptConstants.CALL_HISTORY_ITEM_ID);
    }

     private static final class DatabaseHelper extends SQLiteOpenHelper{
        private static final int DATABASE_VERSION = 21;
        public DatabaseHelper(Context context) {
            super(context, InterceptConstants.DBNAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (InterceptConstants.DBUG){
                Log.i(InterceptConstants.TAG, "DBHelper onCreate create tables...start");
            }

            db.execSQL("create table " + InterceptConstants.TABLENAME + "(" +
                    InterceptConstants.COLUMN_BLOCK_NAME_ID + " integer primary key autoincrement not null," +
                    InterceptConstants.COLUMN_NAME + " text not null," +
                    InterceptConstants.COLUMN_NUMBER + " text not null unique," +
                    InterceptConstants.COLUMN_TYPE + " text not null," +
                    InterceptConstants.COLUMN_MODE + " text not null," +
                    InterceptConstants.COLUMN_PRIVACY + " text not null);");

            db.execSQL("create table " + InterceptConstants.DND_TABLENAME + "(" +
                    InterceptConstants.COLUMN_DND_ID + " integer primary key autoincrement not null," +
                    InterceptConstants.COLUMN_SWITCH + " text not null," +
                    InterceptConstants.COLUMN_SWITCH_MODE + " text not null," +
                    InterceptConstants.COLUMN_START_TIME + " text not null," +
                    InterceptConstants.COLUMN_END_TIME + " text not null)");

            db.execSQL("create table " + InterceptConstants.MSG_TABLENAME + "(" +
                    InterceptConstants.COLUMN_MSG_ID + " integer primary key autoincrement not null," +
                    InterceptConstants.COLUMN_MSG_NAME + " text not null,"    +
                    InterceptConstants.COLUMN_MSG_ADDRESS + " text not null," +
                    InterceptConstants.COLUMN_MSG_LOCATION+ " text not null," +
                    InterceptConstants.COLUMN_MSG_SUBJECT + " text not null," +
                    InterceptConstants.COLUMN_MSG_BODY + " text not null," +
                    InterceptConstants.COLUMN_MSG_READ + " text not null," +
                    InterceptConstants.COLUMN_MSG_DATE + " text not null," +
                    InterceptConstants.COLUMN_MSG_TYPE + " text not null)");

            db.execSQL("create table " + InterceptConstants.CALL_TABLENAME + "(" +
                    InterceptConstants.COLUMN_CALL_ID + " integer primary key autoincrement not null," +
                    InterceptConstants.COLUMN_CALL_NAME + " text not null," +
                    InterceptConstants.COLUMN_CALL_ADDRESS + " text not null," +
                    InterceptConstants.COLUMN_CALL_LOCATION + " text," +
                    InterceptConstants.COLUMN_CALL_READ + " text not null," +
                    InterceptConstants.COLUMN_CALL_DATE + " text not null," +
                    InterceptConstants.COLUMN_CALL_BLOCKTYPE + " text not null," +
                    InterceptConstants.COLUMN_CALL_CAUSE + " text not null)");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            int upgradeVersion = oldVersion;
            if(upgradeVersion<21){
                Cursor cursor=db.rawQuery( "SELECT * FROM "+InterceptConstants.CALL_TABLENAME , null);
                if (cursor.getColumnCount()<8) {          
                    db.execSQL("Alter table " +  InterceptConstants.CALL_TABLENAME + " add cause");

                }            
                cursor.close();           
                upgradeVersion=21;
            }
            
        
        }
     }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int count = 0;
        switch (sMatcher.match(uri)) {
            case InterceptConstants.ITEM:
                count = db.delete(InterceptConstants.TABLENAME, selection, selectionArgs);
                break;
            case InterceptConstants.ITEM_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete(InterceptConstants.TABLENAME, InterceptConstants.COLUMN_BLOCK_NAME_ID
                        + "=" + id + (selection != null && !"".equals(selection)
                        ? " AND("+selection+")":""), selectionArgs);
                break;
            case InterceptConstants.CALL_HISTORY_ITEM:
                count = db.delete(InterceptConstants.CALL_TABLENAME, selection, selectionArgs);
                if(InterceptConstants.DBUG){
                    Log.i(InterceptConstants.TAG, "CALL_HISTORY_ITEM count:"+count);
                }
                break;
            case InterceptConstants.CALL_HISTORY_ITEM_ID:
                id = uri.getPathSegments().get(1);
                if(InterceptConstants.DBUG){
                    Log.i(InterceptConstants.TAG, "CALL_HISTORY_ITEM_ID:"+id);
                }
                count = db.delete(InterceptConstants.CALL_TABLENAME, InterceptConstants.COLUMN_CALL_ID
                        + "=" + id + (selection != null && !"".equals(selection)
                            ? " AND("+selection+")":""), selectionArgs);
                break;
            case InterceptConstants.MSG_HISTORY_ITEM:
                count = db.delete(InterceptConstants.MSG_TABLENAME, selection, selectionArgs);
                break;
            case InterceptConstants.MSG_HISTORY_ITEM_ID:
                id = uri.getPathSegments().get(1);
                if(InterceptConstants.DBUG){
                    Log.i(InterceptConstants.TAG, "MSG_HISTORY_ITEM_ID:"+id);
                }
                count = db.delete(InterceptConstants.MSG_TABLENAME, InterceptConstants.COLUMN_MSG_ID
                        + "=" + id + (selection != null && !"".equals(selection)
                            ? " AND("+selection+")":""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (sMatcher.match(uri)) {
            case InterceptConstants.ITEM:
                return InterceptConstants.CONTENT_TYPE;
            case InterceptConstants.DND_ITEM:
                return InterceptConstants.CONTENT_TYPE;
            case InterceptConstants.MSG_HISTORY_ITEM:
                return InterceptConstants.CONTENT_TYPE;
            case InterceptConstants.CALL_HISTORY_ITEM:
                return InterceptConstants.CONTENT_TYPE;
            case InterceptConstants.ITEM_ID:
                return InterceptConstants.CONTENT_ITEM_TYPE;
            case InterceptConstants.MSG_HISTORY_ITEM_ID:
                return InterceptConstants.CONTENT_TYPE;
            case InterceptConstants.CALL_HISTORY_ITEM_ID:
                return InterceptConstants.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if(InterceptConstants.DBUG){
            Log.i(InterceptConstants.TAG, "InterceptProvider insert uri:"+uri);
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long rowId = -1;
        if(InterceptConstants.DBUG){
            Log.i(InterceptConstants.TAG, "InterceptProvider insert match:"+sMatcher.match(uri));
        }
        if (sMatcher.match(uri) != InterceptConstants.ITEM
                && sMatcher.match(uri) != InterceptConstants.DND_ITEM
                && sMatcher.match(uri) != InterceptConstants.CALL_HISTORY_ITEM
                && sMatcher.match(uri) != InterceptConstants.MSG_HISTORY_ITEM ){
            throw new IllegalArgumentException("Unknown URI"+uri);
        }
        switch (sMatcher.match(uri)) {
            case InterceptConstants.ITEM:
                rowId = db.insert(InterceptConstants.TABLENAME,InterceptConstants.COLUMN_BLOCK_NAME_ID,values);
                break;
            case InterceptConstants.DND_ITEM:
                rowId = db.insert(InterceptConstants.DND_TABLENAME,InterceptConstants.COLUMN_DND_ID,values);
                break;
            case InterceptConstants.MSG_HISTORY_ITEM:
                rowId = db.insert(InterceptConstants.MSG_TABLENAME,InterceptConstants.COLUMN_MSG_ID,values);
                break;
            case InterceptConstants.CALL_HISTORY_ITEM:
                rowId = db.insert(InterceptConstants.CALL_TABLENAME,InterceptConstants.COLUMN_CALL_ID,values);
                break;
            default:
                break;
        }

        if(rowId>0){
            Uri noteUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }
        throw new IllegalArgumentException("Unknown URI"+uri);
    }

    @Override
    public boolean onCreate() {
        this.dbHelper = new DatabaseHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = null;
        String id = null;
        Log.d(InterceptConstants.TAG, "InterceptProvider " +String.valueOf(sMatcher.match(uri)));
        switch (sMatcher.match(uri)) {
            case InterceptConstants.ITEM:
                c = db.query(InterceptConstants.TABLENAME, projection, selection, selectionArgs, null, null, null);
                break;
            case InterceptConstants.ITEM_ID:
                id = uri.getPathSegments().get(1);
                c = db.query(InterceptConstants.TABLENAME, projection, InterceptConstants.COLUMN_BLOCK_NAME_ID + "=" + id
                        + (selection != null && !"".equals(selection) ? " AND("+selection + ")" : ""),
                        selectionArgs, null, null, sortOrder);
                break;
            case InterceptConstants.DND_ITEM:
                c = db.query(InterceptConstants.DND_TABLENAME, projection, selection, selectionArgs, null, null, null);
                break;
            case InterceptConstants.MSG_HISTORY_ITEM:
                c = db.query(InterceptConstants.MSG_TABLENAME, projection, selection, selectionArgs, null , null, sortOrder);
                break;
            case InterceptConstants.MSG_HISTORY_ITEM_ID:
                id = uri.getPathSegments().get(1);
                c = db.query(InterceptConstants.MSG_TABLENAME, null, InterceptConstants.COLUMN_MSG_ID + "=" + id,
                        null, null, null, null);
                break;
            case InterceptConstants.CALL_HISTORY_ITEM:
                c = db.query(InterceptConstants.CALL_TABLENAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case InterceptConstants.CALL_HISTORY_ITEM_ID:
                id = uri.getPathSegments().get(1);
                c = db.query(InterceptConstants.CALL_TABLENAME, null, InterceptConstants.COLUMN_CALL_ID + "=" + id,
                        null, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int count = 0;
        String id = null;
        switch (sMatcher.match(uri)) {
            case InterceptConstants.ITEM_ID:
                id = uri.getPathSegments().get(1);
                count = db.update(InterceptConstants.TABLENAME, values, InterceptConstants.COLUMN_BLOCK_NAME_ID + "=" + id, null);
                break;
            case InterceptConstants.ITEM:
                count = db.update(InterceptConstants.TABLENAME, values, selection, selectionArgs);
                break;
            case InterceptConstants.DND_ITEM:
                count = db.update(InterceptConstants.DND_TABLENAME, values, selection, selectionArgs);
                break;
            case InterceptConstants.MSG_HISTORY_ITEM:
                count = db.update(InterceptConstants.MSG_TABLENAME, values, selection, selectionArgs);
                if(InterceptConstants.DBUG){
                    Log.i(InterceptConstants.TAG, "MSG COUNT:"+count);
                }
                break;
            case InterceptConstants.CALL_HISTORY_ITEM:
                count = db.update(InterceptConstants.CALL_TABLENAME, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI"+uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}




