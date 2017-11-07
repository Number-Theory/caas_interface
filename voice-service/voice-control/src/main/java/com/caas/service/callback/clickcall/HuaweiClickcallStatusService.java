package com.caas.service.callback.clickcall;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.huawei.CallStatusInfo;
import com.caas.model.ClickCallModel;
import com.caas.model.ClickCallStatusModel;
import com.caas.model.HuaweiClickCallBillModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * 
 * @author xupiao 2017年11月2日
 *
 */
@Service
public class HuaweiClickcallStatusService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(HuaweiClickcallStatusService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		HuaweiClickCallBillModel huaweiClickCallBillModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<HuaweiClickCallBillModel>() {
		}.getType());

		logger.info("【华为点击呼叫状态回调接口】接收到华为状态回调请求内容：{}", huaweiClickCallBillModel);

		if ("callin".equals(huaweiClickCallBillModel.getEventType()) || "callout".equals(huaweiClickCallBillModel.getEventType())
				|| "alerting".equals(huaweiClickCallBillModel.getEventType()) || "answer".equals(huaweiClickCallBillModel.getEventType())
				|| "disconnect".equals(huaweiClickCallBillModel.getEventType())) {
			CallStatusInfo callStatusInfo = huaweiClickCallBillModel.getStatusInfo();
			String sessionId = callStatusInfo.getSessionId();
			String clickCallModelString = RedisOpClient.get(RedisKeyConsts.getKey(RedisKeyConsts.CB_REQUEST, sessionId));
			ClickCallModel clickCallModel = JsonUtil.fromJson(clickCallModelString, new TypeToken<ClickCallModel>() {
			}.getType());
			String statusUrl = clickCallModel.getStatusUrl();
			if (StringUtil.isBlank(statusUrl)) {
				logger.info("状态回调地址为空，不进行回调");
			} else {
				logger.info("状态回调地址statusUrl={}，开始进行回调...", statusUrl);
				ClickCallStatusModel clickCallStatusModel = new ClickCallStatusModel();
				clickCallStatusModel.setCaller(callStatusInfo.getCaller());
				clickCallStatusModel.setCalled(callStatusInfo.getCalled());
				clickCallStatusModel.setCallId(clickCallModel.getCallId());
				clickCallStatusModel.setEventTime(callStatusInfo.getTimestamp());
				if ("callin".equals(huaweiClickCallBillModel.getEventType())) {
					clickCallStatusModel.setCallStatus("callin");
				} else if ("callout".equals(huaweiClickCallBillModel.getEventType())) {
					clickCallStatusModel.setCallStatus("callout");
				} else if ("alerting".equals(huaweiClickCallBillModel.getEventType())) {
					clickCallStatusModel.setCallStatus("ringing");
				} else if ("answer".equals(huaweiClickCallBillModel.getEventType())) {
					clickCallStatusModel.setCallStatus("answer");
				} else if ("disconnect".equals(huaweiClickCallBillModel.getEventType())) {
					clickCallStatusModel.setCallStatus("disconnect");
				}
				clickCallStatusModel.setUserData(clickCallModel.getUserData());

				try {
					HttpUtils.httpConnectionPost(statusUrl, JsonUtil.toJsonStr(clickCallStatusModel));
					logger.info("状态回调成功：{}", JsonUtil.toJsonStr(clickCallStatusModel));
				} catch (Exception e) {
					logger.error("状态回调失败：{}", JsonUtil.toJsonStr(clickCallStatusModel), e);
				}
			}
		}

	}
}
