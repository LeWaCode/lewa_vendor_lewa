package com.lewa.player.ui.view;

import com.lewa.player.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ListItemView extends ViewGroup{
    
    private final Context mContext;
    
 //   private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mPaddingTop;
    private final int mPaddingRight;
    private final int mPaddingBottom;
    private final int mPaddingLeft;
    private final int mGapBetweenTiTleAndArtist;
    private final int mline1RightPaddingTop;

    private final int mHeaderTextWidth;
    private final int mHeaderPaddingLeft;
    private final int mLeftImageHeight;
    
    private boolean mHeaderVisible;
    private TextView mHeaderTextView;
    private Drawable mHeaderBackgroundDrawable;
    private int mHeaderBackgroundHeight;
    
    TextView line1Left;
    TextView line1Right;
    TextView line2;  
    
    private boolean mLeftImageVisible;
    private boolean mLine1RightVisible;
    
    private int mLine1LeftHeight;
    private int mLine1RightHeight;
    private int mLine1RightWidth;
    private int mLine2Height;
    private int mLine2Width;

    public ListItemView(Context context,  AttributeSet attrs) {
        super(context);
        mContext = context;

        Resources resources = context.getResources();
        mVerticalDividerMargin =
                resources.getDimensionPixelOffset(R.dimen.list_item_vertical_divider_margin);
        mPaddingTop =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_top);
        mPaddingBottom =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_bottom);
        mPaddingLeft =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_left);
        mPaddingRight =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_right);
        mGapBetweenTiTleAndArtist =
                resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_title_and_artist);
        mHeaderTextWidth =
                resources.getDimensionPixelOffset(R.dimen.list_item_header_text_width);
        mHeaderPaddingLeft = 
            resources.getDimensionPixelOffset(R.dimen.list_item_header_text_padding_left);
        mLeftImageHeight =
            resources.getDimensionPixelOffset(R.dimen.list_item_album_image_height);
        mline1RightPaddingTop = 
            resources.getDimensionPixelOffset(R.dimen.list_item_line1right_padding_top);
    }
    
    public void setSectionHeader(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (mHeaderTextView == null) {
                mHeaderTextView = new TextView(mContext);
                mHeaderTextView.setTypeface(mHeaderTextView.getTypeface(), Typeface.BOLD);
                mHeaderTextView.setTextColor(Color.GRAY);
                mHeaderTextView.setGravity(Gravity.CENTER_VERTICAL);
                mHeaderTextView.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
                addView(mHeaderTextView);
            }
            mHeaderTextView.setText(title);
            mHeaderTextView.setVisibility(View.VISIBLE);
            mHeaderVisible = true;
        } else {
            if (mHeaderTextView != null) {
                mHeaderTextView.setVisibility(View.GONE);
            }
            mHeaderVisible = false;
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We will match parent's width and wrap content vertically, but make sure
        // height is no less than listPreferredItemHeight.
        int width = resolveSize(0, widthMeasureSpec);
        int height = 0;

        mLine1LeftHeight = 0;
        mLine1RightHeight = 0;
        mLine2Height = 0;
        mLine2Width = 0; 

        // Obtain the natural dimensions of the name text (we only care about height)
        line1Left.measure(0, 0);
        mLine1LeftHeight = line1Left.getMeasuredHeight();

        if(mLine1RightVisible) {
            line1Right.measure(0, 0);
            mLine1RightHeight = line1Right.getMeasuredHeight();
            mLine1RightWidth = line1Right.getMeasuredWidth();
        }

        line2.measure(0, 0);
        mLine2Height = line2.getMeasuredHeight();
        mLine2Width = line2.getMeasuredWidth();
        
        height += mLine1LeftHeight + mLine2Height + mGapBetweenTiTleAndArtist + mPaddingTop + mPaddingBottom;
        
        if (mHeaderVisible) {
            ensureHeaderBackground();
            mHeaderTextView.measure(
                    MeasureSpec.makeMeasureSpec(mHeaderTextWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
            height += mHeaderBackgroundDrawable.getIntrinsicHeight();
        }
        
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO Auto-generated method stub
        int height = bottom - top;
        int width = right - left;
        
        int topBound = 0;

        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.setBounds(
                    0,
                    0,
                    width,
                    mHeaderBackgroundHeight);
            mHeaderTextView.layout(mHeaderPaddingLeft, 0, width, mHeaderBackgroundHeight);
            topBound += mHeaderBackgroundHeight;
        }
        
        // Left side
        int leftBound = mPaddingLeft;
        
        topBound += mPaddingTop;
        int bottomBound = height - mPaddingBottom;

        // Right side
        int rightBound = right;
        
        // Text lines, centered vertically
        rightBound -= mPaddingRight;

        // Center text vertically

        int textTopBound = topBound;

        int DurationLeft = rightBound - mLine1RightWidth;
        if (mLine1RightVisible) {
            rightBound = DurationLeft - mPaddingRight;
        }
        line1Left.layout(leftBound,
                textTopBound,
                rightBound,
                textTopBound + mLine1LeftHeight);
        line1Left.setFadingEdgeLength(DurationLeft - leftBound);
        
        if (mLine1RightVisible) {
            line1Right.layout(DurationLeft, 
                    textTopBound + mline1RightPaddingTop,
                    DurationLeft + mLine1RightWidth, 
                    textTopBound + mline1RightPaddingTop + mLine1RightHeight);
        }
        
        int line2Top = textTopBound + mLine1LeftHeight + mGapBetweenTiTleAndArtist;
        line2.layout(leftBound,
                line2Top,
                DurationLeft,
                line2Top +  mLine2Height);
        mLine2Width = DurationLeft - leftBound;
    }
    
    private void ensureHeaderBackground() {
        if (mHeaderBackgroundDrawable == null) {
            mHeaderBackgroundDrawable = mContext.getResources().getDrawable(
                    R.drawable.bg_list_header);/*android.R.drawable.dark_header*/
            mHeaderBackgroundHeight = mHeaderBackgroundDrawable.getIntrinsicHeight();
        }
    }
    
    public TextView getLine1LeftTextView() {
        if (line1Left == null) {            
            line1Left = new TextView(mContext);
            line1Left.setSingleLine(true);
            line1Left.setEllipsize(TruncateAt.MARQUEE);            
            line1Left.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            addView(line1Left);
        }
        return line1Left;
    }
    
    public TextView getLine1RightTextView() {
        if (line1Right == null) {
            mLine1RightVisible = true;
            line1Right = new TextView(mContext);
            line1Right.setSingleLine(true);
            line1Right.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            line1Right.setTypeface(line1Right.getTypeface(), Typeface.BOLD);
            addView(line1Right);
        }
        return line1Right;
    }
    
    public TextView getLine2TextView() {
        if (line2 == null) {
            line2 = new TextView(mContext);
            line2.setSingleLine(true);
            line2.setEllipsize(TruncateAt.MARQUEE);
            line2.setFadingEdgeLength(mLine2Width);
            if(mLeftImageVisible)
                line2.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            else 
                line2.setTextAppearance(mContext, android.R.style.TextAppearance_Medium);
            addView(line2);
        }
        return line2;
    }
    
    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }
    
}
