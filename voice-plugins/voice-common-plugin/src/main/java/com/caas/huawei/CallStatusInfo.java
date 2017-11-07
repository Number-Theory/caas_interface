package com.caas.huawei;

import com.caas.model.BaseModel;

/**
 * 
 * @author xupiao 2017年11月2日
 *
 */
public class CallStatusInfo extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4150780819353331619L;

	private String timestamp;
	private String userData;
	private String sessionId;
	private String caller;
	private String called;
	private String partyType;
	private String stateCode;
	private String stateDesc;
	private String origCalleeNum;
	private String displayCallerNum;
	private String recordStartTime;
	private String recordDomain;
	private String recordBucketName;
	private String recordObjectName;
	private String mptyId;
	private String memberNum;
	private String creatorNum;
	private String digits;
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getUserData() {
		return userData;
	}
	public void setUserData(String userData) {
		this.userData = userData;
	}
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
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
	public String getPartyType() {
		return partyType;
	}
	public void setPartyType(String partyType) {
		this.partyType = partyType;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public String getStateDesc() {
		return stateDesc;
	}
	public void setStateDesc(String stateDesc) {
		this.stateDesc = stateDesc;
	}
	public String getOrigCalleeNum() {
		return origCalleeNum;
	}
	public void setOrigCalleeNum(String origCalleeNum) {
		this.origCalleeNum = origCalleeNum;
	}
	public String getDisplayCallerNum() {
		return displayCallerNum;
	}
	public void setDisplayCallerNum(String displayCallerNum) {
		this.displayCallerNum = displayCallerNum;
	}
	public String getRecordStartTime() {
		return recordStartTime;
	}
	public void setRecordStartTime(String recordStartTime) {
		this.recordStartTime = recordStartTime;
	}
	public String getRecordDomain() {
		return recordDomain;
	}
	public void setRecordDomain(String recordDomain) {
		this.recordDomain = recordDomain;
	}
	public String getRecordBucketName() {
		return recordBucketName;
	}
	public void setRecordBucketName(String recordBucketName) {
		this.recordBucketName = recordBucketName;
	}
	public String getRecordObjectName() {
		return recordObjectName;
	}
	public void setRecordObjectName(String recordObjectName) {
		this.recordObjectName = recordObjectName;
	}
	public String getMptyId() {
		return mptyId;
	}
	public void setMptyId(String mptyId) {
		this.mptyId = mptyId;
	}
	public String getMemberNum() {
		return memberNum;
	}
	public void setMemberNum(String memberNum) {
		this.memberNum = memberNum;
	}
	public String getCreatorNum() {
		return creatorNum;
	}
	public void setCreatorNum(String creatorNum) {
		this.creatorNum = creatorNum;
	}
	public String getDigits() {
		return digits;
	}
	public void setDigits(String digits) {
		this.digits = digits;
	}
}