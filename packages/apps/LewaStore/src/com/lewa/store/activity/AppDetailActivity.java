package com.lewa.store.activity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.core.util.LewaRemoteBitmapUtil;

import com.lewa.store.R;
import com.lewa.store.adapter.PreferencesHelper;
import com.lewa.store.download.Dao;
import com.lewa.store.model.AppInfo;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.NetHelper;
import com.lewa.store.utils.StorageCheck;
import com.lewa.store.utils.StrUtils;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Here is detail info for apps.
 * 
 * @author ypzhu
 * 
 */
public class AppDetailActivity extends BaseActivity {

	private String TAG = AppDetailActivity.class.getSimpleName();

	private ImageView appLogo = null;
	private TextView appName = null;
	private TextView appVersion = null;
	private TextView appDescription = null;
	private TextView appAuthor = null;
	private Button appDownloadBtn = null;
	private Button appIsinstallBtn = null;
	private Button appDownloadingBtn = null;
	private Button appUpdateBtn = null;

	private String packageName = null;
	LewaRemoteBitmapUtil bitmapUtil = null;
	AppInfo ai = null;

	private Dao dao = null;

	// network
	private PreferencesHelper sp = null;
	private int networkStatusCode = 0;// 0 or 1

	private PkgManager pkg = null;
	List<HashMap<String, Object>> systemAppPackagesMap = null;
	// 存储packagename,versioncode
	private Map<String, Integer> systemMap = new HashMap<String, Integer>();
	Map<String, Object> map = null;

	public void initSystemMap() {

		for (int i = 0; i < systemAppPackagesMap.size(); i++) {
			map = systemAppPackagesMap.get(i);
			String pkgName = map.get("packageName").toString();
			int versioncode = Integer.parseInt(map.get("appVersionCode")
					.toString());
			systemMap.put(pkgName, versioncode);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_detail);
		pkg = new PkgManager(this);
		systemAppPackagesMap = pkg.getAppPackages();
		initSystemMap();
		this.dao = new Dao(this);
		bitmapUtil = new LewaRemoteBitmapUtil();
		initPages();
		appDownloadBtn.setOnClickListener(this);
		appUpdateBtn.setOnClickListener(this);

		sp = new PreferencesHelper(this, Constants.updateFlag);
		networkStatusCode = sp.getIntValue(Constants.SETTING_NETWORK_FLAG);
	}

	@Override
	public void initPages() {
		// TODO Auto-generated method stub
		appLogo = (ImageView) findViewById(R.id.detail_app_logo);
		appName = (TextView) findViewById(R.id.detail_app_name);
		appVersion = (TextView) findViewById(R.id.detail_app_version);
		appDescription = (TextView) findViewById(R.id.detail_app_description);
		appAuthor = (TextView) findViewById(R.id.detail_app_author);
		appDownloadBtn = (Button) findViewById(R.id.detail_download_btn);
		appIsinstallBtn = (Button) findViewById(R.id.detail_app_isinstalled);
		appDownloadingBtn = (Button) findViewById(R.id.detail_btn_downloading);
		appUpdateBtn = (Button) findViewById(R.id.detail_btn_update);

		Intent intent = this.getIntent();
		ai = (AppInfo) intent.getSerializableExtra("appInfo");
		if (null != ai) {
			bitmapUtil.fetchDrawableOnThread(ai.getAppLogoUrl(), appLogo);
			appName.setText(ai.getAppName());
			appVersion.setText(ai.getAppVersion());
			appDescription.setText(ai.getDescription());
			appAuthor.setText(StrUtils.getFileMbSize((long) ai.getAppSize()));
			// appAuthor.setText(ai.getAppAuthor());

			String url = ai.getUrl();
			packageName = ai.getPackageName();
			int versionCode = Integer.parseInt(ai.getAppVersionCode());

			if (LaunchApp.isInstallApp(packageName)
					&& (versionCode <= systemMap.get(packageName))) {// 已安装
				appDownloadBtn.setVisibility(View.GONE);
				appDownloadingBtn.setVisibility(View.GONE);
				appUpdateBtn.setVisibility(View.GONE);
				appIsinstallBtn.setVisibility(View.VISIBLE);
			} else if ((null != url)
					&& (dao.getDownloadStatus(url) == Constants.BUTTON_STATUS_DOWNLOADING)) {// 下载中。
				appDownloadBtn.setVisibility(View.GONE);
				appIsinstallBtn.setVisibility(View.GONE);
				appUpdateBtn.setVisibility(View.GONE);
				appDownloadingBtn.setVisibility(View.VISIBLE);
			} else if (LaunchApp.isInstallApp(packageName)
					&& systemMap.get(packageName) != null
					&& (versionCode > systemMap.get(packageName))) {// 更新
				appDownloadBtn.setVisibility(View.GONE);
				appDownloadingBtn.setVisibility(View.GONE);
				appIsinstallBtn.setVisibility(View.GONE);
				appUpdateBtn.setVisibility(View.VISIBLE);
			} else {// 可下载
				appDownloadingBtn.setVisibility(View.GONE);
				appIsinstallBtn.setVisibility(View.GONE);
				appUpdateBtn.setVisibility(View.GONE);
				appDownloadBtn.setVisibility(View.VISIBLE);
			}
		}

	}

