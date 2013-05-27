/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base;

import android.content.Context;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import com.lewa.app.filemanager.ui.CommonActivity;

/**
 *
 * @author chenliang
 */
public class InputmethodUtil {

    public static void setupInputWindow(boolean visible, CommonActivity common) {
        if (visible) {
            int inputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
            common.getWindow().setSoftInputMode(inputMode);
        } else {
            InputMethodManager inputMethodManager = (InputMethodManager) common.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(common.getCurrentFocus().getWindowToken(), 2);
        }


    }

}
