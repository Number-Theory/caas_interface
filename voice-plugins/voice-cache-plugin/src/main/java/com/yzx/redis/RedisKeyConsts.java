package com.yzx.redis;

/**
 * 
 * @author xupiao 2017年8月24日
 *
 */
public class RedisKeyConsts {
	
	public final static String AXNUMBINDS = "AXNUMBINDS:";
	
	public final static String AXBNUMBINDS = "AXBNUMBINDS:";
	
	public final static String VOICE_CODE_SESSION = "VCSESSION:";
	
	public final static String VOICE_NOTIFY_SESSION = "VNSESSION";
	
	public final static String ORDERBINDS = "ORDERBINDS:";
	
	public static String getKey(String... args) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String key : args) {
			stringBuffer.append(key);
		}
		return stringBuffer.toString();
	}
}
