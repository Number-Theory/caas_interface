package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.caas.model.AuthModel;
import com.caas.model.GxInfo;
import com.caas.model.MinNumModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.access.client.HttpClient1;
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
public class UnBindAXService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(UnBindAXService.class);

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

		final MinNumModel minNumModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<MinNumModel>() {
		}.getType());

		String userData = minNumModel.getUserData();
		if (StringUtil.isNotEmpty(userData) && userData.length() > 128) {
			userData = userData.substring(0, 128);
			minNumModel.setUserData(userData.substring(0, 128));
		}

		if (StringUtil.isBlank(minNumModel.getBindId())) {
			setResponse("", response, BusiErrorCode.B_100026, REST_EVENT, minNumModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		final String callId = minNumModel.getBindId();

		// 请求公共鉴权组件
		AuthModel authModel = new AuthModel();
		authModel.setAuth(authorization);
		authModel.setSig(signature);
		authModel.setCallID(callId);
		authModel.setIpWhiteList(clientIp);
		authModel.setEvent(REST_EVENT);
		authModel.setProductType("1"); // 虚拟小号
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
						// TODO
						String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, minNumModel.getBindId());
						Map<String, String> orderRecordMap = RedisOpClient.hgetall(orderRecordKey);
						if (orderRecordMap == null || orderRecordMap.isEmpty()) {
							setResponse(callId, response, BusiErrorCode.B_100027, REST_EVENT, minNumModel.getUserData());
							HttpUtils.sendMessageJson(ctx, response.toString());
							return;
						}

						String subid = orderRecordMap.get("subid");
						String dstVirtualNum = orderRecordMap.get("dstVirtualNum");
						final String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXNUMBINDS, dstVirtualNum);
						final GxInfo gxInfo = new GxInfo();
						gxInfo.setSubid(subid);

						String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/minNumUnbindAX"; // TODO
						try {
							new HttpClient1(new ClientHandler() {
								@Override
								public void execute(HttpResponse response, String context) {
									Map<String, Object> resultMap = JsonUtil.jsonStrToMap(context);
									Log4jUtils.initLog4jContext(request.getLogId());
									ServiceResponse controlResponse = JsonUtil.fromJson(context, new TypeToken<ServiceResponse>() {
									}.getType());
									if (BusiErrorCode.B_000000.getErrCode().equals(controlResponse.getResult())
											&& (resultMap != null && resultMap.containsKey("code") && "0".equals(resultMap.get("code")))) {

										RedisOpClient.delKey(calleeNumBindKey);
										logger.info("【AX号码解绑】删除绑定关系callerNumBindKey={}", calleeNumBindKey);

										String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, minNumModel.getBindId());
										RedisOpClient.delKey(orderRecordKey);
										logger.info("【AX号码解绑】删除订单关系callerNumBindKey={}", calleeNumBindKey);

										// TODO 订单状态更新

										controlResponse.getOtherMap().put("bindId", minNumModel.getBindId());
										controlResponse.getOtherMap().put("userData", minNumModel.getUserData());
										HttpUtils.sendMessageJson(ctx, controlResponse.toString());

									} else {
										if (resultMap != null && resultMap.containsKey("code") && !"0".equals(resultMap.get("code"))) {
											setResponse(callId, controlResponse, BusiErrorCode.B_100028, REST_EVENT, minNumModel.getUserData());
											logger.error("【AX号码解绑】号码解绑失败[{}].", resultMap);
										}
										HttpUtils.sendMessageJson(ctx, controlResponse.toString());
									}
								}

								@Override
								public void failed(Exception ex) {
									Log4jUtils.initLog4jContext(request.getLogId());
									logger.info("请求caas_control组件失败,ex={}", ex);
									setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, minNumModel.getUserData());
									HttpUtils.sendMessageJson(ctx, response.toString());
								}
							}).httpPost(controlUrl, JsonUtil.toJsonStr(gxInfo));
						} catch (Exception e) {
							logger.info("请求caas_control组件出错,ex={}", e);
							setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, minNumModel.getUserData());
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
					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, minNumModel.getUserData());
					HttpUtils.sendMessageJson(ctx, response.toString());
				}
			}).httpPostBack(authUrl, authStr);
		} catch (Exception e) {
			Log4jUtils.initLog4jContext(request.getLogId());
			logger.info("请求caas_auth组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, minNumModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
		}
	}
}
