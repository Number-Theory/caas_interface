package com.caas.service.safetyCall;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.caas.model.GxInfo;
import com.caas.util.HttpUtilsForGx;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 小号,绑定AX号码接口
 * 
 * @author xupiao 2017年8月21日
 *
 */
@Service
public class SafetyCallBindAXBService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(SafetyCallBindAXBService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		GxInfo gxInfo = JsonUtil.fromJson(request.getRequestString(), new TypeToken<GxInfo>() {
		}.getType());

		logger.info("【接收到Rest组件请求信息】gxInfo={}", gxInfo);

		String xmode = "mode101";
		// 封装参数请求广西东信
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("requestId", gxInfo.getRequestId());
		param.put("telA", gxInfo.getTelA());
		param.put("telX", gxInfo.getTelX());
		param.put("telB", gxInfo.getTelB());
		param.put("subts", gxInfo.getSubts());
		param.put("anucode", gxInfo.getAnucode());
		param.put("areacode", gxInfo.getAreacode());
		param.put("expiration", gxInfo.getExpiration());
		Map<String, Object> extraMap = new HashMap<String, Object>();
		extraMap.put("callrecording", gxInfo.getCallrecording());
		param.put("extra", extraMap);

		String body = JsonUtil.toJsonStr(param);
		logger.info("【请求广西东信绑定接口参数】body={}", body);

		// 封装请求广西东信的接口路径

		String url = ConfigUtils.getProperty("baseUrl_gx", String.class) + ConfigUtils.getProperty("bindNumberUrl_gx_axb", String.class) + "/" + xmode;
		logger.info("【请求广西东信绑定接口路径】url={}", url);

		// 请求东信绑定接口
		String respData = HttpUtilsForGx.postJSON(url, body, gxInfo.getPortType() == null ? "0" : gxInfo.getPortType());
		logger.info("【请求广西东信绑定接口路径】返回结果resp={}", respData);

		if (null != respData && respData != "") {
			JSONObject fromJson = JSONObject.parseObject(respData);
			setResponse(gxInfo.getRequestId(), response, BusiErrorCode.B_000000, CONTROL_EVENT, "");
			response.getOtherMap().putAll(fromJson);
		} else {
			setResponse(gxInfo.getRequestId(), response, BusiErrorCode.B_900000, CONTROL_EVENT, "");
		}
	}
}
