package com.lewa.search.util;

import java.io.File;

import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Telephony.Threads;
import android.widget.Toast;

import com.lewa.search.R;
import com.lewa.search.db.AppMemory;
import com.lewa.search.db.ContractDB;
import com.lewa.search.db.ImageDB;
import com.lewa.search.db.MessageDB;
import com.lewa.search.db.MusicDB;
import com.lewa.search.db.NormalDB;
import com.lewa.search.db.VideoDB;
import com.lewa.search.db.SettingMemory;
import com.lewa.search.system.Constants;
import com.lewa.search.system.MimeTypeMap;

/**
 * This class defines all of the file operations in this program.
 * @author		wangfan
 * @version	2012.07.04
 */

public class FileInfoUtil {
	

	/**
	 * This method searchs contents by key.
	 * This method call different database helpers with different class types.
	 * @param classType  defines which kind of information to search
	 * @param key  search key
	 * @param sortMode  defines how to sort the list
	 */
	public static List<?> searchContentsByKey(int classType, String key, String sortMode)
	{
		switch(classType)
		{
			case Constants.CLASS_MESSAGE:
			{
				//search message
				return MessageDB.searchMessages(key, sortMode);
			}
			
			case Constants.CLASS_CONTRACT:
			{
				//search contact
				return ContractDB.searchContracts(key, sortMode);
			}
			
			case Constants.CLASS_NORMAL:
			{
				//search normal files, not include some media files
				return NormalDB.searchNormals(key, sortMode);
			}
			
			case Constants.CLASS_IMAGE:
			{
				//search image files
				return ImageDB.searchImages(key, sortMode);
			}
			
			case Constants.CLASS_MUSIC:
			{
				//search music files
				return MusicDB.searchMusics(key, sortMode);
			}
			
			case Constants.CLASS_APP:
			{
				//search apps in this system
				return AppMemory.searchApps(key, sortMode);
			}
			
			case Constants.CLASS_VIDEO:
			{
				//search video files
				return VideoDB.searchVideos(key, sortMode);
			}
			
			case Constants.CLASS_SETTING:
			{
				//search setting information of this system
				return SettingMemory.searchSettings(key, sortMode);
			}
		
		}
		return null;
	}
	
	//refer to file manager ~~~ begin ~~~
	public static void openFile(String filePath, Context context) 
	{
		
		File aFile = null;
		if(filePath != null)
		{
			aFile = new File(filePath);
		}
		else
		{
			return;
		}
		
        if (!aFile.exists()) 
        {
            return;
        }
        
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(aFile);
        String type = MimeTypeMap.getSingleton().getExtensionToMimeTypeMap().get(
               StringUtil.getFileExtension((aFile.getName()).toString()));
        
        if (type == null) 
        {
            Toast.makeText(context, R.string.find_no_associated_app, Toast.LENGTH_SHORT).show();
            return;
        }
        
        type = type.startsWith("package") ? "application" + type.substring(type.indexOf("/")) : type;
       
        intent.setDataAndType(data, type == null ? "*/*" : type);
        try 
        {
            context.startActivity(intent);
        } 
        catch (ActivityNotFoundException a) 
        {
            Toast.makeText(context, R.string.find_no_associated_app, Toast.LENGTH_LONG).show();
        }
    }
	//refer to file manager ~~~ end ~~~
	
	//refer to PIM ~~~ begin ~~~
	public static void openMessages(Context context, String mRowid, String mThreadId, String mNumber)
	{
		if(MessageDB.isGroupThread(Integer.valueOf(mRowid))) 
		{
            final Intent onClickIntent = createIntentNewMessage(context, Long.valueOf(mThreadId));
            context.startActivity(onClickIntent);
            
        }
		else 
		{
            final Intent onClickIntent = createIntentComposeMessage(context, Long.valueOf(mThreadId));
            onClickIntent.putExtra("thread_id", mThreadId);
            onClickIntent.putExtra("select_id", Long.valueOf(mRowid));
            onClickIntent.putExtra("number", mNumber);
            context.startActivity(onClickIntent);                        
        }
	}
	//refer to PIM ~~~ end ~~~
	
	/**
	 * This method opens a certain app by packageName.
	 * This method refer to resource on the Internet.
	 * @param context  context needed for loading packageManager
	 * @param packageName  helps to locate this app
	 */
	public static boolean openApp(Context context, String packageName) 
	{ 
		PackageManager pManager = context.getPackageManager();
		
		PackageInfo pi = null;
		try
		{
			pi = pManager.getPackageInfo(packageName, 0); 
		}
		catch(Exception e)
		{
			return false;
		}

		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null); 
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER); 
		resolveIntent.setPackage(pi.packageName);

		List<ResolveInfo> apps = pManager.queryIntentActivities(resolveIntent, 0); 
		
		if(apps.size() > 0)
		{
			ResolveInfo ri = apps.iterator().next(); 
			
			if(ri != null ) 
			{ 
				String realName = ri.activityInfo.packageName; 
				String className = ri.activityInfo.name; 

				Intent intent = new Intent(Intent.ACTION_MAIN); 
				intent.addCategory(Intent.CATEGORY_LAUNCHER); 
				ComponentName cn = new ComponentName(realName, className); 

				intent.setComponent(cn); 
				context.startActivity(intent); 
				
				return true;
			}
		}
		
		return false;
	} 
	
	/**
	 * This method opens a setting item.
	 * @param context  context needed for loading packageManager
	 * @param actionName  the actionName of this setting
	 * @param packageName  helps to locate this app
	 */
	public static void openSetting(Context context, String actionName, String packageName)
	{
		//get component from packageName
		ComponentName component = PhoneUtil.getComponentByName(packageName);
		
		//build intent for opening this setting item
		Intent intent = new Intent(); 
		intent.setAction(actionName);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(component);
		context.startActivity(intent); 
	}
	
	//refer to PIM ~~~ begin ~~~
	public static void openContract(Context context, long id)
	{
		Intent viewDetailIntent = new Intent("com.lewa.intent.action.VIEW_PIM_DETAIL");
        
        viewDetailIntent.putExtra("contact_id", id);
        viewDetailIntent.putExtra("type", Constants.OPEN_CONTRACT_TYPE);
        context.startActivity(viewDetailIntent);
	}
	
	public static Intent createIntentNewMessage(Context context, long threadId) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName("com.lewa.PIM", "com.lewa.PIM.ui.NewMessageComposeActivity");
        
        if (threadId > 0) {
            intent.setData(getUri(threadId));
            intent.setComponent(component);
        }

        return intent;
   }
	
	private static Intent createIntentComposeMessage(Context context, long threadId) {
        Intent intent = new Intent();
        ComponentName component = new ComponentName("com.lewa.PIM", "com.lewa.PIM.ui.DetailEntry");
        
        if (threadId > 0) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(getUri(threadId));
            intent.setComponent(component);
        }

        return intent;
   }
	
	public static Uri getUri(long threadId) {
        // TODO: Callers using this should really just have a Conversation
        // and call getUri() on it, but this guarantees no blocking.
		return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }
  
	//~~~ end ~~~
	
}
