package com.meituan.control.zookeeper.cluster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/*
*
* 这是一个永远不会退出的长连接
*
* */
public class ZooKeeperConn implements Runnable {
    private static Log log = LogFactory.getLog(ZooKeeperConn.class);
    private String connectString = null;
    private int sessionTimeOut = 3000;
    private ZooKeeper zooKeeper;
    private CountDownLatch reconnectLatch = null;
    private Thread reconnectWatchThread = null;


    public ZooKeeperConn(String connectString, int sessionTimeOut) {
        this.connectString = connectString;
        if (sessionTimeOut > 3000) {
            this.sessionTimeOut = sessionTimeOut;
        }
        // 第一次连接时以同步方式、直至连接上
        makeSureConnected();
        // reconnect以异步监听方式、以免应用会挂起
        // reconnect watch thread
        reconnectLatch = new CountDownLatch(1);
        reconnectWatchThread = new Thread(this);
        reconnectWatchThread.start();
    }

    public ZooKeeper getConnection() {
        return zooKeeper;
    }

    @Override
    public void run() {
        while (true) {
            // wait for reconnect signal
            try {
                reconnectLatch.await();
            } catch (Exception e) {
            }
            // make sure connected
            makeSureConnected();
            // reset: for next await
            reconnectLatch = new CountDownLatch(1);
        }
    }

    // 触发重新连接、不会挂起
    public synchronized void triggerReconnect() {
        if (zooKeeper != null && zooKeeper.getState() == ZooKeeper.States.CONNECTED) {
            return;
        }
        // close old zookeeper
        try {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        } catch (Exception e) {
            log.error("Exception happens when close zookeeper", e);
        }

        //  reconnect zookeeper 这里不要直接makeSureConnected() 会使应用挂起
        if (reconnectLatch.getCount() == 1) {
            log.info("reconnect(): notice to watcher to do makeSureConnected()");
            reconnectLatch.countDown();
        } else {
            log.info("reconnect(): already in retrying");
        }
    }

    // 自定义策略重试、该方法会使zokeeper一定连接上、否则不会返回
    private void makeSureConnected() {
        // 1 正常重试3次
        log.info("makeSureConnected: start to normal retry 3 times");
        int tryCount = 0;
        while (tryCount++ < 3) {
            if (tryConnect()) {
                log.info("zookeeper connected,sessionId=" + zooKeeper.getSessionId());
                return;
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {

            }
        }

        log.info("makeSureConnected: start to retry 50 times, interval 5 seconds");
        tryCount = 0;
        // 2 间隔5s、重试50次
        while (tryCount++ < 50) {
            if (tryConnect()) {
                log.info("zookeeper connected,sessionId=" + zooKeeper.getSessionId());
                return;
            }
            try {
                Thread.sleep(5000);
            } catch (Exception e) {

            }
        }

        log.info("makeSureConnected: start to retry util conected, interval 20 seconds");
        // 3 间隔20s、一直重试
        while (true) {
            if (tryConnect()) {
                log.info("zookeeper connected,sessionId=" + zooKeeper.getSessionId());
                return;
            }
            try {
                Thread.sleep(20000);
            } catch (Exception e) {

            }
        }
    }

    /**
     * if connected after try, return true, else return false
     *
     * */
    private boolean tryConnect() {
        // if it is already connected
        if (zooKeeper != null && zooKeeper.getState() == ZooKeeper.States.CONNECTED) {
            return true;
        }
        // close old
        try {
            if (zooKeeper != null) {
                zooKeeper.close();
            }
        } catch (Exception e) {
            log.error("close zookeeper Exception", e);
        }
        zooKeeper = null;
        // try to connect
        try {
            CountDownLatch connectedLatch = new CountDownLatch(1);
            zooKeeper = new ZooKeeper(connectString, sessionTimeOut, new SessionWatcher(connectedLatch));
            connectedLatch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("Exception happens when try to connect zookeeper", e);
        }
        // check if connected
        if (zooKeeper != null && zooKeeper.getState() == ZooKeeper.States.CONNECTED) {
            log.info("tryConnect() success, zookeeper get connected!");
            return true;
        }

        log.error("tryConnect() fail, zookeeper not connected!");
        return false;
    }

    class SessionWatcher implements Watcher {
        private CountDownLatch connectedLatch;

        SessionWatcher(CountDownLatch connectedLatch) {
            this.connectedLatch = connectedLatch;
        }

        @Override
        public void process(WatchedEvent event) {
            log.info("receive zookeeper event: " + event.toString());
            if (event.getState() == Event.KeeperState.SyncConnected) {
                connectedLatch.countDown();
            }
        }
    }
}