package com.yzx.engine.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;

import com.yzx.access.url.UrlMatcher;
import com.yzx.access.url.UrlPattern;
import com.yzx.core.util.CoreUtils;
import com.yzx.core.util.LangUtil;
import com.yzx.core.util.XmlConfigManager;

/**
 * 插件配置管理器，用于加载所有插件
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class ServiceConfManager {
	private static final Logger SERVICE_LOGGER = LogManager.getLogger(ServiceConfManager.class);
	private static boolean loadServiceConfsFlag = false;
	// 插件列表
	private static Map<String, ServiceConfWrapper> serviceConfigs = new HashMap<String, ServiceConfWrapper>();

	private static XmlConfigManager xcm = new XmlConfigManager(new Class[] { ServiceConf.class });

	/**
	 * 加载CLASSPATH下的所有插件
	 */
	public static void loadServiceConfs() {
		if (loadServiceConfsFlag)// 已经加载不再重复加载
			return;
		try {
			// 从CLASSPATH中加载 app.service.xml文件
			Resource[] resources = CoreUtils.findResources("classpath*:/service/*.service.xml");
			for (Resource resource : resources) {
				try {
					InputStream is = resource.getInputStream();
					ServiceConf serviceConf = xcm.load(is);

					ServiceConfWrapper serviceConfWrapper = new ServiceConfWrapper();
					serviceConfWrapper.setServiceConf(serviceConf);
					serviceConfWrapper.setServiceConfFileName(serviceConf.getDisplayName());
					UrlMatcher urlMatcher = new UrlPattern(serviceConf.getId());
					serviceConfWrapper.setUrlMatcher(urlMatcher);
					is.close();
					if (serviceConfigs.containsKey(serviceConf.getId())) {
						LangUtil.wrapThrow("接口[%s]id[%s]重复", resource.getFilename(), serviceConf.getId());
					} else {
						serviceConfigs.put(serviceConf.getId(), serviceConfWrapper);
					}
				} catch (Exception ex) {
					SERVICE_LOGGER.error("加载接口[{}]失败", ex, resource.getFilename());
					throw LangUtil.wrapThrow("加载接口[%s]失败", ex, resource.getFilename());
				}
			}
			// 检查插件ID的唯一性
		} catch (Throwable e) {
			SERVICE_LOGGER.error("加载接口信息失败", e);
			throw LangUtil.wrapThrow("加载接口信息失败", e);
		}
		loadServiceConfsFlag = true;
	}

	public static ServiceConfWrapper getServiceConfWrapper(String uri) {
		for (ServiceConfWrapper serviceConfWrapper : serviceConfigs.values()) {
			if (serviceConfWrapper.getUrlMatcher().matches(uri)) {
				return serviceConfWrapper;
			}
		}
		return null;
	}

	public static Map<String, ServiceConfWrapper> getServiceConfigs() {
		return serviceConfigs;
	}

	public static void setServiceConfigs(Map<String, ServiceConfWrapper> serviceConfigs) {
		ServiceConfManager.serviceConfigs = serviceConfigs;
	}
}
