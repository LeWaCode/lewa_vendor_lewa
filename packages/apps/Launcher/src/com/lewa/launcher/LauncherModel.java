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

import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.w;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.os.Process;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import com.lewa.launcher.theme.ThemeUtils;
/**
 * Maintains in-memory state of the Launcher. It is expected that there should
 * be only one LauncherModel object held in a static. Also provide APIs for
 * updating the database state for the Launcher.
 */
public class LauncherModel {
	static final boolean DEBUG_LOADERS = true;
	static final String LOG_TAG = "HomeLoaders";

	private static final int UI_NOTIFICATION_RATE = 4;
	private static final int DEFAULT_APPLICATIONS_NUMBER = 42;
	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

	private static final Collator sCollator = Collator.getInstance();

	private boolean mApplicationsLoaded;
	private boolean mDesktopItemsLoaded;

	private ArrayList<ItemInfo> mDesktopItems;
	private ArrayList<LauncherAppWidgetInfo> mDesktopAppWidgets;

	private HashMap<Long, FolderInfo> mFolders;

	public static ArrayList<ApplicationInfo> mApplications;

	public static ApplicationsAdapter mApplicationsAdapter;
	private ApplicationsLoader mApplicationsLoader;
	private DesktopItemsLoader mDesktopItemsLoader;
	private Thread mApplicationsLoaderThread;
	private Thread mDesktopLoaderThread;
	private int mDesktopColumns;
	private int mDesktopRows;
	private final HashMap<ComponentName, ApplicationInfo> mAppInfoCache = new HashMap<ComponentName, ApplicationInfo>(
			INITIAL_ICON_CACHE_CAPACITY);

       private static Launcher mLauncher;
	private static boolean mIsDefault = true;

	private ArrayList<ApplicationInfo> mFavoriteItems = new ArrayList<ApplicationInfo>();
	private ArrayList<UserFolderInfo> mFolderList = new ArrayList<UserFolderInfo>();
	private ArrayList<UserFolderInfo> mUserFolderList = new ArrayList<UserFolderInfo>();
	public ArrayList<String> mUnabledApps = new ArrayList<String>();
	private ArrayList<ApplicationInfo> mAllApps = new ArrayList<ApplicationInfo>();
	private static boolean mUnEnableApp = false;
	private static HashMap<String, Drawable> imageCache = new HashMap<String, Drawable>();

	synchronized void abortLoaders() {
		if (DEBUG_LOADERS)
			d(LOG_TAG, "aborting loaders");

		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  --> aborting applications loader");
			mApplicationsLoader.stop();
		}

