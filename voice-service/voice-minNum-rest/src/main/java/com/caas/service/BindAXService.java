package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.caas.model.GxInfo;
import com.caas.model.MinNumModel;
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

		if (StringUtil.isBlank(minNumModel.getCalldisplay()) || !"0".equals(minNumModel.getCalldisplay())) {
			minNumModel.setCalldisplay("1");
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

		// 虚拟号码
		final String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXNUMBINDS, dstVirtualNum);
		Map<String, String> callerBindIdMapOld = RedisOpClient.hgetall(calleeNumBindKey);
		if (callerBindIdMapOld != null && !callerBindIdMapOld.isEmpty()) {// 如果选取的中间号存在绑定关系，则报错返回
			logger.info("【中间号码X存在绑定关系】dstVirtualNum={},绑定失败", dstVirtualNum);
			setResponse(callId, response, BusiErrorCode.B_100023, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

		if (StringUtil.isNotEmpty(cityId)) {
			minNumModel.setCityId((String) numberMap.get("city"));
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
						GxInfo gxInfo = new GxInfo();
						gxInfo.setRequestId(callId);
						gxInfo.setTelA(minNumModel.getCaller());
						gxInfo.setTelX(minNumModel.getDstVirtualNum());
						SimpleDateFormat sdDateFormat1 = new SimpleDateFormat("yyyyMMddHHmmssFFF");
						gxInfo.setSubts(sdDateFormat1.format(new Date()));
						gxInfo.setName(minNumModel.getName());
						gxInfo.setCardtype(minNumModel.getCardtype());
						gxInfo.setCardno(minNumModel.getCardno());
						gxInfo.setAreacode(CommonUtils.getMobile(minNumModel.getCityId()));
						gxInfo.setExpiration(minNumModel.getMaxAge());
						gxInfo.setCallrecording(minNumModel.getRecord());
						// gxInfo.setForceRecord("");
						gxInfo.setXmode("mode101");
						gxInfo.setBindId(bindId);
						gxInfo.setCalldisplay(minNumModel.getCalldisplay());
						String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/minNumBindAX"; // TODO
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
										Map<String, String> calleeBindIdMap = new HashMap<String, String>();
										calleeBindIdMap.put("caller", minNumModel.getCaller());
										calleeBindIdMap.put("bindId", bindId);
										calleeBindIdMap.put("dstVirtualNum", minNumModel.getDstVirtualNum());
										calleeBindIdMap.put("cityId", minNumModel.getCityId());
										String callerBindRes = RedisOpClient.hmset(calleeNumBindKey, calleeBindIdMap, Integer.valueOf(minNumModel.getMaxAge()));
										logger.info("【AX号码绑定】号码绑定记录哈希表中插入绑定关系callerBindRes={},callerNumBindKey={},calleeBindIdMap={},maxAge={}", callerBindRes,
												calleeNumBindKey, calleeBindIdMap, Integer.valueOf(minNumModel.getMaxAge()));

										String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindId);
										Map<String, String> orderRecordMap = new HashMap<String, String>();
										orderRecordMap.put("userId", userId);
										orderRecordMap.put("bindId", bindId);
										orderRecordMap.put("caller", minNumModel.getCaller());
										orderRecordMap.put("dstVirtualNum", minNumModel.getDstVirtualNum());
										orderRecordMap.put("maxAge", minNumModel.getMaxAge());
										orderRecordMap.put("requestId", callId);
										orderRecordMap.put("record", minNumModel.getRecord());
										orderRecordMap.put("statusUrl", minNumModel.getStatusUrl());
										orderRecordMap.put("hangupUrl", minNumModel.getHangupUrl());
										orderRecordMap.put("recordUrl", minNumModel.getRecordUrl());
										orderRecordMap.put("cityId", cityId);
										orderRecordMap.put("productType", "1");
										orderRecordMap.put("subid", (String) (((Map<String, Object>) resultMap.get("data")).get("subid")));
										String orderRes = RedisOpClient.hmset(orderRecordKey, orderRecordMap, Integer.valueOf(minNumModel.getMaxAge()));
										logger.info("【AX号码绑定】订单记录哈希表插入订单记录orderRes={},orderRecordKey={},orderRecordMap={},maxAge={}", orderRes, orderRecordKey,
												orderRecordMap, Integer.valueOf(minNumModel.getMaxAge()));

										// TODO 订单入库

										controlResponse.getOtherMap().put("bindId", bindId);
										controlResponse.getOtherMap().put("userData", minNumModel.getUserData());
										HttpUtils.sendMessageJson(ctx, controlResponse.toString());

									} else {
										if (resultMap != null && resultMap.containsKey("code") && !"0".equals(resultMap.get("code"))) {
											setResponse(callId, controlResponse, BusiErrorCode.B_100025, REST_EVENT, minNumModel.getUserData());
											logger.error("【AX号码绑定】号码绑定失败[{}].", resultMap);
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
			logger.info("请求caas_auth组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
		}
	}
}
