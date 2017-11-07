package com.caas.service.clickcall;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.caas.model.ClickCallModel;
import com.caas.util.HttpUtilsForHw;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

@Service
public class HuaWeiCancelClickCall extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(HuaWeiCancelClickCall.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		ClickCallModel clickCallModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<ClickCallModel>() {
		}.getType());

		logger.info("【接收到Rest组件请求信息】clickCallModel={}", clickCallModel);

		// 封装参数请求华为
		String accessToken = ConfigUtils.getProperty("hw_clickcall_accessToken", String.class);
		String appKey = ConfigUtils.getProperty("hw_clickcall_appkey", String.class);

		Map<String, Object> params = new HashMap<String, Object>();
		String sessionId = RedisOpClient.get(RedisKeyConsts.getKey(RedisKeyConsts.CB_SESSION, clickCallModel.getCallId()));
		if (StringUtil.isBlank(sessionId)) {
			setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_100037, CONTROL_EVENT, "");
			return;
		}
		params.put("sessionid", sessionId);
		params.put("signal", "call_stop");

		String body = JsonUtil.toJsonStr(params);
		logger.info("【请求华为取消点击呼叫接口参数】body={}", body);

		// 封装请求华为取消点击呼叫路径
		String url = ConfigUtils.getProperty("baseUrl_huawei_cancelclickcall", String.class) + "?app_key=" + appKey + "&access_token=" + accessToken
				+ "&format=json";
		logger.info("【请求华为取消点击呼叫接口路径】url={}", url);

		// 请求华为取消点击呼叫接口
		String respData = HttpUtilsForHw.postJSON(url, body);
		logger.info("【请求华为取消点击呼叫接口路径】返回结果resp={}", respData);

		if (null != respData && respData != "") {
			JSONObject fromJson = JSONObject.parseObject(respData);
			String resultcode = fromJson.getString("resultcode");
			if ("0".equals(resultcode)) {
				setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_000000, CONTROL_EVENT, "");
				RedisOpClient.delKey(RedisKeyConsts.getKey(RedisKeyConsts.CB_SESSION, clickCallModel.getCallId()));
			} else {
				setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_100036, CONTROL_EVENT, "");
			}
		} else {
			setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_900000, CONTROL_EVENT, "");
		}
	}
}
