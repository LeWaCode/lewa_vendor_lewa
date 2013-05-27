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

import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class DeleteZone extends ImageView implements DropTarget,
		DragController.DragListener {
	private static final int TRANSITION_DURATION = 250;
	private static final int ANIMATION_DURATION = 200;
	private static final String LOG_TAG = "DeleteZone";

	private final int[] mLocation = new int[2];

	private Launcher mLauncher;
	private boolean mTrashMode;

	private AnimationSet mInAnimation;
	private AnimationSet mOutAnimation;

	private DragLayer mDragLayer;

	private final RectF mRegion = new RectF();
	private TransitionDrawable mTransition;
	private boolean shouldUninstall = false;
	private final Handler mHandler = new Handler();
	private boolean mUninstallTarget = false;
	String UninstallPkg = null;
	// Begin [pan] add
	private EditModeZone mEditZone = null;
	private RectF mDeleteRect = null;
	private int mMainMenuTrashHeight = 0;

	// End

	public DeleteZone(Context context) {
		super(context);
	}

	public DeleteZone(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DeleteZone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.DeleteZone, defStyle, 0);
		mMainMenuTrashHeight = a.getDimensionPixelSize(
				R.styleable.DeleteZone_mainMenu_trash_height,
				mMainMenuTrashHeight);
		a.recycle();
	}

	/*
	 * @Override protected void onFinishInflate() { super.onFinishInflate();
	 * mTransition = (TransitionDrawable) getBackground(); }
	 */

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		if (mDeleteRect != null && mDeleteRect.contains(x, y)) {
			return true;
		} else {
			return false;
		}
		// return mPosition != POSITION_NONE;
	}

	public static boolean isServiceRunning(Context mContext, String className) {

		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(30);
		if (!(serviceList.size() > 0)) {
			return false;
		}

		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		return isRunning;

	}

	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, Object dragInfo, Rect recycle) {
		return null;
	}

	private void deleteFolder(AllAppsSlidingView allAppsView, int pos,
			UserFolderInfo info) {
		LauncherModel.deleteItemFromDatabase(mLauncher, info);
		synchronized (ApplicationsAdapter.allItems) {
			
			ArrayList<ApplicationInfo> allItems = ApplicationsAdapter.allItems;
			allItems.remove(pos);
	
			int appsCount;
	
			if (info.contents.size() > 0) {
				int SERCCN_ITEM_NUM = 16;
				ApplicationInfo app = info.contents.get(0);
				app.container = info.container;
				app.screen = info.screen;
				app.cellX = info.cellX;
				allItems.add(pos, app);
				LauncherModel.moveItemInDatabase(mLauncher, app, app.container,
						app.screen, app.cellX, 0);
				int totalScreens = allAppsView.getTotalScreens();
				allAppsView.addNewPage(totalScreens + 1);
				int j = 1;
				int startPos = 0;
				for (int i = info.screen; i < totalScreens + 1; i++) {
					startPos = allAppsView.getFirstPositionInScreen(i);
					appsCount = allAppsView.getAppCountInScreen(i);
					while (appsCount < SERCCN_ITEM_NUM && j < info.contents.size()) {
						app = info.contents.get(j);
						app.container = info.container;
						app.screen = i;
						app.cellX = appsCount;
						allItems.add(app.cellX + startPos, app);
						LauncherModel.moveItemInDatabase(mLauncher, app,
								app.container, app.screen, app.cellX, 0);
						j++;
						appsCount++;
					}
				}
	
				if (allAppsView.getAppCountInScreen(j) == 0) {
					allAppsView.removePage();
				}
				info.contents.clear();
			}
			LauncherModel.resetApplications(allItems, info.screen, mLauncher
                    .getAllAppsSlidingView().getTotalScreens());
			allAppsView.getAdapter().updateDataSet();
		}
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// Begin [pan] add
		// being zhaolei add
		if (dragInfo instanceof ApplicationInfo
				&& ((ApplicationInfo) dragInfo).itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
			final AllAppsSlidingView slidingView = (AllAppsSlidingView) source;
			UserFolderInfo dragItem = (UserFolderInfo) slidingView.mItemInfo;
			int dragPos = dragItem.cellX
					+ slidingView.getFirstPositionInScreen(dragItem.screen);
			if (dragItem.contents.size() > 0) {
				// mLauncher.onCreateDialog(5);
			}
			deleteFolder(slidingView, dragPos, dragItem);
			return;
		}
		// end
		// Begin [pan 110823] add
		if (!(dragInfo instanceof ItemInfo)) {
			return;
		}
		// End
		final ItemInfo item = (ItemInfo) dragInfo;

		if (item.container == -1)
			return;
		// Begin [pan] add
		if (mLauncher.isAllAppsVisible()) {
			if (mLauncher.getAllAppsSlidingView().getIsRemovePage()
					&& (mLauncher.getAllAppsSlidingView().getStartDragScreen() == mLauncher
							.getAllAppsSlidingView().getCurrentScreen())) {
				mLauncher.getAllAppsSlidingView().setIsRemovePage(false);
			}
			if (item.isUserInstalled && UninstallPkg != null) {
				Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
						Uri.parse("package:" + UninstallPkg));
				DeleteZone.this.getContext().startActivity(uninstallIntent);
			} else if (item.isUserInstalled && UninstallPkg == null) {
				if (item.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
					ApplicationInfo app = (ApplicationInfo) item;
					Launcher.getModel().removePackage(mLauncher,
							app.intent.getComponent().getPackageName());
				} else if (item.container == LauncherSettings.Favorites.CONTAINER_FAVORITEBAR) {
					ApplicationInfo app = (ApplicationInfo) item;
					Launcher.getModel().removePackage(mLauncher,
							app.intent.getComponent().getPackageName());
				}
			} else {
				Toast.makeText(mLauncher,
						mContext.getString(R.string.lewa_appdelete),
						Toast.LENGTH_SHORT).show();
				if (mLauncher.getExchangeState()) {
					if (item.container == LauncherSettings.Favorites.CONTAINER_FAVORITEBAR) {
						// do something
					}
					mLauncher.setExchangeState(false);
				}
			}
			return;
		}
		if (item.container == LauncherSettings.Favorites.CONTAINER_MAINMENU
				|| item.container == LauncherSettings.Favorites.CONTAINER_FAVORITEBAR)
			return;
		// End
		final LauncherModel model = Launcher.getModel();

		if (item.container == LauncherSettings.Favorites.CONTAINER_DESKTOP) {
			if (item instanceof LauncherAppWidgetInfo) {
				model.removeDesktopAppWidget((LauncherAppWidgetInfo) item);
			} else {
				model.removeDesktopItem(item);
			}
		} else {
			if (source instanceof AppFolder) {
				final AppFolder userFolder = (AppFolder) source;
				final UserFolderInfo userFolderInfo = (UserFolderInfo) userFolder
						.getInfo();
				model.removeUserFolderItem(userFolderInfo, item);
				userFolderInfo.mFolderIcon.updateFolderIcon();
			}
		}
		if (item instanceof UserFolderInfo) {
			final UserFolderInfo userFolderInfo = (UserFolderInfo) item;
			LauncherModel.deleteUserFolderContentsFromDatabase(mLauncher,
					userFolderInfo);
			model.removeUserFolder(userFolderInfo);
		} else if (item instanceof LauncherAppWidgetInfo) {
			final LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) item;
			final LauncherAppWidgetHost appWidgetHost = mLauncher
					.getAppWidgetHost();
			mLauncher.getWorkspace().unbindWidgetScrollableId(
					launcherAppWidgetInfo.appWidgetId);
			if (appWidgetHost != null) {
				appWidgetHost
						.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
			}
		}
		LauncherModel.deleteItemFromDatabase(mLauncher, item);
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		mTransition.reverseTransition(TRANSITION_DURATION);

		// Begin [pan] add
		if (!(dragInfo instanceof ItemInfo))
			return;
		// End
		// ADW: show uninstall message
		final ItemInfo item = (ItemInfo) dragInfo;
		mTransition.reverseTransition(TRANSITION_DURATION);
		if ((item instanceof ApplicationInfo && item.itemType != LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER)
				|| item instanceof LauncherAppWidgetInfo) {
			mUninstallTarget = true;
			mHandler.removeCallbacks(mShowUninstaller);
			mHandler.postDelayed(mShowUninstaller, 500);
		}
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		mTransition.reverseTransition(TRANSITION_DURATION);
		// ADW: not show uninstall message
		// ADW We need to call this delayed cause onDragExit is always called
		// just before onDragEnd :(
		mHandler.removeCallbacks(mShowUninstaller);
		if (shouldUninstall) {
			mUninstallTarget = false;
			mHandler.postDelayed(mShowUninstaller, 100);
		}
	}

	@Override
	public void onDragStart(View v, DragSource source, Object info,
			int dragAction) {

		if (source instanceof AppInfoList || mLauncher.isPreviewing()) {
			setVisibility(GONE);
			return;
		}
		
		if(mTrashMode) {
			return;
		}
		// Begin [pan] add
		if (!mLauncher.isAllAppsVisible()) {
			setParamsHeight();
			mTrashMode = true;
			createAnimations();
			mTransition.resetTransition();

			if (mLauncher.getBottomBar().getVisibility() == View.VISIBLE)
				mLauncher.getBottomBar().close();
			if (mLauncher.isEditZoneVisibility())
				mEditZone.startOutAnimation();
			if (mLauncher.getDesktopIndicator() != null)
				mLauncher.getDesktopIndicator().setBackgroundDrawable(null);

			startAnimation(mInAnimation);
			setVisibility(VISIBLE);
			return;
		}
		// End
		setParamsHeight();

		final ItemInfo item = (ItemInfo) info;
		UninstallPkg = null;
		if (item != null) {
			mTrashMode = true;
			createAnimations();
			// Begin [pan] modify
			// setDeleteRegion();
			mTransition.resetTransition();
			startAnimation(mInAnimation);
			setVisibility(VISIBLE);
			// End
			// ADW Store app data for uninstall if its an Application
			// ADW Thanks to irrenhaus@xda & Rogro82@xda :)
			if (item instanceof ApplicationInfo
					&& !(item instanceof FolderInfo)) {
				try {
					final ApplicationInfo appInfo = (ApplicationInfo) item;
					if (appInfo.iconResource != null)
						UninstallPkg = appInfo.iconResource.packageName;
					else {
						PackageManager mgr = DeleteZone.this.getContext()
								.getPackageManager();
						ResolveInfo res = mgr
								.resolveActivity(appInfo.intent, 0);
						UninstallPkg = res.activityInfo.packageName;
					}
				} catch (Exception e) {
					Log.w(LOG_TAG, "Could not load shortcut icon: " + item);
					UninstallPkg = null;
				}
			} else if (item instanceof LauncherAppWidgetInfo) {
				LauncherAppWidgetInfo appwidget = (LauncherAppWidgetInfo) item;
				final AppWidgetProviderInfo aw = AppWidgetManager.getInstance(
						mLauncher).getAppWidgetInfo(appwidget.appWidgetId);
				if (aw != null)
					UninstallPkg = aw.provider.getPackageName();
			}
		}
	}

	@Override
	public void onDragEnd() {
		if (mTrashMode) {
			mTrashMode = false;
			mDragLayer.setDeleteRegion(null);
			startAnimation(mOutAnimation);
			setVisibility(INVISIBLE);
			// Begin [pan] add
			if (!mLauncher.isAllAppsVisible()) {
				if (!mLauncher.isPreviewing()
						&& !mLauncher.isEditZoneVisibility()) {
					Workspace workspace = mLauncher.getWorkspace();
					for (int i = 0; i < workspace.getChildCount(); i++) {
						workspace.getChildAt(i).setBackgroundDrawable(null);
					}
					mLauncher.getBottomBar().open();
					mLauncher
							.getDesktopIndicator()
							.setBackgroundDrawable(
									mLauncher
											.bitmap2drawable(R.drawable.desktopindicator_bg));
				} else if (mLauncher.isPreviewing()
						&& !mLauncher.isEditZoneVisibility()) {
					mLauncher.getDrawerHandle().open();
				} else if (!mLauncher.isPreviewing()
						&& mLauncher.isEditZoneVisibility()) {
					mEditZone.startInAnimation();
					mLauncher
							.getDesktopIndicator()
							.setBackgroundDrawable(
									mLauncher
											.bitmap2drawable(R.drawable.desktopindicator_bg));
				}
			}
			mLauncher.getWorkspace().setPadding(0, 0, 0, 0);
			// mLauncher.setDockPadding(0);
			mLauncher.getWorkspace().requestLayout();
		}
		// Begin [pan] delete
		/*
		 * if (shouldUninstall && UninstallPkg != null) { // Intent
		 * uninstallIntent = new Intent(Intent.ACTION_DELETE, //
		 * Uri.parse("package:"+UninstallPkg)); //
		 * DeleteZone.this.getContext().startActivity(uninstallIntent);
		 * mLauncher.removeShortcutsForPackage(UninstallPkg); }
		 */

	}

	private void createAnimations() {
		if (mInAnimation == null) {
			mInAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mInAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(0.0f, 1.0f));

			animationSet.addAnimation(new TranslateAnimation(
					Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
					Animation.RELATIVE_TO_SELF, 1.0f,
					Animation.RELATIVE_TO_SELF, 0.0f));

			animationSet.setDuration(ANIMATION_DURATION);
		}
		if (mOutAnimation == null) {
			mOutAnimation = new FastAnimationSet();
			final AnimationSet animationSet = mOutAnimation;
			animationSet.setInterpolator(new AccelerateInterpolator());
			animationSet.addAnimation(new AlphaAnimation(1.0f, 0.0f));

			animationSet.addAnimation(new FastTranslateAnimation(
					Animation.ABSOLUTE, 0.0f, Animation.ABSOLUTE, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.0f,
					Animation.RELATIVE_TO_SELF, 1.0f));

			animationSet.setDuration(ANIMATION_DURATION);
		}
	}

	void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

	void setDragController(DragLayer dragLayer) {
		mDragLayer = dragLayer;
	}

	private static class FastTranslateAnimation extends TranslateAnimation {
		public FastTranslateAnimation(int fromXType, float fromXValue,
				int toXType, float toXValue, int fromYType, float fromYValue,
				int toYType, float toYValue) {
			super(fromXType, fromXValue, toXType, toXValue, fromYType,
					fromYValue, toYType, toYValue);
		}

		@Override
		public boolean willChangeTransformationMatrix() {
			return true;
		}

		@Override
		public boolean willChangeBounds() {
			return false;
		}
	}

	private static class FastAnimationSet extends AnimationSet {
		FastAnimationSet() {
			super(false);
		}

		@Override
		public boolean willChangeTransformationMatrix() {
			return true;
		}

		@Override
		public boolean willChangeBounds() {
			return false;
		}
	}

	// ADW Runnable to show the uninstall message (or reset the uninstall
	// status)
	private final Runnable mShowUninstaller = new Runnable() {
		@Override
		public void run() {
			shouldUninstall = mUninstallTarget;
			if (shouldUninstall && mLauncher.isAllAppsVisible()) {
				Toast.makeText(mContext, R.string.drop_to_uninstall, 500)
						.show();
			}
		}
	};

	@Override
	public void setBackgroundDrawable(Drawable d) {
		// TODO Auto-generated method stub
		super.setBackgroundDrawable(d);
		mTransition = (TransitionDrawable) d;
	}

	// Begin [pan] add
	public void setParamsHeight() {
		FrameLayout.LayoutParams params = (LayoutParams) getLayoutParams();
		if (mLauncher.isAllAppsVisible()) {
			// params.height = mLauncher.getIndicatorHeight() * 4 / 5;
			// setScaleType(ScaleType.CENTER_INSIDE);
			params.height = mMainMenuTrashHeight;
			setScaleType(ScaleType.CENTER_INSIDE);
		} else {
			params.height = mLauncher.getHandleHeight();
		}
		setLayoutParams(params);
	}

	private void setDeleteRegion() {
		final int[] location = mLocation;
		mLauncher.getWorkspace().requestLayout();
		getLocationOnScreen(location);
		mLauncher.getWorkspace().requestLayout();
		mRegion.set(location[0],
				location[1] + (getBottom() - getTop()) * 2 / 3, location[0]
						+ getRight() - getLeft(), location[1] + getBottom()
						- getTop());

		mDeleteRect = new RectF(0, mDragLayer.getHeight() - getHeight() / 3,
				mDragLayer.getWidth(), mDragLayer.getHeight());
		// mDeleteRect = new RectF(mRegion);
		mDragLayer.setDeleteRegion(mRegion);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		setDeleteRegion();

		super.onLayout(changed, left, top, right, bottom);
	}

	public void setEditModeZone(EditModeZone editzone) {
		mEditZone = editzone;
	}
	// End
}
