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
	private String beginTime;
	private String endTime;
	private String callStatus;
	private String userId;
	private String userData;

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

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

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
