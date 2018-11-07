package com.sankuai.octo.statistic.model;

import com.sankuai.octo.statistic.domain.StatTag;
import com.sankuai.sgagent.thrift.model.PerfCostDataList;

import java.io.Serializable;

public class PerfData implements Serializable {

    private String appkey;

    private int ts;

    private StatEnv env;

    private StatRange range;

    private StatGroup group;

    private PerfRole role;

    private PerfDataType dataType;

    private PerfProtocolType perfProtocolType;

    private StatTag tags;

    private long count;

    private long successCount;

    private long exceptionCount;

    private long timeoutCount;

    private long dropCount;

    private long HTTP2XXCount;

    private long HTTP3XXCount;

    private long HTTP4XXCount;

    private long HTTP5XXCount;

    private double qps;

    private double costMin;

    private double costMean;

    private double cost50;

    private double cost75;

    private double cost90;

    private double cost95;

    private double cost98;

    private double cost99;

    private double cost999;

    private double costMax;

    private PerfCostDataList costData;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public int getTs() {
        return ts;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public StatEnv getEnv() {
        return env;
    }

    public void setEnv(StatEnv env) {
        this.env = env;
    }

    public StatRange getRange() {
        return range;
    }

    public void setRange(StatRange range) {
        this.range = range;
    }

    public StatGroup getGroup() {
        return group;
    }

    public void setGroup(StatGroup group) {
        this.group = group;
    }

    public PerfRole getRole() {
        return role;
    }

    public void setRole(PerfRole role) {
        this.role = role;
    }

    public PerfDataType getDataType() {
        return dataType;
    }

    public void setDataType(PerfDataType dataType) {
        this.dataType = dataType;
    }

    public PerfProtocolType getPerfProtocolType() {
        return perfProtocolType;
    }

    public void setPerfProtocolType(PerfProtocolType perfProtocolType) {
        this.perfProtocolType = perfProtocolType;
    }

    public StatTag getTags() {
        return tags;
    }

    public void setTags(StatTag tags) {
        this.tags = tags;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getExceptionCount() {
        return exceptionCount;
    }

    public void setExceptionCount(long exceptionCount) {
        this.exceptionCount = exceptionCount;
    }

    public long getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(long timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public long getDropCount() {
        return dropCount;
    }

    public void setDropCount(long dropCount) {
        this.dropCount = dropCount;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }

    public double getCostMin() {
        return costMin;
    }

    public void setCostMin(double costMin) {
        this.costMin = costMin;
    }

    public double getCostMean() {
        return costMean;
    }

    public void setCostMean(double costMean) {
        this.costMean = costMean;
    }

    public double getCost50() {
        return cost50;
    }

    public void setCost50(double cost50) {
        this.cost50 = cost50;
    }

    public double getCost75() {
        return cost75;
    }

    public void setCost75(double cost75) {
        this.cost75 = cost75;
    }

    public double getCost90() {
        return cost90;
    }

    public void setCost90(double cost90) {
        this.cost90 = cost90;
    }

    public double getCost95() {
        return cost95;
    }

    public void setCost95(double cost95) {
        this.cost95 = cost95;
    }

    public double getCost98() {
        return cost98;
    }

    public void setCost98(double cost98) {
        this.cost98 = cost98;
    }

    public double getCost99() {
        return cost99;
    }

    public void setCost99(double cost99) {
        this.cost99 = cost99;
    }

    public double getCost999() {
        return cost999;
    }

    public void setCost999(double cost999) {
        this.cost999 = cost999;
    }

    public double getCostMax() {
        return costMax;
    }

    public void setCostMax(double costMax) {
        this.costMax = costMax;
    }

    public PerfCostDataList getCostData() {
        return costData;
    }

    public void setCostData(PerfCostDataList costData) {
        this.costData = costData;
    }

    public long getHTTP2XXCount() {
        return HTTP2XXCount;
    }

    public void setHTTP2XXCount(long HTTP2XXCount) {
        this.HTTP2XXCount = HTTP2XXCount;
    }

    public long getHTTP3XXCount() {
        return HTTP3XXCount;
    }

    public void setHTTP3XXCount(long HTTP3XXCount) {
        this.HTTP3XXCount = HTTP3XXCount;
    }

    public long getHTTP4XXCount() {
        return HTTP4XXCount;
    }

    public void setHTTP4XXCount(long HTTP4XXCount) {
        this.HTTP4XXCount = HTTP4XXCount;
    }

    public long getHTTP5XXCount() {
        return HTTP5XXCount;
    }

    public void setHTTP5XXCount(long HTTP5XXCount) {
        this.HTTP5XXCount = HTTP5XXCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PerfData perfData = (PerfData) o;

        if (HTTP2XXCount != perfData.HTTP2XXCount) return false;
        if (HTTP3XXCount != perfData.HTTP3XXCount) return false;
        if (HTTP4XXCount != perfData.HTTP4XXCount) return false;
        if (HTTP5XXCount != perfData.HTTP5XXCount) return false;
        if (Double.compare(perfData.cost50, cost50) != 0) return false;
        if (Double.compare(perfData.cost75, cost75) != 0) return false;
        if (Double.compare(perfData.cost90, cost90) != 0) return false;
        if (Double.compare(perfData.cost95, cost95) != 0) return false;
        if (Double.compare(perfData.cost98, cost98) != 0) return false;
        if (Double.compare(perfData.cost99, cost99) != 0) return false;
        if (Double.compare(perfData.cost999, cost999) != 0) return false;
        if (Double.compare(perfData.costMax, costMax) != 0) return false;
        if (Double.compare(perfData.costMean, costMean) != 0) return false;
        if (Double.compare(perfData.costMin, costMin) != 0) return false;
        if (count != perfData.count) return false;
        if (dropCount != perfData.dropCount) return false;
        if (exceptionCount != perfData.exceptionCount) return false;
        if (Double.compare(perfData.qps, qps) != 0) return false;
        if (successCount != perfData.successCount) return false;
        if (timeoutCount != perfData.timeoutCount) return false;
        if (ts != perfData.ts) return false;
        if (appkey != null ? !appkey.equals(perfData.appkey) : perfData.appkey != null) return false;
        if (costData != null ? !costData.equals(perfData.costData) : perfData.costData != null) return false;
        if (dataType != perfData.dataType) return false;
        if (env != perfData.env) return false;
        if (group != perfData.group) return false;
        if (perfProtocolType != perfData.perfProtocolType) return false;
        if (range != perfData.range) return false;
        if (role != perfData.role) return false;
        if (tags != null ? !tags.equals(perfData.tags) : perfData.tags != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = appkey != null ? appkey.hashCode() : 0;
        result = 31 * result + ts;
        result = 31 * result + (env != null ? env.hashCode() : 0);
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (perfProtocolType != null ? perfProtocolType.hashCode() : 0);
        result = 31 * result + (tags != null ? tags.hashCode() : 0);
        result = 31 * result + (int) (count ^ (count >>> 32));
        result = 31 * result + (int) (successCount ^ (successCount >>> 32));
        result = 31 * result + (int) (exceptionCount ^ (exceptionCount >>> 32));
        result = 31 * result + (int) (timeoutCount ^ (timeoutCount >>> 32));
        result = 31 * result + (int) (dropCount ^ (dropCount >>> 32));
        result = 31 * result + (int) (HTTP2XXCount ^ (HTTP2XXCount >>> 32));
        result = 31 * result + (int) (HTTP3XXCount ^ (HTTP3XXCount >>> 32));
        result = 31 * result + (int) (HTTP4XXCount ^ (HTTP4XXCount >>> 32));
        result = 31 * result + (int) (HTTP5XXCount ^ (HTTP5XXCount >>> 32));
        temp = Double.doubleToLongBits(qps);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(costMin);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(costMean);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost50);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost75);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost90);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost95);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost98);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost99);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(cost999);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(costMax);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (costData != null ? costData.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PerfData{" +
                "appkey='" + appkey + '\'' +
                ", ts=" + ts +
                ", env=" + env +
                ", range=" + range +
                ", group=" + group +
                ", role=" + role +
                ", dataType=" + dataType +
                ", perfProtocolType=" + perfProtocolType +
                ", tags=" + tags +
                ", count=" + count +
                ", successCount=" + successCount +
                ", exceptionCount=" + exceptionCount +
                ", timeoutCount=" + timeoutCount +
                ", dropCount=" + dropCount +
                ", HTTP2XXCount=" + HTTP2XXCount +
                ", HTTP3XXCount=" + HTTP3XXCount +
                ", HTTP4XXCount=" + HTTP4XXCount +
                ", HTTP5XXCount=" + HTTP5XXCount +
                ", qps=" + qps +
                ", costMin=" + costMin +
                ", costMean=" + costMean +
                ", cost50=" + cost50 +
                ", cost75=" + cost75 +
                ", cost90=" + cost90 +
                ", cost95=" + cost95 +
                ", cost98=" + cost98 +
                ", cost99=" + cost99 +
                ", cost999=" + cost999 +
                ", costMax=" + costMax +
                ", costData=" + costData +
                '}';
    }
}
