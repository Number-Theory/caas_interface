package com.caas.service;

import com.caas.model.BillingModel;
import com.yzx.engine.model.ServiceResponse;

/**
 * 
 * @author xupiao 2017年8月17日
 *
 */
public interface BillingHandler {
	public void handler(BillingModel billingModel, ServiceResponse response);
}
