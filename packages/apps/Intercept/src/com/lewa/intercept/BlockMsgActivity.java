package com.lewa.intercept;

import com.lewa.intercept.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
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
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import android.provider.InterceptConstants;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.intercept.util.ParentActivity;

public class BlockMsgActivity extends ParentActivity {
    private static final int DIALOG_REMOVE_ALL_DATA = 0;
    private static final int DIALOG_BLACK_ADD_TIP = 1;

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

    private MsgContentObserver mMsgContentObserver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.block_msg_list);
        mListView = (ListView) findViewById(R.id.blocksmslist);
        mEmptLayout = (LinearLayout) findViewById(android.R.id.empty);
        initListAdapter();
        mListView.setOnItemLongClickListener(blockMsgLongLs);
        mListView.setOnItemClickListener(blockMsgItemListener);
        addButton = (Button) findViewById(R.id.addButton);
        addButton.setOnClickListener(new ButtonListener());
        mMsgContentObserver = new MsgContentObserver(new Handler());
        getContentResolver().registerContentObserver(
                InterceptConstants.MSG_CONTENT_URI, true, mMsgContentObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(
                InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION);
        intent.putExtra("nf_class", 2);
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMsgContentObserver != null) {
            getContentResolver().unregisterContentObserver(mMsgContentObserver);
        }

        if (mAdapter != null && mAdapter.getmCursor() != null) {
            mAdapter.getCursor().close();
        }

        mEmptLayout = null;
        mAdapter = null;
        mListView = null;
    }

    private void initListAdapter() {
        Cursor cursor = query();
        int count = cursor.getCount();
        if (count > 0) {
            mAdapter = new BlockListAdapter(this, cursor,
                    R.layout.block_msg_list);
            mListView.setAdapter(mAdapter);
        } else {
            mListView.setEmptyView(mEmptLayout);
            cursor.close();
            cursor = null;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isHaveMsg()) {
            menu.findItem(R.id.menu_del_msglist).setVisible(false);
        } else {
            menu.findItem(R.id.menu_del_msglist).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu_del_msg, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_del_msglist:
            Cursor cursor = query();
            if (cursor.getCount() > 0) {
                showDialog(DIALOG_REMOVE_ALL_DATA);
            }
            cursor.close();
            cursor = null;
            break;
        case R.id.menu_setting:
            Intent intent = new Intent();
            intent.setClass(BlockMsgActivity.this, BlockSettingActivity.class);
            startActivity(intent);
            break;
        case R.id.menu_update_intelligence_intercept_library:
            InterceptUtil.checkUpdate(BlockMsgActivity.this);
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isHaveMsg() {
        boolean flag = false;
        Cursor cursor = query();
        int count = cursor.getCount();
        if (count > 0) {
            flag = true;
        }
        return flag;
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

    private Dialog createRemoveAllDataDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(this.getString(R.string.intercept_cleardialog_title));

        String titleContent = getString(R.string.intercept_cleardialog_msg_content);
        String title = getString(R.string.intercept_cleardialog, titleContent);

        builder.setMessage(title);

        builder.setPositiveButton(getString(R.string.intercept_btn_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        InterceptUtil.delete(BlockMsgActivity.this,
                                InterceptConstants.MSG_CONTENT_URI, null, null);
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

    private void createSendMsgDialog(final String number) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.msg_send_recovery_title));
        builder.setMessage(getString(R.string.msg_send_recovery_toast, number));
        builder.setPositiveButton(getString(R.string.intercept_btn_confirm),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        InterceptUtil.msgRecoverCvsByGroup(
                                BlockMsgActivity.this, number);
                        Intent msgIntent = InterceptUtil.sendMessage(
                                BlockMsgActivity.this, number, "");
                        startActivity(msgIntent);
                    }
                });

        builder.setNegativeButton(getString(R.string.intercept_btn_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        Intent msgIntent = InterceptUtil.sendMessage(
                                BlockMsgActivity.this, number, "");
                        startActivity(msgIntent);
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createRecoveryDialog(final String number, String title,
            String toast, final boolean delete, final String name,
            final long itemId) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(getString(R.string.msg_send_recovery_title));
        builder.setMultiChoiceItems(
                new String[] { getString(R.string.dialog_item_add2whitelist_also) }, null,
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
                                BlockMsgActivity.this.sendBroadcast(intent);
                                rowUri = ContentUris.appendId(
                                        InterceptConstants.CONTENT_URI
                                                .buildUpon(), itemId).build();
                                InterceptUtil.delete(BlockMsgActivity.this,
                                        rowUri, null, null);
                                InterceptUtil.updateWhiteOrBlack(
                                        BlockMsgActivity.this, number, 2);
                            }
                            InterceptUtil.addWhiteNameToDB(BlockMsgActivity.this,
                                name, number);
                        }
                        InterceptUtil.msgRecoverCvsByGroup(
                                    BlockMsgActivity.this, number);

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

    private OnItemClickListener blockMsgItemListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            int itemid = (int) BlockMsgActivity.this.mAdapter
                    .getItemId(position);
            String address = null;
            String name = null;
            int count = 0;
            TextView userBlockName = (TextView) view
                    .findViewById(R.id.titleName);

            Uri rowUri = ContentUris.appendId(
                    InterceptConstants.MSG_CONTENT_URI.buildUpon(), itemid)
                    .build();
            Cursor cursor = getContentResolver().query(rowUri, null, null,
                    null, null);
            if (cursor != null) {
                cursor.moveToNext();
                address = cursor.getString(cursor
                        .getColumnIndex(InterceptConstants.COLUMN_MSG_ADDRESS));
                name = cursor.getString(cursor
                        .getColumnIndex(InterceptConstants.COLUMN_MSG_NAME));
            }

            Cursor countCursor = BlockMsgActivity.this.getContentResolver()
                    .query(InterceptConstants.MSG_CONTENT_URI, null,
                            InterceptConstants.COLUMN_MSG_ADDRESS + " = ?",
                            new String[] { address }, null);
            if (countCursor != null) {
                count = countCursor.getCount();
            }

            Bundle bundle = new Bundle();
            bundle.putString("msg_address", address);
            bundle.putString("msg_name", name);
            bundle.putInt("msg_count", count);

            cursor.close();
            cursor = null;

            Intent intent = new Intent();
            intent.putExtras(bundle);
            intent.setClass(BlockMsgActivity.this,
                    BlackMsgBubbleContentActivity.class);
            startActivity(intent);
        }
    };

    private OnItemLongClickListener blockMsgLongLs = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                int position, long id) {
            final int const_pos = position;
            TextView userText = (TextView) view.findViewById(R.id.titleName);
            String title = userText.getText().toString().trim();
            String selection = InterceptConstants.COLUMN_MSG_ID + " = ?";
            String number = null;

            CharSequence[] item0 = {
                    getResources().getString(R.string.dialog_item_send_message),
                    getResources().getString(R.string.dialog_item_send_call),
                    getResources().getString(R.string.dialog_item_recover2conversation),
                    getResources().getString(R.string.dialog_item_delete_call),
                    getResources().getString(R.string.msg_recovery_add) };
            CharSequence[] item1 = {
                    getResources().getString(R.string.dialog_item_send_message),
                    getResources().getString(R.string.dialog_item_send_call),
                    getResources().getString(R.string.dialog_item_recover2conversation),
                    getResources().getString(R.string.dialog_item_delete_call),
                    getResources().getString(R.string.msg_recovery_add_black) };
            CharSequence[] item2 = {
                    getResources().getString(R.string.dialog_item_send_message),
                    getResources().getString(R.string.dialog_item_send_call),
                    getResources().getString(R.string.dialog_item_recover2conversation),
                    getResources().getString(R.string.dialog_item_delete_call),
                    getResources().getString(R.string.msg_recovery_add),
                    getResources().getString(R.string.msg_recovery_add_black) };

            Cursor cursor = getContentResolver().query(
                    InterceptConstants.MSG_CONTENT_URI,
                    new String[] { InterceptConstants.COLUMN_MSG_ADDRESS },
                    selection, new String[] { "" + id }, null);
            if (cursor != null) {
                cursor.moveToNext();
                number = cursor.getString(cursor
                        .getColumnIndex(InterceptConstants.COLUMN_MSG_ADDRESS));
                cursor.close();
            }
            mNumber = number;
            int type = InterceptUtil.isInBlackOrWhiteList(
                    BlockMsgActivity.this, number);
            mNumber = number;
            if (type == Constants.BLOCK_TYPE_BLACK) {
                showItemLongClickDialog(position, title, item0, number, type);
                DIALOG_ITEM_SEND_TO_WHITELIST = 4;
                DIALOG_ITEM_SEND_TO_BLACKLIST = 5;
            } else if (type == Constants.BLOCK_TYPE_WHITE) {
                showItemLongClickDialog(position, title, item1, number, type);
                DIALOG_ITEM_SEND_TO_WHITELIST = 5;
                DIALOG_ITEM_SEND_TO_BLACKLIST = 4;
            } else {
                showItemLongClickDialog(position, title, item2, number, type);
                DIALOG_ITEM_SEND_TO_WHITELIST = 4;
                DIALOG_ITEM_SEND_TO_BLACKLIST = 5;
            }
            return true;
        }
    };

    private void showItemLongClickDialog(final int position,
            final String title, final CharSequence[] item, final String number,
            final int type) {
        AlertDialog dialog = null;
        AlertDialog.Builder recoverBuilder = new AlertDialog.Builder(
                BlockMsgActivity.this);
        final CharSequence[] items = item;
        recoverBuilder.setTitle(title);
        final long id = BlockMsgActivity.this.mListView.getAdapter().getItemId(
                position);
        final String contact_name = GetContactName.getContactName(
                BlockMsgActivity.this, "", number);
        recoverBuilder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == DIALOG_ITEM_SEND_MESSAGE) {
                    createSendMsgDialog(number);
                } else if (item == DIALOG_ITEM_SEND_CALL) {
                    Intent callIntent = InterceptUtil.call(
                            BlockMsgActivity.this, number);
                    startActivity(callIntent);
                } else if (item == DIALOG_ITEM_RECOVERCVS) {
                    String title;
                    String toast;
                    boolean delete;
                    if (type == Constants.BLOCK_TYPE_BLACK) {
                        toast = getString(R.string.msg_recovery_delete_toast,
                                number);
                        title = getString(R.string.msg_recovery_delete);
                        delete = true;
                        createRecoveryDialog(number, title, toast, delete,
                                contact_name, id);
                    } else if (type == Constants.BLOCK_TYPE_WHITE) {
                        InterceptUtil.msgRecoverCvsByGroup(
                            BlockMsgActivity.this, number);
                    }else {
                        toast = getString(R.string.msg_recovery_add_toast,
                                number);
                        title = getString(R.string.msg_recovery_add);
                        delete = false;
                        createRecoveryDialog(number, title, toast, delete,
                                contact_name, id);
                    }
                } else if (item == DIALOG_ITEM_DELETE) {
                    InterceptUtil.createRemoveDataDialog(BlockMsgActivity.this,
                            number, "msg");
                } else if (item == DIALOG_ITEM_SEND_TO_WHITELIST) {
                    
                    InterceptUtil.createWhite(BlockMsgActivity.this, number,type,id,contact_name);
                } else if (item == DIALOG_ITEM_SEND_TO_BLACKLIST) {
                    if (InterceptUtil.isInBlackOrWhiteList(
                            BlockMsgActivity.this, number) == Constants.BLOCK_TYPE_WHITE) {
                        InterceptUtil.number = mNumber;
                        InterceptUtil.type = Constants.BLOCK_TYPE_BLACK;
                        InterceptUtil.isCloseActivity = false;
                        showDialog(DIALOG_BLACK_ADD_TIP);
                    } else {
                        InterceptUtil.addBlackNameToDB(BlockMsgActivity.this,
                                "", number);
                    }
                }
            }
        });
        dialog = recoverBuilder.create();
        dialog.show();
    }

    class MsgContentObserver extends ContentObserver {
        public MsgContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Cursor cursor = query();
            if (cursor != null && cursor.getCount() == 0) {
                mListView.setEmptyView(mEmptLayout);
                cursor.close();
                cursor = null;
            } else {
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                    cursor.close();
                    cursor = null;
                } else {
                    mAdapter = new BlockListAdapter(BlockMsgActivity.this,
                            cursor, R.layout.block_msg_list);
                    mListView.setAdapter(mAdapter);
                }
            }
        }
    };

    private Cursor query() {
        return getContentResolver().query(InterceptConstants.MSG_CONTENT_URI,
                null,
                " (1=1) group by " + InterceptConstants.COLUMN_MSG_ADDRESS,
                null, InterceptConstants.COLUMN_MSG_DATE + " desc");
    }

    public class ButtonListener implements android.view.View.OnClickListener {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            intent = new Intent();
            intent.setClass(BlockMsgActivity.this, BlackNameAddActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public CursorAdapter getAdapter() {
        // TODO Auto-generated method stub
        return null;
    }


}
