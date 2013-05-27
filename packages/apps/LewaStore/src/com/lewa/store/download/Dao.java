package com.lewa.store.download;

import java.util.ArrayList;
import java.util.List;

import com.lewa.store.utils.Constants;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 数据库操作类
 * 
 */
public class Dao {

	private String TAG = Dao.class.getSimpleName();

	private DBHelper dbHelper;

	public Dao(Context context) {
		dbHelper = new DBHelper(context);
	}

	public synchronized boolean isHasInfors(String urlstr) {
		int count = 0;
		try {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			String sql = "select count(*)  from download_info where url=?";
			Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
			if (null != cursor && cursor.moveToFirst()) {
				count = cursor.getInt(0);
			}
			if (null != cursor) {
				cursor.close();
			}
			if (null != database) {
				database.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return count == 0;
	}

	public synchronized boolean isHasInfors(int appId) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select count(*)  from download_info where app_id=?";
		Cursor cursor = database.rawQuery(sql, new String[] { "" + appId });
		int count = 0;
		if (null != cursor && cursor.moveToFirst()) {
			count = cursor.getInt(0);
		}
		if (null != cursor) {
			cursor.close();
		}
		if (null != database) {
			database.close();
		}
		return count == 0;
	}

	public synchronized int getCompletedSize(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select compelete_size from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		int size = 0;
		if (null != cursor && cursor.moveToFirst()) {
			size = cursor.getInt(0);
		}
		if (null != cursor) {
			cursor.close();
		}
		if (null != database) {
			database.close();
		}
		return size;
	}

	public synchronized int getMaxSize(String urlstr) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select max_size from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		int size = 0;
		if (null != cursor && cursor.moveToFirst()) {
			size = cursor.getInt(0);
		}
		if (null != cursor) {
			cursor.close();
		}
		if (null != database) {
			database.close();
		}
		return size;
	}

	public synchronized String getPackageNameById(int packageInt) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String name = "";
		try {
			String sql = "select app_packagename from app_info where app_id=?";
			Cursor cursor = database.rawQuery(sql, new String[] { packageInt
					+ "" });
			if (null != cursor && cursor.moveToFirst()) {
				name = cursor.getString(0).trim();
			} else {
				name = "";
			}
			if (null != cursor) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != database) {
			database.close();
		}
		return name;
	}

	/**
	 * @return packageInt
	 */
	public synchronized int getPackageIntByName(String name) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		int id = 0;
		try {
			String sql = "select app_id from app_info where trim(app_name)=?";
			Cursor cursor = database.rawQuery(sql, new String[] { name });
			if (null != cursor && cursor.moveToFirst()) {
				id = cursor.getInt(0);
			}
			if (null != cursor) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != database) {
			database.close();
		}
		Log.i(TAG, "getPackageIntByName(),id==" + id);
		return id;
	}

	public synchronized String getPackageNameByAppId(int id) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String pkgName = "";
		try {
			String sql = "select app_packagename from app_info where app_id=?";
			Cursor cursor = database.rawQuery(sql, new String[] { id + "" });
			if (null != cursor && cursor.moveToFirst()) {
				pkgName = cursor.getString(0);
			}
			if (null != cursor) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != database) {
			database.close();
		}
		Log.i(TAG, "getPackageNameByAppId(),packagename==" + pkgName);
		return pkgName;
	}
	
	public synchronized String getPackageNameByName(String name) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String pkgName="";
		try {
			String sql = "select app_packagename from app_info where trim(app_name)=?";
			Cursor cursor = database.rawQuery(sql, new String[] { name });
			if (null != cursor && cursor.moveToFirst()) {
				pkgName= cursor.getString(0);
			}
			if (null != cursor) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != database) {
			database.close();
		}
		Log.i(TAG, "getPackageNameByName(),pkgName=" + pkgName);
		return pkgName;
	}

	public synchronized void saveInfos(List<DownloadInfo> infos) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			String sql = "";
			for (DownloadInfo info : infos) {
				sql = "insert into download_info(thread_id,app_id,start_pos, end_pos,max_size,compelete_size,url,status) values (?,?,?,?,?,?,?,?)";
				Object[] bindArgs = { info.getThreadId(), info.getAppId(),
						info.getStartPos(), info.getEndPos(),
						info.getMaxSize(), info.getCompeleteSize(),
						info.getUrl(), info.getStatus() };
				database.execSQL(sql, bindArgs);
			}

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
		}
	}

	public synchronized List<DownloadInfo> getInfos(String urlstr) {
		List<DownloadInfo> list = new ArrayList<DownloadInfo>();
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String sql = "select thread_id, start_pos, end_pos,max_size,compelete_size,appid,url,status from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		DownloadInfo info = null;
		while (cursor.moveToNext()) {
			info = new DownloadInfo(cursor.getInt(0), cursor.getInt(1),
					cursor.getInt(2), cursor.getInt(3), cursor.getInt(4),
					cursor.getInt(5), cursor.getString(6), cursor.getInt(7));
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

	public synchronized void updataInfos(int threadId, int compeleteSize,
			String urlstr) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			String sql = "update download_info set compelete_size=? where thread_id=? and url=?";
			Object[] bindArgs = { compeleteSize, threadId, urlstr };
			database.execSQL(sql, bindArgs);

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
		}
	}

	/**
	 * 
	 * @param fileSize
	 *            文件大小
	 * @param end_pos
	 *            结束位置
	 * @param urlstr
	 */
	public synchronized void updataFileSizeInfo(int fileSize, String urlstr) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			String sql = "update download_info set max_size=? ,end_pos=? where url=?";
			Object[] bindArgs = { fileSize, fileSize - 1, urlstr };
			database.execSQL(sql, bindArgs);

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
		}
	}

	public synchronized void delete(String url) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			database.delete("download_info", "url=?", new String[] { url });

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
		}
	}

	public synchronized void deleteAll() {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			database.delete("download_info", null, null);

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
		}
	}

	/*************************/

	public synchronized String getDownloadUrlById(int appid) {
		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String url = "";
		try {
			String sql = "select url from download_info where app_id=?";
			Cursor cursor = database.rawQuery(sql, new String[] { appid + "" });
			if (null != cursor && cursor.moveToFirst()) {
				url = cursor.getString(0);
			}
			if (null != cursor) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (null != database) {
			database.close();
		}
		Log.d(TAG, "getDownloadUrlById(),url==" + url);
		return url;
	}

	public synchronized int getDownloadStatus(String urlstr) {
		if (!this.isHasInfors(urlstr)) {
			SQLiteDatabase database = dbHelper.getReadableDatabase();
			String sql = "select status from download_info where url=?";
			Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
			cursor.moveToFirst();
			int status = cursor.getInt(0);
			if (null != cursor) {
				cursor.close();
			}
			if (null != database) {
				database.close();
			}
			return status;
		}
		return Constants.BUTTON_STATUS_DOWNLOAD_SUCESS;
	}

	public synchronized void updataAppStatus(int status, String urlstr) {
		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.beginTransaction();
		try {
			String sql = "update download_info set status=? where url=?";
			Object[] bindArgs = { status, urlstr };
			database.execSQL(sql, bindArgs);

			database.setTransactionSuccessful();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			database.endTransaction();
			if (null != database) {
				database.close();
			}
		}
	}

	public synchronized void closeDb() {
		if (null != dbHelper) {
			dbHelper.close();
		}
	}
}
