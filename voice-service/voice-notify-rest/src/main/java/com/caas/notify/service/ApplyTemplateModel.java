package com.caas.notify.service;

import javax.xml.bind.annotation.XmlRootElement;

import com.caas.model.BaseModel;

/**
 * 
 * @author xupiao 2017年9月21日
 *
 */
@XmlRootElement(name = "vc")
public class ApplyTemplateModel extends BaseModel {

	private static final long serialVersionUID = 4751305911231687431L;

	private String appid;
	private String type;
	private String act;
	private String data;

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
