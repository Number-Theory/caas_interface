package com.caas.service.safetyCall;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponse;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.caas.model.BillCallbackModel;
import com.caas.model.FinishModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.callback.ClientHandler;
import com.yzx.access.client.HttpClient;
import com.yzx.core.util.JsonUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
/**
 * 小号，东信AX话单回调接口
 * @author xupiao 2017年8月21日
 *
 */
@Service
public class SafetyCallDXBillService extends DefaultServiceCallBack {
	private static final Logger logger = LogManager.getLogger(SafetyCallDXBillService.class);
	
	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		FinishModel finishModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<FinishModel>() {
		}.getType());

		BillCallbackModel billCallbackModel = new BillCallbackModel();
		
//		billCallbackModel.setUserId(userId);

		try {
			new HttpClient(new ClientHandler() {

				@Override
				public void failed(Exception ex) {

				}

				@Override
				public void execute(HttpResponse response, String context) {

				}
			}).httpPost("", JsonUtil.toJsonStr(billCallbackModel));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("请求挂机回调组件失败：", e);
		}
	}
}
