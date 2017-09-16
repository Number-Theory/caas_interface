package com.caas.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author xupiao 2017年8月21日
 *
 */
@XmlRootElement(name = "gxInfo")
public class GxInfo extends BaseModel {

	private static final long serialVersionUID = -5913751131615487950L;
	private String requestId;
	private String telA;
	private String telX;
	private String telB;
	private String subts;
	private String name;
	private String cardtype;
	private String cardno;
	private String anucode;
	private String areacode;
	private String expiration;
	private String callrecording;
	private String forceRecord;
	private String subid;
	private String safe_user;
	private String safe_password;
	private String bindId;
	private String xmode;
	private String calldisplay;
	private String portType;

	public String getXmode() {
		return xmode;
	}

	public void setXmode(String mode) {
		this.xmode = mode;
	}

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

	public String getSubts() {
		return subts;
	}

	public void setSubts(String subts) {
		this.subts = subts;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getAnucode() {
		return anucode;
	}

	public void setAnucode(String anucode) {
		this.anucode = anucode;
	}

	public String getAreacode() {
		return areacode;
	}

	public void setAreacode(String areacode) {
		this.areacode = areacode;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String getCallrecording() {
		return callrecording;
	}

	public void setCallrecording(String callrecording) {
		this.callrecording = callrecording;
	}

	public String getSubid() {
		return subid;
	}

	public void setSubid(String subid) {
		this.subid = subid;
	}

	public String getSafe_user() {
		return safe_user;
	}

	public void setSafe_user(String safe_user) {
		this.safe_user = safe_user;
	}

	public String getSafe_password() {
		return safe_password;
	}

	public void setSafe_password(String safe_password) {
		this.safe_password = safe_password;
	}

	public String getBindId() {
		return bindId;
	}

	public void setBindId(String bindId) {
		this.bindId = bindId;
	}

	public String getForceRecord() {
		return forceRecord;
	}

	public void setForceRecord(String forceRecord) {
		this.forceRecord = forceRecord;
	}

	public String getCalldisplay() {
		return calldisplay;
	}

	public void setCalldisplay(String calldisplay) {
		this.calldisplay = calldisplay;
	}

	public String getPortType() {
		return portType;
	}

	public void setPortType(String portType) {
		this.portType = portType;
	}

}
