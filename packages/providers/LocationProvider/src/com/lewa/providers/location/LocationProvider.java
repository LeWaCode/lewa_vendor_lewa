package com.lewa.providers.location;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class LocationProvider extends ContentProvider{

    private PhoneLocationSQLiteHelper phoneLocationSQLiteHelper;
    public static final String AUTHORITY = "com.lewa.providers.location";
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int LOCATION = 100;
    private static final int CARDTYPE = 101;
    private static final int AREACODE = 102;
    private static final int SPECIAL_PHONE = 103;
    private static final int LOCATION_NUM = 104;
    
    static {
        URI_MATCHER.addURI(AUTHORITY, "location", LOCATION);
        URI_MATCHER.addURI(AUTHORITY, "cardType", CARDTYPE);
        URI_MATCHER.addURI(AUTHORITY, "areacode", AREACODE);
        URI_MATCHER.addURI(AUTHORITY, "special_phone", SPECIAL_PHONE);
        URI_MATCHER.addURI(AUTHORITY, "location/#", LOCATION_NUM);
    }
    
    public interface Columns extends BaseColumns {
        public static final String PHONE_NO = "number";
        public static final String CARDTYPE = "cardType";
        public static final String LOCATION = "location";
        public static final String AREACODE = "areacode";
/*        public static final String SPECIAL_PHONE = "special_phone";*/
    }
    
    public interface Location extends Columns{
        public static final Uri LOCATION_URI = Uri.parse("content://" + AUTHORITY + "/location");
    }
    
    public interface CardType extends Columns{
        public static final Uri CARDTYPE_URI = Uri.parse("content://" + AUTHORITY + "/cardType");
    }
    
    public interface AreaCode extends Columns{
        public static final Uri AREACODE_URI = Uri.parse("content://" + AUTHORITY + "/areacode");
    }
    
    public interface SpecialPhone extends Columns {
        public static final Uri SPECIAL_PHONE_URI = Uri.parse("content://" + AUTHORITY + "/special_phone");
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri arg0) {
        // TODO Auto-generated method stub
        
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        phoneLocationSQLiteHelper = new PhoneLocationSQLiteHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        
        SQLiteDatabase db = phoneLocationSQLiteHelper.getReadableDatabase();
        /*SQLiteQueryBuilder qb = new SQLiteQueryBuilder();*/
        Cursor cursor = null;
        String table = null;
        
        switch (URI_MATCHER.match(uri)) {
        	case LOCATION_NUM:
                cursor = new MatrixCursor(new String[] {"location","cardtype"});
                String []loc = PhoneLocation.getCityAndCardTypeFromPhone(uri.getLastPathSegment());
                if (loc != null) {
                    ((MatrixCursor) cursor).newRow().add(loc[1]).add(loc[0]);
                }
                return cursor;
            case LOCATION:
                table = "areacode";
                selection = "areacode in (select areacode from location where " + selection + ")"; 
                break;
            case AREACODE:
                table = "areacode";
                break;
            case SPECIAL_PHONE:
                table = "special_phone";
            default:
                break;
        }
        if (table != null) {
            try {
                cursor = db.query(table, projection, selection, null, null, null, null);
            } catch (Exception e) {     
                phoneLocationSQLiteHelper.deleteDB();
                phoneLocationSQLiteHelper = new PhoneLocationSQLiteHelper(getContext());
            }
        }        
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

}
