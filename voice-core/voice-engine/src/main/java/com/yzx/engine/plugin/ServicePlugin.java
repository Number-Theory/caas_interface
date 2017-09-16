package com.yzx.engine.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.access.model.QueueModel;
import com.yzx.auth.service.PluginSupport;
import com.yzx.core.queue.NettyWorkQueue;
import com.yzx.engine.facotry.ThreadPoolsFactory;
import com.yzx.engine.service.ServiceConfManager;

public class ServicePlugin extends PluginSupport {
	private static final Logger logger = LogManager.getLogger(ServicePlugin.class);

	@Override
	public void startUpService() {
		ServiceConfManager.loadServiceConfs();
		ThreadPoolsFactory.init();
//		new Thread(() -> {
//			while (true) {
//				try {
//					if (NettyWorkQueue.queue.size() > 0) {
//						logger.debug("队列大小:{}", NettyWorkQueue.queue.size());
//						QueueModel queueModel = (QueueModel) NettyWorkQueue.queue.poll();
//						queueModel.getHandler().call(queueModel.getUrl(), queueModel.getCtx(),
//								queueModel.getRequestString(), queueModel.getLogId());
//					}
//				} catch (Exception ignore) {
//					logger.error("消费队列错误", ignore);
//				}
//			}
//		}, "netty-workQueue-select").start();
	}
}
