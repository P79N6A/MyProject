package com.meituan.mtrace.hbase;

import com.meituan.mtrace.common.Span;
import com.meituan.mtrace.thriftjava.*;
import com.meituan.service.hbase.exceptions.KerberosAuthFailedException;
import com.meituan.service.hbase.impl.MTAsyncHBaseClient;
import com.meituan.service.hbase.impl.StatisticalFutureCallback;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * CloudTableStoreService 存储ThriftSpanList中的span信息到hbase中，包括数据存储和索引建立
 *
 * @author zhangzhitong
 * @created 9/15/15
 */

public class CloudTableStoreService implements IStoreService {

    private Logger logger = LoggerFactory.getLogger(CloudTableStoreService.class);
    private Env storeEnv = Env.Prod;
    private MTAsyncHBaseClient client;
    private TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
    private TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
    private final int HBASE_PUT_LIMIT = 1000;
    private final int HBASE_GET_LIMIT = 5000;

    public CloudTableStoreService() {
        logger.info("CloudTableStoreService use Prod Env");
        initClient();
    }

    public CloudTableStoreService(Env env) {
        storeEnv = env;
        if (storeEnv.equals(Env.Stage)) {
            logger.info("CloudTableStoreService use Stage Env");
        } else if (storeEnv.equals(Env.Test)) {
            logger.info("CloudTableStoreService use Test Env");
        } else {
            logger.info("CloudTableStoreService use Prod Env");
        }
        initClient();
    }

    private void initClient() {
        try {
            client = new MTAsyncHBaseClient();
            logger.info("Init CloudTableStoreService successfully");
        } catch (KerberosAuthFailedException e) {
            logger.warn("MTAsyncHBaseClient KerberosAuthFail + " + e, e);
        } catch (IOException e) {
            logger.warn("MTAsyncHBaseClient IOException " + e, e);

        }

    }

    /**
     * 反序列化data 然后存hbase
     *
     * @param data 序列化的ThriftSpanList二进制数据
     */
    @Override
    public void store(byte[] data) {
        logger.debug("Store bytes[] size " + data.length);
        try {
            ThriftSpanList thriftSpanList = new ThriftSpanList();
            tDeserializer.deserialize(thriftSpanList, data);
            if (thriftSpanList.spans != null) {
                for (ThriftSpan thriftSpan : thriftSpanList.spans) {
                    store(thriftSpan);
                }
            } else {
                logger.info("CloudTableStoreService.store ThriftSpan.spans is null");
            }
        } catch (TException e) {
            logger.warn("data deserialize ThritSpanList error " + e);
        }
    }

    @Override
    public void store(List<ThriftSpan> thriftSpans) {
        logger.debug("Store list<ThriftSpan> size " + thriftSpans.size());
        if (thriftSpans != null && !thriftSpans.isEmpty()) {
            for (ThriftSpan thriftSpan : thriftSpans) {
                store(thriftSpan);
            }
        }
    }

    @Override
    public void store(ThriftSpan thriftSpan) {
        logger.debug("Store thriftSpan " + thriftSpan.toString());
        if (storeSpan(thriftSpan)) {
            //storeDuration(thriftSpan);
            storeIdxServiceName(thriftSpan);
            storeIdxServiceSpanName(thriftSpan);

        }
    }

    /**
     * 将ThriftSpan 经过thrift序列化后存入hbase
     *
     * @param thriftSpan 请求span元数据
     */
    private boolean storeSpan(ThriftSpan thriftSpan) {
        Put p = new Put(TableLayouts.getRowKeyOfStorage(thriftSpan));
        try {
            p.add(TableLayouts.storageFamily, TableLayouts.getQualOfStorage(thriftSpan), serializer.serialize(thriftSpan));
            if (client != null) {
                client.put(TableLayouts.getStorageTable(storeEnv), p, HBASE_PUT_LIMIT, new StatisticalFutureCallback<Void>() {
                    @Override
                    protected void onSuccessImpl(Void result) {
                    }

                    @Override
                    protected void onFailureImpl(Throwable t) {
                        logger.warn("Put data into " + TableLayouts.getStorageTable(storeEnv) + " failed " + t.getMessage());
                    }
                });
                return true;
            } else {
                logger.warn("MTAsyncHBaseClient is null");
            }

        } catch (TException e) {
            logger.warn("ThriftSpan serializer to bytes error " + e);
        } catch (Exception e) {
            logger.warn("Put data into " + TableLayouts.getStorageTable(storeEnv) + " error " + e, e);
        }
        return false;
    }

