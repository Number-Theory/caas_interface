package com.caas.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.DeductionModel;
import com.google.gson.reflect.TypeToken;
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
public class DeductionService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(DeductionService.class);

	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		super.callService(ctx, request, response, paramsObject);
		DeductionModel deductionModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<DeductionModel>() {
		}.getType());

		String userId = deductionModel.getUserID();

		// 更新账户余额
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("balance", deductionModel.getDeductionMoney());
		dao.update("common.updateBalance", map);
		logger.info("更新账户[" + userId + "]余额[" + deductionModel.getDeductionMoney() + "]成功!");
		// 插入扣费明细
		dao.insert("common.insertDeduction", JsonUtil.jsonStrToMap(JsonUtil.toJsonStr(deductionModel)));
		logger.info("插入扣费明细成功！");
		// 更新扣除状态
		if ("0".equals(deductionModel.getDeductionType())) { // 通话类型
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("userId", userId);
			params.put("callId", deductionModel.getDeductionCode());
			dao.update("common.updateDeductionStatus", params);
		}
		logger.info("更新扣费状态成功！");
	}
}
