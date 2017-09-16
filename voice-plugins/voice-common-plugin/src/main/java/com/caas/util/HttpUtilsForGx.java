package com.caas.util;

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.EncryptUtil;
import com.yzx.core.util.JsonUtil;

/**
 * 
 * @author xupiao 2017年8月21日
 *
 */
public class HttpUtilsForGx {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtilsForGx.class);
	private static String appkey1 = ConfigUtils.getProperty("appKey_gx", String.class);
	private static String appkey2 = ConfigUtils.getProperty("appKey_gxHiddenAndBack", String.class);
	private static DateFormat dtl = new SimpleDateFormat("yyyyMMddkkmmssFFF");
	private static String ts = dtl.format(new Date());

	public static String deleteJSON(String url, String data, String type) {
		String appkey = chooseKey(type);
		StringBuffer msgdgtStr = generateMsgBuffer(data, ts, type);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = null;
		try {
			EncryptUtil encryptUtil = new EncryptUtil();
			String msgdgt = encryptUtil.md5Digest(msgdgtStr.toString());
			HttpDelete delete = new HttpDelete(url);
			delete.setHeader("Accept", "application/json");
			delete.setHeader("Content-Type", "application/json;charset=utf-8");
			delete.setHeader("appkey", appkey);
			delete.setHeader("ts", ts);
			delete.setHeader("msgdgt", msgdgt);

			// 执行客户端请求
			HttpResponse response = httpclient.execute(delete);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			logger.info("【发送post请求JSON】成功： result=" + result);
		} catch (Throwable e) {
			logger.info("【发送post请求JSON】失败： result=" + result, e);
		}
		return result;
	}

	/**
	 * 发送post请求JSON
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	public static String postJSON(String url, String data, String type) {
		logger.info("【发送post请求JSON】开始");
		String appkey = chooseKey(type);
		StringBuffer msgdgtStr = generateMsgBuffer(data, ts, type);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = null;
		try {
			EncryptUtil encryptUtil = new EncryptUtil();
			String msgdgt = encryptUtil.md5Digest(msgdgtStr.toString());
			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-Type", "application/json;charset=utf-8");
			httppost.setHeader("appkey", appkey);
			httppost.setHeader("ts", ts);
			httppost.setHeader("msgdgt", msgdgt);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(data.getBytes("UTF-8")));
			requestBody.setContentLength(data.getBytes("UTF-8").length);
			httppost.setEntity(requestBody);
			// 执行客户端请求
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			logger.info("【发送post请求JSON】成功： result=" + result);
		} catch (Throwable e) {
			logger.info("【发送post请求JSON】失败： result=" + result, e);
		}
		return result;
	}

	public static String putJson(String url, String data, String type) {
		String appkey = chooseKey(type);
		StringBuffer msgdgtStr = generateMsgBuffer(data, ts, type);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = null;
		try {
			EncryptUtil encryptUtil = new EncryptUtil();
			String msgdgt = encryptUtil.md5Digest(msgdgtStr.toString());
			HttpPut httpput = new HttpPut(url);
			httpput.setHeader("Accept", "application/json");
			httpput.setHeader("Content-Type", "application/json;charset=utf-8");
			httpput.setHeader("appkey", appkey);
			httpput.setHeader("ts", ts);
			httpput.setHeader("msgdgt", msgdgt);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(data.getBytes("UTF-8")));
			requestBody.setContentLength(data.getBytes("UTF-8").length);
			httpput.setEntity(requestBody);
			// 执行客户端请求
			HttpResponse response = httpclient.execute(httpput);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			logger.info("【发送post请求JSON】成功： result=" + result);
		} catch (Throwable e) {
			logger.info("【发送post请求JSON】失败： result=" + result, e);
		}
		return result;

	}

	public static StringBuffer generateMsgBuffer(String data, String ts, String type) {
		String appkey = chooseKey(type);
		Map<String, Object> msgdgtMap = new TreeMap<String, Object>();
		JSONObject jsonObject = JsonUtil.toJsonObj(data);
		String extra = jsonObject.get("extra") == null ? "" : jsonObject.get("extra").toString();
		if (!extra.isEmpty()) {
			JSONObject object = JsonUtil.toJsonObj(extra);
			String callrecording = (String) object.get("callrecording");
			String calldisplay = (String) object.get("calldisplay");
			jsonObject.remove("extra");
			jsonObject.put("callrecording", callrecording);
			jsonObject.put("calldisplay", calldisplay);
			msgdgtMap = JsonUtil.jsonStrToMap(jsonObject.toString());
		} else {
			msgdgtMap = JsonUtil.jsonStrToMap(data);
		}

		msgdgtMap.put("appkey", appkey);
		msgdgtMap.put("ts", ts);
		Map<String, Object> resultMap = sortMapByKey(msgdgtMap);
		logger.info("msgMap :" + msgdgtMap.toString());
		StringBuffer msgdgtStr = new StringBuffer();
		String secretKey = chooseSecretKey(type);
		msgdgtStr.append(secretKey);
		// msgdgtStr.append("dfjr_yzxpt_105");
		Iterator<String> mapIt = resultMap.keySet().iterator();
		while (mapIt.hasNext()) {
			String key;
			String value = null;
			key = mapIt.next().toString();

			value = (String) resultMap.get(key);

			msgdgtStr.append(key).append(value);
		}
		logger.info("msgStr :" + msgdgtStr.toString());
		return msgdgtStr;
	}

	/**
	 * 使用 Map按key进行排序
	 * 
	 * @param msgdgtMap
	 * @return
	 */
	public static Map<String, Object> sortMapByKey(Map<String, Object> msgdgtMap) {
		if (msgdgtMap == null || msgdgtMap.isEmpty()) {
			return null;
		}

		Map<String, Object> sortMap = new TreeMap<String, Object>(new Comparator<String>() {
			@Override
			public int compare(String str1, String str2) {

				return str1.compareTo(str2);
			}
		});

		sortMap.putAll(msgdgtMap);

		return sortMap;
	}

	public static String updateJSON(String strUrl, String data, String type) {
		String appkey = chooseKey(type);
		StringBuffer msgdgtStr = generateMsgBuffer(data, ts, type);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = null;
		try {
			EncryptUtil encryptUtil = new EncryptUtil();
			String msgdgt = encryptUtil.md5Digest(msgdgtStr.toString());
			HttpPut httpput = new HttpPut(strUrl);
			httpput.setHeader("Accept", "application/json");
			httpput.setHeader("Content-Type", "application/json;charset=utf-8");
			httpput.setHeader("appkey", appkey);
			httpput.setHeader("ts", ts);
			httpput.setHeader("msgdgt", msgdgt);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(data.getBytes("UTF-8")));
			requestBody.setContentLength(data.getBytes("UTF-8").length);
			httpput.setEntity(requestBody);
			// 执行客户端请求
			HttpResponse response = httpclient.execute(httpput);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			logger.info("【发送post请求JSON】成功： result=" + result);
		} catch (Throwable e) {
			logger.info("【发送post请求JSON】失败： result=" + result, e);
		}
		return result;
	}

	public static String postJSONWithNoBody(String url) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		String result = null;
		try {

			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Accept", "application/json");
			httppost.setHeader("Content-Type", "application/json;charset=utf-8");

			// 执行客户端请求
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			logger.debug("【发送post请求JSON】成功： result=" + result);
		} catch (Throwable e) {
			logger.error("【发送post请求JSON】失败： result=" + result, e);
		}
		return result;
	}

	public static String chooseKey(String key) {
		if ("0".equals(key)) {
			logger.debug("【选择业务类型】成功： appKey=" + appkey1);
			return appkey1;

		} else if ("1".equals(key)) {
			logger.debug("【选择业务类型】成功： appKey=" + appkey2);
			return appkey2;

		} else {
			logger.debug("【选择业务类型】成功： appKey=" + null);
			return null;
		}

	}

	public static String chooseSecretKey(String type) {
		if ("0".equals(type)) {
			logger.debug("【选择业务类型】成功： secretKey=" + ConfigUtils.getProperty("secretKey1", String.class));
			return ConfigUtils.getProperty("secretKey1", String.class);

		} else if ("1".equals(type)) {
			logger.debug("【选择业务类型】成功： secretKey=" + ConfigUtils.getProperty("secretKey2", String.class));
			return ConfigUtils.getProperty("secretKey2", String.class);

		} else {
			logger.debug("【选择业务类型】成功： secretKey=" + null);
			return null;

		}
	}
}
