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
	private String calleeDisplayNum;
	private String callTime;
	private String callStatus;
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
	public String getCalleeDisplayNum() {
		return calleeDisplayNum;
	}
	public void setCalleeDisplayNum(String calleeDisplayNum) {
		this.calleeDisplayNum = calleeDisplayNum;
	}
	public String getCallTime() {
		return callTime;
	}
	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}
	public String getCallStatus() {
		return callStatus;
	}
	public void setCallStatus(String callStatus) {
		this.callStatus = callStatus;
	}
	
	
}
