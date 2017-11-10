package com.yzx.redis;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.StringUtil;

/**
 * 
 * @author xupiao 2017年6月7日
 *
 */
public class RedisOpClient {
	private static Logger logger = LogManager.getLogger(RedisOpClient.class);
	// private static final Vertx vertx = Vertx.vertx();
	// private static RedisClient redisClient;

	private static JedisPool jedisPool = null;
	private static JedisPoolConfig config = null;
	private static RedisInfo redisInfo;

	public static void init() {
		redisInfo = new RedisInfo();
		String servers = ConfigUtils.getProperty("redis.host", "127.0.0.1", String.class);
		String port = ConfigUtils.getProperty("redis.port", "6379", String.class);
		String password = ConfigUtils.getProperty("redis.password", "", String.class);
		;
		String maxIdle = ConfigUtils.getProperty("redis.maxIdle", "10", String.class);
		String maxActive = ConfigUtils.getProperty("redis.maxActive", "500", String.class);
		String maxWait = ConfigUtils.getProperty("redis.maxWait", "100000", String.class);
		String timeout = ConfigUtils.getProperty("redis.timeout", "2000", String.class);
		redisInfo.setIp(servers);
		String testOnBorrow = ConfigUtils.getProperty("redis.testOnBorrow", "true", String.class);
		redisInfo.setMaxActive(maxActive);
		redisInfo.setMaxIdle(maxIdle);
		redisInfo.setMaxWait(maxWait);
		redisInfo.setPort(port);
		redisInfo.setPassword(password);
		redisInfo.setTestOnBorrow(testOnBorrow);
		redisInfo.setTimeout(timeout);
		if (config == null) {
			config = new JedisPoolConfig();
			logger.info("加载Redis配置");
			config.setMaxTotal(Integer.parseInt(redisInfo.getMaxActive()));
			config.setMaxIdle(Integer.parseInt(redisInfo.getMaxIdle()));
			config.setMaxWaitMillis(Long.parseLong(redisInfo.getMaxWait()));
			config.setTestOnBorrow(Boolean.parseBoolean(redisInfo.getTestOnBorrow()));
		}
		if (jedisPool == null) {
			jedisPool = new JedisPool(config, redisInfo.getIp(), Integer.parseInt(redisInfo.getPort()), Integer.parseInt(redisInfo.getTimeout()),
					redisInfo.getPassword());
			logger.info("初始化Redis连接池");
		}
	}

	// public static void _init() {
	// String HOST = ConfigUtils.getProperty("redis.host", "127.0.0.1",
	// String.class);
	// Integer PORT = ConfigUtils.getProperty("redis.port", 6379,
	// Integer.class);
	// String PASSWORD = ConfigUtils.getProperty("redis.password", "",
	// String.class);
	//
	// RedisOptions job = new RedisOptions();
	// job.setHost(HOST).setPort(PORT).setAuth(PASSWORD).setEncoding("UTF-8");
	// redisClient = RedisClient.create(vertx, job);
	// }

	private static Jedis getJedis() {
		Jedis jedis = jedisPool.getResource();
		if (!jedis.isConnected()) {
			logger.error("redis未连接");
			try {
				logger.info("redis尝试连接");
				jedis.connect();
				logger.info("redis连接成功");
			} catch (Exception e) {
				logger.error("redis连接失败");
			}
		}
		return jedis;
	}

