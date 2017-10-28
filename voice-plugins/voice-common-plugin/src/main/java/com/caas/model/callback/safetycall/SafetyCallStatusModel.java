package com.caas.model.callback.safetycall;

import com.caas.model.BaseModel;

/**
 * 
 * @author xupiao 2017年9月17日
 *
 */
public class SafetyCallStatusModel extends BaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9153868095494846546L;

	private String bindId;
	private String callId;
	private String caller;
	private String callee;
	private String dstVirtualNum;
	private String beginTime;
	private String calleeDisplay;
	private String userData;
	private String flag;
	private String record;

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

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public String getRecord() {
		return record;
	}

	public void setRecord(String record) {
		this.record = record;
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

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}
}
