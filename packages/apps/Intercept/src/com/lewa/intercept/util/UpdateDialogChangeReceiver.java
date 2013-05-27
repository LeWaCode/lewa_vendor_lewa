package com.lewa.intercept.util;

import com.lewa.intercept.R;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.widget.Toast;

public class UpdateDialogChangeReceiver extends BroadcastReceiver {
    private Context mContext;
    private ProgressDialog mProgressDialog;

    public UpdateDialogChangeReceiver(Context context) {
        mContext = context;
        mProgressDialog = new ProgressDialog(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String type = intent.getStringExtra("type");
        if ("startCheck".equals(type)) {
            mProgressDialog.setMessage(context.getResources().getString(R.string.checking));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        } else if ("endCheck".equals(type)) {
            mProgressDialog.cancel();
            boolean needToUpdate = intent.getBooleanExtra("needToUpdate", false);
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            if (needToUpdate) {
                builder.setTitle(context.getResources().getString(R.string.update_title));
                builder.setMessage(context.getResources().getString(R.string.update_message));
                builder.setPositiveButton(context.getResources().getString(R.string.confirm), new OnClickListener() {
                    
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent("update_intelligence_intercept_library");
                        mContext.sendBroadcast(intent);
                    }
                });
                builder.setNegativeButton(context.getResources().getString(R.string.cancel), null);
            } else {
                builder.setMessage(context.getResources().getString(R.string.noneedToUpdate));
                builder.setPositiveButton(context.getResources().getString(R.string.confirm), null);
            }
            builder.show();
            if (!needToUpdate) {
                try {
                    mContext.unregisterReceiver(this);
                }catch(Exception e) {
                }
            }
        } else if ("startUpdate".equals(type)) {
            mProgressDialog.setMessage(context.getResources().getString(R.string.updating));
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        } else if ("endUpdate".equals(type)) {
            mProgressDialog.cancel();
            try {
                mContext.unregisterReceiver(this);
            }catch(Exception e) {
            }
            Toast.makeText(mContext, context.getResources().getString(R.string.updateSuccess), Toast.LENGTH_LONG).show();
        }
    }

}
