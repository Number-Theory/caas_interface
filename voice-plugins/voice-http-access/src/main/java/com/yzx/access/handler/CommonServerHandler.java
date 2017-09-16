package com.yzx.access.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yzx.access.callback.ServerHandler;
import com.yzx.access.util.HttpUtils;
import com.yzx.core.consts.EnumType.SysErrorCode;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.Log4jUtils;
import com.yzx.engine.facade.OnlineFacade;
import com.yzx.engine.facotry.ThreadPoolsFactory;
import com.yzx.engine.model.ServiceResponse;

/**
 * 
 * @author xupiao 2017年6月5日
 *
 */
public class CommonServerHandler implements ServerHandler {
	private static final Logger logger = LogManager.getLogger(CommonServerHandler.class);

	@Override
	public void call(HttpRequest request, String url, ChannelHandlerContext ctx, String requestString,
			Map<String, Object> paramObject, final String logId) {
		while (true) {
			try {
				ThreadPoolsFactory.getServiceExecutorPools().submit(
						() -> {
							Log4jUtils.initLog4jContext(logId);
							printInBoundMessage(request, requestString, ctx);
							try {
								String serviceId = handlerUri(url);
								String responseString = OnlineFacade.handler(request, ctx, serviceId, requestString,
										paramObject, logId);
								if (responseString != null) {
									HttpUtils.sendMessageJson(ctx, responseString);
								}
							} catch (Exception e) {
								ServiceResponse response = new ServiceResponse();
								response.setResult(SysErrorCode.S_900000.getErrCode());
								response.setMessage(SysErrorCode.S_900000.getErrMsg());
								HttpUtils.sendMessageJson(ctx, response.toString());
								e.printStackTrace();
							}
							Log4jUtils.clearLog4jContext();
						});
				break;
			} catch (RejectedExecutionException e) {

			} catch (Exception e) {
				ServiceResponse response = new ServiceResponse();
				response.setResult(SysErrorCode.S_900000.getErrCode());
				response.setMessage(SysErrorCode.S_900000.getErrMsg());
				e.printStackTrace();
				break;
			}
		}
	}

	private void printInBoundMessage(HttpRequest request, String requestString, ChannelHandlerContext ctx) {
		StringBuffer info = new StringBuffer("\n---------------------------\nInbound Message\n");
		info.append("ID: ").append(Log4jUtils.getLogId()).append("\n");
		
		info.append(request.uri()).append("\n");

		info.append("Http-Method: ").append(request.method().name()).append("\n");

		info.append("Content-Type: ").append(request.headers().get(HttpHeaderNames.CONTENT_TYPE)).append("\n");

		info.append("Headers: ").append(request.headers().entries()).append("\n");
		
		info.append("ClientIp: ").append(HttpUtils.getClientIp(request, ctx)).append("\n");

		info.append("Payload: ").append(requestString).append("\n---------------------------");
		
		logger.debug(info);
	}

	private String handlerUri(String url) {
		if(url.startsWith("http://")) {
			url = url.substring(7);
		} 
		if(url.startsWith("https://")) {
			url = url.substring(8);
		}
		String serviceId = url.substring(url.indexOf("/"));
		if (serviceId.indexOf("?") != -1) {
			serviceId = serviceId.substring(0, serviceId.indexOf("?"));
		}
		if (StringUtils.isBlank(serviceId)) {
			return "null";
		}
		return serviceId.substring(1);
	}

}
