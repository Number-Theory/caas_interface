package com.caas.model;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
public class DeductionModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4709667519492306256L;

	private String event;
	private String userId;
	private String productType;
	private String deductionType;
	private String deductionCode;
	private Long deductionMoney;
	private String userData;
	private String nowDate;

	public String getNowDate() {
		return nowDate;
	}

	public void setNowDate(String nowDate) {
		this.nowDate = nowDate;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getDeductionType() {
		return deductionType;
	}

	public void setDeductionType(String deductionType) {
		this.deductionType = deductionType;
	}

	public String getDeductionCode() {
		return deductionCode;
	}

	public void setDeductionCode(String deductionCode) {
		this.deductionCode = deductionCode;
	}

	public Long getDeductionMoney() {
		return deductionMoney;
	}

	public void setDeductionMoney(Long deductionMoney) {
		this.deductionMoney = deductionMoney;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

}
