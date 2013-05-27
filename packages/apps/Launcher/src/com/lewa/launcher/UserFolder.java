package com.lewa.launcher;

import java.util.ArrayList;

import com.lewa.launcher.AsyncIconLoader.ImageCallback;
import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Folder which contains applications or shortcuts chosen by the user.
 * 
 */
public class UserFolder extends Folder implements DropTarget {
	public UserFolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Creates a new UserFolder, inflated from R.layout.user_folder.
	 * 
	 * @param context
	 *            The application's context.
	 * 
	 * @return A new UserFolder.
	 */
	static UserFolder fromXml(Context context) {
		return (UserFolder) LayoutInflater.from(context).inflate(
				R.layout.user_folder, null);
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
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
	@SuppressWarnings("unchecked")
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		final ApplicationInfo item = (ApplicationInfo) dragInfo;
		// noinspection unchecked
		((ArrayAdapter<ApplicationInfo>) mContent.getAdapter())
				.add((ApplicationInfo) dragInfo);
		LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0,
				0);
		// Begin [pan] add
		((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon();
		// End
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onDropCompleted(View target, boolean success) {
		if (success) {
			// noinspection unchecked
			ArrayAdapter<ApplicationInfo> adapter = (ArrayAdapter<ApplicationInfo>) mContent
					.getAdapter();
			adapter.remove(mDragItem);
			// Begin [pan] add
			((UserFolderInfo) mInfo).mFolderIcon.updateFolderIcon();
			// End
		}
	}

	@Override
	void bind(FolderInfo info) {
		super.bind(info);
		// setContentAdapter(new ApplicationsAdapter(mContext, ((UserFolderInfo)
		// info).contents));
		setContentAdapter(new FolderAdapter(mContext,
				((UserFolderInfo) info).contents));
	}

	// When the folder opens, we need to refresh the GridView's selection by
	// forcing a layout
	@Override
	void onOpen() {
		super.onOpen();
		requestFocus();
	}

	private class FolderAdapter extends ArrayAdapter<ApplicationInfo> {
		private LayoutInflater mInflater;
		private int mTextColor = 0;
		private boolean useThemeTextColor = false;
		private Typeface themeFont = null;

		public FolderAdapter(Context context, ArrayList<ApplicationInfo> icons) {
			super(context, 0, icons);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ApplicationInfo info = getItem(position);

			if (info.iconBackground != null) {
				convertView = mInflater.inflate(mLauncher.mShortCutStyles[0],
						parent, false);
			} else {
				convertView = mInflater.inflate(mLauncher.mShortCutStyles[1],
						parent, false);
			}

			final CounterTextView textView = (CounterTextView) convertView;

			final int iconBgPadding = mLauncher.mBgPaddingTop;
			if (info.iconBackground != null) {
				textView.mBgPaddingTop = iconBgPadding;
				textView.setPadding(2, mLauncher.mIconPaddingTop, 2, 0);
				textView.mBackGround = mLauncher.mIconBGg;
			} else {
				textView.setPadding(2, iconBgPadding, 2, 0);
			}

			if(!info.isLewaIcon || Launcher.mLocaleChanged) {
				info = mLauncher.mAsyncIconLoad.loadDrawable(info, false ,new ImageCallback() {
					
					@Override
					public void imageLoaded(ApplicationInfo appInfo) {
						// TODO Auto-generated method stub
						textView.setCompoundDrawablesWithIntrinsicBounds(null, appInfo.icon, null,
								null);
						textView.setText(appInfo.title);
						textView.setTag(appInfo);
					}
				});
			}

			textView.setCompoundDrawablesWithIntrinsicBounds(null, info.icon,
					null, null);
			textView.setText(info.title);
			textView.mIsInFolder = true;
			
			// Begin [pan for moon 110908] add
			//if (info.iconBackground != null) {
			//	textView.mIcon_bg = mLauncher.mIcon_bg;
			//}

			if (useThemeTextColor) {
				textView.setTextColor(mTextColor);
			}
			// ADW: Custom font
			if (themeFont != null)
				textView.setTypeface(themeFont);
			return convertView;
		}

	}
}
