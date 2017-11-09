package com.caas.model;

import java.util.Map;

public class HuaweiBindInfo  extends BaseModel{

	private static final long serialVersionUID = -5913751131615487950L;
	
	private String requestId;
	/**
	 * 中间号
	 */
	private String virtualNumber;
	/**
	 * 主叫号码
	 */
	private String aParty;
	/**
	 * 被叫号码
	 */
	private String bParty;
	/**
	 * 是否需要录音
	 */
	private String isRecord;
	
	/**
	 *  呼叫方向
	 *  0：aParty 和bParty 可以相互呼叫；
	 *  1：只能aParty 呼叫bParty
	 *  2：只能bParty 呼叫aPart
	 *  默认为0
	 */
	private String bindDirection;
	/**
	 * 用于指示解绑方式
	 * 1：按虚拟号码解除绑定
	 * 2：按绑定ID解除绑定关系
	 */
	private String type; 
	/**
	 * 绑定关系id
	 */
	private String subscriptionId;
	
	/**
	 * 号码所属城市的代号
	 */
	private String cityCode;
	/**
	 * 该绑定关系结束日期
	 * 格式：YYYY-MM-DD'T'hh:mm:ss'Z'
	 */
	private String endTime;
	/**
	 * AX 模式下显示号码类型0 显示虚拟号码 1显示被叫号码b
	 */
	private String calledNumDisplay;
	
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getVirtualNumber() {
		return virtualNumber;
	}
	public void setVirtualNumber(String virtualNumber) {
		this.virtualNumber = virtualNumber;
	}
	public String getaParty() {
		return aParty;
	}
	public void setaParty(String aParty) {
		this.aParty = aParty;
	}
	public String getbParty() {
		return bParty;
	}
	public void setbParty(String bParty) {
		this.bParty = bParty;
	}
	public String getIsRecord() {
		return isRecord;
	}
	public void setIsRecord(String isRecord) {
		this.isRecord = isRecord;
	}
	public String getBindDirection() {
		return bindDirection;
	}
	public void setBindDirection(String bindDirection) {
		this.bindDirection = bindDirection;
	}
	public String getCityCode() {
		return cityCode;
	}
	public void setCityCode(String cityCode) {
		this.cityCode = cityCode;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSubscriptionId() {
		return subscriptionId;
	}
	public void setSubscriptionId(String subscriptionId) {
		this.subscriptionId = subscriptionId;
	}
	
	public String getCalledNumDisplay() {
		return calledNumDisplay;
	}
	
	public void setCalledNumDisplay(String calledNumDisplay) {
		this.calledNumDisplay = calledNumDisplay;
	}

	
}
