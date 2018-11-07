package com.sankuai.meituan.config.zookeeper;

import org.junit.Test;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-4
 */
public class MtZooKeeperClientTest {

    @Test
    public void test() throws InterruptedException {
        MtZooKeeperClient mtZooKeeperClient = MtZooKeeperClient.getInstance("sgconfig-zk.sankuai.com:9331");
        mtZooKeeperClient.existAndListen("/config/zkclienttest", new NodeChangeListener());

        MtZooKeeperClient mtZooKeeperClient2 = MtZooKeeperClient.getInstance("sgconfig-zk.sankuai.com:9331");
        mtZooKeeperClient2.existAndListen("/config/test", new NodeChangeListener());
        mtZooKeeperClient2.existAndListen("/config/test", new NodeChangeListener());
        mtZooKeeperClient2.existAndListen("/config/test", new NodeChangeListener());
        mtZooKeeperClient2.existAndListen("/config/test", new NodeChangeListener());

        Thread.sleep(10000);

    }

    public class NodeChangeListener implements INodeChangeListener {
        @Override
        public void updateExist(String path) {
            System.out.println("NodeChangeListener: " + path);
        }
    }
}
