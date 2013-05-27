package com.lewa.intercept;

import java.util.ArrayList;

import com.lewa.intercept.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.util.ActivityPool;
import com.lewa.intercept.util.InterceptUtil;
import com.lewa.os.ui.ViewPagerIndicator;
import com.lewa.os.ui.ViewPagerIndicatorActivity;

public class MainActivity extends ViewPagerIndicatorActivity implements ViewPagerIndicator.OnPagerSlidingListener {
    private int currentScreen = 0;
    private static final int firstScreen = 0;
    private static final int secondScreen = 1;
    private static final int thirdScreen = 2;
    private static final int fourthScreen = 3;
    //private static final int fifthScreen = 4;

    private ArrayList<StartParameter> category;

    public BlockSettingActivity bSettingActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // //force start service
        //int screenId = getIntent().getIntExtra("ifBlock", firstScreen);
        int screenId = InterceptUtil.INTENTFLAG;
        if (screenId == firstScreen) {
            currentScreen = firstScreen;
        } else if (screenId == secondScreen) {
            currentScreen = secondScreen;
        } else if (screenId == thirdScreen) {
            currentScreen = thirdScreen;
        } else if (screenId == fourthScreen) {
            currentScreen = fourthScreen;
        } 

        category = new ArrayList<StartParameter>();
        category.add(new StartParameter(BlockMsgActivity.class, null, R.string.app_blocksms_name));
        category.add(new StartParameter(BlockCallActivity.class, null, R.string.app_blockcall_name));
        category.add(new StartParameter(BlockNameActivity.class, null, R.string.app_block_name));
        //category.add(new StartParameter(BlockSettingActivity.class, null, R.string.app_setting_name));
        category.add(new StartParameter(WhiteNameListActivity.class, null, R.string.app_white_name));
        setupFlingParm(category, R.layout.settings_home, R.id.indicator, R.id.pager);
        super.setDisplayScreen(currentScreen);
        super.onCreate(savedInstanceState);

        //bSettingActivity = (BlockSettingActivity) getItemActivity(3);
        setOnTriggerPagerChange(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Constants.DBUG) {
            Log.i(Constants.TAG, "mainActivity onDestory");
        }

        category = null;
    }

    @Override
    public void onChangePagerTrigger(int position) {
        Intent intent = null;
        switch (position) {
            case firstScreen: {
                if (Constants.DBUG) {
                    Log.i(Constants.TAG, "firstScreen");
                }
                intent = new Intent(InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION);
                intent.putExtra("nf_class", 2);
                sendBroadcast(intent);
            }
            break;
            case secondScreen: {
                if (Constants.DBUG) {
                    Log.i(Constants.TAG, "secondScreen");
                }
                intent = new Intent(InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION);
                intent.putExtra("nf_class", 1);
                sendBroadcast(intent);
            }
            break;
            case thirdScreen: {
                if (Constants.DBUG) {
                    Log.i(Constants.TAG, "thirdScreen");
                }
                //bSettingActivity.refreshUI();
                //if (((ActivityPool.get(BlockNameActivity.class))).getAdapter()!=null) {
                    //((ActivityPool.get(BlockNameActivity.class))).getAdapter().notifyDataSetChanged();
                //}
                
            }
            break;
            case fourthScreen: {
                if (Constants.DBUG) {
                    Log.i(Constants.TAG, "fourthScreen");
                }
            }
            break; 
            default:
                break;
        }

    }

}
