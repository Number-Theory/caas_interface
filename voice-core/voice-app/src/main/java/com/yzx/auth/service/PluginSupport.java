package com.yzx.auth.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.core.util.LangUtil;

/**
 * 抽象插件
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class PluginSupport {
	protected Logger bootLog = LogManager.getLogger(PluginSupport.class);

	private Boolean initSuccess = null;
	private PluginConf pluginConf;

	public Boolean isInitSuccess() {
		if (initSuccess == null)
			throw LangUtil.wrapThrow("[%s]插件还未初始化", pluginConf.getDisplayName());
		return initSuccess;
	}

	public void setInitSuccess(Boolean initSuccess) {
		this.initSuccess = initSuccess;
	}

	public String getDisplayName() {
		return pluginConf.getDisplayName();
	}

	public final boolean initPlugin() {
		if (pluginConf.isEnable()) {
			initService();
			bootLog.info("初始化插件[" + pluginConf.getDisplayName() + "]完成");
		}
		return true;

	}

	public final void startupPlugin() {
		if (pluginConf.isEnable()) {
			startUpService();
			bootLog.info("插件[" + pluginConf.getDisplayName() + "]启动完成");
		}
	}

	public final void shutdownPlugin() {
		shutdownService();
		bootLog.info("插件[" + pluginConf.getDisplayName() + "]已停止");
	}

	public PluginConf getPluginConf() {
		return pluginConf;
	}

	public void setPluginConf(PluginConf pluginConf) {
		this.pluginConf = pluginConf;
	}
	
	public boolean initService() {
    	return true;
    }

    public void startUpService() {
    	
    }

    public void shutdownService() {
    	
    }
}
