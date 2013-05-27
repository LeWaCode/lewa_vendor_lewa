/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import android.content.Context;
import com.lewa.filemanager.beans.FileInfo;

/**
 *
 * @author Administrator
 */
public class PathAdapter extends FileAdapter {

    public PathAdapter(Context context, AdaptInfo listViewHolder) {
        super(context, listViewHolder);
    }

    public FileInfo getSimpleFileItem(int pos) {
        return ((FileInfo)this.getItem(pos));
    }
}
