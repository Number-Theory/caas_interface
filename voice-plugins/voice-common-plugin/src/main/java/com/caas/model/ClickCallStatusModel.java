package com.caas.model;

/**
 * 
 * @author xupiao 2017年11月5日
 *
 */
public class ClickCallStatusModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7909997520427750997L;

	private String callId;
	private String caller;
	private String called;
	private String callStatus;
	private String userData;
	private String eventTime;

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

	public String getCalled() {
		return called;
	}

	public void setCalled(String called) {
		this.called = called;
	}

	public String getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
}
