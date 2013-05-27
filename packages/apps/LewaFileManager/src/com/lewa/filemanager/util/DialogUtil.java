package com.lewa.filemanager.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

public class DialogUtil {
    public static class DialogAbstract {
        public String title;
        public int iconId;
        public String message;
        public String positiveButtonText;
        public DialogInterface.OnClickListener positiveButtonClickListener;
        public String negativeButtonText;
        public DialogInterface.OnClickListener negativeButtonClickListener;
        public String neutralButtonText;
        public DialogInterface.OnClickListener neutralButtonClickListener;
        public View view;
        public Context context;
    }

    public static DialogInterface.OnClickListener getNewCancelOption(
    		Context activity) {
        return getNewCancelOption(activity, false);
    }

    public static DialogInterface.OnClickListener getNewCancelOption(
            final Context activity, final boolean destroyActivity) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                dialog.dismiss();
               
            }
        };
    }

    

    public static AlertDialog showChoiceDialog(DialogAbstract abtract) {

        AlertDialog dialog = showChoiceDialog(abtract,false);
        return dialog;
    }
    public static AlertDialog showChoiceDialog(DialogAbstract abtract,boolean cancelable) {
    	if(abtract.context== null){
    		throw new IllegalStateException("contxet is null");
    	}
        Builder dialog = new AlertDialog.Builder(abtract.context)
                .setCancelable(cancelable)
                .setPositiveButton(abtract.positiveButtonText,
                
                        abtract.positiveButtonClickListener)
                .setNegativeButton(abtract.negativeButtonText,
                        abtract.negativeButtonClickListener);
        if(abtract.title!=null){
        	dialog.setTitle(abtract.title);
        }
        if(abtract.iconId!=0){
        	dialog.setIcon(abtract.iconId);
        }
        if(abtract.message!=null){
        	dialog.setMessage(abtract.message);
        }
        return dialog.show();
    }
    public static AlertDialog showNeutralDialog(DialogAbstract abtract) {
        return showNeutralDialog(abtract, false);
    }

    public static AlertDialog showNeutralDialog(DialogAbstract abtract,
            boolean cancable) {
        return new AlertDialog.Builder(abtract.context)
                .setTitle(abtract.title)
                .setIcon(abtract.iconId)
                .setMessage(abtract.message)
                .setNeutralButton(abtract.neutralButtonText,
                        abtract.neutralButtonClickListener)
                .setCancelable(cancable).setView(abtract.view).show();
    }

    public void closeHintNBackToLauncher(DialogInterface dialog, Context context) {
        dialog.dismiss();
        dialog.cancel();
        ((Activity) context).finish();
    }
}
