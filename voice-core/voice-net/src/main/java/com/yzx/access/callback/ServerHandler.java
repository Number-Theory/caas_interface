package com.yzx.access.callback;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

/**
 * 
 * @author xupiao 2017年6月5日
 *
 */
public interface ServerHandler {
	public void call(HttpRequest request, String url, ChannelHandlerContext ctx, String requestString,
			Map<String, Object> paramObject, final String logId);
}
