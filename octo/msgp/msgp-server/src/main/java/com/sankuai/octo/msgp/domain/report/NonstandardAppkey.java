package com.sankuai.octo.msgp.domain.report;

/**
 * Created by yves on 17/2/21.
 */
public class NonstandardAppkey {

    private String business;

    private String appkey;

    private int[] abnormityCodes;

    private String abnormityDescption;

    public NonstandardAppkey(String business, String appkey, int[] abnormityCodes, String abnormityDescption) {
        this.business = business;
        this.appkey = appkey;
        this.abnormityCodes = abnormityCodes;
        this.abnormityDescption = abnormityDescption;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public int[] getAbnormityCodes() {
        return abnormityCodes;
    }

    public void setAbnormityCodes(int[] abnormityCodes) {
        this.abnormityCodes = abnormityCodes;
    }

    public String getAbnormityDescption() {
        return abnormityDescption;
    }

    public void setAbnormityDescption(String abnormityDescption) {
        this.abnormityDescption = abnormityDescption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NonstandardAppkey)) return false;

        NonstandardAppkey that = (NonstandardAppkey) o;

        if (!getBusiness().equals(that.getBusiness())) return false;
        return getAppkey().equals(that.getAppkey());

    }

    @Override
    public int hashCode() {
        int result = getBusiness().hashCode();
        result = 31 * result + getAppkey().hashCode();
        return result;
    }
}
