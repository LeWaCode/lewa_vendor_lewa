package com.lewa.store.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lewa.core.base.LewaUser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lewa.store.R;
import com.lewa.store.activity.ManageActivity;
import com.lewa.store.download.Dao;
import com.lewa.store.download.Downloader;
import com.lewa.store.extras.GooglePackages;
import com.lewa.store.model.AsyncImageLoader;
import com.lewa.store.model.AsyncImageLoader.ImageCallback;
import com.lewa.store.model.LewaNotification;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.FileHelper;
import com.lewa.store.utils.SystemHelper;

public class ManageBaseAdapter extends BaseExpandableListAdapter {

	private String TAG = ManageBaseAdapter.class.getSimpleName();

	public static final int DOWNLOADING_GROUP = 0;
	public static final int UPDATE_GROUP = 1;
	public static final int LOCAL_INSTALLED_GROUP = 2;
	public static final int THIRDPARTY_INSTALLED_GROUP = 3;
	public static final int GROUP_INFO_INDEX = 0;

	private List<List<Map<String, Object>>> child = new ArrayList<List<Map<String, Object>>>();

	public LewaUser user = null;
	private ManageActivity context = null;
	private LayoutInflater mInflater = null;
	private PkgManager pkg = null;
	private Dao dao = null;
	private LewaNotification notification = null;

	private Map<String, Integer> ProgressBars = null;
	private Map<String, Integer> MapSize = null;
	private Map<String, Downloader> downloaders = null;
	private AsyncImageLoader asyncImageLoader = null;
	private ExpandableListView expandListView = null;

	private GooglePackages gpk = null;
	private Map<String, String> gMap = null;
	private Map<String,String> specMap=null;

	private Drawable defaultImage = null;

	private List<Map<String, Object>> downloadingItemList = Collections
			.synchronizedList(new ArrayList<Map<String, Object>>());
	public List<Map<String, Object>> updateItemList = Collections
			.synchronizedList(new ArrayList<Map<String, Object>>());
	public List<Map<String, Object>> localInstalledItemList = Collections
			.synchronizedList(new ArrayList<Map<String, Object>>());
	public List<Map<String, Object>> thirdpartyInstalledItemList = Collections
			.synchronizedList(new ArrayList<Map<String, Object>>());
	
//	private List<List<Map<String, Object>>> allList=new ArrayList<List<Map<String,Object>>>();

	public ManageBaseAdapter(Context a, Map<String, Integer> ProgressBars,
			Dao dao, Map<String, Downloader> downloaders,
			LewaNotification notification, ExpandableListView expandListView,
			Map<String, Integer> mapSize, PkgManager p) {

		this.context = (ManageActivity) a;

		this.setCategoryGroups();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.pkg = p;
		this.dao = dao;
		user = LewaUser.getInstance(context);
		asyncImageLoader = new AsyncImageLoader();
		this.expandListView = expandListView;
		this.downloaders = downloaders;
		this.notification = notification;
		this.ProgressBars = ProgressBars;
		this.MapSize = mapSize;
		this.registerInstalledSucessReceiver();
		this.registerNotifyDataSetChangedReceiver();
		this.registerPackageRemovedReceiver();
		this.gpk = new GooglePackages();
		this.gMap = gpk.getGoogleAppNames();
		this.specMap=gpk.getGoogleSpecPackages();

		this.defaultImage = context.getPackageManager()
				.getDefaultActivityIcon();
	}

	public ManageBaseAdapter(Context a, Map<String, Integer> ProgressBars,
			Dao dao, Map<String, Downloader> downloaders,
			LewaNotification notification, ExpandableListView expandListView,
			Map<String, Integer> mapSize, PkgManager p, int refreshCode) {

		this.context = (ManageActivity) a;

		this.setCategoryGroups();

		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.pkg = p;
		this.dao = dao;
		user = LewaUser.getInstance(context);
		asyncImageLoader = new AsyncImageLoader();
		this.expandListView = expandListView;
		this.downloaders = downloaders;
		this.notification = notification;
		this.ProgressBars = ProgressBars;
		this.MapSize = mapSize;
		this.gpk = new GooglePackages();
		this.gMap = gpk.getGoogleAppNames();
		this.specMap=gpk.getGoogleSpecPackages();

		this.defaultImage = context.getPackageManager()
				.getDefaultActivityIcon();
	}

