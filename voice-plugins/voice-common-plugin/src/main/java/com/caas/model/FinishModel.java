package com.caas.model;

/**
 * 东信AX平台呼叫结束推送携带参数的model
 * 
 * @author xupiao 2017年8月21日
 *
 */
public class FinishModel extends BaseModel {

	private static final long serialVersionUID = -6186475142396533527L;
	//
	private String requestId;
	// 真实号码
	private String telA;
	// 小号号码
	private String telX;
	// 对端号码
	private String telB;
	// 绑定 id
	private String subid;
	// 呼叫类型
	private String calltype;
	// 发起呼叫时间
	private String calltime;
	// 振铃开始时间
	private String ringingtime;
	// 通话开始时间
	private String starttime;
	// 通话结束时间
	private String releasetime;
	// 通话标识
	private String callid;
	// 释放方向1 表示主叫，2 表示被叫，0 表示平台释放
	private String releasedir;
	// 释放原因
	private String releasecause;
	// 录音控制
	private String callrecording;
	// 录音地址 可选
	private String recordUrl;
	// 录音模式 可选
	private String recordMode;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getTelA() {
		return telA;
	}

	public void setTelA(String telA) {
		this.telA = telA;
	}

	public String getTelX() {
		return telX;
	}

	public void setTelX(String telX) {
		this.telX = telX;
	}

	public String getTelB() {
		return telB;
	}

	public void setTelB(String telB) {
		this.telB = telB;
	}

	public String getSubid() {
		return subid;
	}

	public void setSubid(String subid) {
		this.subid = subid;
	}

	public String getCalltype() {
		return calltype;
	}

	public void setCalltype(String calltype) {
		this.calltype = calltype;
	}

	public String getCalltime() {
		return calltime;
	}

	public void setCalltime(String calltime) {
		this.calltime = calltime;
	}

	public String getRingingtime() {
		return ringingtime;
	}

	public void setRingingtime(String ringingtime) {
		this.ringingtime = ringingtime;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getReleasetime() {
		return releasetime;
	}

	public void setReleasetime(String releasetime) {
		this.releasetime = releasetime;
	}

	public String getCallid() {
		return callid;
	}

	public void setCallid(String callid) {
		this.callid = callid;
	}

	public String getReleasedir() {
		return releasedir;
	}

	public void setReleasedir(String releasedir) {
		this.releasedir = releasedir;
	}

	public String getReleasecause() {
		return releasecause;
	}

	public void setReleasecause(String releasecause) {
		this.releasecause = releasecause;
	}

	public String getCallrecording() {
		return callrecording;
	}

	public void setCallrecording(String callrecording) {
		this.callrecording = callrecording;
	}

	public String getRecordUrl() {
		return recordUrl;
	}

	public void setRecordUrl(String recordUrl) {
		this.recordUrl = recordUrl;
	}

	public String getRecordMode() {
		return recordMode;
	}

	public void setRecordMode(String recordMode) {
		this.recordMode = recordMode;
	}

}
