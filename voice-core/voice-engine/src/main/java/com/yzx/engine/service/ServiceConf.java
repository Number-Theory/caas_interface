package com.yzx.engine.service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
/**
 * 服务接口模型
 * @author xupiao 2017年6月2日
 *
 */
@XmlRootElement(name = "service")
@XmlType(propOrder = { "id", "displayName", "activator", "isAsync" })
public class ServiceConf {
	private String id;
	private String displayName;
	private String activator;
	private Boolean isAsync = false;
	
	@XmlAttribute
	public Boolean getIsAsync() {
		return isAsync;
	}

	public void setIsAsync(Boolean isAsync) {
		this.isAsync = isAsync;
	}

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
	public String getActivator() {
		return activator;
	}

	public void setActivator(String activator) {
		this.activator = activator;
	}
}
