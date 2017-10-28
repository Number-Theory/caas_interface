package com.caas.model.callback.safetycall;

import com.caas.model.BaseModel;

/**
 * 
 * @author xupiao 2017年9月17日
 *
 */
public class SafetyCallBillModel extends BaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2997381018785249572L;

	private String userId;
	private String bindId;
	private String callId;
	private String caller;
	private String callee;
	private String calleeCityCode;
	private String calleeDisplay;
	private String userData;
	private String record;
	private String flag;
	private String dstVirtualNum;
	private String callTime;
	private String beginTime;
	private String endTime;
	private String callStatus;
	private String recordUrl;

	public String getCalleeCityCode() {
		return calleeCityCode;
	}

	public void setCalleeCityCode(String calleeCityCode) {
		this.calleeCityCode = calleeCityCode;
	}

	public String getCalleeDisplay() {
		return calleeDisplay;
	}

	public void setCalleeDisplay(String calleeDisplay) {
		this.calleeDisplay = calleeDisplay;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBindId() {
		return bindId;
	}

	public void setBindId(String bindId) {
		this.bindId = bindId;
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

	public String getDstVirtualNum() {
		return dstVirtualNum;
	}

	public void setDstVirtualNum(String dstVirtualNum) {
		this.dstVirtualNum = dstVirtualNum;
	}

	public String getCallTime() {
		return callTime;
	}

	public void setCallTime(String callTime) {
		this.callTime = callTime;
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

	public String getRecordUrl() {
		return recordUrl;
	}

	public void setRecordUrl(String recordUrl) {
		this.recordUrl = recordUrl;
	}

}
