package com.caas.service.callback.voiceNotify;

import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.VoiceBill4ZHModel;
import com.caas.model.VoiceCodeModel;
import com.caas.model.VoiceBill4ZHModel.Bill;
import com.caas.model.XmlCallBackModel;
import com.caas.model.callback.voiceCode.VoiceCodeCallbackModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.util.HttpUtils;
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
public class VoiceNotifyBill4ZHService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(VoiceNotifyBill4ZHService.class);
	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		VoiceBill4ZHModel vcBill = (VoiceBill4ZHModel) XMLUtil.convertXmlStrToObject(VoiceBill4ZHModel.class, request.getRequestString());
		Bill bill = vcBill.getBills().get(0);

		logger.info("【接收到Rest组件请求信息】vioceNotifyBill4ZH={}", vcBill);
		

		// TODO 处理业务

		HttpUtils.sendMessageXml(ctx, XMLUtil.convertToXml(new XmlCallBackModel()));
	}
}
