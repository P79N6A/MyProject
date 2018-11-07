package com.sankuai.octo.msgp.domain.coverage;

import java.util.List;

/**
 * Created by huoyanyu on 2017/7/26.
 */
public class ServiceCoverageOutlinePeriod {
    private String owt;
    private List<String> rates;
    private List<Integer> sums;
    private List<Integer> counts;
    private String cmptVal;

    public String getCmptVal() {
        return cmptVal;
    }

    public void setCmptVal(String cmptVal) {
        this.cmptVal = cmptVal;
    }

    public String getOwt() {
        return owt;
    }

    public void setOwt(String owt) {
        this.owt = owt;
    }

    public List<String> getRates() {
        return rates;
    }

    public void setRates(List<String> rates) {
        this.rates = rates;
    }

    public List<Integer> getSums() {
        return sums;
    }

    public void setSums(List<Integer> sums) {
        this.sums = sums;
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public void setCounts(List<Integer> counts) {
        this.counts = counts;
    }
}
