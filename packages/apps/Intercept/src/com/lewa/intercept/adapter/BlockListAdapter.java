package com.lewa.intercept.adapter;

import com.lewa.intercept.BlackMsgBubbleContentActivity;
import com.lewa.intercept.BlockCallBubbleContentAcitivity;
import com.lewa.intercept.R;

import android.bluetooth.BluetoothClass.Device;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.os.util.LocationUtil;

import android.os.Build;
import android.provider.InterceptConstants;

public class BlockListAdapter extends CursorAdapter {
    private ViewHolder mViewHolder;
    private MsgViewHolder mMsgViewHolder;
    private Context mContext;
    private Cursor mCursor;
    private CallViewHolder cCallViewHolder;

    private int layoutId;
    private int positionId = -1;

    public BlockListAdapter(Context context, Cursor cursor, int layoutResId) {
        super(context, cursor);
        this.layoutId = layoutResId;
        this.mContext = context;
        this.mCursor = cursor;
    }

    public BlockListAdapter(Context context, Cursor cursor, boolean autoRequery) {
        super(context, cursor, autoRequery);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.positionId = position;
        mCursor.moveToPosition(position);
        if(mContext.getClass().equals(BlackMsgBubbleContentActivity.class) ){
            convertView = msgBubbleContent(convertView, parent);
        }else if(mContext.getClass().equals(BlockCallBubbleContentAcitivity.class) ){
            convertView = callBubbleContent(convertView,  parent);
        }else {
            convertView = listContent(position, convertView, parent);
        } 

        bindView(convertView, mContext, mCursor);
        return convertView;
    }

    private View listContent(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(mContext, mCursor, parent);

            mViewHolder = new ViewHolder();

            mViewHolder.titleTextView = (TextView) convertView.findViewById(R.id.titleName);
            mViewHolder.summaryCause = (TextView)convertView.findViewById(R.id.summaryCause);
            mViewHolder.summaryTextView = (TextView) convertView.findViewById(R.id.summaryName);
            mViewHolder.summaryDate = (TextView) convertView.findViewById(R.id.summaryDate);
            mViewHolder.countTextView = (TextView) convertView.findViewById(R.id.blockCount);

            convertView.setTag(mViewHolder);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }

    private View msgBubbleContent(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView(mContext, mCursor, parent);

            mMsgViewHolder = new MsgViewHolder();

            mMsgViewHolder.mMsgLinearLayoutBody = (LinearLayout) convertView.findViewById(R.id.left_item);

            mMsgViewHolder.mMsgItemLayout = (LinearLayout) convertView.findViewById(R.id.left_body);

            mMsgViewHolder.mBodyTextView = (TextView) convertView.findViewById(R.id.left_text_view);

            mMsgViewHolder.mMsgTimeTextView = (TextView) convertView.findViewById(R.id.left_msg_time);
            
            mMsgViewHolder.mCardView = (ImageView) convertView.findViewById(R.id.cardimage);

            convertView.setTag(mMsgViewHolder);
        } else {
            mMsgViewHolder = (MsgViewHolder) convertView.getTag();
        }
        return convertView;
    }

    private View callBubbleContent(View convertView , ViewGroup parent){
        if (convertView == null) {
            convertView = newView(mContext, mCursor, parent);

            cCallViewHolder= new CallViewHolder();

            cCallViewHolder.cCallLinearLayoutBody= (LinearLayout) convertView.findViewById(R.id.left_item);

            cCallViewHolder.cCallItemLayout= (LinearLayout) convertView.findViewById(R.id.left_body);

            cCallViewHolder.cBodyTextView= (TextView) convertView.findViewById(R.id.title);
            
            cCallViewHolder.cCardView = (ImageView) convertView.findViewById(R.id.cardimage);

            cCallViewHolder.cCallTime= (TextView) convertView.findViewById(R.id.time);
            
            cCallViewHolder.cAddress = (TextView) convertView.findViewById(R.id.address);
            
            cCallViewHolder.cCause = (TextView) convertView.findViewById(R.id.summaryCause);

            convertView.setTag(cCallViewHolder);
        } else {
            cCallViewHolder= (CallViewHolder) convertView.getTag();
        }
        return convertView;
    }

    // added
    OnClickListener onclickListener;

    public void setOnClickListener(OnClickListener onclickListener) {
        if (onclickListener == null) {
            throw new IllegalStateException("listener is null");
        }
        this.onclickListener = onclickListener;
    }

    public static class InterceptOnClickListener implements OnClickListener {
        public CursorAdapter cursorAdapter;
        public int postion;

        public static View findItemView(View view) {
            if (view == null || view.getParent() instanceof ListView) {
                return view;
            } else {
                return findItemView((ViewGroup) view.getParent());
            }
        }

        public InterceptOnClickListener(CursorAdapter cursorAdapter) {
            this.cursorAdapter = cursorAdapter;
        }

        public void onClick(View view) {

        };
    }

    // ended
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if(mContext.getClass().equals(BlackMsgBubbleContentActivity.class) ){
            mMsgViewHolder = setMsgViewContent(context, cursor, mMsgViewHolder);
        }else if(mContext.getClass().equals(BlockCallBubbleContentAcitivity.class) ){
            cCallViewHolder = setCallViewContent(context,cursor,cCallViewHolder);
        }else {
            mViewHolder = setViewContent(context, cursor, mViewHolder);
        }
    }

    private ViewHolder setViewContent(Context context, Cursor cursor, ViewHolder holder) {
        String blockType = "";
        String title = null;
        String address = null;
        String location = "";
        String summaryText = null;
        String date = null;
        int call_count=0;
        int msg_count;
        Cursor call_countCursor;
        Cursor countCursor;
        long when;

        switch (layoutId) {
        case R.layout.block_name_list:
            switch (Integer.valueOf(
                    cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MODE)))) {
                case Constants.BLOCK_TYPE_NUMBER_CALL:
                    holder.summaryTextView.setTag(Constants.BLOCK_TYPE_NUMBER_CALL);
                    break;
                case Constants.BLOCK_TYPE_NUMBER_MSG:
                    holder.summaryTextView.setTag(Constants.BLOCK_TYPE_NUMBER_MSG);
                    break;
                case Constants.BLOCK_TYPE_NUMBER_DEFAULT:
                    holder.summaryTextView.setTag(Constants.BLOCK_TYPE_NUMBER_DEFAULT);
                    break;
                default:
                    break;
            }

            title = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));
            address = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));

