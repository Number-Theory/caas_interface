package com.yzx.auth.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
@XmlRootElement(name = "plugin")
@XmlType(propOrder = { "id", "displayName", "activator", "order", "enable", "failOnInitError" })
public class PluginConf {
	// 插件ID
	private String id = "";
	// 插件中文名称
	private String displayName = "";
	// 插件Activator ,必须继承 com.yzx.auth.service.PluginSupport
	private String activator = "";
	// 插件加载顺序
	private int order = 0;
	// 插件是否启动
	private boolean enable = true;
	// 失败是否终止
	private boolean failOnInitError = true;

	@XmlAttribute
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	@XmlAttribute
	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	@XmlAttribute
	public boolean isFailOnInitError() {
		return failOnInitError;
	}

	public void setFailOnInitError(boolean failOnInitError) {
		this.failOnInitError = failOnInitError;
	}

	@XmlAttribute(name = "activator")
	public String getActivator() {
		return activator;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}

	@XmlAttribute
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}
