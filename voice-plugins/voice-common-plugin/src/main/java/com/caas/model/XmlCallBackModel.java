package com.caas.model;

import com.yzx.core.consts.EnumType;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Jweikai on 2017/9/17.
 */
@XmlRootElement(name = "vcresp")
public class XmlCallBackModel extends BaseModel{
    private static final long serialVersionUID = 645624162166951835L;

    private String result;
    private String desc;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public XmlCallBackModel(){
        result = "0";
        desc = "成功";
    };

    public XmlCallBackModel(EnumType.BusiErrorCode errorCode) {
        result = errorCode.getErrCode();
        desc = errorCode.getErrMsg();
    }
}