	// ttl
	public static Long ttl(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			long res = jedis.ttl(key);
			logger.info("ttl key " + key);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// add
	public static Long add(String key, String... value) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			long res = jedis.sadd(key, value);
			logger.info("add key " + key);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// all members
	public static Set<String> smembers(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Set<String> res = jedis.smembers(key);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// is exist
	public static boolean sismember(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			boolean res = jedis.sismember(key, value);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return false;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// numbers
	public static Long scard(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.scard(key);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// delete
	public static Long delKey(final String... key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.del(key);
			logger.info("delete key " + key);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// set a
	public static String set(String key, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String res = jedis.set(key, value);
			logger.info("add key " + key + ",value " + value);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public static synchronized String getAndSet(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String cur = jedis.get(key);
			long curNum = 0;
			if (StringUtil.isNotEmpty(cur)) {
				curNum = Long.parseLong(cur);
			}
			long nownum = curNum + 1;
			jedis.set(key, String.valueOf(nownum));
			logger.info("getAndSet key " + key);
			return String.valueOf(nownum);
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// get a
	public static String get(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String res = jedis.get(key);
			logger.info("get key " + key);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public static Map<String, String> hgetall(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Map<String, String> res = jedis.hgetAll(key);
			logger.info("hgetall key " + key);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public static String hmset(String key, Map<String, String> hash, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String res = jedis.hmset(key, hash);
			if (seconds > 0) { // 设置超时时间
				long expireNum = jedis.expire(key, seconds);
				logger.info("超时时间设置：key = " + key + ",seconds = " + seconds + ",expireNum = " + expireNum);
			}
			return res;
		} catch (Throwable e) {
			logger.error("redis的hmset方法错误, key = " + key + ",hash = " + hash + ",异常：", e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public static long hset(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			long res = jedis.hset(key, field, value);
			logger.info("hset key " + key);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return 0;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 添加有序集合
	 * 
	 * @param key
	 * @param score
	 * @param member
	 * @return
	 */
	public static Long zadd(String key, double score, String member) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.zadd(key, score, member);
			return res;
		} catch (Throwable e) {
			logger.error("zadd,error = ", e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 获取指定下标排序index范围内的有序集合(默认升序)
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> zrange(String key, long start, long end) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Set<String> set = jedis.zrange(key, start, end);
			logger.info("zrange,key= " + key + " start= " + start + " end= " + end);
			return set;
		} catch (Throwable e) {
			logger.error("zrange,error = ", e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * 根据score大小区间查找有序集合
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static Set<String> zrangeByScore(String key, String min, String max) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Set<String> set = jedis.zrangeByScore(key, min, max);

			logger.info("zrange,key= " + key + " min= " + min + " max= " + max);
			return set;
		} catch (Throwable e) {
			logger.error("zrange,error = ", e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	public static Long zrem(String key, String... members) {
		Jedis jedis = null;
		Long remNum = Long.valueOf(0);
		try {
			jedis = getJedis();
			for (String member : members) {
				Long res = jedis.zrem(key, members);
				if (res == 1) {
					remNum++;
					logger.info("zrem,key= " + key + "member= " + member);
				}
			}
			return remNum;
		} catch (Throwable e) {
			logger.error("zrem,error = ", e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// random a
	public static String srandmember(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String res = jedis.srandmember(key);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// remove a random
	public static String spop(String key) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String res = jedis.spop(key);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// remove a or more member
	public static Long srem(String key, String... members) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.srem(key, members);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// move a member from one to another
	public static Long smove(String srckey, String dstkey, String member) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.smove(srckey, dstkey, member);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// union all
	public static Set<String> sunion(String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Set<String> res = jedis.sunion(keys);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// uoion all keys store to dstkey
	public static Long sunionstore(String dstkey, String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.sunionstore(dstkey, keys);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// join inner all keys
	public static Set<String> sinter(String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Set<String> res = jedis.sinter(keys);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// inner all keys store to dstkey
	public static Long sinter(String dstkey, String... keys) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Long res = jedis.sinterstore(dstkey, keys);
			return res;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return 0l;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * @param regx
	 * @return Set<String> getKeys
	 */
	public static Set<String> getKeys(String regx) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			Set<String> set = jedis.keys(regx);
			return set;
		} catch (Exception e) {
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// set a
	public static String setAndExpire(String key, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getJedis();
			String res = jedis.setex(key, seconds, value);
			logger.info("setAndExpire key " + key + ",value " + value + ",seconds " + seconds);
			return res;
		} catch (Exception e) {
			logger.error(e);
			jedisPool.returnBrokenResource(jedis);
			return null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	// public static void close(Handler<AsyncResult<Void>> paramHandler) {
	// redisClient.close(paramHandler);
	// }
	//
	// /**
	// * Append a value to a key
	// *
	// * @param paramString1
	// * @param paramString2
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient append(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.append(paramString1, paramString2, paramHandler);
	// }
	//
	// /**
	// * Append a value to a key
	// *
	// * @param paramString
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient auth(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	// return redisClient.auth(paramString, paramHandler);
	// }
	//
	// /**
	// * Asynchronously rewrite the append-only file
	// *
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient bgrewriteaof(Handler<AsyncResult<String>>
	// paramHandler) {
	// return redisClient.bgrewriteaof(paramHandler);
	// }
	//
	// /**
	// * Asynchronously save the dataset to disk
	// *
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient bgsave(Handler<AsyncResult<String>>
	// paramHandler) {
	// return redisClient.bgsave(paramHandler);
	// }
	//
	// /**
	// * Count set bits in a string
	// *
	// * @param paramString
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient bitcount(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.bitcount(paramString, paramHandler);
	// }
	//
	// /**
	// * Count set bits in a string
	// *
	// * @param paramString
	// * @param paramLong1
	// * @param paramLong2
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient bitcountRange(String paramString, long
	// paramLong1, long paramLong2,
	// Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.bitcountRange(paramString, paramLong1, paramLong2,
	// paramHandler);
	// }
	//
	// /**
	// * Perform bitwise operations between strings
	// *
	// * @param paramBitOperation
	// * @param paramString
	// * @param paramList
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient bitop(BitOperation paramBitOperation, String
	// paramString, List<String> paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.bitop(paramBitOperation, paramString, paramList,
	// paramHandler);
	// }
	//
	// /**
	// * Find first bit set or clear in a string
	// *
	// * @param paramString
	// * @param paramInt
	// * @param paramHandler
	// * @return
	// */
	// public static RedisClient bitpos(String paramString, int paramInt,
	// Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.bitpos(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient bitposFrom(String paramString, int paramInt1,
	// int paramInt2,
	// Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.bitposFrom(paramString, paramInt1, paramInt2,
	// paramHandler);
	// }
	//
	// public static RedisClient bitposRange(String paramString, int paramInt1,
	// int paramInt2, int paramInt3,
	// Handler<AsyncResult<Long>> paramHandler) {
	// return redisClient.bitposRange(paramString, paramInt1, paramInt2,
	// paramInt3, paramHandler);
	// }
	//
	// public static RedisClient blpop(String paramString, int paramInt,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.blpop(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient blpopMany(List<String> paramList, int paramInt,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.blpopMany(paramList, paramInt, paramHandler);
	// }
	//
	// public static RedisClient brpop(String paramString, int paramInt,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.brpop(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient brpopMany(List<String> paramList, int paramInt,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.brpopMany(paramList, paramInt, paramHandler);
	// }
	//
	// public static RedisClient brpoplpush(String paramString1, String
	// paramString2, int paramInt,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.brpoplpush(paramString1, paramString2, paramInt,
	// paramHandler);
	// }
	//
	// public static RedisClient clientKill(KillFilter paramKillFilter,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.clientKill(paramKillFilter, paramHandler);
	// }
	//
	// public static RedisClient clientList(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.clientList(paramHandler);
	// }
	//
	// public static RedisClient clientGetname(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.clientGetname(paramHandler);
	// }
	//
	// public static RedisClient clientPause(long paramLong,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.clientPause(paramLong, paramHandler);
	// }
	//
	// public static RedisClient clientSetname(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.clientSetname(paramString, paramHandler);
	// }
	//
	// public static RedisClient clusterSlots(Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.clusterSlots(paramHandler);
	// }
	//
	// public static RedisClient command(Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.command(paramHandler);
	// }
	//
	// public static RedisClient commandCount(Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.commandCount(paramHandler);
	// }
	//
	// public static RedisClient commandGetkeys(Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.commandGetkeys(paramHandler);
	// }
	//
	// public static RedisClient commandInfo(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.commandInfo(paramList, paramHandler);
	// }
	//
	// public static RedisClient configGet(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.configGet(paramString, paramHandler);
	// }
	//
	// public static RedisClient configRewrite(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.configRewrite(paramHandler);
	// }
	//
	// public static RedisClient configSet(String paramString1, String
	// paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.configSet(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient configResetstat(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.configResetstat(paramHandler);
	// }
	//
	// public static RedisClient dbsize(Handler<AsyncResult<Long>> paramHandler)
	// {
	//
	// return redisClient.dbsize(paramHandler);
	// }
	//
	// public static RedisClient debugObject(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.debugObject(paramString, paramHandler);
	// }
	//
	// public static RedisClient debugSegfault(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.debugSegfault(paramHandler);
	// }
	//
	// public static RedisClient decr(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.decr(paramString, paramHandler);
	// }
	//
	// public static RedisClient decrby(String paramString, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.decrby(paramString, paramLong, paramHandler);
	// }
	//
	// public static RedisClient del(String paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.del(paramList, paramHandler);
	// }
	//
	// public static RedisClient dump(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.dump(paramString, paramHandler);
	// }
	//
	// public static RedisClient echo(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.echo(paramString, paramHandler);
	// }
	//
	// public static RedisClient eval(String paramString, List<String>
	// paramList1, List<String> paramList2,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.eval(paramString, paramList1, paramList2,
	// paramHandler);
	// }
	//
	// public static RedisClient evalsha(String paramString, List<String>
	// paramList1, List<String> paramList2,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.evalsha(paramString, paramList1, paramList2,
	// paramHandler);
	// }
	//
	// public static RedisClient exists(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.exists(paramString, paramHandler);
	// }
	//
	// public static RedisClient expire(String paramString, int paramInt,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.expire(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient expireat(String paramString, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.expireat(paramString, paramLong, paramHandler);
	// }
	//
	// public static RedisClient flushall(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.flushall(paramHandler);
	// }
	//
	// public static RedisClient flushdb(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.flushdb(paramHandler);
	// }
	//
	// public static RedisClient get(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.get(paramString, paramHandler);
	// }
	//
	// public static RedisClient getBinary(String paramString,
	// Handler<AsyncResult<Buffer>> paramHandler) {
	//
	// return redisClient.getBinary(paramString, paramHandler);
	// }
	//
	// public static RedisClient getbit(String paramString, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.getbit(paramString, paramLong, paramHandler);
	// }
	//
	// public static RedisClient getrange(String paramString, long paramLong1,
	// long paramLong2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.getrange(paramString, paramLong1, paramLong2,
	// paramHandler);
	// }
	//
	// public static RedisClient getset(String paramString1, String
	// paramString2, Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.getset(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient hdel(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hdel(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient hdelMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hdelMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient hexists(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hexists(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient hget(String paramString1, String paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.hget(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient hgetall(String paramString,
	// Handler<AsyncResult<JsonObject>> paramHandler) {
	//
	// return redisClient.hgetall(paramString, paramHandler);
	// }
	//
	// public static RedisClient hincrby(String paramString1, String
	// paramString2, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hincrby(paramString1, paramString2, paramLong,
	// paramHandler);
	// }
	//
	// public static RedisClient hincrbyfloat(String paramString1, String
	// paramString2, double paramDouble,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.hincrbyfloat(paramString1, paramString2, paramDouble,
	// paramHandler);
	// }
	//
	// public static RedisClient hkeys(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.hkeys(paramString, paramHandler);
	// }
	//
	// public static RedisClient hlen(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hlen(paramString, paramHandler);
	// }
	//
	// public static RedisClient hmget(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.hmget(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient hmset(String paramString, JsonObject paramMap,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.hmset(paramString, paramMap, paramHandler);
	// }
	//
	// public static RedisClient hset(String paramString1, String paramString2,
	// String paramString3,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hset(paramString1, paramString2, paramString3,
	// paramHandler);
	// }
	//
	// public static RedisClient hsetnx(String paramString1, String
	// paramString2, String paramString3,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.hsetnx(paramString1, paramString2, paramString3,
	// paramHandler);
	// }
	//
	// public static RedisClient hvals(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.hvals(paramString, paramHandler);
	// }
	//
	// public static RedisClient incr(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.incr(paramString, paramHandler);
	// }
	//
	// public static RedisClient incrby(String paramString, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.incrby(paramString, paramLong, paramHandler);
	// }
	//
	// public static RedisClient incrbyfloat(String paramString, double
	// paramDouble,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.incrbyfloat(paramString, paramDouble, paramHandler);
	// }
	//
	// public static RedisClient info(Handler<AsyncResult<JsonObject>>
	// paramHandler) {
	//
	// return redisClient.info(paramHandler);
	// }
	//
	// public static RedisClient infoSection(String paramString,
	// Handler<AsyncResult<JsonObject>> paramHandler) {
	//
	// return redisClient.infoSection(paramString, paramHandler);
	// }
	//
	// public static RedisClient keys(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.keys(paramString, paramHandler);
	// }
	//
	// public static RedisClient lastsave(Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.lastsave(paramHandler);
	// }
	//
	// public static RedisClient lindex(String paramString, int paramInt,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.lindex(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient linsert(String paramString1, InsertOptions
	// paramInsertOptions, String paramString2,
	// String paramString3, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.linsert(paramString1, paramInsertOptions,
	// paramString2, paramString3, paramHandler);
	// }
	//
	// public static RedisClient llen(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.llen(paramString, paramHandler);
	// }
	//
	// public static RedisClient lpop(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.lpop(paramString, paramHandler);
	// }
	//
	// public static RedisClient lpushMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.lpushMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient lpush(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.lpush(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient lpushx(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.lpushx(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient lrange(String paramString, long paramLong1,
	// long paramLong2,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.lrange(paramString, paramLong1, paramLong2,
	// paramHandler);
	// }
	//
	// public static RedisClient lrem(String paramString1, long paramLong,
	// String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.lrem(paramString1, paramLong, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient lset(String paramString1, long paramLong,
	// String paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.lset(paramString1, paramLong, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient ltrim(String paramString, long paramLong1, long
	// paramLong2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.ltrim(paramString, paramLong1, paramLong2,
	// paramHandler);
	// }
	//
	// public static RedisClient mget(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.mget(paramString, paramHandler);
	// }
	//
	// public static RedisClient mgetMany(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.mgetMany(paramList, paramHandler);
	// }
	//
	// public static RedisClient migrate(String paramString1, int paramInt1,
	// String paramString2, int paramInt2,
	// long paramLong, MigrateOptions paramMigrateOptions,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.migrate(paramString1, paramInt1, paramString2,
	// paramInt2, paramLong, paramMigrateOptions,
	// paramHandler);
	// }
	//
	// public static RedisClient monitor(Handler<AsyncResult<Void>>
	// paramHandler) {
	//
	// return redisClient.monitor(paramHandler);
	// }
	//
	// public static RedisClient move(String paramString, int paramInt,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.move(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient mset(JsonObject paramMap,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.mset(paramMap, paramHandler);
	// }
	//
	// public static RedisClient msetnx(JsonObject paramMap,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.msetnx(paramMap, paramHandler);
	// }
	//
	// public static RedisClient object(String paramString, ObjectCmd
	// paramObjectCmd,
	// Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.object(paramString, paramObjectCmd, paramHandler);
	// }
	//
	// public static RedisClient persist(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.persist(paramString, paramHandler);
	// }
	//
	// public static RedisClient pexpire(String paramString, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pexpire(paramString, paramLong, paramHandler);
	// }
	//
	// public static RedisClient pexpireat(String paramString, long paramLong,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pexpireat(paramString, paramLong, paramHandler);
	// }
	//
	// public static RedisClient pfadd(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pfadd(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient pfaddMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pfaddMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient pfcount(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pfcount(paramString, paramHandler);
	// }
	//
	// public static RedisClient pfcountMany(List<String> paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pfcountMany(paramList, paramHandler);
	// }
	//
	// public static RedisClient pfmerge(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.pfmerge(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient ping(Handler<AsyncResult<String>> paramHandler)
	// {
	//
	// return redisClient.ping(paramHandler);
	// }
	//
	// public static RedisClient psetex(String paramString1, long paramLong,
	// String paramString2,
	// Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.psetex(paramString1, paramLong, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient psubscribe(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.psubscribe(paramString, paramHandler);
	// }
	//
	// public static RedisClient psubscribeMany(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.psubscribeMany(paramList, paramHandler);
	// }
	//
	// public static RedisClient pubsubChannels(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.pubsubChannels(paramString, paramHandler);
	// }
	//
	// public static RedisClient pubsubNumsub(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.pubsubNumsub(paramList, paramHandler);
	// }
	//
	// public static RedisClient pubsubNumpat(Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.pubsubNumpat(paramHandler);
	// }
	//
	// public static RedisClient pttl(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.pttl(paramString, paramHandler);
	// }
	//
	// public static RedisClient publish(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.publish(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient punsubscribe(List<String> paramList,
	// Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.punsubscribe(paramList, paramHandler);
	// }
	//
	// public static RedisClient randomkey(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.randomkey(paramHandler);
	// }
	//
	// public static RedisClient rename(String paramString1, String
	// paramString2, Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.rename(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient renamenx(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.renamenx(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient restore(String paramString1, long paramLong,
	// String paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.restore(paramString1, paramLong, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient role(Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.role(paramHandler);
	// }
	//
	// public static RedisClient rpop(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.rpop(paramString, paramHandler);
	// }
	//
	// public static RedisClient rpoplpush(String paramString1, String
	// paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.rpoplpush(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient rpushMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.rpushMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient rpush(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.rpush(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient rpushx(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.rpushx(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient sadd(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.sadd(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient saddMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.saddMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient save(Handler<AsyncResult<String>> paramHandler)
	// {
	//
	// return redisClient.save(paramHandler);
	// }
	//
	// public static RedisClient scard(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.scard(paramString, paramHandler);
	// }
	//
	// public static RedisClient scriptExists(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.scriptExists(paramString, paramHandler);
	// }
	//
	// public static RedisClient scriptExistsMany(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.scriptExistsMany(paramList, paramHandler);
	// }
	//
	// public static RedisClient scriptFlush(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.scriptFlush(paramHandler);
	// }
	//
	// public static RedisClient scriptKill(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.scriptKill(paramHandler);
	// }
	//
	// public static RedisClient scriptLoad(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.scriptLoad(paramString, paramHandler);
	// }
	//
	// public static RedisClient sdiff(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.sdiff(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient sdiffstore(String paramString1, String
	// paramString2, List<String> paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.sdiffstore(paramString1, paramString2, paramList,
	// paramHandler);
	// }
	//
	// public static RedisClient select(int paramInt,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.select(paramInt, paramHandler);
	// }
	//
	// public static RedisClient set(String paramString1, String paramString2,
	// Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.set(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient setWithOptions(String paramString1, String
	// paramString2, SetOptions paramSetOptions,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.setWithOptions(paramString1, paramString2,
	// paramSetOptions, paramHandler);
	// }
	//
	// public static RedisClient setBinary(String paramString1, Buffer
	// paramString2,
	// Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.setBinary(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient setbit(String paramString, long paramLong, int
	// paramInt,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.setbit(paramString, paramLong, paramInt,
	// paramHandler);
	// }
	//
	// public static RedisClient setex(String paramString1, long paramLong,
	// String paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.setex(paramString1, paramLong, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient setnx(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.setnx(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient setrange(String paramString1, int paramInt,
	// String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.setrange(paramString1, paramInt, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient sinter(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.sinter(paramList, paramHandler);
	// }
	//
	// public static RedisClient sinterstore(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.sinterstore(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient sismember(String paramString1, String
	// paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.sismember(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient slaveof(String paramString, int paramInt,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.slaveof(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient slaveofNoone(Handler<AsyncResult<String>>
	// paramHandler) {
	//
	// return redisClient.slaveofNoone(paramHandler);
	// }
	//
	// public static RedisClient slowlogGet(int paramInt,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.slowlogGet(paramInt, paramHandler);
	// }
	//
	// public static RedisClient slowlogLen(Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.slowlogLen(paramHandler);
	// }
	//
	// public static RedisClient slowlogReset(Handler<AsyncResult<Void>>
	// paramHandler) {
	//
	// return redisClient.slowlogReset(paramHandler);
	// }
	//
	// public static RedisClient smembers(String paramString,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.smembers(paramString, paramHandler);
	// }
	//
	// public static RedisClient smove(String paramString1, String paramString2,
	// String paramString3,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.smove(paramString1, paramString2, paramString3,
	// paramHandler);
	// }
	//
	// public static RedisClient sort(String paramString, SortOptions
	// paramSortOptions,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.sort(paramString, paramSortOptions, paramHandler);
	// }
	//
	// public static RedisClient spop(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.spop(paramString, paramHandler);
	// }
	//
	// public static RedisClient spopMany(String paramString, int paramInt,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.spopMany(paramString, paramInt, paramHandler);
	// }
	//
	// public static RedisClient srandmember(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.srandmember(paramString, paramHandler);
	// }
	//
	// public static RedisClient srem(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.srem(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient sremMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.sremMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient strlen(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.strlen(paramString, paramHandler);
	// }
	//
	// public static RedisClient subscribe(String paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.subscribe(paramList, paramHandler);
	// }
	//
	// public static RedisClient sunion(List<String> paramList,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.sunion(paramList, paramHandler);
	// }
	//
	// public static RedisClient sunionstore(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.sunionstore(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient sync(Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.sync(paramHandler);
	// }
	//
	// public static RedisClient time(Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.time(paramHandler);
	// }
	//
	// public static RedisClient ttl(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.ttl(paramString, paramHandler);
	// }
	//
	// public static RedisClient type(String paramString,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.type(paramString, paramHandler);
	// }
	//
	// public static RedisClient unsubscribe(List<String> paramList,
	// Handler<AsyncResult<Void>> paramHandler) {
	//
	// return redisClient.unsubscribe(paramList, paramHandler);
	// }
	//
	// public static RedisClient zadd(String paramString1, double paramDouble,
	// String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zadd(paramString1, paramDouble, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient zaddMany(String paramString, Map<String,
	// Double> paramMap,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zaddMany(paramString, paramMap, paramHandler);
	// }
	//
	// public static RedisClient zcard(String paramString,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zcard(paramString, paramHandler);
	// }
	//
	// public static RedisClient zcount(String paramString, double paramDouble1,
	// double paramDouble2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zcount(paramString, paramDouble1, paramDouble2,
	// paramHandler);
	// }
	//
	// public static RedisClient zincrby(String paramString1, double
	// paramDouble, String paramString2,
	// Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.zincrby(paramString1, paramDouble, paramString2,
	// paramHandler);
	// }
	//
	// public static RedisClient zinterstore(String paramString, List<String>
	// paramList,
	// AggregateOptions paramAggregateOptions, Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.zinterstore(paramString, paramList,
	// paramAggregateOptions, paramHandler);
	// }
	//
	// public static RedisClient zinterstoreWeighed(String paramString,
	// Map<String, Double> paramMap,
	// AggregateOptions paramAggregateOptions, Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.zinterstoreWeighed(paramString, paramMap,
	// paramAggregateOptions, paramHandler);
	// }
	//
	// public static RedisClient zlexcount(String paramString1, String
	// paramString2, String paramString3,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zlexcount(paramString1, paramString2, paramString3,
	// paramHandler);
	// }
	//
	// public static RedisClient zrange(String paramString, long paramLong1,
	// long paramLong2,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.zrange(paramString, paramLong1, paramLong2,
	// paramHandler);
	// }
	//
	// public static RedisClient zrangeWithOptions(String paramString, long
	// paramLong1, long paramLong2,
	// RangeOptions paramRangeOptions, Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.zrangeWithOptions(paramString, paramLong1, paramLong2,
	// paramRangeOptions, paramHandler);
	// }
	//
	// public static RedisClient zrangebylex(String paramString1, String
	// paramString2, String paramString3,
	// LimitOptions paramLimitOptions, Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.zrangebylex(paramString1, paramString2, paramString3,
	// paramLimitOptions, paramHandler);
	// }
	//
	// public static RedisClient zrangebyscore(String paramString1, String
	// paramString2, String paramString3,
	// RangeLimitOptions paramRangeLimitOptions, Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient
	// .zrangebyscore(paramString1, paramString2, paramString3,
	// paramRangeLimitOptions, paramHandler);
	// }
	//
	// public static RedisClient zrank(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zrank(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient zrem(String paramString1, String paramString2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zrem(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient zremMany(String paramString, List<String>
	// paramList,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zremMany(paramString, paramList, paramHandler);
	// }
	//
	// public static RedisClient zremrangebylex(String paramString1, String
	// paramString2, String paramString3,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zremrangebylex(paramString1, paramString2,
	// paramString3, paramHandler);
	// }
	//
	// public static RedisClient zremrangebyrank(String paramString, long
	// paramLong1, long paramLong2,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zremrangebyrank(paramString, paramLong1, paramLong2,
	// paramHandler);
	// }
	//
	// public static RedisClient zremrangebyscore(String paramString1, String
	// paramString2, String paramString3,
	// Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zremrangebyscore(paramString1, paramString2,
	// paramString3, paramHandler);
	// }
	//
	// public static RedisClient zrevrange(String paramString, long paramLong1,
	// long paramLong2,
	// RangeOptions paramRangeOptions, Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.zrevrange(paramString, paramLong1, paramLong2,
	// paramRangeOptions, paramHandler);
	// }
	//
	// public static RedisClient zrevrangebylex(String paramString1, String
	// paramString2, String paramString3,
	// LimitOptions paramLimitOptions, Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.zrevrangebylex(paramString1, paramString2,
	// paramString3, paramLimitOptions, paramHandler);
	// }
	//
	// public static RedisClient zrevrangebyscore(String paramString1, String
	// paramString2, String paramString3,
	// RangeLimitOptions paramRangeLimitOptions, Handler<AsyncResult<JsonArray>>
	// paramHandler) {
	//
	// return redisClient.zrevrangebyscore(paramString1, paramString2,
	// paramString3, paramRangeLimitOptions,
	// paramHandler);
	// }
	//
	// public static RedisClient zrevrank(String paramString1, String
	// paramString2, Handler<AsyncResult<Long>> paramHandler) {
	//
	// return redisClient.zrevrank(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient zscore(String paramString1, String
	// paramString2, Handler<AsyncResult<String>> paramHandler) {
	//
	// return redisClient.zscore(paramString1, paramString2, paramHandler);
	// }
	//
	// public static RedisClient zunionstore(String paramString, List<String>
	// paramList,
	// AggregateOptions paramAggregateOptions, Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.zunionstore(paramString, paramList,
	// paramAggregateOptions, paramHandler);
	// }
	//
	// public static RedisClient zunionstoreWeighed(String paramString,
	// Map<String, Double> paramMap,
	// AggregateOptions paramAggregateOptions, Handler<AsyncResult<Long>>
	// paramHandler) {
	//
	// return redisClient.zunionstoreWeighed(paramString, paramMap,
	// paramAggregateOptions, paramHandler);
	// }
	//
	// public static RedisClient scan(String paramString, ScanOptions
	// paramScanOptions,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.scan(paramString, paramScanOptions, paramHandler);
	// }
	//
	// public static RedisClient sscan(String paramString1, String paramString2,
	// ScanOptions paramScanOptions,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.sscan(paramString1, paramString2, paramScanOptions,
	// paramHandler);
	// }
	//
	// public static RedisClient hscan(String paramString1, String paramString2,
	// ScanOptions paramScanOptions,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.hscan(paramString1, paramString2, paramScanOptions,
	// paramHandler);
	// }
	//
	// public static RedisClient zscan(String paramString1, String paramString2,
	// ScanOptions paramScanOptions,
	// Handler<AsyncResult<JsonArray>> paramHandler) {
	//
	// return redisClient.zscan(paramString1, paramString2, paramScanOptions,
	// paramHandler);
	// }

}
