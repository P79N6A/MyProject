package com.sankuai.octo.statistic.metrics;

import com.meituan.mtrace.thrift.model.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * 简单的 计数 统计
 * 支持时间间隔的次数统计
 */
public class SimpleCountHistogram implements Metric, Sampling, Counting, Persistence, Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCountHistogram.class);

    protected SimpleCountReservoir reservoir;
    protected LongAdder count;
    protected LongAdder successCount;
    protected LongAdder exceptionCount;
    protected LongAdder timeoutCount;
    protected LongAdder dropCount;

    protected LongAdder HTTP2XXCount;
    protected LongAdder HTTP3XXCount;
    protected LongAdder HTTP4XXCount;
    protected LongAdder HTTP5XXCount;


    protected long version;
    protected long createTime;
    protected long updateTime;

    public SimpleCountHistogram() {
        this.reservoir = new SimpleCountReservoir();
        this.count = new LongAdder();
        this.successCount = new LongAdder();
        this.exceptionCount = new LongAdder();
        this.timeoutCount = new LongAdder();
        this.dropCount = new LongAdder();
        this.HTTP2XXCount = new LongAdder();
        this.HTTP3XXCount = new LongAdder();
        this.HTTP4XXCount = new LongAdder();
        this.HTTP5XXCount = new LongAdder();
        this.version = 0L;
        this.updateTime = this.createTime = System.currentTimeMillis();
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
            count.add(valueCount);
            reservoir.update(value, valueCount);
            // 计算其他count
            switch (status) {
                case SUCCESS:
                    successCount.add(valueCount);
                    break;
                case EXCEPTION:
                    exceptionCount.add(valueCount);
                    break;
                case TIMEOUT:
                    timeoutCount.add(valueCount);
                    break;
                case DROP:
                    dropCount.add(valueCount);
                    break;
                case HTTP_2XX:
                    HTTP2XXCount.add(valueCount);
                    break;
                case HTTP_3XX:
                    HTTP3XXCount.add(valueCount);
                    break;
                case HTTP_4XX:
                    HTTP4XXCount.add(valueCount);
                    break;
                case HTTP_5XX:
                    HTTP5XXCount.add(valueCount);
                    break;
                default:
                    successCount.add(valueCount);
                    break;
            }
            updateTime = System.currentTimeMillis();
        }

    }

    @Override
    public long getCount() {
        return count.sum();
    }

    public long getSuccessCount() {
        return successCount.sum();
    }

    public long getExceptionCount() {
        return exceptionCount.sum();
    }

    public long getTimeoutCount() {
        return timeoutCount.sum();
    }

    public long getDropCount() {
        return dropCount.sum();
    }

    public long getHTTP2XXCount() {
        return HTTP2XXCount.sum();
    }

    public long getHTTP3XXCount() {
        return HTTP3XXCount.sum();
    }

    public long getHTTP4XXCount() {
        return HTTP4XXCount.sum();
    }

    public long getHTTP5XXCount() {
        return HTTP5XXCount.sum();
    }

    @Override
    public SimpleCountSnapshot getSnapshot() {
        return reservoir.getSnapshot();
    }

    public void incrVersion() {
        version += 1;
    }

    public boolean newerThan(SimpleCountHistogram other) {
        return this.version >= other.version && this.updateTime >= other.updateTime;
    }

    public void dump(OutputStream stream) throws IOException {
        try (DataOutputStream writer = new DataOutputStream(stream)) {
            try {
                reservoir.dump(writer);
                writer.writeLong(count.sum());
                writer.writeLong(version);
                writer.writeLong(createTime);
                writer.writeLong(updateTime);

                writer.writeLong(successCount.sum());
                writer.writeLong(exceptionCount.sum());
                writer.writeLong(timeoutCount.sum());
                writer.writeLong(dropCount.sum());

                writer.writeLong(HTTP2XXCount.sum());
                writer.writeLong(HTTP3XXCount.sum());
                writer.writeLong(HTTP4XXCount.sum());
                writer.writeLong(HTTP5XXCount.sum());


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
                this.count.reset();
                this.count.add(count);
                this.version = reader.readLong();
                this.createTime = reader.readLong();
                this.updateTime = reader.readLong();

                try {
                    long successCount = reader.readLong();
                    this.successCount.reset();
                    this.successCount.add(successCount);

                    long exceptionCount = reader.readLong();
                    this.exceptionCount.reset();
                    this.exceptionCount.add(exceptionCount);

                    long timeoutCount = reader.readLong();
                    this.timeoutCount.reset();
                    this.timeoutCount.add(timeoutCount);

                    long dropCount = reader.readLong();
                    this.dropCount.reset();
                    this.dropCount.add(dropCount);

                    long HTTP2XXCount = reader.readLong();
                    this.HTTP2XXCount.reset();
                    this.HTTP2XXCount.add(HTTP2XXCount);

                    long HTTP3XXCount = reader.readLong();
                    this.HTTP3XXCount.reset();
                    this.HTTP3XXCount.add(HTTP3XXCount);

                    long HTTP4XXCount = reader.readLong();
                    this.HTTP4XXCount.reset();
                    this.HTTP4XXCount.add(HTTP4XXCount);

                    long HTTP5XXCount = reader.readLong();
                    this.HTTP5XXCount.reset();
                    this.HTTP5XXCount.add(HTTP5XXCount);
                } catch (Exception e) {
                    LOG.info("兼容旧数据初始化");

                    this.successCount.reset();
                    this.successCount.add(count);

                    this.exceptionCount.reset();
                    this.exceptionCount.add(0);

                    this.timeoutCount.reset();
                    this.timeoutCount.add(0);

                    this.dropCount.reset();
                    this.dropCount.add(0);

                    this.HTTP2XXCount.reset();
                    this.HTTP2XXCount.add(0);

                    this.HTTP3XXCount.reset();
                    this.HTTP3XXCount.add(0);

                    this.HTTP4XXCount.reset();
                    this.HTTP4XXCount.add(0);

                    this.HTTP5XXCount.reset();
                    this.HTTP5XXCount.add(0);
                }
            } catch (Exception e) {
                LOG.error("SimpleCountHistogram init failed...", e.getMessage());
            }
        }
    }

    public int size() {
        return reservoir.size();
    }

}