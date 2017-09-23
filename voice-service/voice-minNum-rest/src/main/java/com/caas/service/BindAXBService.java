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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
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

		// 虚拟号码
		final String callerNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXBNUMBINDS, caller, dstVirtualNum);
		final Map<String, String> callerBindIdMapOld = RedisOpClient.hgetall(callerNumBindKey);
		final String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXBNUMBINDS, callee, dstVirtualNum);
		final Map<String, String> calleeBindIdMapOld = RedisOpClient.hgetall(calleeNumBindKey);

		if (StringUtil.isEmpty(cityId)) {
			safetyCallModel.setCityId((String) numberMap.get("cityCode"));
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
						gxInfo.setTelA(safetyCallModel.getCaller());
						gxInfo.setTelB(safetyCallModel.getCallee());
						SimpleDateFormat sdDateFormat1 = new SimpleDateFormat("yyyyMMddHHmmssFFF");
						gxInfo.setSubts(sdDateFormat1.format(new Date()));
						gxInfo.setAnucode("206,207,208");
						gxInfo.setCalldisplay(safetyCallModel.getCallerdisplay() + "," + safetyCallModel.getCalleedisplay());
						gxInfo.setExpiration(safetyCallModel.getMaxAge());
						gxInfo.setCallrecording(safetyCallModel.getRecord());
						final String[] subid = { "" };
						final String[] orderRecordKeyOld = { "" };
						String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallBindAXB";
						if (callerBindIdMapOld != null && !callerBindIdMapOld.isEmpty()) {// 如果选取的主叫和中间号存在绑定关系，绑定更新接口

							String bindIdOld = callerBindIdMapOld.get("bindId");
							orderRecordKeyOld[0] = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindIdOld);
							Map<String, String> orderRecordMapOld = RedisOpClient.hgetall(orderRecordKeyOld[0]);
							subid[0] = orderRecordMapOld.get("subid");

							controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallUpdateAXB" + "/" + subid[0];
						} else if (calleeBindIdMapOld != null && !calleeBindIdMapOld.isEmpty()) {// 如果选取的被叫和中间号存在绑定关系，置换主被叫，绑定更新接口

							String bindIdOld = callerBindIdMapOld.get("bindId");
							orderRecordKeyOld[0] = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindIdOld);
							Map<String, String> orderRecordMapOld = RedisOpClient.hgetall(orderRecordKeyOld[0]);
							subid[0] = orderRecordMapOld.get("subid");

							gxInfo.setTelA(safetyCallModel.getCallee());
							gxInfo.setTelB(safetyCallModel.getCaller());
							controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallUpdateAXB" + "/" + subid[0];
						} else { // 绑定接口
							gxInfo.setTelX(safetyCallModel.getDstVirtualNum());
						}

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

										RedisOpClient.delKey(callerNumBindKey);
										RedisOpClient.delKey(calleeNumBindKey);
										if (StringUtils.isNotBlank(orderRecordKeyOld[0])) {
											RedisOpClient.delKey(orderRecordKeyOld[0]);
										}

										Map<String, String> callerBindIdMap = new HashMap<String, String>();
										callerBindIdMap.put("callee", safetyCallModel.getCallee());
										callerBindIdMap.put("bindId", bindId);
										callerBindIdMap.put("dstVirtualNum", safetyCallModel.getDstVirtualNum());
										callerBindIdMap.put("cityId", safetyCallModel.getCityId());
										String callerBindRes = RedisOpClient.hmset(callerNumBindKey, callerBindIdMap,
												Integer.valueOf(safetyCallModel.getMaxAge()));
										logger.info("【AXB号码绑定】号码绑定记录哈希表中插入绑定关系callerBindRes={},callerNumBindKey={},callerBindIdMap={},maxAge={}",
												callerBindRes, callerNumBindKey, callerBindIdMap, Integer.valueOf(safetyCallModel.getMaxAge()));

										Map<String, String> calleeBindIdMap = new HashMap<String, String>();
										calleeBindIdMap.put("callee", safetyCallModel.getCaller());
										calleeBindIdMap.put("bindId", bindId);
										calleeBindIdMap.put("dstVirtualNum", safetyCallModel.getDstVirtualNum());
										calleeBindIdMap.put("cityId", safetyCallModel.getCityId());
										String calleeBindRes = RedisOpClient.hmset(calleeNumBindKey, calleeBindIdMap,
												Integer.valueOf(safetyCallModel.getMaxAge()));
										logger.info("【AXB号码绑定】号码绑定记录哈希表中插入绑定关系calleeBindRes={},calleeNumBindKey={},calleeBindIdMap={},maxAge={}",
												calleeBindRes, callerNumBindKey, calleeBindIdMap, Integer.valueOf(safetyCallModel.getMaxAge()));

										String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindId);
										Map<String, String> orderRecordMap = new HashMap<String, String>();
										orderRecordMap.put("userId", userId);
										orderRecordMap.put("bindId", bindId);
										orderRecordMap.put("caller", safetyCallModel.getCaller());
										orderRecordMap.put("dstVirtualNum", safetyCallModel.getDstVirtualNum());
										orderRecordMap.put("callee", safetyCallModel.getCallee());
										orderRecordMap.put("maxAge", safetyCallModel.getMaxAge());
										orderRecordMap.put("requestId", callId);
										orderRecordMap.put("record", safetyCallModel.getRecord());
										orderRecordMap.put("callerDisplay", safetyCallModel.getCallerdisplay());
										orderRecordMap.put("calleeDisplay", safetyCallModel.getCalleedisplay());
										Map<String, Object> sqlParams = new HashMap<String, Object>();
										sqlParams.put("userId", userId);
										sqlParams.put("productType", "0");
										Map<String, Object> callbackUrl = dao.selectOne("common.getCallBackUrl", sqlParams);
										if (StringUtil.isNotEmpty(safetyCallModel.getStatusUrl())) {
											orderRecordMap.put("statusUrl", safetyCallModel.getStatusUrl());
										} else {
											orderRecordMap.put("statusUrl", ObjectUtils.defaultIfNull(String.valueOf(callbackUrl.get("statusUrl")), ""));
										}
										if (StringUtil.isNotEmpty(safetyCallModel.getHangupUrl())) {
											orderRecordMap.put("hangupUrl", safetyCallModel.getHangupUrl());
										} else {
											orderRecordMap.put("hangupUrl", ObjectUtils.defaultIfNull(String.valueOf(callbackUrl.get("hangupUrl")), ""));
										}
										if (StringUtil.isNotEmpty(safetyCallModel.getRecordUrl())) {
											orderRecordMap.put("recordUrl", safetyCallModel.getRecordUrl());
										} else {
											orderRecordMap.put("recordUrl", ObjectUtils.defaultIfNull(String.valueOf(callbackUrl.get("recordUrl")), ""));
										}
										orderRecordMap.put("cityId", safetyCallModel.getCityId());
										orderRecordMap.put("productType", "0");
										if (resultMap.containsKey("data")) { // 绑定
											orderRecordMap.put("subid", (String) (((Map<String, Object>) resultMap.get("data")).get("subid")));
										} else { // 绑定更新
											orderRecordMap.put("subid", subid[0]);
										}
										String orderRes = RedisOpClient.hmset(orderRecordKey, orderRecordMap, Integer.valueOf(safetyCallModel.getMaxAge()));
										logger.info("【AXB号码绑定】订单记录哈希表插入订单记录orderRes={},orderRecordKey={},orderRecordMap={},maxAge={}", orderRes,
												orderRecordKey, orderRecordMap, Integer.valueOf(safetyCallModel.getMaxAge()));

										dao.insert("common.insertBindOrder", orderRecordMap);

										controlResponse.getOtherMap().put("bindId", bindId);
										controlResponse.getOtherMap().put("userData", safetyCallModel.getUserData());
										HttpUtils.sendMessageJson(ctx, controlResponse.toString());

									} else {
										if (resultMap != null && resultMap.containsKey("code") && !"0".equals(resultMap.get("code"))) {
											setResponse(callId, controlResponse, BusiErrorCode.B_100025, REST_EVENT, safetyCallModel.getUserData());
											logger.error("【AXB号码绑定】号码绑定失败[{}].", resultMap);
										}
										HttpUtils.sendMessageJson(ctx, controlResponse.toString());
									}
								}

								@Override
								public void failed(Exception ex) {
									Log4jUtils.initLog4jContext(request.getLogId());
									logger.info("请求caas_control组件失败,ex={}", ex);
									setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, safetyCallModel.getUserData());
									HttpUtils.sendMessageJson(ctx, response.toString());
								}
							}).httpPost(controlUrl, JsonUtil.toJsonStr(gxInfo));
						} catch (Exception e) {
							logger.info("请求caas_control组件出错,ex={}", e);
							setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, safetyCallModel.getUserData());
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
