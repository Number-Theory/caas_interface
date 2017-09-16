package com.caas.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
@Service
public class IndentService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(IndentService.class);
	
	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		super.callService(ctx, request, response, paramsObject);
	}
}
