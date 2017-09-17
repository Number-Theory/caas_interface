package com.caas.service.callback.voiceCode;

import com.caas.model.VoiceBill4ZHModel;
import com.caas.model.VoiceBill4ZHModel.Bill;
import com.caas.model.XmlCallBackModel;
import com.google.gson.reflect.TypeToken;
import com.yzx.access.util.HttpUtils;
import com.yzx.core.util.JsonUtil;
import com.yzx.core.util.XMLUtil;
import com.yzx.engine.model.ServiceRequest;
import com.yzx.engine.model.ServiceResponse;
import com.yzx.engine.spi.impl.DefaultServiceCallBack;
import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by Jweikai on 2017/9/17.
 */
@Service
public class VoiceBill4ZHService extends DefaultServiceCallBack {
    private static final Logger logger = LogManager.getLogger(VoiceBill4ZHService.class);

    @Override
    public void callService(ChannelHandlerContext ctx, ServiceRequest request, ServiceResponse response, Map<String, Object> paramsObject) {
        VoiceBill4ZHModel vcBill = JsonUtil.fromJson(request.getRequestString(), new TypeToken<VoiceBill4ZHModel>() {
        }.getType());
        Bill bill = vcBill.getBills().get(0);

        logger.info("【接收到Rest组件请求信息】vioceCodeBill4ZH={}", vcBill);

        //TODO 处理业务


        HttpUtils.sendMessageXml(ctx, XMLUtil.convertToXml(new XmlCallBackModel()));
    }
}
