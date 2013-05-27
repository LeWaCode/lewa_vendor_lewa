/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lewa.filemanager.cpnt.adapter;

import com.lewa.base.adapter.ItemDataSrc;
import com.lewa.filemanager.beans.DatasrcTransferer;
import android.content.Context;
import android.database.Cursor;
import com.lewa.filemanager.beans.FileInfo;
import com.lewa.base.Logs;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author chenliang
 */
public class CursorItemSrc extends ItemDataSrc {

    private Class<? extends FileInfo> targetclazz;
    private Context context;
    public TextIndexer textIndexer;
    private String indexedColumnName;

    public CursorItemSrc() {
    }

    public CursorItemSrc(List list) {
        super(list);
    }

    public CursorItemSrc(Context context, Cursor cursor, Map<String, Class> nameTypePair, Class<? extends FileInfo> targetclazz) {
        this(context, cursor, nameTypePair, targetclazz, "");
    }

    public CursorItemSrc(Context context, Cursor cursor, Map<String, Class> nameTypePair, Class<? extends FileInfo> targetclazz, String indexedColumnName) {
        super(cursor, nameTypePair);
        this.targetclazz = targetclazz;
        this.context = context;
        this.indexedColumnName = indexedColumnName;
//        setTextIndexerInfo(indexedColumnName);
    }

    public Cursor getContentCursor() {
        return mCurrentCursor;
    }

    @Override
    public Object getItem(int position) {
        
        Object src = super.getItem(position);
        return src instanceof Cursor ? DatasrcTransferer.transf(context, (Cursor) src, targetclazz) : src;
    }

    public void setTextIndexerInfo(String indexedColumnName) {
        int indexedSeq = mCurrentCursor.getColumnIndex(indexedColumnName);
        if (indexedSeq < 0 || indexedSeq > mCurrentCursor.getColumnCount() - 1) {
            return;
        }
        Set<String> indexedTexts = new HashSet<String>();
        mCurrentCursor.moveToFirst();
        for (int i = 0; i < mCurrentCursor.getCount(); i++) {
            mCurrentCursor.moveToPosition(i);
            indexedTexts.add(mCurrentCursor.getString(indexedSeq));
        }
        mCurrentCursor.moveToFirst();
        this.textIndexer = new TextIndexer(mCurrentCursor, indexedSeq, indexedTexts.toArray());
    }
}
