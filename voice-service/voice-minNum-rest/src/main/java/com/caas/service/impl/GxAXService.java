package com.caas.service.impl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.caas.dao.CaasDao;
import com.caas.model.GxInfo;
import com.caas.model.MinNumModel;
import com.caas.service.BaseAXService;
import com.caas.util.CommonUtils;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient1;
import com.yzx.access.util.HttpUtils;
import com.yzx.auth.plugin.SpringContext;
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

public class GxAXService extends DefaultServiceCallBack implements BaseAXService {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8744768438864276208L;

	private static final Logger logger = LogManager.getLogger(GxAXService.class);

	private CaasDao dao = SpringContext.getInstance(CaasDao.class);

	@Override
	public void axBind(String callId, MinNumModel minNumModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response) {

		String userData = minNumModel.getUserData();
		// 虚拟号码
		final String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXNUMBINDS, minNumModel.getDstVirtualNum());
		Map<String, String> callerBindIdMapOld = RedisOpClient.hgetall(calleeNumBindKey);
		if (callerBindIdMapOld != null && !callerBindIdMapOld.isEmpty()) {// 如果选取的中间号存在绑定关系，则报错返回
			logger.info("【中间号码X存在绑定关系】dstVirtualNum={},绑定失败", minNumModel.getDstVirtualNum());
			setResponse(callId, response, BusiErrorCode.B_100023, REST_EVENT, userData);
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}

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
		gxInfo.setBindId(minNumModel.getBindId());
		gxInfo.setCalldisplay(minNumModel.getCalldisplay());
		String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/minNumBindAX";
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
						Map<String, String> calleeBindIdMap = new HashMap<String, String>();
						calleeBindIdMap.put("caller", minNumModel.getCaller());
						calleeBindIdMap.put("bindId", minNumModel.getBindId());
						calleeBindIdMap.put("dstVirtualNum", minNumModel.getDstVirtualNum());
						calleeBindIdMap.put("cityId", minNumModel.getCityId());
						String callerBindRes = RedisOpClient.hmset(calleeNumBindKey, calleeBindIdMap, Integer.valueOf(minNumModel.getMaxAge()));
						logger.info("【AX号码绑定】号码绑定记录哈希表中插入绑定关系callerBindRes={},callerNumBindKey={},calleeBindIdMap={},maxAge={}", callerBindRes,
								calleeNumBindKey, calleeBindIdMap, Integer.valueOf(minNumModel.getMaxAge()));

						String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, minNumModel.getBindId());
						Map<String, String> orderRecordMap = new HashMap<String, String>();
						orderRecordMap.put("userId", minNumModel.getUserId());
						orderRecordMap.put("bindId", minNumModel.getBindId());
						orderRecordMap.put("caller", minNumModel.getCaller());
						orderRecordMap.put("dstVirtualNum", minNumModel.getDstVirtualNum());
						orderRecordMap.put("maxAge", minNumModel.getMaxAge());
						orderRecordMap.put("requestId", callId);
						orderRecordMap.put("record", minNumModel.getRecord());
						orderRecordMap.put("callerDisplay", minNumModel.getCalldisplay());
						orderRecordMap.put("bindStatus", "0");
						Map<String, Object> sqlParams = new HashMap<String, Object>();
						sqlParams.put("userId", minNumModel.getUserId());
						sqlParams.put("productType", "0");
						Map<String, Object> callbackUrl = dao.selectOne("common.getCallBackUrl", sqlParams);
						if (StringUtil.isNotEmpty(minNumModel.getStatusUrl())) {
							orderRecordMap.put("statusUrl", minNumModel.getStatusUrl());
						} else {
							orderRecordMap.put("statusUrl", ObjectUtils.defaultIfNull(String.valueOf(callbackUrl.get("statusUrl")), ""));
						}
						if (StringUtil.isNotEmpty(minNumModel.getHangupUrl())) {
							orderRecordMap.put("hangupUrl", minNumModel.getHangupUrl());
						} else {
							orderRecordMap.put("hangupUrl", ObjectUtils.defaultIfNull(String.valueOf(callbackUrl.get("hangupUrl")), ""));
						}
						if (StringUtil.isNotEmpty(minNumModel.getRecordUrl())) {
							orderRecordMap.put("recordUrl", minNumModel.getRecordUrl());
						} else {
							orderRecordMap.put("recordUrl", ObjectUtils.defaultIfNull(String.valueOf(callbackUrl.get("recordUrl")), ""));
						}
						orderRecordMap.put("cityId", minNumModel.getCityId());
						orderRecordMap.put("productType", "1");
						orderRecordMap.put("subid", (String) (((Map<String, Object>) resultMap.get("data")).get("subid")));
						orderRecordMap.put("userData", ObjectUtils.defaultIfNull(minNumModel.getUserData(), ""));
						String orderRes = RedisOpClient.hmset(orderRecordKey, orderRecordMap, Integer.valueOf(minNumModel.getMaxAge()));
						logger.info("【AX号码绑定】订单记录哈希表插入订单记录orderRes={},orderRecordKey={},orderRecordMap={},maxAge={}", orderRes, orderRecordKey, orderRecordMap,
								Integer.valueOf(minNumModel.getMaxAge()));

						dao.insert("common.insertBindOrder", orderRecordMap);

						controlResponse.getOtherMap().put("bindId", minNumModel.getBindId());
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
	}

	@Override
	public void axUnbind(String callId, MinNumModel minNumModel, ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response) {
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
							&& (resultMap != null && resultMap.containsKey("code") && "0".equals(String.valueOf(resultMap.get("code"))))) {

						RedisOpClient.delKey(calleeNumBindKey);
						logger.info("【AX号码解绑】删除绑定关系callerNumBindKey={}", calleeNumBindKey);

						String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, minNumModel.getBindId());
						RedisOpClient.delKey(orderRecordKey);
						logger.info("【AX号码解绑】删除订单关系orderRecordKey={}", orderRecordKey);

						Map<String, Object> sqlParams = new HashMap<String, Object>();
						sqlParams.put("bindId", minNumModel.getBindId());
						dao.update("common.updateBindStatus", sqlParams);

						controlResponse.getOtherMap().put("bindId", minNumModel.getBindId());
						controlResponse.getOtherMap().put("userData", minNumModel.getUserData());
						HttpUtils.sendMessageJson(ctx, controlResponse.toString());

					} else {
						if (resultMap != null && resultMap.containsKey("code") && !"0".equals(String.valueOf(resultMap.get("code")))) {
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
	}
}
