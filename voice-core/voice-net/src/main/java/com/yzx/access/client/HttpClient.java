package com.yzx.access.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.access.callback.ClientHandler;

public class HttpClient {
	private static Logger logger = LogManager.getLogger(HttpClient.class);

	private ClientHandler clientHandler;

	public HttpClient(ClientHandler clientHandler) {
		this.clientHandler = clientHandler;
		b = new Bootstrap();
		b.group(DefaultEventLoopGroup.get().getBossGroupForClient()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
				.handler(new NettyClientInitializer(this.clientHandler));
	}

	Bootstrap b = null;

	public void _shutdown() {

	}

	public ChannelFuture httpPost(String url, String body, HttpHeaders httpHeaders) throws InterruptedException, URISyntaxException,
			UnsupportedEncodingException {
		URI uri = new URI(url);
		ChannelFuture f = b.connect(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort()).sync();

		DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getPath().toString(),
				Unpooled.wrappedBuffer(body.getBytes("UTF-8")));

		// 构建http请求
		request.headers().setAll(httpHeaders);
		request.headers().set(HttpHeaderNames.HOST, uri.getHost());
		request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
		request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=utf-8");

		// 发送http请求
		f.channel().write(request);
		f.channel().flush();
		f.channel().closeFuture().sync();
		f.addListener(ChannelFutureListener.CLOSE);
		return f;
	}

	public ChannelFuture httpPost(String url, String body) throws InterruptedException, URISyntaxException, UnsupportedEncodingException {
		URI uri = new URI(url);
		ChannelFuture f = b.connect(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort()).sync();

		DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getPath().toString(),
				Unpooled.wrappedBuffer(body.getBytes("UTF-8")));

		// 构建http请求
		request.headers().set(HttpHeaderNames.HOST, uri.getHost());
		request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
		request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=utf-8");
		// 发送http请求
		f.channel().write(request);
		f.channel().flush();
		f.channel().closeFuture().sync();
		f.addListener(ChannelFutureListener.CLOSE);
		return f;
	}

	/**
	 * 回调业务单独专用POST方法
	 * 
	 * @param url
	 * @param body
	 * @return
	 * @throws InterruptedException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public ChannelFuture httpPostBack(String url, String body) throws InterruptedException, URISyntaxException, UnsupportedEncodingException {
		URI uri = new URI(url);
		ChannelFuture f = b.connect(uri.getHost(), uri.getPort() == -1 ? 80 : uri.getPort()).sync();
		DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getPath().toString(),
				Unpooled.wrappedBuffer(body.getBytes("UTF-8")));

		// 构建http请求
		request.headers().set(HttpHeaderNames.HOST, uri.getHost());
		request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
		request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json;charset=utf-8");
		// 发送http请求
		f.channel().write(request);
		f.channel().flush();
		f.channel().closeFuture().sync();
		f.addListener(ChannelFutureListener.CLOSE);

		return f;
	}

	public ChannelFuture httpGet(String url) throws InterruptedException, URISyntaxException, UnsupportedEncodingException {
		URI uri = new URI(url);
		ChannelFuture f = b.connect(uri.getHost(), uri.getPort()).sync();
		DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri.toASCIIString());

		// 构建http请求
		request.headers().set(HttpHeaderNames.HOST, uri.getHost());
		request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
		request.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
		// 发送http请求
		f.channel().write(request);
		f.channel().flush();
		f.channel().closeFuture().sync();
		f.addListener(ChannelFutureListener.CLOSE);
		return f;
	}

	public ChannelFuture connect(String host, Integer port) throws InterruptedException {
		ChannelFuture f = b.connect(host, port).sync();
		return f;
	}

	public static class DefaultEventLoopGroup {
		private static DefaultEventLoopGroup defaultEventLoopGroup = new DefaultEventLoopGroup();

		public static DefaultEventLoopGroup get() {
			return defaultEventLoopGroup;
		}

		public DefaultEventLoopGroup() {
			this.bossGroupForClient = new NioEventLoopGroup();
		}

		private EventLoopGroup bossGroupForClient;

		public EventLoopGroup getBossGroupForClient() {
			return bossGroupForClient;
		}
	}
}
