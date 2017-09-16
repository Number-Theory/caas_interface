package com.yzx.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.auth.plugin.SpringContext;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.JsonUtil;
import com.yzx.db.dao.BaseDao;

/**
 * 
 * @author xupiao 2017年6月15日
 *
 */
public class GlobalCache {
	private static Logger logger = LogManager.getLogger(GlobalCache.class);
	private static Map<String, Object> cache = new HashMap<String, Object>();

	public static void load() {
		logger.info("开始加载缓存");
		cache = _load();
		logger.info("缓存加载完成[{}]", cache.size());
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> _load() {
		Map<String, Object> temp = new HashMap<String, Object>();
		List<Map<String, Object>> list = ConfigUtils.getProperty("cache.db", List.class);
		if (list != null && list.size() > 0) {
			list.forEach(map -> {
				String sql = (String) map.get("sql");
				String primary = (String) map.get("primary");
				String dao = (String) map.get("dao");
				BaseDao cacheDao = SpringContext.getInstance(dao);
				List<Map<String, String>> entityList = cacheDao.selectList(sql);
				entityList.forEach(entity -> {
					String[] primaryList = primary.split(",");
					StringBuffer primaryValue = new StringBuffer(sql);
					for (String p : primaryList) {
						if (entity.containsKey(p)) {
							primaryValue.append(entity.get(p));
						}
					}
					temp.put(primaryValue.toString(), entity);
				});
			});
		}
		return temp;
	}

	public static String getString(String key) {
		return JsonUtil.toJsonStr(cache.get(key));
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getHashMap(String key) {
		return (Map<String, String>) cache.get(key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getEntity(String key) {
		return (T) cache.get(key);
	}

	public static void setCache(String key, Object value) {
		cache.put(key, value);
	}

	/**
	 * 
	 * @param args
	 *            tableName, primaryKey1, primaryKey2 ...
	 * @return
	 */
	public static String getCacheKey(String... args) {
		StringBuffer stringBuffer = new StringBuffer();
		for (String key : args) {
			stringBuffer.append(key);
		}
		return stringBuffer.toString();
	}
}
