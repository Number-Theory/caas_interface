package com.caas.model.callback.voiceCode;

import com.caas.model.BaseModel;

/**
 * 
 * @author xupiao 2017年9月19日
 *
 */
public class VoiceCodeCallbackModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2080215016017319850L;

	private String callId;
	private String caller;
	private String callee;
	private String startTime;
	private String endTime;
	private String status;
	private String userId;

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getCallee() {
		return callee;
	}

	public void setCallee(String callee) {
		this.callee = callee;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
