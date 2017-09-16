package com.yzx.access.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.core.config.ConfigUtils;

public class HttpClientUtil {
	private static HttpClientUtil httpClientUtil = new HttpClientUtil();

	private final Logger logger = LogManager.getLogger(HttpClientUtil.class);

	final ConnectingIOReactor ioReactor;

	final PoolingNHttpClientConnectionManager cm;

	final CloseableHttpAsyncClient httpclient;
	final RequestConfig requestConfig;// 设置请求和传输超时时间
	final HttpClientContext ctx;

	private HttpClientUtil() {

		Integer timeout = ConfigUtils.getProperty("http.client.超时时间", 10000, Integer.class);
		Integer maxCount = ConfigUtils.getProperty("http.client.连接池最大连接数", 100, Integer.class);

		try {
			ioReactor = new DefaultConnectingIOReactor();
		} catch (IOReactorException e) {
			logger.error(e);
			throw new RuntimeException(e);
		}

		cm = new PoolingNHttpClientConnectionManager(ioReactor);
		cm.setMaxTotal(maxCount);
		cm.setDefaultMaxPerRoute(maxCount);

		ctx = new HttpClientContext();
		requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout)
				.setConnectionRequestTimeout(timeout).build();// 设置请求和传输超时时间
		ctx.setRequestConfig(requestConfig);

		httpclient = HttpAsyncClients.custom().setConnectionManager(cm).setDefaultRequestConfig(requestConfig)
				.setMaxConnTotal(maxCount).setMaxConnPerRoute(maxCount).build();
		httpclient.start();
	}

	public static HttpClientUtil get() {
		return httpClientUtil;
	}

	public Future<HttpResponse> httpPost(String url, String body, final AbstractFutureCallback abstractFutureCallback) {
		Future<HttpResponse> future = null;
		try {
			final HttpPost request = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.copy(this.requestConfig).build();
			request.setConfig(requestConfig);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "application/json;charset=utf-8");
			request.setHeader("Connection", "Close");
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(body.getBytes("UTF-8")));
			requestBody.setContentLength(body.getBytes("UTF-8").length);
			request.setEntity(requestBody);
			logger.info(" requestUrl= " + url);
			future = httpclient.execute(request, ctx, new FutureCallback<HttpResponse>() {

				public void completed(final HttpResponse response) {
					logger.debug(request.getRequestLine() + "->" + response.getStatusLine());
					try {
						String content = EntityUtils.toString(response.getEntity(), "UTF-8");
						logger.info(" response content is : " + content);
						abstractFutureCallback.execute(content);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					} finally {
					}
				}

				public void failed(final Exception ex) {
					logger.error(request.getRequestLine() + "->" + ex);
					abstractFutureCallback.failed(ex);
				}

				public void cancelled() {
					logger.warn(request.getRequestLine() + " cancelled");
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return future;
	}

	public Future<HttpResponse> httpGet(String url, final AbstractFutureCallback abstractFutureCallback) {
		Future<HttpResponse> future = null;
		try {
			final HttpGet request = new HttpGet(url);
			RequestConfig requestConfig = RequestConfig.copy(this.requestConfig).build();
			request.setConfig(requestConfig);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "application/json;charset=utf-8");
			request.setHeader("Connection", "Close");
			logger.info(" requestUrl= " + url);
			future = httpclient.execute(request, ctx, new FutureCallback<HttpResponse>() {
				public void completed(final HttpResponse response) {
					logger.debug(request.getRequestLine() + "->" + response.getStatusLine());
					try {
						String content = EntityUtils.toString(response.getEntity(), "UTF-8");
						logger.info(" response content is : " + content);
						abstractFutureCallback.execute(content);
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
					}
				}

				public void failed(final Exception ex) {
					logger.error(request.getRequestLine() + "->" + ex);
					ex.printStackTrace();
					abstractFutureCallback.failed(ex);
				}

				public void cancelled() {
					logger.warn(request.getRequestLine() + " cancelled");
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return future;
	}
	/**
	 * 回调专用POST方法
	 * @param url
	 * @param body
	 * @param abstractFutureCallback
	 * @return
	 */
	public Future<HttpResponse> httpPostBack(String url, String body, final AbstractFutureCallback abstractFutureCallback) {
		Future<HttpResponse> future = null;
		try {
			final HttpPost request = new HttpPost(url);
			RequestConfig requestConfig = RequestConfig.copy(this.requestConfig).build();
			request.setConfig(requestConfig);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "application/json;charset=utf-8");
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(body.getBytes("UTF-8")));
			requestBody.setContentLength(body.getBytes("UTF-8").length);
			request.setEntity(requestBody);
			logger.info(" requestUrl= " + url);
			future = httpclient.execute(request, ctx, new FutureCallback<HttpResponse>() {

				public void completed(final HttpResponse response) {
					logger.debug(request.getRequestLine() + "->" + response.getStatusLine());
					try {
						String content = EntityUtils.toString(response.getEntity(), "UTF-8");
						logger.info(" response content is : " + content);
						String statusCode = String.valueOf(response.getStatusLine().getStatusCode());
						abstractFutureCallback.execute(statusCode);
					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					} finally {
					}
				}

				public void failed(final Exception ex) {
					logger.error(request.getRequestLine() + "->" + ex);
					throw new RuntimeException(ex);
				}

				public void cancelled() {
					logger.warn(request.getRequestLine() + " cancelled");
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return future;
	}

	
}
