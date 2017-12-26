package com.caas.service.callback.safetyCall;

import io.netty.channel.ChannelHandlerContext;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.BillingModel;
import com.caas.model.FinishModel;
import com.caas.model.callback.safetycall.SafetyCallBillModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 
 * @author xupiao 2017年9月17日
 *
 */
@Service
public class CallFinishAXBService extends DefaultServiceCallBack {

	private static final Logger logger = LogManager.getLogger(CallFinishAXBService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		FinishModel finishModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<FinishModel>() {
		}.getType());

		logger.info("【AXB话单回调接口】接收到广西话单回调请求内容：{}", finishModel);

		String subid = finishModel.getSubid();

		Map<String, Object> orderBindMap = dao.selectOne("common.getBindOrder", subid);

		BillingModel billingModel = new BillingModel();
		billingModel.setBeginTime(finishModel.getStarttime());
		billingModel.setBeginTimeB(finishModel.getStarttime());
		billingModel.setCalled(finishModel.getTelX());
		billingModel.setCalledDisplay(finishModel.getTelX());
		billingModel.setCaller(finishModel.getTelA());
		billingModel.setCallerDisplay(finishModel.getTelX());
		billingModel.setCallID((String) orderBindMap.get("requestId"));
		if ("31".equals(finishModel.getReleasecause())) {
			billingModel.setCallStatus("1");
			billingModel.setCallStatusB("1");
		} else {
			billingModel.setCallStatus("0");
			billingModel.setCallStatusB("0");
		}
		Long callTime = 0L;
		try {
			callTime = DateUtil.getTime(finishModel.getReleasetime(), "yyyy-MM-dd HH:mm:ss")
					- DateUtil.getTime(finishModel.getStarttime(), "yyyy-MM-dd HH:mm:ss");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		billingModel.setCallTime(callTime);
		billingModel.setCallTimeB(callTime);
		billingModel.setEndTime(finishModel.getReleasetime());
		billingModel.setEndTimeB(finishModel.getReleasetime());
		billingModel.setEvent("0");
		billingModel.setMessage(finishModel.getReleasecause());
		billingModel.setProductType("0");
		billingModel.setRealityNumber(finishModel.getTelB());
		billingModel.setRecordType((String) orderBindMap.get("record"));
		billingModel.setRecordUrl(finishModel.getRecordUrl());
		billingModel.setUserId((String) orderBindMap.get("userId"));
		String billingUrl = ConfigUtils.getProperty("billingUrl", String.class);
		try {
			HttpUtils.httpConnectionPost(billingUrl, JsonUtil.toJsonStr(billingModel));
			logger.info("话单扣費成功：{}", JsonUtil.toJsonStr(billingModel));
		} catch (Exception e) {
			logger.error("话单扣费失败：{}", JsonUtil.toJsonStr(billingModel), e);
		}

		String billUrl = dao.selectOne("common.getBindOrderBillUrl", subid);
		if (StringUtil.isBlank(billUrl)) {
			logger.info("话单回调地址为空，不进行回调");
		} else {
			logger.info("话单回调地址billUrl={}，开始进行回调...", billUrl);

			SafetyCallBillModel safetyCallBillModel = new SafetyCallBillModel();
			safetyCallBillModel.setBindId((String) orderBindMap.get("bindId"));
			safetyCallBillModel.setCallee(finishModel.getTelB());
			safetyCallBillModel.setCaller(finishModel.getTelA());
			safetyCallBillModel.setCallId((String) orderBindMap.get("requestId"));
			if ("31".equals(finishModel.getReleasecause())) {
				safetyCallBillModel.setCallStatus("1");
			} else {
				safetyCallBillModel.setCallStatus("0");
			}
			safetyCallBillModel.setCallTime(finishModel.getCalltime());
			safetyCallBillModel.setDstVirtualNum(finishModel.getTelX());
			safetyCallBillModel.setEndTime(finishModel.getReleasetime());
			safetyCallBillModel.setRecordUrl(finishModel.getRecordUrl());
			safetyCallBillModel.setBeginTime(finishModel.getStarttime());
			// safetyCallBillModel.setCalleeCityCode(calleeCityCode);
			safetyCallBillModel.setCalleeDisplay((String) orderBindMap.get("calleeDisplay"));
			safetyCallBillModel.setRecord(finishModel.getCallrecording());
			safetyCallBillModel.setUserData((String) orderBindMap.get("userData"));
			safetyCallBillModel.setUserId((String) orderBindMap.get("userId"));
			if ("10".equals(finishModel.getCalltype())) {
				safetyCallBillModel.setFlag("0");
			} else if ("11".equals(finishModel.getCalltype())) {
				safetyCallBillModel.setFlag("1");
			}
			if ("16".equals(finishModel.getReleasecause()) || "31".equals(finishModel.getReleasecause())) { // 正常呼叫拆线
				safetyCallBillModel.setCallStatus("0");
			} else if ("17".equals(finishModel.getReleasecause())) { // 用户忙
				safetyCallBillModel.setCallStatus("2");
			} else if ("18".equals(finishModel.getReleasecause())) { // 用户未响应
				safetyCallBillModel.setCallStatus("3");
			} else if ("19".equals(finishModel.getReleasecause())) { // 用户未应答
				safetyCallBillModel.setCallStatus("4");
			} else if ("20".equals(finishModel.getReleasecause())) { // 用户缺席
				safetyCallBillModel.setCallStatus("4");
			} else if ("21".equals(finishModel.getReleasecause())) { // 呼叫拒收
				safetyCallBillModel.setCallStatus("5");
			} else { // 其他
				safetyCallBillModel.setCallStatus("7");
			}
			safetyCallBillModel.setCallTime(finishModel.getCalltime());
			safetyCallBillModel.setDstVirtualNum(finishModel.getTelX());
			try {
				HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(safetyCallBillModel));
				logger.info("话单回调成功：{}", JsonUtil.toJsonStr(safetyCallBillModel));
			} catch (Exception e) {
				logger.error("话单回调失败：{}", JsonUtil.toJsonStr(safetyCallBillModel), e);
			}

			String recordUrl = dao.selectOne("common.getBindOrderRecordUrl", subid);
			if (StringUtil.isBlank(recordUrl) || "".equals(finishModel.getRecordUrl()) || "0".equals(finishModel.getCallrecording())) {
				logger.info("录音回调地址为空或不录音，不进行回调");
			} else {
				logger.info("录音回调地址recordUrl={}，开始进行回调...", recordUrl);

				Map<String, Object> recordCallback = new HashMap<String, Object>();
				recordCallback.put("callId", orderBindMap.get("requestId"));
				recordCallback.put("recordUrl", finishModel.getRecordUrl());
				recordCallback.put("userData", orderBindMap.get("userData"));
				try {
					HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStrDisableHtmlEscaping(recordCallback));
					logger.info("录音回调成功：{}", JsonUtil.toJsonStrDisableHtmlEscaping(recordCallback));
				} catch (Exception e) {
					logger.error("录音回调失败：{}", JsonUtil.toJsonStrDisableHtmlEscaping(recordCallback), e);
				}
			}
		}
	}
}
