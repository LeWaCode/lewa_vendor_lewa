package com.lewa.filemanager.ds.database.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class OtherFile{
	public static class OtherFileColumns implements BaseColumns {
            private OtherFileColumns() {}
            public static final String AUTHORITY = "com.lewa.app.filemanager";
	    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/files");
	    
	    public static final String CONTENT_TYPE = "vnd.Android.cursor.dir/vnd.lewa.files";
	    public static final String CONTENT_ITEM_TYPE = "vnd.Android.cursor.item/vnd.lewa.files";
	    public static final String DEFAULT_SORT_ORDER = "created ASC";
	    public static final String TABLE_NAME = "files";
	    public static final int VERSION = 1;
	    
	    public static final String TITLE = "title";
	    public static final String DATA = "_data";
	    public static final String SIZE = "_size";
            public static final String DATE_MODIFIED = "date_modified";
            
    }
	

}
