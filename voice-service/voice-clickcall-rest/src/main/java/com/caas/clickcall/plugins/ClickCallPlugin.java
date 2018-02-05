package com.caas.clickcall.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.caas.dao.CaasDao;
import com.caas.util.HttpUtilsForHw;
import com.caas.util.HttpUtilsForHwMinNum;
import com.yzx.access.client.HttpUtils;
import com.yzx.auth.plugin.SpringContext;
import com.yzx.auth.service.PluginSupport;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * 
 * @author xupiao 2017年11月6日
 *
 */
public class ClickCallPlugin extends PluginSupport {
	private CaasDao caasDao = null;
	private Timer huaweiAppTokenFlushTimer;
	private Timer huaweiRecordCallbackTimer;

	private static final Logger logger = LogManager.getLogger(ClickCallPlugin.class);

	@Override
	public boolean initService() {
		caasDao = SpringContext.getInstance(CaasDao.class);
		return true;
	}

	@Override
	public void startUpService() {
		String flag = ConfigUtils.getProperty("hw_need_flush_appToken", String.class);
		if (StringUtil.isNotEmpty(flag) && "1".equals(flag)) {
			Long prieod = 30 * 60 * 1000L;

			huaweiAppTokenFlushTimer = new Timer("Huawei-AppToken-Flush");
			huaweiAppTokenFlushTimer.schedule(new TimerTask() { // 串行调度
						@Override
						public void run() {
							execute();
						}
					}, 1000, prieod);

			Long prieod1 = 6 * 60 * 1000L;
			huaweiRecordCallbackTimer = new Timer("Huawei-Record-Callback");
			huaweiRecordCallbackTimer.schedule(new TimerTask() { // 串行调度
						@Override
						public void run() {
							executeRecord();
						}
					}, 1000, prieod1);
		}
	}

	private void executeRecord() {
		List<Map<String, Object>> recordList = caasDao.selectList("common.selectAllRecordCallback");
		for (Map<String, Object> recordMap : recordList) {
			try {
				String recordUrl = (String) recordMap.get("recordUrl");
				String userData = (String) recordMap.get("userData");
				String callId = (String) recordMap.get("callId");
				String recordId = queryRecordList((String) recordMap.get("callIdentifier"));
				if (StringUtil.isNotEmpty("recordId")) {
					String record = "http://api.ucpalm.com/control/record/" + recordId + "/download";
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("recordUrl", record);
					m.put("userData", userData);
					m.put("callId", callId);
					logger.info("华为录音回调 info = {}", m);
					HttpUtils.httpConnectionPost(recordUrl, JsonUtil.toJsonStr(m));
					logger.info("话单录音成功：{}", JsonUtil.toJsonStr(m));
					caasDao.update("common.updateRecordCallback", recordMap);
				}
			} catch (Exception e) {

			}
		}
	}

	@SuppressWarnings("unchecked")
	private String queryRecordList(String callIdentifier) {
		// 封装请求地址
		String url = ConfigUtils.getProperty("baseUrl_hw", String.class) + "/voice/queryRecordList/v1";

		String appKey = ConfigUtils.getProperty("appKey_hw", String.class);
		String appSecret = ConfigUtils.getProperty("appSecret_hw", String.class);
		// 封装JOSN请求
		JSONObject json = new JSONObject();
		json.put("startTime", "2017-03-07T00:00:00Z");
		json.put("endTime", "2050-03-07T23:59:59Z");
		json.put("callIdentifier", callIdentifier);
		json.put("page", "1");
		json.put("pageSize", "1");
		try {
			String respData = HttpUtilsForHwMinNum.sendPost(appKey, appSecret, url, json.toString());
			Map<String, Object> response = JsonUtil.jsonStrToMap(respData);
			if (response != null && "000000".equals(response.get("code"))) {
				String recordId = (String) ((List<Map<String, Object>>) ((Map<String, Object>) response.get("result")).get("record")).get(0).get("recordId");
				return recordId;
			}
		} catch (Exception e) {
			return "";
		}
		return "";
	}

	private void execute() {
		try {
			String app_key = ConfigUtils.getProperty("hw_clickcall_appkey", String.class);
			String username = ConfigUtils.getProperty("hw_clickcall_username", String.class);
			String authorization = ConfigUtils.getProperty("hw_clickcall_authorization", String.class);

			String url = ConfigUtils.getProperty("hw_flush_appToken_url", String.class) + "?app_key=" + app_key + "&username=" + username;
			Map<String, Object> map = new HashMap<String, Object>();

			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", authorization);

			String json = HttpUtilsForHw.postJSON(url, JsonUtil.toJsonStr(map), headers);

			if (StringUtil.isNotEmpty(json)) {
				Map<String, Object> result = JsonUtil.jsonStrToMap(json);
				if ("0".equals(result.get("resultcode"))) {
					String access_token = (String) result.get("access_token");
					Integer expire = Integer.valueOf((String) result.get("expires_in"));
					RedisOpClient.setAndExpire(RedisKeyConsts.HW_APPTOKE, access_token, expire);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	@Override
	public void shutdownService() {
		if (huaweiAppTokenFlushTimer != null) {
			huaweiAppTokenFlushTimer.cancel();
		}
	}
}
