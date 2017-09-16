package com.caas.model;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
public class IndentModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709667519492306256L;

	private String event;
	private String userID;
	private String indenttype;
	private String entercloseType;
	private String payment;
	private String gathering;
	private String indentInsidecode;
	private Long payMoney;
	private String userData;

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

	public String getIndenttype() {
		return indenttype;
	}

	public void setIndenttype(String indenttype) {
		this.indenttype = indenttype;
	}

	public String getEntercloseType() {
		return entercloseType;
	}

	public void setEntercloseType(String entercloseType) {
		this.entercloseType = entercloseType;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}

	public String getGathering() {
		return gathering;
	}

	public void setGathering(String gathering) {
		this.gathering = gathering;
	}

	public String getIndentInsidecode() {
		return indentInsidecode;
	}

	public void setIndentInsidecode(String indentInsidecode) {
		this.indentInsidecode = indentInsidecode;
	}

	public Long getPayMoney() {
		return payMoney;
	}

	public void setPayMoney(Long payMoney) {
		this.payMoney = payMoney;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

}
