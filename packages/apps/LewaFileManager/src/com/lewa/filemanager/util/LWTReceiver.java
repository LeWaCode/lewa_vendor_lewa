/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.util;

import android.app.Activity;
import com.lewa.filemanager.config.Constants;
import com.lewa.base.Logs;
import com.lewa.base.SharedPreferenceUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.lewa.filemanager.ds.database.copersynch.DatasrcDelete;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.app.filemanager.ui.CountActivity;
import java.io.File;

/**
 *
 * @author chenliang
 */
public class LWTReceiver extends BroadcastReceiver {

    private Activity countActivity;

    public LWTReceiver(Activity countActivity) {
        this.countActivity = countActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        if (Constants.GoToInvokeLWT.ACTION_DELETE_LWT.equals(intent.getAction())) {
        String value_lwt_deleted = (String) SharedPreferenceUtil.getValue(context, Constants.SharedPrefernce.RememberedCategory, Constants.SharedPrefernce.KEY_LWT_ISDELETED, String.class);
        Logs.i("resume ------------------------------- > "+value_lwt_deleted);
            if (value_lwt_deleted != null && !value_lwt_deleted.trim().equals("")) {
            File deletedLWT = new File(value_lwt_deleted);
            if (!deletedLWT.exists()) {
                DatasrcDelete.recursiveUpdateDel(new FileInfo(deletedLWT, context), context);
                    if (CountActivity.categoryActivity != null) {
                        CountActivity.categoryActivity.viewHolder.refresh();
                    }
                }
            SharedPreferenceUtil.putValue(context, Constants.SharedPrefernce.RememberedCategory, Constants.SharedPrefernce.KEY_LWT_ISDELETED, "");
        }
        }
    }
}
