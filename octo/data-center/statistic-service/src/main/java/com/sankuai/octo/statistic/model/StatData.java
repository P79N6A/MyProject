package com.sankuai.octo.statistic.model;

import com.sankuai.octo.statistic.domain.StatTag;
import com.sankuai.sgagent.thrift.model.PerfCostDataList;

import java.io.Serializable;
import java.util.Map;

public class StatData implements Serializable {

    private String appkey;

    private int ts;

    private StatEnv env;

    private StatSource source;

    private StatRange range;

    private StatGroup group;

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

    private Map<String, Object> datas;

    private long updateTime;

    private String updateFrom;

    private PerfCostDataList costData;

    @Override
    public String toString() {
        return "StatData{" +
                "appkey='" + appkey + '\'' +
                ", ts=" + ts +
                ", env=" + env +
                ", source=" + source +
                ", range=" + range +
                ", group=" + group +
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
                ", datas=" + datas +
                ", updateTime=" + updateTime +
                ", updateFrom='" + updateFrom + '\'' +
                ", costData=" + costData +
                '}';
    }

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

    public StatSource getSource() {
        return source;
    }

    public void setSource(StatSource source) {
        this.source = source;
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

    public Map<String, Object> getDatas() {
        return datas;
    }

    public void setDatas(Map<String, Object> datas) {
        this.datas = datas;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public String getUpdateFrom() {
        return updateFrom;
    }

    public void setUpdateFrom(String updateFrom) {
        this.updateFrom = updateFrom;
    }

    public PerfCostDataList getCostData() {
        return costData;
    }

    public void setCostData(PerfCostDataList costData) {
        this.costData = costData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StatData statData = (StatData) o;

        if (HTTP2XXCount != statData.HTTP2XXCount) return false;
        if (HTTP3XXCount != statData.HTTP3XXCount) return false;
        if (HTTP4XXCount != statData.HTTP4XXCount) return false;
        if (HTTP5XXCount != statData.HTTP5XXCount) return false;
        if (Double.compare(statData.cost50, cost50) != 0) return false;
        if (Double.compare(statData.cost75, cost75) != 0) return false;
        if (Double.compare(statData.cost90, cost90) != 0) return false;
        if (Double.compare(statData.cost95, cost95) != 0) return false;
        if (Double.compare(statData.cost98, cost98) != 0) return false;
        if (Double.compare(statData.cost99, cost99) != 0) return false;
        if (Double.compare(statData.cost999, cost999) != 0) return false;
        if (Double.compare(statData.costMax, costMax) != 0) return false;
        if (Double.compare(statData.costMean, costMean) != 0) return false;
        if (Double.compare(statData.costMin, costMin) != 0) return false;
        if (count != statData.count) return false;
        if (dropCount != statData.dropCount) return false;
        if (exceptionCount != statData.exceptionCount) return false;
        if (Double.compare(statData.qps, qps) != 0) return false;
        if (successCount != statData.successCount) return false;
        if (timeoutCount != statData.timeoutCount) return false;
        if (ts != statData.ts) return false;
        if (updateTime != statData.updateTime) return false;
        if (appkey != null ? !appkey.equals(statData.appkey) : statData.appkey != null) return false;
        if (costData != null ? !costData.equals(statData.costData) : statData.costData != null) return false;
        if (datas != null ? !datas.equals(statData.datas) : statData.datas != null) return false;
        if (env != statData.env) return false;
        if (group != statData.group) return false;
        if (perfProtocolType != statData.perfProtocolType) return false;
        if (range != statData.range) return false;
        if (source != statData.source) return false;
        if (tags != null ? !tags.equals(statData.tags) : statData.tags != null) return false;
        if (updateFrom != null ? !updateFrom.equals(statData.updateFrom) : statData.updateFrom != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = appkey != null ? appkey.hashCode() : 0;
        result = 31 * result + ts;
        result = 31 * result + (env != null ? env.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (range != null ? range.hashCode() : 0);
        result = 31 * result + (group != null ? group.hashCode() : 0);
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
        result = 31 * result + (datas != null ? datas.hashCode() : 0);
        result = 31 * result + (int) (updateTime ^ (updateTime >>> 32));
        result = 31 * result + (updateFrom != null ? updateFrom.hashCode() : 0);
        result = 31 * result + (costData != null ? costData.hashCode() : 0);
        return result;
    }
}
