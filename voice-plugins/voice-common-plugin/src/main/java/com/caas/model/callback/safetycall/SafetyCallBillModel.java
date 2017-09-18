package com.caas.model.callback.safetycall;

import com.caas.model.BaseModel;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

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
	private String dstVirtualNum;
	private String callTime;
	private String ringingTime;
	private String startTime;
	private String endTime;
	private String callStatus;
	private String recordUrl;
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
	public String getRingingTime() {
		return ringingTime;
	}
	public void setRingingTime(String ringingTime) {
		this.ringingTime = ringingTime;
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
