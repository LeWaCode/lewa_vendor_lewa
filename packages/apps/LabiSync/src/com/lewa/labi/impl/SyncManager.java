package com.lewa.labi.impl;

import java.util.HashMap;
import java.util.Map;

import com.lewa.labi.intf.AbsTask;
import com.lewa.labi.intf.CompanionNotFoundException;
import com.lewa.labi.intf.TaskPool;

public class SyncManager {
	public static final int COMPANION_ID_LABI = 0;
	public static Map<Integer, Class<? extends AbsTask>> thirdParties = new HashMap<Integer, Class<? extends AbsTask>>();

	static {
		thirdParties.put(COMPANION_ID_LABI, LabiTask.class);
	}

	public static AbsTask getThirdPartyInstance(Integer thirdPartyId) {
		if (!thirdParties.containsKey(thirdPartyId)) {
			throw new CompanionNotFoundException("3party id : " + thirdPartyId
					+ " no found");
		}
		AbsTask task = null;
		try {
			task = AbsTask.getFactory(thirdParties.get(thirdPartyId));
			TaskPool.getInstance().addTask(task);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return task;
	}

	public static void finishThirdPartyTask(Integer thirdPartyId) {
			finishThirdPartyTask(TaskPool.getInstance().findTaskById(thirdPartyId));
	}

	public static void finishThirdPartyTask(AbsTask task) {
	    try {
	        if (task == null) {
	            throw new IllegalStateException("task not found in pool");
	        } else {
	            TaskPool.getInstance().finishTask(task);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
