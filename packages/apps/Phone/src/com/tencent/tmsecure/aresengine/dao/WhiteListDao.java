package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.InterceptConstants;

import com.tencent.tmsecure.module.aresengine.ContactEntity;
import com.tencent.tmsecure.module.aresengine.IContactDao;

public class WhiteListDao implements IContactDao<ContactEntity> {
    private static List<ContactEntity> mWhiteList = new ArrayList<ContactEntity>();
    private static WhiteListDao mWhiteListDao;
    private Context mContext;
    private Uri uri = InterceptConstants.CONTENT_URI;

    private WhiteListDao(Context context) {
        mContext = context;
        initFromStaticData();
    }

    public static WhiteListDao getInstance(Context context) {
        if (null == mWhiteListDao) {
            synchronized (WhiteListDao.class) {
                mWhiteListDao = new WhiteListDao(context);
            }
        }
        return mWhiteListDao;
    }

    protected void initFromStaticData() {
        mWhiteList.clear();
        String selection = InterceptConstants.COLUMN_TYPE + "=" + InterceptConstants.BLOCK_TYPE_WHITE ;
        Cursor cursor = mContext.getContentResolver().query(uri, null, selection, null, null);
        while (cursor != null && cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));
           // if (name == null || "".equals(name)) {
          //      name = number;
          //  }
            ContactEntity entity = new ContactEntity();
            entity.phonenum = number;
            entity.name = name;
            mWhiteList.add(entity);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public boolean contains(String phonenum, int flags) {
        return DaoHelper.contains(mWhiteList, phonenum, flags);
    }

    public boolean delete(ContactEntity entity) {
        int size = mWhiteList.size();
        ContactEntity temEntity;
    	
        for (int i = 0; i < size; i++) {
            temEntity = mWhiteList.get(i);
            if (temEntity.phonenum.equals(entity.phonenum)) {
                mWhiteList.remove(temEntity);
                return true;
            }
        }
        return false;  
    }

    public long insert(ContactEntity entity) {
        mWhiteList.add(entity);
        return mWhiteList.size() - 1;
    }

    public boolean update(ContactEntity entity) {
        int size = mWhiteList.size();
        ContactEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mWhiteList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mWhiteList.remove(tempEntity);
                mWhiteList.add(i, entity);
            }
        }
        return true;
    }

    public List<ContactEntity> getAll() {
        return mWhiteList;
    }

    public boolean clearAll() {
        mWhiteList.clear();
        return true;
    }
}
