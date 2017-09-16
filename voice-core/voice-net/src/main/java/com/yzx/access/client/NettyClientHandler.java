package com.yzx.access.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.access.callback.ClientHandler;

public class NettyClientHandler extends ChannelInboundHandlerAdapter {
	private static Logger logger = LogManager.getLogger(NettyClientHandler.class);
	
	private HttpResponse response;
	private ClientHandler clientHandler;

	public NettyClientHandler(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		super.channelRegistered(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof HttpResponse) {
			response = (HttpResponse) msg;
//			System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaderNames.CONTENT_TYPE));
		}
		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();
			clientHandler.execute(response, buf.toString(io.netty.util.CharsetUtil.UTF_8));
			buf.release();
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		super.channelReadComplete(ctx);
		ctx.channel().close();
		ctx.close();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.error("Net客户端异常", cause);
		ctx.close();
	}
}
