package com.caas.clickcall.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
import com.caas.model.ClickCallModel;
import com.caas.model.GxInfo;
import com.caas.model.SafetyCallModel;
import com.caas.util.CommonUtils;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.access.client.HttpClient1;
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
 * 发起点击呼叫接口
 * 
 * @author xupiao 2017年10月26日
 *
 */
public class ClickcallService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(ClickcallService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		final String callId = UUID.randomUUID().toString().replace("-", "");

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
		final ClickCallModel clickCallModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<ClickCallModel>() {
		}.getType());

		String caller = clickCallModel.getCaller();
		String callee = clickCallModel.getCallee();
		String userData = clickCallModel.getUserData();
		String displayCaller = clickCallModel.getDisplayCaller();
		String displayCallee = clickCallModel.getDisplayCallee();
		Integer maxDuraction = clickCallModel.getMaxDuration();

		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			clickCallModel.setUserData(userData.substring(0, 128));
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

		if (StringUtil.isEmpty(maxDuraction) || maxDuraction < 0) {
			maxDuraction = 0;
		} else {
			if (maxDuraction > 480) {
				maxDuraction = 480;
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
		authModel.setProductType("2"); // 标准回拨
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
						
						

						String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/clickcall";

						try {
							new HttpClient1(new ClientHandler() {
								@Override
								public void execute(HttpResponse response, String context) {
									Map<String, Object> resultMap = JsonUtil.jsonStrToMap(context);
									Log4jUtils.initLog4jContext(request.getLogId());
									ServiceResponse controlResponse = JsonUtil.fromJson(context, new TypeToken<ServiceResponse>() {
									}.getType());
									if (BusiErrorCode.B_000000.getErrCode().equals(controlResponse.getResult())
											&& (resultMap != null && resultMap.containsKey("code") && "0".equals(String.valueOf(resultMap.get("code"))))) {

										HttpUtils.sendMessageJson(ctx, controlResponse.toString());

									} else {

										HttpUtils.sendMessageJson(ctx, controlResponse.toString());
									}
								}

								@Override
								public void failed(Exception ex) {
									Log4jUtils.initLog4jContext(request.getLogId());
									logger.info("请求caas_control组件失败,ex={}", ex);
									setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, clickCallModel.getUserData());
									HttpUtils.sendMessageJson(ctx, response.toString());
								}
							}).httpPost(controlUrl, JsonUtil.toJsonStr("")); // TODO
						} catch (Exception e) {
							logger.info("请求caas_control组件出错,ex={}", e);
							setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, clickCallModel.getUserData());
							HttpUtils.sendMessageJson(ctx, response.toString());
						}

					} else {
						// 将鉴权的错误结果异步写回客户端
						HttpUtils.sendMessageJson(ctx, authResponse.toString());
					}
				}

				@Override
				public void failed(Exception ex) {
					Log4jUtils.initLog4jContext(request.getLogId());
					logger.info("请求caas_auth组件失败,ex={}", ex);
					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, clickCallModel.getUserData());
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
