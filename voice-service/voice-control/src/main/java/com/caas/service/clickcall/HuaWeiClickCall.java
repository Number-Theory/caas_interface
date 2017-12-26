package com.caas.service.clickcall;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.caas.model.ClickCallModel;
import com.caas.model.HuaWeiClickcallModel;
import com.caas.util.HttpUtilsForHw;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.EncryptUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

@Service
public class HuaWeiClickCall extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(HuaWeiClickCall.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		ClickCallModel clickCallModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<ClickCallModel>() {
		}.getType());

		logger.info("【接收到Rest组件请求信息】clickCallModel={}", clickCallModel);

		// 封装参数请求华为
		HuaWeiClickcallModel huaWeiClickcallModel = new HuaWeiClickcallModel();
		String accessToken = RedisOpClient.get(RedisKeyConsts.HW_APPTOKE);
		String appKey = ConfigUtils.getProperty("hw_clickcall_appkey", String.class);

		// huaWeiClickcallModel.setAccessToken(accessToken);
		// huaWeiClickcallModel.setAppKey(appKey);
		huaWeiClickcallModel.setBindNbr("+862835084335");
		huaWeiClickcallModel.setCalleeNbr(addPlus86MobileNationPrefix(clickCallModel.getCalled()));
		huaWeiClickcallModel.setCallerNbr(addPlus86MobileNationPrefix(clickCallModel.getCaller()));
		if (StringUtil.isNotEmpty(clickCallModel.getDisplayCalled())) {
			huaWeiClickcallModel.setDisplayCalleeNbr(addPlus86MobileNationPrefix(clickCallModel.getDisplayCalled()));
		}
		if (StringUtil.isNotEmpty(clickCallModel.getDisplayCaller())) {
			huaWeiClickcallModel.setDisplayNbr(addPlus86MobileNationPrefix(clickCallModel.getDisplayCaller()));
		}
		EncryptUtil encryptUtil = new EncryptUtil();
		try {
			huaWeiClickcallModel.setFeeUrl(encryptUtil.base64Encoder(ConfigUtils.getProperty("hw_callback_bill", String.class)));
			huaWeiClickcallModel.setStatusUrl(encryptUtil.base64Encoder(ConfigUtils.getProperty("hw_callback_status", String.class))); // TODO
		} catch (Exception e) {
		}
		huaWeiClickcallModel.setMaxDuration(clickCallModel.getMaxDuration());

		if ("1".equals(clickCallModel.getRecord())) {
			huaWeiClickcallModel.setRecordFlag("true");
		}

		String body = JsonUtil.toJsonStr(huaWeiClickcallModel);
		logger.info("【请求华为点击呼叫接口参数】body={}", body);

		// 封装请求华为点击呼叫路径
		String url = ConfigUtils.getProperty("baseUrl_huawei_clickcall", String.class);
		String params = "?app_key=" + appKey + "&access_token=" + accessToken + "&format=json";
		logger.info("【请求华为点击呼叫接口路径】url={}", url);

		// 请求华为点击呼叫接口
		String respData = HttpUtilsForHw.postJSON(url + params, body);
		logger.info("【请求华为点击呼叫接口路径】返回结果resp={}", respData);

		if (StringUtil.isNotEmpty(respData)) {
			JSONObject fromJson = JSONObject.parseObject(respData);

			String resultcode = fromJson.getString("resultcode");

			if ("0".equals(resultcode)) {
				String sessionId = fromJson.getString("sessionId");
				setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_000000, CONTROL_EVENT, "");
				RedisOpClient.setAndExpire(RedisKeyConsts.getKey(RedisKeyConsts.CB_SESSION, clickCallModel.getCallId()), sessionId,
						RedisKeyConsts.CB_SESSION_EXPIRE);
				RedisOpClient.setAndExpire(RedisKeyConsts.getKey(RedisKeyConsts.CB_REQUEST, sessionId), JsonUtil.toJsonStr(clickCallModel),
						RedisKeyConsts.CB_REQUEST_EXPIRE);
			} else {
				setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_100036, CONTROL_EVENT, "");
			}
		} else {
			setResponse(clickCallModel.getCallId(), response, BusiErrorCode.B_900000, CONTROL_EVENT, "");
		}
	}
}
