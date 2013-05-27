package com.lewa.PIM.calllog.view;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.internal.telephony.CallerInfo;
import com.lewa.PIM.R;
import com.lewa.PIM.calllog.data.CallLog;
import com.lewa.PIM.calllog.data.CallLogGroup;
import com.lewa.PIM.contacts.LayoutQuickContactBadge;
import com.lewa.PIM.engine.PimEngine;
import com.lewa.PIM.util.CommonMethod;
import com.lewa.os.util.ContactPhotoLoader;
import com.lewa.os.util.NumberLocationLoader;
import com.lewa.os.util.Util;
import com.lewa.os.view.ListBaseAdapter;

public class CallLogListAdapter extends ListBaseAdapter<CallLogGroup> {
    //private static final String TAG = "CallLogListAdapter";
    
    private Context mContext;
    private ContactPhotoLoader mPhotoLoader;
    private NumberLocationLoader mNumLocationLoader;

    private SimpleDateFormat mTimeFormat;

    public CallLogListAdapter(
            Context context,
            List<CallLogGroup> clGroups,
            ContactPhotoLoader photoLoader,
            NumberLocationLoader numLocationLoader) {
        super(clGroups);
        mContext = context;
        mPhotoLoader = photoLoader;
        mNumLocationLoader = numLocationLoader;
    }

    @Override
    protected View createItemView(CallLogGroup clGroup, int position) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        return layoutInflater.inflate(R.layout.calllog_entry, null);
    }

    @Override
    protected void bindItemView(CallLogGroup clGroup, int position, View itemView) {
        CallLog cl = clGroup.getLog();
        String name    = cl.getName();
        String number  = cl.getNumber();
        //String date   = new SimpleDateFormat("MM/dd HH:mm a").format(cl.getDate());
        int type       = cl.getType();
        long contactId = cl.getContactId();
        int count      = clGroup.getSize();
        boolean hasName = false;

        String strDate;
        long timeStamp = cl.getDate().getTime();
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
                strDate = mTimeFormat.format(cl.getDate());
            }
        }
        else {
            strDate = DateUtils.formatDateTime(
                    mContext,
                    timeStamp,
                    (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR));
        }
        strDate = CommonMethod.trim(strDate, new char[] {' ', '\u0020'});

        ItemViewHolder viewHolder = (ItemViewHolder )getViewHolder(itemView, clGroup);

        //mPhotoLoader.loadPhoto(viewHolder.mPhoto, cl.getPhotoId());
        if (0 == contactId) {
            //viewHolder.mQuickContact.setImageResource(R.drawable.ic_contact_header_unknow);
            //mPhotoLoader.loadPhoto(viewHolder.mQuickContact, cl.getPhotoId(), contactId);
            mPhotoLoader.loadSpecialPhoto(viewHolder.mQuickContact, number);
            //viewHolder.mQuickContact.assignContactFromPhone(number, true);
            //viewHolder.mQuickContact.setOnClickListener(viewHolder.mQuickContact);
            new LayoutQuickContactBadge.UnknownQCBOnClickListener(
                    mContext,
                    number,
                    viewHolder.mQuickContact);
        }
        else {
            mPhotoLoader.loadPhoto(viewHolder.mQuickContact, cl.getPhotoId());
            viewHolder.mQuickContact.setOnClickListener(null/*viewHolder.mQuickContact*/);
            viewHolder.mQuickContact.assignContactUri(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId));
            
        }

        mNumLocationLoader.loadLocation(viewHolder.mLocation, number);

        if (number.equals(CallerInfo.UNKNOWN_NUMBER)) {
            number = mContext.getResources().getString(R.string.unknown);
        }
        else if (number.equals(CallerInfo.PRIVATE_NUMBER)) {
            number = mContext.getResources().getString(R.string.private_num);
        }
        else if (number.equals(CallerInfo.PAYPHONE_NUMBER)) {
            number = mContext.getResources().getString(R.string.payphone);
        }

        if (!TextUtils.isEmpty(name)) {
            hasName = true;
        }
        else {
            if (Util.isEmergencyNumber(number, PimEngine.getEmergencyNumbers())) {
                name = mContext.getResources().getText(R.string.emergency_number).toString();
                hasName = true;
            }
            else {
                name = number;
            }
        }
        viewHolder.mDispName.setText(name);

        if (count > 1) {
            viewHolder.mDispCount.setText("(" + String.valueOf(count) + ")");
            viewHolder.mDispCount.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.mDispCount.setVisibility(View.GONE);
        }

        if (hasName) {
            viewHolder.mDispNumber.setText(number);
            viewHolder.mDispNumber.setVisibility(View.VISIBLE);
        }
        else {
            viewHolder.mDispNumber.setText(null);
            viewHolder.mDispNumber.setVisibility(View.GONE);
        }

        viewHolder.mDate.setText(strDate);

        if (Calls.MISSED_TYPE == type) {
            viewHolder.mType.setImageResource(R.drawable.ic_log_missed);
        }
        else if (Calls.OUTGOING_TYPE == type) {
            viewHolder.mType.setImageResource(R.drawable.ic_log_out);
        }
        else if (Calls.INCOMING_TYPE == type) {
            viewHolder.mType.setImageResource(R.drawable.ic_log_in);
        }
    }

    @Override
    protected boolean isOriginalItemsFixed() {
        return true;
    }

    @Override
    protected Object createViewHolder(View itemView, CallLogGroup clGroup) {
        ItemViewHolder viewHolder = new ItemViewHolder();
        viewHolder.mDispName   = (TextView )itemView.findViewById(R.id.txt_log_displayname);
        viewHolder.mDispCount  = (TextView )itemView.findViewById(R.id.txt_log_displaycount);
        viewHolder.mDispNumber = (TextView )itemView.findViewById(R.id.txt_log_displaynumber);
        viewHolder.mLocation   = (TextView )itemView.findViewById(R.id.txt_number_location);
        viewHolder.mDate       = (TextView )itemView.findViewById(R.id.txt_log_time);
        viewHolder.mType       = (ImageView )itemView.findViewById(R.id.img_log_type);
        //viewHolder.mDivider    = (ImageView )itemView.findViewById(R.id.img_log_separator);
        //viewHolder.mMainArea   = itemView.findViewById(R.id.v_log_item_main_area);
        viewHolder.mQuickContact = (QuickContactBadge )itemView.findViewById(R.id.img_log_thumnail);
        return viewHolder;
    }


    private class ItemViewHolder {
        TextView  mDispName;
        TextView  mDispCount;
        TextView  mDispNumber;
        TextView  mLocation;
        TextView  mDate;
        ImageView mType;
        //ImageView mDivider;
        //View      mMainArea;
        QuickContactBadge mQuickContact;
    }
}