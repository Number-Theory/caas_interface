package com.caas.service;

import java.io.Serializable;

import io.netty.channel.ChannelHandlerContext;

import com.caas.model.SafetyCallModel;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;

public interface BaseAXBService extends Serializable {
	public void axbBind(String callId, SafetyCallModel safetyCallModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response);

	public void axbUnbind(String callId, SafetyCallModel safetyCallModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response);
}