	private void setCategoryGroups() {

		addGroupInfo(
				((ManageActivity) context)
						.getString(R.string.label_downloading),
				downloadingItemList);
		child.add(DOWNLOADING_GROUP, downloadingItemList);

		addGroupInfo(
				((ManageActivity) context).getString(R.string.label_update),
				updateItemList);
		child.add(UPDATE_GROUP, updateItemList);

		addGroupInfo(
				((ManageActivity) context)
						.getString(R.string.label_local_installed),
				localInstalledItemList);
		child.add(LOCAL_INSTALLED_GROUP, localInstalledItemList);

		addGroupInfo(
				((ManageActivity) context)
						.getString(R.string.label_thirdparty_installed),
				thirdpartyInstalledItemList);
		child.add(THIRDPARTY_INSTALLED_GROUP, thirdpartyInstalledItemList);

	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		if (childPosition >= getChildrenCount(groupPosition)) {
			return null;
		} else {
			return child.get((int) getGroupId(groupPosition)).get(childPosition + 1);
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		// System.out.println("groupPosition=="+groupPosition+" childrenCount=="+child.get(groupPosition).size());
		return child.get((int) getGroupId(groupPosition)).size() - 1;
	}
	
	public void hiddenDivider(ManageViewHolder holder,int groupPosition,int childPosition){
		try {
			if (childPosition+1>=getChildrenCount(groupPosition)) {
				holder.divider.setVisibility(View.GONE);
			}else{
				holder.divider.setVisibility(View.VISIBLE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	int i = 0;
	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		ManageViewHolder holder = null;
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) getChild(groupPosition,
				childPosition);
		String groupName = getGroupName(groupPosition);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.manage_list_item, null);
			holder = new ManageViewHolder();
			holder.iv = (ImageView) convertView
					.findViewById(R.id.download_icon);
			holder.appName = (TextView) convertView
					.findViewById(R.id.download_filename);
			holder.appVersion = (TextView) convertView
					.findViewById(R.id.app_version);
			holder.updateBtn = (Button) convertView
					.findViewById(R.id.app_update);
			holder.uninstallBtn = (Button) convertView
					.findViewById(R.id.uninstall);
			holder.cancelBtn = (Button) convertView
					.findViewById(R.id.cancel_download);
			holder.progressBar = (ProgressBar) convertView
					.findViewById(R.id.download_progressBar);
			holder.redownload = (Button) convertView
					.findViewById(R.id.redownload);
			holder.divider=convertView.findViewById(R.id.divider);
			convertView.setTag(holder);
		} else {
			holder = (ManageViewHolder) convertView.getTag();
		}
		
		hiddenDivider(holder, groupPosition, childPosition);
		
		if (null != map && map.size() > 0) {
			String pkName = "";
			if (map.get("packageName") != null) {
				pkName = map.get("packageName").toString();
			}
			if (gMap.containsKey(pkName)) {
				holder.appName.setText(gMap.get(pkName));
			} else {
				holder.appName.setText(map.get("appName").toString());
			}
			holder.appVersion.setText(map.get("appVersion").toString());
			if (groupName.equals(this.context.getString(R.string.label_update))) {
				String iconUrl = map.get("iconUrl").toString();
				holder.iv.setTag(iconUrl);
				loadingImageDrawable(iconUrl, holder.iv);

				holder.updateBtn.setVisibility(View.VISIBLE);
				holder.cancelBtn.setVisibility(View.GONE);
				holder.uninstallBtn.setVisibility(View.GONE);
				holder.updateBtn.setOnClickListener(new ButtonEventListener(
						groupPosition, childPosition));
				holder.progressBar.setVisibility(View.GONE);
				
			} else if (groupName.equals(this.context
					.getString(R.string.label_local_installed))) {
				Object obj = map.get("iconUrl");
				if (obj instanceof Drawable) {
					holder.iv.setImageDrawable((Drawable) obj);
				} else {
				}
				holder.updateBtn.setVisibility(View.GONE);
				holder.cancelBtn.setVisibility(View.GONE);
				holder.appVersion.setVisibility(View.VISIBLE);
				holder.uninstallBtn.setVisibility(View.VISIBLE);
				holder.uninstallBtn.setOnClickListener(new ButtonEventListener(
						groupPosition, childPosition));
				holder.progressBar.setVisibility(View.GONE);
				
			} else if (groupName.equals(this.context
					.getString(R.string.label_thirdparty_installed))) {
				Object obj = map.get("iconUrl");
				if (obj instanceof Drawable) {
					holder.iv.setImageDrawable((Drawable) obj);
				} else {
				}
				holder.updateBtn.setVisibility(View.GONE);
				holder.cancelBtn.setVisibility(View.GONE);
				holder.appVersion.setVisibility(View.VISIBLE);
				holder.uninstallBtn.setVisibility(View.VISIBLE);
				holder.uninstallBtn.setOnClickListener(new ButtonEventListener(
						groupPosition, childPosition));
				holder.progressBar.setVisibility(View.GONE);
				
			} else if (groupName.equals(context
					.getString(R.string.label_downloading))) {
				
				holder.appVersion.setVisibility(View.GONE);
				holder.progressBar.setVisibility(View.VISIBLE);
				holder.progressBar.setProgress((int) 0);

				int appid = (map.get("appId") != null) ? Integer.parseInt(map
						.get("appId").toString()) : -1;
				final String appUrl = (map.get("appUrl") != null) ? map.get(
						"appUrl").toString() : "";
				String iconUrl = (null != map.get("iconUrl")) ? map.get(
						"iconUrl").toString() : "";

				if (null != dao && !dao.isHasInfors(appUrl)) {
					// 处理下载失败
					if (map != null && appid != -1) {
						if (ManageActivity.failedMaps.containsKey(appid)) {// 包含下载失败项
							holder.updateBtn.setVisibility(View.GONE);
							holder.uninstallBtn.setVisibility(View.GONE);
							holder.cancelBtn.setVisibility(View.GONE);
							holder.redownload.setVisibility(View.VISIBLE);
							holder.redownload
									.setOnClickListener(new ButtonEventListener(
											groupPosition, childPosition));

							holder.iv.setTag(iconUrl);
							loadingImageDrawable(iconUrl, holder.iv);

							Integer maxSize = MapSize.get(appUrl);
							Integer progress = ProgressBars.get(appUrl);
							if (null != progress && null != maxSize) {
								holder.progressBar.setMax((int) maxSize);
								holder.progressBar.setProgress((int) progress);
							}
						} else {
							holder.updateBtn.setVisibility(View.GONE);
							holder.uninstallBtn.setVisibility(View.GONE);
							holder.cancelBtn.setVisibility(View.VISIBLE);
							holder.cancelBtn
									.setOnClickListener(new ButtonEventListener(
											groupPosition, childPosition));

							holder.iv.setTag(iconUrl);
							loadingImageDrawable(iconUrl, holder.iv);

							Integer maxSize = MapSize.get(appUrl);
							Integer progress = ProgressBars.get(appUrl);
							if (null != progress && null != maxSize) {
								holder.progressBar.setMax((int) maxSize);
								holder.progressBar.setProgress((int) progress);
							}
						}
					}
				} else {
					clearUrl(appUrl);
					removeChild(groupPosition, childPosition);
					if (getChildrenCount(groupPosition) == 0) {
						removeGroup(groupPosition);
					}
					invalidateManageViews();
				}
			}
		}
		return convertView;
	}

	private void loadingImageDrawable(String appIconUrl, ImageView iv) {
		Drawable cachedImage = asyncImageLoader.loadDrawable(context,
				appIconUrl, new ImageCallback() {
					public void imageLoaded(Drawable imageDrawable,
							String imageUrl) {
						ImageView imageViewByTag = (ImageView) expandListView
								.findViewWithTag(imageUrl);
						if (imageViewByTag != null && imageDrawable != null) {
							imageViewByTag.setVisibility(View.VISIBLE);
							imageViewByTag.setImageDrawable(imageDrawable);
						}
					}
				});
		if (cachedImage == null) {
			// iv.setVisibility(View.INVISIBLE);
			iv.setImageDrawable(defaultImage);
		} else {
			iv.setVisibility(View.VISIBLE);
			iv.setImageDrawable(cachedImage);
		}
	}

	private void invalidateManageViews() {
		// update manage views
		Intent i = new Intent(Constants.BROADCAST_MANAGE_UPDATE_VIEW);
		i.putExtra(Constants.UPDATE_INSTALLED_ITEMS,
				Constants.UPDATE_MANAGE_ADAPTER_VIEWS_ID);
		context.sendBroadcast(i);
	}

	public void clearUrl(String appUrl) {
		synchronized (this) {
			ProgressBars.remove(appUrl);
			MapSize.remove(appUrl);
		}
	}

	private void addGroupInfo(String groupName,
			List<Map<String, Object>> group_list) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("groupName", groupName);
		group_list.add(GROUP_INFO_INDEX, map);
		map = null;
	}

