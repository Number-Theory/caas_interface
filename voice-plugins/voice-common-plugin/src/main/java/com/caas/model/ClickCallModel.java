package com.caas.model;

/**
 * 双向外呼实体
 * 
 * @author xupiao 2017年10月26日
 *
 */
public class ClickCallModel extends BaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3789505118192635445L;
	private String caller;
	private String called;
	private String displayCaller;
	private String displayCalled;
	private Integer maxDuration; // 最大通话时长
	private String billUrl;
	private String statusUrl;
	private String recordUrl;
	private String userData;
	private String callId;
	private String record;
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRecordUrl() {
		return recordUrl;
	}

	public void setRecordUrl(String recordUrl) {
		this.recordUrl = recordUrl;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
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

	public String getCalled() {
		return called;
	}

	public void setCalled(String called) {
		this.called = called;
	}

	public String getDisplayCaller() {
		return displayCaller;
	}

	public void setDisplayCaller(String displayCaller) {
		this.displayCaller = displayCaller;
	}

	public String getDisplayCalled() {
		return displayCalled;
	}

	public void setDisplayCalled(String displayCalled) {
		this.displayCalled = displayCalled;
	}

	public Integer getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}

	public String getBillUrl() {
		return billUrl;
	}

	public void setBillUrl(String billUrl) {
		this.billUrl = billUrl;
	}

	public String getStatusUrl() {
		return statusUrl;
	}

	public void setStatusUrl(String statusUrl) {
		this.statusUrl = statusUrl;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

}
