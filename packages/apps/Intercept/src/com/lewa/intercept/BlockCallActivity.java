package com.lewa.intercept;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
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

import com.lewa.intercept.adapter.BlockListAdapter;
import com.lewa.intercept.intents.InterceptIntents;
import android.provider.InterceptConstants;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.intercept.util.ParentActivity;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.R;

public class BlockCallActivity extends ParentActivity {
    private static final int DIALOG_REMOVE_ALL_DATA = 0;
    private static final int DIALOG_BLACK_ADD_TIP = 1;

    private static final int ITEM_MENU_RECOVERCVS = 0;
    private static final int ITEM_MENU_DELETE = 1;

    private static final int DIALOG_ITEM_SEND_MESSAGE = 0;
    private static final int DIALOG_ITEM_SEND_CALL = 1;
    private static final int DIALOG_ITEM_RECOVERCVS = 2;
    private static final int DIALOG_ITEM_DELETE = 3;
    private static int DIALOG_ITEM_SEND_TO_WHITELIST;
    private static int DIALOG_ITEM_SEND_TO_BLACKLIST;
    private static boolean addFlag = false;

    private static String mNumber;

    private ListView mListView;
    private LinearLayout mEmptLayout;
    private BlockListAdapter mAdapter;
    private Button addButton;

