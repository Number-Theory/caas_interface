package com.yzx.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.yzx.core.util.JsonUtil;

/**
 * 
 * @author xupiao 2017年6月2日
 *
 */
public class ServiceResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1609683742829283542L;

	private String result;
	private String message;
	private String event;
	private String callId;
	private String userData;
	private Map<String, Object> otherMap = new HashMap<String, Object>();

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public Map<String, Object> getOtherMap() {
		return otherMap;
	}

	public void setOtherMap(Map<String, Object> otherMap) {
		this.otherMap = otherMap;
	}

	@Override
	public String toString() {
		Map<String, Object> map = new HashMap<String, Object>();
		if (null != result && StringUtils.isNotBlank(result)) {
			map.put("result", result);
		}
		if (null != message && StringUtils.isNotBlank(message)) {
			map.put("message", message);
		}
		if (null != event && StringUtils.isNotBlank(event)) {
			map.put("event", event);
		}
		if (null != callId && StringUtils.isNotBlank(callId)) {
			map.put("callId", callId);
		}
		if (null != userData && StringUtils.isNotBlank(userData)) {
			map.put("userData", userData);
		}
		if (otherMap != null && otherMap.size() > 0) {
			map.putAll(otherMap);
		}
		return JsonUtil.toJsonStr(map);
	}
}
