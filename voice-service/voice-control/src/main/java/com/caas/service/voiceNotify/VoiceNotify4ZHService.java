package com.caas.service.voiceNotify;

import com.caas.model.VoiceCode4ZHCallbackModel;
import com.caas.model.Voice4ZHModel;
import com.caas.util.CommonUtils;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.EncryptUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.core.util.XMLUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

import io.netty.channel.ChannelHandlerContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jweikai on 2017/9/17.
 */
@Service
public class VoiceNotify4ZHService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(VoiceNotify4ZHService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		Voice4ZHModel vc = JsonUtil.fromJson(request.getRequestString(), new TypeToken<Voice4ZHModel>() {
		}.getType());

		logger.info("【接收到Rest组件请求信息】vioceCode4ZH={}", vc);

		// 请求语音验证码接口路径
		String url = ConfigUtils.getProperty("voiceCode_zh_url", String.class);
		logger.info("【请求语音验证码接口路径】url={}", url);

		// 请求东信绑定接口
		String respData = null;
		try {
			Map<String, String> headerMap = new HashMap<>();
			headerMap.put("appid", vc.getAppid());
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			String time = df.format(new Date());
			headerMap.put("timestamp", time);
			EncryptUtil md5 = new EncryptUtil();
			headerMap.put("sigkey", md5.md5Digest(vc.getAppid() + ConfigUtils.getProperty("voiceCode_zh_token", String.class) + time));
			headerMap.put("termip", CommonUtils.getUnixIP());
			respData = HttpUtils.httpConnectionPostXML(url, XMLUtil.convertToXml(vc), headerMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.info("【请求语音验证码接口路径】返回结果resp={}", respData);

		if (StringUtil.isNotEmpty(respData)) {
			VoiceCode4ZHCallbackModel vcResp = (VoiceCode4ZHCallbackModel) XMLUtil.convertXmlStrToObject(VoiceCode4ZHCallbackModel.class, respData);
			if ("0".equals(vcResp.getResult())) {
				setResponse(vc.getExtparam(), response, BusiErrorCode.B_000000, CONTROL_EVENT, "");
			} else {
				setResponse(vc.getExtparam(), response, BusiErrorCode.B_100035, CONTROL_EVENT, "");
			}
		} else {
			setResponse(vc.getExtparam(), response, BusiErrorCode.B_900000, CONTROL_EVENT, "");
		}
	}
}
