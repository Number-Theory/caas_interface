package com.yzx.access.plugin;

import com.yzx.access.callback.ServerHandler;
import com.yzx.access.handler.CommonServerHandler;
import com.yzx.access.http.HttpAccessServer;
import com.yzx.auth.service.PluginSupport;

/**
 * Http接入插件
 * 
 * @author xupiao 2017年6月2日
 *
 */
public class HttpAccessPlugin extends PluginSupport {
	HttpAccessServer httpAccessServer;
	ServerHandler serverHandler;

	@Override
	public boolean initService() {
		httpAccessServer = new HttpAccessServer();
		serverHandler = new CommonServerHandler();
		return true;
	}

	@Override
	public void startUpService() {
		try {
			httpAccessServer.getHttpServer().start(serverHandler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void shutdownService() {
		httpAccessServer.getHttpServer()._shutdown();
	}
}
