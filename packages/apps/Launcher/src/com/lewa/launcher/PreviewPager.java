package com.lewa.launcher;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

public class PreviewPager extends ViewGroup {
    private int mTotalItems;
    private int mCurrentItem;
    private int mDotDrawableId;
    private boolean mIslongclick = false;
    private boolean mIsChanged = false;

    public PreviewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        initPager();
    }

    public PreviewPager(Context context) {
        super(context);
        initPager();
        // TODO Auto-generated constructor stub
    }

    private void initPager() {
        setFocusable(false);
        setWillNotDraw(false);
        mDotDrawableId=R.drawable.pager_dots_desktop;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTotalItems <= 0)
            return;
        createLayout();
    }

    private void updateLayout() {
        for (int i = 0; i < getChildCount(); i++) {
            final ImageView img = (ImageView) getChildAt(i);
            TransitionDrawable tmp = (TransitionDrawable) img.getDrawable();
            if (i == mCurrentItem) {
                tmp.startTransition(50);
            } else {
                tmp.resetTransition();
            }
        }
    }

    private void createLayout() {
        if(mTotalItems <= 1) {
            removeAllViews();
            postInvalidate();
            return;
        }
        detachAllViewsFromParent();
        int dotHeight = 0;
        int dotWidth = 0;
        int separation = 0;
        int marginLeft = 0;
        int marginTop;

        dotWidth = getResources().getDrawable(mDotDrawableId)
                .getIntrinsicWidth();
        dotHeight = getResources().getDrawable(mDotDrawableId)
        		.getIntrinsicHeight();
        if (mIslongclick) {
            separation = dotWidth / 2;
        } else {
            separation = dotWidth / 4;
        }
        marginLeft = ((getWidth()) / 2)
                - (((mTotalItems * dotWidth) / 2) + (((mTotalItems - 1) * separation) / 2));
        marginTop = ((getHeight()) / 2) - (dotHeight / 2);
        // Begin[pan] add
        if (marginLeft < 0) {
            marginLeft = 0;
        }
        // End
        for (int i = 0; i < mTotalItems; i++) {
            ImageView dot = new ImageView(getContext());
            TransitionDrawable td;
                td = (TransitionDrawable) getResources().getDrawable(
                        mDotDrawableId);
            td.setCrossFadeEnabled(true);
            dot.setImageDrawable(td);
            ViewGroup.LayoutParams p;
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT);
            dot.setLayoutParams(p);

            int childWidthSpec = 0;
            int childHeightSpec;

            childHeightSpec = getChildMeasureSpec(MeasureSpec.makeMeasureSpec(
            		dotHeight, MeasureSpec.UNSPECIFIED), 0, p.height);
            childWidthSpec = getChildMeasureSpec(
                    MeasureSpec.makeMeasureSpec(dotWidth, MeasureSpec.EXACTLY),
                    0, p.width);

            int left = marginLeft + (i * (dotWidth + separation));
            dot.measure(childWidthSpec, childHeightSpec);

            dot.layout(left, marginTop, left + dotWidth, marginTop + dotHeight);

            addViewInLayout(dot, getChildCount(), p, true);
            if (i == mCurrentItem) {
                TransitionDrawable tmp = (TransitionDrawable) dot.getDrawable();
                tmp.startTransition(200);
            }
        }
        postInvalidate();
    }

    protected int getTotalItems() {
        return mTotalItems;
    }

    protected void setTotalItems(int totalItems) {
        if (totalItems != mTotalItems || mIsChanged) {
            this.mTotalItems = totalItems;
            mIsChanged = false;
            createLayout();
        }
    }

    protected int getCurrentItem() {
        return mCurrentItem;
    }

    protected void setCurrentItem(int currentItem) {
        if (currentItem != mCurrentItem) {
            this.mCurrentItem = currentItem;
            updateLayout();
        }
    }

    protected void setLeft(int value) {
        int width = this.mRight - this.mLeft;
        this.mLeft = value;
        this.mRight = this.mLeft + width;
    }

    public void setOnLongClickState(boolean islongclick) {
        mIslongclick = islongclick;
        mIsChanged = true;
    }
}
