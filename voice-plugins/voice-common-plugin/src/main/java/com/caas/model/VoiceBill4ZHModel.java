package com.caas.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by Jweikai on 2017/9/17.
 */
@XmlRootElement(name = "vc")
public class VoiceBill4ZHModel extends BaseModel{

    private static final long serialVersionUID = -7939372975625936265L;

    private String appid;
    private List<Bill> bills;

    @XmlElementWrapper(name = "bills")
    @XmlElement(name = "bill")
    public List<Bill> getBills() {
        return bills;
    }

    public void setBills(List<Bill> bills) {
        this.bills = bills;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public static class Bill {
        private String recid;
        private String appid;
        private String caller;
        private String called;
        private String starttime;
        private String endtime;
        private String status;
        private String asr;
        private String extparam;

        public String getRecid() {
            return recid;
        }

        public void setRecid(String recid) {
            this.recid = recid;
        }

        public String getAppid() {
            return appid;
        }

        public void setAppid(String appid) {
            this.appid = appid;
        }

        public String getCaller() {
            return caller;
        }

        public void setCaller(String caller) {
            this.caller = caller;
        }

        public String getCalled() {
            return called;
        }

        public void setCalled(String called) {
            this.called = called;
        }

        public String getStarttime() {
            return starttime;
        }

        public void setStarttime(String starttime) {
            this.starttime = starttime;
        }

        public String getEndtime() {
            return endtime;
        }

        public void setEndtime(String endtime) {
            this.endtime = endtime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getAsr() {
            return asr;
        }

        public void setAsr(String asr) {
            this.asr = asr;
        }

        public String getExtparam() {
            return extparam;
        }

        public void setExtparam(String extparam) {
            this.extparam = extparam;
        }
    }

}
