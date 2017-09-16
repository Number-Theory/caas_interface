package com.yzx.auth.plugin;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yzx.auth.comm.AppResourceManager;
import com.yzx.auth.service.PluginSupport;

/**
 * 引入spring容器的插件
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class SpringPlugin extends PluginSupport {
	public static ClassPathXmlApplicationContext context;
	@Override
	public boolean initService() {
		context = new ClassPathXmlApplicationContext(AppResourceManager.springXml);
		return true;
	}

	@Override
	public void startUpService() {
		context.start();
	}

	@Override
	public void shutdownService() {
		context.stop();
	}
}
