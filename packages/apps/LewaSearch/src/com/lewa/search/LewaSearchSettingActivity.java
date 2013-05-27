package com.lewa.search;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.lewa.search.system.Constants;

/**
 * This class defines the Activity on "search setting" page.
 * @author		wangfan
 * @version	2012.07.04
 */
public class LewaSearchSettingActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener {
	
	//this listPreference share installed browsers in this system,it updates each time when this activity starts
	private ListPreference listPreference;
	//this string records current default browser
	private String packageDefault;
	private PackageManager pManager;
	
	@Override  
	protected void onCreate(Bundle savedInstanceState) {
	
    	super.onCreate(savedInstanceState);
    	 
    	addPreferencesFromResource(R.xml.search_settings);
    	
    	//initialize the packageManager
    	pManager = LewaSearchSettingActivity.this.getPackageManager();
    	
    	//updates listPreference
    	initListPreference();
    }
	
	/**
	 * This method was used to initialize listPreference.
	 */
	private void initListPreference()
	{
		//get browser list in this system
    	List<ResolveInfo> browserList = getAllBrowsers();
    	
    	int len = browserList.size();
    	CharSequence items[] = new CharSequence[len]; //the name of the browser
    	CharSequence values[] = new CharSequence[len]; //the packageName of the browser
    	
    	for(int i = 0; i < len; i ++)
    	{
    		//register browser name as item text
    		items[i] = browserList.get(i).activityInfo.loadLabel(pManager);
    		//register browser packageName as item value
    		values[i] = browserList.get(i).activityInfo.packageName;
    	}
    	
    	//initialize the listPreference and set value to current default browser
    	listPreference = (ListPreference) findPreference("select_browser");
    	listPreference.setEntries(items);
    	listPreference.setEntryValues(values);
    	listPreference.setOnPreferenceChangeListener(this);
    	listPreference.setValue(packageDefault);
    	
    	CharSequence summary = "";
    	int index = 0;
    	for(; index < values.length; index ++)
    	{
    		if(values[index].equals(packageDefault))
    		{
    			//set current default browser name as summary
    			summary = items[index];
    			break;
    		}
    	}
    	
    	if(index == values.length)
    	{
    		//set a default string as summary if there is no default browser currently
    		summary = this.getResources().getString(R.string.no_default_browser);
    	}
    	
    	//set listPreference a summary
    	listPreference.setSummary(summary);
	}
	
	/**
	 * This method was used to setDefaultBrowser in this system.
	 * @param preference	preference changed
	 * @param newValue    new value user selected
	 */
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
         //TODO Auto-generated method stub
        
		if(preference.getKey().equals(Constants.KEY_SELECT_BROWSER)) 
		{
			//set default browser in this system
			setDefaultBrowser(LewaSearchSettingActivity.this, newValue.toString());
			//update listPreference summary synchronously
			initListPreference();
        } 
		
        return true;
    }

	/**
	 * This method has no use up to now.
	 * @param preference	preference changed
	 */
	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		if(preference.getKey().equals(Constants.KEY_SELECT_BROWSER)) 
		{
			
        } 
		else if(preference.getKey().equals(Constants.KEY_SELECT_INFO)) 
		{
			
        }
		else if(preference.getKey().equals(Constants.KEY_ABOUT)) 
		{
	
		}
        return false;
	}
	
	/**
	 * This method was used to get all browser information from system. 
	 * This method was derived from a certain method on the Internet.
	 */
	private List<ResolveInfo> getAllBrowsers()
	{
		/*
		ArrayList<String> allLaunchers = new ArrayList<String>();
		
		
        Intent allApps = new Intent(Intent.ACTION_MAIN);
        List<ResolveInfo> allAppList = getPackageManager().queryIntentActivities(allApps, 0);
        for(int i = 0; i < allAppList.size(); i++) 
        {
        	allLaunchers.add(allAppList.get(i).activityInfo.packageName);
        }
		*/
		
		//send intent to receive browsers who response to this intent
        Intent myAppIntent = new Intent(Intent.ACTION_VIEW);
        myAppIntent.setData(Uri.parse("http://"));
        //add all of the browsers to a list and return this list
        List<ResolveInfo> myAppList = getPackageManager().queryIntentActivities(myAppIntent, 0);
        //set current default browser to packageDefault string
        ResolveInfo defInfo = getPackageManager().resolveActivity(myAppIntent, 0);
        packageDefault = defInfo.activityInfo.packageName;
        
        return myAppList;
	}
	
	/**
	 * This method was used to set default browser.
	 * This method was derived from a certain method on the Internet.
	 * @param context	context for loading packageManager
	 * @param packageName	packageName of browser which was willing to be set default
	 */
	@SuppressWarnings("deprecation")
	private void setDefaultBrowser(Context context, String packageName)
	{
		PackageManager packageManager = context.getPackageManager();
		//register the category string to set default browser
		String strDefault = "android.intent.category.DEFAULT";
		String strBrowsable = "android.intent.category.BROWSABLE";
		String strView = "android.intent.action.VIEW";
		
		//set intent filter
		IntentFilter filter = new IntentFilter(strView);
		filter.addCategory(strDefault);
		filter.addCategory(strBrowsable);
		filter.addDataScheme("http");
		
		//get default browser willing to set
		Intent resolveIntent = getPackageManager().getLaunchIntentForPackage(packageName);   
		ComponentName cn = resolveIntent.getComponent();

		Intent intent = new Intent(strView);
		intent.addCategory(strBrowsable);
		intent.addCategory(strDefault);
		Uri uri = Uri.parse("http://");
		intent.setDataAndType(uri, null);
		
		List<ResolveInfo> resolveInfoList = packageManager
				.queryIntentActivities(intent, PackageManager.GET_INTENT_FILTERS);

		int size = resolveInfoList.size();
		ComponentName[] arrayOfComponentName = new ComponentName[size];
		for(int i = 0; i < size; i++)
		{
			ActivityInfo activityInfo = resolveInfoList.get(i).activityInfo;
			String packageNameReal = activityInfo.packageName;
			String classNameReal = activityInfo.name;
		
			packageManager.clearPackagePreferredActivities(packageNameReal);
			ComponentName componentName = new ComponentName(packageNameReal, classNameReal);
			arrayOfComponentName[i] = componentName;
		}
		
		//set default browser
		packageManager.addPreferredActivity(filter, IntentFilter.MATCH_CATEGORY_SCHEME, arrayOfComponentName, cn);

	}
	
}
