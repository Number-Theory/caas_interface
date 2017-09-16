package com.yzx.engine.facotry;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.yzx.core.config.ConfigUtils;

/**
 * 
 * @author xupiao 2017年6月6日
 *
 */
public class ThreadPoolsFactory {

	private static ThreadPoolExecutor ServiceExecutorPools;

	public static void init() {
		Integer defaultCorePoolSize = ConfigUtils.getProperty("Netty.thread.工作线程数", Runtime.getRuntime()
				.availableProcessors() * 2, Integer.class);
		ServiceExecutorPools = new ThreadPoolExecutor(defaultCorePoolSize, defaultCorePoolSize, 0, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(), new SimpleThreadFactory("Works-Thread"));
	}

	public static ThreadPoolExecutor getServiceExecutorPools() {
		return ServiceExecutorPools;
	}

	public static void setServiceExecutorPools(ThreadPoolExecutor serviceExecutorPools) {
		ServiceExecutorPools = serviceExecutorPools;
	}
}
