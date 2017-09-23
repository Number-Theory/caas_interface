package com.caas.notify.service;

import io.netty.channel.ChannelHandlerContext;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.util.CommonUtils;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.util.EncryptUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.XMLUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 语音通知
 * 
 * @author xupiao 2017年9月11日
 *
 */
@Service
public class TemplateApplyService extends DefaultServiceCallBack {

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		ApplyTemplateModel vc = JsonUtil.fromJson(request.getRequestString(), new TypeToken<ApplyTemplateModel>() {
		}.getType());
		
		String url = "http://api.yzm.iflyvoice.com:5678/vcapi/uploaddata";
		String appId = "yunzhanghulian";
		String token = "6bc535fa7134295755ab3f8f7e86e";
		
		String respData = null;
		try {
			Map<String, String> headerMap = new HashMap<>();
			headerMap.put("appid", appId);
			DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			String time = df.format(new Date());
			headerMap.put("timestamp", time);
			EncryptUtil md5 = new EncryptUtil();
			headerMap.put("sigkey", md5.md5Digest(appId + token + time));
			headerMap.put("termip", CommonUtils.getUnixIP());
			respData = HttpUtils.httpConnectionPostXML(url, XMLUtil.convertToXml(vc), headerMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("【请求语音验证码接口路径】返回结果resp=" + respData);
	}
}
