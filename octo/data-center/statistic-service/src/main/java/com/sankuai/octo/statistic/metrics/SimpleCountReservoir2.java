package com.sankuai.octo.statistic.metrics;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by zava on 15/9/23.
 * 固定的时间桶 统计 每个耗时的 访问次数
 */
public class SimpleCountReservoir2 implements Reservoir, Persistence, Serializable {

    //<cost time,count>
    private ConcurrentMap<Integer, Long> values;

    private int max;

    public SimpleCountReservoir2() {
        this.max = Integer.MIN_VALUE;
        this.values = new ConcurrentHashMap<>(50);
    }

    public SimpleCountReservoir2(int max, ConcurrentMap<Integer, Long> values) {
        this.max = max;
        this.values = values;
    }

    @Override
    public void update(long value) {
        update((int) value);
    }


    public void update(int value) {
        update(value, 1L);
    }

    public void update(int value, long countValue) {
        if (value > max) {
            max = value;
        }
        value = TimeBucketUtil.compress(value);
        Long data = values.get(value);
        if (data == null) {
            values.put(value, countValue);
        } else {
            values.put(value, data + countValue);
        }
    }

    @Override
    public int size() {
        return values.size();
    }


    @Override
    public SimpleCountSnapshot2 getSnapshot() {
        final TreeMap<Integer, Long> data = new TreeMap<>();
        Iterator<Map.Entry<Integer, Long>> it = values.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Long> entry = it.next();
            data.put(entry.getKey(), entry.getValue());
        }
        return new SimpleCountSnapshot2(data, max);
    }


    public void dump(OutputStream stream) throws IOException {
        DataOutputStream writer = new DataOutputStream(stream);
        try {
            writer.writeInt(this.max);
            writer.writeInt(values.size());
            for (Map.Entry<Integer, Long> entry : values.entrySet()) {
                writer.writeInt(entry.getKey());
                writer.writeLong(entry.getValue());
            }
        } finally {
            try {
                writer.flush();
            } catch (Exception e) {
                //ignore
            }
        }
    }

    public void init(InputStream stream) throws IOException {
        DataInputStream reader = new DataInputStream(stream);
        this.max = reader.readInt();
        int size = reader.readInt();
        ConcurrentMap<Integer, Long> map = new ConcurrentHashMap<>();
        for (int i = 0; i < size; i++) {
            int key = reader.readInt();
            long value = reader.readLong();
            map.put(key, value);
        }
        this.values = map;
    }

    public ConcurrentMap<Integer, Long> getValues() {
        return values;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMax() {
        return max;
    }
}

