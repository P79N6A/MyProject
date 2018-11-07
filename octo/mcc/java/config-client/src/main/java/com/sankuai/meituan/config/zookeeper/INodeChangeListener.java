package com.sankuai.meituan.config.zookeeper;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-4
 */
public interface INodeChangeListener {
    /**
     * 创建、删除节点，修改数据都会触发
     * @see org.apache.zookeeper.ZooKeeper#exists(String, boolean)
     *
     * @param path
     */
    void updateExist(String path);
}
