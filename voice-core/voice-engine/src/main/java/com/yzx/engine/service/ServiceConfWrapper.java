package com.yzx.engine.service;

import com.yzx.access.url.UrlMatcher;


/**
 * 
 * @author xupiao 2017年6月1日
 *
 */
public class ServiceConfWrapper {
	private ServiceConf serviceConf;
	private String serviceConfFileName;
	private ServiceExecutor serviceActivatorObj;
	private UrlMatcher urlMatcher;

	public ServiceConf getServiceConf() {
		return serviceConf;
	}

	public void setServiceConf(ServiceConf serviceConf) {
		this.serviceConf = serviceConf;
	}

	public String getServiceConfFileName() {
		return serviceConfFileName;
	}

	public void setServiceConfFileName(String serviceConfFileName) {
		this.serviceConfFileName = serviceConfFileName;
	}

	public ServiceExecutor getServiceActivatorObj() {
		return serviceActivatorObj;
	}

	public void setServiceActivatorObj(ServiceExecutor serviceActivatorObj) {
		this.serviceActivatorObj = serviceActivatorObj;
	}

	public UrlMatcher getUrlMatcher() {
		return urlMatcher;
	}

	public void setUrlMatcher(UrlMatcher urlMatcher) {
		this.urlMatcher = urlMatcher;
	}
}