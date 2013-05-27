package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tmsecure.module.aresengine.FilterResult;
import com.tencent.tmsecure.module.aresengine.ISmsDao;
import com.tencent.tmsecure.module.aresengine.SmsEntity;

public class SmsDao implements ISmsDao<SmsEntity> {
    private static List<SmsEntity> mSmsList = new ArrayList<SmsEntity>();
    private static SmsDao mSmsDao;

    private SmsDao() {

    }

    public static SmsDao getInstance() {
        if (null == mSmsDao) {
            synchronized (SmsDao.class) {
                mSmsDao = new SmsDao();
            }
        }
        return mSmsDao;
    }

    public boolean delete(SmsEntity entity) {
        mSmsList.remove(entity);
        return true;
    }

    public long insert(SmsEntity entity) {
        mSmsList.add(entity);
        return mSmsList.size() - 1;
    }

    public boolean update(SmsEntity entity) {
        int size = mSmsList.size();
        SmsEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mSmsList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mSmsList.remove(tempEntity);
                mSmsList.add(i, tempEntity);
            }
        }
        return true;
    }

    public List<SmsEntity> getAll() {
        return mSmsList;
    }

    public boolean clearAll() {
        mSmsList.clear();
        return true;
    }

    @Override
    public long insert(SmsEntity entity, FilterResult paramFilterResult) {
        // TODO Auto-generated method stub
        mSmsList.add(entity);
        return mSmsList.size() - 1;
    }

}
