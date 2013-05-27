package com.lewa.search.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.lewa.search.R;
import com.lewa.search.bean.AppFileInfo;
import com.lewa.search.bean.SettingFileInfo;
import com.lewa.search.system.Constants;
import com.lewa.search.system.SettingInfo;
import com.lewa.search.system.SoftCache;

/**
 * This class defines some methods for searching directly from lower layer.
 * Two ways for searching:One from database, and the other one from the memory.
 * @author		wangfan
 * @version	2012.07.04
 */

public class DBUtil {
	
	//use context to call query method
	public static Context context;
	
	/**
	 * This method initializes this context.
	 * @param context  initialize this context
	 */
	public static void initContext(Context context)
	{
		DBUtil.context = context;
	}
	
	/**
	 * This method searches matched information from database.
	 * @param uri  uri for query
	 * @param projection  columns wants to be searched from table
	 * @param selection  query selection
	 * @param selectionArgs  query selection arguments
	 * @param sortMode  sort mode of this query
	 */
	public static Cursor searchByKey(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortMode)
	{
		//call system query method to visit database
		return context.getContentResolver().query(uri, projection, selection, selectionArgs, sortMode);
	}
	
	/**
	 * This method searches matched application information from the memory.
	 * @param key  key in search
	 * @param sortMode  sort mode of this query, still useless
	 */
	public static List<AppFileInfo> searchAppFromMemory(String key, String sortMode)
	{
		//load packageManager
		PackageManager pManager = context.getPackageManager();
		//load name for different types of applications from resources
		final String systemApp = context.getResources().getString(R.string.system_app);
		final String userApp = context.getResources().getString(R.string.user_app);
		
		//sourceList gets contents from the model class
		List<ApplicationInfo> sourceList = SoftCache.appList;
		//appList provides contents for the view class
		List<AppFileInfo> appList = new ArrayList<AppFileInfo>();
		
		ApplicationInfo sourceInfoItem;
		AppFileInfo appInfoItem;
		
		String title;
		String text;
		String packageName;
		Drawable icon = null;
		
		//get contents from each item in sourceList, then add them into appList
		for(int i = 0; i < sourceList.size(); i ++)
		{
			//get an item
			sourceInfoItem = sourceList.get(i);
			
			//get information of this item
			String appName = (String)sourceInfoItem.loadLabel(pManager);
			title = appName;
			
			if(appName != null && appName.toLowerCase().contains(key.toLowerCase()))
			{
				packageName = sourceInfoItem.packageName;
				
				try
				{
					icon = sourceInfoItem.loadIcon(pManager);
				}
				catch(Exception e)
				{
					Log.v("error", "no icon!");
				}
				
				if((sourceInfoItem.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
				{
					text = systemApp;
					//create a new AppFileInfo, then add to appList
					appInfoItem = new AppFileInfo(title, text, icon, packageName, Constants.APP_SYSTEM_TYPE, Constants.CLASS_APP);
				}
				else
				{
					text = userApp;
					//create a new AppFileInfo, then add to appList
					appInfoItem = new AppFileInfo(title, text, icon, packageName, Constants.APP_USER_TYPE, Constants.CLASS_APP);
				}
				
				
				appList.add(appInfoItem);
			}
		}
		
		return appList;
	}
	
	/**
	 * This method searches matched setting information from the memory.
	 * @param key  key in search
	 * @param sortMode  sort mode of this query, still useless
	 */
	public static List<SettingFileInfo> searchSettingFromMemory(String key, String sortMode)
	{
		//load thumbnail
		Drawable thumbnail = SoftCache.settingThumb;
		//settingList gets contents from the model class
		List<SettingInfo> settingList = SoftCache.settingList;
		//list provides contents for the view class
		List<SettingFileInfo> list = new ArrayList<SettingFileInfo>();
		
		SettingInfo settingItem;
		SettingFileInfo settingInfoItem;
		
		String title;
		String text = context.getResources().getString(R.string.system_setting);
		
		boolean loadThumb = true;
		//get contents from each item in settingList, then add them into list
		for(int i = 0; i < settingList.size(); i ++)
		{
			settingItem = settingList.get(i);
			
			title = settingItem.getNameInCurrentMode();
			
			if(title != null && title.toLowerCase().contains(key.toLowerCase()))
			{
				
				if(loadThumb == true)
				{
					//create a new SettingFileInfo with thumbnail, then add to list
					settingInfoItem = new SettingFileInfo(settingItem.get_id(), title,
							text, thumbnail, settingItem.getActionName(), 
							settingItem.getPackageName(), Constants.CLASS_SETTING);

					loadThumb = false;
				}
				else
				{
					//create a new SettingFileInfo without thumbnail, then add to list
					settingInfoItem = new SettingFileInfo(settingItem.get_id(), title,
							text, null, settingItem.getActionName(),
							settingItem.getPackageName(), Constants.CLASS_SETTING);
				}
				
				list.add(settingInfoItem);
			}
		}
		
		return list;
		
	}
}
