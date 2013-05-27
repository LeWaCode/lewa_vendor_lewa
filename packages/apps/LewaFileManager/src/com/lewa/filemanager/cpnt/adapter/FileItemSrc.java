/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import com.lewa.base.adapter.ItemDataSrc;
import com.lewa.filemanager.beans.FileInfo;
import java.util.List;

/**
 *
 * @author chenliang
 */
public class FileItemSrc extends ItemDataSrc {

    public FileItemSrc(List list) {
        super(list);
    }

    public FileItemSrc() {
    }

    @Override
    public Object getItem(int position) {
        Object src = super.getItem(position);
        if (src instanceof FileInfo) {
            ((FileInfo) src).buildFile();
        }
        return src;
    }
}