		if (mDesktopItemsLoader != null && mDesktopItemsLoader.isRunning()) {
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  --> aborting workspace loader");
			mDesktopItemsLoader.stop();
			mDesktopItemsLoaded = false;
		}
	}

	/**
	 * Drop our cache of components to their lables & icons. We do this from
	 * Launcher when applications are added/removed. It's a bit overkill, but
	 * it's a rare operation anyway.
	 */
	synchronized void dropApplicationCache() {
		mAppInfoCache.clear();
	}

	/**
	 * Loads the list of installed applications in mApplications.
	 * 
	 * @return true if the applications loader must be started (see
	 *         startApplicationsLoader()), false otherwise.
	 */
	synchronized boolean loadApplications(boolean isLaunching,
			Launcher launcher, boolean localeChanged) {

		if (DEBUG_LOADERS)
			d(LOG_TAG, "load applications");

		if (isLaunching && mApplicationsLoaded && !localeChanged) {
			mApplicationsAdapter = new ApplicationsAdapter(launcher,
					mApplications);
                    mApplicationsAdapter.mLauncher = launcher;
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  --> applications loaded, return");
			return false;
		}

		stopAndWaitForApplicationsLoader();

		if (localeChanged) {
			dropApplicationCache();
		}

		if (mApplicationsAdapter == null || isLaunching || localeChanged) {
			mApplications = new ArrayList<ApplicationInfo>(
					DEFAULT_APPLICATIONS_NUMBER);
			mApplicationsAdapter = new ApplicationsAdapter(launcher,
					mApplications);
                    mApplicationsAdapter.mLauncher = launcher;
		}

		mApplicationsLoaded = false;

		if (!isLaunching) {
			startApplicationsLoaderLocked(launcher, false);
			return false;
		}

		return true;
	}

	private synchronized void stopAndWaitForApplicationsLoader() {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			if (DEBUG_LOADERS) {
				d(LOG_TAG, "  --> wait for applications loader ("
						+ mApplicationsLoader.mId + ")");
			}

			mApplicationsLoader.stop();
			// Wait for the currently running thread to finish, this can take a
			// little
			// time but it should be well below the timeout limit
			try {
				mApplicationsLoaderThread.join();
			} catch (InterruptedException e) {
				e(LOG_TAG, "mApplicationsLoaderThread didn't exit in time");
			}
		}
	}

	private synchronized void startApplicationsLoader(Launcher launcher,
			boolean isLaunching) {
		if (DEBUG_LOADERS)
			d(LOG_TAG, "  --> starting applications loader unlocked");
		startApplicationsLoaderLocked(launcher, isLaunching);
	}

	private void startApplicationsLoaderLocked(Launcher launcher,
			boolean isLaunching) {
		if (DEBUG_LOADERS)
			d(LOG_TAG, "  --> starting applications loader");

		stopAndWaitForApplicationsLoader();

		mApplicationsLoader = new ApplicationsLoader(launcher, isLaunching);
		mApplicationsLoaderThread = new Thread(mApplicationsLoader,
				"Applications Loader");
		mApplicationsLoaderThread.start();
	}

	synchronized void addPackage(Launcher launcher, String packageName) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(launcher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			final PackageManager packageManager = launcher.getPackageManager();
			final List<ResolveInfo> matches = findActivitiesForPackage(
					packageManager, packageName);

			if (matches.size() > 0) {
				final ApplicationsAdapter adapter = mApplicationsAdapter;
				final HashMap<ComponentName, ApplicationInfo> cache = mAppInfoCache;

				for (ResolveInfo info : matches) {
					adapter.setNotifyOnChange(false);
					ApplicationInfo app = makeAndCacheApplicationInfo(
							packageManager, cache, info, launcher);
					if (app.container == ItemInfo.NO_ID) {
						app.isUserInstalled = ((info.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0);
						addApplication2LastInDatabase(app, adapter);
					}
				}
				adapter.updateDataSet();
			}
		}
	}

	synchronized void removePackage(Launcher launcher, String packageName) {
		// for add/remove Package, we need use applications adapter's "full"
		// list.

		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			dropApplicationCache(); // TODO: this could be optimized
			startApplicationsLoaderLocked(launcher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			synchronized (ApplicationsAdapter.allItems) {
				
				final ApplicationsAdapter adapter = mApplicationsAdapter;
	
				final List<ApplicationInfo> toRemove = new ArrayList<ApplicationInfo>();
				final ArrayList<ApplicationInfo> allItems = ApplicationsAdapter.allItems;
				final int count = allItems.size();
				// begin zhaolei add
				final ArrayList<ApplicationInfo> realAllItems = new ArrayList<ApplicationInfo>();
				int size = 0;
				for (int i = 0; i < count; i++) {
					ApplicationInfo temp_info = allItems.get(i);
					if (temp_info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
						size = ((UserFolderInfo) temp_info).contents.size();
						for (int j = 0; j < size; j++) {
							realAllItems.add(((UserFolderInfo) temp_info).contents
									.get(j));
						}
					} else {
						realAllItems.add(temp_info);
					}
				}
				size = realAllItems.size();
				for (int i = 0; i < size; i++) {
					final ApplicationInfo applicationInfo = realAllItems.get(i);
					final Intent intent = applicationInfo.intent;
					final ComponentName component = intent.getComponent();
					if (packageName.equals(component.getPackageName())) {
						toRemove.add(applicationInfo);
					}
				}
	
				FolderInfo toUpdate = new FolderInfo();
				size = toRemove.size();
				for (int i = 0; i < size; i++) {
					final ApplicationInfo appInfo = toRemove.get(i);
					if (appInfo.container > 0) {
						for (int j = 0; j < count; j++) {
							if (appInfo.container == allItems.get(j).id) {
								toUpdate = (FolderInfo) allItems.get(j);
							}
						}
						((UserFolderInfo) toUpdate).contents.remove(appInfo);
						((UserFolderInfo) toUpdate).mFolderIcon.updateFolderIcon();
						if (((UserFolderInfo) toUpdate).contents.size() == 0) {
							allItems.remove(toUpdate);
							LauncherModel.deleteItemFromDatabase(mLauncher,
									toUpdate);
						}
					}
				}
				// end
				final HashMap<ComponentName, ApplicationInfo> cache = mAppInfoCache;
				for (ApplicationInfo info : toRemove) {
					cache.remove(info.intent.getComponent());
					adapter.setNotifyOnChange(false);
					adapter.remove(info);
					deleteItemFromDatabase(mLauncher, info);
					LauncherModel.resetApplications(allItems, info.screen,
							allItems.get(allItems.size() - 1).screen);
					adapter.updateDataSet();
				}
				mLauncher.getAllAppsSlidingView().autoArrange();
				toRemove.clear();
				
				// Begin [pan 110809 update favorite bar when remove app ] add
				BottomBar bar = mLauncher.getBottomBar();
				if (bar == null) {
					return;
				}
				size = bar.getChildCount();
				for (int i = 0; i < size; i++) {
					final ApplicationInfo applicationInfo = (ApplicationInfo) bar
							.getChildAt(i).getTag();
					if (applicationInfo == null) {
						continue;
					}
					final Intent intent = applicationInfo.intent;
					final ComponentName component = intent.getComponent();
					if (packageName.equals(component.getPackageName())) {
						toRemove.add(applicationInfo);
					}
				}
	
				for (ApplicationInfo info : toRemove) {
					if (info.container == LauncherSettings.Favorites.CONTAINER_FAVORITEBAR) {
						// do something
						mLauncher.setCurrentBottomButton(null);
						View dragView = bar.getChildAt(info.cellX);
						dragView.setBackgroundDrawable(null);
						dragView.setVisibility(View.GONE);
						dragView.setTag(null);
						mLauncher.setExchangeState(false);
						bar.verifyItemValidWidth();
						cache.remove(info.intent.getComponent());
						deleteItemFromDatabase(mLauncher, info);
					}
	
				}
			}
			// End
		}
	}

	synchronized void updatePackage(Launcher launcher, String packageName) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(launcher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			final PackageManager packageManager = launcher.getPackageManager();
			final ApplicationsAdapter adapter = mApplicationsAdapter;

			final List<ResolveInfo> matches = findActivitiesForPackage(
					packageManager, packageName);
			final int count = matches.size();

			boolean changed = false;

			for (int i = 0; i < count; i++) {
				final ResolveInfo info = matches.get(i);
				final ApplicationInfo applicationInfo = findIntent(adapter,
						info.activityInfo.applicationInfo.packageName,
						info.activityInfo.name);
				if (applicationInfo != null) {
					updateAndCacheApplicationInfo(packageManager, info,
							applicationInfo, launcher);
					changed = true;
				}
			}

			if (syncLocked(launcher, packageName))
				changed = true;

			if (changed) {
				adapter.updateDataSet();
			}
		}
	}

	private void updateAndCacheApplicationInfo(PackageManager packageManager,
			ResolveInfo info, ApplicationInfo applicationInfo, Context context) {
		updateApplicationInfoTitleAndIcon(packageManager, info,
				applicationInfo, context);

		ComponentName componentName = new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name);
		mAppInfoCache.put(componentName, applicationInfo);
	}

	synchronized void syncPackage(Launcher launcher, String packageName) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(launcher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			if (syncLocked(launcher, packageName)) {
				final ApplicationsAdapter adapter = mApplicationsAdapter;
				adapter.updateDataSet();
			}
		}
	}

	private boolean syncLocked(Launcher launcher, String packageName) {
		final PackageManager packageManager = launcher.getPackageManager();
		final List<ResolveInfo> matches = findActivitiesForPackage(
				packageManager, packageName);

		if (matches.size() > 0 && mApplicationsAdapter != null) {
			final ApplicationsAdapter adapter = mApplicationsAdapter;

			// Find disabled activities and remove them from the adapter
			boolean removed = removeDisabledActivities(packageName, matches,
					adapter);
			// Find enable activities and add them to the adapter
			// Also updates existing activities with new labels/icons
			boolean added = addEnabledAndUpdateActivities(matches, adapter,
					launcher);

			return added || removed;
		}

		return false;
	}

	private static List<ResolveInfo> findActivitiesForPackage(
			PackageManager packageManager, String packageName) {

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		final List<ResolveInfo> apps = packageManager.queryIntentActivities(
				mainIntent, 0);
		final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

		if (apps != null) {
			// Find all activities that match the packageName
			int count = apps.size();
			for (int i = 0; i < count; i++) {
				final ResolveInfo info = apps.get(i);
				final ActivityInfo activityInfo = info.activityInfo;
				if (packageName.equals(activityInfo.packageName)) {
					matches.add(info);
				}
			}
		}

		return matches;
	}

	private boolean addEnabledAndUpdateActivities(List<ResolveInfo> matches,
			ApplicationsAdapter adapter, Launcher launcher) {

		final List<ApplicationInfo> toAdd = new ArrayList<ApplicationInfo>();
		final int count = matches.size();

		boolean changed = false;

		for (int i = 0; i < count; i++) {
			final ResolveInfo info = matches.get(i);
			final ApplicationInfo applicationInfo = findIntent(adapter,
					info.activityInfo.applicationInfo.packageName,
					info.activityInfo.name);
			if (applicationInfo == null) {
				// Begin [pan 110819 for main iocn miss] modify
				ApplicationInfo app = makeAndCacheApplicationInfo(
						launcher.getPackageManager(), mAppInfoCache, info,
						launcher);
				if (app.container == ItemInfo.NO_ID) {
					app.isUserInstalled = ((info.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0);
					addApplication2LastInDatabase(app, adapter);
				}
				// End
				changed = true;
			} else {
				updateAndCacheApplicationInfo(launcher.getPackageManager(),
						info, applicationInfo, launcher);
				changed = true;
			}
		}

		for (ApplicationInfo info : toAdd) {
			adapter.setNotifyOnChange(false);
			adapter.add(info);
		}

		return changed;
	}

	private boolean removeDisabledActivities(String packageName,
			List<ResolveInfo> matches, ApplicationsAdapter adapter) {

		final List<ApplicationInfo> toRemove = new ArrayList<ApplicationInfo>();
		final int count = adapter.getCount();

		boolean changed = false;

		for (int i = 0; i < count; i++) {
			final ApplicationInfo applicationInfo = adapter.getItem(i);
			final Intent intent = applicationInfo.intent;
			final ComponentName component = intent.getComponent();
			if (packageName.equals(component.getPackageName())) {
				if (!findIntent(matches, component)) {
					toRemove.add(applicationInfo);
					changed = true;
				}
			}
		}

		final HashMap<ComponentName, ApplicationInfo> cache = mAppInfoCache;
		for (ApplicationInfo info : toRemove) {
			adapter.setNotifyOnChange(false);
			adapter.remove(info);
			cache.remove(info.intent.getComponent());
		}

		return changed;
	}

	private static ApplicationInfo findIntent(ApplicationsAdapter adapter,
			String packageName, String name) {

		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final ApplicationInfo applicationInfo = adapter.getItem(i);
			final Intent intent = applicationInfo.intent;
			final ComponentName component = intent.getComponent();
			if (packageName.equals(component.getPackageName())
					&& name.equals(component.getClassName())) {
				return applicationInfo;
			}
		}

		return null;
	}

	private static boolean findIntent(List<ResolveInfo> apps,
			ComponentName component) {
		final String className = component.getClassName();
		for (ResolveInfo info : apps) {
			final ActivityInfo activityInfo = info.activityInfo;
			if (activityInfo.name.equals(className)) {
				return true;
			}
		}
		return false;
	}

	synchronized Drawable getApplicationInfoIcon(PackageManager manager,
			ApplicationInfo info, Context context) {
		final ResolveInfo resolveInfo = manager.resolveActivity(info.intent, 0);
		if (resolveInfo == null || info.customIcon) {
			return null;
		}
		info.activityInfo = resolveInfo.activityInfo;
		String key = resolveInfo.activityInfo.packageName+"/"+resolveInfo.activityInfo.name;
		Drawable icon = null;
				
		if (imageCache.containsKey(key)) {
			icon = imageCache.get(key);
			 if (icon != null && !icon.equals(mLauncher.mIconBGg)) { 
                 int lewaIconSize = context.getResources()
                    .getDimensionPixelSize(R.dimen.app_lewaicon_size);
                 if(icon.getIntrinsicWidth() == lewaIconSize){
                    info.isLewaIcon = true;
                    info.iconBackground = null;
                 }
				 return icon; 
			 } 
		 }
		 
		icon = getIconForsdApps(manager, info,
				context);
		imageCache.put(key, icon);
		return icon;
	}

	private static ApplicationInfo makeAndCacheApplicationInfo(
			PackageManager manager,
			HashMap<ComponentName, ApplicationInfo> appInfoCache,
			ResolveInfo info, Context context) {

		ComponentName componentName = new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name);
		ApplicationInfo application = appInfoCache.get(componentName);

		if (application == null) {
			application = new ApplicationInfo();
			application.container = ItemInfo.NO_ID;

			updateApplicationInfoTitleAndIcon(manager, info, application,
					context);

			application.setActivity(componentName,
					Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			appInfoCache.put(componentName, application);
		}

		return application;
	}

	private static void updateApplicationInfoTitleAndIcon(
			PackageManager manager, ResolveInfo info,
			ApplicationInfo application, Context context) {
		application.title = info.loadLabel(manager);
		if (application.title == null) {
			application.title = info.activityInfo.name;
		}

		application.icon = getIcon(manager, context, info.activityInfo);
		if (mIsDefault) {
			application.iconBackground = getBackGroundKey();
		}

		application.filtered = false;
	}

	private static final AtomicInteger sAppsLoaderCount = new AtomicInteger(1);
	private static final AtomicInteger sWorkspaceLoaderCount = new AtomicInteger(
			1);

	private class ApplicationsLoader implements Runnable {
		private volatile boolean mStopped;
		private volatile boolean mRunning;
		private final boolean mIsLaunching;
		private final int mId;

		ApplicationsLoader(Launcher launcher, boolean isLaunching) {
			mIsLaunching = isLaunching;
			mLauncher = launcher;
			mRunning = true;
			mId = sAppsLoaderCount.getAndIncrement();
		}

		void stop() {
			mStopped = true;
		}

		boolean isRunning() {
			return mRunning;
		}

		private void verifyApplications(ArrayList<ApplicationInfo> fromDB, ArrayList<ApplicationInfo> result) {
			final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			final Launcher launcher = mLauncher;
			final PackageManager manager = launcher.getPackageManager();
			final List<ResolveInfo> apps = manager.queryIntentActivities(
					mainIntent, 0);
			boolean isFirst = true;
			ArrayList<ApplicationInfo> allapps = new ArrayList<ApplicationInfo>();
			if (apps != null && !mStopped) {
				int screen = 0;
				int index = -1;

				int exIndex = fromDB.size() - 1;
				if (exIndex >= 0) {
					isFirst = false;
				}

				int numPerScreen = launcher.getDrawer().getNumberPerScreen();

				final int count = apps.size();
				// Can be set to null on the UI thread by the unbind() method
				// Do not access without checking for null first
				final HashMap<ComponentName, ApplicationInfo> appInfoCache = mAppInfoCache;
				final String lewaName = "com.lewa.launchercom.lewa.launcher.Launcher";
				final boolean isRom = Launcher.mIsRom;
				
				for (int i = 0; i < count && !mStopped; i++) {
					ResolveInfo info = apps.get(i);
					
					ApplicationInfo application = findApplicationInfoByClassName(
							mAllApps, info);

					if (application == null) {
						String strName = info.activityInfo.applicationInfo.packageName.toString() 
									+ info.activityInfo.name.toString();
						if (!(isRom && lewaName.equals(strName))) {
						
							application = makeAndCacheApplicationInfo(manager,
									appInfoCache, info, launcher);

							allapps.add(application);

						}
					} else if (application.container == LauncherSettings.Favorites.CONTAINER_MAINMENU
							|| application.container > 0) {
						fromDB.remove(application);
					}
					if (application != null) {
						application.isUserInstalled = ((info.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0);
						application.isInstallFlag = info.activityInfo.applicationInfo.flags;
					}
				}
				if (isFirst) {
					Collections.sort(allapps, new ApplicationInfoComparator(
							true));
				}
				int size = allapps.size();
				for (int i = 0; i < size && !mStopped; i++) {
					final ApplicationInfo appinfo = allapps.get(i);
					appinfo.isChanged = true;
					if(isFirst) {
					index++;
					if (index >= numPerScreen) {
						screen++;
						index = 0;
					}
						appinfo.screen = screen;
						appinfo.cellX = index;
						appinfo.container = LauncherSettings.Favorites.CONTAINER_MAINMENU;
						appinfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
						result.add(appinfo);

					} else {
						int pos = addApplicationInPos(result, appinfo);
						result.add(pos, appinfo);
					}	
				}
				size = fromDB.size();
				for (int i = size - 1; i >= 0; --i) {
					if (!Intent.ACTION_MAIN.equals(fromDB.get(i).intent
							.getAction())) {
						break;
					}

					ApplicationInfo unableApss = fromDB.get(i);
					if (unableApss.itemType != LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER
							&& unableApss.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
						if ((unableApss.isInstallFlag & android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
							Log.e("LauncherModel", " title unableApss = "
									+ unableApss.title + " screen = "
									+ unableApss.screen + " index = "
									+ unableApss.cellX);
							result.remove(unableApss);
							deleteItemFromDatabase(launcher, unableApss);
						} else {
							mUnabledApps.add(fromDB.get(i).intent
									.getComponent().getPackageName());
						}
					}
				}
			}
		}

		@Override
		public void run() {
			Looper.prepare();
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  ----> running applications loader (" + mId + ")");

			// Elevate priority when Home launches for the first time to avoid
			// starving at boot time. Staring at a blank home is not cool.
			android.os.Process
					.setThreadPriority(mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT
							: Process.THREAD_PRIORITY_BACKGROUND);

			final Launcher launcher = mLauncher;// mLauncher.get();
			ArrayList<ApplicationInfo> fromDB = loadApplicationInfo(launcher);
			ArrayList<ApplicationInfo> result = new ArrayList<ApplicationInfo>();
			
			int count = fromDB.size();
			for(int i=0; i< count; i++) {
				result.add(fromDB.get(i));
			}
			
			verifyApplications(fromDB, result);
			
			final ApplicationsAdapter applicationList = mApplicationsAdapter;
			ApplicationInfo info;// = fromDB.get(0);
			ChangeNotifier action = new ChangeNotifier(applicationList,
					true);
			count = result.size();
			for (int i = 0; i < count && !mStopped; ++i) {
				info = result.get(i);
				if (info.intent != null && !info.isChanged) {
					mAppInfoCache.put(info.intent.getComponent(), info);
				}				
				
				if (action.add(info) && !mStopped) {
					launcher.runOnUiThread(action);
					action = new ChangeNotifier(applicationList, false);
				}
			}
			launcher.runOnUiThread(action);
			
			fromDB.clear();
			result.clear();
			mAllApps.clear();
			mApplicationsLoaded = !mStopped;

			if (mStopped) {
				if (DEBUG_LOADERS)
					d(LOG_TAG, "  ----> applications loader stopped (" + mId
							+ ")");
			}
			mRunning = false;
		}
	}

	private static class ChangeNotifier implements Runnable {
		private final ApplicationsAdapter mApplicationList;
		private final ArrayList<ApplicationInfo> mBuffer;

		private boolean mFirst = true;

		ChangeNotifier(ApplicationsAdapter applicationList, boolean first) {
			mApplicationList = applicationList;
			mFirst = first;
			mBuffer = new ArrayList<ApplicationInfo>(UI_NOTIFICATION_RATE);
		}

		@Override
		public void run() {
			final ApplicationsAdapter applicationList = mApplicationList;
			// Can be set to null on the UI thread by the unbind() method
			if (applicationList == null)
				return;

			if (mFirst) {
				applicationList.setNotifyOnChange(false);
				applicationList.clear();
				if (DEBUG_LOADERS)
					d(LOG_TAG, "  ----> cleared application list");
				mFirst = false;
			}

			final ArrayList<ApplicationInfo> buffer = mBuffer;
			final int count = buffer.size();

			for (int i = 0; i < count; i++) {
				applicationList.setNotifyOnChange(false);
				// Begin [pan] modify
				ApplicationInfo info = buffer.get(i);
				if (info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
					applicationList.add(info);
					if(info.isChanged) {
						info.isChanged = false;
						addItemToDatabase(mLauncher, info,
								LauncherSettings.Favorites.CONTAINER_MAINMENU,
								info.screen, info.cellX, 0, false);
					}
					applicationList.addToViewCache(mLauncher.getAllAppsSlidingView(), info);
				}
				// End
			}

			buffer.clear();

			applicationList.updateDataSet();
		}

		boolean add(ApplicationInfo application) {
			final ArrayList<ApplicationInfo> buffer = mBuffer;
			buffer.add(application);
			return buffer.size() >= UI_NOTIFICATION_RATE;
		}
	}

	static class ApplicationInfoComparator implements
			Comparator<ApplicationInfo> {
		private boolean isFirst;

		ApplicationInfoComparator(boolean first) {
			isFirst = first;
		}

		@Override
		public final int compare(ApplicationInfo a, ApplicationInfo b) {
			if (isFirst) {
				return sCollator
						.compare(a.title.toString(), b.title.toString());
			}
			if (a.screen == b.screen)
				return a.cellX - b.cellX;
			return a.screen - b.screen;
		}
	}

	boolean isDesktopLoaded() {
		return mDesktopItems != null && mDesktopAppWidgets != null
				&& mDesktopItemsLoaded;
	}

	/**
	 * Loads all of the items on the desktop, in folders, or in the dock. These
	 * can be apps, shortcuts or widgets
	 */
	void loadUserItems(boolean isLaunching, Launcher launcher,
			boolean localeChanged, boolean loadApplications) {
		if (DEBUG_LOADERS)
			d(LOG_TAG, "loading user items in "
					+ Thread.currentThread().toString());
		// ADW: load columns/rows settings
		mDesktopRows = AlmostNexusSettingsHelper.DESKTOP_ROWS;
		mDesktopColumns = AlmostNexusSettingsHelper.DESKTOP_COLUMNS;
		if (isLaunching && isDesktopLoaded()) {
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  --> items loaded, return");
			if (loadApplications)
				startApplicationsLoader(launcher, true);
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  --> loading from cache: " + mDesktopItems.size()
						+ ", " + mDesktopAppWidgets.size());
			launcher.onDesktopItemsLoaded(mDesktopItems, mDesktopAppWidgets);
			return;
		}

		if (mDesktopItemsLoader != null && mDesktopItemsLoader.isRunning()) {
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  --> stopping workspace loader");
			mDesktopItemsLoader.stop();
			// Wait for the currently running thread to finish, this can take a
			// little
			// time but it should be well below the timeout limit
			try {
				mDesktopLoaderThread.join();
			} catch (InterruptedException e) {
				e(LOG_TAG, "mDesktopLoaderThread didn't exit in time");
			}

			// If the thread we are interrupting was tasked to load the list of
			// applications make sure we keep that information in the new loader
			// spawned below
			// note: we don't apply this to localeChanged because the thread can
			// only be stopped *after* the localeChanged handling has occured
			loadApplications = mDesktopItemsLoader.mLoadApplications;
		}

		if (DEBUG_LOADERS)
			d(LOG_TAG, "  --> starting workspace loader");
		mDesktopItemsLoaded = false;
		mDesktopItemsLoader = new DesktopItemsLoader(launcher, localeChanged,
				loadApplications, isLaunching);
		mDesktopLoaderThread = new Thread(mDesktopItemsLoader,
				"Desktop Items Loader");
		mDesktopLoaderThread.start();
	}

	synchronized String getLabel(PackageManager manager,
			ActivityInfo activityInfo) {
		String label = activityInfo.loadLabel(manager).toString();
		if (label == null) {
			label = manager.getApplicationLabel(activityInfo.applicationInfo)
					.toString();
			if (label == null) {
				label = activityInfo.name;
			}
		}
		return label;
	}

	private class DesktopItemsLoader implements Runnable {
		private volatile boolean mStopped;
		private volatile boolean mFinished;

		// private final WeakReference<Launcher> mLauncher;
		private final boolean mLoadApplications;
		private final boolean mIsLaunching;
		private final int mId;

		// private boolean mLocaleChanged;

		DesktopItemsLoader(Launcher launcher, boolean localeChanged,
				boolean loadApplications, boolean isLaunching) {
			mLoadApplications = loadApplications;
			mIsLaunching = isLaunching;
			mLauncher = launcher;
            launcher.setDefaultIcon();
			mId = sWorkspaceLoaderCount.getAndIncrement();
			mFinished = false;
		}

		void stop() {
			d(LOG_TAG, "  ----> workspace loader " + mId + " stopped from "
					+ Thread.currentThread().toString());
			mStopped = true;
		}

		boolean isRunning() {
			return !mFinished;
		}

		@Override
		public void run() {
			assert (!mFinished); // can only run once
			load_workspace();
			mFinished = true;
		}

		private void load_workspace() {
			if (DEBUG_LOADERS)
				d(LOG_TAG, "  ----> running workspace loader (" + mId + ")");

			android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

			final Launcher launcher = mLauncher;
			final ContentResolver contentResolver = launcher
					.getContentResolver();
			final PackageManager manager = launcher.getPackageManager();

			final ArrayList<ItemInfo> desktopItems = new ArrayList<ItemInfo>();
			final ArrayList<LauncherAppWidgetInfo> desktopAppWidgets = new ArrayList<LauncherAppWidgetInfo>();
			final HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();

			final Cursor c = contentResolver.query(
					LauncherSettings.Favorites.CONTENT_URI, null, null, null,
					null);

			try {
				final int idIndex = c.getColumnIndexOrThrow(BaseColumns._ID);
				final int intentIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.INTENT);
				final int titleIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.TITLE);
				final int iconTypeIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ICON_TYPE);
				final int iconIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ICON);
				final int iconPackageIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ICON_PACKAGE);
				final int iconResourceIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ICON_RESOURCE);
				final int containerIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
				final int itemTypeIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ITEM_TYPE);
				final int appWidgetIdIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.APPWIDGET_ID);
				final int screenIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				final int cellXIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				final int cellYIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
				final int spanXIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANX);
				final int spanYIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.SPANY);


				ApplicationInfo info;
				String intentDescription;
				// Widget widgetInfo;
				LauncherAppWidgetInfo appWidgetInfo;
				int container;
				long id;
				Intent intent;

				while (!mStopped && c.moveToNext()) {
					try {
						int itemType = c.getInt(itemTypeIndex);
						switch (itemType) {
						case BaseLauncherColumns.ITEM_TYPE_APPLICATION:
						case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
							intentDescription = c.getString(intentIndex);
							try {
								intent = Intent.parseUri(intentDescription, 0);
							} catch (java.net.URISyntaxException e) {
								continue;
							}
							
							if (itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION) {
								info = getApplicationInfo(manager, intent, launcher);
							} else {
								info = getApplicationInfoShortcut(c, launcher,
										iconTypeIndex, iconPackageIndex,
										iconResourceIndex, iconIndex, intent);
							}

							if (info == null) {
								info = new ApplicationInfo();
								info.icon = manager.getDefaultActivityIcon();
								info.title = c.getString(titleIndex);
							}

							if (info != null) {

								info.intent = intent;
								info.title = c.getString(titleIndex);
								info.id = c.getLong(idIndex);
								container = c.getInt(containerIndex);
								info.container = container;
								info.screen = c.getInt(screenIndex);
								info.cellX = c.getInt(cellXIndex);
								info.cellY = c.getInt(cellYIndex);

								switch (container) {

								case LauncherSettings.Favorites.CONTAINER_FAVORITEBAR:
									mFavoriteItems.add(info);
									mAllApps.add(info);
									desktopItems.add(info);
									if (mUnEnableApp) {
										mUnabledApps.add(info.intent.getComponent().getPackageName());
									} else {
									    // Begin, added by zhumeiquan, 20121018
									    if (info.intent.getComponent().getClassName().equals("com.lewa.app.AppList")) {
									        info.isUserInstalled = false;
                                            info.iconBackground = null;
									    } else {
									        info.isUserInstalled = ((info.activityInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0);
									    }
									    // End
									}
									break;
								case LauncherSettings.Favorites.CONTAINER_DESKTOP:
									desktopItems.add(info);
									break;
								default:
									// Item is in a user folder
									UserFolderInfo folderInfo = findOrMakeUserFolder(folders, container);
									folderInfo.add(info);
									break;
								}
							}
							break;
						case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
							id = c.getLong(idIndex);
							UserFolderInfo folderInfo = findOrMakeUserFolder(
									folders, id);

							folderInfo.title = c.getString(titleIndex);

							folderInfo.id = id;
							container = c.getInt(containerIndex);
							folderInfo.container = container;
							folderInfo.screen = c.getInt(screenIndex);
							folderInfo.cellX = c.getInt(cellXIndex);
							folderInfo.cellY = c.getInt(cellYIndex);
							mUserFolderList.add(folderInfo);
							switch (container) {
							case LauncherSettings.Favorites.CONTAINER_DESKTOP:
							case LauncherSettings.Favorites.CONTAINER_FAVORITEBAR:
								desktopItems.add(folderInfo);
								break;
							}
							break;

						case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
							// Read all Launcher-specific widget details
							int appWidgetId = c.getInt(appWidgetIdIndex);
							appWidgetInfo = new LauncherAppWidgetInfo(
									appWidgetId);
							appWidgetInfo.id = c.getLong(idIndex);
							appWidgetInfo.screen = c.getInt(screenIndex);
							appWidgetInfo.cellX = c.getInt(cellXIndex);
							appWidgetInfo.cellY = c.getInt(cellYIndex);
							appWidgetInfo.spanX = c.getInt(spanXIndex);
							appWidgetInfo.spanY = c.getInt(spanYIndex);

							container = c.getInt(containerIndex);
							if (container != LauncherSettings.Favorites.CONTAINER_DESKTOP) {
								e(Launcher.LOG_TAG,
										"Widget found where container "
												+ "!= CONTAINER_DESKTOP -- ignoring!");
								continue;
							}
							appWidgetInfo.container = c.getInt(containerIndex);

							desktopAppWidgets.add(appWidgetInfo);
							break;
						}
					} catch (Exception e) {
						w(Launcher.LOG_TAG, "Desktop items loading interrupted:", e);
					}
				}
			} finally {
				c.close();
			}
			if (DEBUG_LOADERS) {
				d(LOG_TAG, "  ----> workspace loader " + mId
						+ " finished loading data");
				d(LOG_TAG, "  ----> worskpace items=" + desktopItems.size());
				d(LOG_TAG,
						"  ----> worskpace widgets=" + desktopAppWidgets.size());
			}

			synchronized (LauncherModel.this) {
				if (!mStopped) {
					if (DEBUG_LOADERS) {
						d(LOG_TAG, "  --> done loading workspace; not stopped");
					}
					final int size = mUserFolderList.size(); 
					for(int i=0; i< size; i++) {
						Collections.sort(mUserFolderList.get(i).contents, new ApplicationInfoComparator(false));
					}

					// Create a copy of the lists in case the workspace loader
					// is restarted
					// and the list are cleared before the UI can go through
					// them
					final ArrayList<ItemInfo> uiDesktopItems = new ArrayList<ItemInfo>(
							desktopItems);
					final ArrayList<LauncherAppWidgetInfo> uiDesktopWidgets = new ArrayList<LauncherAppWidgetInfo>(
							desktopAppWidgets);

					if (!mStopped) {
						d(LOG_TAG, "  ----> items cloned, ready to refresh UI");
						launcher.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								if (DEBUG_LOADERS)
									d(LOG_TAG, "  ----> onDesktopItemsLoaded()");
								launcher.onDesktopItemsLoaded(uiDesktopItems,
										uiDesktopWidgets);
							}
						});
					}

					mDesktopItems = desktopItems;
					mDesktopAppWidgets = desktopAppWidgets;
					mFolders = folders;
					mDesktopItemsLoaded = true;
				} else {
					if (DEBUG_LOADERS)
						d(LOG_TAG, "  ----> worskpace loader was stopped");
				}
			}
		}

		// Begin [pan 110815 load apps] add
		private void startLoadApps() {
			if (mLoadApplications) {
				if (DEBUG_LOADERS) {
					d(LOG_TAG,
							"  ----> loading applications from workspace loader");
				}
				startApplicationsLoader(mLauncher, true);
			}
		}

		// End
	}

	/**
	 * Finds the user folder defined by the specified id.
	 * 
	 * @param id
	 *            The id of the folder to look for.
	 * 
	 * @return A UserFolderInfo if the folder exists or null otherwise.
	 */
	FolderInfo findFolderById(long id) {
		if (mFolders == null)
			return null;
		return mFolders.get(id);
	}

	void addFolder(FolderInfo info) {
		if (mFolders == null)
			return;
		mFolders.put(info.id, info);
	}

	/**
	 * Return an existing UserFolderInfo object if we have encountered this ID
	 * previously, or make a new one.
	 */
	private static UserFolderInfo findOrMakeUserFolder(
			HashMap<Long, FolderInfo> folders, long id) {
		// See if a placeholder was created for us already
		FolderInfo folderInfo = folders.get(id);
		if (folderInfo == null || !(folderInfo instanceof UserFolderInfo)) {
			// No placeholder -- create a new instance
			folderInfo = new UserFolderInfo();
			folders.put(id, folderInfo);
		}
		return (UserFolderInfo) folderInfo;
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	void unbind() {
		// Interrupt the applications loader before setting the adapter to null
		stopAndWaitForApplicationsLoader();
		mApplicationsAdapter = null;
		unbindAppDrawables(mApplications);
		unbindDrawables(mDesktopItems);
		unbindAppWidgetHostViews(mDesktopAppWidgets);
		unbindCachedIconDrawables();
		imageCache.clear();
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	private void unbindDrawables(ArrayList<ItemInfo> desktopItems) {
		if (desktopItems != null) {
			final int count = desktopItems.size();
			for (int i = 0; i < count; i++) {
				ItemInfo item = desktopItems.get(i);
				switch (item.itemType) {
				case BaseLauncherColumns.ITEM_TYPE_APPLICATION:
				case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
					if(((ApplicationInfo) item).icon != null)
						((ApplicationInfo) item).icon.setCallback(null);
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	private void unbindAppDrawables(ArrayList<ApplicationInfo> applications) {
		if (applications != null) {
			final int count = applications.size();
			for (int i = 0; i < count; i++) {
				if(applications.get(i).icon != null)
					applications.get(i).icon.setCallback(null);
			}
		}
	}

	/**
	 * Remove any {@link LauncherAppWidgetHostView} references in our widgets.
	 */
	private void unbindAppWidgetHostViews(
			ArrayList<LauncherAppWidgetInfo> appWidgets) {
		if (appWidgets != null) {
			final int count = appWidgets.size();
			for (int i = 0; i < count; i++) {
				LauncherAppWidgetInfo launcherInfo = appWidgets.get(i);
				if(launcherInfo != null)
					launcherInfo.hostView = null;
			}
		}
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	private void unbindCachedIconDrawables() {
		for (ApplicationInfo appInfo : mAppInfoCache.values()) {
			if (appInfo.icon != null)
				appInfo.icon.setCallback(null);
		}
	}

	/**
	 * Fills in the occupied structure with all of the shortcuts, apps, folders
	 * and widgets in the model.
	 */
	void findAllOccupiedCells(boolean[][] occupied, int countX, int countY,
			int screen) {
		final ArrayList<ItemInfo> desktopItems = mDesktopItems;
		if (desktopItems != null) {
			final int count = desktopItems.size();
			for (int i = 0; i < count; i++) {
				// ADW: Don't load items outer current columns/rows limits
				if ((desktopItems.get(i).cellX + (desktopItems.get(i).spanX - 1)) < mDesktopColumns
						&& (desktopItems.get(i).cellY + (desktopItems.get(i).spanY - 1)) < mDesktopRows)
					addOccupiedCells(occupied, screen, desktopItems.get(i));
			}
		}

		final ArrayList<LauncherAppWidgetInfo> desktopAppWidgets = mDesktopAppWidgets;
		if (desktopAppWidgets != null) {
			final int count = desktopAppWidgets.size();
			for (int i = 0; i < count; i++) {
				addOccupiedCells(occupied, screen, desktopAppWidgets.get(i));
			}
		}
	}

	/**
	 * Add the footprint of the specified item to the occupied array
	 */
	private void addOccupiedCells(boolean[][] occupied, int screen,
			ItemInfo item) {
		if (item.screen == screen) {
			for (int xx = item.cellX; xx < item.cellX + item.spanX; xx++) {
				for (int yy = item.cellY; yy < item.cellY + item.spanY; yy++) {
					if (xx < mDesktopColumns && yy < mDesktopRows)
						occupied[xx][yy] = true;
				}
			}
		}
	}

	/**
	 * @return The current list of applications
	 */
	ApplicationsAdapter getApplicationsAdapter() {
		return mApplicationsAdapter;
	}

	/**
	 * Add an item to the desktop
	 * 
	 * @param info
	 */
	void addDesktopItem(ItemInfo info) {
		// TODO: write to DB; also check that folder has been added to folders
		// list
		if (mDesktopItems != null)
			mDesktopItems.add(info);
	}

	/**
	 * Remove an item from the desktop
	 * 
	 * @param info
	 */
	void removeDesktopItem(ItemInfo info) {
		// TODO: write to DB; figure out if we should remove folder from folders
		// list
		if (mDesktopItems != null)
			mDesktopItems.remove(info);
	}

	/**
	 * Add a widget to the desktop
	 */
	void addDesktopAppWidget(LauncherAppWidgetInfo info) {
		if (mDesktopAppWidgets != null)
			mDesktopAppWidgets.add(info);
	}

	/**
	 * Remove a widget from the desktop
	 */
	void removeDesktopAppWidget(LauncherAppWidgetInfo info) {
		if (mDesktopAppWidgets != null)
			mDesktopAppWidgets.remove(info);
	}

	/**
	 * Make an ApplicationInfo object for an application
	 */
	private static ApplicationInfo getApplicationInfo(PackageManager manager,
			Intent intent, Context context) {
		// ADW: Changed the check to avoid bypassing SDcard apps in froyo
		ComponentName componentName = intent.getComponent();
		if (componentName == null) {
			return null;
		}

		if(Launcher.mIsRom) {

			try {
				String strCls = componentName.getClassName();	
				if ( strCls != null && strCls.equals("com.lewa.PIM.dialpad.ui.DialpadActivity") ) {
					ComponentName cn = new ComponentName("com.lewa.PIM", "com.lewa.PIM.ui.DialpadEntryActivity");
					intent.setComponent(cn);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		final ApplicationInfo info = new ApplicationInfo();
		if (resolveInfo != null) {
			final ActivityInfo activityInfo = resolveInfo.activityInfo;
			info.activityInfo = activityInfo;
			mUnEnableApp = false;
			int resId = IconBackgroundSetting.isLewaIcon(activityInfo.name
					.toLowerCase().replace(".", "_"));
			String strBack = null;
			if(resId == -1) {
                
                        String find = ThemeUtils.getAppIconFileName( context, activityInfo.name, activityInfo.packageName);
                        if(find == null){
                            strBack = "iconbg";
                        }
                
			} 
			if (strBack == null) {
				info.isLewaIcon = true;
				info.icon = getIcon(manager, context, activityInfo);
				if(mIsDefault) {
					strBack = "iconbg";
				}
			} else {
				info.isLewaIcon = false; 
				info.icon = mLauncher.mDefaultIcon;
			}
			info.iconBackground = strBack;
		} else {
			// ADW: add default icon for apps on SD
			// Begin [pan 1108089 for save the removed apps] modify
			info.iconBackground = "iconbg";
			info.isLewaIcon = true;
			info.icon = mLauncher.mDefaultIcon;
			mUnEnableApp = true;
			// End
		}
		info.itemType = BaseLauncherColumns.ITEM_TYPE_APPLICATION;

		return info;
	}

	/**
	 * Make an ApplicationInfo object for a shortcut
	 */
	private static ApplicationInfo getApplicationInfoShortcut(Cursor c,
			Context context, int iconTypeIndex, int iconPackageIndex,
			int iconResourceIndex, int iconIndex, Intent intent) {
		final ApplicationInfo info = new ApplicationInfo();
		PackageManager packageManager = context.getPackageManager();
		info.itemType = BaseLauncherColumns.ITEM_TYPE_SHORTCUT;

		int iconType = c.getInt(iconTypeIndex);
		switch (iconType) {
		case BaseLauncherColumns.ICON_TYPE_RESOURCE:
			String packageName = c.getString(iconPackageIndex);
			String resourceName = c.getString(iconResourceIndex);

			try {
				Resources resources = packageManager.getResourcesForApplication(packageName);
				final int id = resources.getIdentifier(resourceName, null, null);
				// Begin, added by zhumeiquan for App list button,20121017
				if (id == R.drawable.drawer_icon) {
                                final IconBackgroundSetting iconBackSetting = mLauncher.mIconBackgroundSetting;
				    Utilities.setDefaultValue(true);
                                info.icon = iconBackSetting.getAppIcon(context, "com_lewa_app_applist", null);
                                if(info.icon == null){
                                    info.icon = Utilities.createIconThumbnail(resources.getDrawable(id), context);  
                                }
                           } else if(id == R.drawable.ic_lewaos_bbs){
                                    final IconBackgroundSetting iconBackSetting = mLauncher.mIconBackgroundSetting;
                                    Utilities.setDefaultValue(true);
                                    info.icon = iconBackSetting.getAppIcon(context, "com_lewa_lewabbs", null);
                                    if(info.icon == null){
                                        info.icon = resources.getDrawable(id);
                                        
                                    }
				}else{
                                info.icon = Utilities.createIconThumbnail(resources.getDrawable(id), context);
                            }
				// End
				
			} catch (Exception e) {
				info.icon = packageManager.getDefaultActivityIcon();
			}
			info.iconResource = new Intent.ShortcutIconResource();
			info.iconResource.packageName = packageName;
			info.iconResource.resourceName = resourceName;
			info.customIcon = false;
			break;
		case BaseLauncherColumns.ICON_TYPE_BITMAP:
			byte[] data = c.getBlob(iconIndex);
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				info.icon = new FastBitmapDrawable(
						Utilities.createBitmapThumbnail(bitmap, context));
			} catch (Exception e) {
				packageManager = context.getPackageManager();
				info.icon = packageManager.getDefaultActivityIcon();
			}
			info.filtered = true;
			info.customIcon = true;
			break;
		default:
			info.icon = context.getPackageManager().getDefaultActivityIcon();
			info.customIcon = false;
			break;
		}
		info.iconBackground = "iconbg";
		return info;
	}

	/**
	 * Remove an item from the in-memory representation of a user folder. Does
	 * not change the DB.
	 */
	void removeUserFolderItem(UserFolderInfo folder, ItemInfo info) {
		// noinspection SuspiciousMethodCalls
		folder.contents.remove(info);
	}

	/**
	 * Removes a UserFolder from the in-memory list of folders. Does not change
	 * the DB.
	 * 
	 * @param userFolderInfo
	 */
	void removeUserFolder(UserFolderInfo userFolderInfo) {
		mFolders.remove(userFolderInfo.id);
	}

	/**
	 * Adds an item to the DB if it was not created previously, or move it to a
	 * new <container, screen, cellX, cellY>
	 */
	static void addOrMoveItemInDatabase(Context context, ItemInfo item,
			long container, int screen, int cellX, int cellY) {
		if (item.container == ItemInfo.NO_ID) {
			// From all apps
			addItemToDatabase(context, item, container, screen, cellX, cellY,
					false);
		} else {
			// From somewhere else
			moveItemInDatabase(context, item, container, screen, cellX, cellY);
		}
	}

	/**
	 * Move an item in the DB to a new <container, screen, cellX, cellY>
	 */
	static void moveItemInDatabase(Context context, ItemInfo item,
			long container, int screen, int cellX, int cellY) {

		item.container = container;
		item.screen = screen;
		item.cellX = cellX;
		item.cellY = cellY;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();
		values.put(LauncherSettings.Favorites.CONTAINER, item.container);
		values.put(LauncherSettings.Favorites.CELLX, item.cellX);
		values.put(LauncherSettings.Favorites.CELLY, item.cellY);
		values.put(LauncherSettings.Favorites.SCREEN, item.screen);

		cr.update(LauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}

	/**
	 * Returns true if the shortcuts already exists in the database. we identify
	 * a shortcut by its title and intent.
	 */
	static boolean shortcutExists(Context context, String title, Intent intent) {
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
				new String[] { "title", "intent" }, "title=? and intent=?",
				new String[] { title, intent.toUri(0) }, null);
		boolean result = false;
		try {
			result = c.moveToFirst();
		} finally {
			c.close();
		}
		return result;
	}

	FolderInfo getFolderById(Context context, long id) {
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr
				.query(LauncherSettings.Favorites.CONTENT_URI,
						null,
						"_id=? and (itemType=? or itemType=?)",
						new String[] {
								String.valueOf(id),
								String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER),
								String.valueOf(LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER) },
						null);

		try {
			if (c.moveToFirst()) {
				final int itemTypeIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ITEM_TYPE);
				final int titleIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.TITLE);
				final int containerIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
				final int screenIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				final int cellXIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				final int cellYIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

				FolderInfo folderInfo = null;
				switch (c.getInt(itemTypeIndex)) {
				case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
					folderInfo = findOrMakeUserFolder(mFolders, id);
					break;
				default:
					break;
				}

				folderInfo.title = c.getString(titleIndex);
				folderInfo.id = id;
				folderInfo.container = c.getInt(containerIndex);
				folderInfo.screen = c.getInt(screenIndex);
				folderInfo.cellX = c.getInt(cellXIndex);
				folderInfo.cellY = c.getInt(cellYIndex);

				return folderInfo;
			}
		} finally {
			c.close();
		}

		return null;
	}

	/**
	 * Add an item to the database in a specified container. Sets the container,
	 * screen, cellX and cellY fields of the item. Also assigns an ID to the
	 * item.
	 */
	static void addItemToDatabase(Context context, ItemInfo item,
			long container, int screen, int cellX, int cellY, boolean notify) {
		item.container = container;
		item.screen = screen;
		item.cellX = cellX;
		item.cellY = cellY;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		item.onAddToDatabase(values);

		Uri result = cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI
				: LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
				values);

		if (result != null) {
			item.id = Integer.parseInt(result.getPathSegments().get(1));
		}
	}

	static long addFolderInDatabase(Context context, ItemInfo item,
			long container, int screen, int cellX, int cellY) {

		addItemToDatabase(context, item, container, screen, cellX, cellY, false);
		return item.id;
	}

	/**
	 * Update an item to the database in a specified container.
	 */
	static void updateItemInDatabase(Context context, ItemInfo item) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		item.onAddToDatabase(values);

		cr.update(LauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}

	/**
	 * Removes the specified item from the database
	 * 
	 * @param context
	 * @param item
	 */
	static void deleteItemFromDatabase(Context context, ItemInfo item) {
		final ContentResolver cr = context.getContentResolver();

		cr.delete(LauncherSettings.Favorites.getContentUri(item.id, false),
				null, null);
	}

	/**
	 * Remove the contents of the specified folder from the database
	 */
	static void deleteUserFolderContentsFromDatabase(Context context,
			UserFolderInfo info) {
		final ContentResolver cr = context.getContentResolver();

		cr.delete(LauncherSettings.Favorites.getContentUri(info.id, false),
				null, null);
		cr.delete(LauncherSettings.Favorites.CONTENT_URI,
				LauncherSettings.Favorites.CONTAINER + "=" + info.id, null);
	}

    static boolean containIconsFile(){

        File icons = new File("/data/system/face/icons");
        if(icons.exists()){
            return true;
        }
        return false;
        
    }

    /**
	 * Get an the icon for an activity Accounts for theme and icon shading
	 */
	static Drawable getIcon(PackageManager manager, Context context,
			ActivityInfo activityInfo) {

		Drawable icon = null;

		if(activityInfo == null) {
			return null;
		}

		final IconBackgroundSetting iconBackSetting = mLauncher.mIconBackgroundSetting;
		if(activityInfo.name != null) {
            if (!containIconsFile()) {
				icon = iconBackSetting.getIconResources(activityInfo.name
						.toLowerCase().replace(".", "_"));
			} else {
				icon = iconBackSetting.getIconResources(activityInfo.name
						.toLowerCase().replace(".", "_"), context);
			}
		}
        if(icon == null){
            icon = iconBackSetting.getAppIcon(context, activityInfo.name, activityInfo.packageName);
        }
		if (icon == null) {
			mIsDefault = true;
			String key = activityInfo.packageName+"/"+activityInfo.name;
			if (imageCache.containsKey(key)) {
				icon = imageCache.get(key);
				return icon;
			}
			icon = Utilities.createIconThumbnail(
					activityInfo.loadIcon(manager), context);
			if (activityInfo.name != null) {
				imageCache.put(key, icon);
			}
		} else {
			mIsDefault = false;
		}
		return icon;
	}

	synchronized Drawable getIconForsdApps(PackageManager manager,
			ApplicationInfo appInfo, Context context) {
        Drawable icon = IconBackgroundSetting.getAppIcon(context, appInfo.activityInfo.name, appInfo.activityInfo.packageName);
        if(icon == null){
            icon = mLauncher.mIconBackgroundSetting.createIconThumbnail(
				appInfo.activityInfo.loadIcon(manager), context);            
        }else{
            appInfo.isLewaIcon = true;
            appInfo.iconBackground = null;
        }
        return icon;
    }

	/**
	 * Resize an item in the DB to a new <container, screen, cellX, cellY>
	 */
	static void resizeItemInDatabase(Context context, ItemInfo item,
			long container, int screen, int cellX, int cellY, int spanX,
			int spanY) {
		item.container = container;
		item.screen = screen;
		item.cellX = cellX;
		item.cellY = cellY;
		item.spanX = spanX;
		item.spanY = spanY;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		values.put(LauncherSettings.Favorites.CONTAINER, item.container);
		values.put(LauncherSettings.Favorites.CELLX, item.cellX);
		values.put(LauncherSettings.Favorites.CELLY, item.cellY);
		values.put(LauncherSettings.Favorites.SPANX, item.spanX);
		values.put(LauncherSettings.Favorites.SPANY, item.spanY);
		values.put(LauncherSettings.Favorites.SCREEN, item.screen);

		cr.update(LauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}

	boolean ocuppiedArea(int screen, int id, Rect rect) {
		final ArrayList<ItemInfo> desktopItems = mDesktopItems;
		int count = desktopItems.size();
		Rect r = new Rect();
		for (int i = 0; i < count; i++) {
			if (desktopItems.get(i).screen == screen) {
				ItemInfo it = desktopItems.get(i);
				r.set(it.cellX, it.cellY, it.cellX + it.spanX, it.cellY
						+ it.spanY);
				if (rect.intersect(r)) {
					return true;
				}
			}
		}
		final ArrayList<LauncherAppWidgetInfo> desktopWidgets = mDesktopAppWidgets;
		count = desktopWidgets.size();
		for (int i = 0; i < count; i++) {
			if (desktopWidgets.get(i).screen == screen) {
				LauncherAppWidgetInfo it = desktopWidgets.get(i);
				if (id != it.appWidgetId) {
					r.set(it.cellX, it.cellY, it.cellX + it.spanX, it.cellY
							+ it.spanY);
					if (rect.intersect(r)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static void resetApplications(ArrayList<ApplicationInfo> apps,
			int fromScreen, int toScreen) {
		final int numPerScreen = mLauncher.getDrawer().getNumberPerScreen();
		int count = apps.size();
		int screen = fromScreen;
		ApplicationInfo info;
		boolean autoArrange = AlmostNexusSettingsHelper
				.getAutoArrange(mLauncher);
		int i = 0;
		int index = 0;
		for (; i < count; ++i) {
			info = apps.get(i);
			if(info!= null && info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
				if (info.screen >= fromScreen) {
					fromScreen = info.screen;
					break;
				}
			}
		}
		if (!autoArrange) {

			boolean tooMany = false;
			while (i < count) {
				info = apps.get(i);
				if(info != null && info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
					if (info.screen == fromScreen) {
						if ((info.screen != screen) || (info.cellX != index)) {
							info.screen = screen;
							info.cellX = index;
							info.isChanged = true;
						}
						index++;
						if (index >= numPerScreen) {
							++screen;
							if (fromScreen >= toScreen)
								break;
							index = 0;
							tooMany = true;
						}
						++i;
					} else {
						if (fromScreen == toScreen)
							break;
						fromScreen = info.screen;
						if (!tooMany) {
							++screen;
							index = 0;
						} else {
							if (index < numPerScreen)
								tooMany = false;
						}
					}
				}
			}
		} else {

			for (; i < count; i++) {
				info = apps.get(i);
				if(info != null && info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
					if (index != info.cellX) {
						info.screen = screen;
						info.cellX = index;
						info.isChanged = true;
	
					}
					index++;
					if (index >= numPerScreen) {
						++screen;
						if (screen > toScreen)
							return;
						index = 0;
					}
				}
			}
		}

	}

	private void addApplication2LastInDatabase(ApplicationInfo app,
			ApplicationsAdapter adapter) {
		synchronized (adapter.allItems) {
			int pos = addApplicationInPos(adapter.allItems, app);
			if (adapter.addExtendInPos(app, pos)) {
				addItemToDatabase(mLauncher, app,
						LauncherSettings.Favorites.CONTAINER_MAINMENU, app.screen,
						app.cellX, 0, false);
			}
		}
	}
	private int addApplicationInPos (ArrayList<ApplicationInfo> apps, ApplicationInfo appInfo) {
		int numPerScreen = mLauncher.getDrawer().getNumberPerScreen();
		int screen = 0;
		int index = 0;

		int count = apps.size();
		int i = 0;
		ApplicationInfo info;
		for (; i < count; i++) {
			info = apps.get(i);
			if(info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
				if (info.cellX == numPerScreen - 1) {
					index = 0;
					screen++;
				} else {
					if (info.screen == screen) {
						index++;
					} else {
						break;
					}
				}
			}
		}
		appInfo.container = LauncherSettings.Favorites.CONTAINER_MAINMENU;
		appInfo.cellX = index;
		appInfo.screen = screen;
		appInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
		return i;
	}

	public ApplicationInfo findApplicationInfoByClassName(
			ArrayList<ApplicationInfo> list, ResolveInfo resolveInfo) {
		for (ApplicationInfo info : list) {
			String pkgStr = resolveInfo.activityInfo.applicationInfo.packageName;
			String clsStr = resolveInfo.activityInfo.name;
			String infoName = info.intent.getComponent().getPackageName() 
				+ info.intent.getComponent().getClassName();
			
			if (infoName.equals(pkgStr+clsStr)) {
				ComponentName componentName = new ComponentName(pkgStr, clsStr);
				mAppInfoCache.put(componentName, info);
				list.remove(info);
				return info;
			}
			
		}
		return null;
	}

	ArrayList<ApplicationInfo> loadApplicationInfo(Context context) {
		ArrayList<ApplicationInfo> result = new ArrayList<ApplicationInfo>();

		final ContentResolver contentResolver = context.getContentResolver();
		Cursor c = contentResolver.query(
				LauncherSettings.Favorites.CONTENT_URI, null,
				LauncherSettings.Favorites.CONTAINER + "="
						+ LauncherSettings.Favorites.CONTAINER_MAINMENU
						+ " or "
						+ LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
						+ "=" + BaseLauncherColumns.ITEM_TYPE_APPLICATION,
				null, null/* LauncherSettings.Favorites.SCREEN */);

		if (c == null)
			return result;
		loadApplicationInfoFromDatabase(context, c, result);
		Collections.sort(result, new ApplicationInfoComparator(false));

		resetApplications(result);
		final int size = mFolderList.size();
		for(int i=0; i< size; i++) {
			Collections.sort(mFolderList.get(i).contents, new ApplicationInfoComparator(false));
		}
		return result;
	}

	void loadApplicationInfoFromDatabase(Context context, Cursor c,
			ArrayList<ApplicationInfo> result) {
		final PackageManager manager = context.getPackageManager();
		final HashMap<Long, FolderInfo> folders = new HashMap<Long, FolderInfo>();
		try {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				final int idIndex = c.getColumnIndexOrThrow(BaseColumns._ID);
				final int intentIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.INTENT);
				final int titleIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.TITLE);
				final int containerIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
				final int itemTypeIndex = c
						.getColumnIndexOrThrow(BaseLauncherColumns.ITEM_TYPE);
				final int screenIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
				final int cellXIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
				final int cellYIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);

				final int displayModeIndex = c
						.getColumnIndexOrThrow(LauncherSettings.Favorites.DISPLAY_MODE);
				ApplicationInfo info = null;
				Intent intent = null;
				String intentDescription = c.getString(intentIndex);

				String title = c.getString(titleIndex);
				try {
					if (intentDescription != null) {
						intent = Intent.parseUri(intentDescription, 0);
					}

				} catch (java.net.URISyntaxException e) {

					return;
				}

				final int itemType = c.getInt(itemTypeIndex);
				switch (itemType) {
				case BaseLauncherColumns.ITEM_TYPE_APPLICATION:
					long container = c.getInt(containerIndex);
					if (container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
						info = getApplicationInfo(manager, intent, context);
					} else if (container > 0) {
						info = getApplicationInfo(manager, intent, context);
						info.title = title;
						info.intent = intent;

						info.id = c.getLong(idIndex);
						info.container = container;
						info.screen = c.getInt(screenIndex);
						info.cellX = c.getInt(cellXIndex);
						info.cellY = c.getInt(cellYIndex);
						info.itemType = itemType;
						info.isInstallFlag = c.getInt(displayModeIndex);

						if (info.activityInfo == null) {
							mUnabledApps.add(info.intent.getComponent()
									.getPackageName());
						}
						UserFolderInfo folderInfo = findOrMakeUserFolder(
								folders, c.getInt(containerIndex));
						
						if(veriAllApps(info, mAllApps)) {
							folderInfo.add(info);
							mAllApps.add(info);
							mFolderList.add(folderInfo);
						}
					}

					break;
				case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
					break;
				case LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER:

					long id = c.getLong(idIndex);

					UserFolderInfo folderInfo = findOrMakeUserFolder(folders,
							id);
					folderInfo.id = id;
					folderInfo.itemType = itemType;
					folderInfo.icon = null;
					folderInfo.filtered = true;

					info = folderInfo;
					break;
				}

				if (info != null) {
					info.container = c.getInt(containerIndex);
					if(info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
						info.title = title;
						info.intent = intent;
						info.id = c.getLong(idIndex);
						info.screen = c.getInt(screenIndex);
						info.cellX = c.getInt(cellXIndex);
						info.cellY = c.getInt(cellYIndex);
						info.itemType = itemType;
						info.isInstallFlag = c.getInt(displayModeIndex);
						if(veriAllApps(info, mAllApps)) {
							mAllApps.add(info);
							result.add(info);
						}
					}
				}
				c.moveToNext();
			}
		} finally {
			c.close();
		}
	}

	public static void resetApplications(ArrayList<ApplicationInfo> apps) {
		synchronized (apps) {
			final int numPerScreen = mLauncher.getDrawer().getNumberPerScreen();
			int sameScreen = -1;
			int screen = -1;
			int index = 0;
			int count = apps.size();
			ApplicationInfo info;
			for (int i = 0; i < count; ++i) {
				info = apps.get(i);
				
				if (info.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
					if (sameScreen != info.screen && info.screen >=0) {
						sameScreen = info.screen;
						screen++;
						index = 0;
					}
					if(sameScreen < 0) {
						sameScreen = 0;
						screen++;
						index = 0;
					}
					if (index >= numPerScreen) {
						screen++;
						index = 0;
					}
					if ((info.screen != screen) || (info.cellX != index)) {
						info.screen = screen;
						info.cellX = index;
						Log.e(LOG_TAG, "Need to update databases title = "
								+ info.title + " screen = " + screen
								+ " index = " + index);
						moveItemInDatabase(mLauncher, info, info.container,
								info.screen, info.cellX, info.cellY);
					}
					index++;
				}
			}
		}
	}

	public static String getBackGroundKey() {
		return "iconbg" + (int) (5 * Math.random());
	}

	public void clearList() {
		mFavoriteItems.clear();
		mUnabledApps.clear();
		mFolderList.clear();
		mUserFolderList.clear();
	}

	public void startLoadApps() {
		mDesktopItemsLoader.startLoadApps();
	}

	static void changeItemInDatabaseByContainer(Context context, ItemInfo item,
			long container) {
		item.container = container;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		values.put(LauncherSettings.Favorites.CONTAINER, item.container);

		cr.update(LauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}
	private boolean veriAllApps(ApplicationInfo info, ArrayList<ApplicationInfo> allApps) {
			final int count = allApps.size();
			boolean found = false;
			boolean isApplication = true;
			
			if(info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
				isApplication = false;
			}
			for (int i = 0; i < count; i++) {
				ApplicationInfo athis = allApps.get(i);
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
				} else if (!isApplication) {
					if (info.id == athis.id) {
						found = true;
						break;
					}
				}
			}
		return !found;
	}
	// End
}
