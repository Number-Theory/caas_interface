package com.yzx.access.util;

import com.yzx.core.util.Log4jUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpUtils {
	private static final Logger logger = LogManager.getLogger(HttpUtils.class);

	public static void sendMessageJson(ChannelHandlerContext ctx, String responseString) {
		FullHttpResponse response;
		try {
			logger.info("发送响应消息内容:【{}】", responseString);
			response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseString
					.getBytes("UTF-8")));
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=utf-8");
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			printOutBoundMessage(response, responseString);
		} catch (Exception e) {
			logger.error("发送响应消息失败", e);
			throw new RuntimeException(e);
		}
	}
	public static void sendMessageXml(ChannelHandlerContext ctx, String responseString) {
		FullHttpResponse response;
		try {
			logger.info("发送响应消息内容:【{}】", responseString);
			response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(responseString
					.getBytes("UTF-8")));
			response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/xml;charset=utf-8");
			response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
			ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
			printOutBoundMessage(response, responseString);
		} catch (Exception e) {
			logger.error("发送响应消息失败", e);
			throw new RuntimeException(e);
		}
	}

	

	private static void printOutBoundMessage(HttpResponse response, String responseString) {
		StringBuffer info = new StringBuffer("\n---------------------------\nOutbound Message\n");
		info.append("ID: ").append(Log4jUtils.getLogId()).append("\n");

		info.append("Content-Type: ").append(response.headers().get(HttpHeaderNames.CONTENT_TYPE)).append("\n");

		info.append("Headers: ").append(response.headers().entries()).append("\n");

		info.append("Messages: ").append(responseString).append("\n---------------------------");
		
		logger.debug(info);
	}



	public static String getClientIp(HttpRequest request, ChannelHandlerContext ctx) {
		String clientIP = request.headers().get("X-Forwarded-For");
		if (clientIP == null) {
			InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
			clientIP = insocket.getAddress().getHostAddress();
		}
		return clientIP;
	}
}
