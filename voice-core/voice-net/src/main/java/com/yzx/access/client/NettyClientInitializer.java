package com.yzx.access.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.nio.charset.Charset;

import com.yzx.access.callback.ClientHandler;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

	private ClientHandler clientHandler;

	public NettyClientInitializer(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
	}
	
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast("frameDecoder", new HttpResponseDecoder());
        pipeline.addLast("frameEncoder", new HttpRequestEncoder());
        Charset c = Charset.forName("UTF-8");
        pipeline.addLast("decoder", new StringDecoder(c));
        pipeline.addLast("encoder", new StringEncoder(c));
        pipeline.addLast("aggegator",new HttpObjectAggregator(1024*1024*64));

        pipeline.addLast(new NettyClientHandler(clientHandler));
    }
}
