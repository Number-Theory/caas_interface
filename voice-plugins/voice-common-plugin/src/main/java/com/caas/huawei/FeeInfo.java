package com.caas.huawei;

import com.caas.model.BaseModel;

/**
 * 
 * @author xupiao 2017年11月2日
 *
 */
public class FeeInfo extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4150780819353331619L;

	private String direction;
	private String spId;
	private String appKey;
	private String icid;
	private String bindNum;
	private String sessionId;
	private String callerNum;
	private String calleeNum;
	private String origCalleeNum;
	private String fwdDisplayNum;
	private String fwdDstNum;
	private String callInTime;
	private String fwdStartTime;
	private String fwdAlertingTime;
	private String fwdAnswerTime;
	private String callEndTime;
	private String fwdUnaswRsn;
	private String failTime;
	private String ulFailReason;
	private String sipStatusCode;
	private String callOutStartTime;
	private String callOutAlertingTime;
	private String callOutAnswerTime;
	private String callOutUnaswRsn;
	private String dynIVRStartTime;
	private String dynIVRPath;
	private String recordFlag;
	private String recordStartTime;
	private String recordObjectName;
	private String recordBucketName;
	private String recordDomain;
	private String recordFileDownloadUrl;
	private String recordPushURL;
	private String ttsPlayTimes;
	private String ttsTransDuration;
	private String createMptyTime;
	private String joinMptyTime;
	private String mptyId;
	private String callType;
	private String endToEndID;
	private String serviceType;
	private String billType;
	private String subbillType;
	private String callerType;
	private String calleeType;
	private String hostName;
	private String userData;

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getSpId() {
		return spId;
	}

	public void setSpId(String spId) {
		this.spId = spId;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getIcid() {
		return icid;
	}

	public void setIcid(String icid) {
		this.icid = icid;
	}

	public String getBindNum() {
		return bindNum;
	}

	public void setBindNum(String bindNum) {
		this.bindNum = bindNum;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getCallerNum() {
		return callerNum;
	}

	public void setCallerNum(String callerNum) {
		this.callerNum = callerNum;
	}

	public String getCalleeNum() {
		return calleeNum;
	}

	public void setCalleeNum(String calleeNum) {
		this.calleeNum = calleeNum;
	}

	public String getOrigCalleeNum() {
		return origCalleeNum;
	}

	public void setOrigCalleeNum(String origCalleeNum) {
		this.origCalleeNum = origCalleeNum;
	}

	public String getFwdDisplayNum() {
		return fwdDisplayNum;
	}

	public void setFwdDisplayNum(String fwdDisplayNum) {
		this.fwdDisplayNum = fwdDisplayNum;
	}

	public String getFwdDstNum() {
		return fwdDstNum;
	}

	public void setFwdDstNum(String fwdDstNum) {
		this.fwdDstNum = fwdDstNum;
	}

	public String getCallInTime() {
		return callInTime;
	}

	public void setCallInTime(String callInTime) {
		this.callInTime = callInTime;
	}

	public String getFwdStartTime() {
		return fwdStartTime;
	}

	public void setFwdStartTime(String fwdStartTime) {
		this.fwdStartTime = fwdStartTime;
	}

	public String getFwdAlertingTime() {
		return fwdAlertingTime;
	}

	public void setFwdAlertingTime(String fwdAlertingTime) {
		this.fwdAlertingTime = fwdAlertingTime;
	}

	public String getFwdAnswerTime() {
		return fwdAnswerTime;
	}

	public void setFwdAnswerTime(String fwdAnswerTime) {
		this.fwdAnswerTime = fwdAnswerTime;
	}

	public String getCallEndTime() {
		return callEndTime;
	}

	public void setCallEndTime(String callEndTime) {
		this.callEndTime = callEndTime;
	}

	public String getFwdUnaswRsn() {
		return fwdUnaswRsn;
	}

	public void setFwdUnaswRsn(String fwdUnaswRsn) {
		this.fwdUnaswRsn = fwdUnaswRsn;
	}

	public String getFailTime() {
		return failTime;
	}

	public void setFailTime(String failTime) {
		this.failTime = failTime;
	}

	public String getUlFailReason() {
		return ulFailReason;
	}

	public void setUlFailReason(String ulFailReason) {
		this.ulFailReason = ulFailReason;
	}

	public String getSipStatusCode() {
		return sipStatusCode;
	}

	public void setSipStatusCode(String sipStatusCode) {
		this.sipStatusCode = sipStatusCode;
	}

	public String getCallOutStartTime() {
		return callOutStartTime;
	}

	public void setCallOutStartTime(String callOutStartTime) {
		this.callOutStartTime = callOutStartTime;
	}

	public String getCallOutAlertingTime() {
		return callOutAlertingTime;
	}

	public void setCallOutAlertingTime(String callOutAlertingTime) {
		this.callOutAlertingTime = callOutAlertingTime;
	}

	public String getCallOutAnswerTime() {
		return callOutAnswerTime;
	}

	public void setCallOutAnswerTime(String callOutAnswerTime) {
		this.callOutAnswerTime = callOutAnswerTime;
	}

	public String getCallOutUnaswRsn() {
		return callOutUnaswRsn;
	}

	public void setCallOutUnaswRsn(String callOutUnaswRsn) {
		this.callOutUnaswRsn = callOutUnaswRsn;
	}

	public String getDynIVRStartTime() {
		return dynIVRStartTime;
	}

	public void setDynIVRStartTime(String dynIVRStartTime) {
		this.dynIVRStartTime = dynIVRStartTime;
	}

	public String getDynIVRPath() {
		return dynIVRPath;
	}

	public void setDynIVRPath(String dynIVRPath) {
		this.dynIVRPath = dynIVRPath;
	}

	public String getRecordFlag() {
		return recordFlag;
	}

	public void setRecordFlag(String recordFlag) {
		this.recordFlag = recordFlag;
	}

	public String getRecordStartTime() {
		return recordStartTime;
	}

	public void setRecordStartTime(String recordStartTime) {
		this.recordStartTime = recordStartTime;
	}

	public String getRecordObjectName() {
		return recordObjectName;
	}

	public void setRecordObjectName(String recordObjectName) {
		this.recordObjectName = recordObjectName;
	}

	public String getRecordBucketName() {
		return recordBucketName;
	}

	public void setRecordBucketName(String recordBucketName) {
		this.recordBucketName = recordBucketName;
	}

	public String getRecordDomain() {
		return recordDomain;
	}

	public void setRecordDomain(String recordDomain) {
		this.recordDomain = recordDomain;
	}

	public String getRecordFileDownloadUrl() {
		return recordFileDownloadUrl;
	}

	public void setRecordFileDownloadUrl(String recordFileDownloadUrl) {
		this.recordFileDownloadUrl = recordFileDownloadUrl;
	}

	public String getRecordPushURL() {
		return recordPushURL;
	}

	public void setRecordPushURL(String recordPushURL) {
		this.recordPushURL = recordPushURL;
	}

	public String getTtsPlayTimes() {
		return ttsPlayTimes;
	}

	public void setTtsPlayTimes(String ttsPlayTimes) {
		this.ttsPlayTimes = ttsPlayTimes;
	}

	public String getTtsTransDuration() {
		return ttsTransDuration;
	}

	public void setTtsTransDuration(String ttsTransDuration) {
		this.ttsTransDuration = ttsTransDuration;
	}

	public String getCreateMptyTime() {
		return createMptyTime;
	}

	public void setCreateMptyTime(String createMptyTime) {
		this.createMptyTime = createMptyTime;
	}

	public String getJoinMptyTime() {
		return joinMptyTime;
	}

	public void setJoinMptyTime(String joinMptyTime) {
		this.joinMptyTime = joinMptyTime;
	}

	public String getMptyId() {
		return mptyId;
	}

	public void setMptyId(String mptyId) {
		this.mptyId = mptyId;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getEndToEndID() {
		return endToEndID;
	}

	public void setEndToEndID(String endToEndID) {
		this.endToEndID = endToEndID;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getBillType() {
		return billType;
	}

	public void setBillType(String billType) {
		this.billType = billType;
	}

	public String getSubbillType() {
		return subbillType;
	}

	public void setSubbillType(String subbillType) {
		this.subbillType = subbillType;
	}

	public String getCallerType() {
		return callerType;
	}

	public void setCallerType(String callerType) {
		this.callerType = callerType;
	}

	public String getCalleeType() {
		return calleeType;
	}

	public void setCalleeType(String calleeType) {
		this.calleeType = calleeType;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}
}