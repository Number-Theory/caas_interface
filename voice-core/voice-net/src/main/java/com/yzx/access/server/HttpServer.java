package com.yzx.access.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.access.callback.DefaultServerHandler;
import com.yzx.access.callback.ServerHandler;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.LangUtil;

public class HttpServer {

	private Logger logger = LogManager.getLogger(HttpServer.class);

	private Integer port;

	public HttpServer(Integer port) {
		this.port = port;
	}

	EventLoopGroup bossGroupForServer = null;
	EventLoopGroup workerGroupForServer = null;

	public void _shutdown() {
		try {
			if (bossGroupForServer != null) {
				bossGroupForServer.shutdownGracefully();
				bossGroupForServer = null;
			}
			if (workerGroupForServer != null) {
				workerGroupForServer.shutdownGracefully();
				workerGroupForServer = null;
			}
		} catch (Exception e) {
			logger.error("Netty服务端关闭异常", e);
			throw LangUtil.wrapThrow("Netty服务端关闭异常", e);
		}
	}

	public void start(ServerHandler handler) throws Exception {
		bossGroupForServer = new NioEventLoopGroup(ConfigUtils.getProperty("Netty.thread.BOSS线程数", 1, Integer.class));
		workerGroupForServer = new NioEventLoopGroup(ConfigUtils.getProperty("Netty.thread.IO线程数", Runtime.getRuntime().availableProcessors() * 2,
				Integer.class));

		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(this.bossGroupForServer, this.workerGroupForServer).channel(NioServerSocketChannel.class)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000).childHandler(new NettyServerInitializer(handler));

		bootstrap.bind(port).sync();
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public static void main(String[] args) {
		try {
			new HttpServer(1111).start(new DefaultServerHandler());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
