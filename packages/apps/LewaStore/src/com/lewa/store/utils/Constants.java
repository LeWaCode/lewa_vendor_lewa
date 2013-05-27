package com.lewa.store.utils;

import java.io.File;

import com.lewa.store.R;

import android.os.Environment;

public class Constants {

//	public final static String DOWNLOAD_ICON_URL = "http://static.lewatek.com/pond/packages/icons/";
	public final static String DOWNLOAD_ICON_URL = "http://assets.lewatek.com/pond/packages/icons/";
	public final static String DOWNLOAD_SDPATH = "/mnt/sdcard/";
	public final static int THREAD_COUNT = 1;
	public final static String ACTION_SET_NETWORK="android.settings.SETTINGS";
	public final static String DOWNLOAD_ACTION = "com.lewa.store.download.service";
	public final static String BROADCAST_DOWNLOAD_SERVICE = "com.lewa.store.download.broadcast";// 下载广播
	public final static String BROADCAST_DOWNLOADING_UPDATE = "com.lewa.store.downloading.broadcast";// 更新ManageActivity下载状态
	public final static String BROADCAST_BEGIN_DOWNLOAD_SERVICE = "com.lewa.store.begin.download.broadcast";// 开始下载的广播
	public final static String BROADCAST_UPDATE_APP_DETAIL_STATUS = "com.lewa.store.update.app.detail.broadcast";// 开始下载的广播
	public final static String BROADCAST_DOWNLOAD_SUCESS = "com.lewa.store.download.sucess.broadcast";// download
	public final static String BROADCAST_INSTALL_SUCESS = "com.lewa.store.install.sucess.broadcast";// download
																										// ok
	public final static String BROADCAST_REFRESH_STATUS = "com.lewa.store.refresh.status.broadcast";
	public final static String BROADCAST_MANAGE_ADAPTER_CHANGED = "com.lewa.store.manage.adapter.changed.broadcast";
	public final static String BROADCAST_MANAGE_SET_NO_DATA = "com.lewa.store.manage.set.nodata.broadcast";
	/** 环境 **/
	public static final String EXTERNAL_STORAGE_DIR = Environment
			.getExternalStorageDirectory().toString();
	public static final String LEWA_DIR = EXTERNAL_STORAGE_DIR + "/LEWA";
	public static final String LEWA_CSTORE_DIR = LEWA_DIR + "/cstore";
	public static final String DATA_DIR = LEWA_CSTORE_DIR + "/data";
	public static final String INFO = DATA_DIR + "/.info";
	public static final String IMAGE = DATA_DIR + "/.image";
	public static final String ERROR_LOG_DIR = LEWA_CSTORE_DIR + "/.log";
	public static final String SD_PATH =DATA_DIR + File.separator;
	public static final String PACKAGE_DATA_DIR="/data/data/";

	public static final int NO_STORAGE_ERROR = -1;
	public static final long LOW_STORAGE_THRESHOLD = 512L * 1024L;
	public static final int STORAGE_STATUS_OK = 0;
	public static final int STORAGE_STATUS_LOW = 1;
	public static final int STORAGE_STATUS_NONE = 2;
	
	public static final String HTTP_REQUEST_URL="http://api.lewatek.com/v1/resource.json";
//	public static final String HTTP_REQUEST_URL="http://api.lewa.me/v1/resource.json";
	public static final int HTTP_TIME_OUT_NUM=5000;//单位：毫秒
	public static final int HTTP_REQUEST_ERROR=555;
	
	public static final String REFRESH_FLAG_STR="refreshLocal";
	public static final int REFRESH_GETLOCALDATA=1;
	public static final int REFRESH_NOT_GETLOCALDATA=0;

	public static final String APK_FILE_SUFFIX = ".apk";
	public static final String ICON_FILE_SUFFIX = ".png";
	public static final int FILE_SIZE_NEGATIVE = -1;

