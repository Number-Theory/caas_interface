package com.caas.model;

/**
 * 
 * @author xupiao 2017年8月23日
 *
 */
public class MinNumModel extends BaseModel {

	private static final long serialVersionUID = -6520859424197943837L;

	private String bindId;// 必选 双方号码+中间号绑定ID，该ID全局唯一
	private String caller;// 必选 主叫号码(必须为11位手机号，号码前加0086，如008613631686024)
	private String callee;// 必选 规则同caller
	private String dstVirtualNum;// 必选 分配的直呼虚拟中间保护号码
	private String calleeDisplayNum;// 被叫显号，如果指定了该字段，被叫透传强显该号码，如果该字段为空，被叫默认显虚拟中间号
	private String maxAge;// 必选主被叫+虚拟保护号码允许合作方最大cache存储时间(单位秒),超过该时间一定要到腾讯平台查询,不能使用cache里的号码对
	private String userData;// 可选 字符串最大长度不超过128字节，该requestId在后面话单和录音URL推送中原样带回
	private String record;// 可选 是否录音，0表示不录音，1表示录音。默认为不录音
	private String statusUrl;// 可选 状态回调通知地址，正式环境可以配置默认推送地址
	private String hangupUrl;// 可选 话单推送地址，不填推到默认协商地址
	private String recordUrl;// 可选 录单URL回调通知地址，不填推到默认协商地址
	private String cityId;// 城市id

	// 新加的字段,标识AX模式是选号还是送号
	private String virtualType;
	// 新加的字段,标识AX模式的身份信息校验
	private String name; //名称
	// 新加的字段，标识AX模式是否显号
	private String calldisplay = "0"; // 来显控制 0：显示真实号码 1：显示 X 号码
	private String cardtype; //证件类型
	private String cardno; //证件号码

	public String getBindId() {
		return bindId;
	}

	public void setBindId(String bindId) {
		this.bindId = bindId;
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

	public String getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(String maxAge) {
		this.maxAge = maxAge;
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

	public String getStatusUrl() {
		return statusUrl;
	}

	public void setStatusUrl(String statusUrl) {
		this.statusUrl = statusUrl;
	}

	public String getHangupUrl() {
		return hangupUrl;
	}

	public void setHangupUrl(String hangupUrl) {
		this.hangupUrl = hangupUrl;
	}

	public String getRecordUrl() {
		return recordUrl;
	}

	public void setRecordUrl(String recordUrl) {
		this.recordUrl = recordUrl;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getVirtualType() {
		return virtualType;
	}

	public void setVirtualType(String virtualType) {
		this.virtualType = virtualType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCalldisplay() {
		return calldisplay;
	}

	public void setCalldisplay(String calldisplay) {
		this.calldisplay = calldisplay;
	}

	public String getCardtype() {
		return cardtype;
	}

	public void setCardtype(String cardtype) {
		this.cardtype = cardtype;
	}

	public String getCardno() {
		return cardno;
	}

	public void setCardno(String cardno) {
		this.cardno = cardno;
	}
}
