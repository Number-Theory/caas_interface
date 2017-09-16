package com.yzx.plugin;

import java.util.Timer;
import java.util.TimerTask;

import com.yzx.auth.service.PluginSupport;
import com.yzx.cache.GlobalCache;
import com.yzx.core.config.ConfigUtils;
import com.yzx.redis.RedisOpClient;

/**
 * 
 * @author xupiao 2017年6月7日
 *
 */
public class CachePlugin extends PluginSupport {
	private Timer cacheFlushTimer;

	@Override
	public void startUpService() {
		// new Thread(() -> {
		// RedisOpClient.init();
		// }, "redis-client-deamon").start();
		Long prieod = ConfigUtils.getProperty("cache.mem.定时刷新间隔", 600000L, Long.class);
		RedisOpClient.init();
		GlobalCache.load();
		cacheFlushTimer = new Timer("memCache-flush-Thread");
		cacheFlushTimer.schedule(new TimerTask() { // 串行调度
					@Override
					public void run() {
						GlobalCache.load();
					}
				}, prieod, prieod);
	}

	@Override
	public void shutdownService() {
		// RedisOpClient.clientKill(new KillFilter(), x -> {
		// });
		if (cacheFlushTimer != null) {
			cacheFlushTimer.cancel();
		}
	}
}
