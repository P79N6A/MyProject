package com.sankuai.meituan.config.test
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.netflix.curator.framework.CuratorFrameworkFactory
import com.netflix.curator.retry.ExponentialBackoffRetry
import com.sankuai.meituan.config.service.PropertySerializeService

import com.sankuai.meituan.zkclient.ZookeeperConfig
import groovy.transform.ToString
import org.junit.After
import org.junit.Before
import org.junit.Test

import javax.annotation.Resource

class CurrentConfigUsageTest {
    static final def TEST_ZK_IPS = "192.168.2.95:9331,192.168.2.245:9331,192.168.2.209:9331"
    static final def MTCONFIG_ROOT_PATH = "/config"
    def client = CuratorFrameworkFactory.builder()
            .connectString(TEST_ZK_IPS).retryPolicy(new ExponentialBackoffRetry(1000, Integer.MAX_VALUE))
            .connectionTimeoutMs(ZookeeperConfig.CONNECT_TIMEOUT).sessionTimeoutMs(ZookeeperConfig.SESSION_TIMEOUT)
            .build()
    def propertySerializeService = new PropertySerializeService()


    @Test
    void testGetNotExistNode() {
        println(client.checkExists().forPath("/config/mtconfig").version)
    }

    @Test
    void getMtConfigData() {
        Map<String, List<NodePath>> appRoots = Maps.newHashMap()
        client.getChildren().forPath(MTCONFIG_ROOT_PATH).each {
            def currentPath = "$MTCONFIG_ROOT_PATH/$it"
            if (it == "mobile") {
                client.getChildren().forPath(currentPath).each {
                    appRoots.put("$currentPath/$it", Lists.newArrayList())
                }
            } else {
                appRoots.put(currentPath, Lists.newArrayList())
            }
        }
        appRoots.each {
            getChildNodeData(it.value, it.key, getData(it.key))
        }

        println("统计数据")
        appRoots.each {
            def maxDataSize = humanReadableByteCount(Collections.max(it.value.collect { it.dataByteSize }) as long)
            def leafPathCount = it.value.findAll { it.isLeaf }.size()
            println("app:$it.key, maxDataSize:$maxDataSize, allPathCount:${it.value.size()}, leafPathCount:$leafPathCount")
        }
    }

    @Test
    void testGetStat() {
        println(client.checkExists().forPath("/null"))
    }

    def getChildNodeData(List<NodePath> appPaths, String currentPath, Map<String, String> parentData) {
        def currentNodeData = mergeData(parentData, getData(currentPath))
        def childNodes = client.getChildren().forPath(currentPath)
        def currentDataSize = currentNodeData.values().collect { it.toString().bytes.size() }.sum() ?: 0
        appPaths.add(new NodePath(path: currentPath, isLeaf: childNodes.isEmpty(), dataByteSize: currentDataSize,
                dataSize: currentNodeData.size()))
        if (!childNodes.isEmpty()) {
            childNodes.each { getChildNodeData(appPaths, "$currentPath/$it", currentNodeData) }
        }
    }

    static def mergeData(Map<String, String> parentData, Map<String, String> currentNodeData) {
        def mergeData = Maps.newHashMap()
        mergeData.putAll(parentData)
        mergeData.putAll(currentNodeData)
        mergeData
    }

    def getData(String path) {
        def data = Maps.newHashMap()
        propertySerializeService.deSerializePropertyValueAsList(client.getData().forPath(path)).each {
            data.put(it.key, "$it.key:$it.value:$it.comment")
        }
        data
    }

    static String humanReadableByteCount(long bytes, boolean si = false) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    @Before
    void init() {
        client.start();
    }

    @After
    void finish() {
        client.close()
    }

    @ToString(includePackage = false, includeNames = true)
    static class NodePath {
        def path
        def isLeaf
        def dataByteSize
        def dataSize
    }
}
