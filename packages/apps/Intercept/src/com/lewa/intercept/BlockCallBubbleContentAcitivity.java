package com.lewa.intercept;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemLongClickListener;
import com.lewa.intercept.adapter.BlockListAdapter;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.intercept.R;

import android.provider.CallLog;
import android.provider.InterceptConstants;

public class BlockCallBubbleContentAcitivity extends Activity{
    private static final int ITEM_MENU_RECOVERCVS = 0;
    private static final int ITEM_MENU_DELETE = 1;

    private String address = null;
    private String userName = null;
    private String count ;

    private TextView mTextView;
    private TextView countTextView;
    private ListView mListView;
    private int countcache;

    private BlockListAdapter mAdapter;

    private CallContentObserver cCallContentObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.compose_call_activity);
        Bundle b = this.getIntent().getExtras();

        address = b.getString("call_address");
        userName = b.getString("call_name");
        countcache = b.getInt("call_count");
        count = "("+countcache+")";

        if (TextUtils.isEmpty(userName)) {
            userName = InterceptUtil.getContactIDFromPhoneNum(this, address);
            if (userName.equals("")) {
                userName = address;
            }
        }

        mListView = (ListView) findViewById(R.id.history);
        mTextView = (TextView) findViewById(R.id.item_name);
        countTextView = (TextView)findViewById(R.id.item_count);

        InterceptUtil.initAvatarHead(BlockCallBubbleContentAcitivity.this, address);

        mTextView.setText(userName);
        countTextView.setText(count);
        initListAdapter();

        mListView.setOnItemLongClickListener(callBubbleItemLongLs);

        cCallContentObserver = new CallContentObserver(new Handler());
        this.getContentResolver().registerContentObserver(InterceptConstants.CALL_CONTENT_URI, true, cCallContentObserver);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (Constants.DBUG) {
//            Log.i(Constants.TAG, "MSG content:onResume");
//        }
//        Intent intent = new Intent(InterceptIntents.BLOCK_CLASSFY_ACTION);
//        intent.putExtra("nf_class", 2);
//        sendBroadcast(intent);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (cCallContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(cCallContentObserver);
        }

        if (mAdapter != null && mAdapter.getmCursor() != null) {
            mAdapter.getCursor().close();
        }

//        mAvatarView = null;
        mTextView = null;
        mAdapter = null;
        mListView = null;
    }

    private void initListAdapter() {
        Cursor cursor = query();
        int count = cursor.getCount();
        if (count > 0) {
            mAdapter = new BlockListAdapter(this, cursor, 0);
            mListView.setAdapter(mAdapter);
        } else {
             //mListView.setEmptyView(mEmptLayout);
             cursor.close();
             cursor = null;
        }
        // TODO:
   }

    private OnItemLongClickListener callBubbleItemLongLs = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final int const_pos = position;

            AlertDialog dialog = null;
            AlertDialog.Builder recoverBuilder =
                    new AlertDialog.Builder(BlockCallBubbleContentAcitivity.this);

            final CharSequence[] items = {
                    getResources().getString(R.string.dialog_item_recover2conversation)
                    , getResources().getString(R.string.dialog_item_delete_call) };

            recoverBuilder.setTitle(userName);
            recoverBuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    long id = BlockCallBubbleContentAcitivity
                            .this.mListView.getAdapter().getItemId(const_pos);
                    switch (item) {
                        case ITEM_MENU_RECOVERCVS:
                            callRecoverCvsById(BlockCallBubbleContentAcitivity.this, id);
                            break;
                        case ITEM_MENU_DELETE:
                            createRemoveSingleDataDialog(BlockCallBubbleContentAcitivity.this,id);
                        default:
                            break;
                    }

                }
            });
            dialog = recoverBuilder.create();
            dialog.show();
            return true;
        }
    };

    private Cursor query() {
        return getContentResolver().query(
                InterceptConstants.CALL_CONTENT_URI, null, "address = ?"
                , new String[] { address }, InterceptConstants.COLUMN_CALL_DATE + " desc ");
    }

    class CallContentObserver extends ContentObserver {
        public CallContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Cursor cursor = query();
            int countcache = cursor.getCount();
            if (countcache == 0) {
                // mListView.setEmptyView(mEmptLayout);
                cursor.close();
                cursor = null;
            } else {
                String count = "("+countcache+")";
                countTextView.setText(count);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    cursor.close();
                    cursor = null;
                } else {
                    mAdapter = new BlockListAdapter(BlockCallBubbleContentAcitivity.this, cursor, 0);
                    mListView.setAdapter(mAdapter);
                }
            }
        }
    };
    
    public  void createRemoveSingleDataDialog(Context context,final long itemId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.app_intercept_delete_title));
        builder.setMessage(context.getString(R.string.app_intercept_call_delete_context));

        builder.setPositiveButton(
                context.getString(R.string.intercept_btn_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                countcache -= 1;
                if (countcache == 0) {
                    finish();
                }
                
                Uri rowUri = ContentUris.appendId(
                        InterceptConstants.CALL_CONTENT_URI.buildUpon(), itemId).build();
                InterceptUtil.delete(
                        BlockCallBubbleContentAcitivity.this, rowUri, null, null);
                
                count = "("+countcache+")";
                countTextView.setText(count);
            }
        });

        builder.setNegativeButton(
                context.getString(R.string.intercept_btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        
        alert.show();
    }

    public void callRecoverCvsById(Context context,long id) {
        String callDate = null;
        String userNum = null;
        countcache -= 1;
        if (countcache == 0) {
                finish();
        }
        Uri rowUri = ContentUris.appendId(InterceptConstants.CALL_CONTENT_URI.buildUpon(), id).build();
        Cursor cursor = context.getContentResolver().query(rowUri, null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            userNum = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_ADDRESS));
            callDate = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_CALL_DATE));
        }
        cursor.close();
        cursor = null;


        ContentValues values = new ContentValues();
        values.put(CallLog.Calls.NUMBER, userNum);
        values.put(CallLog.Calls.DATE, callDate);
        values.put(CallLog.Calls.DURATION, "0");
        values.put(CallLog.Calls.TYPE, CallLog.Calls.MISSED_TYPE);
        values.put(CallLog.Calls.NEW, 1);// 0 read,1 not read
        context.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

        InterceptUtil.delete(context, rowUri, null, null);
        count = "("+countcache+")";
        countTextView.setText(count);
    }

}
