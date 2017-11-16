package com.caas.model.callback.safetycall;

import java.util.List;
import java.util.Map;

/**
 * 
 * 华为回调信息事件扩展信息
 * @author chendi
 *
 */
public class HwExtensionInfoType {

	/**
	 * 信令中的被叫号码
	 */
	private String rawCalled;
	/**
	 * 信令中主叫号码的号码属性
	 * 1：SUBSCRIBER
	 2：UNKNOWN
	 4：INTERNATIONAL
	 3：其他
	 */
	private String rawCalledNOA;
	/**
	 * 信令中的主叫号码
	 */
	private String rawCalling;
	/**
	 * 信令中被叫号码的号码属性
	 * 1：SUBSCRIBER
	 2：UNKNOWN
	 4：INTERNATIONAL
	 3：其他
	 */
	private String rawCallingNOA;
	/**
	 * 扩展信息(Key-Value)列表
	 */
	private Map<String, Object> extParas;
	
	
	public String getRawCalled() {
		return rawCalled;
	}
	
	public void setRawCalled(String rawCalled) {
		this.rawCalled = rawCalled;
	}
	public String getRawCalledNOA() {
		return rawCalledNOA;
	}
	public void setRawCalledNOA(String rawCalledNOA) {
		this.rawCalledNOA = rawCalledNOA;
	}
	public String getRawCalling() {
		return rawCalling;
	}
	public void setRawCalling(String rawCalling) {
		this.rawCalling = rawCalling;
	}
	public String getRawCallingNOA() {
		return rawCallingNOA;
	}
	public void setRawCallingNOA(String rawCallingNOA) {
		this.rawCallingNOA = rawCallingNOA;
	}
	public Map<String, Object> getExtParas() {
		return extParas;
	}
	public void setExtParas(Map<String, Object> extParas) {
		this.extParas = extParas;
	}
	
	
	
}
