package com.sankuai.meituan.config.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author xuehuayang
 */
public class ZooKeeperUtils {
    private static final Logger logger = LoggerFactory.getLogger(ZooKeeperUtils.class);
    public static final char ZNODE_PATH_SEPARATOR = '/';

    public static String getParent(String node) {
        int idx = node.lastIndexOf(ZNODE_PATH_SEPARATOR);
        return idx <= 0 ? null : node.substring(0, idx);
    }

    public static String getNodeName(String path) {
        return path.substring(path.lastIndexOf(ZNODE_PATH_SEPARATOR) + 1);
    }

    public static boolean isEquals(byte[] data1, byte[] data2) {
        if (data1 == null && data2 == null) return true;
        else if (data1 == null || data2 == null || data1.length != data2.length) return false;
        for (int i = data1.length - 1; i >= 0; i--) if (data1[i] != data2[i]) return false;
        return true;
    }

    public static boolean isEquals(List<String> children1, List<String> children2) {
        if (children1 == null && children2 == null) return true;
        else if (children1 == null || children2 == null || children1.size() != children2.size()) return false;
        for (int i = children1.size() - 1; i >= 0; i--) if (!children2.contains(children1.get(i))) return false;
        return true;
    }


    public static String formatPath(String path) {
        if (null == path || "".equals(path)) {
            return null;
        }
        path = path.replace('\\', '/');
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    public static void waitUntilConnected(ZooKeeper zooKeeper) {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        Watcher watcher = new ConnectedWatcher(connectedLatch, zooKeeper);
        zooKeeper.register(watcher);
        if (States.CONNECTING == zooKeeper.getState()) {
            try {
                connectedLatch.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            if(connectedLatch.getCount() > 0) {
                logger.error("can't connect to any zookeeper,the thread will block here.", new RuntimeException());
                try {
                    connectedLatch.await();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    static class ConnectedWatcher implements Watcher {

        private CountDownLatch connectedLatch;
        private ZooKeeper zooKeeper;

        ConnectedWatcher(CountDownLatch connectedLatch, ZooKeeper zooKeeper) {
            this.connectedLatch = connectedLatch;
            this.zooKeeper = zooKeeper;
        }

        @Override
        public void process(WatchedEvent event) {
            System.err.println("process:" + event.toString());
            if (event.getState() == KeeperState.SyncConnected) {
                connectedLatch.countDown();
            }
            waitUntilConnected(zooKeeper);
        }

    }
}
