package com.lewa.intercept;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lewa.intercept.R;
import com.lewa.intercept.adapter.BlockListAdapter;
import com.lewa.intercept.intents.InterceptIntents;
import android.provider.InterceptConstants;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.intercept.util.ParentActivity;
import com.lewa.intercept.intents.Constants;

public class BlockNameActivity extends ParentActivity {
    private static final int DIALOG_REMOVE_ALL_DATA  = 0;
    private static final int DIALOG_REMOVE_ITEM_DATA = 1;
    private static final int DIALOG_ADD_BLOCK_NAME   = 2;
    private static final int DIALOG_BLACK_ADD_TIP    = 3;
    
    private static final int DIALOG_ITEM_SEND_MESSAGE       = 0;
    private static final int DIALOG_ITEM_SEND_CALL          = 1;
    private static final int DIALOG_ITEM_ADD2WHITELIST      = 2;
    private static final int DIALOG_ITEM_MODIFY_REMARKS     = 3;
    private static final int DIALOG_ITEM_DEL_FROM_BLACKLIST = 4;

    private static final int ITEM_MENU_RECOVERCVS = 0;
    private static final int ITEM_MENU_DELETE = 1;

    private static String mBlackNumber = null;
    private static long mId = 0;

    private ListView mListView;
    private LinearLayout mEmptLayout;
    private BlockListAdapter mAdapter;
    private TextView mTextView;
    private Button addButton;

