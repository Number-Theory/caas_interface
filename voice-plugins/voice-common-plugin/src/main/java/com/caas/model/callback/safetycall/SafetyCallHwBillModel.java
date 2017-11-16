package com.caas.model.callback.safetycall;

public class SafetyCallHwBillModel {

  /**
   * 商户应用密钥
   */
  private String appKey;
  /**
   * 回调信息
   */
  private HwCallEvent callEvent;


public String getAppKey() {
	return appKey;
}


public void setAppKey(String appKey) {
	this.appKey = appKey;
}


public HwCallEvent getCallEvent() {
	return callEvent;
}


public void setCallEvent(HwCallEvent callEvent) {
	this.callEvent = callEvent;
}
  
}
