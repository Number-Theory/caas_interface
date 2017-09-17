package com.caas.model;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Jweikai on 2017/9/17.
 */
@XmlRootElement(name = "vc")
public class Voice4ZHModel extends BaseModel {

    private static final long serialVersionUID = 4751305911231687431L;

    private String appid;
    private String called;
    private String extkey;
    private String extkey2;
    private String url;
    private String extparam;
    private String tid = "0";
    private String calling;
    private String repeat = "2";

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getCalled() {
        return called;
    }

    public void setCalled(String called) {
        this.called = called;
    }

    public String getExtkey() {
        return extkey;
    }

    public void setExtkey(String extkey) {
        this.extkey = extkey;
    }

    public String getExtkey2() {
        return extkey2;
    }

    public void setExtkey2(String extkey2) {
        this.extkey2 = extkey2;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExtparam() {
        return extparam;
    }

    public void setExtparam(String extparam) {
        this.extparam = extparam;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getCalling() {
        return calling;
    }

    public void setCalling(String calling) {
        this.calling = calling;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }
}
