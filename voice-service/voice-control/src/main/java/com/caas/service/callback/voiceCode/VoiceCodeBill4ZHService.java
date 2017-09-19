package com.caas.service.callback.voiceCode;

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
import com.caas.model.VoiceBill4ZHModel;
import com.caas.model.VoiceBill4ZHModel.Bill;
import com.caas.model.VoiceCodeModel;
import com.caas.model.XmlCallBackModel;
import com.caas.model.callback.voiceCode.VoiceCodeCallbackModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.util.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.core.util.XMLUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * Created by Jweikai on 2017/9/17.
 */
@Service
public class VoiceCodeBill4ZHService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(VoiceCodeBill4ZHService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		VoiceBill4ZHModel vcBill = (VoiceBill4ZHModel) XMLUtil.convertXmlStrToObject(VoiceBill4ZHModel.class, request.getRequestString());
		Bill bill = vcBill.getBills().get(0);

		logger.info("【接收到Rest组件请求信息】vioceCodeBill4ZH={}", vcBill);
		String callId = bill.getExtparam();
		String voiceCodeModelString = RedisOpClient.get(RedisKeyConsts.getKey(RedisKeyConsts.VOICE_CODE_SESSION, callId));
		VoiceCodeModel voiceCodeModel = JsonUtil.fromJson(voiceCodeModelString, new TypeToken<VoiceCodeModel>() {
		}.getType());

		String billUrl = "";
		if (StringUtil.isBlank(voiceCodeModel.getBillUrl())) {
			Map<String, Object> sqlParams = new HashMap<String, Object>();
			sqlParams.put("userId", voiceCodeModel.getUserId());
			sqlParams.put("productType", "3");
			billUrl = dao.selectOne("common.getAppBillUrl", sqlParams);
		} else {
			billUrl = voiceCodeModel.getBillUrl();
		}
		if (StringUtil.isNotEmpty(billUrl)) { // 开始对接billing扣费、回调
			BillingModel billingModel = new BillingModel();
			billingModel.setBeginTime(bill.getStarttime());
			billingModel.setCalled(bill.getCalled());
			billingModel.setCalledDisplay(bill.getCaller());
			billingModel.setCaller(bill.getCaller());
			billingModel.setCallID(callId);
			if ("2".equals(bill.getStatus())) {
				billingModel.setCallStatus("0");
			} else {
				billingModel.setCallStatus("1");
			}
			Long callTime = 0L;
			try {
				callTime = DateUtil.getTime(bill.getEndtime(), "yyyy-MM-dd HH:mm:ss") - DateUtil.getTime(bill.getStarttime(), "yyyy-MM-dd HH:mm:ss");
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			billingModel.setCallTime(callTime);
			billingModel.setEndTime(bill.getEndtime());
			billingModel.setEvent("3");
			billingModel.setProductType("3");
			billingModel.setRealityNumber(voiceCodeModel.getCaller());
			billingModel.setRecordType("1");
			billingModel.setRecordUrl("");
			billingModel.setUserId(voiceCodeModel.getUserId());
			String billingUrl = ConfigUtils.getProperty("billingUrl", String.class);

			try {
				com.yzx.access.client.HttpUtils.httpConnectionPost(billingUrl, JsonUtil.toJsonStr(billingModel));
				logger.info("话单扣費成功：{}", JsonUtil.toJsonStr(billingModel));
			} catch (Exception e) {
				logger.error("话单扣费失败：{}", JsonUtil.toJsonStr(billingModel), e);
			}

			logger.info("话单回调地址billUrl={}，开始进行回调...", billUrl);
			VoiceCodeCallbackModel voiceCodeCallbackModel = new VoiceCodeCallbackModel();
			voiceCodeCallbackModel.setCallee(bill.getCalled());
			voiceCodeCallbackModel.setCaller(bill.getCaller());
			voiceCodeCallbackModel.setCallId(callId);
			voiceCodeCallbackModel.setEndTime(bill.getEndtime());
			voiceCodeCallbackModel.setStartTime(bill.getStarttime());
			if ("2".equals(bill.getStatus())) {
				voiceCodeCallbackModel.setStatus("0");
			} else {
				voiceCodeCallbackModel.setStatus("1");
			}
			voiceCodeCallbackModel.setUserId(voiceCodeModel.getUserId());

			try {
				com.yzx.access.client.HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(voiceCodeCallbackModel));
				logger.info("话单回调成功：{}", JsonUtil.toJsonStr(voiceCodeCallbackModel));
			} catch (Exception e) {
				logger.error("话单回调失败：{}", JsonUtil.toJsonStr(voiceCodeCallbackModel), e);
			}

		} else {
			logger.info("回调地址为空，不进行回调！");
		}
		// TODO 处理业务

		HttpUtils.sendMessageXml(ctx, XMLUtil.convertToXml(new XmlCallBackModel()));
	}
}
