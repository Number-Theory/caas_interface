package com.yzx.access.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

import java.nio.charset.Charset;

import com.yzx.access.callback.ServerHandler;
import com.yzx.core.config.ConfigUtils;

public class NettyServerInitializer extends ChannelInitializer<SocketChannel> {
	private ServerHandler handler;

	public NettyServerInitializer(ServerHandler handler) {
		this.handler = handler;
	}

	@Override
	protected void initChannel(SocketChannel socketChannel) throws Exception {
		ChannelPipeline pipeline = socketChannel.pipeline();
		pipeline.addLast(new ReadTimeoutHandler(ConfigUtils.getProperty("Netty.thread.IO超时时间", 10, Integer.class)));
		pipeline.addLast(new WriteTimeoutHandler(ConfigUtils.getProperty("Netty.thread.IO超时时间", 10, Integer.class)));
		pipeline.addLast("frameDncoder", new HttpRequestDecoder());
		Charset utf8 = Charset.forName("UTF-8");
		pipeline.addLast("decoder", new StringDecoder(utf8));
		pipeline.addLast("aggegator", new HttpObjectAggregator(1024 * 1024 * 64));
		pipeline.addLast("handler", new NettyServerHandler(handler));
		pipeline.addLast("frameEecoder", new HttpResponseEncoder());
		pipeline.addLast("encoder", new StringEncoder(utf8));
		
	}
}
