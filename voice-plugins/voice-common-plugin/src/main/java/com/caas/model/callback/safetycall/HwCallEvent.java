package com.caas.model.callback.safetycall;


public class HwCallEvent {

	//呼叫唯一标识呼叫唯一标识
	private String callIdentifier;
	//真实被叫号码
	private String called;
	//真实主叫号码
	private String calling;
	/*
	 * 呼叫状态事件
	 * IDP：呼叫开始
	 * Answer：应答
	 * Release：呼叫结束包括
	 * Exception 呼叫过程中发生的异常固定
	 */
	private String event;
	//扩展呼叫事件信息
	private HwExtensionInfoType extInfo;
	//是否录音  
	private String isRecord;
	//通知模式 Notify：App不对呼叫进行控制 Block：App可以指示小号业务平台做呼叫控制 
	private String notificationMode;
	//呼叫事件发生的时间戳
	private String  timeStamp;
	//虚拟号码
	private String virtualNumber;


	public String getCallIdentifier() {
		return callIdentifier;
	}


	public void setCallIdentifier(String callIdentifier) {
		this.callIdentifier = callIdentifier;
	}


	public String getCalled() {
		return called;
	}


	public void setCalled(String called) {
		this.called = called;
	}


	public String getCalling() {
		return calling;
	}


	public void setCalling(String calling) {
		this.calling = calling;
	}


	public String getEvent() {
		return event;
	}


	public void setEvent(String event) {
		this.event = event;
	}


	public HwExtensionInfoType getExtInfo() {
		return extInfo;
	}


	public void setExtInfo(HwExtensionInfoType extInfo) {
		this.extInfo = extInfo;
	}


	public String getIsRecord() {
		return isRecord;
	}


	public void setIsRecord(String isRecord) {
		this.isRecord = isRecord;
	}


	public String getNotificationMode() {
		return notificationMode;
	}


	public void setNotificationMode(String notificationMode) {
		this.notificationMode = notificationMode;
	}


	public String getTimeStamp() {
		return timeStamp;
	}


	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}


	public String getVirtualNumber() {
		return virtualNumber;
	}


	public void setVirtualNumber(String virtualNumber) {
		this.virtualNumber = virtualNumber;
	}

	
	
	
	
}
