package com.caas.service.callback.minNum;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.CallinModel;
import com.caas.model.callback.safetycall.SafetyCallStatusModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
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
public class CallInAXService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(CallInAXService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		CallinModel callinModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<CallinModel>() {
		}.getType());

		logger.info("【AX状态回调接口】接收到广西状态回调请求内容：{}", callinModel);

		String subid = callinModel.getSubid();
		String statusUrl = dao.selectOne("common.getBindOrderStatusUrl", subid);
		if (StringUtil.isBlank(statusUrl)) {
			logger.info("状态回调地址为空，不进行回调");
		} else {
			logger.info("状态回调地址statusUrl={}，开始进行回调...", statusUrl);
			Map<String, Object> orderBindMap = dao.selectOne("common.getBindOrder", subid);
			SafetyCallStatusModel safetyCallStatusModel = new SafetyCallStatusModel();
			safetyCallStatusModel.setBindId((String) orderBindMap.get("bindId"));
			safetyCallStatusModel.setCallee(callinModel.getTelA());
			safetyCallStatusModel.setCaller(callinModel.getTelB());
			safetyCallStatusModel.setCallId((String) orderBindMap.get("requestId"));
			safetyCallStatusModel.setCalleeDisplay((String) orderBindMap.get("calleeDisplay"));
			safetyCallStatusModel.setRecord(callinModel.getCallrecording());
			safetyCallStatusModel.setUserData((String) orderBindMap.get("userData"));
			if ("0".equals(callinModel.getCalltype())) {
				safetyCallStatusModel.setFlag("0");
			} else if ("1".equals(callinModel.getCalltype())) {
				safetyCallStatusModel.setFlag("1");
			}
			safetyCallStatusModel.setBeginTime(callinModel.getCalltime());
			safetyCallStatusModel.setDstVirtualNum(callinModel.getTelX());
			try {
				HttpUtils.httpConnectionPost(statusUrl, JsonUtil.toJsonStr(safetyCallStatusModel));
				logger.info("状态回调成功：{}", JsonUtil.toJsonStr(safetyCallStatusModel));
			} catch (Exception e) {
				logger.error("状态回调失败：{}", JsonUtil.toJsonStr(safetyCallStatusModel), e);
			}
		}

	}
}
