package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tmsecure.module.aresengine.ContactEntity;
import com.tencent.tmsecure.module.aresengine.IContactDao;

public class PrivateListDao implements IContactDao<ContactEntity> {
    private static List<ContactEntity> mSecureList = new ArrayList<ContactEntity>();
    private static PrivateListDao mSecureListDao;
    private static final int NUM_ENTITIES = 5;
    private static int mEntityIds[] = { 13, 15, 16, 20, 22 };
    private static String mPhoneNums[] = { "15914354346", "076926709394",
            "59276037", "58376035", "83423734" };
    private static String mNames[] = { "", "", "", "", "" };

    private PrivateListDao() {
        DaoHelper.populateStaticData(mSecureList, NUM_ENTITIES, mEntityIds,
                mPhoneNums, mNames);
    }

    public static PrivateListDao getInstance() {
        if (null == mSecureListDao) {
            synchronized (PrivateListDao.class) {
                mSecureListDao = new PrivateListDao();
            }
        }
        return mSecureListDao;
    }

    @Override
    public boolean contains(String phonenum, int flags) {
        return DaoHelper.contains(mSecureList, phonenum, flags);
    }

    public boolean delete(ContactEntity entity) {
        boolean isDeleted = mSecureList.remove(entity);
        return isDeleted;
    }

    public long insert(ContactEntity entity) {
        mSecureList.add(entity);
        return mSecureList.size() - 1;
    }

    public boolean update(ContactEntity entity) {
        int size = mSecureList.size();
        ContactEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mSecureList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mSecureList.remove(tempEntity);
                mSecureList.add(i, entity);
            }
        }
        return true;
    }

    public List<ContactEntity> getAll() {
        return mSecureList;
    }

    public boolean clearAll() {
        mSecureList.clear();
        return true;
    }
}
