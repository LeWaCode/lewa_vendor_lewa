package com.lewa.labi.impl;

import java.util.ArrayList;
import java.util.List;

import com.lewa.labi.intf.AbsTask;
import com.lewa.labi.intf.TaskPool;

public class TaskPoolImpl extends TaskPool {
	private static List<AbsTask> pool = new ArrayList<AbsTask>();
	
	public AbsTask findTaskById(Integer id) {
		for (Integer i = 0; i < pool.size(); i++) {
			if (id.equals(pool.get(i).getId())) {
				return pool.get(i); 
			}
		}
		return null;
	}

	@Override
	public void addTask(AbsTask abstask) {
		if(!pool.contains(abstask)){
			pool.add(abstask);
		}
	}

	@Override
	public void finishTask(AbsTask abstask) {
		if(pool.contains(abstask)){
			pool.remove(abstask);
		}
	}
}
