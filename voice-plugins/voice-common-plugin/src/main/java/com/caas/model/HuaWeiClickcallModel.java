package com.caas.model;

/**
 * 
 * @author xupiao 2017年10月26日
 *
 */
public class HuaWeiClickcallModel extends BaseModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3804665873531369619L;
	private String version;
	private String callId;
	private String accessToken;
	private String appKey;
	private String callerNbr;
	private String calleeNbr;
	private String displayNbr;
	private String displayCalleeNbr;
	private Integer maxDuration;
	private String feeListUrl;
	private String statusNotifyUrl;
	private String waitVoice;
	private String bindNbr;
	private String lastMinVoice;
	private String lastMinToUE = "both";
	private String calleeMedia = "all";
	private String recordFlag = "false";
	private String recordHintTone;
	private String partyTypeRequiredInDisconnect = "true";
//	private String returnIdlePort = "false";
	private String userData;

	public String getBindNbr() {
		return bindNbr;
	}

	public void setBindNbr(String bindNbr) {
		this.bindNbr = bindNbr;
	}

	public String getLastMinVoice() {
		return lastMinVoice;
	}

	public void setLastMinVoice(String lastMinVoice) {
		this.lastMinVoice = lastMinVoice;
	}

	public String getLastMinToUE() {
		return lastMinToUE;
	}

	public void setLastMinToUE(String lastMinToUE) {
		this.lastMinToUE = lastMinToUE;
	}

	public String getCalleeMedia() {
		return calleeMedia;
	}

	public void setCalleeMedia(String calleeMedia) {
		this.calleeMedia = calleeMedia;
	}

	public String getRecordFlag() {
		return recordFlag;
	}

	public void setRecordFlag(String recordFlag) {
		this.recordFlag = recordFlag;
	}

	public String getRecordHintTone() {
		return recordHintTone;
	}

	public void setRecordHintTone(String recordHintTone) {
		this.recordHintTone = recordHintTone;
	}

	public String getPartyTypeRequiredInDisconnect() {
		return partyTypeRequiredInDisconnect;
	}

	public void setPartyTypeRequiredInDisconnect(String partyTypeRequiredInDisconnect) {
		this.partyTypeRequiredInDisconnect = partyTypeRequiredInDisconnect;
	}

//	public String getReturnIdlePort() {
//		return returnIdlePort;
//	}
//
//	public void setReturnIdlePort(String returnIdlePort) {
//		this.returnIdlePort = returnIdlePort;
//	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getCallerNbr() {
		return callerNbr;
	}

	public void setCallerNbr(String callerNbr) {
		this.callerNbr = callerNbr;
	}

	public String getCalleeNbr() {
		return calleeNbr;
	}

	public void setCalleeNbr(String calleeNbr) {
		this.calleeNbr = calleeNbr;
	}

	public String getDisplayNbr() {
		return displayNbr;
	}

	public void setDisplayNbr(String displayNbr) {
		this.displayNbr = displayNbr;
	}

	public String getDisplayCalleeNbr() {
		return displayCalleeNbr;
	}

	public void setDisplayCalleeNbr(String displayCalleeNbr) {
		this.displayCalleeNbr = displayCalleeNbr;
	}

	public Integer getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}

	public String getFeeListUrl() {
		return feeListUrl;
	}

	public void setFeeListUrl(String feeListUrl) {
		this.feeListUrl = feeListUrl;
	}

	public String getStatusNotifyUrl() {
		return statusNotifyUrl;
	}

	public void setStatusNotifyUrl(String statusNotifyUrl) {
		this.statusNotifyUrl = statusNotifyUrl;
	}

	public String getWaitVoice() {
		return waitVoice;
	}

	public void setWaitVoice(String waitVoice) {
		this.waitVoice = waitVoice;
	}

}
