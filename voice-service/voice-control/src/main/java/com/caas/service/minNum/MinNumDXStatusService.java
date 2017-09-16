package com.caas.service.minNum;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.caas.model.CallinModel;
import com.caas.model.StatusCallbackModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 小号，东信AX状态回调
 * 
 * @author xupiao 2017年8月21日
 *
 */
@Service
public class MinNumDXStatusService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(MinNumDXStatusService.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		CallinModel callinModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<CallinModel>() {
		}.getType());

		StatusCallbackModel statusCallbackModel = new StatusCallbackModel();

		try {
			new HttpClient(new ClientHandler() {

				@Override
				public void failed(Exception ex) {

				}

				@Override
				public void execute(HttpResponse response, String context) {

				}
			}).httpPost("", JsonUtil.toJsonStr(statusCallbackModel));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("请求状态回调组件失败：", e);
		}

	}
}
