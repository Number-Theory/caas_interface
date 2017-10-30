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
	private String callee;
	private String displayCaller;
	private String displayCallee;
	private Integer maxDuration; // 最大通话时长
	private String billUrl;
	private String statusUrl;
	private String userData;
	private String callId;
	private String record;

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

	public String getCallee() {
		return callee;
	}

	public void setCallee(String callee) {
		this.callee = callee;
	}

	public String getDisplayCaller() {
		return displayCaller;
	}

	public void setDisplayCaller(String displayCaller) {
		this.displayCaller = displayCaller;
	}

	public String getDisplayCallee() {
		return displayCallee;
	}

	public void setDisplayCallee(String displayCallee) {
		this.displayCallee = displayCallee;
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
