package com.caas.service;

import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

import com.caas.model.MinNumModel;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;

public interface BaseAXService extends Serializable {
	public void axBind(String callId, MinNumModel minNumModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response);

	public void axUnbind(String callId, MinNumModel minNumModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response);
	
	public void onlineCall(String callId, MinNumModel minNumModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response);
}