	@Override
	public void onClickListener(View view) {
		// TODO Auto-generated method stub
		int vid = view.getId();
		if (vid == R.id.detail_download_btn) {
			int status = StorageCheck.checkAppDirsAndMkdirs();
			Log.i(TAG, "appDetail storage value==" + status);
			switch (status) {
			case Constants.STORAGE_STATUS_NONE:
				toast(getString(R.string.please_insert_sdcard));
				break;
			case Constants.STORAGE_STATUS_LOW:
				toast(getString(R.string.have_no_storage));
				break;
			case Constants.STORAGE_STATUS_OK:
				networkStatusCode = sp
						.getIntValue(Constants.SETTING_NETWORK_FLAG);
				switch (networkStatusCode) {
				case Constants.SETTING_NETWORK_WIFI_OPEN:
					if (NetHelper.checkWifi(this)) {
						appDownloadBtn.setVisibility(View.GONE);
						appUpdateBtn.setVisibility(View.GONE);
						appIsinstallBtn.setVisibility(View.GONE);
						appDownloadingBtn.setVisibility(View.VISIBLE);

						if (null != dao) {
							dao.updataAppStatus(
									Constants.BUTTON_STATUS_CANCEL_DOWNLOADING,
									ai.getUrl());

							//send to AppListActivity
							Intent intent = new Intent(
									Constants.BROADCAST_APPLIST_UPDATE_VIEW);
							intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
									Constants.UPDATE_APPLIST_ITEMS_ID);
							sendBroadcast(intent);
						}
						Intent intent = new Intent();
						intent.setAction(Constants.DOWNLOAD_ACTION);
						intent.putExtra("appName", ai.getAppName());
						intent.putExtra("url", ai.getUrl());
						intent.putExtra("packageIdInt", ai.getAppId());
						intent.putExtra("localfile", Constants.DOWNLOAD_SDPATH
								+ ai.getAppName());
						startService(intent);
					} else {
						toast(getString(R.string.please_set_wifi));
					}
					break;
				case Constants.SETTING_NETWORK_ANYNETWORK_OPEN:
					if (NetHelper.isAccessNetwork(this)) {
						appDownloadBtn.setVisibility(View.GONE);
						appIsinstallBtn.setVisibility(View.GONE);
						appUpdateBtn.setVisibility(View.GONE);
						appDownloadingBtn.setVisibility(View.VISIBLE);

						if (null != dao) {
							dao.updataAppStatus(
									Constants.BUTTON_STATUS_CANCEL_DOWNLOADING,
									ai.getUrl());
							//send to AppListActivity
							Intent intent = new Intent(
									Constants.BROADCAST_APPLIST_UPDATE_VIEW);
							intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
									Constants.UPDATE_APPLIST_ITEMS_ID);
							sendBroadcast(intent);
						}

						Intent intent = new Intent();
						intent.setAction(Constants.DOWNLOAD_ACTION);
						intent.putExtra("appName", ai.getAppName());
						intent.putExtra("url", ai.getUrl());
						intent.putExtra("packageIdInt", ai.getAppId());
						intent.putExtra("localfile", Constants.DOWNLOAD_SDPATH
								+ ai.getAppName());
						startService(intent);
					} else {
						toast(getString(R.string.please_set_network));
					}
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}
		} else if (vid == R.id.detail_btn_update) {// update
			int status = StorageCheck.checkAppDirsAndMkdirs();
			Log.i(TAG, "appDetail storage value==" + status);
			switch (status) {
			case Constants.STORAGE_STATUS_NONE:
				toast(getString(R.string.please_insert_sdcard));
				break;
			case Constants.STORAGE_STATUS_LOW:
				toast(getString(R.string.have_no_storage));
				break;
			case Constants.STORAGE_STATUS_OK:
				networkStatusCode = sp
						.getIntValue(Constants.SETTING_NETWORK_FLAG);
				switch (networkStatusCode) {
				case Constants.SETTING_NETWORK_WIFI_OPEN:
					if (NetHelper.checkWifi(this)) {
						appDownloadBtn.setVisibility(View.GONE);
						appIsinstallBtn.setVisibility(View.GONE);
						appUpdateBtn.setVisibility(View.GONE);
						appDownloadingBtn.setVisibility(View.VISIBLE);

						if (null != dao) {
							dao.updataAppStatus(
									Constants.BUTTON_STATUS_CANCEL_DOWNLOADING,
									ai.getUrl());
							//send to AppListActivity
							Intent intent = new Intent(
									Constants.BROADCAST_APPLIST_UPDATE_VIEW);
							intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
									Constants.UPDATE_APPLIST_ITEMS_ID);
							sendBroadcast(intent);
						}

						Intent intent = new Intent();
						intent.setAction(Constants.DOWNLOAD_ACTION);
						intent.putExtra("appName", ai.getAppName());
						intent.putExtra("url", ai.getUrl());
						intent.putExtra("packageIdInt", ai.getAppId());
						intent.putExtra("localfile", Constants.DOWNLOAD_SDPATH
								+ ai.getAppName());
						startService(intent);
					} else {
						toast(getString(R.string.please_set_wifi));
					}
					break;
				case Constants.SETTING_NETWORK_ANYNETWORK_OPEN:
					if (NetHelper.isAccessNetwork(this)) {
						appDownloadBtn.setVisibility(View.GONE);
						appIsinstallBtn.setVisibility(View.GONE);
						appUpdateBtn.setVisibility(View.GONE);
						appDownloadingBtn.setVisibility(View.VISIBLE);

						if (null != dao) {
							dao.updataAppStatus(
									Constants.BUTTON_STATUS_CANCEL_DOWNLOADING,
									ai.getUrl());
							//send to AppListActivity
							Intent intent = new Intent(
									Constants.BROADCAST_APPLIST_UPDATE_VIEW);
							intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
									Constants.UPDATE_APPLIST_ITEMS_ID);
							sendBroadcast(intent);
						}

						Intent intent = new Intent();
						intent.setAction(Constants.DOWNLOAD_ACTION);
						intent.putExtra("appName", ai.getAppName());
						intent.putExtra("url", ai.getUrl());
						intent.putExtra("packageIdInt", ai.getAppId());
						intent.putExtra("localfile", Constants.DOWNLOAD_SDPATH
								+ ai.getAppName());
						startService(intent);
					} else {
						toast(getString(R.string.please_set_network));
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
	}

	@Override
	public void handlerMessage(int msg) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		if (null != dao) {
			dao.closeDb();
		}
		super.onDestroy();
	}

}
