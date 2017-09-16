package com.yzx.access.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.access.callback.ServerHandler;

@Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

	private static Logger log = LogManager.getLogger(NettyServerInitializer.class);

	private HttpRequest request;
	private final ServerHandler serverHandler;

	public NettyServerHandler(ServerHandler serverHandler) {
		this.serverHandler = serverHandler;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
		}
		if (msg instanceof HttpContent) {
			final String logId = UUID.randomUUID().toString().replaceAll("-", "");
			log.info("[{}]开始读包", logId);
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();

			String requestString = buf.toString(io.netty.util.CharsetUtil.UTF_8);
			Map<String, Object> map = parse(request);
			serverHandler.call(request, request.uri(), ctx, requestString, map, logId);
			buf.release();
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("netty错误：", cause);
		ctx.close();
	}

	/**
	 * 解析请求参数
	 * 
	 * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
	 *
	 * @throws BaseCheckedException
	 * @throws IOException
	 */
	public Map<String, Object> parse(HttpRequest fullReq) throws IOException {

		Map<String, Object> parmMap = new HashMap<>();

		QueryStringDecoder decoder = new QueryStringDecoder(fullReq.uri());
		decoder.parameters().entrySet().forEach(entry -> {
			parmMap.put(entry.getKey(), entry.getValue().get(0));
		});
		return parmMap;
	}
}
