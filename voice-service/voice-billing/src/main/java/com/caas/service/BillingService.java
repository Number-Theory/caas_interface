package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.BillingModel;
import com.caas.model.DeductionModel;
import com.caas.service.impl.CallbackHandler;
import com.caas.service.impl.MinNumberHandler;
import com.caas.service.impl.SafetyCallHandler;
import com.caas.service.impl.VoiceCodeHandler;
import com.caas.service.impl.VoiceNotifyHandler;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
@Service
public class BillingService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(BillingService.class);
	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		BillingModel billingModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<BillingModel>() {
		}.getType());

		BillingHandler billingHandler = null;
		String productType = billingModel.getProductType();
		if ("0".equals(productType)) { // 隐号
			billingHandler = new SafetyCallHandler();
		} else if ("1".equals(productType)) { // 小号
			billingHandler = new MinNumberHandler();
		} else if ("2".equals(productType)) { // 回拨
			billingHandler = new CallbackHandler();
		} else if ("3".equals(productType)) { // 语音验证码
			billingHandler = new VoiceCodeHandler();
		} else if ("4".equals(productType)) { // 语音通知
			billingHandler = new VoiceNotifyHandler();
		}
		// 扣费AND计费
		billingHandler.handler(billingModel, response);

		// 对接扣费组件
		DeductionModel deductionModel = new DeductionModel();
		deductionModel.setDeductionCode(billingModel.getCallID());
		deductionModel.setDeductionMoney((Long)response.getOtherMap().get("payMoney")); // 总金额
		deductionModel.setDeductionType("0");
		deductionModel.setEvent(DEDUCTION_EVENT);
		deductionModel.setProductType(productType);
		deductionModel.setUserData(billingModel.getUserData());
		deductionModel.setUserId(billingModel.getUserId());

		String deductionUrl = ConfigUtils.getProperty("deductionUrl", String.class);
		try {
			new HttpClient(new ClientHandler() {
				@Override
				public void failed(Exception ex) {
					logger.error("扣费失败", ex);
				}

				@Override
				public void execute(HttpResponse response, String context) {
					logger.info("扣费完成：{}", context);
				}
			}).httpPost(deductionUrl, JsonUtil.toJsonStr(deductionModel)); //
		} catch (Exception e) {
			// TODO
			logger.error("请求扣费组件失败：", e);
		}

	}
}
