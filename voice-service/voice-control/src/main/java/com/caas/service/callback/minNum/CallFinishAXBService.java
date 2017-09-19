package com.caas.service.callback.minNum;

import io.netty.channel.ChannelHandlerContext;

import java.text.ParseException;
import java.util.Map;

import org.apache.commons.pool.impl.GenericKeyedObjectPool.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.BillingModel;
import com.caas.model.FinishModel;
import com.caas.model.callback.safetycall.SafetyCallBillModel;
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
		String billUrl = dao.selectOne("common.getBindOrderBillUrl", subid);
		if (StringUtil.isBlank(billUrl)) {
			logger.info("话单回调地址为空，不进行回调");
		} else {
			Map<String, Object> orderBindMap = dao.selectOne("common.getBindOrder", subid);

			BillingModel billingModel = new BillingModel();
			billingModel.setBeginTime(finishModel.getStarttime());
			billingModel.setBeginTimeB(finishModel.getStarttime());
			billingModel.setCalled(finishModel.getTelB());
			billingModel.setCalledDisplay(finishModel.getTelX());
			billingModel.setCaller(finishModel.getTelA());
			billingModel.setCallerDisplay(finishModel.getTelX());
			billingModel.setCallID((String) orderBindMap.get("requestId")); // TODO
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
			safetyCallBillModel.setRingingTime(finishModel.getRingingtime());
			safetyCallBillModel.setStartTime(finishModel.getStarttime());
			safetyCallBillModel.setUserId((String) orderBindMap.get("userId"));
			if ("20".equals(finishModel.getCalltype())) {
				safetyCallBillModel.setCallStatus("failed");
			} else {
				safetyCallBillModel.setCallStatus("invite");
			}
			safetyCallBillModel.setCallTime(finishModel.getCalltime());
			safetyCallBillModel.setDstVirtualNum(finishModel.getTelX());
			try {
				HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(safetyCallBillModel));
				logger.info("话单回调成功：{}", JsonUtil.toJsonStr(safetyCallBillModel));
			} catch (Exception e) {
				logger.error("话单回调失败：{}", JsonUtil.toJsonStr(safetyCallBillModel), e);
			}
		}
	}
}
