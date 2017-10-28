package com.caas.service;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.SafetyCallModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 
 * @author xupiao 2017年8月21日
 *
 */
@Service
public class QueryBindAXBService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(QueryBindAXBService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		final String callId = UUID.randomUUID().toString().replace("-", "");

		HttpRequest httpRequest = (HttpRequest) request.getHttpRequest();

		// 获取主账户ID
		String userId = ObjectUtils.defaultIfNull(paramsObject.get("userId").toString(), "");
		
		// 解析用户传入的字段
		final SafetyCallModel safetyCallModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<SafetyCallModel>() {
		}.getType());
		
		String dstVirtualNum = safetyCallModel.getDstVirtualNum();
		
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("dstVirtualNum", dstVirtualNum);
		sqlParams.put("userId", userId);
		
		List<Map<String, Object>> bindList = dao.selectList("common.getAllBindByDstVirtaulNum", sqlParams);
		
		logger.info("查询的绑定关系为:{}", bindList);
		
		response.getOtherMap().put("binds", bindList);
		
		setResponse(callId, response, BusiErrorCode.B_000000, REST_EVENT, "");
		
	}
}
