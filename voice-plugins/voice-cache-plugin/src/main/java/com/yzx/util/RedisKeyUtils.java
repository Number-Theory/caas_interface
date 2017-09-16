package com.yzx.util;

public class RedisKeyUtils {
	
	public static final String KEY_WHITE_LIST = "auth_wl:";
	public static final String KEY_ACCOUNT = "auth_main:";
	public static final String KEY_APP = "auth_app:";
	public static final String KEY_BLACK_MOBILE = "auth_mobile:";
	public static final String KEY_BALANCE = "auth_balance:";
	public static final String KEY_REST_CALLID = "rest_callId:";//语音通知,语音验证码业务订单Key
	public static final String KEY_APP_BALANCE = "auth_app_balance:";
	public static final String KEY_RATE_LIMIT = "auth_rl:";

	
	public static String getKey(String prefix, String key) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(prefix).append(key);
		return stringBuffer.toString();
	}
}
