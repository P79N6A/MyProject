package com.sankuai.meituan.config.zookeeper;

import com.google.common.base.Throwables;
import org.apache.commons.lang.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-4
 */
public class MtZooKeeperClient implements IZooKeeperReconnectListener {
    private static final Logger LOG = LoggerFactory.getLogger(MtZooKeeperClient.class);

    private volatile static MtZooKeeperClient SINGLETON = null;

    protected MtZooKeeperConn zooKeeperConn;
    private final Map<String, Set<INodeChangeListener>> path2ExistListeners = new ConcurrentHashMap<String, Set<INodeChangeListener>>();
    // 确保每个节点上的一种类型的Watcher只有一个
    private final Map<String, Boolean> path2Watcher = new ConcurrentHashMap<String, Boolean>();

    public static MtZooKeeperClient getInstance(String connectString) {
        if (SINGLETON == null) {
            synchronized (MtZooKeeperClient.class) {
                if (SINGLETON == null) {
                    SINGLETON = new MtZooKeeperClient(connectString, 3000);
                }
            }
        }
        return SINGLETON;
    }

    private MtZooKeeperClient(String connectString, int sessionTimeout) {
        try {
            init(connectString, sessionTimeout);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected void init(String connectString, int sessionTimeout) throws IOException {
        if (StringUtils.isEmpty(connectString)) {
            throw new IllegalArgumentException("ZK地址为空,无法初始化ZK连接!");
        }
        zooKeeperConn = new MtZooKeeperConn(connectString, sessionTimeout);
        zooKeeperConn.connect();
        zooKeeperConn.addReconnectListener(this);
    }

    /**
     * 销毁zookeeper连接
     */
    public void destroy() {
        if (zooKeeperConn != null) {
            try {
                zooKeeperConn.destroy();
            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    public Boolean existAndListen(String path, INodeChangeListener listener) {
        if (listener != null) {
            Set<INodeChangeListener> listeners = path2ExistListeners.get(path);
            if (listeners == null) {
                synchronized (path2ExistListeners) {
                    listeners = path2ExistListeners.get(path);
                    if (listeners == null) {
                        listeners = new HashSet<INodeChangeListener>();
                        path2ExistListeners.put(path, listeners);
                    }
                }
            }
            listeners.add(listener);
        }
        return existAndWatch(path, true);
    }

    private Boolean existAndWatch(String path, boolean isWatch) {
        if (zooKeeperConn == null) {
            throw new IllegalStateException("无法连接到ZK!");
        }
        Watcher watcher = null;
        try {
            ZooKeeper zooKeeper = zooKeeperConn.getZookeeper();
            if (isWatch) {
                watcher = getNodeWatcher(path, WatcherType.EXIST);
            }
            Stat stat = null != watcher ? zooKeeper.exists(path, watcher) : zooKeeper.exists(path, false);
            if (stat != null) {
                return Boolean.TRUE;
            }
        } catch (KeeperException.SessionExpiredException e) {
            LOG.error(e.getMessage(), e);
            handleSessionExpiredException();
        } catch (KeeperException e) {
            LOG.error(e.getMessage(), e);
            handleNodeWatcherException(path, WatcherType.EXIST);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
            if (watcher != null) {
                clearNodeWatcherFlag(path, WatcherType.EXIST);
            }
        }
        return Boolean.FALSE;
    }

    /**
     * exist Watcher
     *
     * @param path
     */
    private void noticeExistListener(String path) {
        try {
            Set<INodeChangeListener> listeners = path2ExistListeners.get(path);
            if (listeners != null && !listeners.isEmpty()) {
                for (INodeChangeListener listener : listeners) {
                    listener.updateExist(path);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Watcher getNodeWatcher(String path, WatcherType watcherType) {
        String key = createPath2WatcherKey(path, watcherType);
        Boolean added = path2Watcher.get(key);
        Watcher watcher = null;
        if (added == null) {
            synchronized (path2Watcher) {
                added = path2Watcher.get(key);
                if (added == null) {
                    path2Watcher.put(key, Boolean.TRUE);
                    watcher = new NodeChangeWatcher(path, this, watcherType);
                }
            }
        }
        return watcher;
    }

    private void handleSessionExpiredException() {
        if (zooKeeperConn.getZookeeper() == null || zooKeeperConn.getZookeeper().getState() != ZooKeeper.States.CONNECTED) {
            zooKeeperConn.reconnect(false);
        }
    }

    private void handleNodeWatcherException(String path, WatcherType watcherType) {
        if (path != null) {
            clearNodeWatcherFlag(path, watcherType);
        }
        handleSessionExpiredException();
        if (path != null) {
            // 重新添加Watcher
            switch (watcherType) {
                case EXIST:
                    existAndWatch(path, true);
                    break;
            }
        }
    }

    private Boolean clearNodeWatcherFlag(String path, WatcherType watcherType) {
        Boolean hasWatcher = null;
        switch (watcherType) {
            case EXIST:
                hasWatcher = path2Watcher.remove(createPath2WatcherKey(path, watcherType));
                break;
        }
        if (hasWatcher == null) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }

    private String createPath2WatcherKey(String path, WatcherType watcherType) {
        switch (watcherType) {
            case EXIST:
                return path + "$exist";
        }
        return null;
    }


    /**
     * zookeeper重连接
     */
    @Override
    public void reconnected() {
        // 清空Watcher标记
        path2Watcher.clear();
        // 重新添加Watcher
        for (String path : path2ExistListeners.keySet()) {
            existAndWatch(path, true);
        }
    }

    public class NodeChangeWatcher implements Watcher {
        private String path;
        private MtZooKeeperClient listener;
        private WatcherType watcherType;

        public NodeChangeWatcher(String path, MtZooKeeperClient listener, WatcherType watcherType) {
            this.path = path;
            this.listener = listener;
            this.watcherType = watcherType;
            LOG.info("new watcher on node({}) {}", watcherType.name(), path);
        }

        @Override
        public void process(WatchedEvent event) {
            LOG.info("receive zookeeper event on node({}): {}", new Object[]{watcherType.name(), event});
            switch (watcherType) {
                case EXIST:
                    if (Event.EventType.NodeDataChanged == event.getType()
                            || Event.EventType.NodeCreated == event.getType()
                            || Event.EventType.NodeDeleted == event.getType()) {
                        // 清除Watcher标记
                        path2Watcher.remove(createPath2WatcherKey(path, watcherType));
                        // 重新添加Watcher
                        listener.existAndWatch(path, true);
                        // 回调Exist Listener
                        listener.noticeExistListener(path);
                    } else if (Event.KeeperState.Expired == event.getState()
                            || Event.KeeperState.Disconnected == event.getState()) {
                        path2Watcher.remove(createPath2WatcherKey(path, watcherType));
                        zooKeeperConn.reconnect(true);
                        listener.reconnected();
                        listener.noticeExistListener(path);
                    }
                    break;
            }
        }
    }

    private enum WatcherType {
        EXIST;
    }
}
