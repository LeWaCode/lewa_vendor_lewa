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

import com.lewa.launcher.AsyncIconLoader.ImageCallback;
import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;
import com.lewa.launcher.theme.ThemeConstants;
import com.lewa.launcher.theme.ThemeUtils;

import java.io.File;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An icon that can appear on in the workspace representing an
 * {@link UserFolder}.
 */
public class FolderIcon extends CounterTextView implements DropTarget {
	private UserFolderInfo mInfo;
	private Launcher mLauncher;
	private Drawable mCloseIcon;
	// private Drawable mOpenIcon;
	// Begin [pan] add
	private static final int ICON_COUNT = 4;
	private static final int NUM_COL = 2;
	private int sIconWidth;
	// End

	// begin zhaolei add
	private static final int MAX_COUNT_IN_APPFOLDER = 12;

	public FolderIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		final Resources resources = context.getResources();
		sIconWidth  = (int) resources
				.getDimension(R.dimen.app_icon_size);
	}

	public FolderIcon(Context context) {
		super(context);		
		final Resources resources = context.getResources();
		sIconWidth  = (int) resources
				.getDimension(R.dimen.app_icon_size);
	}

	static FolderIcon fromXml(int resId, Launcher launcher, ViewGroup group,
			UserFolderInfo folderInfo) {

		FolderIcon icon = (FolderIcon) LayoutInflater.from(launcher).inflate(
				resId, group, false);
		// TODO:ADW Load icon from theme/iconpack

		icon.setText(folderInfo.title);
		icon.setTag(folderInfo);
		icon.setOnClickListener(launcher);
		icon.mInfo = folderInfo;
		icon.mLauncher = launcher;
		// Begin [pan] add
		icon.updateFolderIcon();
		folderInfo.setFolderIcon(icon);
		// End
		
		if(folderInfo.contents.size() >= MAX_COUNT_IN_APPFOLDER) {
            launcher.getAllAppsSlidingView()
                    .setAppFolderBg(launcher.getResources().getDrawable(
                                    R.drawable.folder_full));
        }
		return icon;
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		if(!(dragInfo instanceof ItemInfo)){
			return false;
		}
		final ItemInfo item = (ItemInfo) dragInfo;
		final int itemType = item.itemType;

		return (itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION || itemType == BaseLauncherColumns.ITEM_TYPE_SHORTCUT)
				&& item.container != mInfo.id;
	}

	public Rect estimateDropLocation(DragSource source, int x, int y,
			int xOffset, int yOffset, Object dragInfo, Rect recycle) {
		return null;
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {	    
	    if(source instanceof AppFolder && mLauncher.getBottomBar().getVisibility() == View.VISIBLE) {
	        return;
	    }
	    if(mLauncher.isAllAppsVisible() && !mLauncher.getAllAppsSlidingView().getIsAppFolder()){
	        return;
	    }
	    if(source instanceof AppInfoList) {
            return;
        }
	    mLauncher.getAllAppsSlidingView().setIsAppFolder(false);
		if (mInfo.contents.size() >= MAX_COUNT_IN_APPFOLDER) {
			Toast t = Toast.makeText(getContext(), R.string.full_folder,
					Toast.LENGTH_SHORT);
			t.show();
			if(source instanceof Workspace)
			    mLauncher.getWorkspace().setFolderFull(true);
			return;
		}

		final ApplicationInfo item = (ApplicationInfo) dragInfo;

		if (item.container == LauncherSettings.Favorites.CONTAINER_MAINMENU) {
			mLauncher.setAppFolderState(new ApplicationInfo(item));
		}
		if (mLauncher.getExchangeState()) {
			mLauncher.setCurrentBottomButton(null);
			mLauncher.setExchangeState(false);
		}
		mInfo.add(item);
		item.cellX = mInfo.contents.size() - 1;
		item.screen = mInfo.screen;
		LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, item.screen, item.cellX,
				0);
		// Begin [pan] add
		updateFolderIcon();
		// End
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// setCompoundDrawablesWithIntrinsicBounds(null, mOpenIcon, null,
		// null);pan
	    //mFolderIconBg = mLauncher.getAllAppsSlidingView().getAppFolderBg();
	}

	/* (non-Javadoc)
	 * @see com.lewa.launcher.DropTarget#onDragOver(com.lewa.launcher.DragSource, int, int, int, int, java.lang.Object)
	 */
	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
	    if(mInfo.contents.size() >= MAX_COUNT_IN_APPFOLDER) {            
	        mLauncher.getAllAppsSlidingView().setIsFullFolder(true);
        } else {
            mLauncher.getAllAppsSlidingView().setIsFullFolder(false);
        }
	 //   mLauncher.getAllAppsSlidingView().startMoveTask();
	    if(mLauncher.isAllAppsVisible()) {
	        mLauncher.getAllAppsSlidingView().onDragOver(source, x, y, xOffset, yOffset, dragInfo);
	    } else {
	        mLauncher.getWorkspace().onDragOver(source, x, y, xOffset, yOffset, dragInfo);
	    }
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null,
		// null);pan
	    if(mLauncher.getAllAppsSlidingView().getAppFolderBg() != null) {
            mLauncher.getAllAppsSlidingView().getAppFolderBg().setBounds(new Rect());
        }
	    if(!mLauncher.isAllAppsVisible()) {
	    	Workspace workspace = mLauncher.getWorkspace();
	    	workspace.clearCellBg();
	        CellLayout layout = (CellLayout) workspace.getChildAt(workspace.getCurrentScreen());
	        layout.invalidate();
	    }
	}

	/**
	 * ADW: Load the floder icon drawables from the theme
	 * 
	 * @param context
	 * @param manager
	 * @param themePackage
	 * @param resourceName
	 * @return
	 */
	static Drawable loadFolderFromTheme(Context context,
			PackageManager manager, String themePackage, String resourceName) {
		Drawable icon = null;
		Resources themeResources = null;
		try {
			themeResources = manager.getResourcesForApplication(themePackage);
		} catch (NameNotFoundException e) {
			// e.printStackTrace();
		}
		if (themeResources != null) {
			int resource_id = themeResources.getIdentifier(resourceName,
					"drawable", themePackage);
			if (resource_id != 0) {
				icon = themeResources.getDrawable(resource_id);
			}
		}
		return icon;
	}

	public void updateFolderIcon() {
		Drawable icon;
		float x, y;
		Bitmap closebmp;
                String iconPath = "/data/system/face/icons";
                File file = new File(iconPath);
                if (!file.exists()) {

			closebmp = BitmapFactory.decodeStream(
					getResources().openRawResource(R.drawable.com_android_forder),
					null, mLauncher.getBitmapOptions());
		} else {
			Drawable folderIcon = ThemeUtils.getThemeDrawable(mContext,
					ThemeConstants.THEME_ICONS_MODEL, "com_android_forder");
			if(folderIcon == null){
			    closebmp = BitmapFactory.decodeStream(
	                    getResources().openRawResource(R.drawable.com_android_forder),
	                    null, mLauncher.getBitmapOptions());
			}else {
			closebmp = ((BitmapDrawable) folderIcon).getBitmap();
            }
		}

		Utilities.setDefaultValue(true);
		closebmp = Utilities.createBitmapThumbnail(closebmp, mContext);

		int iconWidth = closebmp.getWidth();
		int iconHeight = closebmp.getHeight();
		Bitmap folderclose = Bitmap.createBitmap(iconWidth, iconHeight,
				Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(folderclose);
		canvas.drawBitmap(closebmp, 0, 0, null);
		Matrix matrix = new Matrix();

		final Resources resources = mLauncher.getResources();
		int MARGIN = (int) resources.getDimension(R.dimen.folder_icon_margin);
		int PADDING = (int) resources.getDimension(R.dimen.folder_icon_padding);

		float scaleWidth = (iconWidth - MARGIN * 2) / NUM_COL - 2 * PADDING;
		float scale = (scaleWidth / iconWidth);
		matrix.postScale(scale, scale);
		for (int i = 0; i < ICON_COUNT; i++) {
			if (i < mInfo.contents.size()) {
				x = MARGIN + PADDING * (2 * (i % NUM_COL) + 2) + scaleWidth
						* (i % NUM_COL);
				y = MARGIN + PADDING * (2 * (i / NUM_COL) + 2) + scaleWidth
						* (i / NUM_COL);

				ApplicationInfo info;				
                if(mInfo.contents.size() <= ICON_COUNT) {
                    info = mInfo.contents.get(i);
                } else {
                    info =  mInfo.contents.get(mInfo.contents.size() - ICON_COUNT + i);
                }
                
                if(info == null) {
                    return;
                }
                
               if(mLauncher!=null && mLauncher.mAsyncIconLoad != null && (!info.isLewaIcon || Launcher.mLocaleChanged)) {
        			info = mLauncher.mAsyncIconLoad.loadDrawable(info, false ,new ImageCallback() {
        				
        				@Override
        				public void imageLoaded(ApplicationInfo appInfo) {
        					// TODO Auto-generated method stub
        					updateFolderIcon();
        				}
        			});
        		}
            	if(info.container != ItemInfo.NO_ID){
            		icon = info.icon;
    			} else {
    				icon = null;
    			}

				if (icon == null)
					continue;
				/*if (!info.filtered) {
					Utilities.setDefaultValue(false);
					icon = Utilities.createIconThumbnail(icon, mContext);
				}*/
				
				Bitmap iconbmp = drawableToBitmap(icon);

				Bitmap scalebmp = Bitmap.createBitmap(iconbmp, 0, 0,
						iconbmp.getWidth(), iconbmp.getHeight(), matrix, true);
				canvas.drawBitmap(scalebmp, x, y, null);
				if (iconbmp != null) {
					iconbmp.recycle();
					iconbmp = null;
				}
				if (scalebmp != null) {
					scalebmp.recycle();
					scalebmp = null;
				}
			}
		}
		mCloseIcon = new FastBitmapDrawable(folderclose);
		setCompoundDrawablesWithIntrinsicBounds(null, mCloseIcon, null, null);
		if (mInfo.itemType != LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
			setPadding(0, mLauncher.mPaddingTop, 0, 0);
		} else {
			setPadding(0, mLauncher.mIconPaddingTop, 0, 0);
		}
		setTextSize(13.0f);
		canvas.drawBitmap(folderclose, 0, 0, null);
	}

	public Bitmap drawableToBitmap(Drawable drawable) {

		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		float scale = (float)sIconWidth/(float)drawable.getIntrinsicWidth();
		canvas.scale(scale, scale);
		drawable.setBounds(0, 0, sIconWidth, sIconWidth);
		drawable.draw(canvas);
		return bitmap;
	}

}
