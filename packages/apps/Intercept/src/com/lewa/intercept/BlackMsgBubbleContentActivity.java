package com.lewa.intercept;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.provider.InterceptConstants;
import com.lewa.intercept.R;
import com.lewa.intercept.adapter.BlockListAdapter;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.util.InterceptUtil;

public class BlackMsgBubbleContentActivity extends Activity {
    private static final int ITEM_MENU_RECOVERCVS = 0;
    private static final int ITEM_MENU_DELETE = 1;

    private String address = null;
    private String userName = null;
    private String count ;
    private int countcache;

//    private QuickContactBadge mAvatarView;
    private TextView mTextView;
    private TextView countTextView;
    private ListView mListView;

    private BlockListAdapter mAdapter;

    private MsgContentObserver mMsgContentObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.compose_message_activity);

        Bundle b = this.getIntent().getExtras();

        address = b.getString("msg_address");
        userName = b.getString("msg_name");
        countcache = b.getInt("msg_count");
        count = "("+countcache+")";

        if (TextUtils.isEmpty(userName)) {
            userName = InterceptUtil.getContactIDFromPhoneNum(this, address);
            if (userName.equals("")) {
                userName = address;
            }
        }

        mListView = (ListView) findViewById(R.id.history);
//        mAvatarView = (QuickContactBadge) findViewById(R.id.avatar);
        mTextView = (TextView) findViewById(R.id.item_name);
        countTextView = (TextView)findViewById(R.id.item_count);
        

//        InterceptUtil.initAvatarHead(BlackMsgBubbleContentActivity.this, address, mAvatarView);
        InterceptUtil.initAvatarHead(BlackMsgBubbleContentActivity.this, address);

        mTextView.setText(userName);
        countTextView.setText(count);

        initListAdapter();

        mListView.setOnItemLongClickListener(msgBubbleItemLongLs);

        mMsgContentObserver = new MsgContentObserver(new Handler());
        this.getContentResolver().registerContentObserver(InterceptConstants.MSG_CONTENT_URI, true, mMsgContentObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "MSG content:onResume");
        }
        Intent intent = new Intent(InterceptIntents.BLOCK_CLASSFY_ACTION);
        intent.putExtra("nf_class", 2);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mMsgContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(mMsgContentObserver);
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
            // mListView.setEmptyView(mEmptLayout);
            cursor.close();
            cursor = null;
        }
        // TODO:
    }

    private OnItemLongClickListener msgBubbleItemLongLs = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final int const_pos = position;

            AlertDialog dialog = null;
            AlertDialog.Builder recoverBuilder =
                    new AlertDialog.Builder(BlackMsgBubbleContentActivity.this);

            final CharSequence[] items = {
                    getResources().getString(R.string.dialog_item_recover2conversation)
                    , getResources().getString(R.string.dialog_item_delete_msg) };

            recoverBuilder.setTitle(userName);
            recoverBuilder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    long id = BlackMsgBubbleContentActivity
                            .this.mListView.getAdapter().getItemId(const_pos);
                    switch (item) {
                        case ITEM_MENU_RECOVERCVS:
                            msgRecoverCvsById(BlackMsgBubbleContentActivity.this, id);
                            break;
                        case ITEM_MENU_DELETE:
                            createRemoveSingleDataDialog(BlackMsgBubbleContentActivity.this,id);                            
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
                InterceptConstants.MSG_CONTENT_URI, null, "address = ?"
                , new String[] { address }, InterceptConstants.COLUMN_MSG_DATE + " desc ");
    }

    class MsgContentObserver extends ContentObserver {
        public MsgContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Cursor cursor = query();
            if (cursor.getCount() == 0) {
                // mListView.setEmptyView(mEmptLayout);
                cursor.close();
                cursor = null;
            } else {
                int count = cursor.getCount();
                String countcache = "("+count+")";
                countTextView.setText(countcache);
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    cursor.close();
                    cursor = null;
                } else {
                    mAdapter = new BlockListAdapter(BlackMsgBubbleContentActivity.this, cursor, 0);
                    mListView.setAdapter(mAdapter);
                }
            }
        }
    };
    
    public  void createRemoveSingleDataDialog(Context context,final long itemId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(context.getString(R.string.app_intercept_delete_title));
        builder.setMessage(context.getString(R.string.app_intercept_msg_single_delete_context));

        builder.setPositiveButton(
                context.getString(R.string.intercept_btn_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                countcache -= 1;
                if (countcache == 0) {
                        finish();
                }
                Uri rowUri = ContentUris.appendId(
                InterceptConstants.MSG_CONTENT_URI.buildUpon(), itemId).build();
                InterceptUtil.delete(
                            BlackMsgBubbleContentActivity.this, rowUri, null, null);
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
    
    public void msgRecoverCvsById(Context context, long id) {
        String userNum = null;
        String msgDate = null;
        String msgType = null;
        String msgRead = null;
        String msgBody = null;
        
        countcache -= 1;
        if (countcache == 0) {
                finish();
        }

        Uri rowUri = ContentUris.appendId(InterceptConstants.MSG_CONTENT_URI.buildUpon(), id).build();
        Cursor cursor = context.getContentResolver().query(rowUri, null, null, null, null);
        if (cursor!= null && cursor.moveToFirst()) {
            do {
                String userName = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_NAME));
                msgBody = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_BODY));
                msgDate = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_DATE));
                msgRead = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_READ));
                msgType = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_TYPE));
                if (userName.equals(null)) {
                    //TODO
                    userNum = userName;
                } else {
                    userNum = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_MSG_ADDRESS));
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        cursor = null;

        ContentValues values = new ContentValues();
        values.put("date", Long.parseLong(msgDate));
        values.put("read", InterceptUtil.STATE_READ);
        values.put("type", msgType);
        values.put("address", userNum);
        values.put("body", msgBody);
        context.getContentResolver().insert(InterceptConstants.MSG_INBOX_URI, values);

        InterceptUtil.delete(context, rowUri, null, null);
        
        count = "("+countcache+")";
        countTextView.setText(count);
    }
}
