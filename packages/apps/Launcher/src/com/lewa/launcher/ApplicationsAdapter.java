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
import java.util.HashMap;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.lewa.launcher.AsyncIconLoader.ImageCallback;

/**
 * GridView adapter to show the list of applications and shortcuts
 */
public class ApplicationsAdapter extends ArrayAdapter<ApplicationInfo> {
	private final LayoutInflater mInflater;
	private int mTextColor = 0;
	private boolean useThemeTextColor = false;
	private Typeface themeFont = null;
	// TODO: Check if allItems is used somewhere else!
	public static ArrayList<ApplicationInfo> allItems = new ArrayList<ApplicationInfo>();
	public static HashMap<ApplicationInfo, View> viewCache = new HashMap<ApplicationInfo, View>();
	private CatalogueFilter filter;
	private boolean mWithDrawingCache = true;
	public static Launcher mLauncher;

	public ApplicationsAdapter(Context context, ArrayList<ApplicationInfo> apps) {
		super(context, 0, apps);
		mLauncher = (Launcher)context;
		mInflater = LayoutInflater.from(context);
	}

	public void buildViewCache(ViewGroup parent) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ApplicationInfo info = getItem(i);
				synchronized (info) {
					addToViewCache(parent, info);
				}
		}
	}

	void addToViewCache(ViewGroup parent, ApplicationInfo info) {
	    if (info == null || mLauncher == null) {
	        return;
	    }

		if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {

			FolderIcon appFolder = FolderIcon.fromXml(mLauncher.mShortCutStyles[2],
					mLauncher, parent, (UserFolderInfo) info);
			viewCache.put(info, appFolder);
			return;
		}
		View convertView = null;
		// Begin [pan] add
		if (info.iconBackground != null) {
			convertView = mInflater.inflate(mLauncher.mShortCutStyles[0],
					parent, false);
		} else {
			convertView = mInflater.inflate(mLauncher.mShortCutStyles[1],
					parent, false);
		}
		// End
		convertView.setDrawingCacheEnabled(mWithDrawingCache);
		viewCache.put(info, convertView);
		// Begin [pan] modify
		// final TextView textView = (TextView) convertView;
		final CounterTextView textView = (CounterTextView) convertView;
		if (info.iconBackground != null) {
			textView.mBgPaddingTop = mLauncher.mIconPaddingTop;
			textView.setPadding(2, mLauncher.mPaddingTop, 2, 0);
		} else {
			textView.setPadding(2, mLauncher.mIconPaddingTop, 2, 0);
		}
		// End
             ApplicationInfo infochanged = null;
		if(mLauncher!=null && mLauncher.mAsyncIconLoad != null && (!info.isLewaIcon || Launcher.mLocaleChanged)) {
			synchronized (info) {
			infochanged = mLauncher.mAsyncIconLoad.loadDrawable(info, false,
					new ImageCallback() {

						@Override
						public void imageLoaded(ApplicationInfo appInfo) {
							textView.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null, null);
							textView.setText(appInfo.title);
							textView.setTag(appInfo);
						}
					});
				}
		}

		if (infochanged == null) {
			infochanged = info;
		}
		textView.setCompoundDrawablesWithIntrinsicBounds(null, infochanged.icon, null, null);
		textView.setPressed(true);
		textView.setText(infochanged.title);
		textView.setTag(infochanged);
		if (useThemeTextColor) {
			textView.setTextColor(mTextColor);
		}
		
		if (infochanged.iconBackground != null) {
			textView.mBackGround = mLauncher.mIconBGg;
			textView.mIconTopDrawable = mLauncher.mIconTopg;
		}
		// ADW: Custom font
		if (themeFont != null) {
			textView.setTypeface(themeFont);
		}
	}

	public void setChildDrawingCacheEnabled(boolean aValue) {
		if (mWithDrawingCache != aValue) {
			mWithDrawingCache = aValue;
			for (View v : viewCache.values()) {
				v.setDrawingCacheEnabled(aValue);
				if (aValue) {
					v.buildDrawingCache();
				} else {
					v.destroyDrawingCache();
				}
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ApplicationInfo info = getItem(position);
		if (!viewCache.containsKey(info))
			addToViewCache(parent, info);

		View result = viewCache.get(info);

		return result;
	}

	@Override
	public void add(ApplicationInfo info) {
		// check allItems before added. It is a fix for all of the multi-icon
		// issue, but will
		// lose performance. Anyway, we do not expected to have many
		// applications.
		synchronized (allItems) {
			/*
			 * if (!allItems.contains(info)) { changed = true;
			 * allItems.add(info); Collections.sort(allItems,new
			 * ApplicationInfoComparator()); }
			 */
			final int count = allItems.size();
			boolean found = false;
			int i = 0;
			int size = 0;
			boolean isApplication = true;
			
			if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
				isApplication = false;
			}
			
			for (; i < count; i++) {
				ApplicationInfo athis = allItems.get(i);
				if(athis.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && isApplication) {
					if (info.intent.getComponent() != null) {
						if (athis.intent.getComponent().flattenToString().equals(info.intent.getComponent().flattenToString())) {
							found = true;
							break;
						}
					}
				} else if (athis.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER && isApplication) {
					size = ((UserFolderInfo) athis).contents.size();
					for (int j=0; j < size; j++) {
						ApplicationInfo infolder = (((UserFolderInfo) athis).contents.get(j));
						if (info.intent.getComponent() != null) {
							if (infolder.intent.getComponent().flattenToString().equals(info.intent.getComponent().flattenToString())) {
								found = true;
								break;
							}
						}
					}
						
				} else if (!isApplication) {
					if (info.id == athis.id) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				if(isApplication) {
					//ArrayList<ApplicationInfo> favoriteItems = LauncherModel.mFavoriteItems;
					BottomBar favoriteItems = mLauncher.getBottomBar();
					if(favoriteItems == null){
						return;
					}
					size = favoriteItems.getChildCount();
					for (i = 0; i < size; i++) {
						ApplicationInfo athis = (ApplicationInfo) favoriteItems.getChildAt(i).getTag();
	
						if(athis == null) {
							continue;
						}
						
						if (athis.intent.getComponent().flattenToString().equals(info.intent.getComponent().flattenToString())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {
					allItems.add(info);
					// Collections.sort(allItems,new
					// ApplicationInfoComparator());
					updateDataSet();
				}
			}
		}
	}

	// 2 super functions, to make sure related add/clear do not affect allItems.
	// in current Froyo/Eclair, it is not necessary.
	void superAdd(ApplicationInfo info) {
		if (info != null)
			super.add(info);
	}

	void superClear() {
		super.clear();
	}

	@Override
	public void remove(ApplicationInfo info) {
		synchronized (allItems) {
			// allItems.remove(info);
			final int count = allItems.size();
			int size = 0;
			for (int i = 0; i < count; i++) {
				ApplicationInfo athis = allItems.get(i);
				if(athis.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
					if (info.intent.getComponent() != null) {
						if (athis.intent
								.getComponent()
								.flattenToString()
								.equals(info.intent.getComponent()
										.flattenToString())) {
							viewCache.remove(athis);
							allItems.remove(i);
							// Collections.sort(allItems,new
							// ApplicationInfoComparator());
							/*for (int j = i; j < count - 1; ++j) {
								info = allItems.get(j);
								if (info.screen != athis.screen)
									break;
								info.cellX--;
								info.isChanged = true;
							}*/
							updateDataSet();
							break;
						}
					}
				} else if (athis.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
					size = ((UserFolderInfo) athis).contents.size();
					for (int j=0; j < size; j++) {
						ApplicationInfo infolder = (((UserFolderInfo) athis).contents.get(j));
						if (info.intent.getComponent() != null) {
							if (infolder.intent
									.getComponent()
									.flattenToString()
									.equals(info.intent.getComponent()
											.flattenToString())) {
								viewCache.remove(athis);
								allItems.remove(i);
								updateDataSet();
								break;
							}
						}
					}
						
				}
		
			}
		}
	}

	private String getComponentName(ApplicationInfo info) {
		if (info == null || info.intent == null)
			return null;
		ComponentName cmpName = info.intent.getComponent();
		if (cmpName == null)
			return null;
		return cmpName.flattenToString();
	}

	private void filterApps(ArrayList<ApplicationInfo> theFiltered,
			ArrayList<ApplicationInfo> theItems) {
		theFiltered.clear();
		// AppGrpUtils.checkAndInitGrp();
		if (theItems != null) {
			int length = theItems.size();

			for (int i = 0; i < length; i++) {
				ApplicationInfo info = theItems.get(i);
				String s = getComponentName(info);
				if (s != null) {
					theFiltered.add(info);
				}
			}
		}
	}

	// filter,sort,update
	public void updateDataSet() {
		getFilter().filter(null);
	}

	@Override
	public Filter getFilter() {
		if (filter == null)
			filter = new CatalogueFilter();
		return filter;
	}

	private class CatalogueFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			FilterResults result = new FilterResults();
			ArrayList<ApplicationInfo> filt = new ArrayList<ApplicationInfo>();

			synchronized (allItems) {
				filterApps(filt, allItems);
			}

			result.values = filt;
			result.count = filt.size();
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// NOTE: this function is *always* called from the UI thread.
			ArrayList<ApplicationInfo> localFiltered = (ArrayList<ApplicationInfo>) results.values;

			setNotifyOnChange(false);
			superClear();
			// there could be a serious sync issue.
			// very bad
			final int count = results.count;
			for (int i = 0; i < count; i++) {
				superAdd(localFiltered.get(i));
			}

			notifyDataSetChanged();

		}
	}

	public boolean addExtendInPos(ApplicationInfo info, int pos) {
		synchronized (allItems) {
			/*
			 * if (pos > allItems.size()) { allItems.add(info); } else {
			 * allItems.add(pos, info); } updateDataSet();
			 */
			final int count = allItems.size();
			boolean found = false;
			int i = 0;
			int size = 0;
			boolean isApplication = true;
			
			if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
				isApplication = false;
			}
			
			for (; i < count; i++) {
				ApplicationInfo athis = allItems.get(i);
				if(athis.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION && isApplication) { 
					if (info.intent.getComponent() != null) {
						if (athis.intent
								.getComponent()
								.flattenToString()
								.equals(info.intent.getComponent()
										.flattenToString())) {
							found = true;
							break;
						}
					}
				} else if (athis.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER && isApplication) {
					size = ((UserFolderInfo) athis).contents.size();
					for (int j=0; j < size; j++) {
						ApplicationInfo infolder = (((UserFolderInfo) athis).contents.get(j));
						if (info.intent.getComponent() != null) {
							if (infolder.intent
									.getComponent()
									.flattenToString()
									.equals(info.intent.getComponent()
											.flattenToString())) {
								found = true;
								break;
							}
						}
					}
						
				} else if (!isApplication) {
					if(info.id == athis.id) {
						found = true;
						break;
					}
				}
			}
			if (!found) {		
				if(isApplication) {
					//ArrayList<ApplicationInfo> favoriteItems = LauncherModel.mFavoriteItems;
					BottomBar favoriteItems = mLauncher.getBottomBar();
					if(favoriteItems == null){
						return !found;
					}
					size = favoriteItems.getChildCount();
					for (i = 0; i < size; i++) {
						ApplicationInfo athis = (ApplicationInfo) favoriteItems.getChildAt(i).getTag();
	
						if(athis == null) {
							continue;
						}
	
						if (athis.intent
								.getComponent()
								.flattenToString()
								.equals(info.intent.getComponent()
										.flattenToString())) {
							found = true;
							break;
						}
					}
				}
				if (!found) {	
					if (pos > allItems.size()) {
						allItems.add(info);
					} else {
						allItems.add(pos, info);
					}
				// Collections.sort(allItems,new ApplicationInfoComparator());
				updateDataSet();
				}
			}
			return !found;
		}
	}
	

	public void addExtend(ApplicationInfo info) {
		synchronized (allItems) {
			allItems.add(info);
			updateDataSet();
		}
	}
	
	// End
}
