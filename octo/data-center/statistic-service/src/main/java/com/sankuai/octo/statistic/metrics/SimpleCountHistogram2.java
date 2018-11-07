package com.sankuai.octo.statistic.metrics;

import com.meituan.mtrace.thrift.model.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * 简单的 计数 统计
 * 支持时间间隔的次数统计
 */
public class SimpleCountHistogram2 implements Metric, Sampling, Counting, Persistence, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCountHistogram2.class);
    protected long count;
    protected long successCount;
    protected long exceptionCount;
    protected long timeoutCount;
    protected long dropCount;

    protected long HTTP2XXCount;
    protected long HTTP3XXCount;
    protected long HTTP4XXCount;
    protected long HTTP5XXCount;

    protected long version;
    protected long createTime;
    protected long updateTime;
    private SimpleCountReservoir2 reservoir;

    public SimpleCountHistogram2() {
        this.reservoir = new SimpleCountReservoir2();
        this.count = 0L;
        this.successCount = 0L;
        this.exceptionCount = 0L;
        this.timeoutCount = 0L;
        this.dropCount = 0L;

        this.HTTP2XXCount = 0L;
        this.HTTP3XXCount = 0L;
        this.HTTP4XXCount = 0L;
        this.HTTP5XXCount = 0L;

        this.version = 0L;
        this.createTime = System.currentTimeMillis();
        this.updateTime = System.currentTimeMillis();
    }

    public SimpleCountHistogram2(long count,long successCount,long exceptionCount,long timeoutCount,long dropCount,
                                 long HTTP2XXCount,long HTTP3XXCount,long HTTP4XXCount,long HTTP5XXCount,
                                 long version,long createTime,long updateTime,
                                 SimpleCountReservoir2 reservoir) {

        this.count = count;
        this.successCount = successCount;
        this.exceptionCount = exceptionCount;
        this.timeoutCount = timeoutCount;
        this.dropCount = dropCount;
        this.HTTP2XXCount = HTTP2XXCount;
        this.HTTP3XXCount = HTTP3XXCount;
        this.HTTP4XXCount = HTTP4XXCount;
        this.HTTP5XXCount = HTTP5XXCount;
        this.version = version;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.reservoir = reservoir;
    }

    public void update(int value) {
        update(value, 1L, StatusCode.SUCCESS);
    }

    /**
     * @param value      耗时
     * @param valueCount 该耗时对应的的次数
     * @param status     状态
     */
    public void update(int value, long valueCount, StatusCode status) {
        if (valueCount > 0) {
            // 总量count
            count += valueCount;
            reservoir.update(value, valueCount);
            // 计算其他count
            switch (status) {
                case SUCCESS:
                    successCount += valueCount;
                    break;
                case EXCEPTION:
                    exceptionCount += valueCount;
                    break;
                case TIMEOUT:
                    timeoutCount += valueCount;
                    break;
                case DROP:
                    dropCount += valueCount;
                    break;
                case HTTP_2XX:
                    HTTP2XXCount += valueCount;
                    break;
                case HTTP_3XX:
                    HTTP3XXCount += valueCount;
                    break;
                case HTTP_4XX:
                    HTTP4XXCount += valueCount;
                    break;
                case HTTP_5XX:
                    HTTP5XXCount += valueCount;
                    break;
                default:
                    successCount += valueCount;
                    break;
            }
            updateTime = System.currentTimeMillis();
        }

    }

    @Override
    public SimpleCountSnapshot2 getSnapshot() {
        return reservoir.getSnapshot();
    }

    public void incrVersion() {
        version += 1;
    }

    public boolean newerThan(SimpleCountHistogram2 other) {
        return this.version >= other.version && this.updateTime >= other.updateTime;
    }

    public void dump(OutputStream stream) throws IOException {
        try (DataOutputStream writer = new DataOutputStream(stream)) {
            try {
                reservoir.dump(writer);
                writer.writeLong(count);
                writer.writeLong(version);
                writer.writeLong(createTime);
                writer.writeLong(updateTime);

                writer.writeLong(successCount);
                writer.writeLong(exceptionCount);
                writer.writeLong(timeoutCount);
                writer.writeLong(dropCount);

                writer.writeLong(HTTP2XXCount);
                writer.writeLong(HTTP3XXCount);
                writer.writeLong(HTTP4XXCount);
                writer.writeLong(HTTP5XXCount);
            } catch (Exception e) {
                LOG.error("SimpleCountHistogram dump failed...", e);
            }
        }
    }

    public void init(InputStream stream) throws IOException {
        try (DataInputStream reader = new DataInputStream(stream)) {
            try {
                this.reservoir.init(reader);
                long count = reader.readLong();
                this.count = count;
                this.version = reader.readLong();
                this.createTime = reader.readLong();
                this.updateTime = reader.readLong();

                try {
                    this.successCount = reader.readLong();

                    this.exceptionCount = reader.readLong();

                    this.timeoutCount = reader.readLong();

                    this.dropCount = reader.readLong();

                    this.HTTP2XXCount = reader.readLong();

                    this.HTTP3XXCount = reader.readLong();

                    this.HTTP4XXCount = reader.readLong();

                    this.HTTP5XXCount = reader.readLong();
                } catch (Exception e) {
                    LOG.info("兼容旧数据初始化");

                    this.successCount = count;

                    this.exceptionCount = 0;

                    this.timeoutCount = 0;

                    this.dropCount = 0;

                    this.HTTP2XXCount = 0;

                    this.HTTP3XXCount = 0;

                    this.HTTP4XXCount = 0;

                    this.HTTP5XXCount = 0;
                }
            } catch (Exception e) {
                LOG.error("SimpleCountHistogram init failed...", e.getMessage());
            }
        }
    }

    public int size() {
        return reservoir.size();
    }

    public SimpleCountHistogram2 merge(SimpleCountHistogram2 input) {
        this.count = this.count + input.getCount();
        this.successCount =  this.successCount + input.getSuccessCount();
        this.exceptionCount = this.exceptionCount + input.getExceptionCount();
        this.timeoutCount = this.timeoutCount + input.getTimeoutCount();
        this.dropCount = this.dropCount + input.getDropCount();
        this.HTTP2XXCount =  this.HTTP2XXCount + input.getHTTP2XXCount();
        this.HTTP3XXCount = this.HTTP3XXCount + input.getHTTP3XXCount();
        this.HTTP4XXCount = this.HTTP4XXCount + input.getHTTP4XXCount();
        this.HTTP5XXCount = this.HTTP5XXCount + input.getHTTP5XXCount();

        SimpleCountReservoir2 inputReservoir = input.getReservoir();
        ConcurrentMap inputCost2Count = inputReservoir.getValues();
        Iterator<Map.Entry<Integer, Long>> it = inputCost2Count.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            this.reservoir.update(entry.getKey(), entry.getValue());
        }
        if(this.reservoir.getMax()<inputReservoir.getMax()){
            this.reservoir.setMax(inputReservoir.getMax());
        }
        return this;
    }

    @Override
    public long getCount() {
        return count;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public long getExceptionCount() {
        return exceptionCount;
    }

    public long getTimeoutCount() {
        return timeoutCount;
    }

    public long getDropCount() {
        return dropCount;
    }

    public long getHTTP2XXCount() {
        return HTTP2XXCount;
    }

    public long getHTTP3XXCount() {
        return HTTP3XXCount;
    }

    public long getHTTP4XXCount() {
        return HTTP4XXCount;
    }

    public long getHTTP5XXCount() {
        return HTTP5XXCount;
    }

    public SimpleCountReservoir2 getReservoir() {
        return reservoir;
    }

    public long getVersion() {
        return version;
    }

    public long getCreateTime() {
        return createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}