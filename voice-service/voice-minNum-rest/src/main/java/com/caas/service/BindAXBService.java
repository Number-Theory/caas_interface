package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
import com.caas.model.SafetyCallModel;
import com.caas.service.impl.GxAXBService;
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
public class BindAXBService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(BindAXBService.class);

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
		final SafetyCallModel safetyCallModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<SafetyCallModel>() {
		}.getType());
		safetyCallModel.setBindId(bindId);
		safetyCallModel.setUserId(userId);

		String caller = safetyCallModel.getCaller();
		String dstVirtualNum = safetyCallModel.getDstVirtualNum();
		String callee = safetyCallModel.getCallee();
		String userData = safetyCallModel.getUserData();
		String cityId = safetyCallModel.getCityId();
		String maxAge = safetyCallModel.getMaxAge();
		String record = safetyCallModel.getRecord();

		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			safetyCallModel.setUserData(userData.substring(0, 128));
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

		// 被叫号码非空校验
		if (StringUtil.isBlank(callee)) {
			setResponse(callId, response, BusiErrorCode.B_100020, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}
		// 判断被叫号码
		callee = CommonUtils.getSimplePhone(callee);
		if (!CommonUtils.isPhoneNum(callee)) {
			setResponse(callId, response, BusiErrorCode.B_100021, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		if (StringUtil.isBlank(maxAge)) {
			maxAge = ConfigUtils.getProperty("lifeTime", "1800", String.class); // 1800秒
			safetyCallModel.setMaxAge(maxAge);
		} else {
			if (!CommonCheckUtil.isNumber(maxAge)) {
				maxAge = ConfigUtils.getProperty("lifeTime", "1800", String.class);
				safetyCallModel.setMaxAge(maxAge);
			}
		}

		// 录音字段
		if (StringUtil.isBlank(record) || !"1".equals(record)) {
			record = "0";
			safetyCallModel.setRecord(record);
		}

		String callerDisplay = safetyCallModel.getCallerdisplay();
		String calleeDisplay = safetyCallModel.getCalleedisplay();
		if (!"1".equals(callerDisplay)) {
			callerDisplay = "0";
			safetyCallModel.setCallerdisplay(callerDisplay);
		}
		if (!"1".equals(calleeDisplay)) {
			calleeDisplay = "0";
			safetyCallModel.setCalleedisplay(calleeDisplay);
		}

		String callRestrict = safetyCallModel.getCallRestrict();
		if (!"0".equals(callRestrict) || !"2".equals(callRestrict) || !"3".equals(callRestrict)) {
			callRestrict = "1";
			safetyCallModel.setCallRestrict(callRestrict);
		}

		if (StringUtil.isNotEmpty(dstVirtualNum)) {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("phoneNumber", dstVirtualNum);
			paramMap.put("userId", userId);
			paramMap.put("productType", "0");
			Map<String, Object> numberMap = dao.selectOne("common.getNumByUserIdAndProductType", paramMap);
			if (null == numberMap || numberMap.size() <= 0) {
				logger.error("【查询号码归属平台】失败dstVirtualNum={}", dstVirtualNum);
				setResponse(callId, response, BusiErrorCode.B_100024, REST_EVENT, userData);
				HttpUtils.sendMessageJson(ctx, response.toString());
				return;
			}
			if (StringUtil.isEmpty(cityId)) {
				safetyCallModel.setCityId((String) numberMap.get("cityCode"));
			}
			
		} else {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId", userId);
			paramMap.put("productType", "0");
			List<Map<String, Object>> phoneNumebrList = dao.selectList("common.getUsersAllPhoneNumber", paramMap);
			if(phoneNumebrList != null && phoneNumebrList.size() > 0) {
				Random r1 = new Random();
				Map<String, Object> phoneNumber = phoneNumebrList.get(Math.abs(r1.nextInt() % phoneNumebrList.size()));
				
				safetyCallModel.setDstVirtualNum((String) phoneNumber.get("phoneNumber"));
				dstVirtualNum = safetyCallModel.getDstVirtualNum();
				
				if (StringUtil.isEmpty(cityId)) {
					safetyCallModel.setCityId((String) phoneNumber.get("cityCode"));
				}
			}
		}

		

		// 请求公共鉴权组件
		AuthModel authModel = new AuthModel();
		authModel.setAuth(authorization);
		authModel.setSig(signature);
		authModel.setCallID(callId);
		authModel.setIpWhiteList(clientIp);
		authModel.setEvent(REST_EVENT);
		authModel.setPhoneNum(caller);
		authModel.setProductType("0"); // 虚拟小号
		authModel.setUserID(userId);
		authModel.setNeedBalance("0");
		authModel.setCaller(caller);
		authModel.setCallee(callee);

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

						String className = "com.caas.service.impl.HwAXBService";// TODO
						BaseAXBService axbService = new GxAXBService();
						try {
							axbService = (BaseAXBService) Class.forName(className).newInstance();
						} catch (Exception e) {
							logger.error("className=[" + className + "]实例化失败，", e);
							setResponse(callId, response, BusiErrorCode.B_100025, REST_EVENT, safetyCallModel.getUserData());
							HttpUtils.sendMessageJson(ctx, response.toString());
							return;
						}
						axbService.axbBind(callId, safetyCallModel, ctx, request, response);
						RedisOpClient.setAndExpire(RedisKeyConsts.getKey("apiServer:", bindId), className, Integer.valueOf(safetyCallModel.getMaxAge()));

					} else {
						// 将鉴权的错误结果异步写回客户端
						HttpUtils.sendMessageJson(ctx, authResponse.toString());
					}
				}

				@Override
				public void failed(Exception ex) {
					Log4jUtils.initLog4jContext(request.getLogId());
					logger.info("请求caas_auth组件失败,ex={}", ex);
					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, safetyCallModel.getUserData());
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
