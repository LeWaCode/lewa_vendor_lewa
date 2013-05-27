package com.lewa.search.db;

import java.util.List;

import com.lewa.search.bean.AppFileInfo;

/**
 * This class defines a method for searching installed application infomation from the system.
 * @author		wangfan
 * @version	2012.07.04
 */

public class AppMemory {

	/**
	 * This method search matched installed apps
	 * @param key  key in searching apps
	 * @param sortMode  defines how to sort the list
	 */
	public static List<AppFileInfo> searchApps(String key, String sortMode)
	{
		return DBUtil.searchAppFromMemory(key, sortMode);
	}
}
