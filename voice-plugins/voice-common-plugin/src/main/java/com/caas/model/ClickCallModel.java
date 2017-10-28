package com.caas.model;

/**
 * 双向外呼实体
 * 
 * @author xupiao 2017年10月26日
 *
 */
public class ClickCallModel {
	private String caller;
	private String callee;
	private String displayCaller;
	private String displayCallee;
	private Integer maxDuration; // 最大通话时长
	private String billUrl;
	private String stausUrl;
	private String userData;

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

	public String getDisplayCaller() {
		return displayCaller;
	}

	public void setDisplayCaller(String displayCaller) {
		this.displayCaller = displayCaller;
	}

	public String getDisplayCallee() {
		return displayCallee;
	}

	public void setDisplayCallee(String displayCallee) {
		this.displayCallee = displayCallee;
	}

	public Integer getMaxDuration() {
		return maxDuration;
	}

	public void setMaxDuration(Integer maxDuration) {
		this.maxDuration = maxDuration;
	}

	public String getBillUrl() {
		return billUrl;
	}

	public void setBillUrl(String billUrl) {
		this.billUrl = billUrl;
	}

	public String getStausUrl() {
		return stausUrl;
	}

	public void setStausUrl(String stausUrl) {
		this.stausUrl = stausUrl;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

}
