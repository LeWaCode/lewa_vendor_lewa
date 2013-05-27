/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.base;

import android.app.Activity;
import android.content.DialogInterface;

/**
 *
 * @author chenliang
 */
public class CancelClicker implements DialogInterface.OnClickListener{
    private Activity activity;

    public CancelClicker(Activity activity) {
        this.activity = activity;
    }

    
    public void onClick(DialogInterface dialog, int arg1) {
        dialog.cancel();
        dialog.dismiss();
    }

    
}
