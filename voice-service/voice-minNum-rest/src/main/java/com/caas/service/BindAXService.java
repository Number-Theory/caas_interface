package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
import com.caas.model.MinNumModel;
import com.caas.service.impl.GxAXService;
import com.caas.util.CommonUtils;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.access.util.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.CommonCheckUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.Log4jUtils;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * 
 * @author xupiao 2017年8月21日
 *
 */
@Service
public class BindAXService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(BindAXService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		final String callId = UUID.randomUUID().toString().replace("-", "");
		final String bindId = UUID.randomUUID().toString().replace("-", "");

		// 解析header字段获取Authorization字段
		HttpRequest httpRequest = (HttpRequest) request.getHttpRequest();
		String authorization = httpRequest.headers().get("Authorization");

		// 获取signature字段
		String signature = httpRequest.headers().get("sig");

		// 获取主账户ID
		String userId = ObjectUtils.defaultIfNull(paramsObject.get("userId").toString(), "");

		// 获取客户端的IP地址
		String clientIp = HttpUtils.getClientIp(httpRequest, ctx);

		// 解析用户传入的字段
		final MinNumModel minNumModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<MinNumModel>() {
		}.getType());
		minNumModel.setBindId(bindId);
		minNumModel.setUserId(userId);

		String caller = minNumModel.getCaller();
		String dstVirtualNum = minNumModel.getDstVirtualNum();
		String userData = minNumModel.getUserData();
		String cityId = minNumModel.getCityId();
		String maxAge = minNumModel.getMaxAge();
		String record = minNumModel.getRecord();

		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			minNumModel.setUserData(userData.substring(0, 128));
		}

		// 主叫号码非空校验
		if (StringUtil.isBlank(caller)) {
			setResponse(callId, response, BusiErrorCode.B_100020, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}
		// 判断主叫号码
		caller = CommonUtils.getSimplePhone(caller);
		if (!CommonUtils.isPhoneNum(caller)) {
			setResponse(callId, response, BusiErrorCode.B_100021, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		if (StringUtil.isBlank(maxAge)) {
			maxAge = ConfigUtils.getProperty("lifeTime", "1800", String.class); // 1800秒
			minNumModel.setMaxAge(maxAge);
		} else {
			if (!CommonCheckUtil.isNumber(maxAge)) {
				maxAge = ConfigUtils.getProperty("lifeTime", "1800", String.class);
				minNumModel.setMaxAge(maxAge);
			}
		}

		// 录音字段
		if (StringUtil.isBlank(record) || !"1".equals(record)) {
			record = "0";
			minNumModel.setRecord(record);
		}

		if (StringUtil.isBlank(minNumModel.getCalldisplay()) || !"1".equals(minNumModel.getCalldisplay())) {
			minNumModel.setCalldisplay("0");
		}

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("phoneNumber", dstVirtualNum);
		paramMap.put("userId", userId);
		paramMap.put("productType", "1");
		Map<String, Object> numberMap = dao.selectOne("common.getNumByUserIdAndProductType", paramMap);
		if (null == numberMap || numberMap.size() <= 0) {
			logger.error("【查询号码归属平台】失败dstVirtualNum={}", dstVirtualNum);
			setResponse(callId, response, BusiErrorCode.B_100024, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		if (StringUtil.isEmpty(cityId)) {
			minNumModel.setCityId((String) numberMap.get("cityCode"));
		}

		// 请求公共鉴权组件
		AuthModel authModel = new AuthModel();
		authModel.setAuth(authorization);
		authModel.setSig(signature);
		authModel.setCallID(callId);
		authModel.setIpWhiteList(clientIp);
		authModel.setEvent(REST_EVENT);
		authModel.setPhoneNum(caller);
		authModel.setProductType("1"); // 虚拟小号
		authModel.setUserID(userId);
		authModel.setNeedBalance("0");

		String authStr = JsonUtil.toJsonStr(authModel);
		String authUrl = ConfigUtils.getProperty("caas_auth_url", String.class) + "/voiceAuth/caasCalls";
		logger.info("请求caas-auth组件安全鉴权包体信息authStr={},authUrl={}", authStr, authUrl);
		try {
			new HttpClient(new ClientHandler() {
				@Override
				public void execute(HttpResponse httpResponse, String context) {
					Log4jUtils.initLog4jContext(request.getLogId());
					logger.info("caas-auth组件返回结果authResult={}", context);
					ServiceResponse authResponse = JsonUtil.fromJson(context, new TypeToken<ServiceResponse>() {
					}.getType());
					if (BusiErrorCode.B_000000.getErrCode().equals(authResponse.getResult())) {

						String className = "com.caas.service.impl.GxAXService";// TODO
						BaseAXService axbService = new GxAXService();
						axbService.axBind(callId, minNumModel, ctx, request, response);
						RedisOpClient.set(RedisKeyConsts.getKey("apiServer:", bindId), className);

					} else {
						// 将鉴权的错误结果异步写回客户端
						HttpUtils.sendMessageJson(ctx, authResponse.toString());
					}
				}

				@Override
				public void failed(Exception ex) {
					Log4jUtils.initLog4jContext(request.getLogId());
					logger.info("请求caas_auth组件失败,ex={}", ex);
					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, minNumModel.getUserData());
					HttpUtils.sendMessageJson(ctx, response.toString());
				}
			}).httpPostBack(authUrl, authStr);
		} catch (Exception e) {
			logger.info("请求caas_auth组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
		}
	}
}
