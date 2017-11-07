package com.caas.clickcall.plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.caas.util.HttpUtilsForHw;
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
	private Timer huaweiAppTokenFlushTimer;

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
		}
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
