/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.activities.views;

import android.view.View;
import com.lewa.app.filemanager.ui.CommonActivity;
/**
 *
 * @author chenliang
 */
public abstract class ViewHolder implements DataNotifier {

    protected View view;
    protected CommonActivity activity;

    public View getView() {
        return view;
    }

    public ViewHolder(View view, CommonActivity activity) {
        this.view = view;
        this.activity = activity;
    }
    public abstract void refresh();
    public abstract void start();
    public abstract void dataChanged();
    public abstract void rebind();
}
