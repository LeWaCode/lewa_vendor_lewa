package com.lewa.store.activity;

import android.os.Process;

import com.lewa.store.utils.FileHelper;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.lewa.core.base.APIDefinitions;
import com.lewa.core.base.APIException;
import com.lewa.core.base.StorePackage;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lewa.store.R;
import com.lewa.store.adapter.AppListAdapter;
import com.lewa.store.adapter.PreferencesHelper;
import com.lewa.store.dialog.IMessageDialogListener;
import com.lewa.store.dialog.MessageDialog;
import com.lewa.store.download.Dao;
import com.lewa.store.download.DownloadHelper;
import com.lewa.store.extras.XmlContentHandler;
import com.lewa.store.items.LewaStoreFrontReconstructor;
import com.lewa.store.items.LewaStoreFrontReconstructor.LewaStoreFrontInterface;
import com.lewa.store.model.AppInfo;
import com.lewa.store.model.AppListModel;
import com.lewa.store.model.AppSourceDao;
import com.lewa.store.model.LewaNotification;
import com.lewa.store.model.SpecAppInfo;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.StrUtils;
import com.lewa.store.utils.SystemHelper;
import com.lewa.store.utils.TimeHelper;
import com.lewa.store.utils.NetHelper;
import com.lewa.store.utils.StorageCheck;

//import android.content.pm.IPackageInstallObserver.Stub;

/**
 * Here should show all Apps
 * 
 * @author ypzhu
 */
