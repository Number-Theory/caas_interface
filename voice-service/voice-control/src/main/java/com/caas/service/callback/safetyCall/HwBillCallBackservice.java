package com.caas.service.callback.safetyCall;

import io.netty.channel.ChannelHandlerContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.BillingModel;
import com.caas.model.callback.safetycall.SafetyCallBillModel;
import com.caas.model.callback.safetycall.SafetyCallHwBillModel;
import com.caas.model.callback.safetycall.SafetyCallStatusModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

@Service
public class HwBillCallBackservice extends DefaultServiceCallBack {
	@Autowired
	private CaasDao dao;

	private static final Logger logger = LogManager.getLogger(HwBillCallBackservice.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {

		SafetyCallHwBillModel hwCallBackModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<SafetyCallHwBillModel>() {
		}.getType());

		logger.info("华为回调信息 hwCallBackModel={} ", JsonUtil.toJsonStr(hwCallBackModel));

		String reqAppkey = hwCallBackModel.getAppKey();

		String sys_Appkey = ConfigUtils.getProperty("appKey_hw", String.class);
		// 判斷是不是本應用對應的回調
		if (!sys_Appkey.equals(reqAppkey)) {
			logger.info("回调对应的appkey:{}与本平台配置的appkey:{}不一致!", reqAppkey, sys_Appkey);
			return;
		}
		// 回调通知模式
		String callBackModel = hwCallBackModel.getCallEvent().getNotificationMode();
		// 事件類型
		String eventType = hwCallBackModel.getCallEvent().getEvent();

		if ("Notify".equals(callBackModel)) {
			dealEventHandle(eventType, hwCallBackModel);
		}
		if ("Block".equals(callBackModel)) {
			Map<String, Object> actions = new HashMap<>();
			Map<String, Object> record = new HashMap<>();
			List<Map<String, Object>> list = new ArrayList<>();
			// 设置路由
			if ("1".equals(hwCallBackModel.getCallEvent().getIsRecord())) {
				record.put("operation", "Record");
				list.add(record);
			}
			String virtualNumber = remove86MobileNationPrefix(hwCallBackModel.getCallEvent().getVirtualNumber());
			String caller = remove86MobileNationPrefix(hwCallBackModel.getCallEvent().getCalling());
			String calleeNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXNUMBINDS, virtualNumber);
			Map<String, String> callerBindIdMap = RedisOpClient.hgetall(calleeNumBindKey);
			if (callerBindIdMap != null && !callerBindIdMap.isEmpty()) {
				if (caller.equals(callerBindIdMap.get("caller"))) {
					Map<String, Object> vNumber = new HashMap<String, Object>();
					vNumber.put("operation", "vNumberRoute");
					vNumber.put("routingAddress", callerBindIdMap.get("callee"));
					list.add(vNumber);
				}
			}
			actions.put("actions", JsonUtil.toJsonStr(list));
			response.setOtherMap(actions);
		}
	}

	/**
	 * 处理华为事件回调
	 * 
	 * @param eventType
	 *            事件类型
	 * @param hwCallBackModel
	 *            回调信息
	 */
	private void dealEventHandle(String eventType, SafetyCallHwBillModel hwCallBackModel) {
		switch (eventType) {
		case "IDP":
			logger.info("华为接口开始呼叫回调信息", hwCallBackModel);
			dealStatus(hwCallBackModel);
			break;
		case "Answer":
			logger.info("华为接口应答回调信息", hwCallBackModel);
			break;
		case "Release":
			dealEvent(hwCallBackModel, "0");
			logger.info("华为接口呼叫結束回调信息", hwCallBackModel);
			break;
		case "Exception":
			dealEvent(hwCallBackModel, "1");
			logger.info("华为接口呼叫异常回调信息", hwCallBackModel);
			break;
		default:
			logger.info("华为接口回调事件匹配失败", hwCallBackModel);
			break;
		}
	}

	private void dealStatus(SafetyCallHwBillModel hwCallBackModel) {
		String caller = remove86MobileNationPrefix(hwCallBackModel.getCallEvent().getCalling());
		String callee = remove86MobileNationPrefix(hwCallBackModel.getCallEvent().getCalled());
		String virtualNumber = remove86MobileNationPrefix(hwCallBackModel.getCallEvent().getVirtualNumber());
		Map<String, Object> orderBindMap = dao.selectOne("common.getBindOrderByNumber", virtualNumber);
		if (orderBindMap != null && !orderBindMap.isEmpty()) {
			String subid = (String) orderBindMap.get("subid");
			String statusUrl = dao.selectOne("common.getBindOrderStatusUrl", subid);
			if (StringUtil.isBlank(statusUrl)) {
				logger.info("状态回调地址为空，不进行回调");
			} else {
				SafetyCallStatusModel safetyCallStatusModel = new SafetyCallStatusModel();
				safetyCallStatusModel.setBindId((String) orderBindMap.get("bindId"));
				safetyCallStatusModel.setCallee(callee);
				safetyCallStatusModel.setCaller(caller);
				safetyCallStatusModel.setCallId((String) orderBindMap.get("requestId"));
				safetyCallStatusModel.setCalleeDisplay((String) orderBindMap.get("calleeDisplay"));
				safetyCallStatusModel.setRecord(hwCallBackModel.getCallEvent().getIsRecord());
				safetyCallStatusModel.setUserData((String) orderBindMap.get("userData"));
				if (caller.equals(orderBindMap.get("caller"))) {
					safetyCallStatusModel.setFlag("0");
				} else if (callee.equals(orderBindMap.get("caller"))) {
					safetyCallStatusModel.setFlag("1");
				}
				safetyCallStatusModel.setBeginTime(DateUtil.dateStr2Str(hwCallBackModel.getCallEvent().getTimeStamp(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
						"yyyy-MM-dd HH:mm:ss"));
				safetyCallStatusModel.setDstVirtualNum(virtualNumber);
				try {
					HttpUtils.httpConnectionPost(statusUrl, JsonUtil.toJsonStr(safetyCallStatusModel));
					logger.info("状态回调成功：{}", JsonUtil.toJsonStr(safetyCallStatusModel));
				} catch (Exception e) {
					logger.error("状态回调失败：{}", JsonUtil.toJsonStr(safetyCallStatusModel), e);
				}
			}
		}
	}

	/**
	 * 呼叫结束事件、异常事件处理
	 * 
	 * @param eventType
	 *            0：呼叫正常，1：呼叫异常
	 * @param extInfo
	 */
	private void dealEvent(SafetyCallHwBillModel hwBillModel, String eventType) {
		List<Map<String, Object>> exParams = new ArrayList<Map<String, Object>>();
		if (hwBillModel.getCallEvent().getExtInfo() != null) {
			exParams = hwBillModel.getCallEvent().getExtInfo().getExtParas();
		}
		// 通话时长。单位：秒
		String duration = "";
		// 绑定id
		String bindID = "";
		// 呼叫开始的时间戳
		String startTime = "";
		String endTime = DateUtil.dateStr2Str(hwBillModel.getCallEvent().getTimeStamp(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss");

		String ReleaseReason = "";

		for (int i = 0; i < exParams.size(); i++) {
			Map<String, Object> temp = exParams.get(i);
			String key = String.valueOf(temp.get("key"));
			String value = String.valueOf(temp.get("value"));
			if ("Duration".equalsIgnoreCase(key)) {
				duration = value;
			}
			if ("BindID".equalsIgnoreCase(key)) {
				bindID = value;
			}
			if ("StartTime".equalsIgnoreCase(key)) {
				startTime = DateUtil.dateStr2Str(value, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss");
			}
			if ("ReleaseReason".equalsIgnoreCase(key)) {
				ReleaseReason = value;
			}
		}

		// 呼叫结束时间
		if (StringUtil.isBlank(startTime)) {
			startTime = DateUtil.addSecond(endTime, Double.valueOf(duration).intValue() * -1);
		} else {
			endTime = DateUtil.addSecond(startTime, Double.valueOf(duration).intValue());
		}

		String productType = "0";
		// 根据绑定id查询绑定信息
		Map<String, Object> orderBindMap = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		if (StringUtil.isBlank(bindID)) { // AX
			productType = "1";
			params.put("virtualNumber", remove86MobileNationPrefix(hwBillModel.getCallEvent().getVirtualNumber()));
			orderBindMap = dao.selectOne("common.getBindOrderByNumber", params);
		} else { // AXB
			productType = "0";
			params.put("subid", bindID);
			orderBindMap = dao.selectOne("common.getBindOrder", params);
		}

		BillingModel billingModel = new BillingModel();

		billingModel.setBeginTime(startTime);
		billingModel.setBeginTimeB(startTime);
		billingModel.setCalled(remove86MobileNationPrefix(hwBillModel.getCallEvent().getVirtualNumber()));
		billingModel.setCalledDisplay(remove86MobileNationPrefix(hwBillModel.getCallEvent().getVirtualNumber()));
		billingModel.setCaller(remove86MobileNationPrefix(hwBillModel.getCallEvent().getCalling()));
		billingModel.setCallerDisplay(remove86MobileNationPrefix(hwBillModel.getCallEvent().getVirtualNumber()));
		billingModel.setCallID((String) orderBindMap.get("requestId"));
		billingModel.setCallTime(new BigDecimal(duration).longValue() * 1000);
		billingModel.setCallTimeB(new BigDecimal(duration).longValue() * 1000);
		billingModel.setEndTime(endTime);
		billingModel.setEndTimeB(endTime);
		billingModel.setCallStatus(eventType);
		billingModel.setCallStatusB(eventType);
		billingModel.setEvent("0");
		billingModel.setMessage(ReleaseReason);
		billingModel.setProductType(productType);
		billingModel.setRealityNumber(remove86MobileNationPrefix(hwBillModel.getCallEvent().getCalled()));
		billingModel.setRecordType((String) orderBindMap.get("record"));
		billingModel.setRecordUrl("");
		billingModel.setUserId((String) orderBindMap.get("userId"));
		// 计费url
		String billingUrl = ConfigUtils.getProperty("billingUrl", String.class);
		try {

			logger.info("华为话单扣费信息:params = {}", billingModel);
			HttpUtils.httpConnectionPost(billingUrl, JsonUtil.toJsonStr(billingModel));
			logger.info("话单扣費成功：{}", JsonUtil.toJsonStr(billingModel));
		} catch (Exception e) {
			logger.error("话单扣费失败：{}", JsonUtil.toJsonStr(billingModel), e);
		}
		// 获取回调客户地址
		String billUrl = String.valueOf(orderBindMap.get("hangupUrl"));
		if (StringUtil.isBlank(billUrl)) {
			logger.info("话单回调地址为空，不进行回调");
		} else {
			logger.info("话单回调地址billUrl={}，开始进行回调...", billUrl);

			SafetyCallBillModel safetyCallBillModel = new SafetyCallBillModel();
			safetyCallBillModel.setBindId((String) orderBindMap.get("bindId"));
			safetyCallBillModel.setCallee(remove86MobileNationPrefix(hwBillModel.getCallEvent().getCalled()));
			safetyCallBillModel.setCaller(remove86MobileNationPrefix(hwBillModel.getCallEvent().getCalling()));
			safetyCallBillModel.setCallId((String) orderBindMap.get("requestId"));
			safetyCallBillModel.setCallStatus(eventType);
			safetyCallBillModel.setCallTime(duration);
			safetyCallBillModel.setDstVirtualNum(remove86MobileNationPrefix(hwBillModel.getCallEvent().getVirtualNumber()));
			safetyCallBillModel.setEndTime(endTime);
			safetyCallBillModel.setBeginTime(startTime);
			safetyCallBillModel.setCalleeDisplay((String) orderBindMap.get("calleeDisplay"));
			safetyCallBillModel.setRecord((String) orderBindMap.get("record"));
			safetyCallBillModel.setUserData((String) orderBindMap.get("userData"));
			safetyCallBillModel.setUserId((String) orderBindMap.get("userId"));
			if (remove86MobileNationPrefix(hwBillModel.getCallEvent().getCalling()).equals(orderBindMap.get("caller"))) {
				safetyCallBillModel.setFlag("0");
			} else if (remove86MobileNationPrefix(hwBillModel.getCallEvent().getCalled()).equals(orderBindMap.get("caller"))) {
				safetyCallBillModel.setFlag("1");
			}
			if ("Caller Hang up".equals(ReleaseReason) || "Called Hang up".equals(ReleaseReason)) { // 正常呼叫拆线
																									// Caller
																									// Hang
																									// up：
																									// Called
																									// Hang
																									// up：
				safetyCallBillModel.setCallStatus("0");
			} else if ("Busy".equals(ReleaseReason)) { // 用户忙
														// Busy
				safetyCallBillModel.setCallStatus("2");
			} else if ("Not Reachable".equals(ReleaseReason)) { // 用户未响应
																// 被叫不可达
				safetyCallBillModel.setCallStatus("3");
			} else if ("No Answer".equals(ReleaseReason)) { // 用户未应答
															// No
															// Answer：
															// 被叫无应答
				safetyCallBillModel.setCallStatus("4");
			} else if ("Abandon".equals(ReleaseReason)) { // 用户缺席
															// 主叫放弃
				safetyCallBillModel.setCallStatus("4");
			} else if ("Call Terminated".equals(ReleaseReason) || "Call Forbidden".equals(ReleaseReason)) { // 呼叫拒收
																											// Call
																											// Terminated:
																											// 呼叫被终止
																											// Call
																											// Forbidden：
																											// 呼叫被禁止
				safetyCallBillModel.setCallStatus("5");
			} else { // 其他
				safetyCallBillModel.setCallStatus("7"); // 呼叫被终止 Call
														// Terminated：呼叫被禁止
														// Call Forbidden：
			}
			try {
				logger.info("华为话单回调 info = {}", safetyCallBillModel);
				HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(safetyCallBillModel));
				logger.info("话单回调成功：{}", JsonUtil.toJsonStr(safetyCallBillModel));
			} catch (Exception e) {
				logger.error("话单回调失败：{}", JsonUtil.toJsonStr(safetyCallBillModel), e);
			}

			// TODO 录音回调
			String recordUrl = String.valueOf(orderBindMap.get("recordUrl"));
			if (StringUtil.isNotEmpty(recordUrl)) {
				String callIdentifier = hwBillModel.getCallEvent().getCallIdentifier(); // 通话唯一标识
				Map<String, Object> recordMap = new HashMap<String, Object>();
				recordMap.put("callIdentifier", callIdentifier);
				recordMap.put("recordUrl", recordUrl);
				recordMap.put("userData", String.valueOf(orderBindMap.get("userData")));
			}
		}
	}

}
