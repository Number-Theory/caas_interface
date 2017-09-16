package com.yzx.engine.spi.impl;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.ServiceCallBackPoint;

/**
 * 
 * @author xupiao 2017年6月21日
 *
 */
public class DefaultServiceCallBack implements ServiceCallBackPoint {
	
	protected static final String AUTH_EVENT = "Authentication";
	protected static final String DEDUCTION_EVENT = "Deduction";
	protected static final String REST_EVENT = "Rest";
	protected static final String CONTROL_EVENT = "Control";

	@Override
	public void beforeService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		
	}

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		
	}

	@Override
	public void afterService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		
	}

	@Override
	public void successService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		
	}

	@Override
	public boolean exceptionService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject, Exception e) {
		return true;
	}
	
	public void setResponse(String callId, ServiceResponse response, BusiErrorCode busiErrorCode, String event, String userData) {
		response.setCallId(callId);
		response.setResult(busiErrorCode.getErrCode());
		response.setMessage(busiErrorCode.getErrMsg());
		response.setEvent(event);
		response.setUserData(userData);
	}

}
