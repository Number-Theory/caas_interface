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
 * 隐号计费、入库
 * 
 * @author xupiao 2017年8月17日
 *
 */
public class SafetyCallHandler extends DefaultBillingHandler {

	private static final Logger logger = LogManager.getLogger(SafetyCallHandler.class);

	private CaasDao dao = SpringContext.getInstance(CaasDao.class);

	@Override
	public void handler(BillingModel billingModel, ServiceResponse response) {
		String userId = billingModel.getUserId();
		String productType = billingModel.getProductType();
		String caller = billingModel.getCaller();
		String callee = billingModel.getCalled();

		// 根据号码获取费率
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("phoneNumber", callee);
		params.put("productType", productType);
		params.put("userId", billingModel.getUserId());
		Map<String, Object> rateMap = dao.selectOne("common.getNumberRate", params);
		boolean flag = true;
		if (rateMap == null || rateMap.isEmpty()) {
			logger.info("获取号码套餐失败，查询默认套餐！");
			params.put("phoneNumber", "0");
			rateMap = dao.selectOne("common.getNumberRate", params);
			flag = false;
		}
		logger.info("查询到的套餐为[{}]", rateMap);

		// 计费
		String billingType = (String) rateMap.get("billingType");
		Long callTime = 0L, callTimeB = 0L, payMoney = 0L;
		String callerCity = NumberUtils.getMobileAttribution(caller), calledCity = NumberUtils.getMobileAttribution(billingModel.getCalled());
		String callerCityB = NumberUtils.getMobileAttribution(billingModel.getCalled()), calledCityB = NumberUtils.getMobileAttribution(billingModel
				.getRealityNumber());
		Long callPrice = 0L, callPriceB = 0L, deductionUnit = 0L, deductionUnitB = 0L;
		Long billingUnit = Long.valueOf((String) rateMap.get("billingUnit"));
		if ("0".equals(billingType)) { // A路B路分开计费
			if ("0".equals(billingModel.getCallStatus())) { // A路
				callTime = billingModel.getCallTime();
				if (NumberUtils.isInternationalPhone(billingModel.getCaller())) { // 国际电话
					callPrice = (Long) rateMap.get("iddPrice");
					billingModel.setCallType("2");
				} else {
					if (callerCity.equals(calledCity)) { // 市话
						callPrice = (Long) rateMap.get("localPrice");
						billingModel.setCallType("0");
					} else { // 长途
						callPrice = (Long) rateMap.get("dddPrice");
						billingModel.setCallType("1");
					}
				}
			}
			if ("0".equals(billingModel.getCallStatusB())) { // B路
				callTimeB = billingModel.getCallTimeB();
				if (NumberUtils.isInternationalPhone(billingModel.getRealityNumber())) { // 国际电话
					callPriceB = (Long) rateMap.get("iddPrice");
					billingModel.setCallTypeB("2");
				} else {
					if (callerCityB.equals(calledCityB)) { // 市话
						callPriceB = (Long) rateMap.get("localPrice");
						billingModel.setCallTypeB("0");
					} else { // 长途
						callPriceB = (Long) rateMap.get("dddPrice");
						billingModel.setCallTypeB("1");
					}
				}
			}
		} else if ("1".equals(billingType)) {// 按次扣费
			callTime = billingModel.getCallTime();
			callPrice = (Long) rateMap.get("oncePrice");
		} else if ("2".equals(billingType)) {// 按B路时长扣费
			if ("0".equals(billingModel.getCallStatusB())) { // B路
				callTime = billingModel.getCallTime();
				if ("0".equals(billingModel.getCallStatusB())) { // A路
					callTime = billingModel.getCallTimeB();
					if (NumberUtils.isInternationalPhone(billingModel.getCaller())) { // 国际电话
						callPrice = (Long) rateMap.get("iddPrice");
						billingModel.setCallType("2");
					} else {
						if (callerCity.equals(calledCity)) { // 市话
							callPrice = (Long) rateMap.get("localPrice");
							billingModel.setCallType("0");
						} else { // 长途
							callPrice = (Long) rateMap.get("dddPrice");
							billingModel.setCallType("1");
						}
					}

					callTimeB = billingModel.getCallTimeB();
					if (NumberUtils.isInternationalPhone(billingModel.getRealityNumber())) { // 国际电话
						callPriceB = (Long) rateMap.get("iddPrice");
						billingModel.setCallTypeB("2");
					} else {
						if (callerCityB.equals(calledCityB)) { // 市话
							callPriceB = (Long) rateMap.get("localPrice");
							billingModel.setCallTypeB("0");
						} else { // 长途
							callPriceB = (Long) rateMap.get("dddPrice");
							billingModel.setCallTypeB("1");
						}
					}
				}
			}
		}
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
			sqlParams.put("phoneNumber", callee);
			sqlParams.put("productType", billingModel.getProductType());
			sqlParams.put("userId", billingModel.getUserId());
			gratisUnit = dao.selectOne("common.getNumberReSidueUnit", sqlParams);
		}
		String cdrType = "1";
		if (gratisUnit >= deductionUnit) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", deductionUnit);
			rateParams.put("rateId", rateMap.get("id"));
			rateParams.put("productType", billingModel.getProductType());
			rateParams.put("userId", billingModel.getUserId());
			dao.update("common.updateRateDeductionUnit", deductionUnit);
			cdrType = "0";
			deductionUnit = 0L;
		} else if (gratisUnit > 0 && gratisUnit < deductionUnit) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", gratisUnit);
			rateParams.put("rateId", rateMap.get("id"));
			rateParams.put("productType", billingModel.getProductType());
			rateParams.put("userId", billingModel.getUserId());
			dao.update("common.updateRateDeductionUnit", deductionUnit);
			cdrType = "0";
			deductionUnit = deductionUnit - gratisUnit;
		}

		String cdrTypeB = "1";
		if (gratisUnit >= deductionUnitB) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", deductionUnitB);
			rateParams.put("rateId", rateMap.get("id"));
			dao.update("common.updateRateDeductionUnit", deductionUnitB);
			cdrTypeB = "0";
			deductionUnitB = 0L;
		} else if (gratisUnit > 0 && gratisUnit < deductionUnitB) {
			Map<String, Object> rateParams = new HashMap<String, Object>();
			rateParams.put("deductionUnit", gratisUnit);
			rateParams.put("rateId", rateMap.get("id"));
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
		bill.put("callTime", billingModel.getCallTime());
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
		bill.put("callTimeB", billingModel.getCallTimeB());
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
