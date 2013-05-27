package com.tencent.tmsecure.aresengine.dao;

import java.util.ArrayList;
import java.util.List;

import com.tencent.tmsecure.module.aresengine.CallLogEntity;
import com.tencent.tmsecure.module.aresengine.ILastCallLogDao;

public class LastCallLogDao implements ILastCallLogDao {
    private static List<CallLogEntity> mLastCallList = new ArrayList<CallLogEntity>();
    private static LastCallLogDao mLastCallLogDao;

    private LastCallLogDao() {
    }

    public static LastCallLogDao getInstance() {
        if (null == mLastCallLogDao) {
            synchronized (LastCallLogDao.class) {
                mLastCallLogDao = new LastCallLogDao();
            }
        }
        return mLastCallLogDao;
    }

    @Override
    public boolean contains(String phonenum) {
        for (CallLogEntity tempEntity : mLastCallList) {
            if (tempEntity.phonenum.equals(phonenum)) {
                return true;
            }
        }
        return false;
    }

    public void update(CallLogEntity calllog) {
        // TODO Auto-generated method stub
    }

}
