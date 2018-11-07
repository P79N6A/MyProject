package com.sankuai.meituan.config.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-4-25
 */
public class MtZooKeeperConn implements Watcher {
    private static final Logger LOG = LoggerFactory.getLogger(MtZooKeeperConn.class);

    private ZooKeeper zookeeper;
    private String connectString;
    private int sessionTimeout = 3000;
    private Watcher watcher = this;
    private Set<IZooKeeperReconnectListener> reconnectListenerSet = new HashSet<IZooKeeperReconnectListener>();

    public MtZooKeeperConn(String connectString) {
        this.connectString = connectString;
    }

    public MtZooKeeperConn(String connectString, int sessionTimeout) {
        this.connectString = connectString;
        this.sessionTimeout = sessionTimeout;
    }

    public MtZooKeeperConn(String connectString, int sessionTimeout, Watcher watcher) {
        this.connectString = connectString;
        this.sessionTimeout = sessionTimeout;
        this.watcher = watcher;
    }

    public ZooKeeper connect() throws IOException {
        zookeeper = new ZooKeeper(connectString, sessionTimeout, watcher);
        ZooKeeperUtils.waitUntilConnected(zookeeper);
        zookeeper.register(this);
        LOG.info("connect to {}", connectString);
        return zookeeper;
    }

    public void destroy() throws InterruptedException {
        if (zookeeper != null) {
            zookeeper.close();
        }
    }

    public void addReconnectListener(IZooKeeperReconnectListener listener) {
        reconnectListenerSet.add(listener);
    }

    @Override
    public void process(WatchedEvent event) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Receive WatchedEvent: {}", event.toString());
        }
        if (Event.KeeperState.Expired == event.getState()) {
            LOG.warn("ZooKeeper session [{}] expired", zookeeper.getSessionId());
            recover();
        }
    }

    /**
     * 当客户端断网时，原zookeeper链接变成close状态。
     * 只能通过重新new ZooKeeper()的方式生成新的链接。
     *
     * @param force
     *      强制重启
     * @return
     */
    public synchronized Boolean reconnect(Boolean force) {
        if (! force && zookeeper != null && zookeeper.getState() == ZooKeeper.States.CONNECTED) {
            return Boolean.FALSE;
        }
        recover();
        return Boolean.TRUE;
    }

    private synchronized void recover() {
        try {
            zookeeper.close();
            LOG.info("last connect address [{}] closed!", connectString);
        } catch (InterruptedException e) {
            LOG.warn("Close ZooKeeper failed, address is " + connectString, e);
        }
        try {
            connect();
        } catch (IOException e) {
            LOG.error("Connect ZooKeeper failed", e);
        }
    }

    public ZooKeeper getZookeeper() {
        return zookeeper;
    }

    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Watcher getWatcher() {
        return watcher;
    }

    public void setWatcher(Watcher watcher) {
        this.watcher = watcher;
    }
}
