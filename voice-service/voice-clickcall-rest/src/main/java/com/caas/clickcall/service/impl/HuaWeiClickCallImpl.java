//package com.caas.clickcall.service.impl;
//
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.http.HttpResponse;
//
//import java.util.Map;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//import com.caas.clickcall.service.ClickCallApiService;
//import com.caas.model.ClickCallModel;
//import com.google.gson.reflect.TypeToken;
//import com.yzx.access.callback.ClientHandler;
//import com.yzx.access.client.HttpClient1;
//import com.yzx.access.util.HttpUtils;
//import com.yzx.core.config.ConfigUtils;
//import com.yzx.core.consts.EnumType.BusiErrorCode;
//import com.yzx.core.util.JsonUtil;
//import com.yzx.core.util.Log4jUtils;
//import com.yzx.engine.model.ServiceRequest;
//import com.yzx.engine.model.ServiceResponse;
//import com.yzx.engine.spi.impl.DefaultServiceCallBack;
//
///**
// * 
// * @author xupiao 2017年10月28日
// *
// */
//public class HuaWeiClickCallImpl extends DefaultServiceCallBack implements ClickCallApiService {
//	
//	private static Logger logger = LogManager.getLogger(HuaWeiClickCallImpl.class);
//
//	@Override
//	public void callback(String callId, ServiceRequest request, ServiceResponse response, ChannelHandlerContext ctx, ClickCallModel clickCallModel) {
//
//		String controlUrl = ConfigUtils.getProperty("caas_control_url", String.class) + "/control/clickcall";
//
//		try {
//			new HttpClient1(new ClientHandler() {
//				@Override
//				public void execute(HttpResponse response, String context) {
//					Map<String, Object> resultMap = JsonUtil.jsonStrToMap(context);
//					Log4jUtils.initLog4jContext(request.getLogId());
//					ServiceResponse controlResponse = JsonUtil.fromJson(context, new TypeToken<ServiceResponse>() {
//					}.getType());
//					if (BusiErrorCode.B_000000.getErrCode().equals(controlResponse.getResult())
//							&& (resultMap != null && resultMap.containsKey("code") && "0".equals(String.valueOf(resultMap.get("code"))))) {
//
//						HttpUtils.sendMessageJson(ctx, controlResponse.toString());
//
//					} else {
//
//						HttpUtils.sendMessageJson(ctx, controlResponse.toString());
//					}
//				}
//
//				@Override
//				public void failed(Exception ex) {
//					Log4jUtils.initLog4jContext(request.getLogId());
//					logger.info("请求caas_control组件失败,ex={}", ex);
//					setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, clickCallModel.getUserData());
//					HttpUtils.sendMessageJson(ctx, response.toString());
//				}
//			}).httpPost(controlUrl, JsonUtil.toJsonStr("")); // TODO
//		} catch (Exception e) {
//			logger.info("请求caas_control组件出错,ex={}", e);
//			setResponse(callId, response, BusiErrorCode.B_900000, REST_EVENT, clickCallModel.getUserData());
//			HttpUtils.sendMessageJson(ctx, response.toString());
//		}
//	}
//
//}
