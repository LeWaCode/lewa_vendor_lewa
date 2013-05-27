/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.lewa.app.filemanager.R;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.base.DensityUtil;
import com.lewa.base.Logs;

/**
 *
 * @author Administrator
 */
public abstract class FileAdapter extends ThumbnailAdapter {
    
    int fileNameMaxWidth;

    public FileAdapter(Context context, AdaptInfo listViewHolder) {
        super(context, listViewHolder);
    }
    
    @Override
    protected void getViewInDetail(Object item, int position, View convertView) {
        super.getViewInDetail(item, position, convertView);
        if(item==null){
            return;
        }
        fileNameMaxWidth = ((FileInfo) item).getIsDir() ? DensityUtil.dip2px(context, 186) : DensityUtil.dip2px(context, 260);
        ((TextView) convertView.findViewById(R.id.fileNameSubFileNum)).setMaxWidth(fileNameMaxWidth);        
    }
    
    public abstract FileInfo getSimpleFileItem(int pos);
    
    public int getPathOnlyItemPos(FileInfo info) {
        
        for (int i = 0; i < this.getCount(); i++) {
            if (info.getPath().equals(this.getSimpleFileItem(i).getPath())) {
                return i;
            }
        }
        return -1;
    }
}
