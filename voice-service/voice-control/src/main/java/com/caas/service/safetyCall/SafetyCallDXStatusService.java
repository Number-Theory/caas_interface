package com.caas.service.safetyCall;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.caas.model.CallinModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * 小号，东信AX状态回调
 * 
 * @author xupiao 2017年8月21日
 *
 */
@Service
public class SafetyCallDXStatusService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(SafetyCallDXStatusService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		CallinModel callinModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<CallinModel>() {
		}.getType());

		status(callinModel);
	}

	public void status(CallinModel callinModel) {
		String timeStamp = DateUtil.date2TimeStamp(callinModel.getCalltime(), null);// 状态发生的时间戳

		String cityId = "";

		// 通过subid获取AXB绑定时放入redis的缓存
		final String callerNumBindKey = RedisKeyConsts.getKey(RedisKeyConsts.AXBNUMBINDS, callinModel.getTelA(), callinModel.getTelX());
		final Map<String, String> callerBindIdMap = RedisOpClient.hgetall(callerNumBindKey);
		String bindId = callerBindIdMap.get("bindId");
		String statusUrl = callerBindIdMap.get("statusUrl");
		if (StringUtils.isNotBlank(statusUrl)) {
			String orderBindKey = RedisKeyConsts.getKey(RedisKeyConsts.ORDERBINDS, bindId);
			Map<String, String> orderBindMap = RedisOpClient.hgetall(orderBindKey);
			if (orderBindMap != null) {
				try {
					String requestId = orderBindMap.get("requestId");
					String userId = orderBindMap.get("userId");
					cityId = orderBindMap.get("cityId");
					String productType = orderBindMap.get("productType");
					String hangupUrl = orderBindMap.get("hangupUrl");// 话单回调地址
					String storeUrl = orderBindMap.get("storeUrl");// 话单下载地址的前缀
					String record = orderBindMap.get("record");// 0-不录音，1-录音

					// 判断主叫和被叫
					Map<String, String> callMap = judgeCallerCallee(callinModel.getTelA(), callinModel.getTelB(), callinModel.getCalltype());

					String caller = callMap.get("caller");
					String callee = callMap.get("callee");
					logger.info("【发起呼叫推送】此次通话主叫caller为:" + caller + "；被叫callee为：" + callee);

					// 状态转换
					String userFlag = "caller";// 该路通话中用于区分状态属于主叫/被叫

					String callId = callinModel.getCallid();

					String callStatus = "invite";// 发起呼叫

					logger.info("【发起呼叫推送】状态回调开始-------------");

					logger.info("【发起呼叫推送】状态回调结束-------------");

				} catch (Exception e) {
					logger.info("【发起呼叫推送】报错，e=" + e);
				}
			} else {
				logger.info("【发起呼叫推送】通过主被叫查不到订单缓存，无法进行业务处理-------------");
			}

			// StatusCallbackModel statusCallbackModel = new
			// StatusCallbackModel();
			//
			// try {
			// new HttpClient(new ClientHandler() {
			//
			// @Override
			// public void failed(Exception ex) {
			//
			// }
			//
			// @Override
			// public void execute(HttpResponse response, String context) {
			//
			// }
			// }).httpPost("", JsonUtil.toJsonStr(statusCallbackModel));
			// } catch (Exception e) {
			// // TODO Auto-generated catch block
			// logger.error("请求状态回调组件失败：", e);
			// }
		}
	}

	public Map<String, String> judgeCallerCallee(String tellA, String tellB, String callType) {
		Map<String, String> map = new HashMap<String, String>();
		String caller = tellA;
		String callee = tellB;
		if ("11".equals(callType) || "1".equals(callType)) {// AXB业务（针对被保护号码A来分：10：通话主叫，11：通话被叫
															// //
															// AX业务：0-DTMF通话主叫，1-通话被叫，128-ps通话主叫）
			caller = tellB;
			callee = tellA;
			// B打X找到A
			map.put("type", "0");

		} else if ("20".equals(callType)) {// 呼叫不允许，即无绑定关系，被叫为空
			callee = "";
			// 异常
			map.put("type", "1");
		} else {
			map.put("type", "2");
			map.put("realCall", callee);
		}
		// A打X找到B
		map.put("caller", caller);
		map.put("callee", callee);
		return map;
	}
}
