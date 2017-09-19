package com.caas.util;

import java.util.HashMap;
import java.util.Map;

import com.caas.dao.CaasDao;
import com.yzx.auth.plugin.SpringContext;
import com.yzx.core.util.StringUtil;

/**
 * 
 * @author xupiao 2017年8月18日
 *
 */
public class NumberUtils {

	private static CaasDao dao = SpringContext.getInstance(CaasDao.class);

	/**
	 * 判断是否是国籍号码
	 * 
	 * @param phoneNumber
	 *            号码
	 * @return
	 */
	public static Boolean isInternationalPhone(String phoneNumber) { // 判断是否是国际
		return phoneNumber.startsWith("00") && !phoneNumber.startsWith("0086");
	}

	/**
	 * 获取号码归属地
	 * 
	 * @param mobile
	 * @return
	 */
	public static String getMobileAttribution(String mobile) {
		if (mobile.startsWith("12599")) {
			String cityId = dao.selectOne("common.getNumberCityId", mobile);
			if (StringUtil.isNotEmpty(cityId)) {
				return cityId;
			}
			return "0";
		}
		if (CommonUtils.getPhoneType(mobile) == CommonUtils.FIXED_PHONE) { // 固话
			String cityId = CommonUtils.getPhoneCityId(mobile);
			return cityId;
		} else { // 0086 + 查表结果
			Map<String, Object> numInfomationMap = getNumInfomation(mobile);
			if (numInfomationMap != null && !numInfomationMap.isEmpty()) {
				return "0086" + numInfomationMap.get("area");
			} else {
				return "0";
			}
		}
	}

	private static Map<String, Object> getNumInfomation(String mobile) {
		Map<String, Object> sqlParams = new HashMap<String, Object>();
		sqlParams.put("mobile", mobile.substring(0, 7));
		return dao.selectOne("common.getNumInfomation", sqlParams);
	}
}
