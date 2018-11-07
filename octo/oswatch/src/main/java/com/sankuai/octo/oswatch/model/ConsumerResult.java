package com.sankuai.octo.oswatch.model;

/**
 * Created by chenxi on 7/27/15.
 */
public class ConsumerResult {
    String consumerAppKey;
    double qpsAvg;

    public String getConsumerAppKey() {
        return consumerAppKey;
    }

    public void setConsumerAppKey(String consumerAppKey) {
        this.consumerAppKey = consumerAppKey;
    }

    public double getQpsAvg() {
        return qpsAvg;
    }

    public void setQpsAvg(int qpsAvg) {
        this.qpsAvg = qpsAvg;
    }
}
