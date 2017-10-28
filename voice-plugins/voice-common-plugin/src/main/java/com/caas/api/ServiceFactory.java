package com.caas.api;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.caas.dao.CaasDao;
import com.yzx.auth.plugin.SpringContext;

/**
 * 
 * @author xupiao 2016年11月15日
 *
 */
public class ServiceFactory {
	private static final Logger logger = LoggerFactory.getLogger(ServiceFactory.class);
	private static Map<String, ServiceFactory.ServerInfo> servers = new HashMap<String, ServiceFactory.ServerInfo>();
	private static CaasDao caasDao = SpringContext.getInstance(CaasDao.class);
	public static Integer currentVersion = 0;
	public static final String VERSION_KEY = "caas_api_weight";

	public static void init() {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", VERSION_KEY);
		currentVersion = caasDao.selectOne("caas.selectCacheVersion", params);
		servers = initServer();
		new Timer("api-update-thread").schedule(new TimerTask() {
			@Override
			public void run() {
				int DBVersion = caasDao.selectOne("caas.selectCacheVersion", params);
				if (DBVersion > currentVersion) {
					synchronized (servers) {
						currentVersion = DBVersion;
						servers = initServer();
					}
				}
			}
		}, 1000, 60 * 1000);
	}

	private static Map<String, ServiceFactory.ServerInfo> initServer() {
		Map<String, ServiceFactory.ServerInfo> tempServers = new HashMap<String, ServiceFactory.ServerInfo>();
		try {
			logger.info("开始加载权重数据...");

			List<Map<String, Object>> appApiList = caasDao.selectList("caas.getAllAppApiList");
			for (Map<String, Object> appApi : appApiList) {
				Integer apiId = (Integer) appApi.get("apiId");
				String appId = (String) appApi.get("appId");
				Integer weight = (Integer) appApi.get("weight");
				Map<String, Object> sqlParams = new HashMap<String, Object>();
				sqlParams.put("id", apiId);
				Map<String, Object> apiMap = caasDao.selectOne("caas.getApi", sqlParams);

				ServiceFactory.ServerInfo serverInfo = tempServers.get(appId);
				if (serverInfo == null) {
					serverInfo = new ServerInfo();
					tempServers.put(appId, serverInfo);
				}
				APIServer apiServer = new APIServer();
				apiServer.setId(String.valueOf(apiMap.get("id")));
				if (apiMap.get("apiBusiType") != null) {
					apiServer.setApiType((String) apiMap.get("apiBusiType"));
				}
				if (apiMap.get("apiCallUrl") != null) {
					apiServer.setCallUrl((String) apiMap.get("apiCallUrl"));
				}
				if (apiMap.get("apiCancelUrl") != null) {
					apiServer.setCancelUrl((String) apiMap.get("apiCancelUrl"));
				}
				if (apiMap.get("httpProxyUrl") != null) {
					apiServer.setUrl((String) apiMap.get("httpProxyUrl"));
				}
				if (apiMap.get("apiClass") != null) {
					apiServer.setClassName((String) apiMap.get("apiClass"));
				}
				if (apiMap.get("userParams") != null) {
					apiServer.setUseParams((String) apiMap.get("userParams"));
				}
				apiServer.setWeigth(weight);

				serverInfo.getServers().add(apiServer);
			}

			for (String key : tempServers.keySet()) {
				ServerInfo serverInfo = tempServers.get(key);
				serverInfo.setCurrentIndex(-1);
				serverInfo.setCurrentWeight(0);
				serverInfo.setGcdWeight(getGCDForServers(serverInfo.getServers()));
				serverInfo.setMaxWeight(getMaxWeightForServers(serverInfo.getServers()));
				serverInfo.setServerCount(serverInfo.getServers().size());
			}
			logger.info("加载权重数据结束...");
			logger.info("API资源信息为：" + tempServers);
		} catch (Exception e) {
			logger.error("加载权重参数失败！", e);
		}
		return tempServers;
	}

	public static APIServer getBestServer(String key) {
		APIServer server = _getBestServer(key);

		return server;
	}

