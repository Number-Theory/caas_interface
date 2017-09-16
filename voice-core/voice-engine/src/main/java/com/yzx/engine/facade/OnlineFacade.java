package com.yzx.engine.facade;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.service.ServiceExecutor;

/**
 * 
 * @author xupiao 2017年6月5日
 *
 */
public class OnlineFacade {
	public static String handler(HttpRequest request, ChannelHandlerContext ctx, String serviceId,
			String requestString, Map<String, Object> paramsObject, String logId) {
		ServiceRequest serviceRequest = new ServiceRequest();
		ServiceResponse serviceResponse = new ServiceResponse();
		serviceRequest.setServiceId(serviceId);
		serviceRequest.setRequestString(requestString);
		serviceRequest.setHttpRequest(request);
		serviceRequest.setParamsObject(paramsObject);
		serviceRequest.setLogId(logId);
		ServiceExecutor serviceExecutor = new ServiceExecutor(ctx, serviceRequest, serviceResponse);
		if (!serviceExecutor.call()) {
			return serviceResponse.toString();
		} else {
			return null;
		}
	}
}
