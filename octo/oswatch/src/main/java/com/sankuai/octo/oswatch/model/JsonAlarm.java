package com.sankuai.octo.oswatch.model;

/**
 * Created by chenxi on 7/30/15.
 */
public class JsonAlarm {
    String id;
    String providerAppkey;
    int env;
    String method;
    int providerQPSCapacity;
    String consumerAppkey;
    int consumerCurrentQPS;
    int consumerQuotaQPS;
    double degradeRatio;
    int degradeStrategy;
    int degradeEnd;
    int status;//1:alarm 2:degrade 3:alarm and degrade
    long timestamp;

    public JsonAlarm(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDegradeEnd() {
        return degradeEnd;
    }

    public void setDegradeEnd(int degradeEnd) {
        this.degradeEnd = degradeEnd;
    }

    public int getEnv() {
        return env;
    }

    public void setEnv(int env) {
        this.env = env;
    }

    public String getProviderAppkey() {
        return providerAppkey;
    }

    public void setProviderAppkey(String providerAppkey) {
        this.providerAppkey = providerAppkey;
    }

    public String getConsumerAppkey() {
        return consumerAppkey;
    }

    public void setConsumerAppkey(String consumerAppkey) {
        this.consumerAppkey = consumerAppkey;
    }

    public int getProviderQPSCapacity() {
        return providerQPSCapacity;
    }

    public void setProviderQPSCapacity(int providerQPSCapacity) {
        this.providerQPSCapacity = providerQPSCapacity;
    }

    public int getConsumerCurrentQPS() {
        return consumerCurrentQPS;
    }

    public void setConsumerCurrentQPS(int consumerCurrentQPS) {
        this.consumerCurrentQPS = consumerCurrentQPS;
    }

    public int getConsumerQuotaQPS() {
        return consumerQuotaQPS;
    }

    public void setConsumerQuotaQPS(int consumerQuotaQPS) {
        this.consumerQuotaQPS = consumerQuotaQPS;
    }

    public void setDegradeRatio(double degradeRatio) {
        this.degradeRatio = degradeRatio;
    }

    public Double getDegradeRatio() {
        return degradeRatio;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getDegradeStrategy() {
        return degradeStrategy;
    }

    public void setDegradeStrategy(int degradeStrategy) {
        this.degradeStrategy = degradeStrategy;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
