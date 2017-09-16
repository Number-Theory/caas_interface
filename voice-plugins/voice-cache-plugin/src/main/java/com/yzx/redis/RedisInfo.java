package com.yzx.redis;

import java.io.Serializable;

public class RedisInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5268054748613265474L;

	private String maxActive;
	private String ip;
	private String port;
	private String password;
	private String maxIdle;
	private String maxWait;
	private String testOnBorrow;
	private String timeout;
	public String getMaxActive() {
		return maxActive;
	}
	public void setMaxActive(String maxActive) {
		this.maxActive = maxActive;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getMaxIdle() {
		return maxIdle;
	}
	public void setMaxIdle(String maxIdle) {
		this.maxIdle = maxIdle;
	}
	public String getMaxWait() {
		return maxWait;
	}
	public void setMaxWait(String maxWait) {
		this.maxWait = maxWait;
	}
	public String getTestOnBorrow() {
		return testOnBorrow;
	}
	public void setTestOnBorrow(String testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTimeout() {
		return timeout;
	}
	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}
	
}
