package com.caas.notify.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
import com.caas.model.Voice4ZHModel;
import com.caas.model.VoiceNotifyModel;
import com.caas.util.CommonUtils;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.access.client.HttpClient1;
import com.yzx.access.util.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.GenericTokenParserUtil;
import com.yzx.core.util.GenericTokenParserUtil.TokenHandler;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.Log4jUtils;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * 语音通知
 * 
 * @author xupiao 2017年9月11日
 *
 */
@Service
public class VoiceNotifyService extends DefaultServiceCallBack {

	private static final Logger logger = LogManager.getLogger(VoiceNotifyService.class);

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

		VoiceNotifyModel voiceNotifyModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<VoiceNotifyModel>() {
		}.getType());
		
		voiceNotifyModel.setUserId(userId);

		String userData = voiceNotifyModel.getUserData();
		String caller = voiceNotifyModel.getCaller();
		String callee = voiceNotifyModel.getCallee();
		Integer playTimes = voiceNotifyModel.getPlayTimes();
		String content = voiceNotifyModel.getContent();

		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			voiceNotifyModel.setUserData(userData.substring(0, 128));
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

		if (null == playTimes || 0 == playTimes) {
			playTimes = 1;
		} else {
			if (playTimes > 4) {
				playTimes = 4;
			}
			if (playTimes < 1) {
				playTimes = 1;
			}
		}
		voiceNotifyModel.setPlayTimes(playTimes);

		if ("1".equals(voiceNotifyModel.getType())) {
			if (StringUtils.isEmpty(voiceNotifyModel.getTemplateId())) {
				setResponse(callId, response, BusiErrorCode.B_100029, REST_EVENT, userData);
				HttpUtils.sendMessageJson(ctx, response.toString());
				return;
			}
		}

		if (StringUtils.isEmpty(content)) {
			setResponse(callId, response, BusiErrorCode.B_100030, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		if (content.length() > 200) {
			HttpUtils.sendMessageJson(ctx, response.toString());
			setResponse(callId, response, BusiErrorCode.B_100031, REST_EVENT, userData);
			return;
		}

		// 请求公共鉴权组件
		AuthModel authModel = new AuthModel();
		authModel.setAuth(authorization);
		authModel.setSig(signature);
		authModel.setCallID(callId);
		authModel.setIpWhiteList(clientIp);
		authModel.setEvent(REST_EVENT);
		authModel.setPhoneNum(voiceNotifyModel.getCaller());
		authModel.setProductType("4"); // 语音通知
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

						String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/voiceNotify4ZH";
						Voice4ZHModel vc = new Voice4ZHModel();

						vc.setAppid(ConfigUtils.getProperty("voiceCode_zh_appid", String.class));
						vc.setCalled(voiceNotifyModel.getCallee());
						vc.setCalling(voiceNotifyModel.getCaller());
						Map<String, Object> sqlParams = new HashMap<String, Object>();
						sqlParams.put("id", voiceNotifyModel.getTemplateId());
						sqlParams.put("userId", userId);
						String templateContent = dao.selectOne("common.getTemplateContent", sqlParams);
						Map<String, Object> contentParams = JsonUtil.jsonStrToMap(content);
						GenericTokenParserUtil parser = new GenericTokenParserUtil("{", "}", new TokenHandler() {
							@Override
							public String handleToken(String content) {
								return String.valueOf(contentParams.get(content));
							}
						});
						templateContent = parser.parse(templateContent);
						vc.setExtkey2(templateContent);
						vc.setExtparam(callId);
						vc.setRepeat(String.valueOf(voiceNotifyModel.getPlayTimes()));
						vc.setServiceid(ConfigUtils.getProperty("voiceNotify_serviceId", String.class));
						vc.setTid(ConfigUtils.getProperty("voiceNotify_tid", String.class));
						vc.setUrl(ConfigUtils.getProperty("voiceNotify_callback_url", String.class));
						
						RedisOpClient.set(RedisKeyConsts.getKey(RedisKeyConsts.VOICE_NOTIFY_SESSION, callId), JsonUtil.toJsonStr(voiceNotifyModel));

						try {
							new HttpClient1(new ClientHandler() {
								@Override
								public void execute(HttpResponse response, String context) {
									ServiceResponse controlResponse = JsonUtil.fromJson(context, new TypeToken<ServiceResponse>() {
									}.getType());
									HttpUtils.sendMessageJson(ctx, controlResponse.toString());
								}

								@Override
								public void failed(Exception ex) {
									Log4jUtils.initLog4jContext(request.getLogId());
									logger.info("请求caas_control组件失败,ex={}", ex);
									setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceNotifyModel.getUserData());
									HttpUtils.sendMessageJson(ctx, response.toString());
								}
							}).httpPost(controlUrl, JsonUtil.toJsonStr(vc));// TODO
						} catch (Exception e) {
							logger.info("请求caas_control组件出错,ex={}", e);
							setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceNotifyModel.getUserData());
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
					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceNotifyModel.getUserData());
					HttpUtils.sendMessageJson(ctx, response.toString());
				}
			}).httpPostBack(authUrl, authStr);
		} catch (Exception e) {
			logger.info("请求caas_auth组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceNotifyModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
		}

	}
}
