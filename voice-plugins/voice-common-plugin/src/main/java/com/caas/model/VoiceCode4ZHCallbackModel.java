package com.caas.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Jweikai on 2017/9/17.
 */
@XmlRootElement(name = "vcresp")
public class VoiceCode4ZHCallbackModel extends BaseModel{

    private static final long serialVersionUID = -211673312378178501L;

    private String result;
    private String desc;
    private String shownum;
    private String recid;

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

    public String getShownum() {
        return shownum;
    }

    public void setShownum(String shownum) {
        this.shownum = shownum;
    }

    public String getRecid() {
        return recid;
    }

    public void setRecid(String recid) {
        this.recid = recid;
    }
}
