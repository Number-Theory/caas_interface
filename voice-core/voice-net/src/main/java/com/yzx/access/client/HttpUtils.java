package com.yzx.access.client;

import com.yzx.core.config.ConfigUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

public class HttpUtils {
	private static Logger log = LogManager.getLogger(HttpUtils.class);
	
	public static String httpConnectionGet(String strUrl) throws Exception {
		InputStream ins = null;
		BufferedReader l_reader = null;
		URL url = new URL(strUrl);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		connection.setReadTimeout(ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		connection.connect();
		ins = connection.getInputStream();
		l_reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = l_reader.readLine()) != null) {
			result.append(line);
			result.append("\r\n");
		}
		l_reader.close();
		ins.close();
		log.info(result.toString());
		return result.toString();
	}

	public static String httpConnectionPost(String strUrl, String content) throws Exception {
		URL url = new URL(strUrl);
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setConnectTimeout(ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		connection.setReadTimeout(ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		connection.connect();
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.writeBytes(content);
		out.flush();
		out.close();
		InputStream ins = connection.getInputStream();
		BufferedReader l_reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = l_reader.readLine()) != null) {
			result.append(line);
			result.append("\r\n");
		}
		l_reader.close();
		ins.close();
		log.info(result.toString());
		return result.toString();
	}

	public static String httpConnectionPostJson(String strUrl, String content) throws Exception {
		String result = "";
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams
				.setConnectionTimeout(httpParams, ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		HttpConnectionParams.setSoTimeout(httpParams, ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpPost httppost = new HttpPost(strUrl);
		httppost.setHeader("Accept", "application/json");
		httppost.setHeader("Content-Type", "application/json;charset=utf-8");

		BasicHttpEntity requestBody = new BasicHttpEntity();
		requestBody.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
		requestBody.setContentLength(content.getBytes("UTF-8").length);
		httppost.setEntity(requestBody);

		// 执行客户端请求
		HttpResponse response = httpclient.execute(httppost);

		HttpEntity entity = response.getEntity();

		if (entity != null) {
			result = EntityUtils.toString(entity, "UTF-8");
		}
		EntityUtils.consume(entity);
		return result;
	}

	public static String httpConnectionPostXML(String strUrl, String content) throws Exception {
		return httpConnectionPostXML(strUrl, content, null);
	}

	public static String httpConnectionPostXML(String strUrl, String content, Map<String, String> header) throws Exception {
		String result = "";
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams
				.setConnectionTimeout(httpParams, ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		HttpConnectionParams.setSoTimeout(httpParams, ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class));
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParams);
		HttpPost httppost = new HttpPost(strUrl);
		httppost.setHeader("Accept", "application/xml");
		httppost.setHeader("Content-Type", "application/xml;charset=utf-8");
		if ( header != null ) {
			for (String key : header.keySet()) {
				httppost.setHeader(key, header.get(key));
			}
		}

		BasicHttpEntity requestBody = new BasicHttpEntity();
		requestBody.setContent(new ByteArrayInputStream(content.getBytes("UTF-8")));
		requestBody.setContentLength(content.getBytes("UTF-8").length);
		httppost.setEntity(requestBody);

		// 执行客户端请求
		HttpResponse response = httpclient.execute(httppost);

		HttpEntity entity = response.getEntity();

		if (entity != null) {
			result = EntityUtils.toString(entity, "UTF-8");
		}
		EntityUtils.consume(entity);
		return result;
	}
}
