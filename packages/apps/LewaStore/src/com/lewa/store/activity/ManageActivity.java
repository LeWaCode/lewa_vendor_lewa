package com.lewa.store.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.core.base.APIException;
import com.lewa.core.base.LewaUser;
import com.lewa.core.base.StorePackage;

import com.lewa.store.R;
import com.lewa.store.adapter.ManageBaseAdapter;
import com.lewa.store.dialog.MessageDialog;
import com.lewa.store.download.Dao;
import com.lewa.store.download.Downloader;
import com.lewa.store.extras.GooglePackages;
import com.lewa.store.items.LewaBasketHelper;
import com.lewa.store.model.AppInfo;
import com.lewa.store.model.AppSourceDao;
import com.lewa.store.model.LewaNotification;
import com.lewa.store.model.RestoreModel;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.NetHelper;
import com.lewa.store.utils.StorageCheck;
import com.lewa.store.utils.StrUtils;
import com.lewa.store.utils.SystemHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 管理，包括(downloading,update,installed)
 */
public class ManageActivity extends BaseClass implements
		LewaBasketHelper.OnBasketResponseListener {

	private String TAG = ManageActivity.class.getSimpleName();

	public static List<Integer> downloadings = Collections
			.synchronizedList(new ArrayList<Integer>());
	public static Map<String, Downloader> downloaders = Collections
			.synchronizedMap(new HashMap<String, Downloader>());
	public static Map<String, Integer> ProgressBars = Collections
			.synchronizedMap(new HashMap<String, Integer>());
	public static Map<String, Integer> MapSize = Collections
			.synchronizedMap(new HashMap<String, Integer>());
	public static Map<Integer, String> failedMaps = Collections
			.synchronizedMap(new HashMap<Integer, String>());// <app id,url>

	private TextView tv_nodata;
	private RelativeLayout reLayout = null;
	public ExpandableListView expandListView = null;
	public static ManageBaseAdapter adapter = null;

	private MessageDialog mDialog = null;

	private DownloadServiceReceiver receiver = null;
	private UpdateInstalledReceiver updateInstalledReceiver = null;

	public List<AppInfo> allAppList = null;
	public Dao dao = null;
	private AppSourceDao asd = null;
	private RestoreModel restore = null;
	private LewaNotification notification = null;
	public PkgManager pkg = null;
	private LewaBasketHelper basketHelper = null;
	private LewaUser user = null;
	public List<HashMap<String, Object>> systemAppPackagesMap = null;
	public List<AppInfo> restoreList;
	private Context mContext;

	private GooglePackages gpk = null;
	private List<String> gList = null;
	private Map<String,String> gMap=null;

	@Override
	public void onBasketListSuccess(StorePackage[] storePackages) {

	}

	@Override
	public void onBasketListFailure(APIException e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onBasketPackageDownloadCompletionSuccess(boolean completed) {
		Log.d(TAG, String.format("download completed? " + completed));
	}

	@Override
	public void onBasketPackageDownloadCompletionFailure(APIException e) {
		// TODO Auto-generated method stub
	}

	private boolean isUpdateAllViews = false;

	private void updateAllViews() {
		if (isUpdateAllViews) {
			// update manage views
			Intent i = new Intent(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
			i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,
					Constants.UPDATE_MANAGE_ADAPTER_VIEWS_ID);
			sendBroadcast(i);

			// refresh applist adapter,listview
			Intent intent = new Intent(Constants.BROADCAST_APPLIST_UPDATE_VIEW);
			intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
					Constants.UPDATE_APPLIST_ALL_ITEMS_ID);
			sendBroadcast(intent);

			isUpdateAllViews = false;
		}
	}

	/********************* end update progress ************************/

	/************************** start remove progressbar ******************/
	private RemoveProgressBarReceiver removeProgressBarReceiver = null;

	private void registerRemoveProgressBarReceiver() {
		if (null == removeProgressBarReceiver) {
			removeProgressBarReceiver = new RemoveProgressBarReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_REMOVE_PROGRESSBAR);
			registerReceiver(removeProgressBarReceiver, intentFilter);
		}
	}

	public void unRegisterRemoveProgressBarReceiver() {
		if (null != removeProgressBarReceiver) {
			this.unregisterReceiver(removeProgressBarReceiver);
			removeProgressBarReceiver = null;
		}
	}

	class RemoveProgressBarReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				String url = intent.getStringExtra("url");
				if (!url.equals("") && null != url) {
					if (null != downloaders.get(url)) {
						String localFilePath = downloaders.get(url)
								.getLocalfile();
						String fileName = StrUtils
								.getFileNameFromLocalPath(localFilePath);
						Log.i(TAG, fileName + " install success");
						ProgressBars.remove(url);
						MapSize.remove(url);
						downloaders.get(url).delete(url);
						downloaders.get(url).reset();
						downloaders.remove(url);
						isUpdateAllViews = true;
						updateAllViews();

						Log.e(TAG, "RemovedProgressBarReceiver");
					}
				}
			}
		}
	}

	/************************** end remove progressbar ******************/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.manage);

		reLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
		tv_nodata = (TextView) findViewById(R.id.manage_no_data_id);
		expandListView = (ExpandableListView) findViewById(R.id.list);
		dao = new Dao(this);
		asd = new AppSourceDao(this);
		restore = new RestoreModel(this);
		pkg = new PkgManager(this);
		notification = new LewaNotification(this);
		mContext = this;

		if (StorageCheck.checkAppDirsAndMkdirs() == Constants.STORAGE_STATUS_NONE) {
			mDialog = new MessageDialog(this);
			Log.e(TAG, "no sdcard***********************************");
			mDialog.ShowInfo(getString(R.string.no_sdcard_download_failed));

			try {
				notification
						.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "delete notifi error,msg=" + e.getMessage());
			}
		}

		basketHelper = new LewaBasketHelper(this);
		basketHelper.setBasketResponseListener(this);
		user = LewaUser.getInstance(this);

		gpk = new GooglePackages();
		gList = gpk.getGooglePackageNameList();
		gMap=gpk.getGoogleSpecPackages();

		adapter = new ManageBaseAdapter(ManageActivity.this, ProgressBars, dao,
				downloaders, notification, expandListView, MapSize, pkg);
		expandListView.setAdapter(adapter);

		initPages();
	}

	private int refreshCode = Constants.REFRESH_NO;

	public void onRefresh() {
		Log.i(TAG, "onRefresh()");

		setContentView(R.layout.manage);

		refreshCode = Constants.REFRESH_YES;

		reLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
		expandListView = (ExpandableListView) findViewById(R.id.list);
		dao = new Dao(this);
		asd = new AppSourceDao(this);
		restore = new RestoreModel(this);
		pkg = new PkgManager(this);
		notification = new LewaNotification(this);

		if (StorageCheck.checkAppDirsAndMkdirs() == Constants.STORAGE_STATUS_NONE) {
			mDialog = new MessageDialog(this);
			Log.e(TAG, "no sdcard***********************************");
			mDialog.ShowInfo(getString(R.string.no_sdcard_download_failed));
			try {
				notification
						.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e(TAG, "delete notifi error,msg=" + e.getMessage());
			}
		}

		basketHelper = new LewaBasketHelper(this);
		basketHelper.setBasketResponseListener(this);
		user = LewaUser.getInstance(this);
		
		gpk = new GooglePackages();
		gList = gpk.getGooglePackageNameList();
		gMap=gpk.getGoogleSpecPackages();

		adapter = new ManageBaseAdapter(ManageActivity.this, ProgressBars, dao,
				downloaders, notification, expandListView, MapSize, pkg,
				refreshCode);
		expandListView.setAdapter(adapter);

		initPages();
	}

	private Handler dataHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == Constants.HandlerWhatFlagOne) {
				restore();
				if (isHaveData()) {
					Log.i(TAG, "manage have data");
					setProperty();
					reLayout.setVisibility(View.GONE);
					if (null != expandListView) {
						expandListView.setVisibility(View.VISIBLE);
					}
				} else {
					Log.e(TAG, "manage no data");
					setNoDataPage();
				}
			}
		}
	};

	private NoDataReceiver noDataReceiver;

	private void registerNoDataReceiver() {
		if (null == noDataReceiver) {
			noDataReceiver = new NoDataReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_MANAGE_SET_NO_DATA);
			registerReceiver(noDataReceiver, intentFilter);
		}
	}

	public void unRegisterNoDataReceiver() {
		if (null != noDataReceiver) {
			this.unregisterReceiver(noDataReceiver);
			noDataReceiver = null;
		}
	}

	class NoDataReceiver extends BroadcastReceiver {

		String TAG = NoDataReceiver.class.getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				Log.i(this.TAG, "set no data");
				if (!isHaveData()) {
					setNoDataPage();
					Log.i(this.TAG, "have set no data");
				}
			}
		}

	}

	private void setNoDataPage() {
		try {
			reLayout.setVisibility(View.GONE);
			expandListView.setVisibility(View.GONE);
			tv_nodata.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setNormalPage() {
		try {
			reLayout.setVisibility(View.GONE);
			tv_nodata.setVisibility(View.GONE);
			expandListView.setVisibility(View.VISIBLE);
			setProperty();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean isHaveData() {
		boolean flag = true;
		if (null != restoreList && restoreList.size() == 0 && null != adapter
				&& adapter.updateItemList.size() == 1
				&& adapter.localInstalledItemList.size() == 1
				&& adapter.thirdpartyInstalledItemList.size() == 1) {
			flag = false;
		}
		Log.d(TAG,
				String.format(
						"restore size=%d,update size=%d,local size=%d,thirdpart size=%d",
						restoreList.size(), adapter.updateItemList.size(),
						adapter.localInstalledItemList.size(),
						adapter.thirdpartyInstalledItemList.size()));
		return flag;
	}

	private void setProperty() {
		if (null != expandListView) {
			expandListView.setGroupIndicator(null);
			expandListView.setDivider(null);
			this.setDefaultExpandGroup(expandListView);
			expandListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
			expandListView
					.setOnGroupClickListener(new ManageGroupClickListener());
		}
	}

	private void initPages() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				initdatas();

				Message message = Message.obtain();
				message.what = Constants.HandlerWhatFlagOne;
				dataHandler.sendMessage(message);
			}
		}).start();
	}

	private synchronized void removeAllData() {
		try {
			if (null != adapter) {
				int groupsize = adapter.getGroupCount();
				for (int i = groupsize - 1; i >= 0; i--) {
					adapter.removeGroup(i);
				}
				adapter.notifyDataSetChanged();
			}
			if (null != expandListView) {
				expandListView.invalidateViews();
			}
			Log.d(TAG, "removeAllData()");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void initdatas() {
		systemAppPackagesMap = pkg.getAppPackages();
		initSystemMap();
		allAppList = asd.getMetaAppInfo();
		if (NetHelper.checkWifi(this)) {
			this.addUpdateItems();
		}
		this.addInstalledItems();
	}

	/**
	 * 是否是本店应用
	 * 
	 * @param pkgName
	 * @return
	 */
	private boolean isLocalApp(String pkgName) {
		boolean flag = false;
		for (AppInfo ai : allAppList) {
			if (pkgName.equals(ai.getPackageName().trim())) {
				flag = true;
				break;
			}
		}
		return flag;
	}

	private void restore() {
		restoreList = restore.restorePackageItems();
		if (null != restoreList && restoreList.size() > 0) {
			Log.e(TAG, "restore()");
			removeAllData();
			this.restoreDownloadItems(restoreList);
			addUpdateItems();
			addInstalledItems();
		} else {
			Log.e(TAG, "restore data is null");
		}
	}

	private void updateManageViews() {
		boolean isHave = false;
		if (null != adapter) {
			isHave = adapter
					.isHaveDownloadingGroup(ManageBaseAdapter.DOWNLOADING_GROUP);
		}
		if (isHave) {
			restore();
		} else {
			removeAllData();
			addUpdateItems();
			addInstalledItems();
		}
		if (isHaveData()) {
			this.setNormalPage();
		} else {
			this.setNoDataPage();
		}
		if (null != adapter) {
			adapter.notifyDataSetChanged();
		}
		if (null != expandListView) {
			expandListView.invalidate();
		}
	}

	// 存储packagename,versioncode
	public Map<String, Integer> systemMap = null;

	private void initSystemMap() {
		systemMap = new HashMap<String, Integer>();
		Map<String, Object> map = null;
		int size = systemAppPackagesMap.size();
		String pkgName = "";
		for (int i = 0; i < size; i++) {
			map = systemAppPackagesMap.get(i);
			pkgName = map.get("packageName").toString();
			int versioncode = Integer.parseInt(map.get("appVersionCode")
					.toString());
			if (!systemMap.containsKey(pkgName)) {
				systemMap.put(pkgName, versioncode);
			}
		}
		if (null != map) {
			map.clear();
			map = null;
		}
	}

	private void addUpdateItems() {

		int update_cnt = 0;
		if (null != allAppList) {
			int size = allAppList.size();
			AppInfo ai;
			String cstorePkgName = "";
			for (int j = 0; j < size; j++) {
				ai = allAppList.get(j);
				int appId = ai.getAppId();
				cstorePkgName = ai.getPackageName();
				int cstoreVersionCode = Integer
						.parseInt(ai.getAppVersionCode());
				if (systemMap.get(cstorePkgName) != null
						&& (cstoreVersionCode > systemMap.get(cstorePkgName))) {
					adapter.addUpdateChildItem(appId + "", ai.getAppLogoUrl(),
							ai.getUrl(), ai.getAppName(), ai.getAppAuthor(),
							ai.getAppVersion(), ai.getDescription(),
							cstorePkgName);
					update_cnt++;
				}
			}
			ai = null;
			cstorePkgName = "";
		}
		Log.i(TAG, "added update items" + " update items size==" + update_cnt);
	}

	/**
	 * 获得系统已安装的最新应用集合
	 * 
	 * @return
	 */
	private List<HashMap<String, Object>> getNewestAppPackageList() {
		// 每次都取最新的
		if (null != pkg) {
			systemAppPackagesMap = pkg.getAppPackages();
		} else {
			pkg = new PkgManager(this);
			systemAppPackagesMap = pkg.getAppPackages();
		}
		return systemAppPackagesMap;
	}

	public static boolean isWhiteListItem(String pkgName){
		boolean flag=false;
		for(int i=0;i<Constants.whitelist.length;i++){
			if(pkgName.equals(Constants.whitelist[i])){
				flag=true;
				break;
			}
		}
		return flag;
	}
	
	private void addInstalledItems() {

		this.getNewestAppPackageList();

		Map<String, Object> map = null;
		String pkgName = "";
		int size = systemAppPackagesMap.size();
		for (int i = 0; i < size; i++) {
			map = systemAppPackagesMap.get(i);
			pkgName = map.get("packageName").toString().trim();
			if (pkgName.startsWith("com.lewa.")) {
				continue;
			}
			
			if (gList.contains(pkgName) && !isWhiteListItem(pkgName)) {
				continue;
			}
			
			if(gList.contains(pkgName) && !LaunchApp.isInstallGooglePackages(gMap.get(pkgName))){
				continue;
			}
			int versioncode = Integer.parseInt(map.get("appVersionCode").toString());
			if (null != systemMap && !systemMap.containsKey(pkgName)) {
				systemMap.put(pkgName, versioncode);
			}
			if (null != adapter) {
				if (this.isLocalApp(pkgName)) {
					adapter.addLocalInstalledChildItem(
							((i + 1) * 1000 + 1) + "",
							(Drawable) map.get("icon"),
							map.get("appName").toString(),
							getString(R.string.donot_know),
							getString(R.string.version_string)
									+ map.get("appVersion").toString(), map
									.get("appDescripition").toString(), pkgName);
				} else {
					adapter.addThirdpartInstalledChildItem(
							((i + 1) * 1000 + 1) + "",
							(Drawable) map.get("icon"),
							map.get("appName").toString(),
							getString(R.string.donot_know),
							getString(R.string.version_string)
									+ map.get("appVersion").toString(), map
									.get("appDescripition").toString(), pkgName);
				}

			}
		}
		Log.i(TAG,
				"added installed items,the number=="
						+ systemAppPackagesMap.size());
	}

	private void addDownloadingItems(AppInfo ai, String url) {
		if (null != ai) {
			adapter.addDownloadChildItem(ai.getAppId() + "",
					ai.getAppLogoUrl(), ai.getAppName(), ai.getAppAuthor(),
					ai.getAppVersion(), ai.getDescription(), url);
			Log.i(TAG, "added downloading items");
		}
	}

	/**
	 * @param elv
	 */
	private void restoreDownloadItems(List<AppInfo> list) {
		if (null != list && list.size() > 0) {
			int size = list.size();
			for (int k = 0; k < size; k++) {
				AppInfo ai = list.get(k);
				adapter.addDownloadChildItem(ai.getAppId() + "",
						ai.getAppLogoUrl(), ai.getAppName(), ai.getAppAuthor(),
						ai.getAppVersion(), ai.getDescription(), ai.getUrl());
			}
		}
		Log.e(TAG, "restore items size==" + list.size());
	}

	private void setDefaultExpandGroup(ExpandableListView elv) {
		int groupCount = 0;
		if (null != adapter) {
			groupCount = adapter.getGroupCount();
		}
		for (int i = 0; i < groupCount; i++) {
			if (null != expandListView) {
				expandListView.expandGroup(i);
			}
		}
	}

	private void registerReceiver() {
		if (null == receiver) {
			receiver = new DownloadServiceReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_DOWNLOAD_SERVICE);
			registerReceiver(receiver, intentFilter);
		}
	}

	public void unRegisterReceiver() {
		if (null != receiver) {
			this.unregisterReceiver(receiver);
			receiver = null;
		}
	}

	/********************* start ***************************/

	private void registerUpdateInstalledReceiver() {
		if (null == updateInstalledReceiver) {
			updateInstalledReceiver = new UpdateInstalledReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
			registerReceiver(updateInstalledReceiver, intentFilter);
		}
	}

	public void unRegisterUpdateInstalledReceiver() {
		if (null != updateInstalledReceiver) {
			this.unregisterReceiver(updateInstalledReceiver);
			updateInstalledReceiver = null;
		}
	}

	// update installed view
	class UpdateInstalledReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				if (null != intent) {
					int code = intent.getIntExtra(
							Constants.UPDATE_INSTALLED_ITEMS, 0);
					if (code == Constants.UPDATE_INSTALLED_ITEMS_ID) {
						synchronized (this) {
							systemAppPackagesMap = pkg.getAppPackages();
							initSystemMap();
							updateManageViews();
							setProperty();
							if (null != adapter) {
								adapter.notifyDataSetChanged();
							}
							if (null != expandListView) {
								expandListView.invalidateViews();
							}
						}
					} else if (code == Constants.UPDATE_MANAGE_ADAPTER_VIEWS_ID) {
						if (null != adapter) {
							adapter.notifyDataSetChanged();
						}
						if (null != expandListView) {
							expandListView.invalidateViews();
						}
						Log.i(TAG, "refresh manage view");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/********************** end **************************/
	class DownloadServiceReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				String urlstr = intent.getStringExtra("url");
				int packageIdInt = intent.getIntExtra("packageIdInt", 0);

				AppInfo ai = getBeginDownloadItem(packageIdInt);
				if (null != ai) {
					setNormalPage();
					addDownloadingItems(ai, urlstr);
					Log.i(TAG, "add download item ");
					((BaseExpandableListAdapter) (adapter))
							.notifyDataSetChanged();
					setDefaultExpandGroup(expandListView);
					expandListView.invalidate();
				} else {
					Log.e(TAG, "ManageActivity ai==null,error url =  " + urlstr);
				}
			}
		}
	}

	private AppInfo getBeginDownloadItem(int packageIdInt) {
		AppInfo ai = null;
		int searchTimes = 0;
		do {
			searchTimes++;
			this.allAppList = asd.getMetaAppInfo();
			if (null != allAppList) {
				int size = allAppList.size();
				for (int i = 0; i < size; i++) {
					ai = allAppList.get(i);
					if (ai.getAppId() == packageIdInt) {
						Log.e(TAG, "Downloading APP ID========" + ai.getAppId());
						break;
					}
				}
			}
		} while (null == ai);
		Log.e(TAG, "searchTimes===" + searchTimes);
		return ai;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		this.registerReceiver();
		this.registerUpdateInstalledReceiver();
		this.registerRemoveProgressBarReceiver();
		this.registerNoDataReceiver();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "ManageActivity onDestroy()");
		if (dao != null) {
			dao.closeDb();
		}
		if (adapter != null) {
			adapter.unRegisterInstalledSucessReceiver();
			adapter.unRegisterPackageRemovedReceiver();
			adapter.unRegisterNotifyDataSetChangedReceiver();
		}
		this.unRegisterUpdateInstalledReceiver();
		this.unRegisterReceiver();
		this.unRegisterRemoveProgressBarReceiver();
		this.unRegisterNoDataReceiver();
		super.onDestroy();
	}

	class ManageGroupClickListener implements
			android.widget.ExpandableListView.OnGroupClickListener {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			expandListView.expandGroup(groupPosition);
			return true;
		}
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
}