public class AppListActivity extends BaseClass implements
		LewaStoreFrontInterface {

	private String TAG = AppListActivity.class.getSimpleName();

	private String localfile = null;
	public List<String> listTag = new ArrayList<String>();
	public ListView listview = null;
	public List<AppInfo> allAppList = null;
	private RelativeLayout reLayout = null;
	public AppListAdapter adapter = null;
	private AppInfo ai = null;
	private Intent intent = null;
	private DownloadHelper downloadHelper = null;
	public PkgManager pkg = null;
	private Dao dao = null;
	private AppSourceDao asd = null;
	private PreferencesHelper sp = null;
	private int networkStatusCode = 0;// 0 or 1
	private LaunchApp la = null;
	private LewaNotification lewaNotification = null;
	private AppListModel alm = null;

	public List<HashMap<String, Object>> systemAppPackagesMap = null;
	private DownloadHelper dHelper = null;
	private MessageDialog mDialog = null;
	private boolean isRemoteData = false;
	private int refreshCode = Constants.REFRESH_NO;
	private LewaStoreFrontReconstructor storeFront = null;
	private List<String> sortList = null;
	private Context mContext;
	
	public static String xmlContent="";

	public void invalidateViews() {
		if (null != adapter && null != listview) {
			adapter.notifyDataSetChanged();
			listview.invalidateViews();
			Log.i(TAG, "invalidate appListActivity");
		} else {
			Log.e(TAG, "not invalidate appListActivity");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sp = new PreferencesHelper(this, Constants.updateFlag);
		networkStatusCode = sp.getIntValue(Constants.SETTING_NETWORK_FLAG);
		this.mContext = this;
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				xmlContent=FileHelper.getContentFromAssets(mContext,Constants.ASSETS_EXTRAS_FILE_NAME);
			}
		}).start();

		if (NetHelper.isAccessNetwork(this)) {
			setContentView(R.layout.browse);

			reLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
			listview = (ListView) findViewById(R.id.appListView);

			downloadHelper = new DownloadHelper(this);
			lewaNotification = new LewaNotification(this);
			pkg = new PkgManager(this);
			dao = new Dao(this);
			asd = new AppSourceDao(this);
			la = new LaunchApp(this);
			mDialog = new MessageDialog(this);
			alm = new AppListModel(this);
			initPages();
		} else {
			mDialog = new MessageDialog(this);

			LayoutInflater mInflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = mInflater.inflate(R.layout.error, null);
			setContentView(v);
			// toast(getString(R.string.please_set_network));
			// mDialog.ShowInfo(getString(R.string.please_set_network));
//			mDialog.ShowInfo(getString(R.string.MessageDialog_no_network),getString(R.string.MessageDialog_no_network_cannot_connection));
			// mDialog.ShowConfirm(Constants.MESSAGE_DIALOG_ID_03,getString(R.string.MessageDialog_no_network),getString(R.string.MessageDialog_no_network_cannot_connection),
			// new SetNetworkDialogOnClickListener());
		    mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_03, getString(R.string.MessageDialog_no_network),getString(R.string.MessageDialog_no_network_cannot_connection),new SetNetworkDialogOnClickListener());
		}
		FileHelper.createDir(Constants.DATA_DIR + File.separator);
	}

	public void onRefresh() {

		Log.i(TAG,
				"onRefresh(),date==" + sp.getStringValue(Constants.updateTime));

		sp = new PreferencesHelper(this, Constants.updateFlag);
		networkStatusCode = sp.getIntValue(Constants.SETTING_NETWORK_FLAG);

		if (NetHelper.isAccessNetwork(this)) {

			refreshCode = Constants.REFRESH_YES;
			setContentView(R.layout.browse);

			reLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
			listview = (ListView) findViewById(R.id.appListView);

			downloadHelper = new DownloadHelper(this);
			lewaNotification = new LewaNotification(this);
			pkg = new PkgManager(this);
			dao = new Dao(this);
			asd = new AppSourceDao(this);
			la = new LaunchApp(this);
			mDialog = new MessageDialog(this);
			alm = new AppListModel(this);
			initPages();
		} else {
			mDialog = new MessageDialog(this);
			if (null != sortList) {
				sortList.clear();
			}
			LayoutInflater mInflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = mInflater.inflate(R.layout.error, null);
			setContentView(v);
			mDialog.ShowInfo(getString(R.string.please_set_network));
		}
		FileHelper.createDir(Constants.DATA_DIR + File.separator);
	}

	private List<String> getSortList() {

		if (null != sortList && sortList.size() > 0) {
			Log.i(TAG, "sortList is not null");
			return sortList;
		}
		sortList = new ArrayList<String>();

		if (null != this.categories) {
			int size = this.categories.size();
			for (int i = 0; i < size; i++) {
				String category = this.categories.get(i).toString().trim();
				// Log.d(TAG, "list category: " + i + " " + category);
				listTag.add(category);
				sortList.add(category);
				Iterator<Integer> iterator = this.packageCategories.keySet().iterator();
				// Log.e(TAG,"getSortList(),packageCategories size=="+this.packageCategories.size());
				int a=0;
				while (iterator.hasNext()) {
					int packageId = iterator.next();
					String packageCategory = this.packageCategories.get(packageId).trim();
					// Log.d(TAG,String.format("getSortList()  id: %d, category: %s",packageId,packageCategory));
					if (packageCategory.equals(category)) {
						sortList.add("" + packageId);
						a++;
					}
				}
				if(category.equals(getString(R.string.category_life_service))){
					StrUtils.doReverseOrder(sortList, a);
				}else if(category.equals(getString(R.string.category_game))){
					StrUtils.doReverseOrder(sortList, a);
				}
			}
		}
		return sortList;
	}

	Handler dataHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.HandlerWhatFlagOne) {
				if (refreshCode == Constants.REFRESH_YES) {
					adapter = new AppListAdapter(AppListActivity.this,
							allAppList, dao, la, systemMap, listview,
							systemAppPackagesMap, pkg, getSortList(),
							refreshCode, listTag);
					Log.i(TAG, "is refresh,refreshCode==" + refreshCode);
				} else {
					adapter = new AppListAdapter(AppListActivity.this,
							allAppList, dao, la, systemMap, listview,
							systemAppPackagesMap, pkg, getSortList(), listTag);
					Log.i(TAG, "not refresh,refreshCode==" + refreshCode);
				}
				refreshCode = Constants.REFRESH_NO;
				listview.setAdapter(adapter);
				reLayout.setVisibility(View.GONE);
				listview.setVisibility(View.VISIBLE);
			}
		}
	};

	//Store packagename and versioncode
	public Map<String, Integer> systemMap = null;
	public Map<String, Object> tempMap = null;

	public void initSystemMap() {
		if (null == systemMap) {
			systemMap = new HashMap<String, Integer>();
		}
		int size = systemAppPackagesMap.size();
		String pkgName = "";
		for (int i = 0; i < size; i++) {
			tempMap = systemAppPackagesMap.get(i);
			pkgName = tempMap.get("packageName").toString();
			int versioncode = Integer.parseInt(tempMap.get("appVersionCode")
					.toString());
			systemMap.put(pkgName, versioncode);
		}
		if (null != tempMap) {
			tempMap.clear();
			tempMap = null;
		}
	}

	public void initPages() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				try {
					storeFront = new LewaStoreFrontReconstructor(mContext);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(TAG, "storeFront start error!");
				} catch (Exception e) {
					e.printStackTrace();
				}

				systemAppPackagesMap = pkg.getAppPackages();
				initSystemMap();
				try {
					String date = sp.getStringValue(Constants.updateTime);
					if (null == date) {
						isRemoteData = true;
						sp.putValue(Constants.updateTime,
								TimeHelper.getNowTime(Constants.year_month_day));
						getRemoteData();
						Log.d(TAG, "getRemoteData");
					} else if (date.equals(Constants.onClickRefresh)) {
						Log.d(TAG, "refresh,check data from server");
						isRemoteData = false;
						sp.putValue(Constants.updateTime,
								TimeHelper.getNowTime(Constants.year_month_day));
						refreshRemoteData();
					} else {
						if (date.equals(TimeHelper
								.getNowTime(Constants.year_month_day))) {
							if (asd.getRecordCount() == 0) {
								isRemoteData = true;
								sp.putValue(Constants.updateTime, TimeHelper
										.getNowTime(Constants.year_month_day));
								getRemoteData();
								Log.d(TAG+"0", "db count=0,getRemoteData");
							} else {
								isRemoteData = false;
								getLocalData();
								sp.putValue(Constants.updateTime, TimeHelper
										.getNowTime(Constants.year_month_day));
								Log.i(TAG, "getLocalData");
							}
						} else {
							if (isHaveUpdateApps()) {
								isRemoteData = true;
								sp.putValue(Constants.updateTime, TimeHelper
										.getNowTime(Constants.year_month_day));
								getRemoteData();
								Log.d(TAG, "getRemoteData");
							} else if (asd.getRecordCount() == 0) {
								isRemoteData = true;
								sp.putValue(Constants.updateTime, TimeHelper
										.getNowTime(Constants.year_month_day));
								getRemoteData();
								Log.d(TAG+"1", "db count=0,getRemoteData");
							} else {
								isRemoteData = false;
								getLocalData();
								sp.putValue(Constants.updateTime, TimeHelper
										.getNowTime(Constants.year_month_day));
								Log.i(TAG, "getLocalData");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Fetches remote store data from server. This should only be called when
	 * APIDefinitions.loaded is true. If APIDefinitions.loaded is not true and
	 * LewaStoreFrontReconstructor.reconstruct() is called, HTTP url's will be
	 * null values (big problems)
	 * 
	 * @throws IOException
	 */
	private void getRemoteData() throws APIException, IOException {

		if (asd.getRecordCount() != 0) {
			asd.deleteAllData();
		}

//		final LewaStoreFrontReconstructor storeFront = new LewaStoreFrontReconstructor(this);
		final AppListActivity listener = this;
		Thread thread = new Thread() {
			@Override
			public void run() {
				int elapsed = 0;
				int max = 30000;
				while (APIDefinitions.loaded == false) {
					if (elapsed >= max)
						break;
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					elapsed += 1000;
				}
				try {
					storeFront.reconstruct(listener);
				} catch (APIException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.run();
	}

	private boolean isRefresh = false;

	public boolean isRefresh() {
		return isRefresh;
	}

	public void setRefresh(boolean isRefresh) {
		this.isRefresh = isRefresh;
	}

	private void refreshRemoteData() throws IOException {
		this.setRefresh(true);
		try {
			storeFront.reconstruct(this);
		} catch (APIException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> categories = null;
	public Map<Integer, String> packageCategories = null;

	private void setApplicationCategories() {
		AppInfo ai = null;
		if (null != allAppList) {
			categories = new ArrayList<String>();
			packageCategories = new HashMap<Integer, String>();

			int size = allAppList.size();
			for (int i = 0; i < size; i++) {
				ai = allAppList.get(i);

				if (!categories.contains(ai.getAppCategory())) {
					categories.add(ai.getAppCategory().trim());
				}
				packageCategories
						.put(ai.getAppId(), ai.getAppCategory().trim());
			}
		}
	}

	private boolean isHaveUpdateApps() {
		boolean flag = false;
		String localHash = storeFront.getLastLocalHash();
		String remoteHash = storeFront.getLastRemoteHash();
		if (!localHash.equals(remoteHash)) {
			flag = true;
		}
		return flag;
	}

	private void populateStoreFront(StorePackage[] items) {
		Log.d(TAG, "populating store front");
		if (this.isRefresh() && null != allAppList && allAppList.size() > 0) {
			this.setRefresh(false);
			if (!this.isHaveUpdateApps()) {
				Message message = Message.obtain();
				message.what = Constants.HandlerWhatFlagOne;
				dataHandler.sendMessage(message);

				Intent intent = new Intent(Constants.BROADCAST_REFRESH_STATUS);
				sendBroadcast(intent);
				Log.i(TAG, "applist send broadcast refresh ok!");

				sp.putValue(Constants.REFRESH_FLAG_STR,
						Constants.REFRESH_GETLOCALDATA);
				Log.i(TAG, "not have update,Hash is ==");
				return;
			} else {
				this.syncRemoteData(items);
				Log.i(TAG, "remote have update,Hash is not ==");
				return;
			}
		}
		if (this.isRefresh()) {
			this.setRefresh(false);
		}
		allAppList = new ArrayList<AppInfo>();
		categories = new ArrayList<String>();
		packageCategories = new HashMap<Integer, String>();
		String trueUrl = "";
		if (null != items) {
			int length = items.length;
			for (int i = 0; i < length; i++) {
				if (!categories.contains(items[i].getCategory().trim())) {
//					Log.d(TAG, "remote data adding category: "+ items[i].getCategory().trim());
					categories.add(items[i].getCategory().trim());
				}
				packageCategories.put(items[i].getPackageInt(), items[i]
						.getCategory().trim());

				ai = new AppInfo();
				ai.setAppId(items[i].getPackageInt());
				ai.setAppName(items[i].getName());
				ai.setAppVersion(getString(R.string.version_string)
						+ items[i].getVersion());
				trueUrl = downloadHelper.getStoreItemURL(items[i]
						.getPackageInt());
				ai.setUrl(trueUrl);
				ai.setDescription(items[i].getDescription());
				ai.setAppLogoUrl(Constants.DOWNLOAD_ICON_URL
						+ items[i].getPackageInt() + Constants.ICON_FILE_SUFFIX);
				ai.setPackageName(items[i].getFQN().trim());
				ai.setAppVersionCode(items[i].getVersionCode().trim());
				ai.setAccess(items[i].getAccess());
				ai.setAppSize(Integer.parseInt(items[i].getSize().trim()));
				ai.setAppCategory(items[i].getCategory());
				allAppList.add(ai);
			}
			Log.i("isRemoteData", isRemoteData + "");
			if (isRemoteData) {
				asd.saveMetaAppInfo(allAppList);
				Log.i(TAG, "save all data");
			}
			Log.d(TAG,
					String.format("finished processing %d items", items.length));
		}
		Message message = Message.obtain();
		message.what = Constants.HandlerWhatFlagOne;
		dataHandler.sendMessage(message);

		Intent intent = new Intent(Constants.BROADCAST_REFRESH_STATUS);
		sendBroadcast(intent);
		Log.i(TAG, "applist send broadcast refresh ok!");
	}

	private void syncRemoteData(StorePackage[] items) {

		if (asd.getRecordCount() != 0) {
			asd.deleteAllData();
		}
		if (null != sortList) {
			sortList.clear();
		}
		allAppList = new ArrayList<AppInfo>();
		categories = new ArrayList<String>();
		packageCategories = new HashMap<Integer, String>();
		String trueUrl = null;
		if (null != items) {
			int length = items.length;
			for (int i = 0; i < length; i++) {
				if (!categories.contains(items[i].getCategory())) {
					// Log.d(TAG,"remote data adding category: "+
					// items[i].getCategory());
					categories.add(items[i].getCategory());
				}
				packageCategories.put(items[i].getPackageInt(), items[i]
						.getCategory().trim());

				ai = new AppInfo();
				ai.setAppId(items[i].getPackageInt());
				ai.setAppName(items[i].getName());
				ai.setAppVersion(getString(R.string.version_string)
						+ items[i].getVersion());
				trueUrl = downloadHelper.getStoreItemURL(items[i]
						.getPackageInt());
				ai.setUrl(trueUrl);
				ai.setDescription(items[i].getDescription());
				ai.setAppLogoUrl(Constants.DOWNLOAD_ICON_URL
						+ items[i].getPackageInt() + Constants.ICON_FILE_SUFFIX);
				ai.setPackageName(items[i].getFQN().trim());
				ai.setAppVersionCode(items[i].getVersionCode().trim());
				ai.setAccess(items[i].getAccess());
				ai.setAppSize(Integer.parseInt(items[i].getSize().trim()));
				ai.setAppCategory(items[i].getCategory());
				allAppList.add(ai);
			}
			asd.saveMetaAppInfo(allAppList);
			Log.d(TAG, String
					.format("SyncRemoteData,finished processing %d items",
							items.length));
		}
		Message message = Message.obtain();
		message.what = Constants.HandlerWhatFlagOne;
		dataHandler.sendMessage(message);

		Intent intent = new Intent(Constants.BROADCAST_REFRESH_STATUS);
		sendBroadcast(intent);
		Log.i(TAG, "SyncRemoteData,applist send broadcast refresh ok!");
	}

	private List<AppInfo> getLocalData() {

		sp.putValue(Constants.REFRESH_FLAG_STR, Constants.REFRESH_GETLOCALDATA);

		allAppList = new ArrayList<AppInfo>();
		allAppList = asd.getMetaAppInfo();
		this.setApplicationCategories();

		Message message = Message.obtain();
		message.what = Constants.HandlerWhatFlagOne;
		dataHandler.sendMessage(message);
		return allAppList;
	}

	class SetNetworkDialogOnClickListener implements IMessageDialogListener {

		@Override
		public void onDialogClickOk(int requestCode) {
/*			Intent i = new Intent();
			i.setClass(getApplicationContext(), SettingActivity.class);
			startActivity(i);*/
			Intent intent=new Intent();
			intent.setAction(Constants.ACTION_SET_NETWORK);
			startActivity(intent);
		}

		@Override
		public void onDialogClickCancel(int requestCode) {
		}

		@Override
		public void onDialogClickClose(int requestCode) {
		}

	}
	
	class SetNetworkTypeDialogOnClickListener implements IMessageDialogListener {

		@Override
		public void onDialogClickOk(int requestCode) {
			Intent i = new Intent();
			i.setClass(getApplicationContext(), SettingActivity.class);
			startActivity(i);
		}

		@Override
		public void onDialogClickCancel(int requestCode) {
		}

		@Override
		public void onDialogClickClose(int requestCode) {
		}

	}

	public void startUpdate(View v) {

		int status = StorageCheck.checkAppDirsAndMkdirs();
		Log.i(TAG, "status value==" + status);
		switch (status) {
		case Constants.STORAGE_STATUS_NONE:
			mDialog.ShowInfo(getString(R.string.please_insert_sdcard));
			break;
		case Constants.STORAGE_STATUS_LOW:
			mDialog.ShowInfo(getString(R.string.have_no_storage));
			break;
		case Constants.STORAGE_STATUS_OK:
			networkStatusCode = sp.getIntValue(Constants.SETTING_NETWORK_FLAG);
			switch (networkStatusCode) {
			case Constants.SETTING_NETWORK_WIFI_OPEN:
				if (NetHelper.checkWifi(this)) {
					startDownloadTask(v);
				} else {
					mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_01,
							getString(R.string.MessageDialog_no_network),
							getString(R.string.please_set_wifi),
							new SetNetworkDialogOnClickListener());
				}
				break;
			case Constants.SETTING_NETWORK_ANYNETWORK_OPEN:
				if (NetHelper.isAccessNetwork(this)) {
					startDownloadTask(v);
				} else {
					mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_01,
							getString(R.string.MessageDialog_no_network),
							getString(R.string.please_set_network),
							new SetNetworkDialogOnClickListener());
//					mDialog.ShowInfo(getString(R.string.MessageDialog_no_network),getString(R.string.please_set_network));
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	public void startDownload(final View v) {

		int status = StorageCheck.checkAppDirsAndMkdirs();
		Log.i(TAG, "status value==" + status);
		switch (status) {
		case Constants.STORAGE_STATUS_NONE:
			mDialog.ShowInfo(getString(R.string.please_insert_sdcard));
			break;
		case Constants.STORAGE_STATUS_LOW:
			mDialog.ShowInfo(getString(R.string.have_no_storage));
			break;
		case Constants.STORAGE_STATUS_OK:
			networkStatusCode = sp.getIntValue(Constants.SETTING_NETWORK_FLAG);
			switch (networkStatusCode) {
			case Constants.SETTING_NETWORK_WIFI_OPEN:
				if (alm.isExistApkFile(alm.getAppName(v))
						&& dao.getPackageIntByName(alm.getAppName(v)) != 0) {
					toast(getString(R.string.label_install_from_sdcard));
					try {
						new InstallApkTask()
								.execute(v, networkStatusCode, this);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(TAG, "InstallApkTask error:" + e.getMessage());
					}
					if (NetHelper.checkWifi(this)) {
						changeAppStatus(v);
					}
				} else {
					if (NetHelper.checkWifi(this)) {
						startDownloadTask(v);
					} else {
						if (NetHelper.isAccessNetwork(this)) { 
							Dialog dialog = new AlertDialog.Builder(AppListActivity.this)
			                .setTitle(getString(R.string.dialog_title_suggest))
			                .setMessage(getString(R.string.dialog_suggest_content))
			                .setPositiveButton(getString(R.string.menu_setting), new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int whichButton) {
			                    	Intent i = new Intent("android.settings.WIFI_SETTINGS");  
			                    	startActivity(i);
			                    }
			                })
			                .setNegativeButton(getString(R.string.dialog_btn_ignore), new DialogInterface.OnClickListener() {
			                    public void onClick(DialogInterface dialog, int whichButton) {
			                    	PreferencesHelper spHelper = new PreferencesHelper(AppListActivity.this, Constants.updateFlag);
			                    	spHelper.putValue("netsetting","1");
			                    	startDownloadTask(v);
			                    }
			                }).create();
			                dialog.show();
						}else{
							mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_02,
									getString(R.string.MessageDialog_no_network),
									getString(R.string.please_set_wifi),
									new SetNetworkDialogOnClickListener());
						}
					}
				}
				break;
			case Constants.SETTING_NETWORK_ANYNETWORK_OPEN:
				if (alm.isExistApkFile(alm.getAppName(v))
						&& dao.getPackageIntByName(alm.getAppName(v)) != 0) {
					toast(getString(R.string.label_install_from_sdcard));
					try {
						new InstallApkTask()
								.execute(v, networkStatusCode, this);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e(TAG, "InstallApkTask error:" + e.getMessage());
					}
					if (NetHelper.isAccessNetwork(this)) {
						changeAppStatus(v);
					}
				} else {
					if (NetHelper.isAccessNetwork(this)) {
						startDownloadTask(v);
					} else {
						mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_01,
								getString(R.string.MessageDialog_no_network),
								getString(R.string.please_set_network),
								new SetNetworkDialogOnClickListener());
						/*mDialog.ShowInfo(
								getString(R.string.MessageDialog_no_network),
								getString(R.string.please_set_network));*/
					}
				}
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}

	private void changeAppStatus(View v) {
		RelativeLayout layout = (RelativeLayout) v.getParent();
		Button startBtn = (Button) layout.findViewById(R.id.btn_start);
		Button downloadingBtn = (Button) layout
				.findViewById(R.id.app_list_isDownloading);
		Button updateBtn = (Button) layout.findViewById(R.id.app_list_isupdate);
		startBtn.setVisibility(View.GONE);
		updateBtn.setVisibility(View.GONE);
		downloadingBtn.setVisibility(View.VISIBLE);
		// refreshBasketView();
	}

	class InstallApkTask extends AsyncTask<Object, Object, Object> {

		private String TAG = AppListActivity.class.getSimpleName();

		private View v = null;
		private boolean isInstallSuccess = false;
		private int networkCode = 0;
		private Context context = null;
		private String appName = null;
		private String pkgName="";

		@Override
		protected Object doInBackground(Object... params) {
			this.v = (View) params[0];
			this.networkCode = Integer.parseInt(params[1].toString());
			this.context = (Context) params[2];
			this.appName = alm.getAppName(this.v);
			if(null!=dao){
				this.pkgName=dao.getPackageNameByName(this.appName);
			    Log.d(this.TAG,"pkgName 00="+this.pkgName);
			}else{
				Dao d=new Dao(this.context);
				this.pkgName=d.getPackageNameByName(this.appName);
			    Log.d(this.TAG,"pkgName 01="+this.pkgName);
			}

			if (alm.installApkFromSD(this.appName)) {
				this.isInstallSuccess = true;
				Log.i(TAG, "install apk from sdcard success!");
			}
			Log.i(this.TAG, "doInBackground()");
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (this.isInstallSuccess) {
				
				if (null != lewaNotification) {
					lewaNotification.notifyDownloadSuccess(this.appName);
				}
				if (null != dao) {
					addBasketData(dao.getPackageIntByName(appName));
				}
				updateAppListAdapter();
				updateManageData();
			} else {
				toast(this.context
						.getString(R.string.label_install_from_sdcard_failed));
				switch (this.networkCode) {
				case Constants.SETTING_NETWORK_WIFI_OPEN:
					if (NetHelper.checkWifi(this.context)) {
						startDownloadTask(this.v);
					} else {
						mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_02,
								getString(R.string.MessageDialog_no_network),
								getString(R.string.please_set_wifi),
								new SetNetworkDialogOnClickListener());
					}
					break;
				case Constants.SETTING_NETWORK_ANYNETWORK_OPEN:
					if (NetHelper.isAccessNetwork(this.context)) {
						startDownloadTask(this.v);
					} else {
//						mDialog.ShowInfo(getString(R.string.please_set_network));
						mDialog.ShowSpecConfirm(Constants.MESSAGE_DIALOG_ID_01,
								getString(R.string.MessageDialog_no_network),
								getString(R.string.please_set_network),
								new SetNetworkDialogOnClickListener());
					}
					break;
				default:
					break;
				}
			}
			Log.i(this.TAG, "onPostExecute()");
			super.onPostExecute(result);
		}
	}

	private void startDownloadTask(View v) {
		RelativeLayout layout = (RelativeLayout) v.getParent();
		String appName = ((TextView) layout.findViewById(R.id.app_name))
				.getText().toString().trim();
		Button startBtn = (Button) layout.findViewById(R.id.btn_start);
		Button downloadingBtn = (Button) layout
				.findViewById(R.id.app_list_isDownloading);
		Button updateBtn = (Button) layout.findViewById(R.id.app_list_isupdate);

		int appId = this.getDownloadAppId(appName.trim());

		if (getAppAccess(appId).equals(Constants.DOWNLOAD_ACCESS_GUEST)) {

			if (null != this.getAppUrl(appId)) {
				Log.i(TAG, "is guest,can downoad it");
				localfile = Constants.SD_PATH + appName
						+ Constants.APK_FILE_SUFFIX;
				startBtn.setVisibility(View.GONE);
				updateBtn.setVisibility(View.GONE);
				downloadingBtn.setVisibility(View.VISIBLE);

				intent = new Intent();
				intent.setAction(Constants.DOWNLOAD_ACTION);
				intent.putExtra("packageIdInt", appId);
				intent.putExtra("appName", appName);
				intent.putExtra("localfile", localfile);
				startService(intent);

				refreshBasketView();

				// 更新App Detail应用状态
				/*
				 * Intent i = new
				 * Intent(Constants.BROADCAST_UPDATE_APP_DETAIL_STATUS); AppInfo
				 * ai = getDownloadAppInfo(appName);
				 * i.putExtra("downloading_appInfo", ai); sendBroadcast(i);
				 */
			} else {
				toast(getString(R.string.can_not_get_remote_url));
			}
		}
	}

	/***************** update data ******************/
	private void addBasketData(int app_id) {
		// add basket data,to BasketActivity
		Intent ii = new Intent(Constants.BROADCAST_BASKET_ADD_DATA);
		ii.putExtra("appId", app_id);
		sendBroadcast(ii);
	}

	private void updateAppListAdapter() {
		// 给AppListActivity
		Intent intent = new Intent(Constants.BROADCAST_APPLIST_UPDATE_VIEW);
		intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
				Constants.UPDATE_APPLIST_ITEMS_ID);
		sendBroadcast(intent);
	}

	private void updateManageData() {
		// 给ManageActivity
		Intent i = new Intent(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
		i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,
				Constants.UPDATE_INSTALLED_ITEMS_ID);
		sendBroadcast(i);
	}

	/***************** end update data *********************/
	private void refreshBasketView() {
		Intent intent = new Intent(Constants.BROADCAST_BASKET_UPDATE_VIEW);
		sendBroadcast(intent);
		intent = null;
	}

	private String getAppUrl(int appid) {
		dHelper = new DownloadHelper(this);
		return dHelper.getStoreItemURL(appid);
	}

	private int getDownloadAppId(String appName) {
		int size = allAppList.size();
		for (int i = 0; i < size; i++) {
			AppInfo ai = allAppList.get(i);
			if (ai.getAppName().trim().equals(appName)) {
				return ai.getAppId();
			}
		}
		return -1;
	}

	// get Access
	private String getAppAccess(int appid) {
		int size = allAppList.size();
		for (int i = 0; i < size; i++) {
			AppInfo ai = allAppList.get(i);
			if (ai.getAppId() == appid) {
				return ai.getAccess();
			}
		}
		return "";
	}

	class AppListItemOnClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position,
				long arg3) {
			AppInfo ai = allAppList.get(position);
			Intent intent = new Intent(AppListActivity.this,
					AppDetailActivity.class);
			intent.putExtra("appInfo", ai);
			startActivity(intent);
		}
	}

	/**
	 * data load success
	 */
	@Override
	public void onReconstructSuccess(StorePackage[] items) {
		Log.d(TAG,String.format(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> received %d number of packages",items.length));
		this.populateStoreFront(items);
	}

	@Override
	public void onReconstructFailure(APIException exception) {

	}

	@Override
	public void onBackPressed() {
		SystemHelper sh = new SystemHelper(this);
		if (!sh.isDownloading()) {
			sh.stopStoreProcess();
//			sh.beforeStop();
			return;
		} else {
			Log.e(TAG, "there have undone download task,can not stop process!");
			super.onBackPressed();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "AppListActivity onDestroy()");
		if (null != dao) {
			dao.closeDb();
		}
		if (null != sp) {
			sp.putValue(Constants.updateTime,
					TimeHelper.getNowTime(Constants.year_month_day));
		}
		if (adapter != null) {
			adapter.unRegisterUpdateAppListReceiver();
		}
		super.onDestroy();
	}
}