    /**
     * 为ThriftSpan建立ServiceName(appKey) + SpanName + time 维度的索引
     *
     * @param thriftSpan 请求span元数据
     */
    private void storeIdxServiceName(ThriftSpan thriftSpan) {
        logger.debug("StoreIdxServiceName traceId " + thriftSpan.getTraceId());
        Put p = new Put(TableLayouts.getRowKeyOfServiceName(thriftSpan));
        p.add(TableLayouts.idxServiceNameFamily, TableLayouts.getQualOfServiceName(thriftSpan), Bytes.toBytes(true));
        if (client != null) {
            try {
                client.put(TableLayouts.getIdxServiceTable(storeEnv), p, HBASE_PUT_LIMIT, new StatisticalFutureCallback<Void>() {
                    @Override
                    protected void onSuccessImpl(Void result) {

                    }

                    @Override
                    protected void onFailureImpl(Throwable t) {
                        logger.warn("Put data into " + TableLayouts.getIdxServiceTable(storeEnv) + " failed " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.warn("Put data into " + TableLayouts.getIdxServiceTable(storeEnv) + " error " + e, e);
            }
        } else {
            logger.warn("MTAsyncHBaseClient is null");
        }
    }

    /**
     * 为ThriftSpan建立Service Name + SpanName + time 维度的索引
     *
     * @param thriftSpan 请求span元数据
     */
    private void storeIdxServiceSpanName(ThriftSpan thriftSpan) {
        logger.debug("StoreIdxServiceSpanName traceId " + thriftSpan.getTraceId());
        Put p = new Put(TableLayouts.getRowKeyOfServiceSpanName(thriftSpan));
        p.add(TableLayouts.idxServiceSpanNameFamily, TableLayouts.getQualOfServiceSpanName(thriftSpan), Bytes.toBytes(true));
        if (client != null) {
            try {
                client.put(TableLayouts.getIdxServiceSpanTable(storeEnv), p, HBASE_PUT_LIMIT, new StatisticalFutureCallback<Void>() {
                    @Override
                    protected void onSuccessImpl(Void result) {

                    }

                    @Override
                    protected void onFailureImpl(Throwable t) {
                        logger.warn("Put data into " + TableLayouts.getIdxServiceSpanTable(storeEnv) + " failed " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                logger.warn("Put data into " + TableLayouts.getIdxServiceSpanTable(storeEnv) + " error " + e, e);
            }
        } else {
            logger.warn("MTAsyncHBaseClient is null");
        }
    }

    @Override
    public List<Span> getSpansByTraceId(long traceId, Env env) {
        Get get = new Get(TableLayouts.getRowKeyOfStorage(traceId));
        List<Span> spans = new LinkedList<Span>();
        if (client != null) {
            try {
                Result result = client.get(TableLayouts.getStorageTable(env), get, HBASE_GET_LIMIT);
                result.listCells();
                if (!result.isEmpty()) {
                    for (Cell cell : result.rawCells()) {
                        try {
                            ThriftSpan tSpan = new ThriftSpan();
                            tDeserializer.deserialize(tSpan, CellUtil.cloneValue(cell));
                            //System.out.println(span.toString());
                            spans.add(Util.thriftToSpan(tSpan));
                        } catch (TException e) {
                            logger.warn("getSpansByTraceId Bytes deserializer to ThriftSpan error " + e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("getSpansByTraceId Get data from " + TableLayouts.getStorageTable(env) + " error " + e, e);
            }
        } else {
            logger.warn("MTAsyncHBaseClient is null");
        }
        return spans;
    }

    @Override
    public List<List<Span>> getSpansByTraceIds(List<Long> traceIds, Env env) {
        List<List<Span>> traces = new ArrayList<List<Span>>();
        List<Get> gets = new ArrayList<Get>();
        for (long traceId : traceIds) {
            gets.add(new Get(TableLayouts.getRowKeyOfStorage(traceId)));
        }
        if (client != null) {
            try {
                Result[] results = client.get(TableLayouts.getStorageTable(env), gets, HBASE_GET_LIMIT);
                if (results.length > 0) {
                    for (Result r : results) {
                        if (!r.isEmpty()) {
                            List<Span> trace = new ArrayList<Span>();
                            for (Cell cell : r.rawCells()) {
                                ThriftSpan tSpan = new ThriftSpan();
                                try {
                                    tDeserializer.deserialize(tSpan, CellUtil.cloneValue(cell));
                                    trace.add(Util.thriftToSpan(tSpan));
                                } catch (TException e) {
                                    logger.warn("getSpansByTraceIds Bytes deserializer to ThriftSpan error " + e);
                                }
                            }
                            traces.add(trace);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("getSpansByTraceIds Get data from " + TableLayouts.getStorageTable(env) + " error " + e, e);
            }
        } else {
            logger.warn("MTAsyncHBaseClient is null");
        }
        logger.debug("getSpansByTraceIds " + traces);
        return traces;
    }

    @Override
    public List<Long> getTraceIdsByServiceName(String serviceName, long endTs, int limit, Env env) {
        Scan scan = new Scan();
        scan.setStartRow(TableLayouts.getRowKeyOfServiceName(serviceName, endTs));
        scan.setStopRow(TableLayouts.getRowKeyOfServiceName(serviceName, 0));
        scan.setCaching(limit * 10);
        scan.setFilter(new PageFilter(limit));
        List<Long> traceIds = new ArrayList<Long>();
        if (client != null) {
            try {
                ResultScanner rs = client.getScanner(TableLayouts.getIdxServiceTable(env), scan);
                for (Result r : rs) {
                    if (!r.isEmpty()) {
                        for (Cell cell : r.rawCells()) {
                            //traceIds.add();
                            //System.out.println(Bytes.toString(CellUtil.cloneRow(cell)) + " " + Bytes.toLong(CellUtil.cloneQualifier(cell)));
                            traceIds.add(Bytes.toLong(CellUtil.cloneQualifier(cell)));
                        }
                    }
                }
                rs.close();
            } catch (Exception e) {
                logger.warn("MTAsyncHBaseClient getScanner error " + e, e);
            }
        } else {
            logger.warn("MTAsyncHBaseClient is null");
        }
        return traceIds;

    }

    @Override
    public List<Long> getTraceIdsByServiceSpanName(String serviceName, String serviceSpanName, long endTs, int limit, Env env) {
        Scan scan = new Scan();
        scan.setStartRow(TableLayouts.getRowKeyOfServiceSpanName(serviceName, serviceSpanName, endTs));
        scan.setStopRow(TableLayouts.getRowKeyOfServiceSpanName(serviceName, serviceSpanName, 0));
        scan.setCaching(limit * 10);
        scan.setFilter(new PageFilter(limit));

        List<Long> traceIds = new ArrayList<Long>();
        if (client != null) {
            try {
                ResultScanner rs = client.getScanner(TableLayouts.getIdxServiceSpanTable(env), scan);
                for (Result r : rs) {
                    if (!r.isEmpty()) {
                        for (Cell cell : r.rawCells()) {
                            //traceIds.add();
                            //System.out.println(Bytes.toString(CellUtil.cloneRow(cell)) + " " + Bytes.toLong(CellUtil.cloneQualifier(cell)));
                            traceIds.add(Bytes.toLong(CellUtil.cloneQualifier(cell)));
                        }
                    }
                }
                rs.close();
            } catch (Exception e) {
                logger.warn("MTAsyncHBaseClient getScanner error " + e, e);
            }
        } else {
            logger.warn("MTAsyncHBaseClient is null");
        }
        return traceIds;
    }

    @Override
    public List<List<Span>> getSpansByServiceSpanName(String serviceName, String spanName, long endTs, int limit, Env env) {
        List<Long> traceIds;
        if (spanName == null || spanName.isEmpty() || spanName.equals("*")) {
            traceIds = getTraceIdsByServiceName(serviceName, endTs, limit, env);
        } else {
            traceIds = getTraceIdsByServiceSpanName(serviceName, spanName, endTs, limit, env);
        }
        if (!traceIds.isEmpty()) {
            return getSpansByTraceIds(traceIds, env);
        }
        return null;
    }

}
