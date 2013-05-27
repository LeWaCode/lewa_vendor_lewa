package com.lewa.intercept;



import com.lewa.intercept.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
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

import com.lewa.intercept.BlockNameActivity.ButtonListener;
import com.lewa.intercept.adapter.BlockListAdapter;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.intercept.util.ParentActivity;

import android.provider.InterceptConstants;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class WhiteNameListActivity extends ParentActivity {

    private static final int MENU_DEL_WHITELIST       = 0;
    private static final int MENU_ADD_BY_HAND         = 1;
    private static final int MENU_SETTING             = 2;

    private static final int DIALOG_REMOVE_ALL_DATA         = 0;
    private static final int DIALOG_REMOVE_ITEM_DATA        = 1;
    private static final int DIALOG_ADD_WHITE               = 2;

    private static final int DIALOG_ITEM_SEND_MESSAGE       = 0;
    private static final int DIALOG_ITEM_SEND_CALL          = 1;
    private static final int DIALOG_ITEM_MOVE_TO_BLACKLIST  = 2;
    private static final int DIALOG_ITEM_MODIFY_REMARKS     = 3;
    private static final int DIALOG_ITEM_DEL_FROM_WHITELIST = 4;

    private static final int DIALOG_ITEM_ADD_BY_HAND        = 0;

    private static final int SEND_MESSAGER  = 0;
    private static final int CALL_CONTACT   = 1;
    private static final int MODIFY_REMARK  = 2;
    private static final int DELETE_CONTACT = 3;

    private static String mWhiteNumber = null;
    private static long mId = 0;

    private ListView mWhiteNameList;
    private LinearLayout mEmptLayout;
    private TextView mTextView;
    private Button addButton;

    private BlockListAdapter mBlockListAdapter;
    private NameContentObserver mNameContentObserver = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.white_name_list);

        mWhiteNameList = (ListView) findViewById(R.id.lv_block_name);
        mEmptLayout = (LinearLayout) findViewById(android.R.id.empty);
        mTextView = (TextView)findViewById(R.id.txt_empty);
        mTextView.setText(getResources().getString(R.string.intercept_white_namelist_emp));
        addButton = (Button)findViewById(R.id.addButton);
        addButton.setText(getString(R.string.app_addwhite_name));
        addButton.setOnClickListener(new ButtonListener());
        initListAdapter();
        mWhiteNameList.setOnItemLongClickListener(whiteNameLongLs);

        mNameContentObserver = new NameContentObserver(new Handler());
        getContentResolver().registerContentObserver(
                InterceptConstants.CONTENT_URI, true, mNameContentObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mNameContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(mNameContentObserver);
        }

        if (mBlockListAdapter != null && mBlockListAdapter.getmCursor() != null) {
            mBlockListAdapter.getCursor().close();
        }

        mEmptLayout = null;
        mBlockListAdapter = null;
        mWhiteNameList = null;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (!isHaveWhite()){
            menu.findItem(R.id.menu_del_whitelist).setVisible(false);
            menu.findItem(R.id.menu_add_white).setVisible(false);
        }else {
            menu.findItem(R.id.menu_del_whitelist).setVisible(true);
            menu.findItem(R.id.menu_add_white).setVisible(true);
        } 
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_white, menu);
        return true;
    }
            
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_del_whitelist:
                Cursor cursor = query();
                if (cursor.getCount() > 0) {
                    showDialog(DIALOG_REMOVE_ALL_DATA);
                }
                cursor.close();
                cursor = null;
                break;
            case R.id.menu_add_white:
                showDialog(DIALOG_ADD_WHITE);
                break;
            case R.id.menu_setting:
                Intent intent = new Intent();
                intent.setClass(WhiteNameListActivity.this, BlockSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_update_intelligence_intercept_library:
                InterceptUtil.checkUpdate(WhiteNameListActivity.this);
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
                InterceptUtil.number = mWhiteNumber;
                InterceptUtil.type = Constants.BLOCK_TYPE_WHITE;
                return InterceptUtil.createDeleteTipDialog(this);
            case DIALOG_ADD_WHITE:
                //return showAddWhiteDialog(); liuyong
                Intent intent = new Intent();
                intent.setClass(WhiteNameListActivity.this, WhiteNameAddActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return null;
    }

     private boolean isHaveWhite(){
       boolean flag = false;
        String selection = InterceptConstants.COLUMN_TYPE + " = ?";
        Cursor cursor = WhiteNameListActivity.this.getContentResolver().query(
                InterceptConstants.CONTENT_URI
                , null
                , selection, new String[] { "" + Constants.BLOCK_TYPE_WHITE }, null);
        if (cursor.getCount() > 0){
            flag = true;
        }
       return flag;
    }


    private Dialog showAddWhiteDialog() {
        AlertDialog.Builder mAddWhiteNameDialog = new AlertDialog.Builder(WhiteNameListActivity.this);
        final CharSequence[] items = {getResources().getString(R.string.add_name_by_hand)};
        mAddWhiteNameDialog.setTitle(R.string.white_adddialog_title);
        mAddWhiteNameDialog.setItems(items, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemId) {
                Intent intent = null;
                switch(itemId) {
                    case DIALOG_ITEM_ADD_BY_HAND:
                        intent = new Intent();
                        intent.setClass(WhiteNameListActivity.this, WhiteNameAddActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }
        });
        AlertDialog alert = mAddWhiteNameDialog.create();
        return alert;
    }

    private Dialog createClearListDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.getString(R.string.app_white_name));

        String title_content = getString(R.string.app_white_name);
        String title = getString(R.string.intercept_cleardialog, title_content);

        builder.setMessage(title);

        builder.setPositiveButton(getString(
                R.string.intercept_btn_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(
                        InterceptIntents.LEWA_INTERCEPT_DELETEALLWHITEFROMCACHE_ACTION);
                WhiteNameListActivity.this.sendBroadcast(intent);

                Log.i("yzj","send the broadcast LEWA_INTERCEPT_DELETEALLWHITEFROMCACHE_ACTION");
                String selection = InterceptConstants.COLUMN_TYPE + "=?";
                String[] selectionArg = new String[]{"" + Constants.BLOCK_TYPE_WHITE};
                InterceptUtil.delete(WhiteNameListActivity.this, InterceptConstants.CONTENT_URI, selection, selectionArg);
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


    private void showItemLongClickDialog(final int itemPosition,String title) {
        AlertDialog.Builder mAddWhiteNameDialog = new AlertDialog.Builder(WhiteNameListActivity.this);
        final CharSequence[] items = {getResources().getString(R.string.dialog_item_send_message)
                ,getResources().getString(R.string.dialog_item_send_call)
                ,getResources().getString(R.string.dialog_item_send_to_blacklist)
                ,getResources().getString(R.string.dialog_item_modify_remarks)
                ,getResources().getString(R.string.dialog_item_del_from_whitelist)};
        
        final long id = WhiteNameListActivity.this.mWhiteNameList.getAdapter().getItemId(itemPosition);
        String selection = InterceptConstants.COLUMN_BLOCK_NAME_ID + " = ?";
//        Cursor cursor = this.getContentResolver().query(
//                InterceptConstants.CONTENT_URI
//                , new String[] { InterceptConstants.COLUMN_NUMBER }
//                , selection, new String[] { "" + id }, null);
        Cursor cursor = WhiteNameListActivity.this.getContentResolver().query(
                InterceptConstants.CONTENT_URI
                , null
                , selection, new String[] { "" + id }, null);
        cursor.moveToNext();
        final String number = cursor.getString(
                cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));
        final String name = cursor.getString(
                cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));
        mWhiteNumber = number;
        mId = id;
        Log.i("test","mId:"+mId);
        cursor.close();
        
        mAddWhiteNameDialog.setTitle(title);
        mAddWhiteNameDialog.setItems(items, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int itemId) {
                Intent intent = null;
                Uri rowUri = null;
                switch(itemId) {
                    case DIALOG_ITEM_SEND_MESSAGE:
                        Intent msgIntent = InterceptUtil.sendMessage(WhiteNameListActivity.this, number, "");
                        startActivity(msgIntent);
                        break;
                    case DIALOG_ITEM_SEND_CALL:
                        Intent callIntent = InterceptUtil.call(WhiteNameListActivity.this,number);
                        startActivity(callIntent);
                        break;
                    case DIALOG_ITEM_MOVE_TO_BLACKLIST:
//                        if (InterceptUtil.isInBlackOrWhiteList(WhiteNameListActivity.this,number) == Constants.BLOCK_TYPE_WHITE){
//                            InterceptUtil.number = mWhiteNumber;
//                            System.out.println("mWhiteNumber = "+mWhiteNumber);
//                            InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
//                            InterceptUtil.isCloseActivity = false;
//                            showDialog(DIALOG_BLACK_ADD_TIP); 
//                          }else {
//                            InterceptUtil.addBlackNameToDB(WhiteNameListActivity.this,"",number);
//                          }
//                        break;
                        intent = new Intent(InterceptIntents.LEWA_INTERCEPT_DELETEWHITEFROMCACHE_ACTION);
                        intent.putExtra("number", number);
                        sendBroadcast(intent);

                        rowUri = ContentUris.appendId(InterceptConstants.CONTENT_URI.buildUpon(), id).build();
                        
                        InterceptUtil.delete(WhiteNameListActivity.this, rowUri, null, null); 
                        InterceptUtil.addBlackNameToDB(WhiteNameListActivity.this,name,mWhiteNumber);  
                        break;
                    case DIALOG_ITEM_MODIFY_REMARKS:
                        modifyRemark (itemPosition,id);
                        break;
                    case DIALOG_ITEM_DEL_FROM_WHITELIST:
                        // Woody Guo @ 2012/05/22
                        InterceptUtil.itemId = mId;
                        InterceptUtil.number = mWhiteNumber;
                        InterceptUtil.type = Constants.BLOCK_TYPE_WHITE;
                        showDialog(DIALOG_REMOVE_ITEM_DATA);
                        break;
                    default:
                        break;
                }
            }
        });
        mAddWhiteNameDialog.show();
      }

      private OnItemLongClickListener whiteNameLongLs = new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                TextView userText = (TextView) view.findViewById(R.id.titleName);
                String userTitle = userText.getText().toString().trim();
                showItemLongClickDialog(position,userTitle);
                return true;
            }

       };

    private void modifyRemark(final int itemPosition,final long id) {
        int userMode = 0;
        String userName = "";
        String userNumber = "";
        String userId = "" + mWhiteNameList.getAdapter().getItemId(itemPosition);

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
        Intent intent = new Intent(WhiteNameListActivity.this, WhiteNameEditActivity.class);
        intent.putExtras(mBundle);
        startActivity(intent);
    };

    private void initListAdapter() {
        Cursor cursor = query();
        if (cursor.getCount() > 0) {
            mBlockListAdapter = new BlockListAdapter(this, cursor, R.layout.white_name_list);
            mWhiteNameList.setAdapter(mBlockListAdapter);
        } else {
            mWhiteNameList.setEmptyView(mEmptLayout);
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }

    private Cursor query() {
        String selection = InterceptConstants.COLUMN_TYPE + "=?";
        String[] seelctionArg = new String[] { "" + Constants.BLOCK_TYPE_WHITE };
        return getContentResolver().query(InterceptConstants.CONTENT_URI, null, selection, seelctionArg, null);
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
                mWhiteNameList.setEmptyView(mEmptLayout);
                cursor.close();
                cursor = null;
            } else {
                if (null != mBlockListAdapter) {
                    mBlockListAdapter.notifyDataSetChanged();
                    cursor.close();
                    cursor = null;
                } else {
                    mBlockListAdapter = new BlockListAdapter(WhiteNameListActivity.this, cursor, R.layout.white_name_list);
                    mWhiteNameList.setAdapter(mBlockListAdapter);
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
            intent.setClass(WhiteNameListActivity.this, WhiteNameAddActivity.class);
            startActivity(intent);
        }
        
    

    }
    @Override
    public CursorAdapter getAdapter() {
        // TODO Auto-generated method stub
        return null;
    }
    }
