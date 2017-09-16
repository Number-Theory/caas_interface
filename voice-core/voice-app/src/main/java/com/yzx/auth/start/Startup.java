package com.yzx.auth.start;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.auth.GlobalServiceContext;
import com.yzx.auth.GlobalServiceContext.SystemStatus;
import com.yzx.auth.service.PluginConfManager;
import com.yzx.auth.service.PluginServiceManager;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.LangUtil;

/**
 * 启动入口，由{@link Bootstrap}引导启动。
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class Startup implements Bootable {

	protected class AppShutdownHook extends Thread {
		@Override
		public void run() {
			Startup.this.stop();
		}
	}

	static {
//		PluginManager.addPackage(RollingFileAppender.class.getPackage().getName());
//		PluginManager.addPackage(TriggeringPolicy.class.getPackage().getName());
	}

	private static final Logger BOOT_LOGGER = LogManager.getLogger(Startup.class);
	protected ClassLoader parentClassLoader = Startup.class.getClassLoader();

	private boolean await = false;// 是否等待直到收到关闭指令

	private boolean useShutdownHook = true;
	private Thread shutdownHook = null;

	@Override
	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}

	@Override
	public void start() {

		if (GlobalServiceContext.get().getStatus() == SystemStatus.started) {
			BOOT_LOGGER.warn("服务已经启动,不可以再次启动！");
			return;
		}

		if (GlobalServiceContext.get().getStatus() == SystemStatus.starting) {
			BOOT_LOGGER.warn("服务正在启动,不可以再次启动！");
			return;
		}

		// 系统状态=启动中
		GlobalServiceContext.get().setStatus(SystemStatus.starting);

		try {
			BOOT_LOGGER.info("Starting Server");

			long t1 = System.nanoTime();
			try {

				PluginConfManager.loadPluginConfs();
				ConfigUtils.load();

				PluginServiceManager.initPluginServices();

				PluginServiceManager.startAllPluginServices();

				// 系统状态=已启动
				GlobalServiceContext.get().setStatus(SystemStatus.started);

				BOOT_LOGGER.info("Server startup in " + (System.nanoTime() - t1) / 1000000.0 + " ms");
			} catch (Throwable t) {
				throw LangUtil.wrapThrow(t);
			}

			// Register shutdown hook
			if (useShutdownHook) {
				try {
					if (shutdownHook == null)
						shutdownHook = new AppShutdownHook();
					Runtime.getRuntime().addShutdownHook(shutdownHook);
				} catch (Throwable ignore) {

				}
			}

		} catch (Exception ex) {
			BOOT_LOGGER.error("启动失败：" + ex, ex);
			GlobalServiceContext.get().setStatus(SystemStatus.failed);

			try {
				BOOT_LOGGER.info("关闭已启动的服务...");
				stop();
			} catch (Exception ignore) {
				BOOT_LOGGER.error("关闭已启动的服务失败.", ignore);
			}

			throw new RuntimeException("启动失败：" + ex, ex);
		}
	}

	@Override
	public void stop() {
		if (GlobalServiceContext.get().getStatus() == SystemStatus.stopped) {
			BOOT_LOGGER.warn("服务已经停止,不可以再次停止！");
			return;
		}
		if (GlobalServiceContext.get().getStatus() == SystemStatus.stopping) {
			BOOT_LOGGER.warn("服务正在停止,不可以再次停止！");
			return;
		}

		GlobalServiceContext.get().setStatus(SystemStatus.stopping);
		try {
			try {
				if (useShutdownHook) {
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
				}
			} catch (Throwable t) {

			}

			PluginServiceManager.shutdownAllPluginServices();
			BOOT_LOGGER.info("Server Stoped.");

			GlobalServiceContext.get().setStatus(SystemStatus.stopped);

		} catch (Exception ex) {
			BOOT_LOGGER.error("停止失败：", ex);
			GlobalServiceContext.get().setStatus(SystemStatus.failed);
			throw new RuntimeException("停止失败：", ex);
		}
	}

	@Override
	public void stopServer() {
		stopServer(null);
	}

	@Override
	public void stopServer(String[] arguments) {
	}

	@Override
	public void setAwait(boolean await) {
		this.await = await;
	}

	@Override
	public boolean getAwait() {
		return await;
	}
}
