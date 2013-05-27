/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import com.lewa.base.adapter.MapAdapter;
import android.content.Context;
import android.graphics.Color;
import android.view.View;

/**
 *
 * @author chenliang
 */
public class ColorInterchangedAdapter extends MapAdapter {

    public ColorInterchangedAdapter(Context context, AdaptInfo adaptInfo) {
        super(context, adaptInfo);
    }

    @Override
    protected void getViewInDetail(Object item, int position, View convertView) {
        boolean isDouble = (position + 1) % 2 == 0 && position != 0 ? true : false;
        boolean isTotalDouble = ((getCount()) % 2 == 0 && getCount() != 1) ? true : false;
        int color;
        if (!isTotalDouble) {
            color = Color.parseColor(isDouble ? "#ebebeb":"#ffffff");
        } else {
            color = Color.parseColor(isDouble ? "#ffffff":"#ebebeb" );
        }
        convertView.setBackgroundColor(color);
        super.getViewInDetail(item, position, convertView);
    }
}
