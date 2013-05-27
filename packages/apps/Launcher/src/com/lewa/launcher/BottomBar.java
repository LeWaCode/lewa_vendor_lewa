package com.lewa.launcher;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lewa.launcher.AsyncIconLoader.ImageCallback;
import com.lewa.launcher.DragController.DragListener;
import com.lewa.launcher.LauncherSettings.BaseLauncherColumns;

public class BottomBar extends LinearLayout implements DragSource, DropTarget, DragListener {
    private Launcher mLauncher;

    final static int BUTTON_COUNT = 5;
    final static int FAVORITEBAR_SCREEN = -1;

    private int mMovePos = -1;

    private int mItemsValidLeft = 0;
    private int mItemValidWidth = 0;
    
    private int mBgPaddingTop;
    private int mIconPaddingTop;
    
    private CounterImageView mDragView = null;
    private static final int ANIM_DURATION = 250;
    
    private boolean isFromMainMenu = false;
    private boolean mIsDragOver;
    private Drawable mOverBoundsView;

    public BottomBar(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	
    	mLauncher = (Launcher)context;
    	Resources resources = getResources();
    	mBgPaddingTop = (int) resources.getDimension(R.dimen.favorite_iconbg_top);
    	mIconPaddingTop = (int) resources.getDimension(R.dimen.favorite_icon_top);
    	mOverBoundsView = resources.getDrawable(R.drawable.pressed_application_background);
    	mOverBoundsView.setCallback(this);
    	mItemsValidLeft = getPaddingLeft();
    	mItemValidWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }
	
    public void setChildView(ApplicationInfo info) {
        final CounterImageView textView = (CounterImageView) getChildAt(info.cellX);
        if (info.iconBackground != null) {
            textView.mBgPaddingTop = mBgPaddingTop;
            textView.setPadding(0, mIconPaddingTop, 0, mIconPaddingTop);
            textView.mBackGround = mLauncher.mIconBGg;
            textView.mIconTopDrawable = mLauncher.mIconTopg;
        } else {
            textView.mBgPaddingTop = 0;
            textView.setPadding(0, mBgPaddingTop, 0, mBgPaddingTop);
            textView.mBackGround = null;
            textView.mIconTopDrawable = null;
    	}
    	
        if (mLauncher != null && mLauncher.mAsyncIconLoad != null && (!info.isLewaIcon || Launcher.mLocaleChanged)) {

            synchronized (info) {
                info = mLauncher.mAsyncIconLoad.loadDrawable(info, false,
                    new ImageCallback() {
                        @Override
                        public void imageLoaded(ApplicationInfo appInfo) {
                            textView.setImageDrawable(appInfo.icon);
                            textView.setTag(appInfo);
                        }
                });
            }
        }

        textView.setImageDrawable(info.icon);
        textView.setTag(info);
        textView.setVisibility(View.VISIBLE);
        textView.setOnClickListener(mLauncher);
        textView.setOnLongClickListener(mLauncher);
        verifyItemValidWidth();
    }

    @Override
    public void onDropCompleted(View target, boolean success) {
	    
    }

    @Override
    public void onDrop(DragSource source, int x, int y, int xOffset,
    		int yOffset, Object dragInfo) {
        ItemInfo info = (ItemInfo) dragInfo;
        if (info == null || info.screen == FAVORITEBAR_SCREEN) {
            return;
        }
    
        boolean accept = true;
        switch (info.itemType) {
        case BaseLauncherColumns.ITEM_TYPE_APPLICATION:
            if (info.container > 0) {
                Toast.makeText(mContext,R.string.toast_appinfolder_not_supported, Toast.LENGTH_SHORT).show();
                accept = false;
                break;
            }
        case BaseLauncherColumns.ITEM_TYPE_SHORTCUT:
            if (info.container > 0) {
                Toast.makeText(mContext,R.string.toast_appinfolder_not_supported, Toast.LENGTH_SHORT).show();
                accept = false;
            }
            break;
        case LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
        case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
            accept = false;
            break;
        case LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
            Toast.makeText(mContext, R.string.toast_widgets_not_supported, Toast.LENGTH_SHORT).show();
            accept = false;
            break;
        case LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER:
        	Toast.makeText(mContext, R.string.toast_appfolder_not_supported, Toast.LENGTH_SHORT).show();
        	accept = false;
        	break;
        default:
        	Toast.makeText(mContext, R.string.toast_unknown_item, Toast.LENGTH_SHORT).show();
        	accept = false;
        	break;
        }
        
        final LauncherModel model = Launcher.getModel();
        //We need to remove current item from database before adding the new one
        if (info instanceof LauncherAppWidgetInfo) {
            model.removeDesktopAppWidget((LauncherAppWidgetInfo) info);
            LauncherAppWidgetInfo launcherAppWidgetInfo = (LauncherAppWidgetInfo) info;
            LauncherAppWidgetHost appWidgetHost = mLauncher.getAppWidgetHost();
            if (appWidgetHost != null) {
            	appWidgetHost.deleteAppWidgetId(launcherAppWidgetInfo.appWidgetId);
            }
        }
        
        if (accept) {
            int pos = (x - mItemsValidLeft) / mItemValidWidth;
            
            if (pos < 0 ) {
              pos = 0;
            }else if(pos > getVisibityViewCount() - 1){
                pos =  getVisibityViewCount() - 1;
            }
            
            int cellx = getCellXbyPos(pos);
            ApplicationInfo currentInfo = (ApplicationInfo) getChildAt(cellx).getTag();
            ApplicationInfo fromInfo = (ApplicationInfo) info;
            fromInfo = new ApplicationInfo(fromInfo);
            fromInfo.screen = FAVORITEBAR_SCREEN;
            if(currentInfo != null) {
            	fromInfo.cellX = currentInfo.cellX;
            }else {
            	fromInfo.cellX = cellx;
            }

            if (source instanceof AppFolder) {
            	AppFolder fromAppFolder = (AppFolder) source;
            	fromAppFolder.removeFromAdapter((ApplicationInfo) dragInfo);
            	((UserFolderInfo) fromAppFolder.mInfo).contents.remove((ApplicationInfo) dragInfo);
            	((UserFolderInfo) fromAppFolder.mInfo).mFolderIcon.updateFolderIcon();
            }
            
            setChildView(fromInfo);
            mLauncher.setExchangeState(true);
            mLauncher.setCurrentBottomButton(currentInfo);
            isFromMainMenu = true;
            if (currentInfo != null) {
            	LauncherModel.moveItemInDatabase(mLauncher, info,
            			LauncherSettings.Favorites.CONTAINER_FAVORITEBAR,
            			FAVORITEBAR_SCREEN, cellx, -1);
            	isFromMainMenu = false;
            	if (mDragView != null) {
            		mDragView = null;
            	}
            	mMovePos = -1;
            }
    	}
    }

