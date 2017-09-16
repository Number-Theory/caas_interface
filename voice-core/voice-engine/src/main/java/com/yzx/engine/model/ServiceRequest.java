package com.yzx.engine.model;

import io.netty.handler.codec.http.HttpRequest;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author xupiao 2017年6月2日
 *
 */
public class ServiceRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4019281722606023411L;

	private String serviceId;
	private String requestString;
	private Map<String, Object> paramsObject = new HashMap<String, Object>();
	private HttpRequest httpRequest;
	private String logId;

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public Map<String, Object> getParamsObject() {
		return paramsObject;
	}

	public void setParamsObject(Map<String, Object> paramsObject) {
		this.paramsObject = paramsObject;
	}

	public HttpRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getRequestString() {
		return requestString;
	}

	public void setRequestString(String requestString) {
		this.requestString = requestString;
	}
}
