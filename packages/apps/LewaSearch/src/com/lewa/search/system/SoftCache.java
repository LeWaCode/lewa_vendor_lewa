package com.lewa.search.system;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.lewa.search.R;
import com.lewa.search.db.ContractDB;
import com.lewa.search.util.PhoneUtil;

/**
 * This class records some datas and structures when the programe starts.
 * It will reduce operations to database and then, reduce time delay in searching process. 
 * @author		wangfan
 * @version	2012.07.04
 */

public class SoftCache {
	
	//number-contact map, phone number is the key
	public static Map<String, String> contractMapNumber;
	//contact-number map, contact name is the key
	public static Map<String, List<String>> numberMapContract; 
	
	//all apps information in this system
	public static List<ApplicationInfo> appList;
	
	//all setting information restored in xml:array 
	public static List<SettingInfo> settingList = new ArrayList<SettingInfo>();; 
	
	//thumbnails of different file items
	public static Drawable musicThumb;
	public static Drawable imageThumb;
	public static Drawable videoThumb;
	public static Drawable apkThumb;
	public static Drawable themeThumb;
	public static Drawable docThumb;
	
	public static Drawable messageThumb;
	public static Drawable contractThumb;
	public static Drawable settingThumb;
	public static Drawable webThumb;
	
	//initialize this cache
	public static void init(Context context)
	{
		loadCache();
		loadThumbs(context);
		loadAppInfo(context);
		loadSettingInfo(context);
	}
	
	/**
	 * This method loads information from contact database.
	 */
	private static void loadCache()
	{
		contractMapNumber = ContractDB.getPhoneMapContract();
		numberMapContract = ContractDB.getContractMapPhone();
	}
	
	/**
	 * This method loads thumbnails from resources.
	 * @param context  context needed for loading resources
	 */
	private static void loadThumbs(Context context)
	{
		Resources res = context.getResources();
		musicThumb = res.getDrawable(R.drawable.music);
		imageThumb = res.getDrawable(R.drawable.picture);
		videoThumb = res.getDrawable(R.drawable.vedio);
		apkThumb = res.getDrawable(R.drawable.apk);
		themeThumb = res.getDrawable(R.drawable.theme);
		docThumb = res.getDrawable(R.drawable.doc);
		
		messageThumb = res.getDrawable(R.drawable.message);
		contractThumb = res.getDrawable(R.drawable.contact);
		settingThumb = res.getDrawable(R.drawable.setting);
		webThumb = res.getDrawable(R.drawable.web);
	}
	
	/**
	 * This method load application information.
	 */
	private static void loadAppInfo(Context context)
	{
		appList = PhoneUtil.getAllApps(context);
	}
	
	/**
	 * This method loads setting information from xml:array.
	 * @param context  context needed for loading resources
	 */
	private static void loadSettingInfo(Context context)
	{
		//load setting information from xml resources to string array 
		Resources res = context.getResources();
		
		String[] idArray = res.getStringArray(R.array.ids_array);
		String[] cNameArray = res.getStringArray(R.array.chinese_name_array);
		String[] eNameArray = res.getStringArray(R.array.english_name_array);
		String[] actionArray = res.getStringArray(R.array.action_name_array);
		String[] packageArray = res.getStringArray(R.array.package_name_array);
		
		settingList.clear();
		//create a temp setting item
		SettingInfo settingItem = null;
		
		//start and end positions of loading setting items in xml
		int start = Constants.SETTING_START_ID;
		int end = Constants.SETTING_END_ID;
		
		//load different contents in different language modes
		if(SystemMode.langeuageMode == Constants.LANGUAGEMODE_CHINESE)
		{
			//load setting information to list
			for(int i = start; i < end; i ++)
			{
				int id = Integer.valueOf(idArray[i]);
				settingItem = new SettingInfo(id, cNameArray[i], eNameArray[i], actionArray[i], packageArray[i]);				
				settingItem.setLanguageMode(Constants.LANGUAGEMODE_CHINESE);

				settingList.add(settingItem);
			}
		}
		else
		{
			//load setting information to list
			for(int i = start; i < end; i ++)
			{
				int id = Integer.valueOf(idArray[i]);
				settingItem = new SettingInfo(id, cNameArray[i], eNameArray[i], actionArray[i], packageArray[i]);				
				settingItem.setLanguageMode(Constants.LANGUAGEMODE_ENGLISH);

				settingList.add(settingItem);
			}
		}
		
		
	}
}
