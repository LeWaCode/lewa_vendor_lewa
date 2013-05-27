package com.lewa.store.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.store.R;
import com.lewa.store.activity.BasketActivity;
import com.lewa.store.activity.ManageActivity;
import com.lewa.store.dialog.MessageDialog;
import com.lewa.store.download.Dao;
import com.lewa.store.extras.GooglePackages;
import com.lewa.store.model.AppListModel;
import com.lewa.store.model.AsyncImageLoader;
import com.lewa.store.model.AsyncImageLoader.ImageCallback;
import com.lewa.store.pkg.LaunchApp;
import com.lewa.store.pkg.PkgManager;
import com.lewa.store.utils.Constants;
import com.lewa.store.utils.NetHelper;
import com.lewa.store.utils.StorageCheck;

public class BasketBaseAdapter extends BaseExpandableListAdapter {

	private String TAG = BasketBaseAdapter.class.getSimpleName();

	public static final int ONCLOUD_GROUP = 0;
	public static final int ONMOBILE_GROUP = 1;
	public static final int GROUP_INFO_INDEX = 0;

	public List<List<Map<String, Object>>> child = new ArrayList<List<Map<String, Object>>>();
	// 我的云
	public List<Map<String, Object>> cloudItemList = new ArrayList<Map<String, Object>>();
	// 我的手机
	public List<Map<String, Object>> mobileItemList = new ArrayList<Map<String, Object>>();

	private Activity activity = null;
	private Context context;
	private LayoutInflater mInflater;
	private PkgManager pkg = null;
	private Dao dao = null;
	private PreferencesHelper sp = null;
	private int networkStatusCode = 0;// 0 or 1
	private MessageDialog mDialog = null;
	private AsyncImageLoader imageLoader = null;
	private ExpandableListView expandableListView = null;
	private Drawable defaultImage;

	private AppListModel alm = null;

	private GooglePackages gpk = null;
	private Map<String, String> gMap = null;

	public BasketBaseAdapter(Context a, Dao dao, PreferencesHelper sp,
			Activity activity, PkgManager pkgManager, MessageDialog dialog,
			ExpandableListView view) {
		this.context = a;
		this.mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.pkg = pkgManager;
		this.dao = dao;
		this.sp = sp;
		this.activity = activity;
		this.mDialog = dialog;
		this.imageLoader = new AsyncImageLoader();
		this.expandableListView = view;
		this.defaultImage = context.getResources().getDrawable(
				R.drawable.logo_bg);
		this.setCategoryGroups();
		alm = new AppListModel(context);

		// google
		this.gpk = new GooglePackages();
		this.gMap = gpk.getGoogleAppNames();
	}

