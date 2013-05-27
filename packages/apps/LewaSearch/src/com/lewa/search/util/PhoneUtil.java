package com.lewa.search.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * This class defines all of the operations related to phone in this programe.
 * 
 * @author		wangfan
 * @version	2012.07.04
 */

public class PhoneUtil {
	
	/**
	 * This method changes phone number with prefix "+86" to standard form
	 * @param phoneNum  phone number with prefix "+86"
	 */
	public static String StandardizePhoneNum(String phoneNum)
	{	
		if(phoneNum.startsWith("+86"))
		{
			return phoneNum.substring(3);
		}
		else
		{
			return phoneNum;
		}
	}
	
	/**
	 * This method gets all apps in this system
	 * @param context  context for loading packageManager
	 */
	public static List<ApplicationInfo> getAllApps(Context context) 
	{ 
		List<ApplicationInfo> apps = new ArrayList<ApplicationInfo>(); 
		PackageManager pManager = context.getPackageManager();
		apps = pManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);   

		Collections.sort(apps,new ApplicationInfo.DisplayNameComparator(pManager));
		
		return apps; 
	} 
	
	/**
	 * This method helps to build component by packageName and className
	 * @param packageName  this string contains packageName and className(packageName/className)
	 */
	public static ComponentName getComponentByName(String packageName)
	{
		String[] nameArr = packageName.split("/");
		ComponentName component = new ComponentName(nameArr[0], nameArr[0] +  nameArr[1]);
		
		return component;
		
	}

}
