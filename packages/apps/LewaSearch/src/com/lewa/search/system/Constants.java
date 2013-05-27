package com.lewa.search.system;

import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.view.Menu;

/**
 * This class records all of the constants in this programe, these constants are set static for faster loading speed.
 * @author		wangfan
 * @version	2012.07.04
 */

public final class Constants {
	
	//these class types record different searching informaition in this system
	public static final int CLASS_MESSAGE = 1;
	public static final int CLASS_CONTRACT = 2;
	public static final int CLASS_SETTING = 3;
	public static final int CLASS_NORMAL = 4;
	public static final int CLASS_IMAGE = 5;
	public static final int CLASS_VIDEO = 6;
	public static final int CLASS_MUSIC = 7;
	public static final int CLASS_APP = 8;
	public static final int CLASS_WEB = 9;
	
	//this search types record different searching contents on the Internet
	public static final int WEB_SEARCH = 1;
	public static final int APP_SEARCH = 2;
	
	//system uris
	public static final Uri URI_MESSAGE = Uri.parse("content://sms/");
	public static final Uri URI_MESSAGE_THREADS = Uri.parse("content://threads/");
	public static final Uri URI_CONTACT = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	public static final Uri URI_SETTING = null;
	public static final Uri URI_MUSIC = Audio.Media.EXTERNAL_CONTENT_URI;
	public static final Uri URI_IMAGE = Images.Media.EXTERNAL_CONTENT_URI;
	public static final Uri URI_NORMAL = Uri.parse("content://media/external/file");
	public static final Uri URI_VIDEO = Video.Media.EXTERNAL_CONTENT_URI;
	
	//system language mode
	public static final int LANGUAGEMODE_CHINESE = 0;
	public static final int LANGUAGEMODE_ENGLISH = 1;
	
	//this program restores setting information in xml:array
	//when system settings change, update this xml
	//this two integers records start and end position of setting items in this xml
	public static final int SETTING_START_ID = 0;
	public static final int SETTING_END_ID = 30;
	
	//opening contact detail needs this integer
	public static final int OPEN_CONTRACT_TYPE = 2;
	
	//types of applications:system app and user app
	public static final int APP_SYSTEM_TYPE = 1;
	public static final int APP_USER_TYPE = 2;
	
	//notifications for handlers passing message
	public static final int INFO_DATA_CHANGED = 1;
	public static final int INFO_DATA_RESET = 2;
	
	//menu item
	public static final int MENU_SEARCH_SETTING = Menu.FIRST + 1;
	
	//names of preference xml in this program
	public static final String KEY_SELECT_BROWSER = "select_browser";
    public static final String KEY_SELECT_INFO = "select_info";
    public static final String KEY_ABOUT = "about";
}
