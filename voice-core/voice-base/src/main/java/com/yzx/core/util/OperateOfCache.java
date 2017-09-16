package com.yzx.core.util;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 
 * @author xupiao 2017年6月5日
 *
 */
public class OperateOfCache {

	private static ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

	public static interface CacheFunction<T> {
		/**
		 *  要确保缓存方法的唯一性
		 */
		String getKey();

		T call();
	}
	@SuppressWarnings("unchecked")
	public static <T> T execute(CacheFunction<T> f) {
		String key = f.getKey();
		T ret = (T) cache.get(key);
		if (ret != null)
			return ("null".equals(ret) ? null : ret);

		ret = f.call();
		if (ret == null)
			cache.put(key, "null");
		else
			cache.put(key, ret);
		return ret;
	}
}
