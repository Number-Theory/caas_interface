package com.caas.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.caas.model.ControlModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
@Service
public class ControlService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(ControlService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		super.callService(ctx, request, response, paramsObject);
		ControlModel controlModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<ControlModel>() {
		}.getType());
		
		String productType = controlModel.getProductType();
		if ("0".equals(productType)) { // 隐号
		} else if ("1".equals(productType)) { // 小号
			
		} else if ("2".equals(productType)) { // 回拨
		} else if ("3".equals(productType)) { // 语音验证码
		} else if ("4".equals(productType)) { // 语音通知
		}
		
	}
}
