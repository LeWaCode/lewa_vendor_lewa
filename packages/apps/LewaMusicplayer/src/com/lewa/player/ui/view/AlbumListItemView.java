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

public class AlbumListItemView extends ViewGroup{
    
    private final Context mContext;
    
 //   private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mPaddingTop;
    private final int mPaddingRight;
    private final int mPaddingBottom;
    private final int mPaddingLeft;
    private final int mGapBetweenTiTleAndArtist;
    private final int mGapBetweenImageAndText;
    private final int mPresenceIconMargin;
    private final int mHeaderTextWidth;
    private final int mHeaderPaddingLeft;
    private final int mLeftImageHeight;
    private final int mline1RightPaddingTop;
    
    private boolean mHeaderVisible;
    private TextView mHeaderTextView;
    private Drawable mHeaderBackgroundDrawable;
    private Drawable mHorizontalDividerDrawable;
    private int mHeaderBackgroundHeight;
    
    TextView line1;
    TextView line2;
    TextView line3;    
    ImageView mLeftImage; 
    
    private int mLine1Height;
    private int mLine2Height;
    private int mLine2Width;
    private int mLine3Height;
    private int mLine3Width;
    
    private int mHorizontalDividerHeight;
    private int mVerticalHeight;

    public AlbumListItemView(Context context,  AttributeSet attrs) {
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
                resources.getDimensionPixelOffset(R.dimen.album_list_item_padding_left);
        mPaddingRight =
                resources.getDimensionPixelOffset(R.dimen.list_item_padding_right);
        mGapBetweenTiTleAndArtist =
                resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_title_and_artist);
        mGapBetweenImageAndText = 
            resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_image_and_text);
        mPresenceIconMargin =
                resources.getDimensionPixelOffset(R.dimen.list_item_presence_icon_margin);
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

        mLine1Height = 0;
        mLine2Height = 0;
        mLine2Width = 0; 
        mLine3Height = 0;

        // Obtain the natural dimensions of the name text (we only care about height)
        line1.measure(0, 0);
        mLine1Height = line1.getMeasuredHeight();

        line2.measure(0, 0);
        mLine2Height = line2.getMeasuredHeight();
        mLine2Width = line2.getMeasuredWidth();
        
        line3.measure(0, 0);
        mLine3Height = line3.getMeasuredHeight();
        mLine3Width = line3.getMeasuredWidth();
        
        
 //       height += mLine1Height + mLine2Height + mLine3Height + mVerticalHeight;
        height += mLine1Height + mLine2Height + mLine3Height;

        mLeftImage.measure(MeasureSpec.makeMeasureSpec(mLeftImageHeight, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(mLeftImageHeight, MeasureSpec.EXACTLY));
        height = Math.max(height, mLeftImage.getMeasuredHeight());
        height += mPaddingTop + mPaddingBottom;
        
        if (mHeaderVisible) {
            ensureHeaderBackground();
            mHeaderTextView.measure(
                    MeasureSpec.makeMeasureSpec(mHeaderTextWidth, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
            height += mHeaderBackgroundDrawable.getIntrinsicHeight();
          //  height += mHeaderBackgroundHeight;
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
        
        // Right side
        int rightBound = right;
        
        // Text lines, centered vertically
        rightBound -= mPaddingRight;
        
        // Center the photo vertically
        int imageTop = topBound + (height - topBound - mPaddingBottom - mLeftImageHeight) / 2;
        mLeftImage.layout(leftBound, 
                imageTop, 
                leftBound + mLeftImageHeight,
                imageTop + mLeftImageHeight);
        leftBound += mLeftImageHeight + mGapBetweenImageAndText;

        topBound -= mline1RightPaddingTop * 2;
        line1.layout(leftBound,
                topBound,
                rightBound,
                topBound + mLine1Height); 
        
        int line2Top = topBound + mLine1Height;
        line2.layout(leftBound,
                line2Top,
                rightBound,
                line2Top +  mLine2Height);
        mLine2Width = rightBound - leftBound;

        int bottomBound = height - mPaddingBottom + mline1RightPaddingTop;
        line3.layout(leftBound, bottomBound - mLine3Height, leftBound + mLine3Width, bottomBound);
    }
    
    private void ensureHeaderBackground() {
        if (mHeaderBackgroundDrawable == null) {
            mHeaderBackgroundDrawable = mContext.getResources().getDrawable(
                    R.drawable.bg_list_header);/*android.R.drawable.dark_header*/
            mHeaderBackgroundHeight = mHeaderBackgroundDrawable.getIntrinsicHeight();
        }
    }
    
    public TextView getLine1TextView() {
        if (line1 == null) {            
            line1 = new TextView(mContext);
//            line1.setEllipsize(TruncateAt.MARQUEE);
//            line1.setSingleLine(true);  
            line1.setEllipsize(TruncateAt.MARQUEE);
            line1.setMaxLines(1);
            line1.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            addView(line1);
        }
        return line1;
    }
    
    public TextView getLine2TextView() {
        if (line2 == null) {
            line2 = new TextView(mContext);            
            line2.setEllipsize(TruncateAt.MARQUEE);
            line2.setSingleLine(true);
            line2.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            line2.setTypeface(line2.getTypeface(), Typeface.BOLD);
            addView(line2);
        }
        return line2;
    }
    
    public TextView getLine3TextView() {
        if (line3 == null) {
            line3 = new TextView(mContext);
            line1.setEllipsize(TruncateAt.MARQUEE);
            line1.setSingleLine(true);      
//            line3.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            line3.setTextSize(12);
            line3.setTypeface(line3.getTypeface(), Typeface.BOLD);
            line3.setBackgroundResource(R.drawable.music_album_songs);
            addView(line3);
        }
        return line3;
    }
    
    public ImageView getLeftImageView() {
        if (mLeftImage == null) {
            mLeftImage = new ImageView(mContext);
            mLeftImage.setBackgroundDrawable(null);
            addView(mLeftImage);
        }
        return mLeftImage;
    }
    
    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mHeaderVisible) {
            mHeaderBackgroundDrawable.draw(canvas);
        }
        super.dispatchDraw(canvas);
    }
    
}
