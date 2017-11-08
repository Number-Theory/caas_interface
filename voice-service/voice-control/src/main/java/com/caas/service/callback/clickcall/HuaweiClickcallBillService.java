package com.caas.service.callback.clickcall;

import io.netty.channel.ChannelHandlerContext;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.huawei.FeeInfo;
import com.caas.model.BillingModel;
import com.caas.model.ClickCallBillModel;
import com.caas.model.ClickCallModel;
import com.caas.model.HuaweiClickCallBillModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.client.HttpUtils;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import com.yzx.redis.RedisKeyConsts;
import com.yzx.redis.RedisOpClient;

/**
 * 
 * @author xupiao 2017年11月2日
 *
 */
@Service
public class HuaweiClickcallBillService extends DefaultServiceCallBack {

	private static final Logger logger = LogManager.getLogger(HuaweiClickcallBillService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		HuaweiClickCallBillModel huaweiClickCallBillModel = JsonUtil.fromJson(request.getRequestString(), new TypeToken<HuaweiClickCallBillModel>() {
		}.getType());
		logger.info("【华为点击呼叫话单回调接口】接收到华为点击呼叫话单回调请求内容：{}", huaweiClickCallBillModel);

		if ("fee".equals(huaweiClickCallBillModel.getEventType())) {
			List<FeeInfo> feeInfos = huaweiClickCallBillModel.getFeeLst();
			for (FeeInfo feeInfo : feeInfos) {
				String sessionId = feeInfo.getSessionId();
				String clickCallModelString = RedisOpClient.get(RedisKeyConsts.getKey(RedisKeyConsts.CB_REQUEST, sessionId));
				ClickCallModel clickCallModel = JsonUtil.fromJson(clickCallModelString, new TypeToken<ClickCallModel>() {
				}.getType());

				BillingModel billingModel = new BillingModel();
				String nowDataString = DateUtil.getNow(DateUtil.TIME_FORMAT_Y_M_D);
				if (StringUtils.isBlank(feeInfo.getCallOutAnswerTime())) {
					billingModel.setBeginTime(nowDataString);
				} else {
					billingModel.setBeginTime(utc2Local(feeInfo.getCallOutAnswerTime(), DateUtil.TIME_FORMAT_Y_M_D, DateUtil.TIME_FORMAT_Y_M_D));
				}
				if (StringUtil.isBlank(feeInfo.getFwdAnswerTime())) {
					billingModel.setBeginTimeB(nowDataString);
				} else {
					billingModel.setBeginTimeB(utc2Local(feeInfo.getFwdAnswerTime(), DateUtil.TIME_FORMAT_Y_M_D, DateUtil.TIME_FORMAT_Y_M_D));
				}
				if (StringUtil.isBlank(feeInfo.getFwdDstNum())) {
					billingModel.setCalled(clickCallModel.getCalled());
				} else {
					billingModel.setCalled(removeMobileNationPrefix(feeInfo.getFwdDstNum()));
				}
				if (StringUtil.isBlank(feeInfo.getFwdDisplayNum())) {
					billingModel.setCalledDisplay(clickCallModel.getDisplayCalled());
				} else {
					billingModel.setCalledDisplay(removeMobileNationPrefix(feeInfo.getFwdDisplayNum()));
				}
				if (StringUtil.isBlank(feeInfo.getCalleeNum())) {
					billingModel.setCaller(clickCallModel.getCaller());
				} else {
					billingModel.setCaller(removeMobileNationPrefix(feeInfo.getCalleeNum()));
				}
				if (StringUtil.isBlank(feeInfo.getCallerNum())) {
					billingModel.setCallerDisplay(clickCallModel.getDisplayCaller());
				} else {
					billingModel.setCallerDisplay(removeMobileNationPrefix(feeInfo.getCallerNum()));
				}
				billingModel.setCallID(clickCallModel.getCallId());
				Long callTime = 0L, callTimeB = 0L;
				if ("31".equals(feeInfo.getCallOutUnaswRsn())) {
					billingModel.setCallStatus("0");
				} else {
					billingModel.setCallStatus("1");
				}
				if ("31".equals(feeInfo.getFwdUnaswRsn())) {
					billingModel.setCallStatusB("0");
				} else {
					billingModel.setCallStatusB("1");
				}
				try {
					if (StringUtil.isNotEmpty(feeInfo.getCallEndTime()) && StringUtil.isNotEmpty(feeInfo.getCallOutAnswerTime())) {
						callTime = DateUtil.getTime(feeInfo.getCallEndTime(), "yyyy-MM-dd HH:mm:ss")
								- DateUtil.getTime(feeInfo.getCallOutAnswerTime(), "yyyy-MM-dd HH:mm:ss");
					}
					if (StringUtil.isNotEmpty(feeInfo.getCallEndTime()) && StringUtil.isNotEmpty(feeInfo.getFwdAnswerTime())) {
						callTimeB = DateUtil.getTime(feeInfo.getCallEndTime(), "yyyy-MM-dd HH:mm:ss")
								- DateUtil.getTime(feeInfo.getFwdAnswerTime(), "yyyy-MM-dd HH:mm:ss");
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				billingModel.setCallTime(callTime);
				billingModel.setCallTimeB(callTimeB);
				billingModel.setEndTime(utc2Local(feeInfo.getCallEndTime(), DateUtil.TIME_FORMAT_Y_M_D, DateUtil.TIME_FORMAT_Y_M_D));
				billingModel.setEndTimeB(utc2Local(feeInfo.getCallEndTime(), DateUtil.TIME_FORMAT_Y_M_D, DateUtil.TIME_FORMAT_Y_M_D));
				billingModel.setEvent("0");
				billingModel.setMessage(feeInfo.getUlFailReason());
				billingModel.setProductType("2");
				billingModel.setRecordType((String) feeInfo.getRecordFlag());
				if (StringUtil.isNotEmpty(feeInfo.getRecordFileDownloadUrl())) {
					billingModel.setRecordUrl(feeInfo.getRecordFileDownloadUrl());
				} else {
					billingModel.setRecordUrl(feeInfo.getRecordDomain() + feeInfo.getRecordBucketName() + feeInfo.getRecordObjectName());
				}
				billingModel.setUserId(clickCallModel.getUserId());
				String billingUrl = ConfigUtils.getProperty("billingUrl", String.class);
				try {
					HttpUtils.httpConnectionPost(billingUrl, JsonUtil.toJsonStr(billingModel));
					logger.info("话单扣費成功：{}", JsonUtil.toJsonStr(billingModel));
				} catch (Exception e) {
					logger.error("话单扣费失败：{}", JsonUtil.toJsonStr(billingModel), e);
				}

				String billUrl = clickCallModel.getBillUrl();
				if (StringUtil.isBlank(billUrl)) {
					logger.info("话单回调地址为空，不进行回调");
				} else {

					logger.info("话单回调地址billUrl={}，开始进行回调...", billUrl);
					ClickCallBillModel clickCallBillModel = new ClickCallBillModel();
					clickCallBillModel.setCallId(clickCallModel.getUserId());
					clickCallBillModel.setCaller(clickCallModel.getCaller());
					clickCallBillModel.setCalled(clickCallModel.getCalled());
					clickCallBillModel.setCallerDisplay(billingModel.getCallerDisplay());
					clickCallBillModel.setCalledDisplay(billingModel.getCalledDisplay());
					if (StringUtil.isBlank(feeInfo.getCallInTime())) {
						clickCallBillModel.setCallTime(nowDataString);
					} else {
						clickCallBillModel.setCallTime(utc2Local(feeInfo.getCallInTime(), DateUtil.TIME_FORMAT_Y_M_D, DateUtil.TIME_FORMAT_Y_M_D));
					}
					clickCallBillModel.setBeginTimeA(billingModel.getBeginTime());
					clickCallBillModel.setBeginTimeB(billingModel.getBeginTimeB());
					clickCallBillModel.setRecord(clickCallModel.getRecord());
					if (StringUtil.isNotEmpty(feeInfo.getFwdUnaswRsn())) {
						if ("31".equals(feeInfo.getCallOutUnaswRsn())) { // 正常呼叫拆线
							clickCallBillModel.setCallStatusA("0");
						} else if ("17".equals(feeInfo.getCallOutUnaswRsn())) { // 用户忙
							clickCallBillModel.setCallStatusA("2");
						} else if ("18".equals(feeInfo.getCallOutUnaswRsn())) { // 用户未响应
							clickCallBillModel.setCallStatusA("3");
						} else if ("19".equals(feeInfo.getCallOutUnaswRsn())) { // 用户未应答
							clickCallBillModel.setCallStatusA("4");
						} else if ("20".equals(feeInfo.getCallOutUnaswRsn())) { // 用户缺席
							clickCallBillModel.setCallStatusA("4");
						} else if ("21".equals(feeInfo.getCallOutUnaswRsn())) { // 呼叫拒收
							clickCallBillModel.setCallStatusA("5");
						} else { // 其他
							clickCallBillModel.setCallStatusA("7");
						}
					} else {
						clickCallBillModel.setCallStatusA("7");
					}
					if (StringUtil.isNotEmpty(feeInfo.getFwdUnaswRsn())) {
						if ("31".equals(feeInfo.getFwdUnaswRsn())) { // 正常呼叫拆线
							clickCallBillModel.setCallStatusB("0");
						} else if ("17".equals(feeInfo.getFwdUnaswRsn())) { // 用户忙
							clickCallBillModel.setCallStatusB("2");
						} else if ("18".equals(feeInfo.getFwdUnaswRsn())) { // 用户未响应
							clickCallBillModel.setCallStatusB("3");
						} else if ("19".equals(feeInfo.getFwdUnaswRsn())) { // 用户未应答
							clickCallBillModel.setCallStatusB("4");
						} else if ("20".equals(feeInfo.getFwdUnaswRsn())) { // 用户缺席
							clickCallBillModel.setCallStatusB("4");
						} else if ("21".equals(feeInfo.getFwdUnaswRsn())) { // 呼叫拒收
							clickCallBillModel.setCallStatusB("5");
						} else { // 其他
							clickCallBillModel.setCallStatusB("7");
						}
					} else {
						clickCallBillModel.setCallStatusB("7");
					}
					clickCallBillModel.setUserData(clickCallModel.getUserData());
					try {
						HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(clickCallBillModel));
						logger.info("话单回调成功：{}", JsonUtil.toJsonStr(clickCallBillModel));
					} catch (Exception e) {
						logger.error("话单回调失败：{}", JsonUtil.toJsonStr(clickCallBillModel), e);
					}
				}

				String recordUrl = clickCallModel.getRecordUrl();
				if (StringUtil.isBlank(recordUrl) || "0".equals(clickCallModel.getRecord())) {
					logger.info("录音回调地址为空或不录音，不进行回调");
				} else {
					logger.info("录音回调地址recordUrl={}，开始进行回调...", recordUrl);

					Map<String, Object> recordCallback = new HashMap<String, Object>();
					recordCallback.put("callId", clickCallModel.getCallId());
					recordCallback.put("recordUrl", billingModel.getRecordUrl());
					recordCallback.put("userData", clickCallModel.getUserData());
					try {
						HttpUtils.httpConnectionPost(billUrl, JsonUtil.toJsonStr(recordCallback));
						logger.info("录音回调成功：{}", JsonUtil.toJsonStr(recordCallback));
					} catch (Exception e) {
						logger.error("录音回调失败：{}", JsonUtil.toJsonStr(recordCallback), e);
					}
				}
			}
		}
	}
}
