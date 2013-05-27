package com.lewa.store.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lewa.store.download.DBHelper;

public class AppSourceDao {

	String TAG = AppSourceDao.class.getSimpleName();

	private DBHelper dbHelper;

	public AppSourceDao(Context context) {
		dbHelper = new DBHelper(context);
	}

	/**
	 * 存储元数据
	 */
	public synchronized void saveMetaAppInfo(List<AppInfo> infos) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			for (AppInfo info : infos) {
				if (null != info && isNotRecord(info.getUrl())) {
					String sql = "insert into app_info(app_id,app_name,app_author,app_version,app_versioncode,app_packagename,app_description,app_url,app_logourl,app_access,app_size,app_category) values (?,?,?,?,?,?,?,?,?,?,?,?)";
					Object[] bindArgs = {
							info.getAppId(),
							info.getAppName() != null ? info.getAppName()
									.trim() : "",
							info.getAppAuthor() != null ? info.getAppAuthor()
									.trim() : "",
							info.getAppVersion() != null ? info.getAppVersion()
									.trim() : "",
							info.getAppVersionCode(),
							info.getPackageName() != null ? info
									.getPackageName().trim() : "",
							info.getDescription() != null ? info
									.getDescription().trim() : "",
							info.getUrl() != null ? info.getUrl().trim() : "",
							info.getAppLogoUrl() != null ? info.getAppLogoUrl()
									.trim() : "",
							info.getAccess(),
							info.getAppSize(),
							info.getAppCategory() != null ? info
									.getAppCategory().trim() : "" };
					database.execSQL(sql, bindArgs);
				}
			}
			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			database.close();
		}
	}

	// 是否存在相同记录
	private synchronized boolean isNotRecord(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		boolean flag = true;
		try {
			String sql = "select count(*)  from app_info where app_url=?";
			cursor = database.rawQuery(sql, new String[] { urlstr });
			int count = 0;
			if (null != cursor && cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			if (count != 0) {
				flag = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
			/*
			 * if(null!=database){ database.close(); }
			 */
		}
		return flag;
	}

	/**
	 * 获取元数据
	 */
	public synchronized List<AppInfo> getMetaAppInfo() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		List<AppInfo> list = new ArrayList<AppInfo>();
		String sql = "select app_id,app_name,app_author,app_version,app_versioncode,app_packagename,app_description,app_url,app_logourl,app_access,app_size,app_category from app_info";
		Cursor cursor = database.rawQuery(sql, null);
		AppInfo info = null;
		while (null != cursor && cursor.moveToNext()) {
			info = new AppInfo(cursor.getInt(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5),
					cursor.getString(6), cursor.getString(7),
					cursor.getString(8), cursor.getString(9),
					cursor.getInt(10), cursor.getString(11));
			list.add(info);
		}
		if (null != cursor) {
			cursor.close();
		}
		if (null != database) {
			database.close();
		}
		return list;
	}

	/**
	 * 获取元数据通过AppId
	 */
	public synchronized AppInfo getMetaAppInfoById(int AppId) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select app_id,app_name,app_author,app_version,app_versioncode,app_packagename,app_description,app_url,app_logourl,app_access,app_size,app_category from app_info where app_id = "
				+ AppId;
		Cursor cursor = database.rawQuery(sql, null);
		AppInfo info = null;
		if (cursor != null && cursor.moveToFirst()) {
			info = new AppInfo(cursor.getInt(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3),
					cursor.getString(4), cursor.getString(5),
					cursor.getString(6), cursor.getString(7),
					cursor.getString(8), cursor.getString(9),
					cursor.getInt(10), cursor.getString(11));
		}
		if (cursor != null) {
			cursor.close();
		}
		if (null != database) {
			database.close();
		}
		return info;
	}

	/*
	 * get the count of the database
	 */
	public synchronized int getRecordCount() {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		Cursor cursor = null;
		int count = -1;
		try {
			cursor = database.rawQuery("select count(*) from app_info", null);
			cursor.moveToFirst();
			count = cursor.getInt(0);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != cursor) {
				cursor.close();
			}
			if (null != database) {
				database.close();
			}
		}
		Log.i(TAG, "db record numbers==" + count);
		return count;
	}

	/**
	 * clear all data
	 */
	public synchronized void deleteAllData() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			database.delete("app_info", null, null);

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
			Log.i(TAG, "clear all db data");
		}
	}
}
