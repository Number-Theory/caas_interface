package com.yzx.engine.facotry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class SimpleThreadFactory implements ThreadFactory {
	private final ThreadGroup group;
	private final String groupName;
	private final Map<String, AtomicInteger> threadNumberByName = new ConcurrentHashMap<String, AtomicInteger>();
	
	public SimpleThreadFactory(String groupName) {
		this.groupName = groupName;
		
		// 按类别计数
		AtomicInteger threadNumber = threadNumberByName.get(groupName);
		if (threadNumber == null) {
			threadNumber = new AtomicInteger(1);
			threadNumberByName.put(groupName, threadNumber);
		}		
		
		group = new ThreadGroup(groupName);
	}

	public Thread newThread(final Runnable r) {
		final Thread t = __newThread(r);
		
		return t;
	}

	private Thread __newThread(Runnable r) {
		AtomicInteger threadNumber = threadNumberByName.get(groupName);
		Thread t = new Thread(group, r, group.getName() + "-" + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}
}
