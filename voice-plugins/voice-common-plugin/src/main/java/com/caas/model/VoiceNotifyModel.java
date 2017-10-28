package com.caas.model;

public class VoiceNotifyModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5417561332290304750L;

	private String caller; // 语音通知的被叫侧显示的号码
	private String callee; // 被叫号码
	private String type = "1"; // 内容类型：0：文本；1：模板
	private Integer playTimes = 1; //
	private String templateId; // 模板ID,当type为1时必传
	private String content;// 当type为0时：文本内容，平台负责将该内容转成语音，呼通指定号码后，播放该语音文件；当type为1时：表示模板参数值,形式为json格式
	private String hangupUrl; // 话单推送url
	private String userData; // 用户自定义透传字段，128字节大小
	private String userId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getPlayTimes() {
		return playTimes;
	}

	public void setPlayTimes(Integer playTimes) {
		this.playTimes = playTimes;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getHangupUrl() {
		return hangupUrl;
	}

	public void setHangupUrl(String hangupUrl) {
		this.hangupUrl = hangupUrl;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}
}