	// status
	public static final int BUTTON_STATUS_DOWNLOADING = 1;// 下载中
	public static final int BUTTON_STATUS_CANCEL_DOWNLOADING = 2;// 取消下载
	public static final int BUTTON_STATUS_DOWNLOAD_SUCESS = 3;// 下载完成
	public static final int PROGRESSBAR_TEMPSIZE=20000;
	public static final int PROGRESSBAR_NOTIFICATION_TEMPSIZE=204800;

	// package
	public final static String PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
	public final static String PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
	// download access
	public final static String DOWNLOAD_ACCESS_GUEST = "0";
	public final static String DOWNLOAD_ACCESS_REGISTER = "1";
	public final static String DOWNLOAD_ACCESS_PRIVATE = "2";

	public final static String[] letters = { "A", "B", "C", "D", "E", "F", "G",
			"H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T",
			"U", "V", "W", "X", "Y", "Z", "0", "1", "2", "3", "4", "5", "6",
			"7", "8", "9" };
	public final static int strNums = 4;
	public final static String updateFlag = "cstore_update";
	public final static String updateTime = "cstore_date";
	public final static String year_month_day = "yyyy-MM-dd";
	public final static String onClickRefresh = "refresh";	
	
	public final static String SETTING_NAME="system_setting";
	
    public final static int SETTING_UPDATE_OPEN=1;
    public final static int SETTING_NETWORK_WIFI_OPEN=0;
    public final static int SETTING_NETWORK_ANYNETWORK_OPEN=1;
    
    public final static String SETTING_IS_UPDATE_FLAG="isOpenUpdate";
    public final static String SETTING_NETWORK_FLAG="networkStatus";
    
    public final static int HandlerWhatFlagOne=1;
    public final static int HandlerWhatFlagTwo=2;
    public final static int HandlerWhatFlagThree=3;//网络错误
    public final static int HandlerWhatFlagFour=4;//其他错误
    public final static int HandlerWhatFlagFive=5;//no space on device
    public final static int HandlerWhatFlagSix=6;//unexpected end of stream
    public final static int HandlerWhatFlagSeven=7;//No such file or directory
    public final static int HandlerWhatFalgInstallFailed=8;//install apk failed
    public final static int HandlerWhatFlagStopProcess=9;

    
    public final static int HandlerWhatFalgGoogleServiceNotice=7;
    public final static int HandlerWhatFlagDownloading=5;
    
    public final static int MAX_DOWNLOAD_NUMBERS=2;
    
    public final static int REFRESH_YES=1;
    public final static int REFRESH_NO=0;
    
    public final static String GOTO_LOGIN_ACTION="cstore.goto.action";
    public final static String START_POND_SERVICE="storeStartPondService";
    
    //Notification
    public final static int NOTIFICATION_DOWNLOAD_SUCESS_ID=10086;
    public final static int NOTIFICATION_DOWNLOAD_FAILED_ID=10010;
    public final static int NOTIFICATION_DOWNLOADING_ID=122;
    public final static int NOTIFICATION_GOOGLE_APP_REBOOT=123;
    
    public final static String UPDATE_INSTALLED_ITEMS="update_installed_items";
    public final static int UPDATE_INSTALLED_ITEMS_ID=100;
    public final static String UPDATE_INSTALLED_SUCESS_ITEMS="update_installed_sucess_items";
    public final static int UPDATE_INSTALLED_SUCESS_ITEMS_ID=200;
    public final static int UPDATE_MANAGE_ADAPTER_VIEWS_ID=201;
    
    public final static String UPDATE_APPLIST_ITEMS="update_applist_items";
    public final static int UPDATE_APPLIST_ITEMS_ID=101;
    public final static int UPDATE_APPLIST_ALL_ITEMS_ID=111;
    
    public final static int MANAGE_GROUP_NUMS=3;
    public final static int MANAGE_GROUP_NUMS_TWO=2;
    
    //notification id
    public final static int NOTIFICATION_UPDATE_MESSAGE_ID=155;    
    public final static int STORAGE_EXCEPIION_NOTIFICATION_ID=322;
    
