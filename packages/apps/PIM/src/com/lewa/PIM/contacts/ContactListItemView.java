/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.lewa.PIM.contacts;

import com.lewa.PIM.R;
import com.lewa.PIM.contacts.ui.widget.DontPressWithParentImageView;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * A custom view for an item in the contact list.
 */
public class ContactListItemView extends ViewGroup {

    private static final int QUICK_CONTACT_BADGE_STYLE =
        com.android.internal.R.attr.quickContactBadgeStyleWindowMedium;

    private final Context mContext;

    private final int mPreferredHeight;
    private final int mVerticalDividerMargin;
    private final int mPaddingTop;
    private final int mPaddingRight;
    private final int mPaddingBottom;
    private final int mPaddingLeft;
    private final int mGapBetweenImageAndText;
    private final int mGapBetweenLabelAndData;
    private final int mCallButtonPadding;
    private final int mPresenceIconMargin;
    private final int mHeaderTextWidth;
    //private final float mHeaderTextHeight;
    //private final int mDividerMargin;
    private final int mDividerMarginLeft;
    private final int mDividerMarginRight;
    private final int mImageViewHeight;

    private boolean mHorizontalDividerVisible;
    private Drawable mHorizontalDividerDrawable;

    private boolean mVerticalDividerVisible;
    private Drawable mVerticalDividerDrawable;
    private int mVerticalDividerWidth;

    private boolean mHeaderVisible;
    private boolean mIsBigHeader = false;
    //private Drawable mHeaderBackgroundDrawable;
    //private Drawable mBigHeaderBackgroundDrawable;
    private float mHeaderBackgroundHeight;
    private float mBigHeaderBackgroundHeight;
    private TextView mHeaderTextView;

    private QuickContactBadge mQuickContact;
    private ImageView mPhotoView;
    private TextView mNameTextView;
    private DontPressWithParentImageView mCallButton;
    private TextView mLabelView;
    private TextView mDataView;
    private TextView mSnippetView;
    private ImageView mPresenceIcon;
    private ImageView mStatusImageView;
    private int mPhotoViewWidth;
    private int mPhotoViewHeight;
    private int mLine1Height;
    private int mLine2Height;
    private int mLine3Height;
    private OnClickListener mCallButtonClickListener;
    private OnClickListener mStatusBtnClickListener;
    private Paint mPaint = new Paint();
    private float mWidth;
    private float mHeight;
    private float mHeaderHeight;
    private float mDividerHeight;
    private float mHeaderTextViewSize;

    private Drawable mYiLiaoIconTip;
    private Drawable mYiLiaoOnLineTip;

    private int mYiLiaoIconPaddingLeft;
    private int mYiLiaoIconWidth;
    private int mYiLiaoOnLineWidth;
    private int mYiLiaoOnLineHeight;
    private static final int ACTIVITY_BACKGROUND = 0x00000000;
    private static final int BG_CONTACTLISTITEM_HEADER = 0xFF009BE2;
    private static final int BG_CONTACTLISTITEM_DIVIDER = 0xFFC2C2C2;
    private static final int HEADERTEXT = 0xFF0098E2;
    private static final int BG_PHOTO = 0xFF52AAE4;
    
    public static final int STATE_YILIAO_ONLINE = 1;
    public static final int STATE_YILIAO_OFFLINE = 2;
    public static final int STATE_YILIAO_DISABLE = 3;
    public static final int STATE_YILIAO_NULL = 4;

    private final int mylImageMarg;

    public ContactListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        // Obtain preferred item height from the current theme
        TypedArray a = context.obtainStyledAttributes(null, com.android.internal.R.styleable.Theme);
        mPreferredHeight =
            a.getDimensionPixelSize(android.R.styleable.Theme_listPreferredItemHeight, 0);
        a.recycle();

