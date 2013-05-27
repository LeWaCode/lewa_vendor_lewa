package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;
import com.tencent.tmsecure.module.aresengine.CallLogEntity;
import com.tencent.tmsecure.module.aresengine.FilterResult;
import com.tencent.tmsecure.module.aresengine.ICallLogDao;

public class CallLogDao implements ICallLogDao<CallLogEntity> {
    private static List<CallLogEntity> mCallLogList = new ArrayList<CallLogEntity>();
    private static CallLogDao mCallLogDao;

    private CallLogDao() {
    
    }

    public static CallLogDao getInstance() {
        if (null == mCallLogDao) {
            synchronized (CallLogDao.class) {
                mCallLogDao = new CallLogDao();
            }
        }
        return mCallLogDao;
    }

    public List<CallLogEntity> getSecureCallList() {
        List<CallLogEntity> callLogList = new ArrayList<CallLogEntity>();
        for (CallLogEntity entity : callLogList) {
            callLogList.add(entity);
        }
        return callLogList;
    }

    public boolean delete(CallLogEntity entity) {
        mCallLogList.remove(entity);
        return true;
    }

    public boolean update(CallLogEntity entity) {
        int size = mCallLogList.size();
        CallLogEntity tempEntity;
        for (int i = 0; i < size; i++) {
            tempEntity = mCallLogList.get(i);
            if (tempEntity.phonenum.equals(entity.phonenum)) {
                mCallLogList.remove(tempEntity);
                mCallLogList.add(i, entity);
            }
        }
        return true;
    }

    public List<CallLogEntity> getAll() {
        return mCallLogList;
    }

    public boolean clearAll() {
        mCallLogList.clear();
        return true;
    }

    @Override
    public long insert(CallLogEntity entity, FilterResult paramFilterResult) {
        mCallLogList.add(entity);
        return 0;
    }

}
