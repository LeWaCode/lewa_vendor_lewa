package com.lewa.launcher;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

public class CounterTextView extends TextView {
    // ADW custom notifier counters
    public Drawable mBackGround = null;

    public Drawable mShadow = null;
    public Drawable mSelectedBackGround = null;
    public boolean mIsInDesktop = false;
    public boolean mIsInFolder = false;
    public boolean mIsInList = false;
    private boolean mBackgroundSizeChanged;

    public int mSelectedBgPaddingX;
    public int mSelectedBgPaddingTop;
    public int mSelectedBgPaddingBottom;
    public int mShadowSize;

    private int mIconWidht;
    private int mIconTop;

    // End
    public int mBgPaddingTop = 0;
    public Drawable mIconTopDrawable;

    public CounterTextView(Context context) {
        super(context);
        init(context);
    }

    public CounterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CounterTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mSelectedBackGround = IconHighlights.getDrawable(getContext(),
                IconHighlights.TYPE_DESKTOP);
        setBackgroundDrawable(null);
        mSelectedBackGround.setCallback(this);
        Resources resources = context.getResources();
        mSelectedBgPaddingX = resources
                .getDimensionPixelSize(R.dimen.textview_padding_x);
        mSelectedBgPaddingTop = resources
                .getDimensionPixelSize(R.dimen.textview_padding_top);
        mSelectedBgPaddingBottom = resources
                .getDimensionPixelSize(R.dimen.textview_padding_bottom);

        mIconWidht = (int) getResources().getDimension(
                R.dimen.app_lewaicon_size);
        mIconTop = (int) getResources().getDimension(R.dimen.app_iconbg_top)
                + (int) getResources().getDimension(R.dimen.app_icon_top);

        mShadowSize = resources.getDimensionPixelSize(R.dimen.textview_shadow);
    }

    @Override
    protected boolean setFrame(int left, int top, int right, int bottom) {
        if (mLeft != left || mRight != right || mTop != top
                || mBottom != bottom) {
            mBackgroundSizeChanged = true;
        }
        return super.setFrame(left, top, right, bottom);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mSelectedBackGround || super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        Drawable d = mSelectedBackGround;
        if (d != null && d.isStateful()) {
            d.setState(getDrawableState());
        }
        super.drawableStateChanged();
    }

    @Override
    public void draw(Canvas canvas) {
        final Drawable selectGround = mSelectedBackGround;
        final Drawable backGround = mBackGround;
        final Drawable topDrawable = mIconTopDrawable;

        final int scrollX = mScrollX;
        final int scrollY = mScrollY;

        if (selectGround != null) {

            if (mBackgroundSizeChanged) {
                selectGround.setBounds(0, 0, mRight - mLeft, mBottom - mTop);
                mBackgroundSizeChanged = false;
            }

            if ((scrollX | scrollY) == 0) {
                selectGround.draw(canvas);
            } else {
                canvas.translate(scrollX, scrollY);
                selectGround.draw(canvas);
                canvas.translate(-scrollX, -scrollY);
            }
        }
        if (mIsInDesktop && AlmostNexusSettingsHelper.getIconShadow(mContext)) {
            canvas.save();
            final int iconWidth = mIconWidht;
            int mIconLeft = (getWidth() - iconWidth) / 2;
            int shadowLeft = mIconLeft + mShadowSize;
            int shadowTop = mIconTop + mShadowSize;
            int shadowRight = mIconLeft + iconWidth - mShadowSize;
            int shadowBottom = mIconTop + iconWidth;
            int shadowColor = Color.argb(80, 0, 0, 0);
            Paint paint = new Paint();
            paint.setShadowLayer(mShadowSize, 0, mShadowSize / 2, shadowColor);
            paint.setAlpha(0);
            RectF rectF = new RectF(shadowLeft, shadowTop, shadowRight,
                    shadowBottom);
            canvas.translate(scrollX, scrollY);
            canvas.drawRoundRect(rectF, 10f, 10f, paint);
            canvas.restore();
        }
        translateDrawable(backGround, canvas, scrollX, scrollY);
        // End
        super.draw(canvas);

        translateDrawable(topDrawable, canvas, scrollX, scrollY);
    }

    private Rect getBoundsRect(Drawable d) {
        int left = (getWidth() - d.getIntrinsicWidth()) / 2;
        int top = mBgPaddingTop;
        int right = left + d.getIntrinsicWidth();
        int bottom = top + d.getIntrinsicHeight();
        return new Rect(left, top, right, bottom);
    }

    private void translateDrawable(Drawable drawable, Canvas canvas, int scrollX, int scrollY) {
        Rect rect = null;
        if (drawable != null) {
            canvas.save();
            rect = getBoundsRect(drawable);
            drawable.setBounds(rect);
            if (Launcher.mIsNeedChanged) {
                if ((scrollX | scrollY) == 0) {
                    canvas.translate(rect.left, mBgPaddingTop);
                }else {
                    canvas.translate(scrollX + rect.left, scrollY
                            + mBgPaddingTop);
                }
            } else {
                if ((scrollX | scrollY) != 0) {
                    canvas.translate(scrollX, scrollY);
                }
            }
            drawable.draw(canvas);
            canvas.restore();
        }
    }
 }
