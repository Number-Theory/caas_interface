package com.yzx.engine.service;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.auth.plugin.SpringContext;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.consts.EnumType.SysErrorCode;
import com.yzx.core.util.ClassUtils;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.ServiceCallBackPoint;

/**
 * 服务接口执行入口
 * 
 * @author xupiao 2017年6月2日
 *
 */
public class ServiceExecutor {
	private static final Logger logger = LogManager.getLogger(ServiceExecutor.class);
	private final ServiceRequest serviceRequest;
	private final ServiceResponse serviceResponse;
	private final ChannelHandlerContext ctx;

	public ServiceExecutor(ChannelHandlerContext ctx, ServiceRequest serviceRequest, ServiceResponse serviceResponse) {
		this.serviceRequest = serviceRequest;
		this.serviceResponse = serviceResponse;
		this.ctx = ctx;
	}

	/**
	 * 执行Service服务接口
	 */
	public Boolean call() {
		try {
			ServiceConfWrapper serviceConfWrapper = ServiceConfManager.getServiceConfWrapper(serviceRequest.getServiceId());
			if (serviceConfWrapper == null) {
				serviceResponse.setResult(SysErrorCode.S_200000.getErrCode());
				serviceResponse.setMessage(SysErrorCode.S_200000.getErrMsg());
				return false;
			} else {
				serviceRequest.getParamsObject().putAll(serviceConfWrapper.getUrlMatcher().match(serviceRequest.getServiceId()).getParameters());
				Class<?> type = ClassUtils.classForName(serviceConfWrapper.getServiceConf().getActivator());
				ServiceCallBackPoint serviceCallBackPoint = SpringContext.getInstance(type);
				try {
					serviceResponse.setResult(BusiErrorCode.B_000000.getErrCode());
					serviceResponse.setMessage(BusiErrorCode.B_000000.getErrMsg());
					serviceCallBackPoint.beforeService(ctx, serviceRequest, serviceResponse, serviceRequest.getParamsObject());
					serviceCallBackPoint.callService(ctx, serviceRequest, serviceResponse, serviceRequest.getParamsObject());
					serviceCallBackPoint.successService(ctx, serviceRequest, serviceResponse, serviceRequest.getParamsObject());
					return serviceConfWrapper.getServiceConf().getIsAsync();
				} catch (Exception e) {
					logger.error("执行业务服务错误：", e);
					if (serviceCallBackPoint.exceptionService(ctx, serviceRequest, serviceResponse, serviceRequest.getParamsObject(), e)) {
						serviceResponse.setResult(SysErrorCode.S_900000.getErrCode());
						serviceResponse.setMessage(SysErrorCode.S_900000.getErrMsg());
						throw new RuntimeException(e);
					} else {
						return serviceConfWrapper.getServiceConf().getIsAsync();
					}
				} finally {
					serviceCallBackPoint.afterService(ctx, serviceRequest, serviceResponse, serviceRequest.getParamsObject());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
