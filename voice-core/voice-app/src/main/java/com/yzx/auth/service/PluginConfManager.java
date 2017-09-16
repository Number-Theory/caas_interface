package com.yzx.auth.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;

import com.yzx.core.util.ClassUtils;
import com.yzx.core.util.CoreUtils;
import com.yzx.core.util.LangUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.core.util.XmlConfigManager;

/**
 * 插件配置管理器，用于加载所有插件
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class PluginConfManager {
	private static final Logger PLUGIN_LOGGER = LogManager.getLogger(PluginConfManager.class);
	private static boolean loadPluginConfsFlag = false;
	// 插件列表
	private static List<PluginConfWrapper> pluginConfigs = new ArrayList<PluginConfWrapper>();

	private static XmlConfigManager xcm = new XmlConfigManager(new Class[] { PluginConf.class });

	/**
	 * 加载CLASSPATH下的所有插件
	 */
	public static void loadPluginConfs() {
		if (loadPluginConfsFlag)// 已经加载不再重复加载
			return;
		try {
			// 从CLASSPATH中加载 *.plugin.xml文件
			Resource[] resources = CoreUtils.findResources("classpath*:/plugin/*.plugin.xml");
			for (Resource resource : resources) {
				try {
					InputStream is = resource.getInputStream();
					PluginConf pluginConf = xcm.load(is);
					if (!pluginConf.isEnable())
						continue;
					PluginConfWrapper pluginConfWrapper = new PluginConfWrapper();
					pluginConfWrapper.setPluginConf(pluginConf);
					pluginConfWrapper.setPluginConfFileName(resource.getFilename());
					pluginConfigs.add(pluginConfWrapper);
					is.close();

					// load activator
					Class<?> type;
					// 未配置插件启动类，则使用默认的
					if (StringUtil.isEmpty(pluginConf.getActivator())) {
						type = PluginSupport.class;
					} else {
						// load activator
						type = ClassUtils.classForName(pluginConf.getActivator());
						if (!PluginSupport.class.isAssignableFrom(type))
							throw LangUtil.wrapThrow("插件[%]的activator必须是cn.sunline.ltts.core.api.PluginSupport的子类",
									pluginConf.getId());
					}

					PluginSupport activatorObj = (PluginSupport) type.newInstance();
					activatorObj.setPluginConf(pluginConf);
					pluginConfWrapper.setPluginActivatorObj(activatorObj);
				} catch (Exception ex) {
					PLUGIN_LOGGER.error("加载插件{}失败", ex, resource.getFilename());
					throw LangUtil.wrapThrow("加载插件[%s]失败", ex, resource.getFilename());
				}
			}
			// 检查插件ID的唯一性
			checkIdUnique(pluginConfigs);
			// 对pluginConfigs按照order进行升序排序
			Collections.sort(pluginConfigs, (a, b) -> {
				if (a.getOrder() < b.getOrder())
					return -1;
				else
					return 1;
			});

			dumpLog();
		} catch (Throwable e) {
			PLUGIN_LOGGER.error("加载插件信息失败", e);
			throw LangUtil.wrapThrow("加载插件信息失败", e);
		}
		loadPluginConfsFlag = true;
	}

	private static void checkIdUnique(List<PluginConfWrapper> plugins) {
		Map<String, PluginConfWrapper> checkMap = new HashMap<>();
		for (PluginConfWrapper plugin : plugins) {
			String pluginId = plugin.getPluginConf().getId();
			if (checkMap.get(pluginId) != null) {
				String _pluginFileName = checkMap.get(pluginId).getPluginConfFileName();
				LangUtil.wrapThrow("插件ID[%s]重复. 插件[%s]与插件[%s]", pluginId, plugin.getPluginConfFileName(),
						_pluginFileName);
			}
			checkMap.put(pluginId, plugin);
		}
	}

	public static List<PluginConfWrapper> getPluginConfs() {
		return pluginConfigs;
	}

	/**
	 * 打印到特定日志文件
	 */
	private static void dumpLog() {
		PLUGIN_LOGGER.info("加载插件信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		for (PluginConfWrapper pluginWrapper : getPluginConfs()) {
			PluginConf plugin = pluginWrapper.getPluginConf();
			PLUGIN_LOGGER.info("加载插件[{}][{}][{}]", plugin.getId(), plugin.getDisplayName(), plugin.getOrder());
		}
		PLUGIN_LOGGER.info("加载插件信息>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
	}
}