    private NameContentObserver mNameContentObserver = null;
    private MsgContentObserver mMsgContentObserver = null;
    private CallContentObserver cCallContentObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.block_name_list);

        mListView = (ListView) findViewById(R.id.lv_block_name);
        mEmptLayout = (LinearLayout) findViewById(android.R.id.empty);
        mTextView = (TextView) findViewById(R.id.txt_empty);
        mTextView.setText(getString(R.string.intercept_blocknamelist_emp));
        addButton = (Button)findViewById(R.id.addButton);
        addButton.setText(getString(R.string.app_addblock_name));
        addButton.setOnClickListener(new ButtonListener());
        initListAdapter();
        mListView.setOnItemLongClickListener(bLockNameLongLs);

        mNameContentObserver = new NameContentObserver(new Handler());
        mMsgContentObserver = new MsgContentObserver(new Handler());
        cCallContentObserver = new CallContentObserver(new Handler());
        getContentResolver().registerContentObserver(
                InterceptConstants.CONTENT_URI, true, mNameContentObserver);
        getContentResolver().registerContentObserver(
                InterceptConstants.MSG_CONTENT_URI, true, mMsgContentObserver);
        getContentResolver().registerContentObserver(
                InterceptConstants.CALL_CONTENT_URI, true, cCallContentObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNameContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(mNameContentObserver);
        }
        
        if (mMsgContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(mMsgContentObserver);
        }

        if (cCallContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(cCallContentObserver);
        }

        if (mAdapter != null && mAdapter.getmCursor() != null) {
            mAdapter.getCursor().close();
        }

        mEmptLayout = null;
        mAdapter = null;
        mListView = null;
        mAdapter = null;
    }

    private void modifyRemark(final int itemPosition,final long id) {
        int userMode = 0;
        String userName = "";
        String userNumber = "";
        String userId = "" + mListView.getAdapter().getItemId(itemPosition);

        Uri rowUri = ContentUris.appendId(InterceptConstants.CONTENT_URI.buildUpon(), id).build();
        Cursor cursor = getContentResolver().query(rowUri, null, null, null, null);
        if (cursor.moveToFirst()) {
            userNumber = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));
            userName = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));
            userMode = cursor.getInt(cursor.getColumnIndex(InterceptConstants.COLUMN_MODE));
        }
        cursor.close();
        cursor = null;

        Bundle mBundle = new Bundle();
        mBundle.putString("userId", userId);
        mBundle.putString("userName", userName);
        mBundle.putString("userNumber", userNumber);
        mBundle.putInt("userMode", userMode);
        Intent intent = new Intent(BlockNameActivity.this, BlackNameEditActivity.class);
        intent.putExtras(mBundle);
        startActivity(intent);
    };

    private Cursor query() {
        String selection = InterceptConstants.COLUMN_TYPE + "=?";
		String[] seelctionArg = new String[]{"" + Constants.BLOCK_TYPE_BLACK};
		return getContentResolver().query(InterceptConstants.CONTENT_URI, null, selection, seelctionArg, null);
    }

    public void initListAdapter() {
        Cursor cursor = query();
        if (cursor.getCount() > 0) {
            mAdapter = new BlockListAdapter(this, cursor, R.layout.block_name_list);
            mListView.setAdapter(mAdapter);
        } else {
            mListView.setEmptyView(mEmptLayout);
            cursor.close();
            cursor = null;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (!isHaveBlack()){
            menu.findItem(R.id.menu_del_blacklist).setVisible(false);
            menu.findItem(R.id.menu_add_black).setVisible(false);
        }else {
            menu.findItem(R.id.menu_del_blacklist).setVisible(true);
            menu.findItem(R.id.menu_add_black).setVisible(true);
        } 
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_black, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.menu_del_blacklist:
                Cursor cursor = query();
                if (cursor.getCount() > 0) {
                    showDialog(DIALOG_REMOVE_ALL_DATA);
                }
                cursor.close();
                cursor = null;
                break;
            case R.id.menu_add_black:
                intent = new Intent();
                intent.setClass(BlockNameActivity.this, BlackNameAddActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_setting:
                intent = new Intent();
                intent.setClass(BlockNameActivity.this, BlockSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_update_intelligence_intercept_library:
                InterceptUtil.checkUpdate(BlockNameActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_REMOVE_ALL_DATA:
                return createClearListDialog();
            case DIALOG_REMOVE_ITEM_DATA:
                // Woody Guo @ 2012/05/22
                InterceptUtil.itemId = mId;
                InterceptUtil.number = mBlackNumber;
                InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
                return InterceptUtil.createDeleteTipDialog(this);
            case DIALOG_ADD_BLOCK_NAME:
                Intent intent = new Intent();
                intent.setClass(BlockNameActivity.this, BlackNameAddActivity.class);
                startActivity(intent);
                break;
            case DIALOG_BLACK_ADD_TIP:
                InterceptUtil.number = mBlackNumber;
                InterceptUtil.type = Constants.BLOCK_TYPE_WHITE;
                InterceptUtil.isCloseActivity = false;
                return InterceptUtil.createAdd2WhiteTipDialog(this);
            default:
                break;
        }
        return null;
    }

    private boolean isHaveBlack(){
       boolean flag = false;
       String selection = InterceptConstants.COLUMN_TYPE + " = ?";
       Cursor cursor = BlockNameActivity.this.getContentResolver().query(
                InterceptConstants.CONTENT_URI
                , null
                , selection, new String[] { "" + Constants.BLOCK_TYPE_BLACK }, null);
       if (cursor.getCount() > 0){
            flag = true;
       }
       return flag;
    }

    private Dialog createClearListDialog() {
        String title_content = getString(R.string.intercept_cleardialog_block_content);
        String title = getString(R.string.intercept_cleardialog, title_content);
       
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.intercept_cleardialog_title));
        builder.setMessage(title);

        builder.setPositiveButton(getString(
                R.string.intercept_btn_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(
                        InterceptIntents.LEWA_INTERCEPT_DELETEALLBLACKFROMCACHE_ACTION);
                BlockNameActivity.this.sendBroadcast(intent);
                Log.i("yzj","send broadcaset LEWA_INTERCEPT_DELETEALLBLACKFROMCACHE_ACTION"); 
                String selection = InterceptConstants.COLUMN_TYPE + "=?";
                String[] seelctionArg = new String[]{"" + Constants.BLOCK_TYPE_BLACK};
                InterceptUtil.delete(BlockNameActivity.this, InterceptConstants.CONTENT_URI, selection, seelctionArg);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getString(
                R.string.intercept_btn_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        return alert;
    }

    private OnItemLongClickListener bLockNameLongLs = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            TextView userText = (TextView) view.findViewById(R.id.titleName);
            String userTitle = userText.getText().toString().trim();
            showItemLongClickDialog(position,userTitle);
            return true;
        }
    };
    
    private void showItemLongClickDialog(final int itemPosition,String title) {
        AlertDialog.Builder blackNameDialog = new AlertDialog.Builder(BlockNameActivity.this);
        final CharSequence[] items = {getResources().getString(R.string.dialog_item_send_message)
                ,getResources().getString(R.string.dialog_item_send_call)
                ,getResources().getString(R.string.dialog_item_send_to_whitelist)
                ,getResources().getString(R.string.dialog_item_modify_remarks)
                ,getResources().getString(R.string.dialog_item_del_from_blacklist)};
        
        final long id = BlockNameActivity.this.mListView.getAdapter().getItemId(itemPosition);
        String selection = InterceptConstants.COLUMN_BLOCK_NAME_ID + " = ?";
        Cursor cursor = BlockNameActivity.this.getContentResolver().query(
                InterceptConstants.CONTENT_URI
                , null
                , selection, new String[] { "" + id }, null);
        cursor.moveToNext();
        final String number = cursor.getString(
                cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));
        final String name = cursor.getString(
                cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));

        mBlackNumber = number;
        mId = id;
        Log.i("test","mId:"+mId);
        cursor.close();
        
        blackNameDialog.setTitle(title);
        blackNameDialog.setItems(items, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemId) {
                Intent intent = null;
                Uri rowUri = null;
                switch(itemId) {
                    case DIALOG_ITEM_SEND_MESSAGE:
                        Intent msgIntent = InterceptUtil.sendMessage(BlockNameActivity.this, number, "");
                        startActivity(msgIntent);
                        break;
                    case DIALOG_ITEM_SEND_CALL:
                        Intent callIntent = InterceptUtil.call(BlockNameActivity.this,number);
                        startActivity(callIntent);
                        break;
                    case DIALOG_ITEM_ADD2WHITELIST:
                        intent = new Intent(InterceptIntents.LEWA_INTERCEPT_DELETEBLACKFROMCACHE_ACTION);
                        intent.putExtra("number", number);
                        sendBroadcast(intent);

                        rowUri = ContentUris.appendId(InterceptConstants.CONTENT_URI.buildUpon(), id).build();
                        if (Constants.DBUG) {
                            Log.i(Constants.TAG, "rowUri:" + rowUri);
                        }
                        InterceptUtil.delete(BlockNameActivity.this, rowUri, null, null); 

                        InterceptUtil.addWhiteNameToDB(BlockNameActivity.this,name,number);  
                        break;
                    case DIALOG_ITEM_MODIFY_REMARKS:
                        modifyRemark(itemPosition,id);
                        break;
                    case DIALOG_ITEM_DEL_FROM_BLACKLIST:
                        // Woody Guo @ 2012/05/22
                        InterceptUtil.itemId = mId;
                        InterceptUtil.number = mBlackNumber;
                        InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
                        showDialog(DIALOG_REMOVE_ITEM_DATA); 
                        break;
                }
            }
        });
        blackNameDialog.show();
      }

    class NameContentObserver extends ContentObserver {
        public NameContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (Constants.DBUG) {
                Log.i(Constants.TAG, "NameContentObserver updateNameUIReceiver");
            }

            Cursor cursor = query();
            if (cursor.getCount() == 0) {
                mListView.setEmptyView(mEmptLayout);
                cursor.close();
                cursor = null;
            } else {
                if (null != mAdapter) {
                    mAdapter.notifyDataSetChanged();
                    cursor.close();
                    cursor = null;
                } else {
                    mAdapter = new BlockListAdapter(BlockNameActivity.this, cursor, R.layout.block_name_list);
                    mListView.setAdapter(mAdapter);
                }
            }
        }
    };
    
    public class ButtonListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent = new Intent();
            intent.setClass(BlockNameActivity.this, BlackNameAddActivity.class);
            startActivity(intent);
        }
        
    }
    
    public class BlockContenctObeserver extends ContentObserver{

        public BlockContenctObeserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            // TODO Auto-generated method stub
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            // TODO Auto-generated method stub
            super.onChange(selfChange);
        }
        
        
    }

    @Override
    public CursorAdapter getAdapter() {
        // TODO Auto-generated method stub
        return mAdapter;
    }

    
    class MsgContentObserver extends ContentObserver {
            public MsgContentObserver(Handler handler) {
                super(handler);
            }
    
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                Cursor blockcursor = query();
                if (blockcursor.getCount() == 0 ) {
                    mListView.setEmptyView(mEmptLayout);
                    blockcursor.close();
                    blockcursor = null;
                } else {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter = new BlockListAdapter(
                                BlockNameActivity.this, blockcursor, R.layout.block_name_list);
                        mListView.setAdapter(mAdapter);
                    }
                }
            }
        }
    
    class CallContentObserver extends ContentObserver {
        public CallContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Cursor blockcursor = query();
            if (blockcursor.getCount() == 0 ) {
                mListView.setEmptyView(mEmptLayout);
                blockcursor.close();
                blockcursor = null;
            } else {
                if (null != mAdapter) {
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter = new BlockListAdapter(
                            BlockNameActivity.this, blockcursor, R.layout.block_name_list);
                    mListView.setAdapter(mAdapter);
                }
            }
        }
    }

}
