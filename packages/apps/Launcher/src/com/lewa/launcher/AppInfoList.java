/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lewa.launcher;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;
import com.lewa.launcher.theme.ThemeConstants;
import com.lewa.launcher.theme.ThemeUtils;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.Toast;

/**
 * Adapter showing the types of items that can be added to a {@link Workspace}.
 * 
 * @param <T>
 */

public class AppInfoList extends Gallery implements OnItemLongClickListener,
		AdapterView.OnItemSelectedListener, DragSource {
	private Context mContext;
	private Launcher mLauncher;
	private DragLayer mDragLayer;
	private int mItemType;
	private Workspace mWorkspace;
	private int mClickItemPos = -1;
	private static final LauncherModel sModel = new LauncherModel();

	// begin zhaolei add
	private boolean mIsOpen = false;
	private boolean mIsAppFolder = false;
	private AppInfoAdapter mAppListAdapter;
	private int mCurrentPos;
	private PackageManager mPackagemanager;
	private AppWidgetManager mAppwidgetmanager = null;

	// end

	public AppInfoList(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void loadAppList(Launcher launcher, int itemType) {
		mLauncher = launcher;
		mItemType = itemType;
		mWorkspace = mLauncher.getWorkspace();
		mPackagemanager = mLauncher.getPackageManager();
		mAppListAdapter = new AppInfoAdapter(itemType);
		setAdapter(mAppListAdapter);
	}

	public void setDragController(DragLayer dragLayer) {
		mDragLayer = dragLayer;
	}

	class AppInfoAdapter extends BaseAdapter {
		private Typeface themeFont;
		private ArrayList<? extends Object> mItems;

		public AppInfoAdapter(int itemType) {
			themeFont = mLauncher.getThemeFont();
			loadRefItems();
			setFocusable(false);
			setSelected(false);
		}

		public void loadRefItems() {
			switch (mItemType) {
			case BaseLauncherColumns.ITEM_TYPE_SHORTCUT: {
				synchronized (ApplicationsAdapter.allItems) {
				
					ArrayList<ApplicationInfo> app_info = new ArrayList<ApplicationInfo>();
					final int size = ApplicationsAdapter.allItems.size();
					int count = 0;
					for (int i = 0; i < size; i++) {
						ApplicationInfo temp_info = ApplicationsAdapter.allItems
								.get(i);
						if (temp_info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
							if (!mLauncher.isAllAppsVisible()) {
								count = ((UserFolderInfo) temp_info).contents.size();
								for (int j = 0; j < count; j++) {
									ApplicationInfo infoInFolder = ((UserFolderInfo) temp_info).contents
									.get(j);
									if(!infoInFolder.isLewaIcon && mLauncher.mDefaultIcon.equals(infoInFolder.icon)) {
										infoInFolder.icon =  Launcher.getModel()
										.getApplicationInfoIcon(mLauncher.getPackageManager(),
												infoInFolder, mLauncher);
									}
									app_info.add(infoInFolder);
								}
							}
						} else {
							app_info.add(temp_info);
						}
					}
					mItems = app_info;
				}
				break;
			}
			case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER: {
				ArrayList<String> str = new ArrayList<String>();
				String[] strFolder = mLauncher.getResources().getStringArray(
						R.array.folders);
				final int length = strFolder.length;
				for (int i = 0; i < length; i++) {
					str.add(strFolder[i]);
				}
				mItems = str;
				break;
			}
			case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
				mAppwidgetmanager = AppWidgetManager.getInstance(mContext);
				List<AppWidgetProviderInfo> widgetList = mAppwidgetmanager
						.getInstalledProviders();
				mItems = (ArrayList<? extends Object>) widgetList;
				break;
			default:
				break;
			}
			if (mLauncher.isAllAppsVisible()) {
				setPosition();
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CharSequence labelText = null;
			Drawable icon = null;
			String backstr = null;
			if (convertView == null) {
				switch (mItemType) {
				case BaseLauncherColumns.ITEM_TYPE_SHORTCUT: {
					ApplicationInfo item = (ApplicationInfo) mItems
							.get(position);
					labelText = item.title;
					icon = item.icon;
					backstr = item.iconBackground;
					break;
				}
				case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER: {
					labelText = (String) mItems.get(position);

                                        String iconPath = "/data/system/face/icons";
                                        File file = new File(iconPath);
                                        if (file.exists()) {
						icon = ThemeUtils.getThemeDrawable(mLauncher,
								ThemeConstants.THEME_ICONS_MODEL,
								"com_android_forder");
						if (icon == null) {
							icon = mLauncher.getResources().getDrawable(
									R.drawable.com_android_forder);
						}
					} else {
						icon = mLauncher.getResources().getDrawable(
								R.drawable.com_android_forder);
					}
					if(icon != null){
						Utilities.setDefaultValue(true);
						icon = Utilities.createIconThumbnail(icon, mLauncher);
					}
					backstr = null;
					break;
				}
				case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET: {
					AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo) mItems
							.get(position);
					if (widgetInfo.icon != 0) {
						icon = mPackagemanager.getDrawable(
								widgetInfo.provider.getPackageName(),
								widgetInfo.icon, null);
						if(icon == null) {
							icon = mLauncher.mDefaultIcon;
						}
						Utilities.setDefaultValue(false);
						icon = Utilities.createIconThumbnail(icon, mLauncher);
						labelText = widgetInfo.label;
						backstr = null;
					}
					break;
				}
				}
				LayoutInflater mInflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = mInflater.inflate(R.layout.counter_layout, null);
				convertView.setLayoutParams(new Gallery.LayoutParams(mLauncher
						.getDrawerHandle().getWidth() / 4, mLauncher
						.getDrawerHandle().getWidth() / 4));

				CounterTextView textView = (CounterTextView) convertView;
				if (mItemType == LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET) {
					textView.setMaxLines(2);
				}
				textView.setText(labelText);
				textView.mIsInFolder = true;
				textView.setCompoundDrawablesWithIntrinsicBounds(null, icon,
						null, null);
				final int bgPaddingTop = mLauncher.mBgPaddingTop;
				if (backstr != null) {
					textView.setCompoundDrawablePadding(bgPaddingTop);
					textView.setPadding(2, mLauncher.mIconPaddingTop, 2, 2);
					textView.mBgPaddingTop = bgPaddingTop;
					textView.mBackGround = mLauncher.mIconBGg;
					textView.mIconTopDrawable = mLauncher.mIconTopg;
				} else {
					textView.setCompoundDrawablePadding(bgPaddingTop / 6);
					if (mItemType != LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET){
						textView.setPadding(2, bgPaddingTop, 2, 2);
                    }else{
                        textView.setPadding(2, bgPaddingTop*2, 2, 2);
                    }
					textView.mBackGround = null;
				}
				if (themeFont != null) {
					textView.setTypeface(themeFont);
				}
				convertView.setTag(position);
				convertView.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent me) {
						// TODO Auto-generated method stub
						if (me.getAction() == MotionEvent.ACTION_DOWN) {
							mClickItemPos = (Integer) v.getTag();
						}
						return false;
					}
				});
			}
			return convertView;
		}

		@Override
		public int getCount() {
			return mItems.size();
		}

		@Override
		public Object getItem(int position) {
			return mItems.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void remove(Object info) {
			mItems.remove(info);
		}

		public ArrayList<?> getAllItems() {
			return mItems;
		}

		public void setPosition() {
			for (int i = 0; i < mItems.size(); i++) {
				((ApplicationInfo) mItems.get(i)).cellX = i;
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		switch (mItemType) {
		case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
			ApplicationInfo app = (ApplicationInfo) parent
					.getItemAtPosition(position);
			if (!mLauncher.isAllAppsVisible()) {
				app = new ApplicationInfo(app);
				if (mLauncher.getOpenFolder() == null) {
					// mLauncher.startDesktopEdit();
					// mLauncher.getWorkspace().enableChildrenCache();
					app.container = ItemInfo.NO_ID;
					sModel.addDesktopItem(app);
					mWorkspace.lock();
					// ((CellLayout)
					// mWorkspace.getChildAt(mWorkspace.getCurrentScreen()))
					// .setBackgroundResource(R.drawable.addwidget_bg);

				} else {
					//app.container = ItemInfo.NO_ID;
					// mDragLayer.startDrag(view, this, app,
					// DragController.DRAG_ACTION_MOVE);
				}
			}
			app.cellX = position;
			mDragLayer.startDrag(view, this, app,
					DragController.DRAG_ACTION_MOVE);
			break;
		case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
			UserFolderInfo folderInfo = new UserFolderInfo();
			folderInfo.title = (String) parent.getItemAtPosition(position);
			// mLauncher.startDesktopEdit();
			// mLauncher.getWorkspace().enableChildrenCache();
			mWorkspace.lock();
			// ((CellLayout)
			// mWorkspace.getChildAt(mWorkspace.getCurrentScreen()))
			// .setBackgroundResource(R.drawable.addwidget_bg);
			mDragLayer.startDrag(view, this, folderInfo,
					DragController.DRAG_ACTION_MOVE);
			break;
		case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
			AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo) mAppListAdapter
					.getItem(mClickItemPos);

			mLauncher.mAppWidgetId = mLauncher.mAppWidgetHost
					.allocateAppWidgetId();
			try {
				AppWidgetManager appwidgetmanager = mAppwidgetmanager;
				ComponentName componentname = widgetInfo.provider;
				appwidgetmanager.bindAppWidgetId(mLauncher.mAppWidgetId,
						componentname);
			} catch (IllegalArgumentException illegalargumentexception) {
				Toast.makeText(mLauncher, "load app widget error", 500).show();
				return false;
			}

			mDragLayer.startDrag(view, this, widgetInfo,
					DragController.DRAG_ACTION_MOVE);
			break;
		}
		mClickItemPos = -1;
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if ((mAppListAdapter.getCount() - getSelectedItemPosition() == 4)
				&& (e1.getX() - e2.getX() > 0)) {
			return false;
		}
		return super.onFling(e1, e2, velocityX, velocityY);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		if ((mAppListAdapter.getCount() - getSelectedItemPosition() == 4)
				&& (e1.getX() - e2.getX() > 0)) {
			return false;
		}
		mClickItemPos = -1;
		return super.onScroll(e1, e2, distanceX, distanceY);
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		if(mClickItemPos == -1) {
			return false;
		}
		if ((e.getAction() == MotionEvent.ACTION_UP)) {
			switch (mItemType) {
			case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
				// ApplicationInfo app = (ApplicationInfo) parent
				// .getItemAtPosition(position);
				if (mClickItemPos < mAppListAdapter.getCount()) {
					ApplicationInfo app = (ApplicationInfo) mAppListAdapter
							.getItem(mClickItemPos);
					if (mLauncher.getOpenFolder() != null) {
						AppFolder openFolder = (AppFolder) mLauncher
								.getOpenFolder();
						if (!mLauncher.isAllAppsVisible()) {
							app = new ApplicationInfo(app);
						} else {
							mAppListAdapter.setPosition();
						}
						openFolder.addAppInFolder(app);
						mLauncher.getAllAppsSlidingView().onDropCompleted(
								openFolder, true);
					} else {
						mLauncher.completeAddShortcut(
								app = new ApplicationInfo(app), true);
					}
				}
				break;
			case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
			
				mLauncher.addFolder(
						(String) mAppListAdapter.getItem(mClickItemPos),
						true);
			
				break;

			case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
				if ( mAppwidgetmanager != null) {
					AppWidgetProviderInfo widgetInfo = (AppWidgetProviderInfo) mAppListAdapter
							.getItem(mClickItemPos);
					if (widgetInfo.configure != null) {
						Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
						intent.setComponent(widgetInfo.configure);

						final int widgetId = mLauncher.mAppWidgetHost
								.allocateAppWidgetId();
						try {
							AppWidgetManager appwidgetmanager = mAppwidgetmanager;
							ComponentName componentname = widgetInfo.provider;
							appwidgetmanager.bindAppWidgetId(widgetId,
									componentname);
						} catch (IllegalArgumentException illegalargumentexception) {
							Toast.makeText(mLauncher, "load app widget error",
									500).show();
							return false;
						}
						intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
								widgetId);
						mLauncher.startActivityForResult(intent,
								Launcher.REQUEST_CREATE_APPWIDGET);

					} else {
						mLauncher.completeAddAppWidget(widgetInfo, null, true);
					}
				}
				break;
			}
		}
		mClickItemPos = -1;
		return false;
	}

	public boolean getAppListState() {
		return mIsOpen;
	}

	public void setAppListState(boolean listState) {
		mIsOpen = listState;
	}

	public boolean getIsAppFolderState() {
		return mIsAppFolder;
	}

	public void setIsAppFolder(boolean isAppFolder) {
		mIsAppFolder = isAppFolder;
	}

	public int getCurrentPos() {
		return mCurrentPos;
	}

	public AppInfoAdapter getAdapter() {
		return mAppListAdapter;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		mCurrentPos = position;
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub

	}
}
