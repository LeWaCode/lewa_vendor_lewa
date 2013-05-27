package com.lewa.labi.intf;

import com.lewa.labi.impl.TaskPoolImpl;

public abstract class TaskPool {
    private static TaskPool impl;
    public abstract void addTask(AbsTask abstask);
    public abstract void finishTask(AbsTask abstask);

    public static TaskPool getInstance() {
        if (impl == null) {
			impl = new TaskPoolImpl();
		}
		return impl;
	}
	public abstract AbsTask findTaskById(Integer id);
}
