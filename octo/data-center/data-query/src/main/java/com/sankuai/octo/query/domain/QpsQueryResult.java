package com.sankuai.octo.query.domain;

import java.util.List;

/**
 * Created by wujinwu on 15/7/21.
 */
public class QpsQueryResult {

    private String providerAppKey;

    private String spanName;

    private List<Consumer2Qps> consumer2QpsList;

    public QpsQueryResult() {
    }

    public QpsQueryResult(String providerAppKey, String spanName, List<Consumer2Qps> consumer2QpsList) {
        this.providerAppKey = providerAppKey;
        this.spanName = spanName;
        this.consumer2QpsList = consumer2QpsList;
    }

    public String getProviderAppKey() {
        return providerAppKey;
    }

    public void setProviderAppKey(String providerAppKey) {
        this.providerAppKey = providerAppKey;
    }

    public String getSpanName() {
        return spanName;
    }

    public void setSpanName(String spanName) {
        this.spanName = spanName;
    }

    public List<com.sankuai.octo.query.domain.QpsQueryResult.Consumer2Qps> getConsumer2QpsList() {
        return consumer2QpsList;
    }

    public void setConsumer2QpsList(List<com.sankuai.octo.query.domain.QpsQueryResult.Consumer2Qps> consumer2QpsList) {
        this.consumer2QpsList = consumer2QpsList;
    }

    public static class Consumer2Qps {

        private String consumerAppKey;

        private double qpsAvg;

        public Consumer2Qps() {
        }

        public Consumer2Qps(String consumerAppKey, double qpsAvg) {
            this.consumerAppKey = consumerAppKey;
            this.qpsAvg = qpsAvg;
        }

        public String getConsumerAppKey() {
            return consumerAppKey;
        }

        public void setConsumerAppKey(String consumerAppKey) {
            this.consumerAppKey = consumerAppKey;
        }

        public double getQpsAvg() {
            return qpsAvg;
        }

        public void setQpsAvg(double qpsAvg) {
            this.qpsAvg = qpsAvg;
        }
    }

}
