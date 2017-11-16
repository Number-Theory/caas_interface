package com.caas.service.callback.safetyCall;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.BillingModel;
import com.caas.model.callback.safetycall.HwCallEvent;
import com.caas.model.callback.safetycall.SafetyCallBillModel;
import com.caas.model.callback.safetycall.SafetyCallHwBillModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

import io.netty.channel.ChannelHandlerContext;

@Service
public class HwBillCallBackservice extends DefaultServiceCallBack {
	@Autowired
	private CaasDao dao;

	private static final Logger logger = LogManager.getLogger(HwBillCallBackservice.class);

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response,
			Map<String, Object> paramsObject) {

		SafetyCallHwBillModel hwCallBackModel = JsonUtil.fromJson(request.getRequestString(),
				new TypeToken<SafetyCallHwBillModel>() {
				}.getType());

		logger.info("华为回调信息 hwCallBackModel={} ",JsonUtil.toJsonStr(hwCallBackModel));

		String reqAppkey = hwCallBackModel.getAppKey();

		String sys_Appkey = ConfigUtils.getProperty("appKey_hw", String.class);
		// 判斷是不是本應用對應的回調
		if (!sys_Appkey.equals(reqAppkey)) {
			logger.info("回调对应的appkey:{}与本平台配置的appkey:{}不一致!", reqAppkey,sys_Appkey);
			return;
		}
		//回调通知模式
		String callBackModel = hwCallBackModel.getCallEvent().getNotificationMode();
		// 事件類型
		String eventType = hwCallBackModel.getCallEvent().getEvent();
		
		if ("Notify".equals(callBackModel)) {
			dealEventHandle(eventType, hwCallBackModel);
		}
		if ("Block".equals(callBackModel)) {
			Map<String, Object> actions = new HashMap<>();
			Map<String, Object> params = new HashMap<>();
			List<Map<String, Object>> list = new ArrayList<>();
			//设置路由
			params.put("operation", "Record");
			list.add(params);
			params.put("operation", "vNumberRoute");
			params.put("routingAddress", hwCallBackModel.getCallEvent().getCalling());
			list.add(params);
			actions.put("actions", JsonUtil.toJsonStr(list));
			response.setOtherMap(actions);
		}
		
	}

	/**
	 *  处理华为事件回调
	 * @param eventType 事件类型
	 * @param hwCallBackModel 回调信息
	 */
	private void dealEventHandle(String eventType,SafetyCallHwBillModel hwCallBackModel) {
		switch (eventType) {
		case "IDP":
			logger.info("华为接口开始呼叫回调信息", hwCallBackModel);
			break;
		case "Answer":
			logger.info("华为接口应答回调信息", hwCallBackModel);
			break;
		case "Release":
			dealEvent(hwCallBackModel, "0");
			logger.info("华为接口呼叫結束回调信息", hwCallBackModel);
			break;
		case "Exception":
			dealEvent(hwCallBackModel, "1");
			logger.info("华为接口呼叫异常回调信息", hwCallBackModel);
			break;
		default:
			logger.info("华为接口回调事件匹配失败", hwCallBackModel);
			break;
		}
	}
	
	/**
	 * 呼叫结束事件、异常事件处理
	 * @param eventType  0：呼叫正常，1：呼叫异常
	 * @param extInfo
	 */
	private void dealEvent(SafetyCallHwBillModel hwBillModel,String eventType) {
		 	Map<String,Object> exParams =new HashMap<>();
			if (hwBillModel.getCallEvent().getExtInfo()!=null) {
				  exParams =hwBillModel.getCallEvent().getExtInfo().getExtParas();
			}
		  //通话时长。单位：秒
		  String duration = String.valueOf(exParams.get("Duration")) ;
		  //电话挂断原因
		  String releaseReason = String.valueOf(exParams.get("ReleaseReason"));
		  //请求唯一标识
		  String uniqueId = String.valueOf(exParams.get("UniqueId"));
		  //绑定id
		  String bindID = String.valueOf(exParams.get("BindID"));
		  //呼叫开始的时间戳
		  String startTime = DateUtil.dateStr2Str(String.valueOf(exParams.get("StartTime")), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd HH:mm:ss") ;
		  //呼叫结束时间
		  String endTime =DateUtil.addSecond(startTime, Double.valueOf(duration).intValue());
		  //根据绑定id查询绑定信息
		  Map<String, Object> orderBindMap = dao.selectOne("common.getBindOrder", bindID);
		  
		  BillingModel billingModel = new BillingModel();
		  
		  billingModel.setBeginTime(startTime);
		  billingModel.setBeginTimeB(startTime);
		  billingModel.setCalled(hwBillModel.getCallEvent().getCalled());
		  billingModel.setCalledDisplay(hwBillModel.getCallEvent().getVirtualNumber());
		  billingModel.setCaller(hwBillModel.getCallEvent().getCalling());
		  billingModel.setCallerDisplay(hwBillModel.getCallEvent().getVirtualNumber());
		  billingModel.setCallID((String) orderBindMap.get("requestId"));
		  billingModel.setCallTime(new BigDecimal(duration).longValue());
		  billingModel.setCallTimeB(new BigDecimal(duration).longValue());
		  billingModel.setEndTime(endTime);
		  billingModel.setEndTimeB(endTime);
		  billingModel.setCallStatus(eventType);
		  billingModel.setCallStatusB(eventType);
		  billingModel.setEvent("0");
		  billingModel.setMessage(releaseReason);
		  billingModel.setProductType("0");
		  billingModel.setRealityNumber(hwBillModel.getCallEvent().getCalled());
		  billingModel.setRecordType((String) orderBindMap.get("record"));
		  billingModel.setRecordUrl("");
		  billingModel.setUserId((String) orderBindMap.get("userId"));
		  //计费url
		  String billingUrl = ConfigUtils.getProperty("billingUrl", String.class);
		  try {
			  
			  	logger.info("华为话单扣费信息:params = {}", billingModel);
				HttpUtils.httpConnectionPost(billingUrl, JsonUtil.toJsonStr(billingModel));
				logger.info("话单扣費成功：{}", JsonUtil.toJsonStr(billingModel));
		  } catch (Exception e) {
				logger.error("话单扣费失败：{}", JsonUtil.toJsonStr(billingModel), e);
		  }
		  //获取回调客户地址
		  String billUrl = dao.selectOne("common.getBindOrderBillUrl", bindID);
		  if (StringUtil.isBlank(billUrl)) {
				logger.info("话单回调地址为空，不进行回调");
			} else {
				logger.info("话单回调地址billUrl={}，开始进行回调...", billUrl);

				SafetyCallBillModel safetyCallBillModel = new SafetyCallBillModel();
				safetyCallBillModel.setBindId((String) orderBindMap.get("bindId"));
				safetyCallBillModel.setCallee(hwBillModel.getCallEvent().getCalled());
				safetyCallBillModel.setCaller(hwBillModel.getCallEvent().getCalling());
				safetyCallBillModel.setCallId((String) orderBindMap.get("requestId"));
				safetyCallBillModel.setCallStatus(eventType);
				safetyCallBillModel.setCallTime(duration);
				safetyCallBillModel.setDstVirtualNum(hwBillModel.getCallEvent().getVirtualNumber());
				safetyCallBillModel.setEndTime(endTime);
				safetyCallBillModel.setRecordUrl("");
				safetyCallBillModel.setBeginTime(startTime);
				// safetyCallBillModel.setCalleeCityCode(calleeCityCode);
				safetyCallBillModel.setCalleeDisplay((String) orderBindMap.get("calleeDisplay"));
				safetyCallBillModel.setRecord((String) orderBindMap.get("record"));
				safetyCallBillModel.setUserData((String) orderBindMap.get("userData"));
				safetyCallBillModel.setUserId((String) orderBindMap.get("userId"));
				//TODO 华为 接口未标示
				safetyCallBillModel.setFlag("0");
				/*if ("10".equals(finishModel.getCalltype())) {
					safetyCallBillModel.setFlag("0");
				} else if ("11".equals(finishModel.getCalltype())) {
					safetyCallBillModel.setFlag("1");
				}*/
				if(exParams.get("ReleaseReason")!=null) {
					if ("Caller Hang up".equals(exParams.get("ReleaseReason")) || "Called Hang up".equals(exParams.get("ReleaseReason"))) { // 正常呼叫拆线     Caller Hang up： Called Hang up：
						safetyCallBillModel.setCallStatus("0");
					} else if ("Busy".equals(exParams.get("ReleaseReason"))) { // 用户忙  Busy
						safetyCallBillModel.setCallStatus("2");
					} else if ("Not Reachable".equals(exParams.get("Not Reachable"))) { // 用户未响应    被叫不可达
						safetyCallBillModel.setCallStatus("3");
					} else if ("No Answer".equals(exParams.get("No Answer"))) { // 用户未应答  No Answer： 被叫无应答
						safetyCallBillModel.setCallStatus("4");
					} else if ("Abandon".equals(exParams.get("Abandon"))) { // 用户缺席 主叫放弃 
						safetyCallBillModel.setCallStatus("4");
					} else if ("Call Terminated".equals(exParams.get("Call Terminated"))
							    ||"Call Forbidden".equals(exParams.get("Call Forbidden"))) { // 呼叫拒收  Call Terminated:  呼叫被终止    Call Forbidden： 呼叫被禁止 
						safetyCallBillModel.setCallStatus("5");
					} else { // 其他
						safetyCallBillModel.setCallStatus("7");   //呼叫被终止   Call Terminated：呼叫被禁止   Call Forbidden：
					}
				}
				try {
					logger.info("华为话单回调 info = {}", safetyCallBillModel);
					HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(safetyCallBillModel));
					logger.info("话单回调成功：{}", JsonUtil.toJsonStr(safetyCallBillModel));
				} catch (Exception e) {
					logger.error("话单回调失败：{}", JsonUtil.toJsonStr(safetyCallBillModel), e);
				}
			}
	}

}
