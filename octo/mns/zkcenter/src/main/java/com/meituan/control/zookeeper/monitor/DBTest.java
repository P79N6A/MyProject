package com.meituan.control.zookeeper.monitor;

import com.meituan.control.zookeeper.db.C3p0PoolSource;
import com.meituan.control.zookeeper.db.MtDBClient;
import org.apache.log4j.PropertyConfigurator;

import java.util.List;

/**
 * User: jinmengzhe
 * Date: 2015-07-23
 */
public class DBTest {
    private final static String log4jPath = "/Users/jinmengzhe/IdeaProjects/zk-control-center/src/main/resources/conf/log4j.properties";
    private final static String c3p0Path = "/Users/jinmengzhe/IdeaProjects/zk-control-center/src/main/resources/conf/c3p0.xml";
    private final static String DB_NAME = "mobile_zk";
    public static void testQuery() {
        String sql = "select * from zk_monitor where timestamp > ?";
        try {
            PropertyConfigurator.configure(log4jPath);
            C3p0PoolSource.init(c3p0Path);
            List<Object> list = MtDBClient.executeQuery(ZkMonitor.class, sql, null, DB_NAME);
            for (Object object : list) {
                ZkMonitor monitor = (ZkMonitor) object;
                System.out.println(monitor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        testQuery();
    }
}