package com.yzx.auth.service;


/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class PluginConfWrapper implements IOrder {
	private PluginConf pluginConf;
	private String pluginConfFileName;
	private PluginSupport pluginActivatorObj;

	public String getPluginConfFileName() {
		return this.pluginConfFileName;
	}

	public void setPluginConfFileName(String pluginConfFileName) {
		this.pluginConfFileName = pluginConfFileName;
	}

	public PluginSupport getPluginActivatorObj() {
		return this.pluginActivatorObj;
	}

	public void setPluginActivatorObj(PluginSupport pluginActivatorObj) {
		this.pluginActivatorObj = pluginActivatorObj;
	}

	public PluginConf getPluginConf() {
		return this.pluginConf;
	}

	public void setPluginConf(PluginConf pluginConf) {
		this.pluginConf = pluginConf;
	}

	public int getOrder() {
		return this.pluginConf.getOrder();
	}
}