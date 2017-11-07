package com.caas.service;

import io.netty.channel.ChannelHandlerContext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caas.dao.CaasDao;
import com.caas.model.AuthModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.core.config.ConfigUtils;
import com.yzx.core.consts.EnumType.BusiErrorCode;
import com.yzx.core.util.DateUtil;
import com.yzx.core.util.EncryptUtil;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.StringUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;

/**
 * 
 * @author xupiao 2017年8月16日
 *
 */
@Service
public class AuthService extends DefaultServiceCallBack {
	private static Logger logger = LogManager.getLogger(AuthService.class);

	@Autowired
	private CaasDao dao;

	@Override
	public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
		AuthModel authBean = JsonUtil.fromJson(request.getRequestString(), new TypeToken<AuthModel>() {
		}.getType());

		String auth = authBean.getAuth();
		String userId = authBean.getUserID();
		EncryptUtil encryptUtil = new EncryptUtil();
		if (StringUtil.isEmpty(auth)) {
			logger.warn("主账号[" + userId + "]请求包头Authorization参数为空");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100009, AUTH_EVENT, authBean.getUserData());
			return;
		}
		try {
			auth = encryptUtil.base64Decoder(auth);
		} catch (Exception e) {
			logger.warn("主账号[" + userId + "]Authorization参数Base64解码失败");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100010, AUTH_EVENT, authBean.getUserData());
			return;
		}
		String[] auths = auth.split(":");
		if (auths.length != 2) {
			logger.warn("主账号[" + userId + "]Authorization参数Base64解码失败");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100010, AUTH_EVENT, authBean.getUserData());
			return;
		}
		if (StringUtil.isEmpty(auths[0])) {
			logger.warn("主账号[" + userId + "]Authorization参数解码后Token为空");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100011, AUTH_EVENT, authBean.getUserData());
			return;
		}
		if (StringUtil.isEmpty(auths[1])) {
			logger.warn("主账号[" + userId + "]Authorization参数解码后时间戳为空");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100012, AUTH_EVENT, authBean.getUserData());
			return;
		}
		if (StringUtils.isBlank(userId)) {
			logger.warn("主账户ID为空！");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100001, AUTH_EVENT, authBean.getUserData());
			return;
		}

		String regex = "^[a-zA-Z0-9]+$";
		boolean is = userId.matches(regex);
		if (!is) {
			logger.warn("主账号[" + authBean.getCallID() + "]存在非法字符");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100016, AUTH_EVENT, authBean.getUserData());
			return;
		}
		Long reqTime = 0L;
		try {
			reqTime = DateUtil.getTime(auths[1]);
		} catch (Exception ignore) {
			logger.warn("[" + auths[1] + "]Authorization参数解码后时间格式有误");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100017, AUTH_EVENT, authBean.getUserData());
			return;
		}
		Long timeOut = ConfigUtils.getProperty("auth.request.有效时间", 300L, Long.class);
		Long nowTime = new Date().getTime();
		if ((reqTime + timeOut * 1000) < nowTime || (reqTime - timeOut * 1000) > nowTime) {
			logger.warn("主账号[" + userId + "]Authorization参数解码后时间戳过期");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100018, AUTH_EVENT, authBean.getUserData());
			return;
		}

		// 账户校验：账户是否存在并可用、token是否正确
		Map<String, Object> userInfo = dao.selectOne("common.getUser", userId);
		if (userInfo == null || userInfo.isEmpty()) {
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100003, AUTH_EVENT, authBean.getUserData());
			logger.warn("主账户[" + userId + "]不存在！");
			return;
		}
		if (!"0".equals(userInfo.get("status"))) { // 账号被禁用
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100004, AUTH_EVENT, authBean.getUserData());
			logger.warn("主账户[" + userId + "]状态[" + userInfo.get("status") + "]已禁用！");
			return;
		}

		if (StringUtil.isBlank(userInfo.get("token"))) {
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100002, AUTH_EVENT, authBean.getUserData());
			logger.warn("主账户Token为空！");
			return;
		}

		String token = (String) userInfo.get("token");

		if (!token.toUpperCase().equals(auths[0].toUpperCase())) {
			logger.warn("主账号[" + userId + "]Authorization参数中账户token不正确");
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100015, AUTH_EVENT, authBean.getUserData());
			return;
		}

		// 应用验证：业务是否存在、可用
		Map<String, Object> appParams = new HashMap<String, Object>();
		appParams.put("userId", userId);
		appParams.put("productName", authBean.getProductType());
		Map<String, Object> appInfo = dao.selectOne("common.getApplication", appParams);
		if (appInfo == null || appInfo.isEmpty()) {
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100006, AUTH_EVENT, authBean.getUserData());
			logger.warn("主账户[" + userId + "]的业务[" + authBean.getProductType() + "]未开通！");
			return;
		}
		if (!"0".equals(appInfo.get("status"))) {
			setResponse(authBean.getCallID(), response, BusiErrorCode.B_100007, AUTH_EVENT, authBean.getUserData());
			logger.warn("主账户[" + userId + "]的业务[" + authBean.getProductType() + "]状态[" + userInfo.get("status") + "]已禁用！");
			return;
		}

		// 号码黑名单
		if (StringUtil.isNotEmpty(authBean.getCaller())) {
			String mobile = authBean.getCaller();
			int count = dao.selectOne("common.getBlackMobile", mobile);
			if (count > 0) {
				logger.warn("号码[" + authBean.getCaller() + "]为受保护的号码");
				setResponse(authBean.getCallID(), response, BusiErrorCode.B_100038, AUTH_EVENT, authBean.getUserData());
			}
		}
		if (StringUtil.isNotEmpty(authBean.getCallee())) {
			String mobile = authBean.getCallee();
			int count = dao.selectOne("common.getBlackMobile", mobile);
			if (count > 0) {
				logger.warn("号码[" + authBean.getCallee() + "]为受保护的号码");
				setResponse(authBean.getCallID(), response, BusiErrorCode.B_100038, AUTH_EVENT, authBean.getUserData());
			}
		}

		// 是否是服务器白名单
		if (StringUtil.isNotEmpty(authBean.getIpWhiteList())) {
			String whiteList = (String) appInfo.get("ipWhiteList");
			boolean flag = false;
			if (StringUtil.isNotEmpty(whiteList)) {
				String[] ipList = whiteList.split(";");
				for (String ip : ipList) {
					if (ip.equals(authBean.getIpWhiteList())) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					setResponse(authBean.getCallID(), response, BusiErrorCode.B_100008, AUTH_EVENT, authBean.getUserData());
					logger.warn("ip[" + authBean.getIpWhiteList() + "]不在服务器白名单中！");
					return;
				}
			}
		}

		// 预付费模式下账户余额是否充足
		String userType = (String) userInfo.get("userType"); // 0/预付费用户，1/后付费用户
		if (!"1".equals(userType) && "0".equals(authBean.getNeedBalance())) { // 预付费
			Map<String, Object> balanceMap = dao.selectOne("common.getBalance", userId);
			long balance = 0L;
			if ("1".equals(balanceMap.get("creditType"))) {
				balance = (long) balanceMap.get("balance") + (long) balanceMap.get("creditMoney");
			} else {
				balance = (long) balanceMap.get("balance");
			}
			if (balance <= 0L) {
				setResponse(authBean.getCallID(), response, BusiErrorCode.B_100019, AUTH_EVENT, authBean.getUserData());
				logger.warn("主账户[" + userId + "]的余额[" + balance + "]不足！");
				return;
			}
			logger.debug("余额校验通过");
		} else {
			logger.info("后付费模式不用校验余额");
		}

		setResponse(authBean.getCallID(), response, BusiErrorCode.B_000000, AUTH_EVENT, authBean.getUserData());
	}

}
