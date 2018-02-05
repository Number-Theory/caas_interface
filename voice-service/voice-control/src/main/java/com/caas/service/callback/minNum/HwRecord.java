package com.caas.service.callback.minNum;

import io.netty.channel.ChannelHandlerContext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.yzx.access.util.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.EncryptUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

@Service
public class HwRecord extends DefaultServiceCallBack {

	private static final Logger logger = LogManager.getLogger(HwRecord.class);

	private static final char CHAR_ARRAY[] = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

	/** The Constant UTC. */
	private static final String UTC = "UTC";

	/** The Constant UTC_FORMAT. */
	private static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		String recordId = (String) paramsObject.get("recordId");
		downloadRecord(ctx, recordId);
	}

	public void downloadRecord(ChannelHandlerContext ctx, String recordId) {
		// 封装请求地址
		String url = ConfigUtils.getProperty("baseUrl_hw", String.class) + "/voice/downloadRecord/v1/recordId/";

		String appKey = ConfigUtils.getProperty("appKey_hw", String.class);
		logger.info("【请求华为绑定接口路径】appKey={}", appKey);
		String appSecret = ConfigUtils.getProperty("appSecret_hw", String.class);
		logger.info("【请求华为绑定接口路径】appSecret={}", appSecret);
		// 录音唯一标识(必填)
		File recordFile = sendGet(ctx, appKey, appSecret, url, recordId);
		HttpUtils.sendMessageFile(ctx, recordFile);
	}

	private File sendGet(ChannelHandlerContext ctx, String appKey, String appSecret, String url, String params) {
		String path = System.getProperty("user.home") + File.separatorChar + "recordFile" + File.separatorChar + params + ".wav";

		File file = new File(path);

		HttpURLConnection connection = null;
		try {
			String realPath = url + (StringUtils.isEmpty(params) ? "" : params);
			URL realUrl = new URL(realPath);

			connection = (HttpURLConnection) realUrl.openConnection();
			connection.setDoInput(true);

			// 设置请求方式
			connection.setRequestMethod("GET");

			// 设置接收数据的格式
			connection.setRequestProperty("Accept", "application/json");

			// 设置发送数据的格式
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

			// 设置数据的授权
			connection.setRequestProperty("Authorization", "WSSE realm=\"SDP\", profile=\"UsernameToken\", type=\"AppKey\"");

			// 设置X-WSSE
			generateXWsse(connection, appKey, appSecret);

			connection.connect();

			int fileLength = connection.getContentLength();

			System.out.println("file length---->" + fileLength);

			BufferedInputStream bin = new BufferedInputStream(connection.getInputStream());

			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			OutputStream out = new FileOutputStream(file);
			int size = 0;
			byte[] buf = new byte[1024];
			while ((size = bin.read(buf)) != -1) {
				out.write(buf, 0, size);
			}

			bin.close();
			out.close();
		} catch (Exception e) {
			// logger.info("Send GET request catch exception: " + e.toString());
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			// IOUtils.closeQuietly(in);
			if (null != connection) {
				// IOUtils.close(connection);
				connection = null;
			}
		}
		return file;
	}

	private void generateXWsse(HttpURLConnection connection, String aepAppKey, String aepAppSecret) {
		// 根据AEP校验方式，封装请求参数
		String pwdDigest = "";
		String created = getUtcTime();
		String nonce = genNonce(25);
		String appSecret = aepAppSecret;
		// 加密操作
		try {
			pwdDigest = EncryptUtil.base64(EncryptUtil.SHA256(nonce + created + appSecret));
		} catch (NoSuchAlgorithmException e) {
		}

		String xWsse = "UsernameToken Username=\"" + aepAppKey + "\",PasswordDigest=\"" + pwdDigest + "\",Nonce=\"" + nonce + "\",Created=\"" + created + "\"";

		connection.setRequestProperty("X-WSSE", xWsse);
	}

	/**
	 * 获得UTC时间
	 * 
	 * @return
	 */
	private static String getUtcTime() {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(UTC_FORMAT);
		sdf.setTimeZone(TimeZone.getTimeZone(UTC));
		return sdf.format(calendar.getTime());
	}

	/**
	 * 发送请求时生成的一个随机数
	 * 
	 * @param length
	 *            生成长度
	 * @return
	 */
	private static String genNonce(int length) {
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		int nextPos = CHAR_ARRAY.length;
		int tmp = 0;
		for (int i = 0; i < length; i++) {
			tmp = random.nextInt(nextPos);
			sb.append(CHAR_ARRAY[tmp]);
		}

		return sb.toString();
	}
}
