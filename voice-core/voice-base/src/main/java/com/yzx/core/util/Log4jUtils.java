package com.yzx.core.util;

import org.apache.logging.log4j.ThreadContext;

/**
 * 日志输出工具
 * 
 * @author xupiao 2016年9月18日
 *
 */
public class Log4jUtils {
	
	/**
	 * 初始化日志上下文
	 * 
	 * @param token
	 */
	public static void initLog4jContext(String token) {
		clearLog4jContext();
		ThreadContext.put("token", token);
	}

	/**
	 * 清除日志上下文
	 */
	public static void clearLog4jContext() {
		ThreadContext.clearAll();
	}
	
	public static String getLogId() {
		return ThreadContext.get("token");
	}
}
