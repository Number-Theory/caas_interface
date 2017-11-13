package com.caas.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.caas.dao.CaasDao;
import com.caas.model.BillingModel;
import com.caas.util.NumberUtils;
import com.yzx.auth.plugin.SpringContext;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceResponse;

/**
 * 标准回拨计费、入库
 * 
 * @author xupiao 2017年8月17日
 *
 */
public class CallbackHandler extends DefaultBillingHandler {

	private static final Logger logger = LogManager.getLogger(CallbackHandler.class);

	private CaasDao dao = SpringContext.getInstance(CaasDao.class);

	@Override
	public void handler(BillingModel billingModel, ServiceResponse response) {
		String userId = billingModel.getUserId();
		String productType = billingModel.getProductType();
		String caller = billingModel.getCaller(); // 原始主叫
		String callee = billingModel.getCalled(); // 原始被叫

		// 根据号码获取费率
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("phoneNumber", billingModel.getCallerDisplay());
		params.put("productType", productType);
		params.put("userId", billingModel.getUserId());
		String selectPhoneNumber = billingModel.getCallerDisplay();
		boolean flag = true;
		Map<String, Object> rateMap = dao.selectOne("common.getNumberRate", params);
		if (rateMap == null || rateMap.isEmpty()) {
			logger.info("获取号码套餐失败，查询用户默认套餐！");
			params.put("phoneNumber", userId);
			selectPhoneNumber = userId;
			rateMap = dao.selectOne("common.getNumberRate", params);
			if (rateMap == null || rateMap.isEmpty()) {
				logger.info("获取用户套餐失败，查询默认套餐！");
				selectPhoneNumber = "0";
				params.put("phoneNumber", "0");
				rateMap = dao.selectOne("common.getNumberRate", params);
				flag = false;
			}
		}
		logger.info("查询到的套餐为[{}]", rateMap);

		// 计费
		String billingType = (String) rateMap.get("billingType");
		Long callTime = 0L, callTimeB = 0L, payMoney = 0L;
		String callerCity = NumberUtils.getMobileAttribution(caller), calledCity = NumberUtils.getMobileAttribution(callee);
		Long callPrice = 0L, callPriceB = 0L, deductionUnit = 0L, deductionUnitB = 0L;
		Long billingUnit = Long.valueOf((String) rateMap.get("billingUnit"));
		if ("0".equals(billingType)) { // A路B路分开计费
			if ("0".equals(billingModel.getCallStatus())) { // A路
				if (NumberUtils.isInternationalPhone(caller)) { // 国际电话
					callPriceB = (Long) rateMap.get("iddPrice");
					billingModel.setCallTypeB("2");
				} else {
					callTime = billingModel.getCallTime(); // 强显
					callPrice = (Long) rateMap.get("coercePrice");
					billingModel.setCallType("3");
				}
			}
			if ("0".equals(billingModel.getCallStatusB())) { // B路
				callTimeB = billingModel.getCallTimeB();
				if (NumberUtils.isInternationalPhone(callee)) { // 国际电话
					callPriceB = (Long) rateMap.get("iddPrice");
					billingModel.setCallTypeB("2");
				} else {
					if (!caller.equals(billingModel.getCalledDisplay())) { // 强显
						callPriceB = (Long) rateMap.get("coercePrice");
						billingModel.setCallTypeB("3");
					} else { // 透传
						callPriceB = (Long) rateMap.get("lucencyPrice");
						billingModel.setCallTypeB("4");
					}
				}
			}
		} else if ("2".equals(billingType)) {// 按B路时长扣费
			if ("0".equals(billingModel.getCallStatusB())) { // B路
				if ("0".equals(billingModel.getCallStatusB())) { // A路
					callTime = billingModel.getCallTimeB();
					if (NumberUtils.isInternationalPhone(caller)) { // 国际电话
						callPrice = (Long) rateMap.get("iddPrice");
						billingModel.setCallType("2");
					} else {
						callTime = billingModel.getCallTime(); // 强显
						callPrice = (Long) rateMap.get("coercePrice");
						billingModel.setCallType("3");
					}

					callTimeB = billingModel.getCallTimeB();
					if (NumberUtils.isInternationalPhone(callee)) { // 国际电话
						callPriceB = (Long) rateMap.get("iddPrice");
						billingModel.setCallTypeB("2");
					} else {
						if (!caller.equals(billingModel.getCalledDisplay())) { // 强显
							callPriceB = (Long) rateMap.get("coercePrice");
							billingModel.setCallTypeB("3");
						} else { // 透传
							callPriceB = (Long) rateMap.get("lucencyPrice");
							billingModel.setCallTypeB("4");
						}
					}
				}
			}
		}
		callTime = callTime / 1000;
		callTimeB = callTimeB / 1000;
		deductionUnit = (callTime + billingUnit - 1) / billingUnit;
		deductionUnitB = (callTimeB + billingUnit - 1) / billingUnit;

		Long recordPrice = 0L, recordPayMoney = 0L;
		if ("0".equals(billingModel.getRecordType())) {
			recordPrice = (Long) rateMap.get("recordPrice");
			recordPayMoney = recordPrice * deductionUnitB;
		}

		Long gratisUnit = 0L;
		if (flag) {
			Map<String, Object> sqlParams = new HashMap<String, Object>();
			sqlParams.put("phoneNumber", selectPhoneNumber);
			sqlParams.put("productType", billingModel.getProductType());
			sqlParams.put("userId", billingModel.getUserId());
			gratisUnit = dao.selectOne("common.getNumberReSidueUnit", sqlParams);
		}
		String cdrType = "1";
		if (gratisUnit >= deductionUnit) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", deductionUnit);
			rateParams.put("phoneNumber", selectPhoneNumber);
			rateParams.put("productType", billingModel.getProductType());
			rateParams.put("userId", billingModel.getUserId());
			dao.update("common.updateRateDeductionUnit", deductionUnit);
			cdrType = "0";
			deductionUnit = 0L;
		} else if (gratisUnit > 0 && gratisUnit < deductionUnit) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", gratisUnit);
			rateParams.put("phoneNumber", selectPhoneNumber);
			rateParams.put("productType", billingModel.getProductType());
			rateParams.put("userId", billingModel.getUserId());
			dao.update("common.updateRateDeductionUnit", deductionUnit);
			cdrType = "0";
			deductionUnit = deductionUnit - gratisUnit;
		}

		String cdrTypeB = "1";
		if (gratisUnit >= deductionUnitB) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", gratisUnit);
			rateParams.put("phoneNumber", selectPhoneNumber);
			rateParams.put("productType", billingModel.getProductType());
			rateParams.put("userId", billingModel.getUserId());
			dao.update("common.updateRateDeductionUnit", deductionUnitB);
			cdrTypeB = "0";
			deductionUnitB = 0L;
		} else if (gratisUnit > 0 && gratisUnit < deductionUnitB) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", gratisUnit);
			rateParams.put("phoneNumber", selectPhoneNumber);
			rateParams.put("productType", billingModel.getProductType());
			rateParams.put("userId", billingModel.getUserId());
			dao.update("common.updateRateDeductionUnit", deductionUnitB);
			cdrTypeB = "0";
			deductionUnitB = deductionUnitB - gratisUnit;
		}

		Long callPayMoney = callPrice * deductionUnit + callPriceB * deductionUnitB;
		payMoney = recordPayMoney + callPayMoney;

		// 话单入库
		Map<String, Object> bill = new HashMap<String, Object>();
		bill.put("userId", userId);
		bill.put("productType", productType);
		bill.put("callId", billingModel.getCallID());
		bill.put("caller", billingModel.getCaller());
		bill.put("callerCityCode", callerCity);
		bill.put("callerProvider", "");
		bill.put("callerDisplay", billingModel.getCallerDisplay());
		bill.put("called", billingModel.getCalled());
		bill.put("calledCityCode", calledCity);
		bill.put("calledProvider", "");
		bill.put("calledDisplay", billingModel.getCalledDisplay());
		bill.put("realityNumber", billingModel.getRealityNumber());
		bill.put("recordType", billingModel.getRecordType());
		bill.put("callStatus", billingModel.getCallStatus());
		bill.put("beginTime", billingModel.getBeginTime());
		bill.put("endTime", billingModel.getEndTime());
		bill.put("callType", billingModel.getCallType());
		bill.put("callTime", callTime);
		bill.put("rateId", rateMap.get("id"));
		bill.put("billingType", rateMap.get("billingType"));
		bill.put("billingUnit", rateMap.get("billingUnit"));
		bill.put("callPrice", callPrice); // 费率
		bill.put("deductionUnit", deductionUnit); // 计费单元
		bill.put("cdrType", cdrType); // 扣费类型，0：套餐内，1：套餐外
		bill.put("recordPrice", recordPrice); // 录音费率
		bill.put("recordUrl", billingModel.getRecordUrl()); // 录音地址
		bill.put("callStatusB", billingModel.getCallStatusB());
		bill.put("beginTimeB", billingModel.getBeginTimeB());
		bill.put("callTypeB", billingModel.getCallTypeB());
		bill.put("beginTimeB", billingModel.getBeginTimeB());
		bill.put("endTimeB", billingModel.getEndTimeB());
		bill.put("callTypeB", billingModel.getCallTypeB());
		bill.put("callTimeB", callTimeB);
		bill.put("callPriceB", callPriceB); // 费率
		bill.put("deductionUnitB", deductionUnitB); // 计费单元
		bill.put("cdrTypeB", cdrTypeB); // 扣费类型，0：套餐内，1：套餐外
		bill.put("callPayMoney", callPayMoney);// 通话费用
		bill.put("recordPayMoney", recordPayMoney);// 录音费用
		bill.put("payMoney", payMoney);// 总费用
		bill.put("deductionStatus", "1");

		dao.insert("common.insertBill", bill);
		// 设置响应值
		response.getOtherMap().putAll(JsonUtil.jsonStrToMap(JsonUtil.toJsonStr(billingModel)));
		response.getOtherMap().putAll(bill);
	}
}
