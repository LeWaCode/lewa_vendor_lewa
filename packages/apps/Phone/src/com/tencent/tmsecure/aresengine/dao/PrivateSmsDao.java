package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tmsecure.module.aresengine.FilterResult;
import com.tencent.tmsecure.module.aresengine.ISmsDao;
import com.tencent.tmsecure.module.aresengine.SmsEntity;

public class PrivateSmsDao implements ISmsDao<SmsEntity> {
    private static List<SmsEntity> mSecureSmsList = new ArrayList<SmsEntity>();
    private static PrivateSmsDao mSecureSmsDao;

    private PrivateSmsDao() {
    }

    public static PrivateSmsDao getInstance() {
        if (null == mSecureSmsDao) {
            synchronized (PrivateSmsDao.class) {
                mSecureSmsDao = new PrivateSmsDao();
            }
        }
        return mSecureSmsDao;
    }

    public boolean delete(SmsEntity entity) {
        mSecureSmsList.remove(entity);
        return true;
    }

    public boolean update(SmsEntity entity) {
        int size = mSecureSmsList.size();
        SmsEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mSecureSmsList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mSecureSmsList.remove(tempEntity);
                mSecureSmsList.add(i, entity);
            }
        }
        return true;
    }

    public List<SmsEntity> getAll() {
        return mSecureSmsList;
    }

    public boolean clearAll() {
        mSecureSmsList.clear();
        return true;
    }

    @Override
    public long insert(SmsEntity entity, FilterResult paramFilterResult) {
        mSecureSmsList.add(entity);
        return mSecureSmsList.size() - 1;
    }

}