    @Override
    public void onDragEnter(DragSource source, int x, int y, int xOffset,
    		int yOffset, Object dragInfo) {

    }

    @Override
    public void onDragOver(DragSource source, int x, int y, int xOffset,
    		int yOffset, Object dragInfo) {
        mOverBoundsView.setBounds(new Rect());
        invalidate();

        if(dragInfo instanceof UserFolderInfo || source instanceof AppFolder) {
            return;
        }
    
        ApplicationInfo info = (ApplicationInfo) dragInfo;
        if (info.container == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
            return;
        }

        int pos = (x - mItemsValidLeft) / mItemValidWidth;
 
        if (pos < 0 || pos > getVisibityViewCount() - 1) {
            return;		
        }
    
        int cellX = getCellXbyPos(pos);  
        View toView = getChildAt(cellX);        
        
        int goneViewCellx = getGoneViewCellx();
        if (mDragView == null && goneViewCellx == -1) {
            if (source instanceof AllAppsSlidingView) {
                mIsDragOver = true;
                if (toView != null) {
                    mOverBoundsView.setBounds(toView.getLeft(), toView.getTop(),
                            toView.getRight(), toView.getBottom());
                }
            }
            return;
        }
    
        mIsDragOver = false;
        mOverBoundsView.setBounds(new Rect());

        if (mDragView == null && goneViewCellx != -1) {
            View v = getChildAt(goneViewCellx);
            v.setVisibility(View.INVISIBLE);
            verifyItemValidWidth();
            mMovePos = (x - mItemsValidLeft) / mItemValidWidth;
            int vericellx = getCellXbyPos(mMovePos);
            if (goneViewCellx != vericellx) {
                removeView(v);
                addView(v, vericellx);
        	}
            mDragView = (CounterImageView) v;
            return;
        }
    
        int dragPos = mMovePos;
        if (dragPos == pos) {
        	return;
        }
        
        CounterImageView dragView = mDragView;		
		
        removeViewAt(getCellXbyPos(dragPos));
        addView(dragView, cellX);
        
        mMovePos = pos;
        
        final int[] translateLocation = new int[2];
        final int[] targetLocation = new int[2];
        toView.getLocationOnScreen(translateLocation);
        dragView.getLocationOnScreen(targetLocation);
        playSingleViewAnimation(toView, translateLocation[0], targetLocation[0], translateLocation[1], targetLocation[1]);
    }

    public void playSingleViewAnimation(View movedView, int fromX, int toX, int fromY, int toY) {
        int xOffest = toX - fromX;
        int yOffest = toY - fromY;
        TranslateAnimation translateAnimation = new TranslateAnimation(-xOffest, 0, -yOffest, 0);
        translateAnimation.setDuration(400);
        movedView.startAnimation(translateAnimation);
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        if (mIsDragOver) {
            mOverBoundsView.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }

    @Override
    public void onDragExit(DragSource source, int x, int y, int xOffset,
            int yOffset, Object dragInfo) {
        if (mOverBoundsView != null) {
            mOverBoundsView.setBounds(new Rect());
            invalidate();
        }
    }

    @Override
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
    		int yOffset, Object dragInfo) {
        return mLauncher.isAllAppsVisible();
    }
    
