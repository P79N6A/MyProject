package com.meituan.service.mobile.mtthrift.client.pool;

import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author YaoZhidong
 * @version 1.0
 * @created 12-9-20
 * @deprecated
 */

@Deprecated
public class MTThriftPool implements IMTThriftPool {
    private final static Logger logger = LoggerFactory.getLogger(MTThriftPool.class);

    public final static boolean testLog = false;

    private static final int DEFAULT_TIMEOUT = 10000;

    /**
     * 对象缓存池
     */
    private ConcurrentMap<String, ObjectPool> objectPools = new ConcurrentHashMap<String, ObjectPool>();

    private ThreadLocal<ObjectPool> currentPool = null;

    /**
     * 超时设置
     */
    private int timeOut;

    private String service; // 注册到zookeeper的服务名
    private String zkUrl; // zk服务器地址
    private int zkSessionTimeout = 30000;// 强制使用30秒，客户端配置不生效（fromversion:1.0.21）

    private GenericObjectPool.Config poolConfig;

    private Random random = new Random();

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Set<Integer> closings = new CopyOnWriteArraySet<Integer>();

    private ThreadLocal<AtomicLong> startTime = new ThreadLocal<AtomicLong>();

    /**
     * 使用ip，端口
     *
     * @param host
     * @param port
     */
    public MTThriftPool(String host, int port) {
        this(new MTThriftPoolConfig(), host, port);
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final String host, int port, int timeout) {
        addPool(host, port, timeout, poolConfig);
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final String host, final int port) {
        this(poolConfig, host, port, DEFAULT_TIMEOUT);
    }

    /**
     * 使用ip端口列表
     *
     * @param addresses
     */
    public MTThriftPool(List<String> addresses) {
        this(new MTThriftPoolConfig(), addresses);
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final List<String> addresses) {
        this(poolConfig, addresses, DEFAULT_TIMEOUT);
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final List<String> addresses, int timeout) {
        for (String each : addresses) {
            String[] address = each.split(":");
            addPool(address[0], Integer.parseInt(address[1]), timeout, poolConfig);
        }
    }

    /**
     * 使用service名称，zk使用配置文件
     *
     * @param service
     */
    public MTThriftPool(String service) throws Exception {

        this(new MTThriftPoolConfig(), service);
    }

    /**
     *
     * @param poolConfig
     * @param service
     *            zk中的节点路径
     * @param zk
     *            是否使用zkProp作为取zk地址的Propert文件名
     * @param zkProp
     *            取zk地址的Propert文件名
     * @param timeout
     *            客户端调用服务器的超时时间
     * @throws Exception
     */
    public MTThriftPool(final GenericObjectPool.Config poolConfig, final String service, boolean zk, String zkProp, int timeout) throws Exception {
        this.service = service;
        this.timeOut = timeout;
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final String service) throws Exception {
        this(poolConfig, service, false, null, DEFAULT_TIMEOUT);
    }

    /**
     * 使用service名称，zk使用地址
     *
     * @param service
     * @param zkUrl
     */
    public MTThriftPool(String service, String zkUrl, int zkTimeout) throws Exception {
        this(new MTThriftPoolConfig(), service, zkUrl, zkTimeout);
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final String service, String zkUrl, int zkTimeout, int timeout) throws Exception {
        this.service = service;
        this.zkUrl = zkUrl;
        this.zkSessionTimeout = zkTimeout;
        this.timeOut = timeout;
        this.poolConfig = poolConfig;
    }

    public MTThriftPool(final GenericObjectPool.Config poolConfig, final String service, String zkUrl, int zkTimeout) throws Exception {
        this(poolConfig, service, zkUrl, zkTimeout, DEFAULT_TIMEOUT);
    }

    private String formatPoolName(String host, int port) {
        return new StringBuilder(host).append(":").append(port).toString();
    }

