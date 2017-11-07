package com.caas.util;

import java.io.ByteArrayInputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONException;

/**
 * 
 * @author xupiao 2017年8月21日
 *
 */
public class HttpUtilsForHw {
	private static final Logger logger = LoggerFactory.getLogger(HttpUtilsForHw.class);

	public static String postJSON(String url, String data) {
		return postJSON(url, data, null);
	}

	/**
	 * 发送post请求JSON
	 * 
	 * @param url
	 * @param data
	 * @return
	 * @throws JSONException
	 */
	@SuppressWarnings("deprecation")
	public static String postJSON(String url, String data, Map<String, String> headers) {
		logger.info("【发送post请求JSON】开始");
		String result = null;
		try {
			SSLContext sslcontext = createIgnoreVerifySSL();
			SSLSocketFactory ssf = new SSLSocketFactory(sslcontext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
					.register("http", PlainConnectionSocketFactory.INSTANCE).register("https", ssf).build();

			PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			// HttpClients.custom().setConnectionManager(connManager);

			// 创建自定义的httpclient对象
			CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(connManager).build();

			HttpPost httppost = new HttpPost(url);
			httppost.setHeader("Accept", "*/*");
			httppost.setHeader("Content-Type", "application/json;charset=UTF-8");
			setHttpHeaders(httppost, headers);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(data.getBytes("UTF-8")));
			requestBody.setContentLength(data.getBytes("UTF-8").length);
			httppost.setEntity(requestBody);

			printInBoundMessage(httppost, data);
			// 执行客户端请求
			HttpResponse response = httpclient.execute(httppost);

			HttpEntity entity = response.getEntity();

			if (entity != null) {
				result = EntityUtils.toString(entity, "UTF-8");
			}
			EntityUtils.consume(entity);
			logger.info("【发送post请求JSON】成功： result=" + result + ",statusCode=" + response.getStatusLine().getStatusCode());
		} catch (Throwable e) {
			logger.info("【发送post请求JSON】失败： result=" + result, e);
		}
		return result;
	}

	private static void setHttpHeaders(HttpPost httppost, Map<String, String> headers) {
		if (headers != null && headers.size() > 0) {
			Set<String> keys = headers.keySet();
			for (String key : keys) {
				httppost.setHeader(key, headers.get(key));
			}
		}
	}

	/**
	 * 绕过验证
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sc = SSLContext.getInstance("SSL");

		// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
		X509TrustManager trustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException {
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sc.init(null, new TrustManager[] { trustManager }, null);
		return sc;
	}

	private static void printInBoundMessage(HttpPost request, String requestString) {
		StringBuffer info = new StringBuffer();
		info.append(request.getURI()).append("\n");

		info.append("Http-Method: ").append(request.getMethod()).append("\n");
		info.append("Headers: [");
		boolean flag = true;
		for (Header header : request.getAllHeaders()) {
			if (flag) {
				info.append(header.getName()).append(":").append(header.getValue());
			} else {
				info.append(";").append(header.getName()).append(":").append(header.getValue());
			}
			flag = false;
		}
		info.append("]\n");

		info.append("Payload: ").append(requestString).append("\n---------------------------");
		System.out.println(info);
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
}
