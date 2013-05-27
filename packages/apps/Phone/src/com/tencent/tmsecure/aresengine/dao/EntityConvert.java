package com.tencent.tmsecure.aresengine.dao;

import com.tencent.tmsecure.module.aresengine.CallLogEntity;
import com.tencent.tmsecure.module.aresengine.ContactEntity;
import com.tencent.tmsecure.module.aresengine.IEntityConverter;
import com.tencent.tmsecure.module.aresengine.SmsEntity;

public class EntityConvert implements IEntityConverter {

	@Override
	public SmsEntity convert(SmsEntity entity) {
		SmsEntity sms = new SmsEntity(entity);
		return sms;
	}

	@Override
	public CallLogEntity convert(CallLogEntity entity) {
		CallLogEntity calllog = new CallLogEntity(entity);
		return calllog;
	}
}
