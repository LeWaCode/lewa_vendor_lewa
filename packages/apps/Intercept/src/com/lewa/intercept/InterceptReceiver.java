package com.lewa.intercept;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lewa.intercept.intents.InterceptIntents;
import com.lewa.intercept.intents.Constants;
import com.lewa.intercept.util.InterceptUtil;
import android.util.Log;
import android.telephony.SmsMessage;
import android.os.Bundle;

public class InterceptReceiver extends BroadcastReceiver {
    public static int mNew_sms_count = 0;
    public static int mNew_call_count = 0;
    public static int contentFlag = 0;
    String content =null;
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_ACTION)) {
            if (Constants.DBUG) {
                Log.i(Constants.TAG, "action:" + InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_ACTION);
            }
            int smsCount = intent.getIntExtra("smsCount", 0);
            int callCount = intent.getIntExtra("callCount", 0);
            int cardInfo = intent.getIntExtra("cardInfo", 0);
            String number = intent.getStringExtra("number");
            mNew_sms_count = mNew_sms_count + smsCount;
            mNew_call_count = mNew_call_count + callCount;
            if (cardInfo==Constants.GEMINI_SIM_2) {
                Constants.SIMCARD_INFO=Constants.GEMINI_SIM_2;
            }else {
                Constants.SIMCARD_INFO=Constants.GEMINI_SIM_1;
            }
            if(smsCount>0) {
                content = context.getString(R.string.intercept_msg)+":"+number;
                contentFlag = Constants.INTERCEPT_SMS;
            } else if(callCount>0){
                content = context.getString(R.string.intercept_call)+":"+number;
                contentFlag = Constants.INTERCEPT_CALL;
            }
            InterceptUtil.showNotification(context, MainActivity.class, mNew_sms_count, mNew_call_count ,content);
        } else if (intent.getAction().equals(InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION)) {
            if (Constants.DBUG) {
                Log.i(Constants.TAG, "action:" + InterceptIntents.LEWA_INTERCEPT_NOTIFICATION_CLASSFY_ACTION);
            }
            int mode = intent.getExtras().getInt("nf_class");
            if (2 == mode /* from msg */) {
                mNew_sms_count = 0;
            } else if (1 == mode /* from call */) {
                mNew_call_count = 0;
            } else if (3 == mode /* from call and msg */) {
                mNew_sms_count = 0;
                mNew_call_count = 0;
            }
            InterceptUtil.showNotification(context, InterceptReceiver.class, mNew_sms_count, mNew_call_count,content);
        }
    }
}
