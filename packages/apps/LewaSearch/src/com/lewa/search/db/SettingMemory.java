package com.lewa.search.db;

import java.util.List;

import com.lewa.search.bean.SettingFileInfo;

/**
 * This class defines a method for searching setting infomation from the system.
 * @author		wangfan
 * @version	2012.07.04
 */
public class SettingMemory {
	
	/**
	 * This method search matched setting items
	 * @param key  key in searching apps
	 * @param sortMode  defines how to sort the list
	 */
	public static List<SettingFileInfo> searchSettings(String key, String sortMode)
	{
		return DBUtil.searchSettingFromMemory(key, sortMode);
	}
}
