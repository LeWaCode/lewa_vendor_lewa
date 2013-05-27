/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.activities.views;

import android.app.Activity;
import android.view.View;
import com.lewa.base.Logs;

/**
 *
 * @author chenliang
 */
public class Switcher {

    public static void turntoView(ViewHolder toViewHolder, View contentView, Activity activity) {

        if (toViewHolder.getView() == null) {
            toViewHolder.start();
        } else {
            Logs.i("", contentView + " ----------------->");
            if (toViewHolder.getView() != contentView) {
                contentView = toViewHolder.getView();
                activity.setContentView(contentView);
            }
            toViewHolder.dataChanged();
            toViewHolder.rebind();
        }

    }

    public static void viewFlowTo(ViewHolder toViewHolder, View contentView, Activity activity) {
        Logs.i("", " toView contentView " + contentView);//home
        Logs.i("", " toView toViewHolder.getView() " + toViewHolder.getView());//list

        if (toViewHolder.getView() == null) {
            toViewHolder.start();
            Logs.i("", " toViewHolder.getView() " + toViewHolder.getView());
        }
        if (toViewHolder.getView() != contentView) {
            contentView = toViewHolder.getView();
        }
        activity.setContentView(contentView);
        toViewHolder.dataChanged();
        toViewHolder.rebind();
    }
}
