package com.caas.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.caas.model.BillingModel;
import com.yzx.engine.model.ServiceResponse;

/**
 * 语音通知计费、入库
 * 
 * @author xupiao 2017年8月17日
 *
 */
public class VoiceNotifyHandler extends DefaultBillingHandler {
	private static final Logger logger = LogManager.getLogger(VoiceNotifyHandler.class);

	@Override
	public void handler(BillingModel billingModel, ServiceResponse response) {
		
	}
}
