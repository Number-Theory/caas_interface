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
public class MinNumBindHWAXService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(MinNumBindHWAXService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		HuaweiBindInfo huaweiBindInfo = JsonUtil.fromJson(request.getRequestString(), new TypeToken<HuaweiBindInfo>() {
		}.getType());

		logger.info("【接收到Rest组件请求信息】huaweiBindInfo={}", huaweiBindInfo);

		// 封装参数请求华为
		//String xmode = huaweiBindInfo.getXmode();
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("requestId", huaweiBindInfo.getRequestId());
		param.put("aParty", huaweiBindInfo.getaParty());
		param.put("virtualNumber", huaweiBindInfo.getVirtualNumber());
		param.put("isRecord", huaweiBindInfo.getIsRecord());
		param.put("calledNumDisplay", huaweiBindInfo.getCalledNumDisplay());
		/*Map<String, Object> extraMap = new HashMap<String, Object>();
		param.put("extParas", extraMap);
*/
		String body = JsonUtil.toJsonStr(param);
		logger.info("【请求华为绑定接口参数】body={}", body);

		// 封装请求华为的接口路径

		String url = ConfigUtils.getProperty("baseUrl_hw", String.class) + ConfigUtils.getProperty("bindNumberUrl_hw_ax", String.class) ;
		logger.info("【请求华为绑定接口路径】url={}", url);
		String appKey = ConfigUtils.getProperty("appKey_hw", String.class);
		logger.info("【请求华为绑定接口路径】appKey={}", appKey);
		String appSecret = ConfigUtils.getProperty("appSecret_hw", String.class);
		logger.info("【请求华为绑定接口路径】appSecret={}", appSecret);

		// 请求华为绑定接口
		String respData = HttpUtilsForHwMinNum.sendPost(appKey, appSecret, url, body);
		logger.info("【请求华为绑定接口路径】返回结果resp={}", respData);

		if (null != respData && respData != "") {
			JSONObject fromJson = JSONObject.parseObject(respData);
			setResponse(huaweiBindInfo.getRequestId(), response, BusiErrorCode.B_000000, CONTROL_EVENT, "");
			response.getOtherMap().putAll(fromJson);
		} else {
			setResponse(huaweiBindInfo.getRequestId(), response, BusiErrorCode.B_900000, CONTROL_EVENT, "");
		}
	}
}
