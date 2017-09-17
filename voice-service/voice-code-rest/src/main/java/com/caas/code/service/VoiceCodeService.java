package com.caas.code.service;

import com.caas.model.AuthModel;
import com.caas.model.Voice4ZHModel;
import com.caas.model.VoiceCodeModel;
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
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;

/**
 * 语音验证码
 * 
 * @author xupiao 2017年9月11日
 *
 */
public class VoiceCodeService extends DefaultServiceCallBack {

	private static final Logger logger = LogManager.getLogger(VoiceCodeService.class);

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

		VoiceCodeModel voiceCodeModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<VoiceCodeModel>() {
		}.getType());

		String userData = voiceCodeModel.getUserData();
		String caller = voiceCodeModel.getCaller();
		String callee = voiceCodeModel.getCallee();
		String captchaCode = voiceCodeModel.getCaptchaCode();
		Integer playTimes = voiceCodeModel.getPlayTimes();

		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			voiceCodeModel.setUserData(userData.substring(0, 128));
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
		voiceCodeModel.setPlayTimes(playTimes);

		if (StringUtils.isEmpty(captchaCode)) {
			setResponse(callId, response, BusiErrorCode.B_100032, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		if (captchaCode.length() > 6 || captchaCode.length() < 4) {
			HttpUtils.sendMessageJson(ctx, response.toString());
			setResponse(callId, response, BusiErrorCode.B_100033, REST_EVENT, userData);
			return;
		}

		if (!CommonCheckUtil.isNumber(captchaCode)) {
			HttpUtils.sendMessageJson(ctx, response.toString());
			setResponse(callId, response, BusiErrorCode.B_100034, REST_EVENT, userData);
			return;
		}

		// 请求公共鉴权组件
		AuthModel authModel = new AuthModel();
		authModel.setAuth(authorization);
		authModel.setSig(signature);
		authModel.setCallID(callId);
		authModel.setIpWhiteList(clientIp);
		authModel.setEvent(REST_EVENT);
		authModel.setPhoneNum(voiceCodeModel.getCaller());
		authModel.setProductType("3"); // 语音通知
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

						String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/voiceNotify";

						Voice4ZHModel vc = new Voice4ZHModel();
						vc.setAppid(ConfigUtils.getProperty("voiceCode_zh_appid", String.class));
						vc.setCalled(voiceCodeModel.getCallee());
						vc.setCalling(voiceCodeModel.getCaller());
						vc.setExtkey(voiceCodeModel.getCaptchaCode());
						vc.setExtparam(callId);
						vc.setRepeat(String.valueOf(voiceCodeModel.getPlayTimes()));
						vc.setUrl(ConfigUtils.getProperty("voiceCode_zh_url", String.class));

						try {
							new HttpClient1(new ClientHandler() {
								@Override
								public void execute(HttpResponse response, String context) {
									//TODO
								}

								@Override
								public void failed(Exception ex) {
									Log4jUtils.initLog4jContext(request.getLogId());
									logger.info("请求caas_control组件失败,ex={}", ex);
									setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceCodeModel.getUserData());
									HttpUtils.sendMessageJson(ctx, response.toString());
								}
							}).httpPost(controlUrl, JsonUtil.toJsonStr(JsonUtil.toJsonStr(vc)));// TODO
						} catch (Exception e) {
							logger.info("请求caas_control组件出错,ex={}", e);
							setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceCodeModel.getUserData());
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
					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceCodeModel.getUserData());
					HttpUtils.sendMessageJson(ctx, response.toString());
				}
			}).httpPostBack(authUrl, authStr);
		} catch (Exception e) {
			logger.info("请求caas_auth组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, voiceCodeModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
		}
	}

}
