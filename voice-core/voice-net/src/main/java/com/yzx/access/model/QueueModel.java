package com.yzx.access.model;

import io.netty.channel.ChannelHandlerContext;

import com.yzx.access.callback.ServerHandler;
import com.yzx.core.queue.BaseQueueModel;

/**
 * 
 * @author xupiao 2017年6月9日
 *
 */
public class QueueModel extends BaseQueueModel {
	private String url;
	private ChannelHandlerContext ctx;
	private String requestString;
	private String logId;
	private ServerHandler handler;

	public QueueModel(String url, ChannelHandlerContext ctx, String requestString, String logId, ServerHandler handler) {
		this.url = url;
		this.ctx = ctx;
		this.requestString = requestString;
		this.logId = logId;
		this.handler = handler;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public String getRequestString() {
		return requestString;
	}

	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public ServerHandler getHandler() {
		return handler;
	}

	public void setHandler(ServerHandler handler) {
		this.handler = handler;
	}
}