	/**
	 * 算法流程： 假设有一组服务器 S = {S0, S1, …, Sn-1} 有相应的权重，变量currentIndex表示上次选择的服务器
	 * 权值currentWeight初始化为0，currentIndex初始化为-1 ，当第一次的时候返回 权值取最大的那个服务器， 通过权重的不断递减
	 * 寻找 适合的服务器返回，直到轮询结束，权值返回为0
	 */
	private static APIServer _getBestServer(String key) {
		ServerInfo serverInfo = servers.get(key);
		if (serverInfo == null) {
			logger.error("key={}的服务器没有配置服务器信息！", key);
			return null;
		}
		List<APIServer> serverList = serverInfo.getServers();
		Integer currentIndex = serverInfo.getCurrentIndex();
		Integer gcdWeight = serverInfo.getGcdWeight();
		Integer currentWeight = serverInfo.getCurrentWeight();
		Integer serverCount = serverInfo.getServerCount();
		Integer maxWeight = serverInfo.getMaxWeight();
		logger.info("key={}的服务器信息为：{}", key, serverList);
		if (serverList == null || serverList.size() <= 0) {
			logger.error("key={}的服务器没有配置服务器信息！", key);
			return null;
		}
		while (true) {
			currentIndex = (currentIndex + 1) % serverCount;
			serverInfo.setCurrentIndex(currentIndex);
			if (currentIndex == 0) {
				currentWeight = currentWeight - gcdWeight;
				serverInfo.setCurrentWeight(currentWeight);
				if (currentWeight <= 0) {
					currentWeight = maxWeight;
					serverInfo.setCurrentWeight(currentWeight);
					if (currentWeight == 0)
						return null;
				}
			}
			if (serverList.get(currentIndex).getWeigth() >= currentWeight) {
				logger.info("currentIndex={}的服务器信息为：{}", currentIndex, serverList.get(currentIndex));
				return serverList.get(currentIndex);
			}
		}
	}

	/**
	 * 返回最大公约数
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static int gcd(int a, int b) {
		BigInteger b1 = new BigInteger(String.valueOf(a));
		BigInteger b2 = new BigInteger(String.valueOf(b));
		BigInteger gcd = b1.gcd(b2);
		return gcd.intValue();
	}

	/**
	 * 返回所有服务器权重的最大公约数
	 * 
	 * @param serverList
	 * @return
	 */
	private static int getGCDForServers(List<APIServer> serverList) {
		int w = 0;
		for (int i = 0, len = serverList.size(); i < len - 1; i++) {
			if (w == 0) {
				w = gcd(serverList.get(i).getWeigth(), serverList.get(i + 1).getWeigth());
			} else {
				w = gcd(w, serverList.get(i + 1).getWeigth());
			}
		}
		return w;
	}

	/**
	 * 返回所有服务器中的最大权重
	 * 
	 * @param serverList
	 * @return
	 */
	private static int getMaxWeightForServers(List<APIServer> serverList) {
		int w = 0;
		for (int i = 0, len = serverList.size(); i < len; i++) {
			w = Math.max(w, serverList.get(i).getWeigth());
		}
		return w;
	}

	static class ServerInfo {
		private List<APIServer> servers = new ArrayList<APIServer>();
		private int currentIndex = -1;// 上一次选择的服务器
		private int currentWeight = 0;// 当前调度的权值
		private int maxWeight = 0; // 最大权重
		private int gcdWeight = 0; // 所有服务器权重的最大公约数
		private int serverCount = 0; // 服务器数量

		public List<APIServer> getServers() {
			return servers;
		}

		public void setServers(List<APIServer> servers) {
			this.servers = servers;
		}

		public int getCurrentIndex() {
			return currentIndex;
		}

		public void setCurrentIndex(int currentIndex) {
			this.currentIndex = currentIndex;
		}

		public int getCurrentWeight() {
			return currentWeight;
		}

		public void setCurrentWeight(int currentWeight) {
			this.currentWeight = currentWeight;
		}

		public int getMaxWeight() {
			return maxWeight;
		}

		public void setMaxWeight(int maxWeight) {
			this.maxWeight = maxWeight;
		}

		public int getGcdWeight() {
			return gcdWeight;
		}

		public void setGcdWeight(int gcdWeight) {
			this.gcdWeight = gcdWeight;
		}

		public int getServerCount() {
			return serverCount;
		}

		public void setServerCount(int serverCount) {
			this.serverCount = serverCount;
		}

		@Override
		public String toString() {
			return "ServerInfo [servers=" + servers + "]";
		}
	}
}
