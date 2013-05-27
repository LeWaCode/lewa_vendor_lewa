package com.lewa.launcher;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class AllAppsSlidingView extends AdapterView<ApplicationsAdapter>
		implements OnItemClickListener, OnItemLongClickListener, DragSource,
		Drawer, DropTarget, View.OnClickListener {// implements DragScroller{
	static final String LOG_TAG = "AllAppsSlidingView";
	static final boolean LOGD = true;

	private static final int DEFAULT_SCREEN = 0;
	private static final int INVALID_SCREEN = -1;
	private static final int SNAP_VELOCITY = 120;// 1000

	private int mCurrentScreen;
	private int mTotalScreens;
	private int mCurrentHolder = 1;
	private int mPageWidth;
	private final int mDefaultScreen = DEFAULT_SCREEN;
	private int mNextScreen = INVALID_SCREEN;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private float mLastMotionX;
	private float mLastMotionY;

	static final int TOUCH_STATE_DOWN = 3;
	static final int TOUCH_STATE_TAP = 4;
	static final int TOUCH_STATE_DONE_WAITING = 5;

	private final static int TOUCH_STATE_REST = 0;
	private final static int TOUCH_STATE_SCROLLING = 1;
	private int mTouchState = TOUCH_STATE_REST;
	private int mTouchSlop;
	private int mMaximumVelocity;
	private Launcher mLauncher;
	private DragController mDragger;
	private boolean mFirstLayout = true;
	private ApplicationsAdapter mAdapter;
	private boolean mIsAutoArrange = false;

	/**
	 * Should be used by subclasses to listen to changes in the dataset
	 */
	AdapterDataSetObserver mDataSetObserver;
	public boolean mDataChanged;
	public int mItemCount;
	public int mOldItemCount;

	// Begin [pan 110815 4*4] modify
	private int mNumColumns = 4;
	private int mNumRows = 4;
	// End

	static final int LAYOUT_NORMAL = 0;
	static final int LAYOUT_SCROLLING = 1;
	private static final int MAX_SCREENS = 9;
	int mLayoutMode = LAYOUT_NORMAL;

	/**
	 * The drawable used to draw the selector
	 */
	Drawable mSelector;

	/**
	 * Defines the selector's location and dimension at drawing time
	 */
	Rect mSelectorRect = new Rect();
	/**
	 * The selection's left padding
	 */
	int mSelectionLeftPadding = 0;

	/**
	 * The selection's top padding
	 */
	int mSelectionTopPadding = 0;

	/**
	 * The selection's right padding
	 */
	int mSelectionRightPadding = 0;

	/**
	 * The selection's bottom padding
	 */
	int mSelectionBottomPadding = 0;
	/**
	 * The last CheckForLongPress runnable we posted, if any
	 */
	private CheckForLongPress mPendingCheckForLongPress;

	/**
	 * The last CheckForTap runnable we posted, if any
	 */
	private Runnable mPendingCheckForTap;

	/**
	 * The last CheckForKeyLongPress runnable we posted, if any
	 */
	private CheckForKeyLongPress mPendingCheckForKeyLongPress;
	private int mCheckTapPosition;
	private int mSelectedPosition = INVALID_POSITION;
	/**
	 * Acts upon click
	 */
	private AllAppsSlidingView.PerformClick mPerformClick;
	/**
	 * The data set used to store unused views that should be reused during the
	 * next layout to avoid creating new ones
	 */
	final RecycleBin mRecycler = new RecycleBin();
	// ADW:Hack the texture thing to make scrolling faster
	// private boolean forceOpaque=false;
	// private Bitmap mTexture;
	private Paint mPaint;
	private int mCacheColorHint = 0;
	private boolean mBlockLayouts;
	private int mScrollToScreen;
	// ADW: Animation variables
	private int mBgAlpha = 255;
	// Begin [pan] modify
	// ADW: speed for new scrolling transitions
	private int mScrollingSpeed = 100;// 600
	// ADW: bounce scroll
	private final int mScrollingBounce = 15;// 15
	// End

	// Begin [pan add]
	private int mItemsValidLeft = 0;
	private int mItemsValidTop = 0;
	private int mItemValidWidth = 0;
	private int mItemValidHeight = 0;
	private int mItemPressX = 0;
	private int mItemPressY = 0;
	public ItemInfo mItemInfo = null;
	private boolean mDragtoNextScreen = false;
	private int mNumPerScreen = mNumColumns * mNumRows;
	private boolean mReomvePage = false;
	private int mStartDragScreen = 0;
	private boolean mScrollerEnd = true;
	private boolean mLastScreen;
	private boolean mIsDroped = false;
	private boolean mScreenFull = false;
	private MySliderIndicator mMainMenuIndicator = null;
	// End

	private int mIconWidth = 0;
	private int mIconHeight = 0;

	private Handler objHandler = new Handler();
	private int mWhichScreentoSnap = 0;
	private boolean mIsStartedSnap = true;
	private boolean mIsAppFoldered = false;
	private Drawable mAppFolderIcon = null;
	private ArrayList<ApplicationInfo> mAllItems = ApplicationsAdapter.allItems;

	private int mPaddingTopSize;
	private int mPaddingLeftSize;

	private int mCreateAppFolderOffset = 0;
	private int mAppFolderOffsetMin = 0;
	private int mAppFolderOffsetMax = 0;

	private int Xstart;
	private int Ystart;
	private int Xend;
	private int Yend;
	private boolean mHandleNotMove;
	private boolean mIsFullFolder;

	public AllAppsSlidingView(Context context) {
		super(context);
		initWorkspace();
	}

	public AllAppsSlidingView(Context context, AttributeSet attrs) {
		this(context, attrs, com.android.internal.R.attr.absListViewStyle);
		initWorkspace();
	}

	public AllAppsSlidingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.AllAppsSlidingView, defStyle, 0);

		mPaddingTopSize = a
				.getDimensionPixelSize(
						R.styleable.AllAppsSlidingView_padding_topSize,
						mPaddingTopSize);
		mPaddingLeftSize = a.getDimensionPixelSize(
				R.styleable.AllAppsSlidingView_padding_leftSize,
				mPaddingLeftSize);
		a.recycle();
		initWorkspace();
	}

	@Override
	public boolean isOpaque() {
		if (mBgAlpha >= 255)
			return true;
		else
			return false;
	}

	private void initWorkspace() {
		setVerticalScrollBarEnabled(false);
		setHorizontalScrollBarEnabled(false);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setWillNotDraw(false);
		mScroller = new Scroller(getContext(), new DecelerateInterpolator(1.5f));//
		mCurrentScreen = mDefaultScreen;
		mScroller.forceFinished(true);
		mPaint = new Paint();
		mPaint.setDither(false);

		mMainMenuIndicator = new MySliderIndicator(getContext());

		final ViewConfiguration configuration = ViewConfiguration
				.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
	}

	@Override
	protected void onFinishInflate() {
		setOnItemClickListener(this);
		setOnItemLongClickListener(this);
	}

	@Override
	public void setLauncher(Launcher launcher) {
		mLauncher = launcher;
		Resources resources = mLauncher.getResources();
		mIconHeight = resources
				.getDimensionPixelSize(R.dimen.app_iconselect_width);
		mAppFolderIcon = resources.getDrawable(R.drawable.folder_board);
		mCreateAppFolderOffset = resources
				.getDimensionPixelSize(R.dimen.create_appfolder_offset);
		mAppFolderOffsetMin = resources
				.getDimensionPixelSize(R.dimen.appfolder_icon_offset_min);
		mAppFolderOffsetMax = resources
				.getDimensionPixelSize(R.dimen.appfolder_icon_offset_max);
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		mMainMenuIndicator.setLeft(l);
		if (mLayoutMode == LAYOUT_SCROLLING) {
			final int screenWidth = mPageWidth;
			final int whichScreen = (getScrollX() + (screenWidth / 2))
					/ screenWidth;
			if (whichScreen != mScrollToScreen) {
				if (mScrollToScreen != INVALID_POSITION) {
					addRemovePages(mScrollToScreen, whichScreen);
				}
				mScrollToScreen = whichScreen;
				// Begin [pan 110825] delete
				// mPager.setCurrentItem(whichScreen);
				// End
			}
		}
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			// Begin [pan 110825 for mainmenu indicator] add
			mMainMenuIndicator.indicate((float) mScroller.getCurrX()
					/ (float) (mTotalScreens * getWidth()));
			// End
			postInvalidate();
		} else if (mNextScreen != INVALID_SCREEN) {
			mNextScreen = INVALID_SCREEN;
			mLayoutMode = LAYOUT_NORMAL;
			findCurrentHolder();
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (mIsAppFoldered) {
			mAppFolderIcon.draw(canvas);
		}
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		// Begin [pan] modify
		// int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		// End

		// setMeasuredDimension(widthSize, heightSize);
		mPageWidth = widthSize;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (mFirstLayout) {
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			mMainMenuIndicator.measure(mPageWidth, mPaddingTopSize);
			mMainMenuIndicator.layout(0, 0, mPageWidth, mPaddingTopSize);
			mMainMenuIndicator.setPadding(0, 2, 0, 0);
			mMainMenuIndicator
					.setBackgroundResource(R.drawable.menuindicator_bg);
			mMainMenuIndicator.indicate(0);
			addViewInLayout(mMainMenuIndicator, getChildCount(), params);
			mFirstLayout = false;
		}
		if (!mBlockLayouts) {
			layoutChildren();
		}
		invalidate();
	}

	private void layoutChildren() {
		final RecycleBin recycleBin = mRecycler;
		final int count = getChildCount();
		int childCount = 0;
		for (int i = 0; i < count; i++) {
			if (getChildAt(i) instanceof HolderLayout) {
				final ViewGroup h = (ViewGroup) getChildAt(i);
				childCount = h.getChildCount();
				for (int j = 0; j < childCount; j++) {
					recycleBin.addScrapView(h.getChildAt(j));
				}
			}
		}
		// Begin [pan add]
		mItemsValidLeft = mPaddingLeftSize;
		mItemsValidTop = mPaddingTopSize;
		mItemValidWidth = (getMeasuredWidth() - mItemsValidLeft - mPaddingLeftSize)
				/ mNumColumns;
		mItemValidHeight = (getMeasuredHeight() - 3 * mItemsValidTop)
				/ mNumRows;
		// End
		detachViewsFromParent(1, getChildCount());
		makePage(mCurrentScreen - 1);
		makePage(mCurrentScreen);
		makePage(mCurrentScreen + 1);
		requestFocus();
		setFocusable(true);
		mDataChanged = false;
		mBlockLayouts = true;
		findCurrentHolder();
	}

	public void makePage(int pageNum) {
		if (pageNum < 0 || pageNum > mTotalScreens - 1) {//
			return;
		}
		final int pageSpacing = pageNum * mPageWidth;
		// Begin [pan] modify
		final int startPos = getFirstPositionInScreen(pageNum);// pageNum*mNumColumns*mNumRows;
		// End
		final int marginTop = getPaddingTop() + mPaddingTopSize;
		final int marginBottom = getPaddingBottom();
		final int marginLeft = getPaddingLeft() + mPaddingLeftSize;
		int marginRight = getPaddingRight() + mPaddingLeftSize;
		marginRight = marginRight == marginLeft ? marginRight : marginLeft;
		final int actualWidth = getMeasuredWidth() - marginLeft - marginRight;
		final int actualHeight = getMeasuredHeight() - marginTop - marginBottom
				- mPaddingTopSize * 3;
		final int columnWidth = (actualWidth) / mNumColumns;
		final int rowHeight = actualHeight / mNumRows;

		AllAppsSlidingView.LayoutParams p;
		p = new AllAppsSlidingView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
		int pos = startPos;
		int x = marginLeft;
		int y = marginTop;
		HolderLayout holder = new HolderLayout(getContext());
		final int count = mAdapter.getCount();

		for (int i = 0; i < mNumRows; i++) {
			for (int j = 0; j < mNumColumns; j++) {
				if (pos < count) {
					if (mAdapter.getItem(pos).screen != pageNum)
						break;
					View child;
					child = obtainView(pos);
					if(child == null){
						continue;
					}
					child.setLayoutParams(p);
					child.setSelected(false);
					child.setPressed(false);
					int childHeightSpec = getChildMeasureSpec(
							MeasureSpec.makeMeasureSpec(0,
									MeasureSpec.UNSPECIFIED), 0, p.height);
					int childWidthSpec = getChildMeasureSpec(
							MeasureSpec.makeMeasureSpec(columnWidth,
									MeasureSpec.EXACTLY), 0, p.width);
					child.measure(childWidthSpec, childHeightSpec);
					int left = x;
					int top = y;
					int w = columnWidth;
					int h = rowHeight;

					child.layout(left, top, left + w, top + h);
					holder.addViewInLayout(child, holder.getChildCount(), p,
							true);
					pos++;
					x += columnWidth;
				}
			}
			x = marginLeft;
			y += rowHeight;
		}
		AllAppsSlidingView.LayoutParams holderParams = new AllAppsSlidingView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);

		holder.layout(pageSpacing, mPaddingTopSize, pageSpacing + mPageWidth,
				getMeasuredHeight());

		holder.setTag(pageNum);
		addViewInLayout(holder, getChildCount(), holderParams, true);

	}

	// Begin [pan] add
	public void addNewPage(int pageNum) {
		final int pageSpacing = pageNum * mPageWidth;
		HolderLayout holder = new HolderLayout(getContext());
		AllAppsSlidingView.LayoutParams holderParams = new AllAppsSlidingView.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
		holder.layout(pageSpacing, mPaddingTopSize, pageSpacing + mPageWidth,
				getMeasuredHeight());

		holder.setTag(pageNum);

		addViewInLayout(holder, getChildCount(), holderParams, true);
		mTotalScreens = mTotalScreens + 1;
	}

	public void removePage() {// int pageNum
		// System.out.println("removePage mCurrentScreen="+mCurrentScreen+" mStartDragScreen="+mStartDragScreen+" mCurrentScreen="+mCurrentScreen+" mTotalScreens="+mTotalScreens);
		if ((mCurrentScreen > mStartDragScreen)) {
			snapToScreen(mCurrentScreen - 1);
			// Begin [pan] modify
			// mPager.setCurrentItem(0);
			mMainMenuIndicator.indicate(0);
			// End
		}

		HolderLayout h = null;
		h = (HolderLayout) getChildAt(1);
		detachViewFromParent(h);
		removeDetachedView(h, false);
		mTotalScreens = mTotalScreens - 1;

		// Begin [pan] add
		mMainMenuIndicator.setItems(mTotalScreens);
		if (mTotalScreens > 0)
			mMainMenuIndicator.indicate((float) mCurrentScreen
					/ (float) mTotalScreens);
		else
			mMainMenuIndicator.indicate(0);
		// End
	}

	// End

	private void addRemovePages(int current, int next) {
		int addPage;
		int removePage;
		if (current > next) {
			// Going left
			addPage = next - 1;
			removePage = current + 1;
		} else {
			// Going right
			addPage = next + 1;
			removePage = current - 1;
		}
		if (removePage >= 0 && removePage < mTotalScreens) {
			HolderLayout h = null;
			int count = getChildCount();
			for (int i = 1; i < count; i++) {
				if (getChildAt(i).getTag().equals(removePage)) {
					h = (HolderLayout) getChildAt(i);
					break;
				}
			}
			if (h != null) {
				count = h.getChildCount();
				for (int i = 0; i < count; i++) {
					mRecycler.addScrapView(h.getChildAt(i));
				}
				detachViewFromParent(h);
				removeDetachedView(h, false);
			}
		}
		makePage(addPage);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {

		/*
		 * This method JUST determines whether we want to intercept the motion.
		 * If we return true, onTouchEvent will be called and we do the actual
		 * scrolling there.
		 */

		/*
		 * Shortcut the most recurring case: the user is in the dragging state
		 * and he is moving his finger. We want to intercept this motion.
		 */
		final int action = ev.getAction();
		if ((action == MotionEvent.ACTION_MOVE)
				&& (mTouchState != TOUCH_STATE_REST)) {
			return true;
		}
             
		//if(mLauncher.mScrollDisable || !mScroller.isFinished()){
		//	return false;
		//}

		final float x = ev.getX();
		final float y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_MOVE:
			/*
			 * mIsBeingDragged == false, otherwise the shortcut would have
			 * caught it. Check whether the user has moved far enough from his
			 * original down touch.
			 */

			/*
			 * Locally do absolute value. mLastMotionX is set to the y value of
			 * the down event.
			 */
			final int xDiff = (int) Math.abs(x - mLastMotionX);
			final int yDiff = (int) Math.abs(y - mLastMotionY);

			final int touchSlop = mTouchSlop;
			boolean xMoved = xDiff > touchSlop;
			boolean yMoved = yDiff > touchSlop;

			if (xMoved || yMoved) {

				if (xMoved) {
					// Scroll if the user moved far enough along the X axis
					mTouchState = TOUCH_STATE_SCROLLING;
				}
			}
			break;

		case MotionEvent.ACTION_DOWN:
			// Remember location of down touch
			mLastMotionX = x;
			mLastMotionY = y;
			// Begin [pan] add
			mItemPressX = Math.round(x);
			mItemPressY = Math.round(y);
			// End
			/*
			 * If being flinged and user touches the screen, initiate drag;
			 * otherwise don't. mScroller.isFinished should be false when being
			 * flinged.
			 */
			mTouchState = mScroller.isFinished() ? TOUCH_STATE_DOWN
					: TOUCH_STATE_SCROLLING;
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			// Release the drag
			mTouchState = TOUCH_STATE_REST;
			break;
		}

		/*
		 * The only time we want to intercept motion events is if we are in the
		 * drag mode.
		 */
		return mTouchState != TOUCH_STATE_REST;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
	    if(mLauncher.isPopupShowing()) {
            mLauncher.clearPopupTip();
            return false;
        }
		if (mLauncher.getOpenFolder() != null
				&& !((AppFolder) mLauncher.getOpenFolder())
						.getAppInfoListState()) {
			mLauncher.closeFolder(mLauncher.getOpenFolder());
			return false;
		}
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(ev);

		final int action = ev.getAction();
		final float x = ev.getX();
		final float y = ev.getY();
		final View child;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			/*
			 * If being flinged and user touches, stop the fling. isFinished
			 * will be false if being flinged.
			 */
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			mTouchState = TOUCH_STATE_DOWN;
			child = pointToView((int) x, (int) y);
			if (child != null) {
				// FIXME Debounce
				if (mPendingCheckForTap == null) {
					mPendingCheckForTap = new CheckForTap();
				}
				postDelayed(mPendingCheckForTap,
						ViewConfiguration.getTapTimeout());
				// Remember where the motion event started
				mCheckTapPosition = getPositionForView(child);
			}
			// Remember where the motion event started
			mLastMotionX = x;
			break;
		case MotionEvent.ACTION_MOVE:

			if (mTouchState == TOUCH_STATE_SCROLLING
					|| mTouchState == TOUCH_STATE_DOWN) {
				// Scroll to follow the motion event
				final int deltaX = (int) (mLastMotionX - x);
				if (Math.abs(deltaX) > mTouchSlop
						|| mTouchState == TOUCH_STATE_SCROLLING) {

					mTouchState = TOUCH_STATE_SCROLLING;
					mLastMotionX = x;

					if (deltaX < 0) {
						if (getScrollX() > -mScrollingBounce) {
							scrollBy(Math.min(deltaX, mScrollingBounce), 0);
						}
					} else if (deltaX > 0) {
						final int availableToScroll = ((mTotalScreens) * mPageWidth)
								- getScrollX() - mPageWidth + mScrollingBounce;
						if (availableToScroll > 0) {
							scrollBy(deltaX, 0);
						}
					}
					// Begin [pan 110825 for mainmenu indicator] add
					mMainMenuIndicator.indicate((float) getScrollX()
							/ (float) (mTotalScreens * getWidth()));
					// End

				}
				final int deltaY = (int) (mLastMotionY - y);
				if (Math.abs(deltaY) > mTouchSlop
						|| mTouchState == TOUCH_STATE_SCROLLING) {
					mTouchState = TOUCH_STATE_SCROLLING;
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			// mTouchState = TOUCH_STATE_REST;
		case MotionEvent.ACTION_UP:
			if (mTouchState == TOUCH_STATE_SCROLLING) {
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);

				int velocityX = (int) velocityTracker.getXVelocity();
				if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
					// Fling hard enough to move left
					// snapToScreen(destinationScreen);
					snapToScreen(mCurrentScreen - 1);
				} else if (velocityX < -SNAP_VELOCITY
						&& mCurrentScreen < (mTotalScreens - 1)) {
					// Fling hard enough to move right
					// snapToScreen(destinationScreen);
					snapToScreen(mCurrentScreen + 1);
				} else {
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}

			} else {
				child = getViewAtPosition(mCheckTapPosition);
				if (child != null
						&& child.equals(pointToView((int) x, (int) y))) {
					if (mPerformClick == null) {
						mPerformClick = new PerformClick();
					}

					final AllAppsSlidingView.PerformClick performClick = mPerformClick;
					performClick.mChild = child;
					performClick.mClickMotionPosition = mCheckTapPosition;
					performClick.rememberWindowAttachCount();
					if (mTouchState == TOUCH_STATE_DOWN
							|| mTouchState == TOUCH_STATE_TAP) {
						final Handler handler = getHandler();
						if (handler != null) {
							handler.removeCallbacks(mTouchState == TOUCH_STATE_DOWN ? mPendingCheckForTap
									: mPendingCheckForLongPress);
						}
						mLayoutMode = LAYOUT_NORMAL;
						mTouchState = TOUCH_STATE_TAP;
						if (!mDataChanged) {
							if (mSelector != null) {
								Drawable d = mSelector.getCurrent();
								if (d != null
										&& d instanceof TransitionDrawable) {
									((TransitionDrawable) d).resetTransition();
								}
							}
							postDelayed(new Runnable() {
								@Override
								public void run() {
									child.setPressed(false);
									if (!mDataChanged) {
										post(performClick);
									}
									mTouchState = TOUCH_STATE_REST;
								}
							}, ViewConfiguration.getPressedStateDuration());
						}
						return true;
					} else {

					}
				} else {
					resurrectSelection();
				}

			}
			mTouchState = TOUCH_STATE_REST;
			mCheckTapPosition = INVALID_POSITION;
			hideSelector();
			invalidate();

			final Handler handler = getHandler();
			if (handler != null) {
				handler.removeCallbacks(mPendingCheckForLongPress);
			}
			break;
		}

		return true;
	}

	public void onTouchModeChanged(boolean isInTouchMode) {
		if (isInTouchMode) {
			// Get rid of the selection when we enter touch mode
			hideSelector();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return commonKey(keyCode, 1, event);
	}

	@Override
	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
		return commonKey(keyCode, repeatCount, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = commonKey(keyCode, 1, event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (isPressed() && mSelectedPosition >= 0 && mAdapter != null
					&& mSelectedPosition < mAdapter.getCount()) {
				final HolderLayout h = (HolderLayout) getChildAt(mCurrentHolder);
				final View view = h.getChildAt(mSelectedPosition);
				final int realPosition = getPositionForView(view);
				performItemClick(view, realPosition,
						mAdapter.getItemId(realPosition));
				setPressed(false);
				if (view != null)
					view.setPressed(false);
				return true;
			}
		}
		return handled;
	}

	private boolean commonKey(int keyCode, int count, KeyEvent event) {
		if (mAdapter == null) {
			return false;
		}

		if (mDataChanged) {
			layoutChildren();
		}

		boolean handled = false;
		int action = event.getAction();

		if (action != KeyEvent.ACTION_UP) {
			if (mSelectedPosition < 0) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_SPACE:
				case KeyEvent.KEYCODE_ENTER:
					resurrectSelection();
					return true;
				}
			}
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				handled = arrowScroll(FOCUS_LEFT);
				break;

			case KeyEvent.KEYCODE_DPAD_RIGHT:
				handled = arrowScroll(FOCUS_RIGHT);
				break;

			case KeyEvent.KEYCODE_DPAD_UP:
				handled = arrowScroll(FOCUS_UP);
				break;

			case KeyEvent.KEYCODE_DPAD_DOWN:
				handled = arrowScroll(FOCUS_DOWN);
				break;

			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER: {
				if (getChildCount() > 0 && event.getRepeatCount() == 0) {
					keyPressed();
				}
				return true;
			}

			}
		}

		if (handled) {
			return true;
		} else {
			switch (action) {
			case KeyEvent.ACTION_DOWN:
				return super.onKeyDown(keyCode, event);
			case KeyEvent.ACTION_UP:
				return super.onKeyUp(keyCode, event);
			case KeyEvent.ACTION_MULTIPLE:
				return super.onKeyMultiple(keyCode, count, event);
			default:
				return false;
			}
		}
	}

	/**
	 * Scrolls to the next or previous item, horizontally or vertically.
	 * 
	 * @param direction
	 *            either {@link View#FOCUS_LEFT}, {@link View#FOCUS_RIGHT},
	 *            {@link View#FOCUS_UP} or {@link View#FOCUS_DOWN}
	 * 
	 * @return whether selection was moved
	 */
	boolean arrowScroll(int direction) {
		final int selectedPosition = (mSelectedPosition == INVALID_POSITION) ? 0
				: mSelectedPosition;
		final int numColumns = mNumColumns;
		final int numRows = mNumRows;
		int rowPos;
		int colPos;

		boolean moved = false;
		final HolderLayout h = (HolderLayout) getChildAt(mCurrentHolder);

		colPos = (selectedPosition % numColumns);
		int lastColPos = mNumColumns;// (h.getChildCount()-1)%numColumns;
		rowPos = (selectedPosition / numColumns);
		int lastRowPos = mNumRows;// (h.getChildCount()-1)/numColumns;
		switch (direction) {
		case FOCUS_UP:
			if (rowPos > 0) {
				rowPos--;
				moved = true;
			}
			break;
		case FOCUS_DOWN:
			if (rowPos < numRows - 1 && rowPos < lastRowPos) {
				rowPos++;
				moved = true;
			}
			break;
		case FOCUS_LEFT:
			if (colPos > 0) {
				colPos--;
				moved = true;
			} else {
				if (mCurrentScreen > 0) {
					setSelection(INVALID_POSITION);
					snapToScreen(mCurrentScreen - 1);
					invalidate();
					return true;
				}
			}
			break;
		case FOCUS_RIGHT:
			if (colPos < numColumns - 1 && colPos < lastColPos) {
				colPos++;
				moved = true;
			} else {
				if (mCurrentScreen < mTotalScreens - 1) {
					setSelection(INVALID_POSITION);
					snapToScreen(mCurrentScreen + 1);
					invalidate();
					return true;
				}
			}
			break;
		}
		if (moved) {
			int pos = ((rowPos * numColumns) + (colPos));
			if (pos < h.getChildCount()) {
				playSoundEffect(SoundEffectConstants
						.getContantForFocusDirection(direction));
				setSelection(Math.max(0, pos));
				positionSelector(h.getChildAt(pos));
				invalidate();
			}
		}

		return moved;
	}

	/**
	 * Attempt to bring the selection back if the user is switching from touch
	 * to trackball mode
	 * 
	 * @return Whether selection was set to something.
	 */
	boolean resurrectSelection() {
		if (getChildCount() <= 0) {
			return false;
		}
		final HolderLayout h = (HolderLayout) getChildAt(mCurrentHolder);
		if (h != null && h instanceof HolderLayout) {
			final int childCount = h.getChildCount();

			if (childCount <= 0) {
				return false;
			}
			for (int i = 0; i < childCount; i++) {
				h.getChildAt(i).setPressed(false);
			}
			positionSelector(h.getChildAt(0));
			setSelection(0);
		}
		return true;
	}

	public View getViewAtPosition(int pos) {
		View v = null;
		int position = pos;
		int realScreen = mCurrentHolder;
		if (mCurrentScreen > 0) {
			// Begin [pan] modify
			position -= this.getFirstPositionInScreen(mCurrentScreen);
			// End
		}
		final ViewGroup h = (ViewGroup) getChildAt(realScreen);
		if (h != null) {
			if (h instanceof HolderLayout)
				v = h.getChildAt(position);
		}
		return v;
	}

	@Override
	public int getPositionForView(View view) {
		View listItem = view;
		int realScreen = mCurrentHolder;
		int pos = 0;
		if (mCurrentScreen > 0) {
			// Begin [pan] modify
			pos += this.getFirstPositionInScreen(mCurrentScreen);
			// End
		}
		final ViewGroup h = (ViewGroup) getChildAt(realScreen);
		final int count = h.getChildCount();
		for (int i = 0; i < count; i++) {
			if (h.getChildAt(i).equals(listItem)) {
				return (i + pos);
			}
		}
		// Child not found!
		return INVALID_POSITION;
	}

	public View pointToView(int x, int y) {
		if (getChildCount() > 1) {
			Rect frame = new Rect();
			int realScreen = mCurrentHolder;
			final ViewGroup h = (ViewGroup) getChildAt(realScreen);
			// ADW: fix possible nullPointerException when flinging too fast
			if (h != null) {
				final int count = h.getChildCount();
				for (int i = 0; i < count; i++) {
					final View child = h.getChildAt(i);
					if (child.getVisibility() == View.VISIBLE) {
						child.getHitRect(frame);
						if (frame.contains(x, y)) {
							return child;
						}
					}
				}
			}
		}
		return null;
	}

	private void snapToDestination() {
		final int screenWidth = mPageWidth;
		final int whichScreen = (getScrollX() + (screenWidth / 2))
				/ screenWidth;
		snapToScreen(whichScreen);
	}

	void snapToScreen(int whichScreen) {
		if (!mScroller.isFinished())
			return;

		// Begin [pan] add
		mScrollerEnd = false;
		// End
		whichScreen = Math.max(0, Math.min(whichScreen, mTotalScreens - 1));
		boolean changingScreens = whichScreen != mCurrentScreen;

		mNextScreen = whichScreen;
		//final int screenDelta = Math.abs(whichScreen - mCurrentScreen);
		mCurrentScreen = whichScreen;

		Scroller scroller = mScroller;
		int pageWidth = mPageWidth;

		if (changingScreens) {
			mLayoutMode = LAYOUT_SCROLLING;
		}
		View focusedChild = getFocusedChild();
		if (focusedChild != null && changingScreens
				&& focusedChild == getChildAt(mCurrentHolder)) {
			focusedChild.clearFocus();
		}

		int durationOffset = 1;
		// Faruq: Added to allow easing even when Screen doesn't changed (when
		// revert happens)
		/*if (screenDelta == 0) {
			durationOffset = 310;
		}*/
		final int duration = mScrollingSpeed + durationOffset;
		final int newX = whichScreen * pageWidth;
		final int delta = newX - getScrollX();
		scroller.startScroll(getScrollX(), 0, delta, 0, duration);
		invalidate();
		// Begin [pan] add
		mScrollerEnd = true;
		// End
	}

	@Override
	public ApplicationsAdapter getAdapter() {
		// TODO Auto-generated method stub
		return mAdapter;
	}

	@Override
	public void setAdapter(ApplicationsAdapter adapter) {
		// TODO Auto-generated method stub
		if (null != mAdapter) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		mRecycler.clear();
		mAdapter = adapter;

		if (mAdapter != null) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			mTotalScreens = getPageCount();
			// Begin [pan] modify
			// mPager.setTotalItems(mTotalScreens, true);
			if (mMainMenuIndicator != null) {
				mMainMenuIndicator.setItems(mTotalScreens);
				if (mTotalScreens > 0)
					mMainMenuIndicator
							.indicate((mCurrentScreen / mTotalScreens));
				else
					mMainMenuIndicator.indicate(0);
			}
			// End
			mDataChanged = true;

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());

		}
		mBlockLayouts = false;
		requestLayout();
	}

	void hideSelector() {
		if (mSelectedPosition != INVALID_POSITION) {
			setSelection(INVALID_POSITION);
			mSelectorRect.setEmpty();
		}
	}

	@Override
	public View getSelectedView() {
		final ViewGroup h = (ViewGroup) getChildAt(0);
		if (mItemCount > 0 && mSelectedPosition >= 0) {
			return h.getChildAt(mSelectedPosition);
		} else {
			return null;
		}

	}

	@Override
	public void setSelection(int position) {
		// TODO Auto-generated method stub
		mSelectedPosition = position;
		invalidate();
	}

	View obtainView(int position) {
		View scrapView;

		scrapView = mRecycler.getScrapView(position);

		View child;
		if (scrapView != null) {
			if(position < mAdapter.getCount())
				child = mAdapter.getView(position, scrapView, this);
			else 
				child = null;

			if (child != scrapView) {
				mRecycler.addScrapView(scrapView);
			}
		} else {
			if(position < mAdapter.getCount())
				child = mAdapter.getView(position, null, this);
			else 
				child = null;
		}
		return child;
	}

	public int getPageCount() {
		// Begin [pan] modify
		synchronized (mAllItems) {
			if (mAllItems == null) {
				return 0;
			}
	
			int count = mAllItems.size();
			if (count > 0) {
				if (mReomvePage) {
					return mTotalScreens;
				} else {
					int num = mAllItems.get(count - 1).screen + 1;
					if (num > MAX_SCREENS) {
						num = MAX_SCREENS;
					} else if (num <= 0) {
						LauncherModel.resetApplications(mAllItems, 0, MAX_SCREENS);
						mBlockLayouts = true;
						editUpdateState();
						num = mAllItems.get(count - 1).screen + 1;
					}
					return num;
				}
			}
			return 0;	
		}
		// End
	}

	// TODO:ADW Focus things :)
	/**
	 * @return True if the current touch mode requires that we draw the selector
	 *         in the pressed state.
	 */
	boolean touchModeDrawsInPressedState() {
		// FIXME use isPressed for this
		switch (mTouchState) {
		case TOUCH_STATE_TAP:
		case TOUCH_STATE_DONE_WAITING:
			return true;
		default:
			return false;
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mSelector != null) {
			mSelector.setState(getDrawableState());
		}
	}

	void positionSelector(View sel) {
		final Rect selectorRect = mSelectorRect;
		selectorRect.set(sel.getLeft(), sel.getTop() + mPaddingTopSize,
				sel.getRight(), sel.getBottom() + mPaddingTopSize);
		positionSelector(selectorRect.left, selectorRect.top,
				selectorRect.right, selectorRect.bottom);
		refreshDrawableState();
	}

	private void positionSelector(int l, int t, int r, int b) {

		int left = (r + l) / 2 - mIconHeight / 2;
		int top = t;
		int right = (r + l) / 2 + mIconHeight / 2;
		;
		int bottom = t + mIconHeight + getPaddingTop();

		mSelectorRect.set(left - mSelectionLeftPadding + getScrollX(), top
				- mSelectionTopPadding + getScrollY(), right
				+ mSelectionRightPadding + getScrollX(), bottom
				+ mSelectionBottomPadding + getScrollY());
	}

	/**
	 * Indicates whether this view is in a state where the selector should be
	 * drawn. This will happen if we have focus but are not in touch mode, or we
	 * are in the middle of displaying the pressed state for an item.
	 * 
	 * @return True if the selector should be shown
	 */
	boolean shouldShowSelector() {
		return (hasFocus() && !isInTouchMode())
				|| touchModeDrawsInPressedState();
	}

	/*private void drawSelector(Canvas canvas) {
		if (shouldShowSelector() && mSelectorRect != null
				&& !mSelectorRect.isEmpty()) {
			final Drawable selector = mSelector;
			selector.setBounds(mSelectorRect);
			selector.setState(getDrawableState());
			selector.draw(canvas);
		}
	}*/

	/**
	 * Returns the selector {@link android.graphics.drawable.Drawable} that is
	 * used to draw the selection in the list.
	 * 
	 * @return the drawable used to display the selector
	 */
	public Drawable getSelector() {
		return mSelector;
	}

	@Override
	public int getSolidColor() {
		return mCacheColorHint;
	}

	/**
	 * When set to a non-zero value, the cache color hint indicates that this
	 * list is always drawn on top of a solid, single-color, opaque background
	 * 
	 * @param color
	 *            The background color
	 */
	public void setCacheColorHint(int color) {
		mCacheColorHint = color;
	}

	/**
	 * When set to a non-zero value, the cache color hint indicates that this
	 * list is always drawn on top of a solid, single-color, opaque background
	 * 
	 * @return The cache color hint
	 */
	public int getCacheColorHint() {
		return mCacheColorHint;
	}

	/**
	 * Sets the recycler listener to be notified whenever a View is set aside in
	 * the recycler for later reuse. This listener can be used to free resources
	 * associated to the View.
	 * 
	 * @param listener
	 *            The recycler listener to be notified of views set aside in the
	 *            recycler.
	 * 
	 * @see android.widget.AbsListView.RecycleBin
	 * @see android.widget.AbsListView.RecyclerListener
	 */
	public void setRecyclerListener(RecyclerListener listener) {
		mRecycler.mRecyclerListener = listener;
	}

	/**
	 * A RecyclerListener is used to receive a notification whenever a View is
	 * placed inside the RecycleBin's scrap heap. This listener is used to free
	 * resources associated to Views placed in the RecycleBin.
	 * 
	 * @see android.widget.AbsListView.RecycleBin
	 * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
	 */
	public static interface RecyclerListener {
		/**
		 * Indicates that the specified View was moved into the recycler's scrap
		 * heap. The view is not displayed on screen any more and any expensive
		 * resource associated with the view should be discarded.
		 * 
		 * @param view
		 */
		void onMovedToScrapHeap(View view);
	}

	/**
	 * The RecycleBin facilitates reuse of views across layouts. The RecycleBin
	 * has two levels of storage: ActiveViews and ScrapViews. ActiveViews are
	 * those views which were onscreen at the start of a layout. By
	 * construction, they are displaying current information. At the end of
	 * layout, all views in ActiveViews are demoted to ScrapViews. ScrapViews
	 * are old views that could potentially be used by the adapter to avoid
	 * allocating views unnecessarily.
	 * 
	 * @see android.widget.AbsListView#setRecyclerListener(android.widget.AbsListView.RecyclerListener)
	 * @see android.widget.AbsListView.RecyclerListener
	 */
	class RecycleBin {
		private RecyclerListener mRecyclerListener;

		/**
		 * The position of the first view stored in mActiveViews.
		 */
		private int mFirstActivePosition;

		/**
		 * Views that were on screen at the start of layout. This array is
		 * populated at the start of layout, and at the end of layout all view
		 * in mActiveViews are moved to mScrapViews. Views in mActiveViews
		 * represent a contiguous range of Views, with position of the first
		 * view store in mFirstActivePosition.
		 */
		private View[] mActiveViews = new View[0];

		/**
		 * Unsorted views that can be used by the adapter as a convert view.
		 */
		private ArrayList<View>[] mScrapViews;

		private int mViewTypeCount;

		private ArrayList<View> mCurrentScrap;

		@SuppressWarnings("unchecked")
		public void setViewTypeCount(int viewTypeCount) {
			if (viewTypeCount < 1) {
				throw new IllegalArgumentException(
						"Can't have a viewTypeCount < 1");
			}
			// noinspection unchecked
			ArrayList<View>[] scrapViews = new ArrayList[viewTypeCount];
			for (int i = 0; i < viewTypeCount; i++) {
				scrapViews[i] = new ArrayList<View>();
			}
			mViewTypeCount = viewTypeCount;
			mCurrentScrap = scrapViews[0];
			mScrapViews = scrapViews;
		}

		public boolean shouldRecycleViewType(int viewType) {
			return viewType >= 0;
		}

		/**
		 * Clears the scrap heap.
		 */
		void clear() {
			if (mViewTypeCount == 1) {
				final ArrayList<View> scrap = mCurrentScrap;
				final int scrapCount = scrap.size();
				for (int i = 0; i < scrapCount; i++) {
					removeDetachedView(scrap.remove(scrapCount - 1 - i), false);
				}
			} else {
				final int typeCount = mViewTypeCount;
				for (int i = 0; i < typeCount; i++) {
					final ArrayList<View> scrap = mScrapViews[i];
					final int scrapCount = scrap.size();
					for (int j = 0; j < scrapCount; j++) {
						removeDetachedView(scrap.remove(scrapCount - 1 - j),
								false);
					}
				}
			}
		}

		/**
		 * Fill ActiveViews with all of the children of the AbsListView.
		 * 
		 * @param childCount
		 *            The minimum number of views mActiveViews should hold
		 * @param firstActivePosition
		 *            The position of the first view that will be stored in
		 *            mActiveViews
		 */
		void fillActiveViews(int childCount, int firstActivePosition) {
			if (mActiveViews.length < childCount) {
				mActiveViews = new View[childCount];
			}
			mFirstActivePosition = firstActivePosition;

			final View[] activeViews = mActiveViews;
			for (int i = 0; i < childCount; i++) {
				View child = getChildAt(i);
				AllAppsSlidingView.LayoutParams lp = (AllAppsSlidingView.LayoutParams) child
						.getLayoutParams();
				// Don't put header or footer views into the scrap heap
				if (lp != null
						&& lp.viewType != AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
					// Note: We do place AdapterView.ITEM_VIEW_TYPE_IGNORE in
					// active views.
					// However, we will NOT place them into scrap views.
					activeViews[i] = child;
				}
			}
			for (int i = 0; i < activeViews.length; i++) {
				// Log.d("MyRecycler","We have recycled activeview "+i);
				// Log.d("MyRecycler","So whe we call it will be "+(i-mFirstActivePosition));
			}
		}

		/**
		 * Get the view corresponding to the specified position. The view will
		 * be removed from mActiveViews if it is found.
		 * 
		 * @param position
		 *            The position to look up in mActiveViews
		 * @return The view if it is found, null otherwise
		 */
		View getActiveView(int position) {
			int index = position - mFirstActivePosition;
			final View[] activeViews = mActiveViews;
			// Log.d("MyRecycler","We're recovering view "+index+" of a list of "+activeViews.length);
			if (index >= 0 && index < activeViews.length) {
				final View match = activeViews[index];
				activeViews[index] = null;
				return match;
			}
			return null;
		}

		/**
		 * @return A view from the ScrapViews collection. These are unordered.
		 */
		View getScrapView(int position) {
			ArrayList<View> scrapViews;
			if (mViewTypeCount == 1) {
				scrapViews = mCurrentScrap;
				int size = scrapViews.size();
				if (size > 0) {
					return scrapViews.remove(size - 1);
				} else {
					return null;
				}
			} else {
				int whichScrap = mAdapter.getItemViewType(position);
				if (whichScrap >= 0 && whichScrap < mScrapViews.length) {
					scrapViews = mScrapViews[whichScrap];
					int size = scrapViews.size();
					if (size > 0) {
						return scrapViews.remove(size - 1);
					}
				}
			}
			return null;
		}

		/**
		 * Put a view into the ScapViews list. These views are unordered.
		 * 
		 * @param scrap
		 *            The view to add
		 */
		void addScrapView(View scrap) {
			AllAppsSlidingView.LayoutParams lp = (AllAppsSlidingView.LayoutParams) scrap
					.getLayoutParams();
			if (lp == null) {
				return;
			}

			// Don't put header or footer views or views that should be ignored
			// into the scrap heap
			int viewType = lp.viewType;
			if (!shouldRecycleViewType(viewType)) {
				return;
			}

			if (mViewTypeCount == 1) {
				mCurrentScrap.add(scrap);
			} else {
				mScrapViews[viewType].add(scrap);
			}

			if (mRecyclerListener != null) {
				mRecyclerListener.onMovedToScrapHeap(scrap);
			}
		}

		/**
		 * Move all views remaining in mActiveViews to mScrapViews.
		 */
		void scrapActiveViews() {
			final View[] activeViews = mActiveViews;
			final boolean hasListener = mRecyclerListener != null;
			final boolean multipleScraps = mViewTypeCount > 1;

			ArrayList<View> scrapViews = mCurrentScrap;
			final int count = activeViews.length;
			for (int i = 0; i < count; ++i) {
				final View victim = activeViews[i];
				if (victim != null) {
					int whichScrap = ((AllAppsSlidingView.LayoutParams) victim
							.getLayoutParams()).viewType;

					activeViews[i] = null;

					if (whichScrap == AdapterView.ITEM_VIEW_TYPE_IGNORE) {
						// Do not move views that should be ignored
						continue;
					}

					if (multipleScraps) {
						scrapViews = mScrapViews[whichScrap];
					}
					scrapViews.add(victim);

					if (hasListener) {
						mRecyclerListener.onMovedToScrapHeap(victim);
					}

				}
			}

			pruneScrapViews();
		}

		/**
		 * Makes sure that the size of mScrapViews does not exceed the size of
		 * mActiveViews. (This can happen if an adapter does not recycle its
		 * views).
		 */
		private void pruneScrapViews() {
			final int maxViews = mActiveViews.length;
			final int viewTypeCount = mViewTypeCount;
			final ArrayList<View>[] scrapViews = mScrapViews;
			for (int i = 0; i < viewTypeCount; ++i) {
				final ArrayList<View> scrapPile = scrapViews[i];
				int size = scrapPile.size();
				final int extras = size - maxViews;
				size--;
				for (int j = 0; j < extras; j++) {
					removeDetachedView(scrapPile.remove(size--), false);
				}
			}
		}

		/**
		 * Puts all views in the scrap heap into the supplied list.
		 */
		void reclaimScrapViews(List<View> views) {
			if (mViewTypeCount == 1) {
				views.addAll(mCurrentScrap);
			} else {
				final int viewTypeCount = mViewTypeCount;
				final ArrayList<View>[] scrapViews = mScrapViews;
				for (int i = 0; i < viewTypeCount; ++i) {
					final ArrayList<View> scrapPile = scrapViews[i];
					views.addAll(scrapPile);
				}
			}
		}
	}

	// TODO:ADW Helper classes
	final class CheckForTap implements Runnable {
		@Override
		public void run() {
			if (mTouchState == TOUCH_STATE_DOWN) {
				mTouchState = TOUCH_STATE_TAP;
				final View child = getViewAtPosition(mCheckTapPosition);
				if (child != null && !child.hasFocusable()) {
					mLayoutMode = LAYOUT_NORMAL;

					if (!mDataChanged) {
						child.setPressed(true);
						setPressed(true);
						setSelection(mCheckTapPosition);
						positionSelector(child);
						final int longPressTimeout = ViewConfiguration
								.getLongPressTimeout();
						final boolean longClickable = isLongClickable();

						if (mSelector != null) {
							Drawable d = mSelector.getCurrent();
							if (d != null && d instanceof TransitionDrawable) {
								if (longClickable) {
									((TransitionDrawable) d)
											.startTransition(longPressTimeout);
								} else {
									((TransitionDrawable) d).resetTransition();
								}
							}
						}

						if (longClickable) {
							if (mPendingCheckForLongPress == null) {
								mPendingCheckForLongPress = new CheckForLongPress();
							}
							mPendingCheckForLongPress
									.rememberWindowAttachCount();
							postDelayed(mPendingCheckForLongPress,
									longPressTimeout);
						} else {
							mTouchState = TOUCH_STATE_DONE_WAITING;
						}
					} else {
						mTouchState = TOUCH_STATE_DONE_WAITING;
					}
				}
			}
		}
	}

	/**
	 * Sets the selector state to "pressed" and posts a CheckForKeyLongPress to
	 * see if this is a long press.
	 */
	void keyPressed() {
		Drawable selector = mSelector;
		Rect selectorRect = mSelectorRect;
		if (selector != null && (isFocused() || touchModeDrawsInPressedState())
				&& selectorRect != null && !selectorRect.isEmpty()) {

			final View v = getViewAtPosition(mSelectedPosition);

			if (v != null) {
				if (v.hasFocusable())
					return;
				v.setPressed(true);
			}
			setPressed(true);

			final boolean longClickable = isLongClickable();
			Drawable d = selector.getCurrent();
			if (d != null && d instanceof TransitionDrawable) {
				if (longClickable) {
					((TransitionDrawable) d).startTransition(ViewConfiguration
							.getLongPressTimeout());
				} else {
					((TransitionDrawable) d).resetTransition();
				}
			}
			if (longClickable && !mDataChanged) {
				if (mPendingCheckForKeyLongPress == null) {
					mPendingCheckForKeyLongPress = new CheckForKeyLongPress();
				}
				mPendingCheckForKeyLongPress.rememberWindowAttachCount();
				postDelayed(mPendingCheckForKeyLongPress,
						ViewConfiguration.getLongPressTimeout());
			}
		}
	}

	/**
	 * A base class for Runnables that will check that their view is still
	 * attached to the original window as when the Runnable was created.
	 * 
	 */
	private class WindowRunnnable {
		private int mOriginalAttachCount;

		public void rememberWindowAttachCount() {
			mOriginalAttachCount = getWindowAttachCount();
		}

		public boolean sameWindow() {
			return hasWindowFocus()
					&& getWindowAttachCount() == mOriginalAttachCount;
		}
	}

	private class PerformClick extends WindowRunnnable implements Runnable {
		View mChild;
		int mClickMotionPosition;

		@Override
		public void run() {
			// The data has changed since we posted this action in the event
			// queue,
			// bail out before bad things happen
			if (mDataChanged)
				return;
			final int realPosition = mClickMotionPosition;
			if (realPosition == INVALID_POSITION)
				return;
			if (mAdapter != null && realPosition < mAdapter.getCount()
					&& sameWindow()) {
				performItemClick(mChild, realPosition,
						mAdapter.getItemId(realPosition));
				setSelection(INVALID_POSITION);
			}
		}
	}

	private class CheckForLongPress extends WindowRunnnable implements Runnable {
		@Override
		public void run() {
			final int motionPosition = mCheckTapPosition;
			final View child = getViewAtPosition(motionPosition);
			if (child != null && mAdapter != null) {
				final int longPressPosition = motionPosition;
				final long longPressId = mAdapter.getItemId(motionPosition);

				boolean handled = false;
				if (sameWindow() && !mDataChanged) {
					handled = performLongPress(child, longPressPosition,
							longPressId);
				}
				if (handled) {
					mTouchState = TOUCH_STATE_REST;
					child.setPressed(false);
				} else {
					mTouchState = TOUCH_STATE_DONE_WAITING;
				}

			}
		}
	}

	private class CheckForKeyLongPress extends WindowRunnnable implements
			Runnable {
		@Override
		public void run() {
			if (isPressed() && mCheckTapPosition >= 0) {
				int index = mCheckTapPosition;
				View v = getChildAt(index);

				if (!mDataChanged) {
					boolean handled = false;
					if (sameWindow()) {
						handled = performLongPress(v, mCheckTapPosition,
								mCheckTapPosition);
					}
					if (handled) {
						v.setPressed(false);
					}
				} else {
					v.setPressed(false);
					if (v != null)
						v.setPressed(false);
				}
			}
		}
	}

	private boolean performLongPress(final View child,
			final int longPressPosition, final long longPressId) {
		boolean handled = false;

		if (getOnItemLongClickListener() != null) {
			handled = getOnItemLongClickListener().onItemLongClick(
					AllAppsSlidingView.this, child, longPressPosition,
					longPressId);
		}
		if (handled) {
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}
		return handled;
	}

	/**
	 * AbsListView extends LayoutParams to provide a place to hold the view
	 * type.
	 */
	public class LayoutParams extends AdapterView.LayoutParams {
		/**
		 * View type for this view, as returned by
		 * {@link android.widget.Adapter#getItemViewType(int) }
		 */
		int viewType;

		/**
		 * When this boolean is set, the view has been added to the AbsListView
		 * at least once. It is used to know whether headers/footers have
		 * already been added to the list view and whether they should be
		 * treated as recycled views or not.
		 */
		boolean recycledHeaderFooter;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(int w, int h, int viewType) {
			super(w, h);
			this.viewType = viewType;
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(
			ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new AllAppsSlidingView.LayoutParams(getContext(), attrs);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof AllAppsSlidingView.LayoutParams;
	}

	// TODO:ADW DATA HANDLING
	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		@Override
		public void onChanged() {
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = getAdapter().getCount();
			mTotalScreens = getPageCount();
			// Begin [pan] modify
			// mPager.setTotalItems(mTotalScreens, true);
			mMainMenuIndicator.setItems(mTotalScreens);
			if (mTotalScreens > 0)
				mMainMenuIndicator.indicate((float) mCurrentScreen
						/ (float) mTotalScreens);
			else
				mMainMenuIndicator.indicate(0);
			// End
			if (mTotalScreens - 1 < mCurrentScreen) {
				scrollTo(0, 0);
				mCurrentScreen = 0;
				mCurrentHolder = 1;
				// Begin [pan] modify
				// mPager.setCurrentItem(0);
				mMainMenuIndicator.indicate(0);
				// End
				mBlockLayouts = false;
				mScrollToScreen = 0;
				mLayoutMode = LAYOUT_NORMAL;
			}
			// Detect the case where a cursor that was previously invalidated
			// has
			// been repopulated with new data.
			if (AllAppsSlidingView.this.getAdapter().hasStableIds()
					&& mInstanceState != null && mOldItemCount == 0
					&& mItemCount > 0) {
				AllAppsSlidingView.this.onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			}
			mBlockLayouts = false;
			requestLayout();

		}

		@Override
		public void onInvalidated() {
			mDataChanged = true;

			if (AllAppsSlidingView.this.getAdapter().hasStableIds()) {
				// Remember the current state for the case where our hosting
				// activity is being
				// stopped and later restarted
				mInstanceState = AllAppsSlidingView.this.onSaveInstanceState();
			}

			// Data is invalid so we should reset our state
			mOldItemCount = mItemCount;
			mItemCount = 0;
			mSelectedPosition = INVALID_POSITION;
		}

		public void clearSavedState() {
			mInstanceState = null;
		}
	}

	// TODO: ADW Events

	@Override
	public void onItemClick(AdapterView<?> adapter, View v, int position,
			long id) {
		// TODO Auto-generated method stub	    
		ApplicationInfo app = (ApplicationInfo) getItemAtPosition(position);
		if (app.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
			mLauncher.openFolder((FolderInfo) app);
			return;
		}
		mLauncher.startActivitySafely(app.intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		// TODO Auto-generated method stub

		if (!v.isInTouchMode()) {
			return false;
		}
		// Begin [pan] modify
		if (mLauncher.isAllAppsVisible() && mScrollerEnd) {
			Object app;
			Object temp_object = parent.getItemAtPosition(position);

			if (temp_object instanceof UserFolderInfo) {
				app = new UserFolderInfo((UserFolderInfo) temp_object);
			} else {
				app = new ApplicationInfo((ApplicationInfo) temp_object);
			}
			mItemInfo = (ItemInfo) parent.getItemAtPosition(position);
			mStartDragScreen = mItemInfo.screen;
			// mReomvePage = (getAppCountInScreen(mStartDragScreen) <= 1)
			// && (getAppCountInScreen(mStartDragScreen - 1) < mNumPerScreen);
			// mReomvePage = (getAppCountInScreen(mStartDragScreen) <= 1);
			if (mItemInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
				mReomvePage = false;
			}
			mIconWidth = v.getWidth();
			mHandleNotMove = false;
			mDragger.startDrag(v, this, app, DragController.DRAG_ACTION_MOVE);
			objHandler.postDelayed(mMoveTask, 500);
			Xstart = Ystart = Xend = Yend = 0;
			mAppFolderIcon = mLauncher.getResources().getDrawable(
					R.drawable.folder_board);
		}
		// End
		return true;
	}

	@Override
	public void onDropCompleted(View target, boolean success) {
		// TODO Auto-generated method stub
		// Begin [pan] add

		objHandler.removeCallbacks(mMoveTask);
		setIsFullFolder(false);

		if (LOGD)
			Log.d(LOG_TAG, "onDropCompleted = " + success);
		if (!success) {
			return;
		}
		synchronized (mAllItems) {
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			if (mLauncher.getExchangeState()) {
				// exchange between favorite bar with main menu
				mDragger.setExchangeState(true);
				ApplicationInfo appMenu = new ApplicationInfo(
						(ApplicationInfo) mItemInfo);
	
				int screen = mItemInfo.screen;
				int position = mItemInfo.cellX + getFirstPositionInScreen(screen);
				ItemInfo info = mLauncher.getCurrentBottomButton();
				if (position < allItems.size() && allItems.get(position) != null) {
					allItems.remove(position);
				}
				if (info != null) {
					ApplicationInfo appAction = (ApplicationInfo) info;
					appAction.screen = appMenu.screen;
					appAction.cellX = appMenu.cellX;
					appAction.cellY = appMenu.cellY;
					appAction.container = LauncherSettings.Favorites.CONTAINER_MAINMENU;
					if(position < allItems.size()) {
						allItems.add(position, appAction);
					} else {
						allItems.add(appAction);
					}
	
					mBlockLayouts = true;
					// LauncherModel.resetApplications(allItems, mCurrentScreen,
					// mTotalScreens);
					LauncherModel.resetApplications(allItems, 0, mTotalScreens);
					mAdapter.updateDataSet();
					LauncherModel.moveItemInDatabase(mLauncher, appAction,
							LauncherSettings.Favorites.CONTAINER_MAINMENU,
							appAction.screen, appAction.cellX, 0);
					if (mReomvePage) {
						removePage();
					}
				} else {
					// LauncherModel.resetApplications(allItems, mStartDragScreen,
					// mTotalScreens);
					LauncherModel.resetApplications(allItems, 0, mTotalScreens);
					if (mReomvePage) {// && mCurrentScreen == mStartDragScreen
						removePage();// mStartDragScreen
						if (mCurrentScreen >= mTotalScreens) {
							snapToScreen(mCurrentScreen - 1);
						}
					}
					if (getAppCountInScreen(mCurrentScreen) == 0
							&& mStartDragScreen != mCurrentScreen) {
						// removePage();
						snapToScreen(mCurrentScreen - 1);
						// Begin [pan] modify
						// mPager.setCurrentItem(mCurrentScreen);
						if (mTotalScreens > 0)
							mMainMenuIndicator.indicate((float) mCurrentScreen
									/ (float) mTotalScreens);
						else
							mMainMenuIndicator.indicate(0);
						// End
					}
					mBlockLayouts = true;
					mAdapter.updateDataSet();
					editUpdateState();
				}
				mLauncher.setExchangeState(false);
				mLauncher.setCurrentBottomButton(null);
			} else if (mLauncher.getAppFolderState() != null) {
				ApplicationInfo itemInfo = mLauncher.getAppFolderState();
				allItems.remove(itemInfo.cellX
						+ getFirstPositionInScreen(itemInfo.screen));
				LauncherModel.resetApplications(allItems, 0, mTotalScreens);
				mBlockLayouts = true;
				mAdapter.updateDataSet();
				mLauncher.setAppFolderState(null);
				mLastScreen = false;
				mScreenFull = false;
				mDragtoNextScreen = false;
				editUpdateState();
			} else {
				if (mIsDroped) {
					mIsDroped = false;
				} else {
					if (getAppCountInScreen(mCurrentScreen) == 0 || mReomvePage) {
						removePage();
						LauncherModel.resetApplications(allItems, 0, mTotalScreens);
						mBlockLayouts = true;
						mAdapter.updateDataSet();
					}
				}
				mLastScreen = false;
				mScreenFull = false;
				mDragtoNextScreen = false;
				editUpdateState();
			}
		}
		// End
	}

	// Begin [pan] add
	public class updateDatabaseRunnable implements Runnable {
		boolean mForceStop = false;
		boolean mFinished = true;

		public updateDatabaseRunnable() {

		}

		public void stop() {
			mForceStop = true;
			while (!mFinished) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void run() {
			if (LOGD)
				Log.d(LOG_TAG, "updateDatabase begin");
			synchronized (mAllItems) {
				mFinished = false;
				mForceStop = false;
				final ArrayList<ApplicationInfo> allItems = mAllItems;
				ApplicationInfo app;
				if (allItems == null || allItems.size() == 0) {
					return;
				}
				final int size = allItems.size();
				for (int i = size - 1; (i >= 0) && !mForceStop; --i) {
					app = allItems.get(i);
					synchronized (app){
						if (app == null)
							return;
						if (app.isChanged) {
								app.isChanged = false;
								LauncherModel.moveItemInDatabase(mLauncher, app, app.container,
										app.screen, app.cellX, app.cellY);
						}
					}
				}
				// LauncherModel.updateApplicationInfoInDatabase(mLauncher,
				// mAdapter.allItems);
				mForceStop = true;
				mFinished = true;
			}
		}
	}

	public updateDatabaseRunnable mUpdateAppRunnable = new updateDatabaseRunnable();

	// End
	@Override
	public void setDragger(DragController dragger) {
		// TODO Auto-generated method stub
		mDragger = dragger;

	}

	public int getNumColumns() {
		return mNumColumns;
	}

	public int getNumRows() {
		return mNumRows;
	}

	/**
	 * ADW: find the current child page
	 */
	private void findCurrentHolder() {
		final int count = getChildCount();
		for (int i = 1; i < count; i++) {
			if (getChildAt(i).getTag().equals(mCurrentScreen)) {
				mCurrentHolder = i;
				break;
			}
		}
	}

	// Begin [pan add]
	@Override
	public int getTotalScreens() {
		return mTotalScreens;
	}

	@Override
	public int getCurrentScreen() {
		return mCurrentScreen;
	}

	public ArrayList<ApplicationInfo> getAllItems() {
		return mAllItems;
	}

	public boolean getIsRemovePage() {
		return mReomvePage;
	}

	public void setIsRemovePage(boolean isRemovePage) {
		mReomvePage = isRemovePage;
	}

	public boolean getIsDragToNextPage() {
		return mDragtoNextScreen;
	}

	public int getStartDragScreen() {
		return mStartDragScreen;
	}

	@Override
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub
		// Begin [pan] add
		synchronized (mAllItems) {
		
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			if (mLauncher.getExchangeState()) {
				ApplicationInfo info = (ApplicationInfo) dragInfo;
				int pos = (x - mItemsValidLeft) / mItemValidWidth
						+ (y - mItemsValidTop) / mItemValidHeight * mNumColumns;//
				int count = getAppCountInScreen(mCurrentScreen);
				if (pos >= count) {
					mLauncher.setCurrentBottomButton(null);
					pos = count;
					info.container = LauncherSettings.Favorites.CONTAINER_MAINMENU;
					info.cellX = pos;
					info.screen = mCurrentScreen;
					info.cellY = 0;
					pos = pos + getFirstPositionInScreen(mCurrentScreen);
					allItems.add(pos, info);
					LauncherModel.moveItemInDatabase(mLauncher, info,
							LauncherSettings.Favorites.CONTAINER_MAINMENU,
							mCurrentScreen, info.cellX, info.cellY);
				} else {
					if (mIsAppFoldered) {
						mLauncher.setCurrentBottomButton(null);
						createAppFolder(pos, dragInfo);
						mIsAppFoldered = false;
					}
				}
				mBlockLayouts = true;
				mAdapter.updateDataSet();
				verifyPressPos(pos % mNumColumns, pos / mNumColumns);
				mLauncher.setExchangeState(false);
			} else {
				if (!mIsStartedSnap) {
					objHandler.removeCallbacks(mTasks);
					mIsStartedSnap = true;
					return;
				}
				mIsDroped = true;
				if (mScreenFull) {
					return;
				}
				if (mIsAppFoldered) {
					int pos = (x - mItemsValidLeft) / mItemValidWidth
							+ (y - mItemsValidTop) / mItemValidHeight * mNumColumns;//
					createAppFolder(pos, dragInfo);
					mIsAppFoldered = false;
					if (mReomvePage) {
						removePage();
					}
					return;
				}
				// begin zhaolei add
				if (source instanceof AppFolder) {
					AppFolder fromAppFolder = (AppFolder) source;
					fromAppFolder.removeFromAdapter((ApplicationInfo) dragInfo);
					if (fromAppFolder.countInAppFolder() == 0) {
						allItems.remove(fromAppFolder.mInfo);
						LauncherModel.deleteItemFromDatabase(mLauncher,
								fromAppFolder.mInfo);
					}
					int cellX = (x - mItemsValidLeft) / mItemValidWidth;
					int cellY = (y - mItemsValidTop) / mItemValidHeight;
					// int pos = cellX + cellY * mNumColumns;
					leaveAppFolder(cellX, cellY, (ApplicationInfo) dragInfo);
				}
				// end
				if (mDragtoNextScreen && !mReomvePage) {
					moveAppPosition((ItemInfo) dragInfo, -1);
				} else if (mDragtoNextScreen && mReomvePage) {
					moveAppPosition((ItemInfo) dragInfo, -1);
					if (mCurrentScreen != mStartDragScreen)
						removePage();// mStartDragScreen
				} else if (!mDragtoNextScreen && mReomvePage) {
					// mStartDragScreen = mCurrentScreen;
					if (mStartDragScreen == mCurrentScreen) {
						return;
					}
					int toDragScreen = mCurrentScreen;
					if (mStartDragScreen > mCurrentScreen) {
						int temp = toDragScreen;
						toDragScreen = mStartDragScreen;
						mStartDragScreen = temp;
					}
					// LauncherModel.resetApplications(allItems, mStartDragScreen,
					// mTotalScreens);
					LauncherModel.resetApplications(allItems, 0, mTotalScreens);
					mBlockLayouts = true;
					mAdapter.updateDataSet();
					// if (mCurrentScreen > mStartDragScreen)
					removePage();// mStartDragScreen
				}
			}
		}
		// End
	}

	private void createAppFolder(int pos, Object dragInfo) {
		ApplicationInfo appFrom = (ApplicationInfo) dragInfo;
		if (pos == appFrom.cellX && !(mLauncher.getExchangeState())) {
			return;
		}

		int startPosForFromPage = getFirstPositionInScreen(appFrom.screen);
		int startPosForToPage = getFirstPositionInScreen(mCurrentScreen);

		pos = startPosForToPage + pos;
		synchronized (mAllItems) {
			
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			final int size = allItems.size();
	
			if (allItems == null || size == 0)
				return;
	
			if (pos >= size)
				return;
			ApplicationInfo infoTo = allItems.get(pos);
	
			if (appFrom.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
				return;
			} else {
				UserFolderInfo folderInfo = new UserFolderInfo();
				folderInfo.title = mLauncher.getString(R.string.folder);
	
				String str = (new Date()).toString();
				folderInfo.setActivity(new ComponentName(str, str), 0);
				folderInfo.icon = null;
				folderInfo.screen = infoTo.screen;
				folderInfo.cellX = infoTo.cellX;
				folderInfo.cellY = infoTo.cellY;
	
				folderInfo.filtered = false;
				folderInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER;
				folderInfo.isUserInstalled = true;
	
				folderInfo.add(appFrom);
				folderInfo.add(infoTo);
				allItems.remove(pos);
				allItems.add(pos, folderInfo);
				if (!mLauncher.getExchangeState())
					allItems.remove(appFrom.cellX + startPosForFromPage);
	
				LauncherModel.addItemToDatabase(mLauncher, folderInfo,
						LauncherSettings.Favorites.CONTAINER_MAINMENU,
						folderInfo.screen, folderInfo.cellX, folderInfo.cellY,
						false);
	
				// LauncherModel.changeItemInDatabaseByContainer(mLauncher, appFrom,
				// folderInfo.id);
				// LauncherModel.changeItemInDatabaseByContainer(mLauncher, infoTo,
				// folderInfo.id);
				LauncherModel.moveItemInDatabase(mLauncher, appFrom, folderInfo.id,
						folderInfo.screen, 0, folderInfo.cellY);
				LauncherModel.moveItemInDatabase(mLauncher, infoTo, folderInfo.id,
						folderInfo.screen, 1, folderInfo.cellY);
			}
	
			LauncherModel.resetApplications(allItems, 0, mTotalScreens);
			mBlockLayouts = true;
			mAdapter.updateDataSet();
			verifyPressPos(pos % mNumColumns, pos / mNumColumns);
		}
	}

	private void leaveAppFolder(int cellX, int cellY, ApplicationInfo info) {
		synchronized (mAllItems) {
			int appCountInToPage = getAppCountInScreen(mCurrentScreen);
			int toPos = cellX + cellY * mNumColumns;
			if (toPos >= appCountInToPage) {
				toPos = appCountInToPage;
			}

			int startPosForToPage = getFirstPositionInScreen(mCurrentScreen);
			toPos = startPosForToPage + toPos;
			info.container = LauncherSettings.Favorites.CONTAINER_MAINMENU;
			info.screen = mCurrentScreen;
			info.cellX = cellX;
			info.cellY = cellY;
			mAllItems.add(toPos, info);
			// LauncherModel.changeItemInDatabaseByContainer(mLauncher, info,
			// info.container);
			LauncherModel.moveItemInDatabase(mLauncher, info, info.container,
					info.screen, info.cellX, info.cellY);
			LauncherModel.resetApplications(mAllItems, 0, mTotalScreens);
			mBlockLayouts = true;
			mAdapter.updateDataSet();
		}
	}

	@Override
	public void onDragEnter(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub
		if (mLauncher.getExchangeState()) {
			int pos = (x - mItemsValidLeft) / mItemValidWidth
					+ (y - mItemsValidTop) / mItemValidHeight * mNumColumns;

			mItemPressX = (mPageWidth / 4) * pos + mPageWidth / 8;
			mItemPressY = mPageWidth / 8;
		}

	}

	@Override
	public void onDragOver(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {

		mAppFolderIcon.setBounds(new Rect());
		invalidate();

		if (source instanceof AppFolder) {
			return;
		}

		if (mLauncher.getExchangeState()) {
			preCreateAppFolder(x, y, -1, -1);
			invalidate();
			return;
		}

		int dragPos = ((ItemInfo) dragInfo).cellX;
		int dragScerrn = ((ItemInfo) dragInfo).screen;

		int offset = mCreateAppFolderOffset;
		if (x - xOffset <= -offset) {
			// snap to last screen
			if (!mIsStartedSnap) {
				return;
			}
			if (mCurrentScreen > 0) {
				mIsStartedSnap = false;
				mWhichScreentoSnap = mCurrentScreen - 1;
				objHandler.postDelayed(mTasks, 800);
				mDragtoNextScreen = true;
				return;
			} else {
				Log.e(LOG_TAG, "mCurrentScreen <= 0");
				return;
			}

		} else if (x + mIconWidth - xOffset >= getMeasuredWidth() + offset) {
			// snap to next screen
			if (!mIsStartedSnap) {
				return;
			}
			if ((mCurrentScreen >= MAX_SCREENS - 1)) {
				if (!mLastScreen) {
					android.widget.Toast.makeText(mContext,
							R.string.lewa_mainmenu, 800).show();
					mLastScreen = true;
				}
				return;
			}
			if (mCurrentScreen < mTotalScreens - 1) {
				mIsStartedSnap = false;
				mWhichScreentoSnap = mCurrentScreen + 1;
				objHandler.postDelayed(mTasks, 800);
				mDragtoNextScreen = true;
				return;
			} else if (mCurrentScreen == mTotalScreens - 1 && !mIsAutoArrange) {
				// auto add a new Page
				if (getAppCountInScreen(mCurrentScreen) < 2) {
					return;
				}
				mIsStartedSnap = false;
				mWhichScreentoSnap = mCurrentScreen + 1;
				objHandler.postDelayed(mTasks, 800);
				mDragtoNextScreen = true;
				return;
			}
		}

		if (mDragtoNextScreen && (mTotalScreens == MAX_SCREENS)
				&& (getAppCountInScreen(mTotalScreens - 1) == mNumPerScreen)) {
			boolean isAll = true;
			for (int i = mCurrentScreen; i < MAX_SCREENS; i++) {
				if (getAppCountInScreen(i) < mNumPerScreen) {
					isAll = false;
					break;
				}
			}
			if (isAll) {
				mScreenFull = true;
				return;
			}
		}

		objHandler.removeCallbacks(mTasks);

		if (Xstart == 0 && Ystart == 0) {
			Xstart = mItemPressX;
			Ystart = mItemPressY;
		}
		Xend = x;
		Yend = y;

		// int offset = mCreateAppFolderOffset;
		if ((x < mItemPressX) || (y < mItemPressY))
			offset = -offset;

		int pos = dragPos + ((x - mItemPressX) + offset) / mItemValidWidth
				+ ((y - mItemPressY) + offset) / mItemValidHeight * mNumColumns;

		int realPos = (x - mItemsValidLeft) / mItemValidWidth
				+ (y - mItemsValidTop) / mItemValidHeight * mNumColumns;

		if ((pos < 0) || ((dragPos == pos) && (!mDragtoNextScreen))) {
			return;
		}

		int appCountInToPage = getAppCountInScreen(mCurrentScreen);

		int posFrom = dragPos + getFirstPositionInScreen(dragScerrn);
		int posTo = realPos + getFirstPositionInScreen(mCurrentScreen);

		if (posFrom == posTo)
			return;

		if (((Math.abs(x - mItemPressX) <= mItemValidWidth / 2) && (Math.abs(y
				- mItemPressY) <= mItemValidHeight / 2))
				&& !mDragtoNextScreen) {
			// invalid pos
			return;
		}

		// objHandler.postDelayed(mMoveTask, 800);
		mIsStartedSnap = true;
		if (mHandleNotMove) {
			if (pos < appCountInToPage) {
				if (preCreateAppFolder(x, y, posTo, posFrom)) {
					return;
				}
			} else {
				if (!mIsAutoArrange) {
					//realPos = appCountInToPage;
				}
			}
			moveAppPosition((ItemInfo) dragInfo, realPos);
			// if(!mIsAutoArrange)
			//mDragtoNextScreen = false;
			// mHandleNotMove = false;
			objHandler.removeCallbacks(mMoveTask);
			objHandler.postDelayed(mMoveTask, 350);
		}
		mAppFolderIcon.setBounds(new Rect());
	}

	@Override
	public void onDragExit(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub
		// Begin [pan] add
		// startMoveTask();
		if (LOGD)
			Log.d(LOG_TAG, "onDragExit, x:" + x + ", y:" + y);
		if (mAppFolderIcon != null)
			mAppFolderIcon.setBounds(new Rect());
		if (source instanceof AppFolder) {
			return;
		}
		if (!mLauncher.getExchangeState()) {
			mItemInfo.assignFrom((ItemInfo) dragInfo);
		}
		if (getAppCountInScreen(mCurrentScreen) == 0) {
			onDrop(source, x, y, xOffset, yOffset, dragInfo);
		}
		// End
	}

	@Override
	public boolean acceptDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		// TODO Auto-generated method stub
		return true;
	}

	private boolean preCreateAppFolder(int x, int y, int posTo, int posFrom) {

		if (posTo == -1) {
			posTo = (x - mItemsValidLeft) / mItemValidWidth
					+ (y - mItemsValidTop) / mItemValidHeight * mNumColumns;
			posTo = posTo + getFirstPositionInScreen(mCurrentScreen);
		}

		View toView = getViewAtPosition(posTo);

		if (toView == null)
			return false;

		if (mIsFullFolder) {
			mAppFolderIcon = mLauncher.getResources().getDrawable(
					R.drawable.folder_full);
		} else {
			mAppFolderIcon = mLauncher.getResources().getDrawable(
					R.drawable.folder_board);
		}

		mIsFullFolder = false;

		boolean isdragFolder = true;
		synchronized (mAllItems) {
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			if (posFrom != -1 && posFrom <= allItems.size() - 1) {
				ApplicationInfo fromInfo = allItems.get(posFrom);
				if (fromInfo.itemType == LauncherSettings.Favorites.ITEM_TYPE_APP_FOLDER) {
					isdragFolder = false;
				}
			}
		}

		if (isdragFolder) {

			if ((Math.abs(x - mItemPressX) % mItemValidWidth <= mAppFolderOffsetMin)
					|| (Math.abs(x - mItemPressX) % mItemValidWidth >= mAppFolderOffsetMax)) {
				mIsAppFoldered = true;

				int left = toView.getLeft();
				int top = toView.getTop();
				int right = toView.getRight();
				int bottom = toView.getBottom();

				mAppFolderIcon.setBounds(left + getScrollX(), top
						+ getScrollY(), right + getScrollX(), bottom
						+ getScrollY());
				return true;
			}
			mAppFolderIcon.setBounds(new Rect());
			mIsAppFoldered = false;
		}
		return false;// mIsAppFoldered;

	}

	// Begin [pan] add
	public int getFirstPositionInScreen(int screen) {
		synchronized (mAllItems) {
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			ApplicationInfo info;
			int count = allItems.size();
			for (int i = 0; i < count; ++i) {
				info = allItems.get(i);
				if (info.screen == screen)
					return i;
			}
			return count;
		}
	}

	public int getAppCountInScreen(int screen) {
		synchronized (mAllItems) {
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			ApplicationInfo info;
			int count = allItems.size();
			int i = 0;
			int result = 0;
			for (; i < count; ++i) {
				info = allItems.get(i);
				if (info.screen == screen)
					break;
			}
			for (; i < count; ++i) {
				info = allItems.get(i);
				if (info.screen == screen)
					++result;
				else
					break;
			}
			return result;
		}
	}

	public int getLastPositionInScreen(int screen) {
		synchronized (mAllItems) {
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			ApplicationInfo info;
			int count = allItems.size();
			if (count == 0)
				return 0;
			for (int i = count - 1; i >= 0; --i) {
				info = allItems.get(i);
				if (info.screen == screen)
					return i;
			}
			return count;
		}
	}

	private void moveAppPosition(ItemInfo dragInfo, int toPos) {
		synchronized (mAllItems) {
			final ArrayList<ApplicationInfo> allItems = mAllItems;
			int dragFromScreen = dragInfo.screen;
			int dragToScreen = mCurrentScreen;
			int dragFromPos = dragInfo.cellX;
			int startPosForFromPage = getFirstPositionInScreen(dragFromScreen);
			int startPosForToPage = getFirstPositionInScreen(dragToScreen);
			int appCountInToPage = getAppCountInScreen(dragToScreen);
	
			if (toPos >= appCountInToPage) {
				if (!mDragtoNextScreen) {
					toPos = appCountInToPage - 1;
				} else {
					return;
				}
			}
			if (toPos < 0) {
				mReomvePage = false;
				if (appCountInToPage == mNumPerScreen) {
					return;
				}
				toPos = appCountInToPage;
			}
			if (mReomvePage && mDragtoNextScreen
					&& (mStartDragScreen > mCurrentScreen)) {
				for (int i = mCurrentScreen; i < mStartDragScreen; i++) {
					if (getAppCountInScreen(i) < mNumPerScreen) {
						mReomvePage = true;
						break;
					} else {
						mReomvePage = false;
					}
				}
	
			}
			if (startPosForToPage >= allItems.size()
					&& mCurrentScreen < mTotalScreens - 1 && dragToScreen > 0) {
				startPosForToPage = getFirstPositionInScreen(dragToScreen - 1)
						+ getAppCountInScreen(dragToScreen - 1);
			}
			if (dragFromPos + startPosForFromPage >= allItems.size()) {
				return;
			}
			ApplicationInfo appFrom = allItems.get(dragFromPos
					+ startPosForFromPage);
			// appFrom.cellX = toPos;
			appFrom.screen = dragToScreen;
			appFrom.cellX = toPos;
			appFrom.isChanged = true;
	
			dragInfo.cellX = toPos;
			dragInfo.screen = dragToScreen;
	
			allItems.remove(startPosForFromPage + dragFromPos);
			if (mDragtoNextScreen) {
				if (dragFromScreen >= dragToScreen) {
					int temp = dragFromScreen;
					dragFromScreen = dragToScreen;
					dragToScreen = temp;
				} else {
					dragToScreen = mTotalScreens;
					toPos = toPos - 1;
				}
			}
			if (startPosForToPage + toPos > allItems.size()) {
				allItems.add(allItems.size(), appFrom);
			} else {
				allItems.add(startPosForToPage + toPos, appFrom);
			}
	
			if (mReomvePage) {
				if (dragToScreen == mStartDragScreen) {
					dragToScreen = dragToScreen - 1;
				} else if (dragFromScreen == mStartDragScreen) {
					dragFromScreen = dragFromScreen + 1;
				} else if (dragFromScreen < mStartDragScreen
						&& mStartDragScreen < dragToScreen) {
					// LauncherModel.resetApplications(allItems, dragFromScreen,
					// dragFromScreen);// mStartDragScreen - 1
					LauncherModel.resetApplications(allItems, 0, mTotalScreens);
					dragFromScreen = mStartDragScreen + 1;
				}
			}
	
			// LauncherModel.resetApplications(allItems, dragFromScreen,
			// dragToScreen);
			LauncherModel.resetApplications(allItems, 0, mTotalScreens);
			mBlockLayouts = true;
			mAdapter.updateDataSet();
			verifyPressPos(toPos % mNumColumns, toPos / mNumColumns);
	
			// TranslateAnimation
			ArrayList<View> movedList = new ArrayList<View>();
			int translatePos = toPos + startPosForToPage;
			int movedSize = 0;
			if (!mDragtoNextScreen) {
				movedSize = Math.abs(toPos - dragFromPos) + 1;
				for (int i = movedSize; i > 0; i--) {
					View movedView = getViewAtPosition(translatePos);
					movedList.add(movedView);
					if (toPos > dragFromPos)
						translatePos--;
					else
						translatePos++;
				}
			} else {
				movedSize = Math.abs(getAppCountInScreen(dragInfo.screen) - toPos) + 1;
				for (int i = movedSize; i > 0; i--) {
					View movedView = getViewAtPosition(translatePos);
					movedList.add(movedView);
					translatePos++;
				}
			}
			
			mDragtoNextScreen = false;
			mScreenFull = false;
			View targetView = null;
			View translateView = null;
			for (int i = 0; i < movedSize - 1; i++) {
				translateView = movedList.get(i);
				targetView = movedList.get(i + 1);
				if (translateView == null || targetView == null)
					return;
				if (translateView.getTag() == dragInfo) {
					continue;
				}
				final int[] translateLocation = new int[2];
				final int[] targetLocation = new int[2];
				translateView.getLocationOnScreen(translateLocation);
				targetView.getLocationOnScreen(targetLocation);
				playSingleViewAnimation(translateView, targetView,
						translateLocation, targetLocation);
			}
		}
		
		// objHandler.removeCallbacks(mMoveTask);
	}

	public void playSingleViewAnimation(View movedView, View targView,
			int[] fromViewCoordinate, int[] toViewCoordinate) {
		int xOffest = toViewCoordinate[0] - fromViewCoordinate[0];
		int yOffest = toViewCoordinate[1] - fromViewCoordinate[1];
		TranslateAnimation translateAnimation = new TranslateAnimation(
				-xOffest, 0, -yOffest, 0);
		translateAnimation.setDuration(300);
		movedView.layout(targView.getLeft(), targView.getTop(),
				targView.getRight(), targView.getBottom());
		movedView.startAnimation(translateAnimation);
	}

	private void verifyPressPos(int gridX, int gridY) {
		int minX = mItemsValidLeft + gridX * mItemValidWidth;
		int maxX = minX + mItemValidWidth;
		int minY = mItemsValidTop + gridY * mItemValidHeight;
		int maxY = minY + mItemValidHeight;

		while (mItemPressX < minX)
			mItemPressX += mItemValidWidth;
		while (mItemPressX > maxX)
			mItemPressX -= mItemValidWidth;
		while (mItemPressY < minY)
			mItemPressY += mItemValidHeight;
		while (mItemPressY > maxY)
			mItemPressY -= mItemValidHeight;
	}

	@Override
	public int getNumberPerScreen() {
		// TODO Auto-generated method stub
		return mNumPerScreen;
	}

	public void editUpdateState() {
		if (mLauncher.isAllAppsVisible()) {
			mUpdateAppRunnable.stop();
			// mLauncher.runOnUiThread(mUpdateAppRunnable);
			Thread thread = new Thread(mUpdateAppRunnable, "Upadte apps");
			thread.start();
		}
	}

	private Runnable mTasks = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (mDragtoNextScreen
					&& getAppCountInScreen(mWhichScreentoSnap) == 0) {
				addNewPage(mWhichScreentoSnap);
			}
			mIsStartedSnap = true;
			snapToScreen(mWhichScreentoSnap);
		}

	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mLauncher.getOpenFolder() != null) {
			if (!((AppFolder) mLauncher.getOpenFolder()).getAppInfoListState()) {
				mLauncher.closeFolder(mLauncher.getOpenFolder());
			}
		}
	}

	private Runnable mMoveTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			if ((Math.abs(Xend - Xstart)) < 10 && (Math.abs(Yend - Ystart) < 10)) {
				mHandleNotMove = true;
			} else {
				mHandleNotMove = false;

			}

			objHandler.postDelayed(mMoveTask, 100);
			Xstart = Xend;
			Ystart = Yend;
		}
	};

	public void stopMoveTask() {
		objHandler.removeCallbacks(mMoveTask);
	}

	public void setIsAppFolder(boolean isFolder) {
		mIsAppFoldered = isFolder;
	}

	public boolean getIsAppFolder() {
		return mIsAppFoldered;
	}

	public Drawable getAppFolderBg() {
		return mAppFolderIcon;
	}

	public void setAppFolderBg(Drawable folderBg) {
		mAppFolderIcon = folderBg;
	}

	public void setIsFullFolder(boolean isFull) {
		mIsFullFolder = isFull;
	}

	public void updateBarForPackage(ArrayList<String> packageNames) {
		synchronized (mAllItems) {
			final int size = mAllItems.size();
			if (mAllItems == null || mAdapter == null || size == 0 || mLauncher == null) {
				return;
			}
			final int count = packageNames.size();
			for (int i = 0; i < size; i++) {
				View view = obtainView(i);
				if(view == null) {
					return;
				}
				boolean find = false;
				find = mLauncher.getWorkspace().updateIcons(view,
						packageNames, true);
				
				if (count <= 0) {
					mBlockLayouts = true;
					mAdapter.updateDataSet();
					return;
				}
				if(find){
					mBlockLayouts = true;
					mAdapter.updateDataSet();
				}
				
			}
		}
		//mBlockLayouts = true;
		//mAdapter.updateDataSet();
	}

	// End
	@Override
	public void autoArrange() {
		// TODO Auto-generated method stub
		synchronized (mAllItems) {
			mIsAutoArrange = AlmostNexusSettingsHelper.getAutoArrange(mLauncher);
			if (mIsAutoArrange && mAllItems != null && mAllItems.size() > 0
					&& mTotalScreens > 0 && mAdapter != null) {
				LauncherModel.resetApplications(mAllItems, 0, mTotalScreens);
				mBlockLayouts = true;
				mAdapter.updateDataSet();
				mUpdateAppRunnable.stop();
				Thread thread = new Thread(mUpdateAppRunnable, "Upadte apps");
				thread.start();
			}
		}
	}

	@Override
	public void setScrollSpeed(boolean changed) {
		// TODO Auto-generated method stub
		if(!changed){
			mScrollingSpeed = 250;
			mScroller = new Scroller(mContext, new DecelerateInterpolator(1.5f));//
		} else {
			mScrollingSpeed = 100;
			mScroller = new Scroller(mContext, new OvershootInterpolator(1.0f));//
		}
	}
}