    public void setDragView(CounterImageView dragview, int cellX) {
        mDragView = dragview;
        mMovePos = getDragPosBycellX(cellX);
    }
    
    public void open() {
        setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(0, 0, getHeight(), 0);
        anim.setDuration(ANIM_DURATION);
        startAnimation(anim);
    }

    public void close() {
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, getHeight());
        anim.setDuration(ANIM_DURATION);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
            	setVisibility(View.INVISIBLE);
            }
        });
        startAnimation(anim);
    }

    @Override
    public void onDragStart(View v, DragSource source, Object info,
    		int dragAction) {
    }

    @Override
    public void onDragEnd() {
        if (mDragView == null) {
            return;
        }

        ApplicationInfo fromInfo = (ApplicationInfo) mLauncher.getCurrentBottomButton();
        if ((fromInfo == null && !isFromMainMenu) || (!mLauncher.isAllAppsVisible() && mDragView.getTag() == null)) {
            mDragView.setBackgroundDrawable(null);
            mDragView.setVisibility(View.GONE);
            mDragView.setTag(null);
        }

        isFromMainMenu = false;
    
        for (int i = 0; i < BUTTON_COUNT; i++) {
            CounterImageView cv = (CounterImageView) getChildAt(i);
            if (cv.getTag() == null) {
                continue;
            }
            if (cv.getVisibility() == View.GONE) {
                cv.setTag(null);
                continue;
            }
            ApplicationInfo info = (ApplicationInfo) cv.getTag();
            info.cellX = i;
            LauncherModel.moveItemInDatabase(mLauncher, info, LauncherSettings.Favorites.CONTAINER_FAVORITEBAR,
				FAVORITEBAR_SCREEN, i, -1);
    	}
        mMovePos = -1;
        mLauncher.setCurrentBottomButton(null);
        mDragView = null;
        mLauncher.setExchangeState(false);
        mIsDragOver = false;
        verifyItemValidWidth();
    }

    private int getGoneViewCellx() {
        int result = -1;
        for (int i = 0; i < BUTTON_COUNT; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() == View.GONE) {
                result = i;
                break;
            }
        }
        return result;
    }

    private int getVisibityViewCount() {
        int result = 0;
        for (int i = 0; i < BUTTON_COUNT; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                result++;
            }
        }
        return result;
    }

    private int getDragPosBycellX(int cellX) {
        int result = 0;
        for (int i = 0; i < BUTTON_COUNT; i++) {
            if (i == cellX) {
                break;
            }
            View v = getChildAt(i);
            if (v.getVisibility() != View.GONE) {
                result++;
            }
        }
        return result;
    }

    private int getCellXbyPos(int pos) {
        int result = 0;
        int i = 0;
        for (; i < BUTTON_COUNT; i++) {
            View v = getChildAt(i);
            if (v.getVisibility() == View.GONE)
                continue;
            if (result == pos)
                break;
            result++;
        }
        if (i > 3 && result == 0) {
            i = 0;
        }
        return i;
    }

    public void verifyItemValidWidth() {
        int result = getVisibityViewCount();
        if (result != 0) {
            mItemValidWidth = (getMeasuredWidth() - mItemsValidLeft - getPaddingRight()) / result;
        } else {
            mItemValidWidth = getMeasuredWidth() - mItemsValidLeft - getPaddingRight();
        }
    }

    public boolean onTounchEvent(MotionEvent ev) {
        return super.onTouchEvent(ev);
    }
	
    public void updateBarForPackage(ArrayList<String> packageNames) {
        final int size = packageNames.size();
        if(size == 0) {
            return;
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            CounterImageView cv = (CounterImageView) getChildAt(i);
            ApplicationInfo app = (ApplicationInfo) cv.getTag();
            if (app == null) {
                continue;
            }

            final Intent intent = app.intent;
            final ComponentName name = intent.getComponent();
            if (app.itemType == BaseLauncherColumns.ITEM_TYPE_APPLICATION
                    && Intent.ACTION_MAIN.equals(intent.getAction())
                    && name != null) {
                boolean find = false;
                for (int k = 0; k < size; k++) {
                    String packageNameStr = packageNames.get(k);
                    if (packageNameStr!= null && packageNameStr.equals(name.getPackageName())) {
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    continue;
                }

                Drawable icon = Launcher.getModel().getApplicationInfoIcon(mLauncher.getPackageManager(), app, mLauncher);
                if (icon == null) {
                    final ResolveInfo resolveInfo = mLauncher.getPackageManager().resolveActivity(app.intent, 0);
                    if (resolveInfo != null) {
                        app.activityInfo = resolveInfo.activityInfo;
                        icon = Launcher.getModel().getIconForsdApps(mLauncher.getPackageManager(), app, mLauncher);
                    }
                }
                if (icon != null && icon != app.icon) {
                    app.icon = icon;
                    cv.setImageDrawable(icon);
                    cv.setTag(app);
                    cv.invalidate();
                }
            }
        }
    }
}