    //refresh view
    public final static String BROADCAST_MANAGE_UPDATE_VIEW = "com.lewa.store.manage.update.view.broadcast";
	public final static String BROADCAST_APPLIST_UPDATE_VIEW = "com.lewa.store.applist.update.view.broadcast";
	public final static String BROADCAST_BASKET_UPDATE_VIEW = "com.lewa.store.basket.update.view.broadcast";
	public final static String BROADCAST_BASKET_ADD_DATA = "com.lewa.store.basket.add.data.broadcast";
	public final static String BROADCAST_BASKET_REMOVE_DATA = "com.lewa.store.basket.remove.data.broadcast";
	public final static String BROADCAST_UPDATE_PROGRESSBAR = "com.lewa.store.update.progressbar.broadcast";
	public final static String BROADCAST_REMOVE_PROGRESSBAR = "com.lewa.store.remove.progressbar.broadcast";
	//google特殊处理包
	
	public final static String ASSETS_EXTRAS_FILE_NAME="extras.xml";
	
    public final static String SYSTEM_DIR="/system/app";
    public final static String[] goos={
        "com.android.calendar",//谷歌日历
        "com.google.android.gsf",//谷歌底层服务
        "com.google.android.backup",//谷歌备份同步
        "com.google.android.syncadapters.calendar",//谷歌日历同步
        "com.google.android.syncadapters.contacts",//谷歌通讯录同步
        "com.android.vending",//谷歌电子市场
        "com.google.android.gm",//gmail
        "com.google.android.talk",//gtalk
        "com.google.android.apps.maps",//google maps
        "com.google.android.location"//networkLocation
    };
    
	public static String[] whitelist=new String[]{"com.autonavi.minimap","com.socogame.ppc","com.bel.android.dspmanager"};
    
	public class Google{
		public final static String GOOGLE_CALENDAR="GoogleCalendar.apk";
		public final static String GOOGLE_SERVICE_FRAMEWORK="GoogleServicesFramework.apk";
	    public final static String GOOGLE_BACKUP_TRANSPORT="GoogleBackupTransport.apk";
	    public final static String GOOGLE_CALENDAR_SYNC_ADAPTER="GoogleCalendarSyncAdapter.apk";
		public final static String GOOGLE_CONTACTS_SYNC_ADAPTER="GoogleContactsSyncAdapter.apk";
	    public final static String GOOGLE_MARKET="GoogleMarket.apk";
	    public final static String GOOGLE_GMAIL="Gmail.apk";
	    public final static String GOOGLE_GTALK="Gtalk.apk";
	    public final static String GOOGLE_MAPS="GoogleMaps.apk";
	    public final static String GOOGLE_LOCATION="GoogleLocation.apk";
	}

	//异常
	public final static String EXCEPITION_IO_NO_SPACE_LEFT_ON_DEVICE="No space left on device";
	public final static String EXCEPITION_IO_UNEXPECTED_END_OF_STREAM="unexpected end of stream";
	public final static String EXCEPITION_NO_SUCH_FILE_OR_DIRECTORY="No such file or directory";//创建目录异常
	
	//dialog
	public final static int MESSAGE_DIALOG_ID_01=1;
	public final static int MESSAGE_DIALOG_ID_02=2;
	public final static int MESSAGE_DIALOG_ID_03=3;
	public final static int MESSAGE_DIALOG_ID_04=4;
	
	//应用安装位置参数
	public static final int APP_INSTALL_AUTO = 0;
    public static final int APP_INSTALL_DEVICE = 1;
    public static final int APP_INSTALL_SDCARD = 2;    
    public static final String APP_INSTALL_DEVICE_ID = "device";
    public static final String APP_INSTALL_SDCARD_ID = "sdcard";
    public static final String APP_INSTALL_AUTO_ID = "auto";	
    public static final String SET_INSTALL_LOCATION = "set_install_location";
	public static final String DEFAULT_INSTALL_LOCATION = "default_install_location";
	
	//pool
	public static final int COREPOOLSIZE=1;
	public static final int MAXPOOLSIZE=1;
	public static final int KEEPALIVETIME=10;	
}
