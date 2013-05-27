/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;
import com.lewa.app.filemanager.R;
import com.lewa.base.images.ThumbnailBase;
import com.lewa.filemanager.beans.ApkInfo;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.base.adapter.ItemDataSrc;
import com.lewa.base.DensityUtil;
import com.lewa.filemanager.beans.MusicInfo;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author chenliang
 */
public class CountAdapter extends FileAdapter {

    public CountAdapter(Context context, AdaptInfo listViewHolder) {
        super(context, listViewHolder);
    }
    public Map<Integer, Object> items = new TreeMap<Integer, Object>(new Comparator<Integer>() {

        public int compare(Integer arg0, Integer arg1) {
            return arg0 - arg1;
        }
    });

    @Override
    public Object getItem(int position) {
        if (!items.containsKey(position)) {
            items.put(position, super.getItem(position));
        }
        Object obj = items.get(position);
        if (obj == null) {
            return null;
        }
        return obj;
    }
    int apkVersionMaxWidth;

    @Override
    protected void getViewInDetail(Object item, int position, View convertView) {
        super.getViewInDetail(item, position, convertView);
        View apkVersion = convertView.findViewById(R.id.fileTimeSize);
        if (apkVersion != null) {
            apkVersionMaxWidth = item instanceof ApkInfo ? DensityUtil.dip2px(context, 105) : DensityUtil.dip2px(context, 260);
            ((TextView) apkVersion).setMaxWidth(this.apkVersionMaxWidth);
        }
    }

    public void setItemDataSrc(ItemDataSrc itemDataSrc) {
        items.clear();
        super.setItemDataSrc(itemDataSrc);
    }
    Cursor cursorTmp;

    public FileInfo getSimpleFileItem(int pos) {
        FileInfo info = null;
        try {
        cursorTmp = ((Cursor) this.getItemDataSrc().getContent());
        cursorTmp.moveToPosition(pos);
            info = new FileInfo(cursorTmp.getString(cursorTmp.getColumnIndex("_data")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    @Override
    protected ThumbnailBase getThumbnailBase(FileInfo fileInfo) {
        ThumbnailBase tb = super.getThumbnailBase(fileInfo);
        if (fileInfo instanceof MusicInfo) {
            tb.thumbnailPath = ((MusicInfo) fileInfo).thumbnail;
        }
        return tb;
    }
}