    private CallContentObserver mCallContentObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_call_list);
        mListView = (ListView) findViewById(R.id.blockcalllist);
        mEmptLayout = (LinearLayout) findViewById(android.R.id.empty);
        addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new ButtonListener());
        initListAdapter();
        mListView.setOnItemLongClickListener(bLockCallLongLs);
        mListView.setOnItemClickListener(blockCallItemListener);

        mCallContentObserver = new CallContentObserver(new Handler());
        this.getContentResolver()
                .registerContentObserver(InterceptConstants.CALL_CONTENT_URI,
                        true, mCallContentObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(
                InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION);
        intent.putExtra("nf_class", 1);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCallContentObserver != null) {
            this.getContentResolver().unregisterContentObserver(
                    mCallContentObserver);
        }
        if (mAdapter != null && mAdapter.getmCursor() != null) {
            mAdapter.getCursor().close();
        }

        mEmptLayout = null;
        mAdapter = null;
        mListView = null;
        mAdapter = null;
    }

    private OnItemLongClickListener bLockCallLongLs = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                int position, long id) {
            TextView userText = (TextView) view.findViewById(R.id.titleName);
            String title = userText.getText().toString().trim();
            String selection = InterceptConstants.COLUMN_CALL_ID + " = ?";
            CharSequence[] item0 = {
                    getResources().getString(R.string.dialog_item_send_message),
                    getResources().getString(R.string.dialog_item_send_call),
                    getResources().getString(
                            R.string.dialog_item_recover2callconversation),
                    getResources().getString(R.string.dialog_item_delete_call),
                    getResources().getString(R.string.msg_recovery_add) };
            CharSequence[] item1 = {
                    getResources().getString(R.string.dialog_item_send_message),
                    getResources().getString(R.string.dialog_item_send_call),
                    getResources().getString(
                            R.string.dialog_item_recover2callconversation),
                    getResources().getString(R.string.dialog_item_delete_call),
                    getResources().getString(R.string.msg_recovery_add_black) };
            CharSequence[] item2 = {
                    getResources().getString(R.string.dialog_item_send_message),
                    getResources().getString(R.string.dialog_item_send_call),
                    getResources().getString(
                            R.string.dialog_item_recover2callconversation),
                    getResources().getString(R.string.dialog_item_delete_call),
                    getResources().getString(R.string.msg_recovery_add),
                    getResources().getString(R.string.msg_recovery_add_black) };
            String address = null;
            Cursor cursor = getContentResolver().query(
                    InterceptConstants.CALL_CONTENT_URI,
                    new String[] { InterceptConstants.COLUMN_CALL_ADDRESS },
                    selection, new String[] { "" + id }, null);
            if (cursor != null) {
                cursor.moveToNext();
                address = cursor
                        .getString(cursor
                                .getColumnIndex(InterceptConstants.COLUMN_CALL_ADDRESS));
                cursor.close();
            }
            
            int type = InterceptUtil.isInBlackOrWhiteList(
                    BlockCallActivity.this, address);
            mNumber = address;
            if (type == Constants.BLOCK_TYPE_BLACK) {
                showItemLongClickDialog(position, title, item0, address, type);
                DIALOG_ITEM_SEND_TO_WHITELIST = 4;
                DIALOG_ITEM_SEND_TO_BLACKLIST = 5;
            } else if (type == Constants.BLOCK_TYPE_WHITE) {
                showItemLongClickDialog(position, title, item1, address, type);
                DIALOG_ITEM_SEND_TO_WHITELIST = 5;
                DIALOG_ITEM_SEND_TO_BLACKLIST = 4;
            } else {
                showItemLongClickDialog(position, title, item2, address, type);
                DIALOG_ITEM_SEND_TO_WHITELIST = 4;
                DIALOG_ITEM_SEND_TO_BLACKLIST = 5;
            }
            return true;
        }
    };

    private void showItemLongClickDialog(final int position,
            final String title, final CharSequence[] item,
            final String address, final int type) {
        AlertDialog dialog = null;
        AlertDialog.Builder recoverBuilder = new AlertDialog.Builder(
                BlockCallActivity.this);
        final CharSequence[] items = item;
        recoverBuilder.setTitle(title);

        final long id = BlockCallActivity.this.mListView.getAdapter()
                .getItemId(position);

        final String contact_name = GetContactName.getContactName(
                BlockCallActivity.this, "", address);
        recoverBuilder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == DIALOG_ITEM_SEND_MESSAGE) {
                    Intent msgIntent = InterceptUtil.sendMessage(
                            BlockCallActivity.this, address, "");
                    startActivity(msgIntent);
                } else if (item == DIALOG_ITEM_SEND_CALL) {
                    Intent callIntent = InterceptUtil.call(
                            BlockCallActivity.this, address);
                    startActivity(callIntent);
                } else if (item == DIALOG_ITEM_RECOVERCVS) {
                    String title;
                    String toast;
                    boolean delete;
                    if (type == Constants.BLOCK_TYPE_BLACK) {
                        toast = getString(R.string.msg_recovery_delete_toast,
                                address);
                        delete = true;
                        createRecoveryDialog(address, toast, delete,
                                contact_name, id);
                    } else if(type == Constants.BLOCK_TYPE_WHITE){
                        InterceptUtil.callRecoverCvsByGroup(
                                BlockCallActivity.this, mNumber);
                    }else {
                        toast = getString(R.string.msg_recovery_add_toast,
                                address);
                        delete = false;
                        createRecoveryDialog(address, toast, delete,
                            contact_name, id);
                    }
                    
                } else if (item == DIALOG_ITEM_DELETE) {
                    InterceptUtil.createRemoveDataDialog(
                            BlockCallActivity.this, address, "call");
                } else if (item == DIALOG_ITEM_SEND_TO_WHITELIST) {
                    InterceptUtil.createWhite(BlockCallActivity.this,
                            address,type,id,contact_name);
                } else if (item == DIALOG_ITEM_SEND_TO_BLACKLIST) {
                    if (InterceptUtil.isInBlackOrWhiteList(
                            BlockCallActivity.this, address) == Constants.BLOCK_TYPE_WHITE) {
                        InterceptUtil.number = mNumber;
                        InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
                        InterceptUtil.isCloseActivity = false;
                        showDialog(DIALOG_BLACK_ADD_TIP);
                    } else {
                        InterceptUtil.addBlackNameToDB(BlockCallActivity.this,
                                "", address);
                    }
                }

            }
        });
        dialog = recoverBuilder.create();
        dialog.show();
    }

    private void initListAdapter() {
        Cursor cursor = query();
        if (cursor != null && cursor.getCount() > 0) {
            mAdapter = new BlockListAdapter(this, cursor,
                    R.layout.block_call_list);
            mListView.setAdapter(mAdapter);
        } else {
            mListView.setEmptyView(mEmptLayout);
            cursor.close();
            cursor = null;
        }
    }

    private Cursor query() {
        return getContentResolver().query(InterceptConstants.CALL_CONTENT_URI,
                null,
                " (1=1) group by " + InterceptConstants.COLUMN_CALL_ADDRESS,
                null, InterceptConstants.COLUMN_CALL_DATE + " desc");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isHaveCall()) {
            menu.findItem(R.id.menu_del_calllist).setVisible(false);
        } else {
            menu.findItem(R.id.menu_del_calllist).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_del_call, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_del_calllist:
            Cursor cursor = query();
            if (cursor != null && cursor.getCount() > 0) {
                showDialog(DIALOG_REMOVE_ALL_DATA);
            }
            cursor.close();
            cursor = null;
            break;
        case R.id.menu_setting:
            Intent intent = new Intent();
            intent.setClass(BlockCallActivity.this, BlockSettingActivity.class);
            startActivity(intent);
            break;
        case R.id.menu_update_intelligence_intercept_library:
            InterceptUtil.checkUpdate(BlockCallActivity.this);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DIALOG_REMOVE_ALL_DATA:
            return createRemoveAllDataDialog();
        case DIALOG_BLACK_ADD_TIP:
            InterceptUtil.number = mNumber;
            InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
            InterceptUtil.isCloseActivity = false;
            return InterceptUtil.createAdd2WhiteTipDialog(this);
        default:
            break;
        }

        return null;
    }

    private boolean isHaveCall() {
        boolean flag = false;
        Cursor cursor = query();
        if (cursor != null && cursor.getCount() > 0) {
            flag = true;
        }
        return flag;
    }

    private Dialog createRemoveAllDataDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(this.getString(R.string.intercept_cleardialog_title));

        String title_content = getString(R.string.intercept_cleardialog_call_content);
        String title = getString(R.string.intercept_cleardialog, title_content);

        builder.setMessage(title);

        builder.setPositiveButton(getString(R.string.intercept_btn_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InterceptUtil
                                .delete(BlockCallActivity.this,
                                        InterceptConstants.CALL_CONTENT_URI,
                                        null, null);
                        dialog.dismiss();
                    }
                });

        builder.setNegativeButton(getString(R.string.intercept_btn_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();

        return alert;
    }

    class CallContentObserver extends ContentObserver {
        public CallContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (Constants.DBUG) {
                Log.i(Constants.TAG, "CallContentObserver updateCallUIReceiver");
            }
            Cursor cursor = query();
            if (cursor != null && cursor.getCount() == 0) {
                mListView.setEmptyView(mEmptLayout);
                cursor.close();
                cursor = null;
            } else {
                if (null != mAdapter) {
                    mAdapter.notifyDataSetChanged();
                    cursor.close();
                    cursor = null;
                } else {
                    mAdapter = new BlockListAdapter(BlockCallActivity.this,
                            cursor, R.layout.block_call_list);
                    mListView.setAdapter(mAdapter);
                }
            }
        }
    };

    private void createRecoveryDialog(final String number, String toast,
            final boolean delete, final String name, final long itemId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.msg_send_recovery_title));
        builder.setMultiChoiceItems(new String[] {getString(R.string.dialog_item_add2whitelist_also)}, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which,
                            boolean isChecked) {
                        addFlag = isChecked;
                    }
                });
        builder.setPositiveButton(getString(R.string.intercept_btn_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (addFlag) {
                            addFlag = false;
                            Intent intent = null;
                            Uri rowUri = null;
                            if (delete) {
                                intent = new Intent(
                                        InterceptIntents.LEWA_INTERCEPT_DELETEBLACKFROMCACHE_ACTION);
                                intent.putExtra("number", number);
                                BlockCallActivity.this.sendBroadcast(intent);
                                rowUri = ContentUris.appendId(
                                        InterceptConstants.CONTENT_URI
                                                .buildUpon(), itemId).build();
                                InterceptUtil.delete(BlockCallActivity.this,
                                        rowUri, null, null);
                                InterceptUtil.updateWhiteOrBlack(
                                        BlockCallActivity.this, number, 2);
                            }
                            InterceptUtil.addWhiteNameToDB(
                                    BlockCallActivity.this, name, number);
                        }

                        InterceptUtil.callRecoverCvsByGroup(
                                BlockCallActivity.this, mNumber);
                    }
                });

        builder.setNegativeButton(getString(R.string.intercept_btn_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public class ButtonListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent = new Intent();
            intent.setClass(BlockCallActivity.this, BlackNameAddActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public CursorAdapter getAdapter() {
        // TODO Auto-generated method stub
        return null;
    }

    private OnItemClickListener blockCallItemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            int itemid = (int) BlockCallActivity.this.mAdapter
                    .getItemId(position);

            TextView userBlockName = (TextView) view
                    .findViewById(R.id.titleName);

            Uri rowUri = ContentUris.appendId(
                    InterceptConstants.CALL_CONTENT_URI.buildUpon(), itemid)
                    .build();
            String address = null;
            String name = null;
            int count = 0;
            Cursor cursor = getContentResolver().query(rowUri, null, null,
                    null, null);
            if (cursor != null) {
                cursor.moveToNext();
                address = cursor
                        .getString(cursor
                                .getColumnIndex(InterceptConstants.COLUMN_CALL_ADDRESS));
                name = cursor.getString(cursor
                        .getColumnIndex(InterceptConstants.COLUMN_CALL_NAME));
            }
            Cursor countCursor = BlockCallActivity.this.getContentResolver()
                    .query(InterceptConstants.CALL_CONTENT_URI, null,
                            InterceptConstants.COLUMN_CALL_ADDRESS + " = ?",
                            new String[] { address }, null);
            if (countCursor != null) {
                count = countCursor.getCount();
            }

            Bundle bundle = new Bundle();
            bundle.putString("call_address", address);
            bundle.putString("call_name", name);
            bundle.putInt("call_count", count);

            cursor.close();
            cursor = null;

            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(BlockCallActivity.this,
                    BlockCallBubbleContentAcitivity.class);
            startActivity(intent);
        }
    };
}
