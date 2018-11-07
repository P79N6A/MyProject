package com.sankuai.octo.statistic.metrics;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zava on 15/9/23.
 * 固定的时间桶 统计 每个耗时的 访问次数
 */
public class SimpleCountReservoir implements Reservoir, Persistence, Serializable {

    //<time,count>
    private ConcurrentHashMap<Integer, LongAdder> values;

    private volatile AtomicInteger max = new AtomicInteger(0);

    public SimpleCountReservoir() {
        this.max = new AtomicInteger(0);
        this.values = new ConcurrentHashMap<>(100);
    }

    @Override
    public void update(long value) {
        update((int) value);
    }


    public void update(int value) {
        update(value, 1L);
    }

    public void update(int value, long countValue) {
        for (; ; ) {
            int current = max.get();
            if (value > current) {
                if (max.compareAndSet(current, value))
                    break;
            } else {
                break;
            }
        }
        value = TimeBucketUtil.compress(value);
        LongAdder data = values.get(value);
        if (data == null) {
            data = new LongAdder();
            LongAdder oldData = values.putIfAbsent(value, data);
            if (oldData != null) {
                data = oldData;
            }
        }
        data.add(countValue);
    }

    @Override
    public int size() {
        return values.size();
    }


    @Override
    public SimpleCountSnapshot getSnapshot() {
        final TreeMap<Integer, Long> data = new TreeMap<>();
        for (Map.Entry<Integer, LongAdder> entry : values.entrySet()) {
            data.put(entry.getKey(), entry.getValue().longValue());
        }
        return new SimpleCountSnapshot(data, max.get());
    }


    public void dump(OutputStream stream) throws IOException {
        DataOutputStream writer = new DataOutputStream(stream);
        try {
            writer.writeInt(this.max.get());
            writer.writeInt(values.size());
            for (Map.Entry<Integer, LongAdder> entry : values.entrySet()) {
                writer.writeInt(entry.getKey());
                writer.writeLong(entry.getValue().longValue());
            }
        } finally {
            writer.flush();
        }
    }

    public void init(InputStream stream) throws IOException {
        DataInputStream reader = new DataInputStream(stream);
        this.max = new AtomicInteger(reader.readInt());
        int size = reader.readInt();
        this.values = new ConcurrentHashMap<>();
        for (int i = 0; i < size; i++) {
            int key = reader.readInt();
            long value = reader.readLong();
            LongAdder longValue = new LongAdder();
            longValue.add(value);
            this.values.put(key, longValue);
        }
    }
}

