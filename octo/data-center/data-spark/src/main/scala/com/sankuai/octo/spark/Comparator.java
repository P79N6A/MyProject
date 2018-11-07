package com.sankuai.octo.spark;

import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.KeyValueSortReducer;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Created by wujinwu on 16/4/15.
 */
public class Comparator extends KeyValueSortReducer {
    @Override
    protected void reduce(ImmutableBytesWritable row, Iterable<KeyValue> kvs, Context context) throws IOException, InterruptedException {
        super.reduce(row, kvs, context);
        System.out.println("row:"+row.toString());
        for(KeyValue kv : kvs){
            System.out.println("Row:"+new String(kv.getRowArray(),StandardCharsets.UTF_8));
            System.out.println("Qualifier:"+new String(kv.getQualifierArray(),StandardCharsets.UTF_8));
            System.out.println("Value:"+new String(kv.getValueArray(),StandardCharsets.UTF_8));
        }
    }
}
