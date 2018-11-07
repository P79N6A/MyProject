package com.sankuai.octo.msgp.service.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkConnectionStateListener implements ConnectionStateListener {
    private final Logger logger = LoggerFactory.getLogger(ZkConnectionStateListener.class);

    private String zkRegPathPrefix;
    private String regContent;

    public ZkConnectionStateListener(String zkRegPathPrefix, String regContent) {
        this.zkRegPathPrefix = zkRegPathPrefix;
        this.regContent = regContent;
    }

    public void stateChanged(CuratorFramework client, ConnectionState state) {
        if (state == ConnectionState.LOST) {
            //连接丢失
            logger.error("lost session with zookeeper");
            int i = 0;
            while (true) {
                logger.info("trying to reconnect to zk:" + (i++));
                try {
                    if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                                .forPath(zkRegPathPrefix, regContent.getBytes("UTF-8"));
                        break;
                    }
                } catch (InterruptedException e) {
                    logger.error("reconnect zookeeper interrupted", e);
                    break;
                } catch (Exception e) {
                    logger.error("reconnect zookeeper error", e);
                }
            }
        } else if (state == ConnectionState.CONNECTED) {
            //连接新建
            logger.info("connected with zookeeper");
        } else if (state == ConnectionState.RECONNECTED) {
            logger.info("reconnected with zookeeper");
        }
    }
}
