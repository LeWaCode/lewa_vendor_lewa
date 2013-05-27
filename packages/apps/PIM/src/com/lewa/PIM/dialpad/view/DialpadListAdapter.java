package com.lewa.PIM.dialpad.view;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.lewa.os.filter.FilterItem;
import com.lewa.os.filter.SpannableFilterItemVisitor;
import com.lewa.os.util.ContactPhotoLoader;
import com.lewa.os.util.NumberLocationLoader;
import com.lewa.os.view.ListBaseAdapter;
import com.lewa.PIM.contacts.LayoutQuickContactBadge;
import com.lewa.PIM.dialpad.data.DialpadItem;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.PIM.R;

public class DialpadListAdapter extends ListBaseAdapter<DialpadItem> {
    //private static final String TAG = "DialpadListAdapter";
    
    private Context mContext;
    private SpannableFilterItemVisitor mItemVisitor;
    private ContactPhotoLoader mPhotoLoader;
    private NumberLocationLoader mNumLocationLoader;
    private int mFilterCallType = DialpadView.CALLLOG_TYPE_ALL;

    private SimpleDateFormat mTimeFormat;
    //add by zenghuaying
    private boolean mIsShowInFilterList;
    //add end
    public DialpadListAdapter(
            Context context,
            List<DialpadItem> dialpadItems,
            ContactPhotoLoader photoLoader,
            NumberLocationLoader numLocationLoader,
            boolean isShowInFilterList) {
        super(dialpadItems);
        mContext = context;
        mItemVisitor = new SpannableFilterItemVisitor();
        mPhotoLoader = photoLoader;
        mNumLocationLoader = numLocationLoader;
        mIsShowInFilterList = isShowInFilterList;
    }

    public void setFilterCallType(int calltype) {
        mFilterCallType = calltype;
    }

    public int getFilterCallType() {
        return mFilterCallType;
    }

    @Override
    public int getItemViewType(int position) {
        int type = ((DialpadItem )getItem(position)).getType();
        return type;
    }
    //add by zenghuaying
    public void setIsShowInFilterList(boolean isShowInFilterList){
        mIsShowInFilterList = isShowInFilterList;
    }
    
    protected boolean isCallLogType(DialpadItem item){
        
        return item.getType() == DialpadItem.CALLLOG_TYPE ? true : false;
    }
    

    
    protected boolean isSpecialNum(DialpadItem item){
        return item.getIsSpecialNum();
    }
    /**
    protected void logNumberAndName(DialpadItem item,String from){
        Log.i("zhy","name = " + item.getName() +" number= " + item.getNumber()+" type= "+item.getType()
                + " filterCount = " + item.getFilterCount() + " contactedTimes = " + item.getContactedTimes()
                +" mode = " + from);
    }*/
    //add end
    @Override
    public int getViewTypeCount() {
        return (DialpadItem.CONTACT_TYPE + 1);
    }

    @Override
    protected View createItemView(DialpadItem dialpadItem, int position) {
        int type = dialpadItem.getType();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        if (DialpadItem.CALLLOG_TYPE == type) {
            return layoutInflater.inflate(R.layout.dialpad_calllog_item_entry, null);
        }
        else {
            return layoutInflater.inflate(R.layout.dialpad_contact_item_entry, null);
        }
    }