	/********************* 处理更新应用 ********************************/

	// 记录用户更新哪个item
	private List<Map<String, Object>> tempUpdateItems = null;

	private void setRecordUpdateItem(Map<String, Object> map) {
		if (null == tempUpdateItems) {
			tempUpdateItems = new ArrayList<Map<String, Object>>();
		}
		if (!tempUpdateItems.contains(map)) {
			tempUpdateItems.add(map);
		}
		Log.d(TAG, "setRecordUpdateItem()");
	}

	private boolean isHaveRecordUpdateItem(Map<String, Object> map) {
		Log.d(TAG, "isHaveRecordUpdateItem()");
		Log.d(TAG, "map=" + map.toString());
		boolean flag = false;
		if (null != tempUpdateItems) {
			for (Map<String, Object> m : tempUpdateItems) {
				if (null != m.get("appId")
						&& null != map.get("appId")
						&& Integer.parseInt(m.get("appId").toString()) == Integer
								.parseInt(map.get("appId").toString())) {
					Log.e(TAG, "isHaveRecordUpdateItem ok!");
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	private void removeRecordUpdateItem(Map<String, Object> map) {
		if (null != tempUpdateItems) {
			if (tempUpdateItems.contains(map)) {
				Log.d(TAG, "removeRecordUpdateItem(),map=" + map.toString());
				tempUpdateItems.remove(map);
			}
		}
	}

	/********************* 处理更新应用end ********************************/
	class ButtonEventListener implements OnClickListener {
		private int groupPosition;
		private int childPosition;

		public ButtonEventListener(int gPosition, int cPosition) {
			this.groupPosition = gPosition;
			this.childPosition = cPosition;
		}

		@Override
		public void onClick(View v) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) getChild(
					groupPosition, childPosition);
			// Log.e(TAG,"onClick,map=="+map.toString());
			if (map == null) {
				return;
			}
			int vid = v.getId();
			switch (vid) {
			case R.id.redownload:
				if (getGroupId(groupPosition) == DOWNLOADING_GROUP) {
					int appid = (map.get("appId") != null) ? Integer
							.parseInt(map.get("appId").toString()) : -1;
					String url = map.get("appUrl").toString();
					if (appid != -1) {
						if (ManageActivity.failedMaps.containsKey(appid)) {
							ManageActivity.failedMaps.remove(appid);
						}
						if (null != ProgressBars.get(url)) {
							MapSize.remove(url);
							ProgressBars.remove(url);
						}
						if (null != downloaders.get(url)) {
							downloaders.get(url).pause();
							downloaders.get(url).delete(url);
							downloaders.get(url).reset();
							downloaders.remove(url);
						}
						removeDownloadingItem(appid);
						removeChild(groupPosition, childPosition);

						Intent intent = new Intent();
						intent.setAction(Constants.DOWNLOAD_ACTION);
						intent.putExtra("appName", map.get("appName")
								.toString().trim());
						intent.putExtra("url", map.get("appUrl").toString()
								.trim());
						intent.putExtra("packageIdInt",
								Integer.parseInt(map.get("appId").toString()));
						intent.putExtra("localfile",
								Constants.SD_PATH + map.get("appName")
										+ Constants.APK_FILE_SUFFIX);
						context.startService(intent);
					} else {
						Log.e(TAG, "redownoad click error");
					}
				}
				break;
			case R.id.uninstall:
				String packageName="";
				if (getGroupId(groupPosition) == LOCAL_INSTALLED_GROUP) {
					packageName = map.get("packageName").toString();
					if(specMap.containsKey(packageName)){
						 String fileName=specMap.get(packageName);
						 FileHelper.delFile(Constants.SYSTEM_DIR+File.separator+fileName);
	                     FileHelper.rootDelFolder(Constants.PACKAGE_DATA_DIR+packageName);
						 removeChild(groupPosition, childPosition);
					}else{
						pkg.uninstallApk(packageName);
					}
				} else if (getGroupId(groupPosition) == THIRDPARTY_INSTALLED_GROUP) {
					packageName = map.get("packageName").toString();	
					if(specMap.containsKey(packageName)){
						String fileName=specMap.get(packageName);
						FileHelper.delFile(Constants.SYSTEM_DIR+File.separator+fileName);
	                    FileHelper.rootDelFolder(Constants.PACKAGE_DATA_DIR+packageName);
						removeChild(groupPosition, childPosition);		
				    }else{
				    	pkg.uninstallApk(packageName);
				    }				
				}
				break;
			case R.id.cancel_download:
				if (getGroupId(groupPosition) == DOWNLOADING_GROUP) {
					int appId = Integer.parseInt(map.get("appId").toString());
					String appUrl = map.get("appUrl").toString();
					if (null != ProgressBars.get(appUrl)) {
						MapSize.remove(appUrl);
						ProgressBars.remove(appUrl);
					}
					if (null != downloaders.get(appUrl)) {
						downloaders.get(appUrl).pause();
						downloaders.get(appUrl).delete(appUrl);
						downloaders.get(appUrl).reset();
						downloaders.remove(appUrl);
					}
					if (null != dao && !dao.isHasInfors(appUrl)) {
						dao.delete(appUrl);
					}
					removeDownloadingItem(appId);

					// refresh applist adapter,listview
					Intent i = new Intent(
							Constants.BROADCAST_APPLIST_UPDATE_VIEW);
					i.putExtra(Constants.UPDATE_APPLIST_ITEMS,
							Constants.UPDATE_APPLIST_ALL_ITEMS_ID);
					context.sendBroadcast(i);

					invalidateManageViews();

					// Intent intent=new
					// Intent(Constants.BROADCAST_BASKET_UPDATE_VIEW);
					// context.sendBroadcast(intent);

					notification
							.deleteNotification(Constants.NOTIFICATION_DOWNLOADING_ID);
					restoreUpdateItem(map);
					new SystemHelper(context).stopStoreService();

					// set no data
					synchronized (this) {
						isSetNoData = true;
						sendNoDataBroadcast();
					}
				}
				break;
			case R.id.app_update:
				if (getGroupId(groupPosition) == UPDATE_GROUP) {
					Intent intent = new Intent();
					intent.setAction(Constants.DOWNLOAD_ACTION);
					intent.putExtra("appName", map.get("appName").toString()
							.trim());
					intent.putExtra("url", map.get("appUrl").toString().trim());
					intent.putExtra("packageIdInt",
							Integer.parseInt(map.get("appId").toString()));
					intent.putExtra("localfile",
							Constants.SD_PATH + map.get("appName")
									+ Constants.APK_FILE_SUFFIX);
					context.startService(intent);

					// remove through getChildView method
					removeChild(groupPosition, childPosition);
					if (getChildrenCount(groupPosition) == 0) {
						removeGroup(groupPosition);
					}
					invalidateManageViews();
					// refresh applist adapter,listview
					Intent i = new Intent(
							Constants.BROADCAST_APPLIST_UPDATE_VIEW);
					i.putExtra(Constants.UPDATE_APPLIST_ITEMS,
							Constants.UPDATE_APPLIST_ALL_ITEMS_ID);
					context.sendBroadcast(i);
					// 记录更新项
					setRecordUpdateItem(map);
				}
				break;
			default:
				break;
			}
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		// Log.e(TAG,"*****getGroup(),groupPosition=="+groupPosition);
		return child.get((int) getGroupId(groupPosition)).get(GROUP_INFO_INDEX);
	}

	@Override
	public long getGroupId(int groupPosition) {
		// Log.e(TAG,"***getGroupId***,groupPosition=="+groupPosition);
		int group_index = 0;
		for (int i = 0; i <= THIRDPARTY_INSTALLED_GROUP; i++) {
			if (child.get(i).size() > 1) {
				if (group_index == groupPosition) {
					return i;
				} else {
					group_index++;
				}
			}
		}
		return 0;
	}

	@Override
	public int getGroupCount() {
		int group_size = 0;
		group_size = ((child.get(DOWNLOADING_GROUP).size()) > 1 ? 1 : 0)
				+ ((child.get(UPDATE_GROUP).size()) > 1 ? 1 : 0)
				+ ((child.get(LOCAL_INSTALLED_GROUP).size()) > 1 ? 1 : 0)
				+ ((child.get(THIRDPARTY_INSTALLED_GROUP).size()) > 1 ? 1 : 0);
		return group_size;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View view = convertView;
		// if (view == null) {
		view = mInflater.inflate(R.layout.groupitem, null);
		// }
		TextView title = (TextView) view.findViewById(R.id.groupText);
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) getGroup(groupPosition);
		if (null != title && null != map && map.get("groupName") != null) {
			title.setText(map.get("groupName").toString());
		}
		return view;
	}

	private String getGroupName(int groupPosition) {
		String name="";
		try {
			name=child.get((int) getGroupId(groupPosition)).get(GROUP_INFO_INDEX).get("groupName").toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void removeGroup(int groupPosition) {
		Log.v(TAG, "Removing group==" + groupPosition);
		// for (int i = child.get((int)getGroupId(groupPosition)).size() - 1; i
		// > 1; i --) {
		for (int i = child.get((int) getGroupId(groupPosition)).size() - 1; i >= 1; i--) {
			child.get((int) getGroupId(groupPosition)).remove(i);
		}
		notifyDataSetChanged();
	}

	public void removeChild(int groupPosition, int childPosition) {
		// TODO: Remove the according child
		Log.v(TAG, "Removing child " + childPosition + " in group "
				+ groupPosition);
		child.get((int) getGroupId(groupPosition)).remove(childPosition + 1);// map移除
		notifyDataSetChanged();
	}

	public void restoreUpdateItem(Map<String, Object> map) {
		if (null != tempUpdateItems && isHaveRecordUpdateItem(map)) {
			Log.i(TAG, "restoreUpdateItem(),map==" + map.toString());
			updateItemList.add(map);
			Log.i(TAG, "updateItemList.size==" + updateItemList.size());
			removeRecordUpdateItem(map);
			notifyDataSetChanged();
			this.setDefaultExpandGroup();
		} else {
			Log.e(TAG, "error");
		}
	}

	public void addUpdateChildItem(String appId, String iconUrl, String appUrl,
			String appName, String appAuthor, String appVersion,
			String appDescription, String packageName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("appId", appId);
		map.put("appUrl", appUrl);
		map.put("iconUrl", iconUrl);
		map.put("appName", appName);
		map.put("appAuthor", appAuthor);
		map.put("appVersion", appVersion);
		map.put("appDescription", appDescription);
		map.put("packageName", packageName);
		if (!updateItemList.contains(map)) {
			updateItemList.add(map);
		}
		map = null;
	}

	// 已安装(本店应用)
	public void addLocalInstalledChildItem(String appId, Drawable iconUrl,
			String appName, String appAuthor, String appVersion,
			String appDescription, String packageName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("appId", appId);
		map.put("iconUrl", iconUrl);
		map.put("appName", appName);
		map.put("appAuthor", appAuthor);
		map.put("appVersion", appVersion);
		map.put("appDescription", appDescription);
		map.put("packageName", packageName);
		if (!localInstalledItemList.contains(map)) {
			localInstalledItemList.add(map);
		}
		map = null;
	}

	// 已安装(三方应用)
	public void addThirdpartInstalledChildItem(String appId, Drawable iconUrl,
			String appName, String appAuthor, String appVersion,
			String appDescription, String packageName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("appId", appId);
		map.put("iconUrl", iconUrl);
		map.put("appName", appName);
		map.put("appAuthor", appAuthor);
		map.put("appVersion", appVersion);
		map.put("appDescription", appDescription);
		map.put("packageName", packageName);
		if (!thirdpartyInstalledItemList.contains(map)) {
			thirdpartyInstalledItemList.add(map);
		}
		map = null;
	}

	// 动态添加，带有进度条的的item
	public void addDownloadChildItem(String appId, String iconUrl,
			String appName, String appAuthor, String appVersion,
			String appDescription, String appUrl) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("appId", appId);
		map.put("iconUrl", iconUrl);
		map.put("appName", appName);
		map.put("appAuthor", appAuthor);
		map.put("appVersion", appVersion);
		map.put("appDescription", appDescription);
		map.put("appUrl", appUrl);
		if (!downloadingItemList.contains(map)) {
			downloadingItemList.add(map);
		}
		map = null;
	}

	/**
	 * 是否有某个下载组
	 * 
	 * @param groupid
	 *            下载组id
	 * @return
	 */
	public boolean isHaveDownloadingGroup(int groupid) {
		int count = child.get(groupid).size() - 1;
		return (count > 0);
	}

	static class ManageViewHolder {
		ImageView iv;
		ProgressBar progressBar;
		TextView appName;
		TextView appVersion;
		Button updateBtn;// 更新
		Button uninstallBtn;// 卸载
		Button cancelBtn;// 取消
		Button redownload;// 重新下载
		View divider;
	}

	/*************************************************************/
	private PackageRemovedReceiver packageRemovedReceiver = null;

	private void registerPackageRemovedReceiver() {
		if (null == packageRemovedReceiver) {
			packageRemovedReceiver = new PackageRemovedReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			intentFilter.addDataScheme("package");
			context.registerReceiver(packageRemovedReceiver, intentFilter);
		}
	}

	public void unRegisterPackageRemovedReceiver() {
		if (null != packageRemovedReceiver) {
			context.unregisterReceiver(packageRemovedReceiver);
			packageRemovedReceiver = null;
		}
	}

	boolean isRefreshAppList = false;

	private void invalidateAppListView() {
		if (isRefreshAppList) {
			// refresh adapter,listview
			Intent intent = new Intent(Constants.BROADCAST_APPLIST_UPDATE_VIEW);
			intent.putExtra(Constants.UPDATE_APPLIST_ITEMS,
					Constants.UPDATE_APPLIST_ALL_ITEMS_ID);
			context.sendBroadcast(intent);
			isRefreshAppList = false;
			Log.d(TAG, "invalidateAppListView()");
		}
	}

	// 移除basket数据
	boolean isRemoveBasketData = false;

	private void removeBasketData(String packageName) {
		if (isRemoveBasketData) {
			if (user.getRegistrationState() == LewaUser.USER_IS_REGISTERED) {
				Intent ii = new Intent(Constants.BROADCAST_BASKET_REMOVE_DATA);
				ii.putExtra("packageName", packageName);
				context.sendBroadcast(ii);
				isRemoveBasketData = false;
				// Log.i(TAG,"removeBasketData()");
			}
		}
	}

	// 是否设置no data
	private boolean isSetNoData = false;

	private void sendNoDataBroadcast() {
		synchronized (this) {
			if (isSetNoData) {
				Intent intent = new Intent(
						Constants.BROADCAST_MANAGE_SET_NO_DATA);
				context.sendBroadcast(intent);
				isSetNoData = false;
			}
		}
	}

	class PackageRemovedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
					Log.d(TAG, intent.getDataString() + " app by removed");
					String unInstallName = intent.getDataString()
							.replace("package:", "").trim();
					if (unInstallName != null) {
						removeUpdatePackages(unInstallName);
						removeLocalUninstallPackage(unInstallName);
						removeThirdpartyUninstallPackage(unInstallName);
						notifyDataSetChanged();
						if (null != expandListView) {
							expandListView.invalidateViews();
						}
						synchronized (this) {
							isRefreshAppList = true;
							invalidateAppListView();
							isSetNoData = true;
							sendNoDataBroadcast();
						}
						// isRemoveBasketData=true;//暂不使用
						// removeBasketData(unInstallName);//暂不使用
					} else {
						Log.e(TAG, "will be uninstalled packagename is null");
					}
				}
			}
		}

	}

	private void removeLocalUninstallPackage(String name) {
		if (localInstalledItemList != null && localInstalledItemList.size() > 0) {
			Map<String, Object> m = null;
			int size = localInstalledItemList.size();
			String pkgName = "";
			for (int i = 1; i < size; i++) {
				m = localInstalledItemList.get(i);
				if (null != m && (null != m.get("packageName"))) {
					pkgName = m.get("packageName").toString().trim();
					if (pkgName.equals(name)) {
						Log.d(TAG,
								"remove package from localInstalledItemList,packageName=="
										+ pkgName);
						localInstalledItemList.remove(i);
						break;
					}
				}
			}
		}
	}

	/**
	 * 从三方应用移除
	 * 
	 * @param name
	 */
	private void removeThirdpartyUninstallPackage(String name) {
		if (thirdpartyInstalledItemList != null
				&& thirdpartyInstalledItemList.size() > 0) {
			Map<String, Object> m = null;
			int size = thirdpartyInstalledItemList.size();
			String pkgName = "";
			for (int i = 1; i < size; i++) {
				m = thirdpartyInstalledItemList.get(i);
				if (null != m && (null != m.get("packageName"))) {
					pkgName = m.get("packageName").toString().trim();
					if (pkgName.equals(name)) {
						Log.d(TAG,
								"remove package from thirdpartyInstalledItemList,packageName=="
										+ pkgName);
						thirdpartyInstalledItemList.remove(i);
						break;
					}
				}
			}
		}
	}

	private void removeUpdatePackages(String name) {
		if (updateItemList != null && updateItemList.size() > 0) {
			Map<String, Object> m = null;
			int size = updateItemList.size();
			String pkgName = "";
			for (int i = 1; i < size; i++) {
				m = updateItemList.get(i);
				if (null != m && (null != m.get("packageName"))) {
					pkgName = m.get("packageName").toString().trim();
					if (pkgName.equals(name)) {
						Log.d(TAG,
								"remove package from updateItemList,pkgname="
										+ pkgName);
						updateItemList.remove(i);
						break;
					}
				}
			}
		}
	}

	/************************************************************/

	private InstalledSucessReceiver innstalledSucessReceiver = null;

	private void registerInstalledSucessReceiver() {
		if (innstalledSucessReceiver == null) {
			innstalledSucessReceiver = new InstalledSucessReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_INSTALL_SUCESS);
			context.registerReceiver(innstalledSucessReceiver, intentFilter);
		}
	}

	public void unRegisterInstalledSucessReceiver() {
		if (null != innstalledSucessReceiver) {
			context.unregisterReceiver(innstalledSucessReceiver);
			innstalledSucessReceiver = null;
		}
	}

	class InstalledSucessReceiver extends BroadcastReceiver {

		private String TAG = InstalledSucessReceiver.class.getSimpleName();

		@Override
		public void onReceive(Context context, Intent intent) {
			synchronized (this) {
				String url = intent.getStringExtra("urlstr");
				ManageActivity.ProgressBars.remove(url);
				if (ManageActivity.downloaders.get(url) != null) {
					ManageActivity.downloaders.get(url).delete(url);
					ManageActivity.downloaders.get(url).reset();
					ManageActivity.downloaders.remove(url);
					Log.i(this.TAG, "onReceive");
				}
				removeDownloadSucessItem(url);
			}
		}
	}

	/********************************************************/
	public NotifyDataSetChangedReceiver manageDataChangedReceiver = null;

	public void registerNotifyDataSetChangedReceiver() {
		if (null == manageDataChangedReceiver) {
			manageDataChangedReceiver = new NotifyDataSetChangedReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.BROADCAST_MANAGE_ADAPTER_CHANGED);
			context.registerReceiver(manageDataChangedReceiver, intentFilter);
		}
	}

	public void unRegisterNotifyDataSetChangedReceiver() {
		if (null != manageDataChangedReceiver) {
			context.unregisterReceiver(manageDataChangedReceiver);
			manageDataChangedReceiver = null;
		}
	}

	class NotifyDataSetChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			notifyDataSetChanged();
			Log.d(TAG, "NotifyDataSetChangedReceiver update progress");
		}
	}

	/*******************************************************/

	// remove downloaded success
	private void removeDownloadSucessItem(String url) {
		if (null != this.downloadingItemList) {
			Map<String, Object> m = null;
			Log.e(TAG,
					"downloadingItemList size=="
							+ this.downloadingItemList.size());
			Log.e(TAG, "downloadingItemList context=="
					+ this.downloadingItemList);
			int size = this.downloadingItemList.size();
			for (int i = 1; i < size; i++) {
				m = this.downloadingItemList.get(i);
				if (null != m) {
					String appUrl = m.get("appUrl").toString();
					if (appUrl.equals(url)) {
						Log.e(TAG, "remove downloadingItemList index==" + i);
						this.downloadingItemList.remove(i);
						break;
					}
				}
			}
			this.notifyDataSetChanged();

		}
	}

	private void removeDownloadingItem(int appId) {
		for (int i = 0; i < ManageActivity.downloadings.size(); i++) {
			if (appId == ManageActivity.downloadings.get(i).intValue()) {
				ManageActivity.downloadings.remove(i);
				break;
			}
		}
		this.printDownloadingId(ManageActivity.downloadings);
	}

	private void printDownloadingId(List<Integer> list) {
		if (null != list) {
			if (list.size() == 0) {
				Log.d(TAG, "no downloading app id");
				return;
			}
			int size = list.size();
			for (int i = 0; i < size; i++) {
				Log.d(TAG, "downloading app id===" + list.get(i).intValue());
			}
		}
	}

	private void setDefaultExpandGroup() {
		int count = this.getGroupCount();
		for (int i = 0; i < count; i++) {
			if (null != expandListView) {
				expandListView.expandGroup(i);
			}
		}
	}
}
