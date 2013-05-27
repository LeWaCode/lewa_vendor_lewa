package com.lewa.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/*
 * add by pan for go back to home
 * */
public class HomeZone extends TextView implements DropTarget,
		DragController.DragListener {

	private Launcher mLauncher;
	//private DragLayer mDragLayer;

	//private final int[] mLocation = new int[2];
	//private final RectF mRegion = new RectF();

	public HomeZone(Context context) {
		super(context);
	}

	public HomeZone(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HomeZone(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		// TODO Auto-generated method stub
		final ItemInfo item = (ItemInfo) dragInfo;
		item.container = ItemInfo.NO_ID;
		
		if (mLauncher.getExchangeState()) {	
			mLauncher.setExchangeState(false);
			//mLauncher.getBottomBar().setDragView(null, -1);
		}
		setVisibility(GONE);
		mLauncher.setAllAppsState(false);
		mLauncher.getDeleteZone().setParamsHeight();
		mLauncher.getSlidingDrawer().animateClose();
		mLauncher.getBottomBar().close();
		mLauncher.getDesktopIndicator().setBackgroundDrawable(null);
		mLauncher.startDesktopEdit();
		// mLauncher.setEditState(false);
		
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDragEnd() {
		setVisibility(GONE);
	}

	@Override
	public void onDragStart(View v, DragSource source, Object info,
			int dragAction) {
		// TODO Auto-generated method stub
        if (mLauncher.isAllAppsVisible()
                && !(source instanceof AppInfoList)
              //  && !(((ItemInfo) info).itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER)
                ) {
            setVisibility(VISIBLE);
        }
    }

	void setLauncher(Launcher launcher) {
		mLauncher = launcher;
	}

}
