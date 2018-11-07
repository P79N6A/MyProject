package com.sankuai.meituan.config.service;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.framework.api.CuratorWatcher;
import com.netflix.curator.framework.api.transaction.CuratorTransactionFinal;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.framework.state.ConnectionStateListener;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import com.sankuai.meituan.config.function.Consumer;
import com.sankuai.meituan.config.model.ConfigNode;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.util.ConfigUtilAdapter;
import com.sankuai.meituan.zkclient.ConnectionListener;
import com.sankuai.meituan.zkclient.ZookeeperConfig;
import org.apache.commons.collections.CollectionUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ZookeeperService {
    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperService.class);

    private CuratorFramework client = null;
    private volatile boolean closed = false;
    private String url = "";
    private Set<ConnectionListener> connectionListeners = new CopyOnWriteArraySet<>();
    private ExecutorService asycExecutor = Executors.newFixedThreadPool(1);

    @Resource
    private PropertySerializeService propertySerializeService;

    private void connect(String url) {
        this.url = url;

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(this.url).retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
                .connectionTimeoutMs(ZookeeperConfig.CONNECT_TIMEOUT).sessionTimeoutMs(ZookeeperConfig.SESSION_TIMEOUT);
        client = builder.build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                ConnectionListener.State connectionState = toConnectionState(state);
                if (connectionState != null) {
                    connectionStateChanged(connectionState);
                }
            }

            private ConnectionListener.State toConnectionState(ConnectionState state) {
                switch (state) {
                    case LOST:
                        return ConnectionListener.State.DISCONNECTED;
                    case CONNECTED:
                        return ConnectionListener.State.CONNECTED;
                    case RECONNECTED:
                        return ConnectionListener.State.RECONNECTED;
                    default:
                        return null;
                }
            }
        });
        client.start();
    }

    private void connectionStateChanged(ConnectionListener.State state) {
        for (ConnectionListener listener : getConnectionListeners()) {
            listener.stateChanged(state);
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

    public void close() {
        if (closed) {
            return;
        }
        synchronized (this) {
            try {
                client.close();
                closed = true;
            } catch (Throwable t) {
                LOG.warn(t.getMessage(), t);
            }
        }
    }

    public boolean exist(String path) {
        try {
            Stat stat = client.checkExists().forPath(path);
            return stat != null;
        } catch (Exception e) {
            LOG.warn("", e);
        }
        return false;
    }

    public void create(String path, byte[] data) {
        try {
            client.create().creatingParentsIfNeeded().forPath(path, data);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }


    public void createAsyc(final String path, final Consumer<Exception> callback) {
        asycExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!exist(path)) {
                        client.create().creatingParentsIfNeeded().forPath(path, new byte[0]);
                    }
                    try {
                        callback.accept(null);
                    } catch (Exception e) {
                        LOG.error(MessageFormatter.format("执行异步创建成功,但回调调用出错, path:[{}]", path).getMessage(), e);
                    }
                } catch (KeeperException.NodeExistsException ignored) {

                } catch (Exception e) {
                    callback.accept(e);
                }
            }
        });
    }

    public void delete(String path) {
        try {
            client.delete().forPath(path);
        } catch (KeeperException.NoNodeException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void delRecurse(String path) {
        try {
            List<String> nodes = new ArrayList<>();
            getTreeNodes(nodes, path);
            LOG.debug(path + " children to delete: " + nodes);
            if (!nodes.isEmpty()) {
                CuratorTransactionFinal transaction = client.inTransaction().delete().forPath(nodes.get(0)).and();
                for (int i = 1; i < nodes.size(); i++) {
                    transaction = transaction.delete().forPath(nodes.get(i)).and();
                }
                transaction.commit();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public List<String> getTreeNodes(List<String> nodes, String path) {
        path = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        try {
            List<String> children = client.getChildren().forPath(path);
            for (String node : children) {
                getTreeNodes(nodes, path + "/" + node);
            }
            nodes.add(path);
        } catch (KeeperException.NoNodeException ignored) {
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return nodes;
    }

    public List<String> getNodes(String path) {
        try {
            return client.getChildren().storingStatIn(new Stat()).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public byte[] getData(String path) {
        try {
            return client.getData().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public byte[] getData(String path, Stat stat) {
        try {
            return client.getData().storingStatIn(stat).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public byte[] wgetData(String path, CuratorWatcher watcher){
        try {
            return client.getData().usingWatcher(watcher).forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Stat getStat(String path) {
        try {
            return client.checkExists().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public void setData(String path, byte[] bytes, int version) throws Exception {
        if (null != bytes && bytes.length > 1024 * 1000) {
            throw new IllegalArgumentException("the length of setting data must be smaller than 1024*1000. path = " + path + ", len = " + bytes.length);
        }
        client.setData().withVersion(version).forPath(path, bytes);
    }

    Set<ConnectionListener> getConnectionListeners() {
        return connectionListeners;
    }


    @PostConstruct
    public void init() {
        String url = ConfigUtilAdapter.getString("config.zookeeper");
        try {
            if (url != null) {
                connect(url);
            }
        } catch (Exception e) {
            LOG.error("init client connect fail...", e);
        }
    }


    public Map<String, String> getDataMap(String path) {
        byte[] zkData = getData(path);
        return propertySerializeService.deSerializePropertyValueAsMap(zkData);
    }


    public void setData(String path, ConfigNode configNode, int version) throws Exception {
        Assert.notNull(configNode, "设置的configNode不允许为null");
        Assert.hasText(path, "设置的path不允许为空");

        Collection<? extends PropertyValue> values = configNode.getData();
        byte[] data = propertySerializeService.serializePropertyValue(values);
        Assert.notNull(data, "configNode里的dataMap不能序列化");

        setData(path, data, version);
    }


    void forData(String fullPath, Consumer<PropertyValue> consumer) {
        byte[] data = getData(fullPath);
        List<PropertyValue> spaceData = propertySerializeService.deSerializePropertyValueAsList(data);
        for (PropertyValue propertyValue : spaceData) {
            consumer.accept(propertyValue);
        }
    }

    void iterateAndDeleteEmpty(String zkPath, Consumer<String> consumer) {
        List<String> childes = getNodes(zkPath);
        if (CollectionUtils.isNotEmpty(childes)) {
            for (String node : childes) {
                consumer.accept(node);
            }
        } else {
            delete(zkPath);
        }
    }
}
