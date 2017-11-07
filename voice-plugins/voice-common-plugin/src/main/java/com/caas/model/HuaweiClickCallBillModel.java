package com.caas.model;

import java.util.List;

import com.caas.huawei.CallStatusInfo;
import com.caas.huawei.FeeInfo;

/**
 * 
 * @author xupiao 2017年11月2日
 *
 */
public class HuaweiClickCallBillModel extends BaseModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1013072076113323L;

	private String eventType;
	private CallStatusInfo statusInfo;
	private List<FeeInfo> feeLst;

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public CallStatusInfo getStatusInfo() {
		return statusInfo;
	}

	public void setStatusInfo(CallStatusInfo statusInfo) {
		this.statusInfo = statusInfo;
	}

	public List<FeeInfo> getFeeLst() {
		return feeLst;
	}

	public void setFeeLst(List<FeeInfo> feeLst) {
		this.feeLst = feeLst;
	}

}
