package com.caas.model;

/**
 * 
 * @author xupiao 2017年11月5日
 *
 */
public class ClickCallBillModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7909997520427750997L;

	private String callId;
	private String caller;
	private String called;
	private String callerDisplay;
	private String calledDisplay;
	private String callTime;
	private String beginTimeA;
	private String beginTimeB;
	private String endTime;
	private String record;
	private String userData;
	private String callStatusA;
	private String callStatusB;

	public String getCallStatusA() {
		return callStatusA;
	}

	public void setCallStatusA(String callStatusA) {
		this.callStatusA = callStatusA;
	}

	public String getCallStatusB() {
		return callStatusB;
	}

	public void setCallStatusB(String callStatusB) {
		this.callStatusB = callStatusB;
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

	public String getCallerDisplay() {
		return callerDisplay;
	}

	public void setCallerDisplay(String callerDisplay) {
		this.callerDisplay = callerDisplay;
	}

	public String getCalledDisplay() {
		return calledDisplay;
	}

	public void setCalledDisplay(String calledDisplay) {
		this.calledDisplay = calledDisplay;
	}

	public String getCallTime() {
		return callTime;
	}

	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}

	public String getBeginTimeA() {
		return beginTimeA;
	}

	public void setBeginTimeA(String beginTimeA) {
		this.beginTimeA = beginTimeA;
	}

	public String getBeginTimeB() {
		return beginTimeB;
	}

	public void setBeginTimeB(String beginTimeB) {
		this.beginTimeB = beginTimeB;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

}
