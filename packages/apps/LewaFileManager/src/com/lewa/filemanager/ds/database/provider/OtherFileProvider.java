package com.lewa.filemanager.ds.database.provider;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class OtherFileProvider extends ContentProvider {
	
	private static final String TABLE_NAME = "files";
	private static final int TABLES = 1;
	private static final int TABLE_ID = 2;
	private static final UriMatcher sUriMatcher ;
	static{
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(OtherFile.OtherFileColumns.AUTHORITY, "files", TABLES);
		sUriMatcher.addURI(OtherFile.OtherFileColumns.AUTHORITY, "files/#", TABLE_ID);
	};


	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db.delete(TABLE_NAME, arg1, arg2);
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
			case TABLES:
			return OtherFile.OtherFileColumns.CONTENT_TYPE;
			case TABLE_ID:
			return OtherFile.OtherFileColumns.CONTENT_ITEM_TYPE;
			default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

	}

	int i = 1;
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		// TODO Auto-generated method stub
		if (sUriMatcher.match(uri) != TABLES) {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, null, values);
		if (rowId > 0) {
			Uri myUri= ContentUris.withAppendedId(OtherFile.OtherFileColumns.CONTENT_URI, rowId);
			return myUri;
		}
		throw new SQLException("Failed to insert row into " + uri);

	}

	DatabaseHelper mOpenHelper ;
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mOpenHelper = new DatabaseHelper(getContext());	
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (sUriMatcher.match(uri)) {
			case TABLES:
				qb.setTables(TABLE_NAME);
				break;
			case TABLE_ID:
				qb.setTables(TABLE_NAME);
				qb.appendWhere(OtherFile.OtherFileColumns._ID + "="
						+ uri.getPathSegments().get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, sortOrder);
		return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db.update(TABLE_NAME, values, selection, selectionArgs);
	}

}