	// add group info
	private void addGroupInfo(String groupName,
			List<Map<String, Object>> group_list) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("groupName", groupName);
		group_list.add(GROUP_INFO_INDEX, map);
		map = null;
	}

	public void setCategoryGroups() {
		// on my cloud
		addGroupInfo(
				((BasketActivity) context).getString(R.string.label_oncloud),
				cloudItemList);
		child.add(ONCLOUD_GROUP, cloudItemList);

		// on my phone
		addGroupInfo(
				((BasketActivity) context).getString(R.string.label_onmyphone),
				mobileItemList);
		child.add(ONMOBILE_GROUP, mobileItemList);
	}

	// child method stub
	@Override
	public Object getChild(int groupPosition, int childPosition) {
		if (childPosition >= getChildrenCount(groupPosition)) {
			return null;
		} else {
			return child.get((int) getGroupId(groupPosition)).get(
					childPosition + 1);
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return child.get((int) getGroupId(groupPosition)).size() - 1;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		// System.out.println("groupPosition==" +
		// groupPosition+" childPosition==" + childPosition);
		BasketViewHolder holder = null;
		Map<String, Object> map = (Map<String, Object>) getChild(groupPosition,
				childPosition);
		String groupName = getGroupName(groupPosition);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.basket_list_item, null);
			holder = new BasketViewHolder();
			holder.iv = (ImageView) convertView
					.findViewById(R.id.download_icon);
			holder.appName = (TextView) convertView
					.findViewById(R.id.download_filename);
			holder.appVersion = (TextView) convertView
					.findViewById(R.id.app_version);
			holder.isInstalledBtn = (Button) convertView
					.findViewById(R.id.basket_isinstall);
			holder.downloadingBtn = (Button) convertView
					.findViewById(R.id.basket_downloading);
			holder.downloadBtn = (Button) convertView
					.findViewById(R.id.basket_download);
			convertView.setTag(holder);
		} else {
			holder = (BasketViewHolder) convertView.getTag();
		}
		if (null == map) {
			this.removeChild(groupPosition, childPosition);
			if (null != expandableListView) {
				expandableListView.invalidateViews();
			}
			return convertView;
		}
		if (null != map && map.size() > 0) {
			String packageName = map.get("packageName").toString().trim();
			if (gMap.containsKey(packageName)) {
				holder.appName.setText(gMap.get(packageName));
			} else {
				holder.appName.setText(map.get("appName").toString());
			}
			holder.appVersion.setText(map.get("appVersion").toString());
			if (groupName
					.equals(this.context.getString(R.string.label_oncloud))) {// 我的云
				String iconUrl = map.get("iconUrl").toString();
				int appId = Integer.valueOf((String) map.get("appId"))
						.intValue();
				holder.iv.setTag(iconUrl);
				Drawable cacheImage = imageLoader.loadDrawable(context,
						iconUrl, new ImageCallback() {
							@Override
							public void imageLoaded(Drawable imageDrawable,
									String imageUrl) {
								ImageView imageViewByTag = (ImageView) expandableListView
										.findViewWithTag(imageUrl);
								if (null != imageDrawable
										&& null != imageViewByTag) {
									imageViewByTag.setVisibility(View.VISIBLE);
									imageViewByTag
											.setImageDrawable(imageDrawable);
								}
							}
						});
				if (null != cacheImage) {
					holder.iv.setVisibility(View.VISIBLE);
					holder.iv.setImageDrawable(cacheImage);
				} else {
					holder.iv.setVisibility(View.INVISIBLE);
				}
				if (LaunchApp.isInstallApp(packageName)) {
					removeChild(groupPosition, childPosition);
					if (getChildrenCount(groupPosition) == 0) {
						removeGroup(groupPosition);
					}
				} else if ((isDownloading(appId) == true)
						&& !LaunchApp.isInstallApp(packageName)) {// 下载中
					holder.isInstalledBtn.setVisibility(View.GONE);
					holder.downloadBtn.setVisibility(View.GONE);
					holder.downloadingBtn.setVisibility(View.VISIBLE);
				} else {
					holder.downloadingBtn.setVisibility(View.GONE);
					holder.isInstalledBtn.setVisibility(View.GONE);
					holder.downloadBtn.setVisibility(View.VISIBLE);
					holder.downloadBtn
							.setOnClickListener(new ButtonEventListener(
									groupPosition, childPosition));
				}
			} else if (groupName.equals(this.context
					.getString(R.string.label_onmyphone))) {// 我的手机
				try {
					Object ob = map.get("iconUrl");
					if (null == ob) {
						ob = map.get("icon");
					}
					if (ob instanceof Drawable) {
						holder.iv.setVisibility(View.VISIBLE);
						holder.iv.setImageDrawable((Drawable) ob);
					}
				} catch (ClassCastException e) {
					Log.e(TAG, "is not Drawable object");
					e.printStackTrace();
					// holder.iv.setVisibility(View.INVISIBLE);
					holder.iv.setImageDrawable(defaultImage);
				}
				holder.downloadingBtn.setVisibility(View.GONE);
				holder.downloadBtn.setVisibility(View.GONE);
				holder.isInstalledBtn.setVisibility(View.VISIBLE);
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

	class ButtonEventListener implements OnClickListener {
		private int groupPosition;
		private int childPosition;

		public ButtonEventListener(int gPosition, int cPosition) {
			this.groupPosition = gPosition;
			this.childPosition = cPosition;
		}

		@Override
		public void onClick(View v) {
			Map<String, Object> map = (Map<String, Object>) getChild(
					groupPosition, childPosition);
			if (null == map) {
				return;
			}
			int vid = v.getId();
			switch (vid) {
			case R.id.basket_download:
				if (getGroupId(groupPosition) == ONCLOUD_GROUP) {
					if (alm.getDownloadingNumbers() > Constants.MAX_DOWNLOAD_NUMBERS) {
						Toast.makeText(
								context,
								context.getString(R.string.max_downloader_notice),
								Toast.LENGTH_LONG).show();
						return;
					}
					int status = StorageCheck.checkAppDirsAndMkdirs();
					Log.i(TAG, "status value==" + status);
					switch (status) {
					case Constants.STORAGE_STATUS_NONE:
						mDialog.ShowInfo(context
								.getString(R.string.please_insert_sdcard));
						break;
					case Constants.STORAGE_STATUS_LOW:
						mDialog.ShowInfo(context
								.getString(R.string.have_no_storage));
						break;
					case Constants.STORAGE_STATUS_OK:
						networkStatusCode = sp
								.getIntValue(Constants.SETTING_NETWORK_FLAG);
						String appUrl = map.get("appUrl").toString();
						switch (networkStatusCode) {
						case Constants.SETTING_NETWORK_WIFI_OPEN:
							if (NetHelper.checkWifi(activity)) {
								RelativeLayout layout = (RelativeLayout) v
										.getParent();
								Button isInstalledBtn = (Button) layout
										.findViewById(R.id.basket_isinstall);
								Button downloadingBtn = (Button) layout
										.findViewById(R.id.basket_downloading);
								Button downloadBtn = (Button) layout
										.findViewById(R.id.basket_download);
								isInstalledBtn.setVisibility(View.GONE);
								downloadBtn.setVisibility(View.GONE);
								downloadingBtn.setVisibility(View.VISIBLE);

								Intent intent = new Intent();
								intent.setAction(Constants.DOWNLOAD_ACTION);
								intent.putExtra("appName", map.get("appName")
										.toString());
								intent.putExtra("url", appUrl);// download url
								intent.putExtra("packageIdInt", Integer
										.parseInt(map.get("appId").toString()));
								intent.putExtra(
										"localfile",
										Constants.DOWNLOAD_SDPATH
												+ map.get("appName"));
								context.startService(intent);
							} else {
								mDialog.ShowInfo(context
										.getString(R.string.please_set_wifi));
							}
							break;
						case Constants.SETTING_NETWORK_ANYNETWORK_OPEN:
							if (NetHelper.isAccessNetwork(context)) {
								RelativeLayout layout = (RelativeLayout) v
										.getParent();
								Button isInstalledBtn = (Button) layout
										.findViewById(R.id.basket_isinstall);
								Button downloadingBtn = (Button) layout
										.findViewById(R.id.basket_downloading);
								Button downloadBtn = (Button) layout
										.findViewById(R.id.basket_download);
								isInstalledBtn.setVisibility(View.GONE);
								downloadBtn.setVisibility(View.GONE);
								downloadingBtn.setVisibility(View.VISIBLE);

								Intent intent = new Intent();
								intent.setAction(Constants.DOWNLOAD_ACTION);
								intent.putExtra("appName", map.get("appName")
										.toString());
								intent.putExtra("url", appUrl);// download url
								intent.putExtra("packageIdInt", Integer
										.parseInt(map.get("appId").toString()));
								intent.putExtra(
										"localfile",
										Constants.DOWNLOAD_SDPATH
												+ map.get("appName"));
								context.startService(intent);
							} else {
								mDialog.ShowInfo(context
										.getString(R.string.please_set_network));
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
				break;
			default:
				break;
			}
		}
	}

	private String getGroupName(int groupPosition) {
		return child.get((int) getGroupId(groupPosition)).get(GROUP_INFO_INDEX)
				.get("groupName").toString();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// System.out.println("*****getGroup(),groupPosition=="+groupPosition);
		return child.get((int) getGroupId(groupPosition)).get(GROUP_INFO_INDEX);
	}

	@Override
	public long getGroupId(int groupPosition) {
		// System.out.println("****getGroupId***,groupPosition=="+groupPosition);
		int group_index = 0;
		for (int i = 0; i <= ONMOBILE_GROUP; i++) {
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
		group_size = ((child.get(ONCLOUD_GROUP).size()) > 1 ? 1 : 0)
				+ ((child.get(ONMOBILE_GROUP).size()) > 1 ? 1 : 0);
		// System.out.println("******getGroupCount*********"+group_size);
		// System.out.println("******mycloud_GROUP*********"+child.get(ONCLOUD_GROUP));
		// System.out.println("******mymobile_GROUP*********"+child.get(ONMOBILE_GROUP));
		return group_size;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View view = convertView;
		// if(null==view){
		view = mInflater.inflate(R.layout.groupitem, null);
		// }
		TextView title = (TextView) view.findViewById(R.id.groupText);
		Map<String, Object> map = (Map<String, Object>) getGroup(groupPosition);
		if (null != map && null != map.get("groupName") && null != title) {
			title.setText(map.get("groupName").toString());
		}
		return view;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public void removeGroup(int groupPosition) {
		Log.v(TAG, "Removing group==" + groupPosition);
		Log.e(TAG,
				"removeGroup groupsize============="
						+ child.get((int) getGroupId(groupPosition)).size());
		for (int i = child.get((int) getGroupId(groupPosition)).size() - 1; i >= 1; i--) {
			child.get((int) getGroupId(groupPosition)).remove(i);
		}
		notifyDataSetChanged();
	}

	public void removeChild(int groupPosition, int childPosition) {
		Log.v(TAG, "Removing child " + childPosition + " in group "
				+ groupPosition);
		child.get((int) getGroupId(groupPosition)).remove(childPosition + 1);// map移除
		notifyDataSetChanged();
	}

	static class BasketViewHolder {
		ImageView iv;
		TextView appName;
		TextView appVersion;
		Button downloadBtn;// 下载
		Button isInstalledBtn;// 已安装
		Button downloadingBtn;// 下载中
	}

	public void addOnCloudChildItem(String appId, String appUrl,
			String iconUrl, String appName, String appAuthor,
			String appVersion, String appDescription, String packageName) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("appId", appId);
		map.put("appUrl", appUrl);
		map.put("iconUrl", iconUrl);
		map.put("appName", appName);
		map.put("appAuthor", appAuthor);
		map.put("appVersion", appVersion);
		map.put("appDescription", appDescription);
		map.put("packageName", packageName);
		if (!cloudItemList.contains(map)) {
			cloudItemList.add(map);
		}
		map = null;
	}

	public void addMyPhoneChildItem(String appId, Drawable iconUrl,
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
		if (!mobileItemList.contains(map)) {
			mobileItemList.add(map);
		}
		map = null;
	}
}