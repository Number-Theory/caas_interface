package com.yzx.auth.plugin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.auth.service.PluginSupport;

public class TestPlugin extends PluginSupport{
	private static final Logger logger = LogManager.getLogger(TestPlugin.class); 
	
	@Override
	public boolean initService() {
		logger.info("TestPlugin插件初始化成功");
		return true;
	}
	
	@Override
	public void startUpService() {
		logger.info("TestPlugin插件启动成功");
	}
	
	@Override
	public void shutdownService() {
		logger.info("TestPlugin插件已停止");
	}
}
