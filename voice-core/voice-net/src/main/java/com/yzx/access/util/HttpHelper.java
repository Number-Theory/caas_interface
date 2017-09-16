package com.yzx.access.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class HttpHelper {
	private static final Log log = LogFactory.getLog(HttpHelper.class);
	
	public static String httpConnectionGet(String strUrl,int http_timeout) {
		try {
			InputStream ins=null;
			BufferedReader l_reader=null;
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(http_timeout*1000);
			connection.setReadTimeout(http_timeout*1000);
			connection.connect();
			ins = connection.getInputStream();
			l_reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
			StringBuffer result = new StringBuffer();
			String line="";
			while ((line=l_reader.readLine())!=null) {
				result.append(line);
				result.append("\r\n");
			}
			l_reader.close();
			ins.close();
			return result.toString();
		} catch (Exception e) {
			log.error(e+","+strUrl);
		}
		return null;
	}
	
	public static String httpConnectionPost(String strUrl, String content) {
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			connection.setConnectTimeout(1000);
			connection.setReadTimeout(1000);
			connection.connect();
			DataOutputStream out=new DataOutputStream(connection.getOutputStream());
			out.writeBytes(content);
			out.flush();
			out.close();
			InputStream ins = connection.getInputStream();
			BufferedReader l_reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line=l_reader.readLine())!=null){
				result.append(line);
				result.append("\r\n");
			}
			l_reader.close();
			ins.close();
			log.info(result.toString());
			return result.toString();
		} catch (Exception e) {
			log.error(e+","+strUrl);
		}
		return null;
	}
	public static String httpConnectionPostJson(String strUrl, String content) {
		try {
			URL url = new URL(strUrl);
			URLConnection connection = url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Content-Type","application/json;charset=utf-8");
			connection.setConnectTimeout(1000);
			connection.setReadTimeout(1000);
			connection.connect();
			OutputStreamWriter out = new OutputStreamWriter(
					connection.getOutputStream());
			out.write(new String(content.getBytes("UTF-8")));
			out.flush();
			out.close();
			InputStream ins = connection.getInputStream();
			BufferedReader l_reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
			StringBuffer result = new StringBuffer();
			String line = "";
			while ((line = l_reader.readLine())!=null) {
				result.append(line);
				result.append("\r\n");
			}
			l_reader.close();
			ins.close();
			log.info(result.toString());
			return result.toString();
		} catch (Exception e) {
			log.error(e+","+strUrl);
		}
		return null;
	}
}
