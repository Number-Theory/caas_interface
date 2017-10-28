package com.caas.model;

public class VoiceCodeModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -602994785548873925L;

	private String content;// 验证码内容，为数字0~9,长度为4或6
	private Integer playTimes = 1; // 语音播放次数，默认为1
	private String callee; // 接收号码
	private String caller; // 显示的主叫号码
	private String userData; // 用户透传数据
	private String hangupUrl; // 话单回调地址
	private String userId;
	

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getPlayTimes() {
		return playTimes;
	}

	public void setPlayTimes(Integer playTimes) {
		this.playTimes = playTimes;
	}

	public String getCallee() {
		return callee;
	}

	public void setCallee(String callee) {
		this.callee = callee;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public String getHangupUrl() {
		return hangupUrl;
	}

	public void setHangupUrl(String hangupUrl) {
		this.hangupUrl = hangupUrl;
	}

}