    private void addPool(String host, int port, int timeOut, GenericObjectPool.Config poolConfig) {
        if (host.equals("127.0.0.1") || host.equals("localhost")) {
            host = LocalPointConf.getAppIp();
        }
        if (this.objectPools.containsKey(formatPoolName(host, port)))
            return;
        this.poolConfig = poolConfig;
        ObjectPool old = this.objectPools.put(formatPoolName(host, port), new GenericObjectPool(new ThriftPoolableObjectFactory(host, port, timeOut, timeOut),
                poolConfig));
        if (old != null) {
            destroyPool(old);
        }
    }

    public void destroy() {
        try {// 销毁定时器
            executor.shutdownNow();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {// 销毁thrift连接池
            for (Iterator<String> it = objectPools.keySet().iterator(); it.hasNext();) {// 删除报有server
                String key = it.next();
                ObjectPool pool = objectPools.get(key);
                destroyPool(pool);
            }
        } catch (Exception e) {
            throw new RuntimeException("error destroy()", e);
        }
    }

    private void destroyPool(ObjectPool pool) {
        if (pool != null) {
            try {// 销毁旧池
                pool.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public TSocket getConnection() {
        long start = System.currentTimeMillis();
        if (startTime.get() == null) {
            startTime.set(new AtomicLong(0));
        }
        startTime.get().set(System.nanoTime());
        int count = 2;
        int index = -1;
        while (--count >= 0) {
            try {
                if (objectPools.size() == 0) {
                    throw new Exception("empty server list");
                }
                index = random.nextInt(objectPools.size());
                while (closings.contains(index) && closings.size() < objectPools.size()) {
                    index = random.nextInt(objectPools.size());
                }
                if (currentPool == null) {
                    currentPool = new ThreadLocal<ObjectPool>();
                }
                Iterator<Map.Entry<String, ObjectPool>> it = objectPools.entrySet().iterator();
                for (int i = 0; it.hasNext(); i++) {
                    Map.Entry<String, ObjectPool> entry = it.next();
                    if (i == index) {
                        currentPool.set(entry.getValue());
                        break;
                    }
                }
                TSocket socket = (TSocket) currentPool.get().borrowObject();
                if (testLog) {
                    logger.info("borrow:{}", socket);
                }
                long takes = System.currentTimeMillis() - start;
                if (takes > 2000) {
                    logger.warn("Get Connection Success!Time:" + takes + ",actives:" + currentPool.get().getNumActive() + ",idle:"
                            + currentPool.get().getNumIdle());
                }
                return socket;
            } catch (Exception e) {
                if (count > 0 && index != -1) {
                    closings.add(index);
                    final int removed = index;
                    executor.schedule(new Runnable() {
                        public void run() {
                            closings.remove(removed);
                        }
                    }, zkSessionTimeout, TimeUnit.MILLISECONDS);
                    index = -1;
                    continue;
                }
                if (currentPool.get() != null)
                    logger.error("Get Connection Exception! Time:" + (System.currentTimeMillis() - start) + ",actives:" + currentPool.get().getNumActive()
                            + ",idle:" + currentPool.get().getNumIdle());
                else
                    logger.error("Get Connection Exception! Time:" + (System.currentTimeMillis() - start));
                throw new RuntimeException("error getConnection()", e);
            }
        }
        return null;
    }

    @Override
    public void returnBrokenConnection(TSocket socket) {
        try {
            if (socket == null || currentPool == null || currentPool.get() == null)
                return;
            currentPool.get().invalidateObject(socket);
            clear();
        } catch (Exception e) {
            throw new RuntimeException("error returnBrokenConnection()", e);
        }
    }

    @Override
    public void returnConnection(TSocket socket) {
        try {
            if (socket == null || currentPool == null || currentPool.get() == null)
                return;
            currentPool.get().returnObject(socket);
            clear();
        } catch (Exception e) {
            throw new RuntimeException("error returnConnection()", e);
        }
    }

    private void clear() {
        currentPool.remove();
    }
}
