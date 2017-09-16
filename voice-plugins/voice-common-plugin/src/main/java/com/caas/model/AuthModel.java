package com.caas.model;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
public class AuthModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709667519492306256L;

	private String auth;
	private String sig;

	private String event;
	private String userID;
	private String productType;
	private String ipWhiteList;
	private String phoneNum;
	private String callID;
	private String needBalance; // 是否需要校验余额，0：是，1：否
	private String userData;

	public String getNeedBalance() {
		return needBalance;
	}

	public void setNeedBalance(String needBalance) {
		this.needBalance = needBalance;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}

	public String getSig() {
		return sig;
	}

	public void setSig(String sig) {
		this.sig = sig;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getIpWhiteList() {
		return ipWhiteList;
	}

	public void setIpWhiteList(String ipWhiteList) {
		this.ipWhiteList = ipWhiteList;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getCallID() {
		return callID;
	}

	public void setCallID(String callID) {
		this.callID = callID;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}
}
