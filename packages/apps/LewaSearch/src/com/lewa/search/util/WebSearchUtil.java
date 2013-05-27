package com.lewa.search.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.lewa.search.R;
import com.lewa.search.bean.FileInfo;
import com.lewa.search.bean.WebSearchFileInfo;
import com.lewa.search.system.Constants;
import com.lewa.search.system.SoftCache;

/**
 * This class defines all of the operations in searching web.
 * @author		wangfan
 * @version	2012.07.04
 */

public class WebSearchUtil {
	
	//url for searching information
	private static final String searchUrl = "http://m.baidu.com/s";
	//url for searching apps
	private static final String appUrl = "http://m.baidu.com/s?st=10a001&tn=app&f=app%40index%40input";
	
	//lewa token from baidu
	private static final String from = "1602a";
	private static final String token = "lewa";
	
	//thumbnail of search item
	private static Drawable webThumb;
	
	/**
	 * This method searches matched information from the Internet. 
	 * @param context  context for starting activity
	 * @param searchType  two kinds of contents to search:app search and info search
	 * @param key  key of searching
	 */
	public static void webSearch(Context context, int searchType, String key)
	{
		if(searchType == Constants.WEB_SEARCH)
		{
			//build intent for searching information from the Internet
			Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(searchUrl + "?word=" + 
					key + "&from=" + from + "&token=" + token + "&type=app"));
			context.startActivity(viewIntent); 
		}
		else
		{
			//build intent for searching apps from the Internet
			Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(appUrl + "&word=" +
					key + "&from=" + from + "&token=" + token));
			context.startActivity(viewIntent); 
		}
	}
	
	/**
	 * This method takes web search buttons as a kind of file items.
	 * @param context  context for getting resources
	 * @param key  key of searching
	 */
	public static List<FileInfo> buildWebSearchButtons(Context context, String key)
	{
		//load web search item thumbnail
		webThumb = SoftCache.webThumb;
		
		//load resources
		Resources re = context.getResources();
		List<FileInfo> tempList = new ArrayList<FileInfo>();
		
		//build web search title and text 
		String title = re.getString(R.string.web_search);
		String text = re.getString(R.string.web_search_prefix) + key + re.getString(R.string.web_search_suffix);
		//create a web search item
		WebSearchFileInfo fileItem = new WebSearchFileInfo(title, text, webThumb, Constants.WEB_SEARCH, key, Constants.CLASS_WEB);
		tempList.add(fileItem);
		
		//build app search title text
		title = re.getString(R.string.app_search);
		text = re.getString(R.string.app_search_prefix) + key + re.getString(R.string.app_search_suffix);
		
		//create a app search item
		fileItem = new WebSearchFileInfo(title, text, null, Constants.APP_SEARCH, key, Constants.CLASS_WEB);
		tempList.add(fileItem);
		
		return tempList;
	}
	
}
