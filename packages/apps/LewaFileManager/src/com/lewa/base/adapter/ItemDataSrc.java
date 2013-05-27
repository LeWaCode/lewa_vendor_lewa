package com.lewa.base.adapter;

import java.util.List;
import java.util.Map;

import android.database.Cursor;
import com.lewa.base.Logs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

public class ItemDataSrc {

    Map<Integer, Cursor> tailIdx_Cursor_Map = new TreeMap<Integer, Cursor>(new Comparator<Integer>() {

        public int compare(Integer arg0, Integer arg1) {
            return arg0 - arg1;
        }
    });
    List<Integer> posKeys;
    protected Cursor mCurrentCursor;
    public static final int CURSOR_TYPE = 0;
    public static final int LIST_OBJECT_TYPE = 1;
    private List list;
    private int type = -1;
    private Map<String, Class> nameTypePair;
    private int mCursorGetCount;

    public Map<String, Class> getNameTypePair() {
        return nameTypePair;
    }

    public ItemDataSrc(Cursor cursor, Map<String, Class> nameTypePair) {
        super();
        this.tailIdx_Cursor_Map.put(cursor.getCount() - 1, cursor);
        Logs.i("put " + (cursor.getCount() - 1) + " " + cursor + " " + cursor.getCount());
        setDataSrc(cursor, nameTypePair);
        mCursorGetCount += cursor.getCount();
        // TODO Auto-generated constructor stub
    }

    public void swapCursor(Cursor cursor) {
        setDataSrc(cursor, nameTypePair);
        Integer idxKey = cursor.getCount() == 0 ? -1 : (Integer) tailIdx_Cursor_Map.keySet().toArray()[tailIdx_Cursor_Map.size() - 1] + cursor.getCount();
        tailIdx_Cursor_Map.put(idxKey, cursor);
        mCursorGetCount += cursor.getCount();
    }

    public ItemDataSrc(List list) {
        super();
        setContent(list);
        // TODO Auto-generated constructor stub
    }

    public ItemDataSrc() {
        // TODO Auto-generated constructor stub
    }

    public Object getContent() {
        switch (type) {
            case CURSOR_TYPE:
                return mCurrentCursor;
            case LIST_OBJECT_TYPE:
                return list;
            default:
                return null;
        }
    }

    public void clear() {
        if (this.type == CURSOR_TYPE) {
            if (mCurrentCursor != null && !mCurrentCursor.isClosed()) {
                this.mCurrentCursor.close();
                this.mCurrentCursor = null;
            }
            for (Cursor cursor : tailIdx_Cursor_Map.values()) {
                if (!cursor.isClosed()) {
                    cursor.close();
                    cursor = null;
                }
            }
            tailIdx_Cursor_Map.clear();
        } else if (this.type == LIST_OBJECT_TYPE) {
            this.list.clear();
        }
    }

    public void setDataSrc(Cursor cursor, Map<String, Class> nameTypePair) {
        if (cursor == null) {
            throw new NullPointerException("cursor is null");
        }

        this.mCurrentCursor = cursor;
        type = CURSOR_TYPE;
        this.nameTypePair = nameTypePair;
    }

    public void setContent(List list) {

        if (list == null) {
            throw new NullPointerException("list is null");
        }
        this.list = list;
        type = LIST_OBJECT_TYPE;
    }

    public int getType() {
        return type;
    }

    public int getCount() {
        switch (type) {
            case CURSOR_TYPE:
                if (mCurrentCursor == null) {
                    return 0;
                }
                return mCursorGetCount;
            case LIST_OBJECT_TYPE:
                return list.size();
            default:
                return 0;
        }
    }

    public Object getItem(int position) {
        switch (type) {
            case CURSOR_TYPE:
                Cursor cursor = null;
                if (posKeys == null) {
                    posKeys = new ArrayList<Integer>(tailIdx_Cursor_Map.keySet());
                }
                int insertionPoint = Collections.binarySearch(posKeys, position);
                insertionPoint = insertionPoint < 0 ? -insertionPoint - 1 : insertionPoint;
                Logs.i("position " + position + " insertionPoint " + insertionPoint);
                if (insertionPoint < posKeys.size()) {
                    int posLessThankey = posKeys.get(insertionPoint);
                    cursor = tailIdx_Cursor_Map.get(posLessThankey);

                    Logs.i("put posLessThankey " + posLessThankey + " " + cursor + " " + cursor.getCount() + " " + tailIdx_Cursor_Map.size());
                    int currCount = cursor.getCount();
                    if (currCount == 0) {
                        return null;
                    }
                    Logs.i("pos insertionPoint " + insertionPoint);
                    int realPos = insertionPoint == 0 ? position : (currCount - 1 - (posLessThankey - position));
                    Logs.i("realPos " + realPos);
                    if (cursor != null && cursor.isClosed()) {
                        return cursor;
                    }
                    cursor.moveToPosition(realPos);
                }
                return cursor;
            case LIST_OBJECT_TYPE:                    
                return position<list.size()?list.get(position):null;
            default:
                return null;
        }
    }
}