//            if (address != null || !address.equals("")) {
//                location = LocationUtil.getPhoneLocation(mContext, address);
//                location = location == null ? "" : location;
//            }
            call_countCursor = mContext.getContentResolver().query(
                    InterceptConstants.CALL_CONTENT_URI, null
                    , InterceptConstants.COLUMN_CALL_ADDRESS + " = ?", new String[] { address }
                    , null);
            countCursor = mContext.getContentResolver().query(
                    InterceptConstants.MSG_CONTENT_URI, null
                    , InterceptConstants.COLUMN_MSG_ADDRESS + " = ?", new String[] { address }
                    , null);
            msg_count = countCursor.getCount();
            call_count = call_countCursor.getCount();
            location=context.getString(R.string.intercept_call_data, call_count)+"  "+context.getString(R.string.intercept_msg_data, msg_count);
//            if (!title.trim().equals("")) {
//                holder.summaryTextView.setText(address);
//                holder.summaryDate.setText(location);
//            } else {
//                title = address;
//                holder.summaryDate.setText(location);
//            }
//            
//            holder.titleTextView.setText(title);
            if (!title.trim().equals("")) {
                summaryText = address + " " + location;
//                summaryText = address;
            } else {
                title = address;
                summaryText = location;
            }

            holder.titleTextView.setText(title);
            holder.summaryTextView.setText(summaryText);
            break;
        case R.layout.white_name_list:
            title = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));
            address = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));

            if (address != null || !address.equals("")) {
                location = LocationUtil.getPhoneLocation(mContext, address);
                location = location == null ? "" : location;
            }

            if (!title.trim().equals("")) {
//                summaryText = address + " " + location;
                summaryText = address;
            } else {
                title = address;
//                summaryText = location;
            }
            holder.titleTextView.setText(title);
            holder.summaryTextView.setText(summaryText);
            break;
            case R.layout.block_call_list:
                 title = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_NAME));
                 address = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_ADDRESS));
                 location = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_LOCATION));
                 String numberAddress = null;
                 String cause = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_CAUSE));
                 
                 if (LocationUtil.getPhoneLocation(context, address) != null) {
                    numberAddress = LocationUtil.getPhoneLocation(context, address)+"("+LocationUtil.getPhoneCardType(context, address)+")";
                }else {
                    numberAddress = context.getString(R.string.unknown);
                }
                
                if (title == null || title.equals("")) {
                    title = InterceptUtil.getContactIDFromPhoneNum(mContext, address);
                    if (title == null || title.equals("")) {
                        title = address;
                    }
                }

                if (title.equals("")) {
                    title = mContext.getResources().getString(R.string.unknowPhoneNum);
                }

                call_countCursor = mContext.getContentResolver().query(
                        InterceptConstants.CALL_CONTENT_URI, null
                        , InterceptConstants.COLUMN_CALL_ADDRESS + " = ?", new String[] { address }
                        , null);
                
                call_count = call_countCursor.getCount();
                when = Long.parseLong(cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_DATE)));
                date = InterceptUtil.formatTimeStamp(mContext, when, false);
                
                if (call_count > 0) {
                    holder.countTextView.setText("(" + call_count + ")");
                }

                if (numberAddress != null && !numberAddress.isEmpty() ) {
                    summaryText = numberAddress ;
                    
                }
                
                if(InterceptConstants.COLUMN_INTERCEPT_CAUSE.equalsIgnoreCase(cause)){
                    holder.summaryCause.setText(mContext.getString(R.string.intercept_ringonce));
                }else {
                    holder.summaryCause.setText(cause);
                }
                
                holder.titleTextView.setText(title);
                holder.summaryTextView.setText(summaryText);
                holder.summaryDate.setText(InterceptUtil.deleteBlank(date));

                call_countCursor.close();
                call_countCursor = null;
                break;
            case R.layout.block_msg_list:
                title = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_NAME));
                address = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_ADDRESS));
                address = InterceptUtil.removePrefix(address);

                if (title == null || title.equals("")) {
                    title = InterceptUtil.getContactIDFromPhoneNum(mContext, address);
                    if (title.equals("")) {
                        title = address;
                    }
                }
                String body = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_BODY));
                when = Long.parseLong(cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_DATE)));
                date = InterceptUtil.formatTimeStamp(mContext, when, false);


                countCursor = mContext.getContentResolver().query(
                        InterceptConstants.MSG_CONTENT_URI, null
                        , InterceptConstants.COLUMN_MSG_ADDRESS + " = ?", new String[] { address }
                        , null);
                msg_count = countCursor.getCount();

                if (msg_count > 0) {
//                    title = title + "(" + count + ")";
                    holder.countTextView.setText("(" + msg_count + ")");
                }

                holder.titleTextView.setText(title);
                holder.titleTextView.setTag(address);
                holder.summaryTextView.setText(body);
                holder.summaryDate.setText(InterceptUtil.deleteBlank(date));

                countCursor.close();
                countCursor = null;
                break;
                
            default:
                break;
        }
        return holder;
    }

    private MsgViewHolder setMsgViewContent(Context context, Cursor cursor, MsgViewHolder holder) {
        String body = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_BODY));
        long when = cursor.getLong(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_DATE));
        holder.mBodyTextView.setText(body);
        holder.mMsgTimeTextView.setText(InterceptUtil.formatTimeStampString(context, when, false));
        if ("a60".equalsIgnoreCase(Build.DEVICE)) {
        	int simId = cursor.getInt(cursor.getColumnIndex("cardInfo"));
            if (simId == Constants.GEMINI_SIM_1) {
                holder.mCardView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sim1));
            }else if(simId == Constants.GEMINI_SIM_2){
                holder.mCardView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sim2));
            }
        }
        return holder;
    }

    private CallViewHolder setCallViewContent(Context context, Cursor cursor, CallViewHolder holder) {
        String cause = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_CAUSE));
        long when = cursor.getLong(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_DATE));
        String title = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_ADDRESS));
        String numberAddress ;
        if (LocationUtil.getPhoneLocation(context, title) != null) {
           numberAddress = LocationUtil.getPhoneLocation(context, title)+"("+LocationUtil.getPhoneCardType(context, title)+")";
        }else {
            numberAddress = context.getString(R.string.unknown);
        }
        if ("a60".equalsIgnoreCase(Build.DEVICE)) {
        	int simId = cursor.getInt(cursor.getColumnIndex("cardInfo"));
            if (simId == Constants.GEMINI_SIM_1) {
                holder.cCardView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_sim1));
            }else if(simId == Constants.GEMINI_SIM_2){
                holder.cCardView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ic_sim2));
            }
        }
        if(InterceptConstants.COLUMN_INTERCEPT_CAUSE.equalsIgnoreCase(cause)){
              holder.cCause.setText(mContext.getString(R.string.intercept_ringonce));
        }else {
              holder.cCause.setText(cause);
        }
        holder.cAddress.setText(numberAddress);
        holder.cBodyTextView.setText(title);
        holder.cCallTime.setText(InterceptUtil.formatTimeStampString(context, when, false));
        return holder;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater mLI = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;
        if(mContext.getClass().equals(BlackMsgBubbleContentActivity.class) ){
            view = mLI.inflate(R.layout.message_list_item, parent, false);
        }else if(mContext.getClass().equals(BlockCallBubbleContentAcitivity.class) ){
            view = mLI.inflate(R.layout.call_list_item, parent, false);
        }else {
            view = mLI.inflate(R.layout.block_content_item, parent, false);
        } 
        return view;
    }

    public class ViewHolder {
        TextView titleTextView;
        TextView summaryTextView;
        TextView summaryDate;
        TextView countTextView;
        TextView summaryCause;
    }

    public class MsgViewHolder {
        LinearLayout mMsgLinearLayoutBody;
        LinearLayout mMsgItemLayout;
        TextView mBodyTextView;
        ImageView mCardView;
        TextView mMsgTimeTextView;
    }

    public class CallViewHolder{
        LinearLayout cCallLinearLayoutBody;
        LinearLayout cCallItemLayout;
        TextView cBodyTextView;
        TextView cCallTime;
        TextView cCause;
        TextView cAddress;
        ImageView cCardView;
    }

    public Cursor getmCursor() {
        return mCursor;
    }

    public void setmCursor(Cursor mCursor) {
        this.mCursor = mCursor;
    }
}
