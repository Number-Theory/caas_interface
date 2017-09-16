package com.yzx.auth.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * 插件服务管理器，每个插件可以认为就是一个服务。
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class PluginServiceManager {
	private static Logger BOOT_LOGGER = LogManager.getLogger(PluginServiceManager.class);
	private static Logger ERROR_LOGGER = LogManager.getLogger(PluginServiceManager.class);
	private static boolean initPluginServicesFlag = false;
	private static final Map<String, PluginSupport> plugins = new LinkedHashMap<String, PluginSupport>();
	private static final List<PluginSupport> pluginsOrdered = new ArrayList<PluginSupport>();

	private PluginServiceManager() {
	}

	public static void initPluginServices() {
		PluginConfManager.loadPluginConfs();

		if (initPluginServicesFlag)
			return;
		_initPluginServices();
		initPluginServicesFlag = true;
	}

	/**
	 * 初始化所有插件
	 */
	private static void _initPluginServices() {
		BOOT_LOGGER.info("所有插件初始化开始.");

		plugins.clear();
		pluginsOrdered.clear();

		List<PluginConfWrapper> pluginConfs = PluginConfManager.getPluginConfs();
		for (int i = 0; i < pluginConfs.size(); i++) {
			PluginConfWrapper pluginConfWrapper = pluginConfs.get(i);
			PluginConf pluginConf = pluginConfWrapper.getPluginConf();

			long start = System.currentTimeMillis();
			try {
				PluginSupport plugin = pluginConfWrapper.getPluginActivatorObj();

				boolean isInitSuccess = plugin.initPlugin();
				plugin.setInitSuccess(isInitSuccess);
				if (isInitSuccess) {
					BOOT_LOGGER.info("插件[" + pluginConf.getDisplayName() + "]初始化耗时："
							+ (System.currentTimeMillis() - start) / 1000.0 + "s");

					plugins.put(pluginConf.getId(), plugin);
					pluginsOrdered.add(plugin);
				}
			} catch (Throwable e) {
				if (pluginConf.isFailOnInitError()) {

					BOOT_LOGGER.error("初始化插件出错:id=" + pluginConf.getId(), e);

					// 2015.1.15 错误日志输出到专门的日志文件
					ERROR_LOGGER.error("初始化插件出错:id=[" + pluginConf.getId() + "]", e);

					if (e instanceof RuntimeException)
						throw (RuntimeException) e;
					throw new IllegalStateException("初始化插件出错:id=[" + pluginConf.getId() + "]" + e.getMessage(), e);
				}

				BOOT_LOGGER.info("初始化插件[" + pluginConf.getDisplayName() + "]出错, " + e.getMessage() + ", 允许跳过...");

				continue;
			}
		}

		BOOT_LOGGER.info("所有插件初始化完成.");
	}

	/**
	 * 按顺序启动所有的插件
	 */
	public static void startAllPluginServices() {
		for (PluginSupport plugin : pluginsOrdered) {
			BOOT_LOGGER.info("插件[" + plugin.getDisplayName() + "] ... 正在启动.");
			try {
				plugin.startupPlugin();
				BOOT_LOGGER.info("插件[" + plugin.getDisplayName() + "] ... 启动完成.");

			} catch (Throwable e) {
				BOOT_LOGGER.error("插件[" + plugin.getDisplayName() + "] ... 启动错误.", e);
				ERROR_LOGGER.error("插件[" + plugin.getDisplayName() + "] ... 启动错误.", e);


				throw new RuntimeException("插件[" + plugin.getDisplayName() + "] ... 启动错误.", e);
			}
		}

		BOOT_LOGGER.info("所有插件启动完成.");
	}

	/**
	 * 按倒序关闭全部所有插件
	 */
	public static void shutdownAllPluginServices() {
		for (int i = pluginsOrdered.size() - 1; i >= 0; i--) {
			PluginSupport plugin = pluginsOrdered.get(i);
			BOOT_LOGGER.info("正在停止插件[" + plugin.getDisplayName() + "].");
			try {
				plugin.shutdownPlugin();
				BOOT_LOGGER.info("插件[" + plugin.getDisplayName() + "]已停止.");
			} catch (Throwable e) {
				BOOT_LOGGER.error("停止插件[" + plugin.getDisplayName() + "]出错.", e);
				continue;
			}
		}
	}
}
