package com.caas.model;


/**
 * 东信AX小号状态回调
 * @author xupiao 2017年8月21日
 *
 */
public class CallinModel extends BaseModel{

	private static final long serialVersionUID = 8032026064679685643L;
	
	//业务 id,消息请求标识
	private String requestId;
	//真实号码
	private String telA;
	//小号号码
	private String telX;
	//对端号码
	private String telB;
	//绑定 Id
	private String subid;
	//呼叫类型
	private String calltype;
	//发起呼叫时间
	private String calltime;
	//通话标识
	private String callid;
	//录音控制
	private String callrecording;
	//Z号码
	private String telZ;
	
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
	public String getCallid() {
		return callid;
	}
	public void setCallid(String callid) {
		this.callid = callid;
	}
	public String getCallrecording() {
		return callrecording;
	}
	public void setCallrecording(String callrecording) {
		this.callrecording = callrecording;
	}
	public String getTelZ() {
		return telZ;
	}
	public void setTelZ(String telZ) {
		this.telZ = telZ;
	}

	
}

