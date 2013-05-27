package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tmsecure.module.aresengine.CallLogEntity;
import com.tencent.tmsecure.module.aresengine.FilterResult;
import com.tencent.tmsecure.module.aresengine.ICallLogDao;

public class PrivateCallLogDao implements ICallLogDao<CallLogEntity> {
    private static List<CallLogEntity> mSecureCallLogList = new ArrayList<CallLogEntity>();
    private static PrivateCallLogDao msecureCallLogDao;

    private PrivateCallLogDao() {
    }

    public static PrivateCallLogDao getInstance() {
        if (null == msecureCallLogDao) {
            synchronized (PrivateCallLogDao.class) {
                msecureCallLogDao = new PrivateCallLogDao();
            }
        }
        return msecureCallLogDao;
    }

    public boolean delete(CallLogEntity entity) {
        mSecureCallLogList.remove(entity);
        return true;
    }

    public boolean update(CallLogEntity entity) {
        int size = mSecureCallLogList.size();
        CallLogEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mSecureCallLogList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mSecureCallLogList.remove(tempEntity);
                mSecureCallLogList.add(i, entity);
            }
        }
        return true;
    }

    public List<CallLogEntity> getAll() {
        return mSecureCallLogList;
    }

    public boolean clearAll() {
        mSecureCallLogList.clear();
        return true;
    }

    @Override
    public long insert(CallLogEntity entity, FilterResult paramFilterResult) {
         mSecureCallLogList.add(entity);
        return 0;
    }

}
