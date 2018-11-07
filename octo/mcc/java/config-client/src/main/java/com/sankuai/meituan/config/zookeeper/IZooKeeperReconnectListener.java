package com.sankuai.meituan.config.zookeeper;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-4
 */
public interface IZooKeeperReconnectListener {
    // 重连接zookeeper服务器
    void reconnected();
}
