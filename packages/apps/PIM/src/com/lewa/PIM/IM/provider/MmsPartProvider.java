package com.lewa.PIM.IM.provider;

import im.gexin.talk.data.Const;
import im.gexin.talk.util.UiUtils;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;


public class MmsPartProvider
        extends
        ContentProvider
{
	
	public static String getMmsFileDir(){
	    return new StringBuilder()
	        .append(Environment.getExternalStorageDirectory().getPath())
	        .append("/")
	        .append("LEWA")
	        .append("/")
	        .append("PIM")
	        .toString();
	}
	
	private static final String LOG_TAG = "MmsPartProvider";
	
	@Override
	public boolean onCreate() {
		File mmsFileDir = new File(getMmsFileDir());
        if(!mmsFileDir.exists()){
        	mmsFileDir.mkdirs();
        }
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Uri insert(Uri u, ContentValues values) {
		int id = (int) (Math.random() * 123456789);
		Uri uri = Uri.parse("content://" + Const.MMSPART_PROVIDER_AUTHORITY + "/" + id);
		Log.d(LOG_TAG, "insert " + uri + " succeeded");
		return uri;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		Log.d(LOG_TAG, "open file uri: " + uri);
		int match = sURLMatcher.match(uri);
		switch (match) {
			case MMSPART_ALL:
				break;
			case MMSPART_ALL_ID:
				String id = uri.getPathSegments().get(0);
				Log.d(LOG_TAG, "open file id : " + id);
				int modeBits = UiUtils.modeToMode(uri, mode);
				return ParcelFileDescriptor.open(new File(getMmsFileDir(), id), modeBits);
		}
		return super.openFile(uri, mode);
	}
	
	private static final int        MMSPART_ALL    = 0;
	private static final int        MMSPART_ALL_ID = 1;
	private static final UriMatcher sURLMatcher    = new UriMatcher(UriMatcher.NO_MATCH);
	
	static
	{
		sURLMatcher.addURI(Const.MMSPART_PROVIDER_AUTHORITY, null, MMSPART_ALL);
		sURLMatcher.addURI(Const.MMSPART_PROVIDER_AUTHORITY, "#", MMSPART_ALL_ID);
	}
}
