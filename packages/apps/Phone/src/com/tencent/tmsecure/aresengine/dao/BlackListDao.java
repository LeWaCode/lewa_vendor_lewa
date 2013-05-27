package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.InterceptConstants;

import com.tencent.tmsecure.module.aresengine.ContactEntity;
import com.tencent.tmsecure.module.aresengine.IContactDao;
import android.util.Log;

public class BlackListDao implements IContactDao<ContactEntity> {	
    private static List<ContactEntity> mBlackList = new ArrayList<ContactEntity>();
    private static BlackListDao mBlackListDao;
    private Context mContext;
    private Uri uri = InterceptConstants.CONTENT_URI;
	

    private BlackListDao(Context context) {
    	mContext = context;
    	initFromStaticData();
    }
	
    protected void initFromStaticData() {
        mBlackList.clear();
        String selection = InterceptConstants.COLUMN_TYPE+"="+InterceptConstants.BLOCK_TYPE_BLACK;
        Cursor cursor = mContext.getContentResolver().query(uri, null, selection, null, null);
        while (cursor != null && cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(InterceptConstants.COLUMN_NAME));
            int modeType = cursor.getInt(cursor.getColumnIndex(InterceptConstants.COLUMN_MODE));
           // if (name == null || "".equals(name)) {
            //    name = number;
           // }
            ContactEntity entity = new ContactEntity();
            entity.phonenum = number;
            entity.name = name;
            if (modeType == InterceptConstants.BLOCK_TYPE_NUMBER_DEFAULT) {
            	entity.enableForCalling = true;
            	entity.enableForSMS = true;
            } else if (modeType == InterceptConstants.BLOCK_TYPE_NUMBER_CALL) {
            	entity.enableForCalling = true;
            } else if (modeType == InterceptConstants.BLOCK_TYPE_NUMBER_MSG) {
            	entity.enableForSMS = true;
            }
            mBlackList.add(entity);
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public static BlackListDao getInstance(Context context) {
        if (null == mBlackListDao) {
            synchronized (BlackListDao.class) {
                mBlackListDao = new BlackListDao(context);
            }
        }
        return mBlackListDao;
    }

    public boolean contains(String phonenum, int flags) {
        return DaoHelper.isInBlackList(mBlackList, phonenum, flags);
    }

    public boolean delete(ContactEntity entity) {
        int size = mBlackList.size();
        ContactEntity temEntity;
    	
        for (int i = 0; i < size; i++) {
            temEntity = mBlackList.get(i);
            if (temEntity.phonenum.equals(entity.phonenum)) {
                mBlackList.remove(temEntity);
                return true;
            }
        }
        return false;
    }

    public long insert(ContactEntity entity) {
        mBlackList.add(entity);
        return mBlackList.size() - 1;
    }

    public boolean update(ContactEntity entity) {
        int size = mBlackList.size();
        ContactEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mBlackList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mBlackList.remove(tempEntity);
                mBlackList.add(i, entity);
                return true;
            }
        }
        return false;
    }

    public boolean clearAll() {
        mBlackList.clear();
        return true;
    }
}