        Resources resources = context.getResources();
        mVerticalDividerMargin =
            resources.getDimensionPixelOffset(R.dimen.list_item_vertical_divider_margin);
        mPaddingTop =
            resources.getDimensionPixelOffset(R.dimen.list_item_padding_top);
        mPaddingBottom =
            resources.getDimensionPixelOffset(R.dimen.list_item_padding_bottom);
        //mPaddingLeft =
        //        resources.getDimensionPixelOffset(R.dimen.list_item_padding_left);
        mPaddingRight =
            resources.getDimensionPixelOffset(R.dimen.list_item_padding_right);
        mGapBetweenImageAndText =
            resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_yl_image_left);
        mGapBetweenLabelAndData =
            resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_label_and_data);
        mCallButtonPadding =
            resources.getDimensionPixelOffset(R.dimen.list_item_call_button_padding);
        mPresenceIconMargin =
            resources.getDimensionPixelOffset(R.dimen.list_item_presence_icon_margin);
        mHeaderTextWidth =
            resources.getDimensionPixelOffset(R.dimen.list_item_header_text_width);
        //mHeaderTextHeight = resources.getDimensionPixelOffset(R.dimen.list_item_header_text_height);
        //mDividerMargin = 
        //        resources.getDimensionPixelOffset(R.dimen.list_item_divider_margin);
        mImageViewHeight = 
            resources.getDimensionPixelOffset(R.dimen.list_item_contact_photo_height);  

        mylImageMarg = resources.getDimensionPixelOffset(R.dimen.list_item_gap_between_yl_image_right); 

        //mHorizontalDividerHeight = resources.getDimensionPixelOffset(R.dimen.list_item_divider_height);
        mHeaderBackgroundHeight = resources.getDimensionPixelOffset(R.dimen.list_item_contact_headerbg_height);
        mBigHeaderBackgroundHeight = resources.getDimensionPixelOffset(R.dimen.list_item_contact_bigheaderbg_height);
        mHeaderHeight = resources.getDimensionPixelOffset(R.dimen.list_item_contact_header_height);
        mDividerHeight = resources.getDimensionPixelOffset(R.dimen.list_item_divider_height);
        mHeaderTextViewSize = resources.getDimensionPixelOffset(R.dimen.list_item_contact_headertextview_size);

        if (context instanceof ItemDimension) {
            mDividerMarginLeft = ((ItemDimension )context).getItemMarginLeft();
            mDividerMarginRight = ((ItemDimension )context).getItemMarginRight();
            mPaddingLeft = mDividerMarginLeft;
        }
        else {
            mPaddingLeft = resources.getDimensionPixelOffset(R.dimen.list_item_padding_left);
            mDividerMarginLeft = resources.getDimensionPixelOffset(R.dimen.list_item_divider_margin_left);
            mDividerMarginRight = resources.getDimensionPixelOffset(R.dimen.list_item_divider_margin_right);
        }

        mYiLiaoIconPaddingLeft = mContext.getResources().getDimensionPixelOffset(R.dimen.list_item_yiliaostate_paddingleft);
    }

    /**
     * Installs a call button listener.
     */
    public void setOnCallButtonClickListener(OnClickListener callButtonClickListener) {
        mCallButtonClickListener = callButtonClickListener;
    }

    public void setStatusBtnClickListener(OnClickListener statusBtnClickListener) {
        mStatusBtnClickListener = statusBtnClickListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // We will match parent's width and wrap content vertically, but make sure
        // height is no less than listPreferredItemHeight.
        int width = resolveSize(0, widthMeasureSpec);
        int height = 0;

        mLine1Height = 0;
        mLine2Height = 0;
        mLine3Height = 0;

        // Obtain the natural dimensions of the name text (we only care about height)
        mNameTextView.measure(0, 0);

        mLine1Height = mNameTextView.getMeasuredHeight();

        if (isVisible(mLabelView)) {
            mLabelView.measure(0, 0);
            mLine2Height = mLabelView.getMeasuredHeight();
        }

        if (isVisible(mDataView)) {
            mDataView.measure(0, 0);
            mLine2Height = Math.max(mLine2Height, mDataView.getMeasuredHeight());
        }

        if (isVisible(mSnippetView)) {
            mSnippetView.measure(0, 0);
            mLine3Height = mSnippetView.getMeasuredHeight();
        }

        height += mLine1Height + mLine2Height + mLine3Height;

        if (isVisible(mCallButton)) {
            mCallButton.measure(0, 0);
        }
        if (isVisible(mPresenceIcon)) {
            mPresenceIcon.measure(0, 0);
        }

        ensurePhotoViewSize();

        height = Math.max(height, mPhotoViewHeight);
        height = Math.max(height, mPreferredHeight);

        height = (int) (height + 2 * mDividerHeight);

        if (mHeaderVisible) {
            //ensureHeaderBackground();
            if (mIsBigHeader) {
                mHeaderTextView.measure(
                        MeasureSpec.makeMeasureSpec(mHeaderTextWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec((int)mBigHeaderBackgroundHeight, MeasureSpec.EXACTLY));
                height += mBigHeaderBackgroundHeight;
            }
            else {
                mHeaderTextView.measure(
                        MeasureSpec.makeMeasureSpec(mHeaderTextWidth, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec((int)mHeaderBackgroundHeight, MeasureSpec.EXACTLY));
                height += mHeaderBackgroundHeight; //mHeaderBackgroundDrawable.getIntrinsicHeight();
            }
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(width, height);
        }

        mWidth = (float)width;
        mHeight = (float)height;

        if (mStatusImageView != null) {
            mStatusImageView.measure(mYiLiaoOnLineWidth, mYiLiaoOnLineHeight);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int height = (int) (bottom - top - 2*mDividerHeight);
        int width = right - left;

        // Determine the vertical bounds by laying out the header first.
        int topBound = 0;

        if (mHeaderVisible) {
            if (mIsBigHeader) {
                /*mBigHeaderBackgroundDrawable.setBounds(
                        mDividerMarginLeft, //0,
                        0,
                        (width - mDividerMarginRight), //width,
                        mBigHeaderBackgroundHeight);*/
                mHeaderTextView.layout(
                        mDividerMarginLeft, //0,
                        0,
                        (mHeaderTextWidth + mDividerMarginLeft), //mHeaderTextWidth,
                        (int)mBigHeaderBackgroundHeight);
                topBound += mBigHeaderBackgroundHeight;
                //Log.e("ContactListItemView", "onLayout: HeadText=" + mHeaderTextHeight
                //        + " IsBig=true Height=" + mBigHeaderBackgroundHeight);
            }
            else {
                //topBound += mPaddingTop;
                /*mHeaderBackgroundDrawable.setBounds(
                        mDividerMarginLeft, //0,
                        topBound,
                        (width - mDividerMarginRight), //width,
                        (topBound + mHeaderBackgroundHeight));*/
                //mHeaderTextView.layout(0, 0, width, mHeaderBackgroundHeight);
                mHeaderTextView.layout(
                        mDividerMarginLeft, //0,
                        (int)(topBound),
                        (mHeaderTextWidth + mDividerMarginLeft), //mHeaderTextWidth,
                        (int) (topBound + mHeaderBackgroundHeight));
                topBound += mHeaderBackgroundHeight;
                //Log.e("ContactListItemView", "onLayout: HeadText=" + mHeaderTextView.getText()
                //        + " IsBig=false Height=" + mHeaderBackgroundHeight);
            }
        }

        // Positions of views on the left are fixed and so are those on the right side.
        // The stretchable part of the layout is in the middle.  So, we will start off
        // by laying out the left and right sides. Then we will allocate the remainder
        // to the text fields in the middle.

        // Left side
        int leftBound = mPaddingLeft;
        View photoView = mQuickContact != null ? mQuickContact : mPhotoView;
        if (photoView != null) {
            // Center the photo vertically
            int photoTop = topBound + (height - topBound - mImageViewHeight) / 2;
            //int photoTop = height - mImageViewHeight;
            photoView.layout(
                    0,//leftBound modify by zenghuaying 2012.8.15
                    photoTop,
                    mImageViewHeight,//leftBound + mImageViewHeight modify by zenghuaying 2012.8.15
                    photoTop + mImageViewHeight);
            if(mYiLiaoIconTip != null) {
                mYiLiaoIconTip.setBounds((int) (leftBound+mYiLiaoIconPaddingLeft), 
                        photoTop + mImageViewHeight - mYiLiaoIconTip.getIntrinsicHeight(), 
                        (int) (leftBound+mYiLiaoIconPaddingLeft+mYiLiaoIconTip.getIntrinsicWidth()), 
                        photoTop + mImageViewHeight);
            }

            leftBound += mImageViewHeight + mGapBetweenImageAndText;
        }

        // Right side
        int rightBound = right;
        if (isVisible(mCallButton)) {
            int buttonWidth = mCallButton.getMeasuredWidth();
            rightBound -= buttonWidth;
            mCallButton.layout(
                    rightBound,
                    topBound,
                    rightBound + buttonWidth,
                    height);
            mVerticalDividerVisible = true;
            ensureVerticalDivider();
            rightBound -= mVerticalDividerWidth;
            mVerticalDividerDrawable.setBounds(
                    rightBound,
                    topBound + mVerticalDividerMargin,
                    rightBound + mVerticalDividerWidth,
                    height - mVerticalDividerMargin);
        } else {
            mVerticalDividerVisible = false;
        }

        if (isVisible(mPresenceIcon)) {
            int iconWidth = mPresenceIcon.getMeasuredWidth();
            rightBound -= mPresenceIconMargin + iconWidth;
            mPresenceIcon.layout(
                    rightBound,
                    topBound,
                    rightBound + iconWidth,
                    height);
        }

        /* if (mHorizontalDividerVisible) {
            ensureHorizontalDivider();
            mHorizontalDividerDrawable.setBounds(
                    mDividerMarginLeft, //mDividerMargin,
                    height - mHorizontalDividerHeight,
                    (width - mDividerMarginRight),//width-mDividerMargin,
                    height);
        }*/

        topBound += mPaddingTop;
        int bottomBound = height - mPaddingBottom;

        // Text lines, centered vertically
        rightBound -= mPaddingRight;

        // Center text vertically
        int totalTextHeight = mLine1Height + mLine2Height + mLine3Height;
        int textTopBound = (bottomBound + topBound - totalTextHeight) / 2;
        int statusImageTopBound = (bottomBound + topBound - mYiLiaoOnLineHeight) / 2;
        //leftBound += mYiLiaoIconWidth/2;
        //rightBound += mYiLiaoIconWidth/2;

        if(mStatusImageView != null){
            mStatusImageView.layout(leftBound - mPaddingLeft,//leftBound modify by zenghuaying 2012.8.22
                    statusImageTopBound,
                    (int)leftBound + mYiLiaoOnLineWidth- mPaddingLeft,//leftBound+ mYiLiaoOnLineWidth modify by zenghuaying 2012.8.22
                    statusImageTopBound + mYiLiaoOnLineHeight);
        }
        leftBound += mYiLiaoOnLineWidth + mylImageMarg;
        mNameTextView.layout(leftBound-mPaddingLeft,//leftBound modify by zenghuaying 2012.8.15
                textTopBound,
                rightBound,
                textTopBound + mLine1Height);


        int dataLeftBound = leftBound;
        if (isVisible(mLabelView)) {
            dataLeftBound = leftBound + mLabelView.getMeasuredWidth();
            mLabelView.layout(leftBound,
                    textTopBound + mLine1Height,
                    dataLeftBound,
                    textTopBound + mLine1Height + mLine2Height);
            dataLeftBound += mGapBetweenLabelAndData;
        }

        if (isVisible(mDataView)) {
            mDataView.layout(dataLeftBound,
                    textTopBound + mLine1Height,
                    rightBound,
                    textTopBound + mLine1Height + mLine2Height);
        }

        if (isVisible(mSnippetView)) {
            mSnippetView.layout(leftBound,
                    textTopBound + mLine1Height + mLine2Height,
                    rightBound,
                    textTopBound + mLine1Height + mLine2Height + mLine3Height);
        }
    }

    private boolean isVisible(View view) {
        return view != null && view.getVisibility() == View.VISIBLE;
    }

    /**
     * Loads the drawable for the vertical divider if it has not yet been loaded.
     */
    private void ensureVerticalDivider() {
        if (mVerticalDividerDrawable == null) {
            mVerticalDividerDrawable = mContext.getResources().getDrawable(
                    R.drawable.divider_vertical_dark);
            mVerticalDividerWidth = mVerticalDividerDrawable.getIntrinsicWidth();
        }
    }

    /**
     * Loads the drawable for the horizontal divider if it has not yet been loaded.
     */
    /*private void ensureHorizontalDivider() {
        if (mHorizontalDividerDrawable == null) {
            mHorizontalDividerDrawable = mContext.getResources().getDrawable(
                    com.android.internal.R.drawable.divider_horizontal_dark_opaque);
            mHorizontalDividerDrawable = mContext.getResources().getDrawable(
                    R.drawable.ic_list_horizontal_divider);
            mHorizontalDividerHeight = mHorizontalDividerDrawable.getIntrinsicHeight();
        }
    }*/

    /**
     * Loads the drawable for the header background if it has not yet been loaded.
     */
    /* private void ensureHeaderBackground() {
        if ((mHeaderBackgroundDrawable == null) || (null == mBigHeaderBackgroundDrawable)) {
            mHeaderBackgroundDrawable = mContext.getResources().getDrawable(
                        R.drawable.bg_contacts_list_header);android.R.drawable.dark_header
            mHeaderBackgroundHeight = mHeaderBackgroundDrawable.getIntrinsicHeight();

            mBigHeaderBackgroundDrawable = mContext.getResources().getDrawable(
                    R.drawable.bg_contacts_list_header_big);
            mBigHeaderBackgroundHeight = mBigHeaderBackgroundDrawable.getIntrinsicHeight();
        }
    }*/

    /**
     * Extracts width and height from the style
     */
    private void ensurePhotoViewSize() {
        if (mPhotoViewWidth == 0 && mPhotoViewHeight == 0) {
            TypedArray a = mContext.obtainStyledAttributes(null,
                    com.android.internal.R.styleable.ViewGroup_Layout,
                    QUICK_CONTACT_BADGE_STYLE, 0);
            mPhotoViewWidth = a.getLayoutDimension(
                    android.R.styleable.ViewGroup_Layout_layout_width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mPhotoViewHeight = a.getLayoutDimension(
                    android.R.styleable.ViewGroup_Layout_layout_height,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            a.recycle();
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mHeaderVisible) {
            if (mIsBigHeader) {
                //mBigHeaderBackgroundDrawable.draw(canvas);
                //mPaint.setColor(this.getResources().getColor(R.color.detail_activity_background));
                mPaint.setColor(ACTIVITY_BACKGROUND);
                canvas.drawRect(mDividerMarginLeft, 0, mWidth, mBigHeaderBackgroundHeight, mPaint);
                // change color from fix int to res.
                int dividerColor = getResources().getColor(R.color.contactlist_index_divider_line_color);
                mPaint.setColor(dividerColor);
                //end
                //canvas.drawRect(mDividerMarginLeft, mBigHeaderBackgroundHeight - mHeaderHeight, mWidth-mDividerMarginRight, mBigHeaderBackgroundHeight, mPaint);
                canvas.drawRect(0, mBigHeaderBackgroundHeight - mHeaderHeight, mWidth, mBigHeaderBackgroundHeight, mPaint);
            }
            else {
                //mHeaderBackgroundDrawable.draw(canvas);
                //mPaint.setColor(this.getResources().getColor(R.color.detail_activity_background));
                mPaint.setColor(ACTIVITY_BACKGROUND);
                canvas.drawRect(mDividerMarginLeft, 0, mWidth, mHeaderBackgroundHeight, mPaint);
                // change color from fix int to res.
                int dividerColor = getResources().getColor(R.color.contactlist_index_divider_line_color);
                mPaint.setColor(dividerColor);
                // end
                //canvas.drawRect(mDividerMarginLeft, mHeaderBackgroundHeight - mHeaderHeight, mWidth-mDividerMarginRight, mHeaderBackgroundHeight, mPaint);
                canvas.drawRect(0, mHeaderBackgroundHeight - mHeaderHeight, mWidth, mHeaderBackgroundHeight, mPaint);
            }
        }
        if (mHorizontalDividerVisible) {
            //mHorizontalDividerDrawable.draw(canvas);
            //mPaint.setColor(BG_CONTACTLISTITEM_DIVIDER);
            //canvas.drawLine(mDividerMarginLeft, mHeight - mDividerHeight, mWidth-mDividerMarginRight, mHeight, mPaint);

            mPaint.setColor(0xffd8d8d8);
            //canvas.drawLine(mDividerMarginLeft, mHeight - 2*mDividerHeight, mWidth-mDividerMarginRight, mHeight - mDividerHeight, mPaint);
            canvas.drawLine(0, mHeight - 2*mDividerHeight, mWidth, mHeight - mDividerHeight, mPaint);
            mPaint.setColor(0xffffffff);
            //canvas.drawLine(mDividerMarginLeft, mHeight - mDividerHeight, mWidth-mDividerMarginRight, mHeight, mPaint);
            canvas.drawLine(0, mHeight - mDividerHeight, mWidth, mHeight, mPaint);
            mPaint.setColor(ACTIVITY_BACKGROUND);    		
            canvas.drawRect(mWidth-mDividerMarginRight, mHeight - 2*mDividerHeight, mWidth, mHeight, mPaint);
        }

        if (mVerticalDividerVisible) {
            mVerticalDividerDrawable.draw(canvas);
        }

        if(mDividerMarginLeft >0 ) {
            //mPaint.setColor(this.getResources().getColor(R.color.detail_activity_background));
            mPaint.setColor(ACTIVITY_BACKGROUND);
            canvas.drawRect(0, 0, mDividerMarginLeft, mHeight, mPaint);
        }

        super.dispatchDraw(canvas);

        if(mYiLiaoIconTip != null) {
            mYiLiaoIconTip.draw(canvas);
        }

        //        if(mYiLiaoOnLineTip != null) {
        //        	mYiLiaoOnLineTip.draw(canvas);
        //        }

    }

    /**
     * Sets the flag that determines whether a divider should drawn at the bottom
     * of the view.
     */
    public void setDividerVisible(boolean visible) {
        mHorizontalDividerVisible = visible;
    }

    public void setHeaderIsBig(boolean isBigHeader) {
        mIsBigHeader = isBigHeader;
    }

    /**
     * Sets section header or makes it invisible if the title is null.
     */
    public void setSectionHeader(String title) {
        if (!TextUtils.isEmpty(title)) {
            if (mHeaderTextView == null) {
                mHeaderTextView = new TextView(mContext);
                //mHeaderTextView.setTypeface(mHeaderTextView.getTypeface(), Typeface.BOLD);
                //change color from fix int to res.
                int heaerTextxColor = getResources().getColor(R.color.contactlist_index_headertext_color);
                mHeaderTextView.setTextColor(heaerTextxColor); //(0xff007aff);
                //end
                mHeaderTextView.setTextSize(17);
                mHeaderTextView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                addView(mHeaderTextView);
            }
            if (title.equals(" ") == true)
                mHeaderTextView.setText("#");
            else
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

    /**
     * Returns the quick contact badge, creating it if necessary.
     */
    public QuickContactBadge getQuickContact() {
        if (mQuickContact == null) {
            mQuickContact = new QuickContactBadge(mContext, null);
            mQuickContact.setExcludeMimes(new String[] { Contacts.CONTENT_ITEM_TYPE });
            addView(mQuickContact);
        }
        return mQuickContact;
    }

    /**
     * Returns the photo view, creating it if necessary.
     */
    public ImageView getPhotoView() {
        if (mPhotoView == null) {
            mPhotoView = new ImageView(mContext, null);
            // Quick contact style used above will set a background - remove it
            mPhotoView.setBackgroundDrawable(null);
            mPhotoView.setImageDrawable(null);
            addView(mPhotoView);
        }
        return mPhotoView;
    }
    public ImageView getStatusView(int id) {
        if (mStatusImageView == null) {
            mStatusImageView = new ImageView(mContext, null);
            // Quick contact style used above will set a background - remove it
            mStatusImageView.setBackgroundDrawable(null);
            mStatusImageView.setId(id);
            mStatusImageView.setOnClickListener(mStatusBtnClickListener);
            addView(mStatusImageView);
        }
        return mStatusImageView;
    }
    /**
     * Returns the text view for the contact name, creating it if necessary.
     */
    public TextView getNameTextView() {
        if (mNameTextView == null) {
            mNameTextView = new TextView(mContext);
            mNameTextView.setSingleLine(true);
            mNameTextView.setEllipsize(TruncateAt.MARQUEE);
            //mNameTextView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
            mNameTextView.setTextColor(0xff3b3b3b);
            mNameTextView.setTextSize(18);
            mNameTextView.setGravity(Gravity.CENTER_VERTICAL);
            addView(mNameTextView);
        }
        return mNameTextView;
    }

    /**
     * Adds a call button using the supplied arguments as an id and tag.
     */
    public void showCallButton(int id, int tag) {
        if (mCallButton == null) {
            mCallButton = new DontPressWithParentImageView(mContext, null);
            mCallButton.setId(id);
            mCallButton.setOnClickListener(mCallButtonClickListener);
            mCallButton.setBackgroundResource(R.drawable.call_background);
            mCallButton.setImageResource(android.R.drawable.sym_action_call);
            mCallButton.setPadding(mCallButtonPadding, 0, mCallButtonPadding, 0);
            mCallButton.setScaleType(ScaleType.CENTER);
            addView(mCallButton);
        }

        mCallButton.setTag(tag);
        mCallButton.setVisibility(View.VISIBLE);
    }

    public void hideCallButton() {
        if (mCallButton != null) {
            mCallButton.setVisibility(View.GONE);
        }
    }

    /**
     * Adds or updates a text view for the data label.
     */
    public void setLabel(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mLabelView != null) {
                mLabelView.setVisibility(View.GONE);
            }
        } else {
            getLabelView();
            mLabelView.setText(text);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Adds or updates a text view for the data label.
     */
    public void setLabel(char[] text, int size) {
        if (text == null || size == 0) {
            if (mLabelView != null) {
                mLabelView.setVisibility(View.GONE);
            }
        } else {
            getLabelView();
            mLabelView.setText(text, 0, size);
            mLabelView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data label, creating it if necessary.
     */
    public TextView getLabelView() {
        if (mLabelView == null) {
            mLabelView = new TextView(mContext);
            mLabelView.setSingleLine(true);
            mLabelView.setEllipsize(TruncateAt.MARQUEE);
            mLabelView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mLabelView.setTypeface(mLabelView.getTypeface(), Typeface.BOLD);
            addView(mLabelView);
        }
        return mLabelView;
    }

    /**
     * Adds or updates a text view for the data element.
     */
    public void setData(char[] text, int size) {
        if (text == null || size == 0) {
            if (mDataView != null) {
                mDataView.setVisibility(View.GONE);
            }
            return;
        } else {
            getDataView();
            mDataView.setText(text, 0, size);
            mDataView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the data text, creating it if necessary.
     */
    public TextView getDataView() {
        if (mDataView == null) {
            mDataView = new TextView(mContext);
            mDataView.setSingleLine(true);
            mDataView.setEllipsize(TruncateAt.MARQUEE);
            mDataView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            addView(mDataView);
        }
        return mDataView;
    }

    /**
     * Adds or updates a text view for the search snippet.
     */
    public void setSnippet(CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            if (mSnippetView != null) {
                mSnippetView.setVisibility(View.GONE);
            }
        } else {
            getSnippetView();
            mSnippetView.setText(text);
            mSnippetView.setVisibility(VISIBLE);
        }
    }

    /**
     * Returns the text view for the search snippet, creating it if necessary.
     */
    public TextView getSnippetView() {
        if (mSnippetView == null) {
            mSnippetView = new TextView(mContext);
            mSnippetView.setSingleLine(true);
            mSnippetView.setEllipsize(TruncateAt.MARQUEE);
            mSnippetView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
            mSnippetView.setTypeface(mSnippetView.getTypeface(), Typeface.BOLD);
            addView(mSnippetView);
        }
        return mSnippetView;
    }

    /**
     * Adds or updates the presence icon view.
     */
    public void setPresence(Drawable icon) {
        if (icon != null) {
            if (mPresenceIcon == null) {
                mPresenceIcon = new ImageView(mContext);
                addView(mPresenceIcon);
            }
            mPresenceIcon.setImageDrawable(icon);
            mPresenceIcon.setScaleType(ScaleType.CENTER);
            mPresenceIcon.setVisibility(View.VISIBLE);
        } else {
            if (mPresenceIcon != null) {
                mPresenceIcon.setVisibility(View.GONE);
            }
        }
    }        
    public void setYiLiaoState(int state) {
        switch(state) {
        case STATE_YILIAO_ONLINE:
            mYiLiaoIconTip = mContext.getResources().getDrawable(
                    R.drawable.conect_icon_online_people);
            break;
        case STATE_YILIAO_OFFLINE:
            mYiLiaoIconTip = mContext.getResources().getDrawable(
                    R.drawable.conect_icon_offline_people);
            break;
        case STATE_YILIAO_DISABLE:
            mYiLiaoIconTip = null;
            //mYiLiaoIconWidth = 0;
            break;
        }
        if (mYiLiaoIconTip != null)
            mYiLiaoIconWidth = mYiLiaoIconTip.getIntrinsicWidth();
        requestLayout();
    }

    public void setYiLiaoOnLineState(int state) {
        switch(state) {
        case STATE_YILIAO_ONLINE:
            mYiLiaoOnLineTip = mContext.getResources().getDrawable(
                    R.drawable.icon_contact_header_online);
            break;
        case STATE_YILIAO_OFFLINE:
            mYiLiaoOnLineTip = mContext.getResources().getDrawable(
                    R.drawable.icon_contact_header_offline);
            break;
        case STATE_YILIAO_DISABLE:
        case STATE_YILIAO_NULL:
            mYiLiaoOnLineTip = mContext.getResources().getDrawable(
                    R.drawable.icon_contact_header_offline);
            //mYiLiaoIconWidth = 0;
            break;    		
        }

        if (mStatusImageView == null){
            mStatusImageView = new ImageView(mContext, null);
            // Quick contact style used above will set a background - remove it
            addView(mStatusImageView);
        }
        //    mStatusImageView.setBackgroundDrawable(mYiLiaoOnLineTip);

        if (state == STATE_YILIAO_NULL) {
            mYiLiaoOnLineWidth = 0;
            mYiLiaoOnLineHeight = 0;

        }else {
            mYiLiaoOnLineWidth = mYiLiaoOnLineTip.getIntrinsicWidth();			
            mYiLiaoOnLineHeight = mYiLiaoOnLineTip.getIntrinsicHeight();
        }

        if (state == STATE_YILIAO_ONLINE || state == STATE_YILIAO_OFFLINE) {
            mStatusImageView.setImageDrawable(mYiLiaoOnLineTip);
            mStatusImageView.setVisibility(View.VISIBLE);
        }else {
            mStatusImageView.setImageDrawable(null);
            mStatusImageView.setVisibility(View.GONE);
            mStatusImageView = null;
        }
    }

    interface ItemDimension {
        public int getItemMarginLeft();
        public int getItemMarginRight();
    }
}
