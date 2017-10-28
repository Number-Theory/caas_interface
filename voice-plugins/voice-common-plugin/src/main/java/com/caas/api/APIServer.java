package com.caas.api;

public class APIServer {
	/**
	 * appId
	 */
	private String id;
	/**
	 * class qualified name
	 */
	private String className;
	/**
	 * http proxy
	 */
	private String url;
	/**
	 * api callUrl
	 */
	private String callUrl;
	/**
	 * api cancelUrl
	 */
	private String cancelUrl;
	/**
	 * JSON字符串，用于兼容处理
	 */
	private String useParams;
	/**
	 * 权重
	 */
	private Integer weigth;
	/**
	 * apiType
	 */
	private String apiType;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getUseParams() {
		return useParams;
	}

	public void setUseParams(String useParams) {
		this.useParams = useParams;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCancelUrl() {
		return cancelUrl;
	}

	public void setCancelUrl(String cancelUrl) {
		this.cancelUrl = cancelUrl;
	}

	public String getCallUrl() {
		return callUrl;
	}

	public void setCallUrl(String callUrl) {
		this.callUrl = callUrl;
	}

	public Integer getWeigth() {
		return weigth;
	}

	public void setWeigth(Integer weigth) {
		this.weigth = weigth;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}

	@Override
	public String toString() {
		return "APIServer [id=" + id + ", className=" + className + ", url=" + url + ", callUrl=" + callUrl + ", cancelUrl=" + cancelUrl + ", useParams="
				+ useParams + ", weigth=" + weigth + ", apiType=" + apiType + "]";
	}
}
