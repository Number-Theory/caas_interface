package com.yzx.engine.spi;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;

/**
 * 业务执行点
 * 
 * @author xupiao 2017年6月2日
 *
 */
public interface ServiceCallBackPoint {
	/**
	 * 业务前处理
	 * 
	 * @param requestString
	 */
	public void beforeService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject);

	/**
	 * 业务处理
	 * 
	 * @param requestString
	 */
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject);

	/**
	 * 业务后处理，不管成功都会执行
	 * 
	 * @param requestString
	 */
	public void afterService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject);

	/**
	 * 业务成功后处理
	 * 
	 * @param requestString
	 */
	public void successService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject);

	/**
	 * 业务异常后处理
	 * 
	 * @param request
	 * @param response
	 * @param e
	 * @return Whether throw the exception,If want return true else false.
	 */
	public boolean exceptionService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject, Exception e);
}
