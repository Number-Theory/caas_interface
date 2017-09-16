package com.yzx.access.http;

import com.yzx.access.server.HttpServer;
import com.yzx.core.config.ConfigUtils;

/**
 * Http接入服务
 * 
 * @author xupiao 2017年6月2日
 *
 */
public class HttpAccessServer {
	HttpServer httpServer = null;

	public synchronized HttpServer getHttpServer() {
		if (httpServer == null) {
			Integer port = ConfigUtils.getProperty("Http.access.port", 5081, Integer.class);
			httpServer = new HttpServer(port);
		}
		return httpServer;
	}

	public void setHttpServer(HttpServer httpServer) {
		this.httpServer = httpServer;
	}
}
