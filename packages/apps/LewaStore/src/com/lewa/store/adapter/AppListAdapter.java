package com.lewa.store.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.store.activity.AppListActivity;
import com.lewa.store.activity.ManageActivity;

import com.lewa.store.R;
import com.lewa.store.download.Dao;
import com.lewa.store.extras.GooglePackages;
import com.lewa.store.model.AppInfo;
import com.lewa.store.model.AsyncImageLoader;
import com.lewa.store.model.AsyncImageLoader.ImageCallback;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.StrUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class AppListAdapter extends BaseAdapter {

	private String TAG = AppListAdapter.class.getSimpleName();

	private Context context;
	private LayoutInflater mInflater;
	private ListView listview;
	private List<AppInfo> list = null;
	private Dao dao = null;
	private LaunchApp la = null;
	private AsyncImageLoader asyncImageLoader;
	private PkgManager pkg = null;
	private List<HashMap<String, Object>> systemAppPackagesMap = null;
	private Map<String, Integer> systemMap = null;
	private List<String> mSortList;
	private int mStartIndex = 0;
	private int mEndIndex = 0;
	// private boolean isOnScroll = false;// 是否拖动
	private Drawable defaultImage = null;
	private List<String> listTag = null;

	//google apps
	private GooglePackages gpk = null;
	private Map<String, String> gMap = null;
	
	public AppListAdapter(Context context, List<AppInfo> l, Dao dao,
			LaunchApp la, Map<String, Integer> systemMap, ListView listView,
			List<HashMap<String, Object>> systemAppPackagesMap, PkgManager pkg,
			List<String> sortList, List<String> listTag) {
		this.context = (AppListActivity) context;
		this.mInflater = LayoutInflater.from(context);
		this.listview = listView;
		this.list = l;
		this.dao = dao;
		this.la = la;
		this.systemMap = systemMap;
		this.mSortList = sortList;
		asyncImageLoader = new AsyncImageLoader();
//		imageDownloader = new ImageDownloader();
		this.pkg = pkg;
		this.systemAppPackagesMap = systemAppPackagesMap;
		this.registerUpdateAppListReceiver();
		this.listTag = listTag;

		// Log.e(TAG, "mSortList==" + mSortList.toString());

		this.defaultImage = context.getPackageManager()
				.getDefaultActivityIcon();
		this.gpk = new GooglePackages();
		this.gMap = gpk.getGoogleSpecPackages();
	}

	public AppListAdapter(Context context, List<AppInfo> l, Dao dao,
			LaunchApp la, Map<String, Integer> systemMap, ListView listView,
			List<HashMap<String, Object>> systemAppPackagesMap, PkgManager pkg,
			List<String> sortList, int refreshCode, List<String> listTag) {
		this.context = (AppListActivity) context;
		this.mInflater = LayoutInflater.from(context);
		this.listview = listView;
		this.list = l;
		this.dao = dao;
		this.la = la;
		this.systemMap = systemMap;
		this.mSortList = sortList;
		asyncImageLoader = new AsyncImageLoader();
//		imageDownloader = new ImageDownloader();
		this.pkg = pkg;
		this.systemAppPackagesMap = systemAppPackagesMap;
		this.listTag = listTag;

		this.defaultImage = context.getPackageManager()
				.getDefaultActivityIcon();
		this.gpk = new GooglePackages();
		this.gMap = gpk.getGoogleSpecPackages();
	}

	@Override
	public int getCount() {
		return mSortList.size();
	}

	@Override
	public Object getItem(int position) {
		return mSortList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ViewHolder holder = null;

	private void loadingImageDrawable(final String appIconUrl, ImageView iv) {
		Drawable cachedImage = asyncImageLoader.loadDrawable(context,appIconUrl,new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable,String imageUrl) {
						ImageView imageViewByTag = (ImageView) listview.findViewWithTag(imageUrl);
						if (imageViewByTag != null && imageDrawable != null) {
							imageViewByTag.setVisibility(View.VISIBLE);
							imageViewByTag.setImageDrawable(imageDrawable);
						}
					}
				});
		if (cachedImage == null) {
			iv.setImageDrawable(defaultImage);
		} else {
			iv.setVisibility(View.VISIBLE);
			iv.setImageDrawable(cachedImage);
		}
	}
	
	public void autoLoadImage(String appIconUrl, ImageView iv){
		asyncImageLoader.fetchDrawableOnThread(context, appIconUrl, iv);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.app_list_item, null);
			holder = new ViewHolder();
			holder.icon = (ImageView) convertView.findViewById(R.id.app_logo);
			holder.name = (TextView) convertView.findViewById(R.id.app_name);
			holder.version = (TextView) convertView
					.findViewById(R.id.app_version);
			holder.button = (Button) convertView.findViewById(R.id.btn_start);
			holder.isinstalled = (Button) convertView
					.findViewById(R.id.app_list_isinstall);
			holder.app_list_isDownloading = (Button) convertView
					.findViewById(R.id.app_list_isDownloading);
			holder.updateBtn = (Button) convertView
					.findViewById(R.id.app_list_isupdate);
			holder.divider = (View) convertView.findViewById(R.id.divider);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (((AppListActivity) context).listTag.contains(getItem(position))) {// 显示分类名称
			/*
			 * holder.name.setText(getItem(position).toString());
			 * holder.name.setPadding(10, 0, 0, 0); holder.name.setTextSize(18);
			 * holder.name.setTextColor(0xff38A1DB);
			 * holder.icon.setVisibility(View.GONE);
			 * holder.version.setVisibility(View.GONE);
			 * holder.button.setVisibility(View.GONE);
			 * holder.isinstalled.setVisibility(View.GONE);
			 * holder.updateBtn.setVisibility(View.GONE);
			 * holder.app_list_isDownloading.setVisibility(View.GONE);
			 * holder.divider.setVisibility(View.VISIBLE);
			 */
			ImageView iv = (ImageView) convertView.findViewById(R.id.app_logo);
			TextView tvAppName = (TextView) convertView
					.findViewById(R.id.app_name);
			TextView tvAppVersion = (TextView) convertView
					.findViewById(R.id.app_version);
			Button btnStart = (Button) convertView.findViewById(R.id.btn_start);
			Button btnInstalled = (Button) convertView
					.findViewById(R.id.app_list_isinstall);
			Button btnDownloading = (Button) convertView
					.findViewById(R.id.app_list_isDownloading);
			Button btnUpdate = (Button) convertView
					.findViewById(R.id.app_list_isupdate);
			View view = (View) convertView.findViewById(R.id.divider);

			tvAppName.setText(getItem(position).toString());
			tvAppName.setPadding(10, 0, 0, 0);
			tvAppName.setTextSize(18);
			tvAppName.setTextColor(0xff38A1DB);
			iv.setVisibility(View.GONE);
			tvAppVersion.setVisibility(View.GONE);
			btnStart.setVisibility(View.GONE);
			btnInstalled.setVisibility(View.GONE);
			btnDownloading.setVisibility(View.GONE);
			btnUpdate.setVisibility(View.GONE);
			view.setVisibility(View.VISIBLE);
		} else {// 显示app
			holder.name.setPadding(1, 0, 0, 0);
			holder.icon.setVisibility(View.VISIBLE);
			holder.version.setVisibility(View.VISIBLE);
			holder.button.setVisibility(View.VISIBLE);
			holder.isinstalled.setVisibility(View.VISIBLE);
			holder.updateBtn.setVisibility(View.VISIBLE);
			if (((position + 1) >= mSortList.size())
					|| ((AppListActivity) context).listTag
							.contains(getItem(position + 1))) {
				holder.divider.setVisibility(View.GONE);
			} else {
				holder.divider.setVisibility(View.VISIBLE);
			}
			holder.name.setTextColor(0xff000000);
			AppInfo info = getAppInfoByAppId(Integer.parseInt(mSortList.get(position)));
			if (info == null) {
				mSortList.remove(position);
				notifyDataSetChanged();
				if(null!=listview){
					listview.invalidate();
				}
				return convertView;
			}
			String appIconUrl = Constants.DOWNLOAD_ICON_URL + info.getAppId()
					+ Constants.ICON_FILE_SUFFIX;
			holder.icon.setTag(appIconUrl);
			loadingImageDrawable(appIconUrl, holder.icon);
//			autoLoadImage(appIconUrl, holder.icon);
			holder.name.setText(info.getAppName());
			holder.version.setText(StrUtils.replaceBr(info.getDescription()));
			String packageName = info.getPackageName().trim();
			int versionCode = Integer.parseInt(info.getAppVersionCode());
			String appUrl = info.getUrl();
			// 谷歌应用
			String gFileName = "";
			if (gMap.containsKey(packageName)) {
				gFileName = gMap.get(packageName);
			}
			if (!gFileName.equals("")) {// is google packages
				if (LaunchApp.isInstallGooglePackages(gFileName)) {
					holder.button.setVisibility(View.GONE);
					holder.updateBtn.setVisibility(View.GONE);
					holder.app_list_isDownloading.setVisibility(View.GONE);
					holder.isinstalled.setVisibility(View.VISIBLE);
				} else if (isDownloading(Integer.parseInt(mSortList
						.get(position))) == true
						&& !LaunchApp.isInstallGooglePackages(gFileName)) {
					holder.button.setVisibility(View.GONE);
					holder.updateBtn.setVisibility(View.GONE);
					holder.isinstalled.setVisibility(View.GONE);
					holder.app_list_isDownloading.setVisibility(View.VISIBLE);
				} else {
					if (LaunchApp.isInstallGooglePackages(gFileName)) {
						holder.button.setVisibility(View.GONE);
						holder.updateBtn.setVisibility(View.GONE);
						holder.app_list_isDownloading.setVisibility(View.GONE);
						holder.isinstalled.setVisibility(View.VISIBLE);
					} else {
						holder.updateBtn.setVisibility(View.GONE);
						holder.app_list_isDownloading.setVisibility(View.GONE);
						holder.isinstalled.setVisibility(View.GONE);
						holder.button.setVisibility(View.VISIBLE);
					}
				}
			} else {// not google app
				if (LaunchApp.isInstallApp(packageName)
						&& systemMap.get(packageName) != null
						&& (versionCode <= systemMap.get(packageName))) {// installed
					holder.button.setVisibility(View.GONE);
					holder.updateBtn.setVisibility(View.GONE);
					holder.app_list_isDownloading.setVisibility(View.GONE);
					holder.isinstalled.setVisibility(View.VISIBLE);
				} else if (LaunchApp.isInstallApp(packageName)
						&& systemMap.get(packageName) != null
						&& (versionCode > systemMap.get(packageName))) {// 有更新
					holder.button.setVisibility(View.GONE);
					holder.isinstalled.setVisibility(View.GONE);
					holder.app_list_isDownloading.setVisibility(View.GONE);
					holder.updateBtn.setVisibility(View.VISIBLE);
				} else if ((null != appUrl)
						&& (isDownloading(Integer.parseInt(mSortList
								.get(position))) == true)
						&& !LaunchApp.isInstallApp(packageName)) {// 下载中
					holder.button.setVisibility(View.GONE);
					holder.updateBtn.setVisibility(View.GONE);
					holder.isinstalled.setVisibility(View.GONE);
					holder.app_list_isDownloading.setVisibility(View.VISIBLE);
				} else if (!LaunchApp.isInstallApp(packageName)) {// 下载
					holder.isinstalled.setVisibility(View.GONE);
					holder.updateBtn.setVisibility(View.GONE);
					holder.app_list_isDownloading.setVisibility(View.GONE);
					holder.button.setVisibility(View.VISIBLE);
				}
			}
		}
		return convertView;
	}

	private boolean isDownloading(int appid) {
		boolean flag = false;
		if (null != ManageActivity.downloadings) {
			for (int i = 0; i < ManageActivity.downloadings.size(); i++) {
				if (appid == ManageActivity.downloadings.get(i).intValue()) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	public AppInfo getAppInfoByAppId(int appid) {
		try {
			for (AppInfo appinfo : list) {
				if (appinfo.getAppId() == appid) {
					return appinfo;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
		return null;
	}

	@Override
	public int getItemViewType(int position) {
		// TODO Auto-generated method stub
		return super.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return super.getViewTypeCount();
	}

	private static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView version;
		Button button;// 下载
		Button isinstalled;
		Button app_list_isDownloading;
		Button updateBtn;
		View divider;
	}

	/********************* start ***************************/

	Map<String, Object> tempMap = null;

	public void initSystemMap() {
		systemMap.clear();
		int size = systemAppPackagesMap.size();
		for (int i = 0; i < size; i++) {
			tempMap = systemAppPackagesMap.get(i);
			String pkgName = tempMap.get("packageName").toString();
			int versioncode = Integer.parseInt(tempMap.get("appVersionCode")
					.toString());
			systemMap.put(pkgName, versioncode);
		}
		tempMap = null;
		// Log.e(TAG,"current installed app number================"+systemMap.size());
	}

	/********************* Receiver ****************************/

	private UpdateAppListReceiver updateAppListReceiver = null;

	public void registerUpdateAppListReceiver() {
		if (null == updateAppListReceiver) {
			updateAppListReceiver = new UpdateAppListReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_APPLIST_UPDATE_VIEW);
			context.registerReceiver(updateAppListReceiver, intentFilter);
		}
	}

	public void unRegisterUpdateAppListReceiver() {
		if (null != updateAppListReceiver) {
			context.unregisterReceiver(updateAppListReceiver);
			updateAppListReceiver = null;
		}
	}

	// update installed view
	class UpdateAppListReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				if (null != intent) {
					int code = intent.getIntExtra(
							Constants.UPDATE_APPLIST_ITEMS, 0);
					Log.d(TAG, "update app list adapter,code=" + code);
					if (code == Constants.UPDATE_APPLIST_ITEMS_ID) {
						systemAppPackagesMap = pkg.getAppPackages();
						initSystemMap();
						notifyDataSetChanged();
						listview.invalidateViews();
						Log.e(TAG, systemMap.toString());
						Log.i(TAG, "receiver update applist broadcast");
					} else if (code == Constants.UPDATE_APPLIST_ALL_ITEMS_ID) {
						systemAppPackagesMap = pkg.getAppPackages();
						initSystemMap();
						notifyDataSetChanged();
						if (null != listview) {
							listview.invalidateViews();
						}
						Log.i(TAG, "invalidate AppListAdapter");
					}
				}
			}
		}
	}

	/********************** end **************************/
}