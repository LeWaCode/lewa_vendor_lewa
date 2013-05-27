package com.tencent.tmsecure.aresengine.dao;

import android.content.Context;

import com.tencent.tmsecure.aresengine.dao.BlackListDao;
import com.tencent.tmsecure.aresengine.dao.CallLogDao;
import com.tencent.tmsecure.aresengine.dao.KeyWordDao;
import com.tencent.tmsecure.aresengine.dao.LastCallLogDao;
import com.tencent.tmsecure.aresengine.dao.PrivateCallLogDao;
import com.tencent.tmsecure.aresengine.dao.PrivateListDao;
import com.tencent.tmsecure.aresengine.dao.PrivateSmsDao;
import com.tencent.tmsecure.aresengine.dao.SmsDao;
import com.tencent.tmsecure.aresengine.dao.WhiteListDao;
import com.tencent.tmsecure.module.aresengine.AbsSysDao;
import com.tencent.tmsecure.module.aresengine.AresEngineFactor;
import com.tencent.tmsecure.module.aresengine.CallLogEntity;
import com.tencent.tmsecure.module.aresengine.ContactEntity;
import com.tencent.tmsecure.module.aresengine.ICallLogDao;
import com.tencent.tmsecure.module.aresengine.IContactDao;
import com.tencent.tmsecure.module.aresengine.IEntityConverter;
import com.tencent.tmsecure.module.aresengine.IKeyWordDao;
import com.tencent.tmsecure.module.aresengine.ILastCallLogDao;
import com.tencent.tmsecure.module.aresengine.IPhoneDeviceController;
import com.tencent.tmsecure.module.aresengine.ISmsDao;
import com.tencent.tmsecure.module.aresengine.SmsEntity;


public final class LewaAresEngineFactor extends AresEngineFactor {
	private Context mContext;

	public LewaAresEngineFactor(Context context) {
		mContext = context;
	}

	@Override
	public IContactDao<? extends ContactEntity> getBlackListDao() {
		return BlackListDao.getInstance(mContext);
	}

	@Override
	public ICallLogDao<? extends CallLogEntity> getCallLogDao() {
		return CallLogDao.getInstance();
	}

	@Override
	public IEntityConverter getEntityConverter() {
		return new EntityConvert();
	}

	@Override
	public IKeyWordDao getKeyWordDao() {
		return KeyWordDao.getInstance();
	}

	@Override
	public ILastCallLogDao getLastCallLogDao() {
		return LastCallLogDao.getInstance();
	}

	@Override
	public ICallLogDao<? extends CallLogEntity> getPrivateCallLogDao() {
		return PrivateCallLogDao.getInstance();
	}

	@Override
	public IContactDao<? extends ContactEntity> getPrivateListDao() {
		return PrivateListDao.getInstance();
	}

	@Override
	public ISmsDao<? extends SmsEntity> getPrivateSmsDao() {
		return PrivateSmsDao.getInstance();
	}

	@Override
	public ISmsDao<? extends SmsEntity> getSmsDao() {
		return SmsDao.getInstance();
	}

	@Override
	public IContactDao<? extends ContactEntity> getWhiteListDao() {
		return WhiteListDao.getInstance(mContext);
	}
	
	@Override
	public IPhoneDeviceController getPhoneDeviceController() {
		return super.getPhoneDeviceController();
	}

	@Override
	public AbsSysDao getSysDao() {
		return AbsSysDao.getDefault(mContext);
	}
}
