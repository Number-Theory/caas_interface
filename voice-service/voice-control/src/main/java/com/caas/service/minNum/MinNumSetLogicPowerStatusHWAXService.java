package com.caas.service.minNum;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.caas.model.GxInfo;
import com.caas.model.HuaweiBindInfo;
import com.caas.util.HttpUtilsForGx;
import com.caas.util.HttpUtilsForHw;
import com.caas.util.HttpUtilsForHwMinNum;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 小号,ax逻辑开关机接口
 * 
 * @author cd 
 *
 */
@Service
public class MinNumSetLogicPowerStatusHWAXService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(MinNumSetLogicPowerStatusHWAXService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		HuaweiBindInfo huaweiBindInfo = JsonUtil.fromJson(request.getRequestString(), new TypeToken<HuaweiBindInfo>() {
		}.getType());

		logger.info("【接收到Rest组件请求信息】gxInfo={}", huaweiBindInfo);

		// 封装参数请求华为
		//String xmode = huaweiBindInfo.getXmode();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("requestId", huaweiBindInfo.getRequestId());
		param.put("virtualNumber", huaweiBindInfo.getVirtualNumber());
		/*
	    DEFAULT：该小号的所有功能，目前只支持话音功能
		  VOICE：话音功能
 		  SMS：短信功能（目前不支持）
		 */
		param.put("function", "DEFAULT");
		/*
		 * 设置小号状态
		 PowerOn：逻辑开机
 		 PowerOff：逻辑关机 
		 */
		param.put("status", "");
		String body = JsonUtil.toJsonStr(param);
		logger.info("【请求华为设置小号状态接口参数】body={}", body);
		// 封装请求华为的接口路径
		String url = ConfigUtils.getProperty("baseUrl_hw", String.class) + ConfigUtils.getProperty("setNumberStatusUrl_hw_ax", String.class) ;
		logger.info("【请求华为设置小号状态接口路径】url={}", url);
		String appKey = ConfigUtils.getProperty("appKey_hw", String.class)  ;
		logger.info("【请求华为设置小号状态接口路径】appKey={}", appKey);
		String appSecret = ConfigUtils.getProperty("appSecret_hw", String.class) ;
		logger.info("【请求华为设置小号状态接口路径】appSecret={}", appSecret);
		// 请求华为接口
		String respData = HttpUtilsForHwMinNum.sendPost(appKey, appSecret, url, body);
		logger.info("【请求华为设置小号状态接口路径】返回结果resp={}", respData);

		if (StringUtil.isNotEmpty(respData)) {
			JSONObject fromJson = JSONObject.parseObject(respData);
			setResponse(huaweiBindInfo.getRequestId(), response, BusiErrorCode.B_000000, CONTROL_EVENT, "");
			response.getOtherMap().putAll(fromJson);
		} else {
			setResponse(huaweiBindInfo.getRequestId(), response, BusiErrorCode.B_900000, CONTROL_EVENT, "");
		}
	}
}
