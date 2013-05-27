/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import android.view.View;
import com.lewa.app.filemanager.ui.CommonActivity;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Administrator
 */
public class SelectedIndicator {

    public static Map<CommonActivity, Boolean> clearState = new HashMap<CommonActivity, Boolean>();

    public static void putSelected(CommonActivity activity) {
        clearState.put(activity, false);
    }

    public static void clearOthersSelectionState(CommonActivity currActivity) {
        Boolean isClear;
        CommonActivity activity = null;
        for (Entry<CommonActivity, Boolean> en : clearState.entrySet()) {
            isClear = en.getValue();
            activity = en.getKey();
            if (isClear || activity == currActivity) {
                continue;
            }
            activity.makeSelectAll(false, true);
            activity.showBottomBar(View.GONE);
            clearState.put(activity, true);
        }
    }
}
