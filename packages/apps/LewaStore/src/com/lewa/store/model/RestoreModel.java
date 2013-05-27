package com.lewa.store.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lewa.store.download.DBHelper;

public class RestoreModel {
	
	String TAG=RestoreModel.class.getSimpleName();

	private DBHelper dbHelper;

	public RestoreModel(Context context) {
		dbHelper = new DBHelper(context);
	}
	
	//所有正在下载的URL
    private synchronized List<String> downloadUrlList(){
    	List<String> list = new ArrayList<String>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		try {
			String sql = "select url from download_info";
			Cursor cursor = database.rawQuery(sql,null);
			while (null!=cursor && cursor.moveToNext()) {
				list.add(cursor.getString(0).trim());
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null!=database){
			database.close();
		}	
		return list;
    }
    
	public synchronized List<AppInfo> restorePackageItems(){
		List<AppInfo> appList = new ArrayList<AppInfo>();
		List<String> urlList=this.downloadUrlList();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int size=urlList.size();
		for(int i=0;i<size;i++){
			String url=urlList.get(i).trim();
			String sql = "select app_id,app_name,app_author,app_version,app_versioncode,app_packagename,app_description,app_url,app_logourl,app_access,app_size,app_category from app_info where trim(app_url)=?";
			Cursor cursor = database.rawQuery(sql,new String[]{url});
			AppInfo info=null;
			while (cursor.moveToNext()) {
				 info= new AppInfo(cursor.getInt(0),cursor.getString(1), cursor.getString(2), 
						cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),
						cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getInt(10),cursor.getString(11));
				appList.add(info);
			}
			cursor.close();
		}
		if(null!=database){
			database.close();
		}	
		Log.i(TAG, "Restore Data length=="+appList.size());
		return appList;
	}
}
