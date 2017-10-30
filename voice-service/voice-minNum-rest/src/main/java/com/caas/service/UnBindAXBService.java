package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
import com.caas.model.SafetyCallModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.access.util.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
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
public class UnBindAXBService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(UnBindAXBService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
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

		String userData = safetyCallModel.getUserData();
		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			safetyCallModel.setUserData(userData.substring(0, 128));
		}

		if (StringUtil.isBlank(safetyCallModel.getBindId())) {
			setResponse("", response, BusiErrorCode.B_100026, REST_EVENT, safetyCallModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		final String callId = safetyCallModel.getBindId();

		// 请求公共鉴权组件
		AuthModel authModel = new AuthModel();
		authModel.setAuth(authorization);
		authModel.setSig(signature);
		authModel.setCallID(callId);
		authModel.setIpWhiteList(clientIp);
		authModel.setEvent(REST_EVENT);
		authModel.setProductType("0"); // 虚拟小号
		authModel.setUserID(userId);
		authModel.setNeedBalance("1");

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
						String className = RedisOpClient.get(RedisKeyConsts.getKey("apiServer:", safetyCallModel.getBindId()));
						if (StringUtils.isBlank(className)) {
							setResponse(callId, response, BusiErrorCode.B_100027, REST_EVENT, safetyCallModel.getUserData());
							HttpUtils.sendMessageJson(ctx, response.toString());
							return;
						}
						BaseAXBService axbService;
						try {
							axbService = (BaseAXBService) Class.forName(className).newInstance();
						} catch (Exception e) {
							setResponse(callId, response, BusiErrorCode.B_100027, REST_EVENT, safetyCallModel.getUserData());
							HttpUtils.sendMessageJson(ctx, response.toString());
							return;
						}
						axbService.axbUnbind(callId, safetyCallModel, ctx, request, response);

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
			Log4jUtils.initLog4jContext(request.getLogId());
			logger.info("请求caas_auth组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, safetyCallModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
		}
	}
}
