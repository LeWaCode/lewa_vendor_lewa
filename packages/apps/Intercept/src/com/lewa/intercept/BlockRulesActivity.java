package com.lewa.intercept;

import com.lewa.intercept.R;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.util.Log;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import android.database.Cursor;
import android.provider.InterceptConstants;
import android.content.ContentValues;

public class BlockRulesActivity extends Activity {
    RadioGroup mInterceptRG;
    RadioButton mBlackSmartRB;
    RadioButton mBlackListRB;
    RadioButton mWhiteListRB;
    RadioButton mContactRB;
    RadioButton mAllNumRB;

    RelativeLayout mBlackSmartRL;
    RelativeLayout mBlackListRL;
    RelativeLayout mWhiteListRL;
    RelativeLayout mContactRL;
    RelativeLayout mAllNumRL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.interceptrule);
        initUI();
    }

    private void initUI() {
        mBlackSmartRL = (RelativeLayout) findViewById(R.id.blocking_black_smart_rl);
        mBlackListRL = (RelativeLayout) findViewById(R.id.blocking_blacklist_rl);
        mWhiteListRL = (RelativeLayout) findViewById(R.id.only_accept_whitelist_rl);
        mContactRL = (RelativeLayout) findViewById(R.id.blocking_num_rl);
        mAllNumRL = (RelativeLayout) findViewById(R.id.blocking_all_rl);

        mBlackSmartRB = (RadioButton) findViewById(R.id.blocking_black_smart_rb);
        mBlackListRB = (RadioButton) findViewById(R.id.blocking_blacklist_rb);
        mWhiteListRB = (RadioButton) findViewById(R.id.only_accept_whitelist_rb);
        mContactRB = (RadioButton) findViewById(R.id.blocking_num_rb);
        mAllNumRB = (RadioButton) findViewById(R.id.blocking_all_rb);

        SharedPreferences sPreferences
                = getSharedPreferences(Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_READABLE);
        int blockMode = sPreferences.getInt(Constants.KEY_BLOCK_MODE, Constants.BLOCK_MODE_SMART);
        changeCheckBoxState(blockMode);

        // register Listener
        //Begin add by chenqiang for bug 5146,20120407
        mBlackSmartRL.setOnClickListener(mBlacmModeRBListener);
        //add End
        mBlackListRL.setOnClickListener(mBlacmModeRBListener);
        mWhiteListRL.setOnClickListener(mBlacmModeRBListener);
        mContactRL.setOnClickListener(mBlacmModeRBListener);
        mAllNumRL.setOnClickListener(mBlacmModeRBListener);
        mBlackSmartRB.setOnClickListener(mBlacmModeRBListener);
        mBlackListRB.setOnClickListener(mBlacmModeRBListener);
        mWhiteListRB.setOnClickListener(mBlacmModeRBListener);
        mContactRB.setOnClickListener(mBlacmModeRBListener);
        mAllNumRB.setOnClickListener(mBlacmModeRBListener);
    }

    private void changeCheckBoxState(int blockMode) {
        switch (blockMode) {
            case Constants.BLOCK_MODE_SMART:
                mBlackSmartRB.setChecked(true);
                mBlackListRB.setChecked(false);
                mWhiteListRB.setChecked(false);
                mContactRB.setChecked(false);
                mAllNumRB.setChecked(false);
                break;
            case Constants.BLOCK_MODE_BLACKLIST:
                mBlackSmartRB.setChecked(false);
                mBlackListRB.setChecked(true);
                mWhiteListRB.setChecked(false);
                mContactRB.setChecked(false);
                mAllNumRB.setChecked(false);
                break;
            case Constants.BLOCK_MODE_OUT_OF_WHITELIST:
                mBlackSmartRB.setChecked(false);
                mBlackListRB.setChecked(false);
                mWhiteListRB.setChecked(true);
                mContactRB.setChecked(false);
                mAllNumRB.setChecked(false);
                break;
            case Constants.BLOCK_MODE_EXCEPT_CONTACT:
                mBlackSmartRB.setChecked(false);
                mContactRB.setChecked(true);
                mBlackListRB.setChecked(false);
                mWhiteListRB.setChecked(false);
                mAllNumRB.setChecked(false);
                break;
            case Constants.BLOCK_MODE_ALLNUM:
                mBlackSmartRB.setChecked(false);
                mAllNumRB.setChecked(true);
                mContactRB.setChecked(false);
                mWhiteListRB.setChecked(false);
                mBlackListRB.setChecked(false);
                break;
            default:
                break;
        }
    }

    // Blocking blacklist radio button Listener
    private View.OnClickListener mBlacmModeRBListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.blocking_black_smart_rl:
                case R.id.blocking_black_smart_rb:
                    saveBlockModeValue(Constants.BLOCK_MODE_SMART);
                    break;
                case R.id.blocking_blacklist_rl:
                case R.id.blocking_blacklist_rb:
                    saveBlockModeValue(Constants.BLOCK_MODE_BLACKLIST);
                    break;
                case R.id.only_accept_whitelist_rl:
                case R.id.only_accept_whitelist_rb:
                    saveBlockModeValue(Constants.BLOCK_MODE_OUT_OF_WHITELIST);
                    break;
                case R.id.blocking_num_rl:
                case R.id.blocking_num_rb:
                    saveBlockModeValue(Constants.BLOCK_MODE_EXCEPT_CONTACT);
                    break;
                case R.id.blocking_all_rl:
                case R.id.blocking_all_rb:
                    saveBlockModeValue(Constants.BLOCK_MODE_ALLNUM);
                    break;
                default:
                    break;
            }
        }
    };

    private void saveBlockModeValue(int blockMode) {
        changeCheckBoxState(blockMode);
        SharedPreferences prefs
                = getSharedPreferences(Constants.SHARE_PREFERENCE_NAME, MODE_WORLD_WRITEABLE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(Constants.KEY_BLOCK_MODE, blockMode);
        editor.commit();
        Log.i("blockMode", ""+blockMode);
        Intent intent = new Intent(InterceptIntents.LEWA_INTERCEPT_SAVE_MODE_ACTION);
        intent.putExtra("blockMode", blockMode);
        sendBroadcast(intent);
        updateDataBaseDND(blockMode);
     }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mInterceptRG = null;
        mBlackSmartRB = null;
        mBlackListRB = null;
        mWhiteListRB = null;
        mContactRB = null;
        mAllNumRB = null;

        mBlackSmartRL = null;
        mBlackListRL = null;
        mWhiteListRL = null;
        mContactRL = null;
        mAllNumRL = null;
    }
    
    /**
     * Update DataBase
     */
    public void updateDataBaseDND(int mode) {
        Cursor cursor = getContentResolver().query(
                InterceptConstants.DND_CONTENT_URI, null, null, null, null);

        if (cursor != null) {
            String selection = InterceptConstants.COLUMN_BLOCK_NAME_ID + " = ?";
            ContentValues values = new ContentValues();
            values.put(InterceptConstants.COLUMN_SWITCH_MODE, mode);

            getContentResolver().update(
                     InterceptConstants.DND_CONTENT_URI, values, selection, new String[] { "1" });
            cursor.close();
            cursor = null;
        } else {
            Log.e(Constants.TAG, "table dnd doesn't exist");
        }
    }
}
