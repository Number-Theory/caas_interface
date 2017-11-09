package com.caas.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.caas.dao.CaasDao;
import com.caas.model.HuaweiBindInfo;
import com.caas.model.SafetyCallModel;
import com.caas.service.BaseAXBService;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient1;
import com.yzx.access.util.HttpUtils;
import com.yzx.auth.plugin.SpringContext;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.Log4jUtils;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

public class HwAXBService extends DefaultServiceCallBack implements BaseAXBService {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2404744465880917295L;

	private static final Logger logger = LogManager.getLogger(HwAXBService.class);

	private CaasDao dao = SpringContext.getInstance(CaasDao.class);
	
	@Override
	public void axbBind(String callId, SafetyCallModel safetyCallModel, ChannelHandlerContext ctx,
			ServiceRequest request, ServiceResponse response) {
		String caller = safetyCallModel.getCaller();
		String callee = safetyCallModel.getCallee();
		String dstVirtualNum = safetyCallModel.getDstVirtualNum();
		// 虚拟号码
		final String callerNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXBNUMBINDS, caller, dstVirtualNum);
		final Map<String, String> callerBindIdMapOld = RedisOpClient.hgetall(callerNumBindKey);
		final String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXBNUMBINDS, callee, dstVirtualNum);
		final Map<String, String> calleeBindIdMapOld = RedisOpClient.hgetall(calleeNumBindKey);

		HuaweiBindInfo HwBindInfo = new HuaweiBindInfo();
		HwBindInfo.setRequestId(callId);
		HwBindInfo.setaParty(safetyCallModel.getCaller());
		HwBindInfo.setbParty(safetyCallModel.getCallee());
		HwBindInfo.setIsRecord(safetyCallModel.getRecord());
		HwBindInfo.setBindDirection(changeBindDirection(safetyCallModel));
		HwBindInfo.setCityCode(safetyCallModel.getCityId());
		//获取
		Date  endDate= DateUtil.addSecond(new Date(), Integer.parseInt(safetyCallModel.getMaxAge()));
		HwBindInfo.setEndTime(DateUtil.dateToStr(endDate, "YYYY-MM-DD'T'hh:mm:ss'Z'"));
		
		final String[] subid = { "" };
		final String[] orderRecordKeyOld = { "" };
		String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallBindHWAXB";
		if (callerBindIdMapOld != null && !callerBindIdMapOld.isEmpty()) {// 如果选取的主叫和中间号存在绑定关系，绑定更新接口

			String bindIdOld = callerBindIdMapOld.get("bindId");
			orderRecordKeyOld[0] = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindIdOld);
			Map<String, String> orderRecordMapOld = RedisOpClient.hgetall(orderRecordKeyOld[0]);
			subid[0] = orderRecordMapOld.get("subid");

			controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallUpdateHWAXB" + "/" + subid[0];
		} else if (calleeBindIdMapOld != null && !calleeBindIdMapOld.isEmpty()) {// 如果选取的被叫和中间号存在绑定关系，置换主被叫，绑定更新接口

			String bindIdOld = callerBindIdMapOld.get("bindId");
			orderRecordKeyOld[0] = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindIdOld);
			Map<String, String> orderRecordMapOld = RedisOpClient.hgetall(orderRecordKeyOld[0]);
			subid[0] = orderRecordMapOld.get("subid");

			HwBindInfo.setaParty(safetyCallModel.getCallee());
			HwBindInfo.setbParty(safetyCallModel.getCaller());
			controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallUpdateHWAXB" + "/" + subid[0];
		} else { // 绑定接口
			HwBindInfo.setVirtualNumber(safetyCallModel.getDstVirtualNum());
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
							&& (resultMap != null && resultMap.containsKey("code") && "000000".equals(String.valueOf(resultMap.get("code"))))) {

						RedisOpClient.delKey(callerNumBindKey);
						RedisOpClient.delKey(calleeNumBindKey);
						if (StringUtils.isNotBlank(orderRecordKeyOld[0])) {
							RedisOpClient.delKey(orderRecordKeyOld[0]);
						}

						Map<String, String> callerBindIdMap = new HashMap<String, String>();
						callerBindIdMap.put("callee", safetyCallModel.getCallee());
						callerBindIdMap.put("bindId", safetyCallModel.getBindId());
						callerBindIdMap.put("dstVirtualNum", safetyCallModel.getDstVirtualNum());
						callerBindIdMap.put("cityId", safetyCallModel.getCityId());
						String callerBindRes = RedisOpClient.hmset(callerNumBindKey, callerBindIdMap, Integer.valueOf(safetyCallModel.getMaxAge()));
						logger.info("【AXB号码绑定】号码绑定记录哈希表中插入绑定关系callerBindRes={},callerNumBindKey={},callerBindIdMap={},maxAge={}", callerBindRes,
								callerNumBindKey, callerBindIdMap, Integer.valueOf(safetyCallModel.getMaxAge()));

						Map<String, String> calleeBindIdMap = new HashMap<String, String>();
						calleeBindIdMap.put("callee", safetyCallModel.getCaller());
						calleeBindIdMap.put("bindId", safetyCallModel.getBindId());
						calleeBindIdMap.put("dstVirtualNum", safetyCallModel.getDstVirtualNum());
						calleeBindIdMap.put("cityId", safetyCallModel.getCityId());
						String calleeBindRes = RedisOpClient.hmset(calleeNumBindKey, calleeBindIdMap, Integer.valueOf(safetyCallModel.getMaxAge()));
						logger.info("【AXB号码绑定】号码绑定记录哈希表中插入绑定关系calleeBindRes={},calleeNumBindKey={},calleeBindIdMap={},maxAge={}", calleeBindRes,
								callerNumBindKey, calleeBindIdMap, Integer.valueOf(safetyCallModel.getMaxAge()));

						String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, safetyCallModel.getBindId());
						Map<String, String> orderRecordMap = new HashMap<String, String>();
						orderRecordMap.put("userId", safetyCallModel.getUserId());
						orderRecordMap.put("bindId", safetyCallModel.getBindId());
						orderRecordMap.put("caller", safetyCallModel.getCaller());
						orderRecordMap.put("dstVirtualNum", safetyCallModel.getDstVirtualNum());
						orderRecordMap.put("callee", safetyCallModel.getCallee());
						orderRecordMap.put("maxAge", safetyCallModel.getMaxAge());
						orderRecordMap.put("requestId", callId);
						orderRecordMap.put("record", safetyCallModel.getRecord());
						orderRecordMap.put("bindStatus", "0");
						orderRecordMap.put("callerDisplay", safetyCallModel.getCallerdisplay());
						orderRecordMap.put("calleeDisplay", safetyCallModel.getCalleedisplay());
						Map<String, Object> sqlParams = new HashMap<String, Object>();
						sqlParams.put("userId", safetyCallModel.getUserId());
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
							orderRecordMap.put("subid", (String) (((Map<String, Object>) resultMap.get("result")).get("subscriptionId")));
						} else { // 绑定更新
							orderRecordMap.put("subid", subid[0]);
						}
						orderRecordMap.put("callRestrict", safetyCallModel.getCallRestrict());
						orderRecordMap.put("userData", ObjectUtils.defaultIfNull(safetyCallModel.getUserData(), ""));
						String orderRes = RedisOpClient.hmset(orderRecordKey, orderRecordMap, Integer.valueOf(safetyCallModel.getMaxAge()));
						logger.info("【AXB号码绑定】订单记录哈希表插入订单记录orderRes={},orderRecordKey={},orderRecordMap={},maxAge={}", orderRes, orderRecordKey,
								orderRecordMap, Integer.valueOf(safetyCallModel.getMaxAge()));

						dao.insert("common.insertBindOrder", orderRecordMap);

						controlResponse.getOtherMap().put("bindId", safetyCallModel.getBindId());
						controlResponse.getOtherMap().put("userData", safetyCallModel.getUserData());
						HttpUtils.sendMessageJson(ctx, controlResponse.toString());

					} else {
						if (resultMap != null && resultMap.containsKey("code") && !"000000".equals(resultMap.get("code"))) {
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
			}).httpPost(controlUrl, JsonUtil.toJsonStr(HwBindInfo));
		} catch (Exception e) {
			logger.info("请求caas_control组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, safetyCallModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
		}
		
		
		
	}

	
	
	
	@Override
	public void axbUnbind(String callId, SafetyCallModel safetyCallModel, ChannelHandlerContext ctx,
			ServiceRequest request, ServiceResponse response) {
		String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, safetyCallModel.getBindId());
		Map<String, String> orderRecordMap = RedisOpClient.hgetall(orderRecordKey);
		if (orderRecordMap == null || orderRecordMap.isEmpty()) {
			setResponse(callId, response, BusiErrorCode.B_100027, REST_EVENT, safetyCallModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
			return;
		}
		String subid = orderRecordMap.get("subid");
		String caller = orderRecordMap.get("caller");
		String callee = orderRecordMap.get("callee");
		String dstVirtualNum = orderRecordMap.get("dstVirtualNum");
		final String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXNUMBINDS, callee, dstVirtualNum);
		final String callerNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXNUMBINDS, caller, dstVirtualNum);
		final HuaweiBindInfo HwBindInfo = new HuaweiBindInfo();
		//按绑定关系解绑
		HwBindInfo.setType("2"); 
		//绑定关系
		HwBindInfo.setSubscriptionId(subid); 
		HwBindInfo.setRequestId(callId);

		String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/safetyCallUnbindAXB";
		try {
			new HttpClient1(new ClientHandler() {
				@Override
				public void execute(HttpResponse response, String context) {
					Map<String, Object> resultMap = JsonUtil.jsonStrToMap(context);
					Log4jUtils.initLog4jContext(request.getLogId());
					ServiceResponse controlResponse = JsonUtil.fromJson(context, new TypeToken<ServiceResponse>() {
					}.getType());
					if (BusiErrorCode.B_000000.getErrCode().equals(controlResponse.getResult())
							&& (resultMap != null && resultMap.containsKey("code") && "000000".equals(String.valueOf(resultMap.get("code"))))) {

						RedisOpClient.delKey(callerNumBindKey);
						logger.info("【AXB号码解绑】删除绑定关系callerNumBindKey={}", callerNumBindKey);
						RedisOpClient.delKey(calleeNumBindKey);
						logger.info("【AXB号码解绑】删除绑定关系calleeNumBindKey={}", calleeNumBindKey);

						String orderRecordKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, safetyCallModel.getBindId());
						RedisOpClient.delKey(orderRecordKey);
						logger.info("【AXB号码解绑】删除订单关系orderRecordKey={}", orderRecordKey);

						Map<String, Object> sqlParams = new HashMap<String, Object>();
						sqlParams.put("bindId", safetyCallModel.getBindId());
						dao.update("common.updateBindStatus", sqlParams);

						controlResponse.getOtherMap().put("bindId", safetyCallModel.getBindId());
						controlResponse.getOtherMap().put("userData", safetyCallModel.getUserData());
						HttpUtils.sendMessageJson(ctx, controlResponse.toString());

					} else {
						if (resultMap != null && resultMap.containsKey("code") && !"000000".equals(String.valueOf(resultMap.get("code")))) {
							setResponse(callId, controlResponse, BusiErrorCode.B_100028, REST_EVENT, safetyCallModel.getUserData());
							logger.error("【AXB号码解绑】号码解绑失败[{}].", resultMap);
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
			}).httpPost(controlUrl, JsonUtil.toJsonStr(HwBindInfo));
		} catch (Exception e) {
			logger.info("请求caas_control组件出错,ex={}", e);
			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, safetyCallModel.getUserData());
			HttpUtils.sendMessageJson(ctx, response.toString());
		}

	}

	/**
	 * 将我平台对应的axb呼入权限设置转化为华为接口对应的参数
	 * @param model
	 */
	private String changeBindDirection(SafetyCallModel model) {
		String callRestrict = model.getCallRestrict();
		String res ="";
		if ("1".equals(callRestrict)) {//、AXB做呼叫控制，A和B有权限，其他号码无权限
			res = "0"; //华为接口 0表示ab可以互打
		}
		if ("2".equals(callRestrict)) {//，A无权限，B有权限
			res = "2"; //华为接口 1 表示只能B打给A
		}
		if ("3".equals(callRestrict)) {//A有权限，B无权限
			res = "1"; //华为接口 1 表示只能A 打给 B
		}
		return res;
	}
	
}
