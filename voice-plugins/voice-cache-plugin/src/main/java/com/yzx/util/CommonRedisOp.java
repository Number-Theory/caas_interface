package com.yzx.util;

import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.cache.GlobalCache;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.db.dao.BaseDao;
import com.yzx.redis.RedisOpClient;

/**
 * 
 * @author xupiao 2017年6月14日
 *
 */
public class CommonRedisOp {

	private static final Logger logger = LogManager.getLogger(CommonRedisOp.class);
	
	private static final Integer EXPIRE = ConfigUtils.getProperty("cache.redis.过期时间", 7200, Integer.class);

	public static String getOneInfoString(BaseDao baseDao, String cacheKey, String sqlCode, final String redisKey,
			Object parameter) {
		String rst = GlobalCache.getString(cacheKey);
		if (StringUtil.isNotEmpty(rst)) {
			logger.info("【获取cache缓存】sqlCode={},key={},value={}", sqlCode, cacheKey, rst);
			return rst;
		}
		final Result<String> result = new Result<String>();
		final long timered = new Date().getTime();
		// RedisOpClient.get(
		// redisKey,
		// x -> {
		// if (x.succeeded()) {
		// result.setResult(x.result());
		// logger.info("【获取redis缓存】sqlCode={}访问redis时长time={},key={},value={}",
		// sqlCode,
		// (new Date().getTime() - timered), redisKey, x.result());
		// }
		// });
		result.setResult(RedisOpClient.get(redisKey));
		logger.info("【获取redis缓存】sqlCode={}访问redis时长time={},key={},value={}", sqlCode, (new Date().getTime() - timered),
				redisKey, result.result);

		if (StringUtil.isBlank(result.getResult())) {
			long timesjk = new Date().getTime();
			Map<String, Object> valueDB = baseDao.selectOne(sqlCode, parameter);
			logger.info("【无缓存,直接访问数据库】sqlCode={}访问数据库时长time={},key={},valueDB={}", sqlCode,
					(new Date().getTime() - timesjk), redisKey, valueDB);

			if (valueDB == null) {
				result.setResult("");
			} else {
				result.setResult(JsonUtil.toJsonStr(valueDB));
				RedisOpClient.setAndExpire(redisKey, result.result, EXPIRE);
			}
			// RedisOpClient.set(redisKey, result.getResult(), x -> {
			// });
		}
		return result.getResult();
	}

	public static Map<String, String> getHashMap(BaseDao baseDao, String cacheKey, String sqlCode,
			final String redisKey, Object parameter) {
		Map<String, String> rst = GlobalCache.getHashMap(cacheKey);
		if (rst != null) {
			logger.info("【获取cache缓存】sqlCode={},key={},value={}", sqlCode, cacheKey, rst);
			return rst;
		}
		final long timered = new Date().getTime();
		final Result<Map<String, String>> result = new Result<Map<String, String>>();
		// RedisOpClient.hgetall(
		// redisKey,
		// x -> {
		// if (x.succeeded()) {
		// result.setResult(x.result().getMap());
		// logger.info("【获取redis缓存】sqlCode={}访问redis时长time={},key={},value={}",
		// sqlCode,
		// (new Date().getTime() - timered), redisKey, x.result());
		// }
		// });
		result.setResult(RedisOpClient.hgetall(redisKey));
		logger.info("【获取redis缓存】sqlCode={}访问redis时长time={},key={},value={}", sqlCode, (new Date().getTime() - timered),
				redisKey, result.result);
		if (result.result == null || result.result.isEmpty()) {
			long timesjk = new Date().getTime();
			Map<String, String> valueDB = baseDao.selectOne(sqlCode, parameter);
			logger.info("【无缓存,直接访问数据库】sqlCode={}访问数据库时长time={},key={},valueDB={}", sqlCode,
					(new Date().getTime() - timesjk), redisKey, valueDB);

			if (valueDB != null) {
				result.setResult(valueDB);
				RedisOpClient.hmset(redisKey, result.result, EXPIRE);
			}
			// RedisOpClient.hmset(redisKey, new JsonObject(result.getResult()),
			// x -> {
			// });
		}
		return result.getResult();
	}

	public static Map<String, String> getHashMapCacheOnly(String cacheKey) {
		Map<String, String> rst = GlobalCache.getHashMap(cacheKey);
		logger.info("【获取cache缓存】key={},value={}", cacheKey, rst);
		return rst;
	}

	public static String getOneInfoStringCacheOnly(String cacheKey) {
		String rst = GlobalCache.getString(cacheKey);
		logger.info("【获取cache缓存】key={},value={}", cacheKey, rst);
		return rst;
	}

	static class Result<T> {
		private T result;

		public T getResult() {
			return result;
		}

		public void setResult(T result) {
			this.result = result;
		}
	}
}