    @Override
    protected void bindItemView(DialpadItem dialpadItem, int position, View itemView) {
        int type       = dialpadItem.getType();
        long contactId = dialpadItem.getContactId();
        String name    = dialpadItem.getName();
        int count      = 0;
        String number  = dialpadItem.getNumber();
        String location = dialpadItem.getLocation();
        //add by zenghuaying
        boolean isSpecialNum = dialpadItem.getIsSpecialNum();
        boolean hasName = false;
        
        ItemViewHolder viewHolder = (ItemViewHolder )getViewHolder(itemView, dialpadItem);

        if (mFilterCallType == DialpadView.CALLLOG_TYPE_ALL) {
            count = dialpadItem.getSize();
        } else {
            count = dialpadItem.getCallTypeSize(mFilterCallType);
        }

        if ((position + 1) == getCount()) {
            viewHolder.mDivider.setVisibility(View.GONE);
        }
        else {
            viewHolder.mDivider.setVisibility(View.VISIBLE);
        }
        //if ((position + 1) == getCount()) {
        //    Log.i(TAG, "bindItemView: position=" + position);
        //    if (0 == position) {
        //        viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_uniline_s);
        //    }
        //    else {
        //        int preType = ((DialpadItem )getItem(position - 1)).getType();
        //        if (preType == type) {
        //            viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_bottom_s);
        //        }
        //        else {
        //            viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_uniline_s);
        //        }
        //    }
        //    viewHolder.mDivider.setVisibility(View.GONE);
        //}
        //else {
        //    int nextType = ((DialpadItem )getItem(position + 1)).getType();
        //    if (nextType != type) {
        //        Log.i(TAG, "bindItemView: position=" + position + " type=" + type);
        //        if (0 == position) {
        //            viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_uniline_s);
        //        }
        //        else {
        //            int preType = ((DialpadItem )getItem(position - 1)).getType();
        //            if (preType == type) {
        //                viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_bottom_s);
        //            }
        //            else {
        //                viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_uniline_s);
        //            }
        //        }
        //        viewHolder.mDivider.setVisibility(View.GONE);
        //    }
        //    else {
        //        if (0 == position) {
        //            viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_top_s);
        //        }
        //        else {
        //            int preType = ((DialpadItem )getItem(position - 1)).getType();
        //            if (preType == type) {
        //                viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_middle_s);
        //            }
        //            else {
        //                viewHolder.mMainArea.setBackgroundResource(R.drawable.bg_contacts_list_top_s);
        //            }
        //        }
        //        viewHolder.mDivider.setVisibility(View.VISIBLE);
        //    }
        //}

        //mPhotoLoader.loadPhoto(viewHolder.mPhoto, dialpadItem.getPhotoId());
        if (0 == contactId) {
            //viewHolder.mPhoto.setImageResource(R.drawable.ic_contact_header_unknow);
            //mPhotoLoader.loadPhoto(viewHolder.mPhoto, dialpadItem.getPhotoId(), contactId);
            mPhotoLoader.loadSpecialPhoto(viewHolder.mPhoto, number);
            new LayoutQuickContactBadge.UnknownQCBOnClickListener(
                    mContext,
                    number,
                    viewHolder.mPhoto);
        }
        else {
            mPhotoLoader.loadPhoto(viewHolder.mPhoto, dialpadItem.getPhotoId());
            viewHolder.mPhoto.setOnClickListener(null/*viewHolder.mPhoto*/);
            viewHolder.mPhoto.assignContactUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
        }

        //mNumLocationLoader.loadLocation(viewHolder.mLocation, number);
        viewHolder.mLocation.setText(location);

        if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
            number = mContext.getResources().getString(R.string.unknown);
        }
        else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
            number = mContext.getResources().getString(R.string.private_num);
        }
        else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
            number = mContext.getResources().getString(R.string.payphone);
        }

        if (FilterItem.NAME_FIELD == dialpadItem.getMatchField()) {
            SpannableStringBuilder strBuilder = new SpannableStringBuilder();
            mItemVisitor.Visit(dialpadItem, strBuilder);
            viewHolder.mDispName.setText(strBuilder, TextView.BufferType.SPANNABLE);

            hasName = true;
        }
        else {
            if (!TextUtils.isEmpty(name)) {
                hasName = true;
            }
            else {
                name = number;
            }
            viewHolder.mDispName.setText(name);
        }

        if (FilterItem.NUMBER_FIELD == dialpadItem.getMatchField()) {
            SpannableStringBuilder strBuilder = new SpannableStringBuilder();
            mItemVisitor.Visit(dialpadItem, strBuilder);
            viewHolder.mDispNumber.setText(strBuilder, TextView.BufferType.SPANNABLE);
            //viewHolder.mDispNumber.setVisibility(View.VISIBLE);
            viewHolder.mDispNumber.setVisibility(!mIsShowInFilterList ? View.GONE : View.VISIBLE);
        }
        else {
            if (hasName) {
                viewHolder.mDispNumber.setText(number);
                viewHolder.mDispNumber.setVisibility(View.VISIBLE);
            }
            else {
                viewHolder.mDispNumber.setText(null);
                viewHolder.mDispNumber.setVisibility(View.GONE);
            }
        }

        if (DialpadItem.CALLLOG_TYPE == type) {
            if (null != viewHolder.mDispCount) {
                if (count > 1) {
                    viewHolder.mDispCount.setText("(" + String.valueOf(count) + ")");
                    //viewHolder.mDispCount.setVisibility(View.VISIBLE);
                    viewHolder.mDispCount.setVisibility(!mIsShowInFilterList ? View.VISIBLE : View.GONE);
                }
                else {
                    viewHolder.mDispCount.setVisibility(View.GONE);
                }
            }

            if (null != viewHolder.mDate) {
                //String date = new SimpleDateFormat("MM/dd HH:mm").format(dialpadItem.getDate());
                String strDate;
                long timeStamp = dialpadItem.getDate().getTime();
                if (DateUtils.isToday(timeStamp)) {
                    if (DateFormat.is24HourFormat(mContext)) {
                        strDate = DateUtils.formatDateTime(
                                mContext,
                                timeStamp,
                                (DateUtils.FORMAT_24HOUR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_NO_YEAR));
                    }
                    else {
                        if (null == mTimeFormat) {
                            mTimeFormat = new SimpleDateFormat(mContext.getString(R.string.twelve_hour_time_format));
                        }
                        strDate = mTimeFormat.format(dialpadItem.getDate());
                    }
                }
                else {
                    strDate = DateUtils.formatDateTime(
                            mContext,
                            timeStamp,
                            (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
                }
                strDate = CommonMethod.trim(strDate, new char[] {' ', '\u0020'});
                viewHolder.mDate.setText(strDate);
                
                viewHolder.mDate.setVisibility(!mIsShowInFilterList ? View.VISIBLE : View.GONE);
            }

            if (null != viewHolder.mType) {
                int callType = DialpadView.CALLLOG_TYPE_ALL;
                if (mFilterCallType == DialpadView.CALLLOG_TYPE_ALL) {
                    callType = dialpadItem.getCallType();
                } else {
                    callType = mFilterCallType;
                }

                if (Calls.MISSED_TYPE == callType) {
                    viewHolder.mType.setImageResource(R.drawable.ic_log_missed);
                }
                else if (Calls.OUTGOING_TYPE == callType) {
                    viewHolder.mType.setImageResource(R.drawable.ic_log_out);
                }
                else if (Calls.INCOMING_TYPE == callType) {
                    viewHolder.mType.setImageResource(R.drawable.ic_log_in);
                }
                viewHolder.mType.setVisibility(!mIsShowInFilterList ? View.VISIBLE : View.GONE);
            }
        }
        else {
            if (null != viewHolder.mDate) {
                //viewHolder.mDate.setText("");
                //viewHolder.mDate.setVisibility(View.GONE);
            }

            if (null != viewHolder.mType) {
                //viewHolder.mType.setImageResource(0);
                //viewHolder.mType.setVisibility(View.GONE);
            }
        }
    }

    protected Object createViewHolder(View itemView, DialpadItem dialpadItem) {
        ItemViewHolder viewHolder = new ItemViewHolder();
        if (DialpadItem.CALLLOG_TYPE == dialpadItem.getType()) {
            viewHolder.mDispName   = (TextView )itemView.findViewById(R.id.txt_dpi_displayname);
            viewHolder.mDispCount  = (TextView )itemView.findViewById(R.id.txt_dpi_displaycount);
            viewHolder.mDispNumber = (TextView )itemView.findViewById(R.id.txt_dpi_displaynumber);
            viewHolder.mLocation   = (TextView )itemView.findViewById(R.id.txt_number_location);
            viewHolder.mDate       = (TextView )itemView.findViewById(R.id.txt_dpi_time);
            //viewHolder.mPhoto      = (ImageView )itemView.findViewById(R.id.img_dpi_thumnail);
            viewHolder.mPhoto      = (QuickContactBadge )itemView.findViewById(R.id.img_dpi_thumnail);
            viewHolder.mType       = (ImageView )itemView.findViewById(R.id.img_dpi_type);
            viewHolder.mDivider    = (ImageView )itemView.findViewById(R.id.img_dpi_separator);
            //viewHolder.mMainArea   = itemView.findViewById(R.id.v_dpi_item_main_area);
        }
        else {
            viewHolder.mDispName   = (TextView )itemView.findViewById(R.id.txt_dpi_displayname);
            viewHolder.mDispNumber = (TextView )itemView.findViewById(R.id.txt_dpi_displaynumber);
            viewHolder.mLocation   = (TextView )itemView.findViewById(R.id.txt_number_location);
            //viewHolder.mDate       = (TextView )itemView.findViewById(R.id.txt_dpi_time);
            //viewHolder.mPhoto      = (ImageView )itemView.findViewById(R.id.img_dpi_thumnail);
            viewHolder.mPhoto      = (QuickContactBadge )itemView.findViewById(R.id.img_dpi_thumnail);
            //viewHolder.mType       = (ImageView )itemView.findViewById(R.id.img_dpi_type);
            viewHolder.mDivider    = (ImageView )itemView.findViewById(R.id.img_dpi_separator);
            //viewHolder.mMainArea   = itemView.findViewById(R.id.v_dpi_item_main_area);
        }
        return viewHolder;
    }


    private class ItemViewHolder {
        TextView  mDispName;
        TextView  mDispCount;
        TextView  mDispNumber;
        TextView  mLocation;
        TextView  mDate;
        //ImageView mPhoto;
        QuickContactBadge mPhoto;
        ImageView mType;
        ImageView mDivider;
        //View      mMainArea;
    }
}
