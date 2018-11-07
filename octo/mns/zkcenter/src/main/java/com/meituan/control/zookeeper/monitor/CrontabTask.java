package com.meituan.control.zookeeper.monitor;

import com.meituan.control.zookeeper.common.PropertiesHelper;
import com.meituan.control.zookeeper.db.C3p0PoolSource;
import com.meituan.control.zookeeper.db.MtDBClient;
import com.meituan.control.zookeeper.flwc.FlwcCmdUtil;
import com.meituan.control.zookeeper.flwc.MntrData;
import com.meituan.control.zookeeper.util.RuntimeUtil;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-07-24
 * Desc:
 * A crontab task to dump the monitor data of zookeeper to mysql
 */
@Component
public class CrontabTask {
    private final static Logger logger = Logger.getLogger(CrontabTask.class);
    private final static String LOG4J_PATH = "src/main/resources/conf/log4j.properties";
    private final static String C3P0_PATH = "src/main/resources/conf/c3p0.xml";
    private final static String SERVERS_CONFIG_PATH = "src/main/resources/conf/servers.properties";
    private final static String MNTR_CONF = "mntr/";
    private final static String SERVERS_KEY = "servers";
    private final static String DB_NAME = "mobile_zk";
    private final static String insertSQL = "insert into zk_monitor(server, znode_count, ephemerals_count, watch_count, num_alive_connections, max_latency, min_latency, avg_latency, packets_sent, packets_received, open_file_descriptor_count, max_file_descriptor_count, outstanding_requests, approximate_data_size) "
            + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    public boolean dumpServerMonitor(String ip, int port) {
        int tryTimes = 0;
        while (tryTimes++ < 5) {
            // 重试之前sleep一下
            if (tryTimes != 1) {
                try {
                    Thread.sleep(2000);
                } catch (Exception e) {

                }
            }
            try {
                MntrData data = FlwcCmdUtil.exeMntr(ip, port);
                if (data != null && data.allItemsFully()) {
                    MntrData lastData = readFromFile(ip, port);
                    writeToFile(data, ip, port);

                    if (lastData != null) {
                        Map<String, String> kvMap = data.getMonitorMap();
                        int znode_count = Integer.parseInt(kvMap.get("zk_znode_count"));
                        int ephemerals_count = Integer.parseInt(kvMap.get("zk_ephemerals_count"));
                        int watch_count = Integer.parseInt(kvMap.get("zk_watch_count"));
                        int num_alive_connections = Integer.parseInt(kvMap.get("zk_num_alive_connections"));
                        int max_latency = Integer.parseInt(kvMap.get("zk_max_latency"));
                        int min_latency = Integer.parseInt(kvMap.get("zk_min_latency"));
                        int avg_latency = Integer.parseInt(kvMap.get("zk_avg_latency"));
                        int open_file_descriptor_count = Integer.parseInt(kvMap.get("zk_open_file_descriptor_count"));
                        int max_file_descriptor_count = Integer.parseInt(kvMap.get("zk_max_file_descriptor_count"));
                        int outstanding_requests = Integer.parseInt(kvMap.get("zk_outstanding_requests"));
                        int approximate_data_size = Integer.parseInt(kvMap.get("zk_approximate_data_size"));
                        // 五分钟的值 maybe reset
                        long packets_sent = Long.parseLong(kvMap.get("zk_packets_sent"));
                        long packets_sent_last = Long.parseLong(lastData.getMonitorMap().get("zk_packets_sent"));
                        long packets_received = Long.parseLong(kvMap.get("zk_packets_received"));
                        long packets_received_last = Long.parseLong(lastData.getMonitorMap().get("zk_packets_received"));
                        if (packets_sent > Long.MAX_VALUE - 10000000 || packets_received > Long.MAX_VALUE - 10000000) {
                            FlwcCmdUtil.exeSrvr(ip, port);
                        }

                        int packets_sent_db = 0;
                        int packets_received_db = 0;
                        if (packets_sent > packets_sent_last) {
                            packets_sent_db = (int) (packets_sent - packets_sent_last);
                        } else {
                            packets_sent_db = (int) packets_sent;
                        }
                        if (packets_received > packets_received_last) {
                            packets_received_db = (int) (packets_received - packets_received_last);
                        } else {
                            packets_received_db = (int) packets_received;
                        }

                        if ((znode_count > -1) && (ephemerals_count > -1) && (watch_count > -1) && (num_alive_connections > -1)
                                && (max_latency > -1) && (min_latency > -1) && (avg_latency > -1) && (packets_sent_db > -1)
                                && (packets_received_db > -1) && (open_file_descriptor_count > -1) && (max_file_descriptor_count > -1)
                                && (outstanding_requests > -1) && (approximate_data_size > -1)) {

                            String server = ip + ":" + port;
                            int successCount = MtDBClient.executeUpdate(insertSQL,
                                    new Object[]{
                                            server, znode_count, ephemerals_count, watch_count,
                                            num_alive_connections, max_latency, min_latency, avg_latency,
                                            packets_sent_db, packets_received_db, open_file_descriptor_count, max_file_descriptor_count,
                                            outstanding_requests, approximate_data_size
                                    },
                                    DB_NAME);
                            if (1 == successCount) {
                                return true;
                            }
                        }
                    }
                } else {
                    logger.fatal("data is not fully.data=" + data);
                }
            } catch (Exception e) {
                logger.error("dumpServerMonitor fail.ip=" + ip + ", port=" + port, e);
            }
        }

        return false;
    }

    private static Set<String> initServerList(String configFilePath) {
        PropertiesHelper.init(configFilePath);
        System.out.println("path = " + configFilePath);
        String serverList = PropertiesHelper.getPropertiesValue(SERVERS_KEY);
        Set<String> serverSet = new HashSet<String>();
        System.out.println("size = " + serverSet.size());
        System.out.println("serverList = " + serverList);
        String[] items = serverList.split(";");
        for (String item : items) {
            serverSet.add(item);
        }

        return serverSet;
    }

    private static MntrData readFromFile(String ip, int port) {
        String fileName = ip + ":" + port + ".mntr";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(new File(MNTR_CONF + fileName)));
            Map<String, String> kvMap = new HashMap<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.trim().split("=");
                if (items != null && items.length == 2) {
                    kvMap.put(items[0].trim(), items[1].trim());
                }
            }

            return new MntrData(kvMap);
        } catch (Exception e) {
            logger.error("Exception happens in readFromFile().ip=" + ip + ",port=" + port, e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {

                }
            }
        }

        return null;
    }

    private static boolean writeToFile(MntrData data, String ip, int port) {
        String fileName = ip + ":" + port + ".mntr";
        BufferedWriter writer = null;
        try {
            if (data != null && data.getMonitorMap().size() > 0) {
                Map<String, String> kvMap = data.getMonitorMap();
                if (kvMap.containsKey("zk_packets_sent") && kvMap.containsKey("zk_packets_received")) {
                    writer = new BufferedWriter(new FileWriter(new File(MNTR_CONF + fileName)));
                    for (Map.Entry<String, String> entry : data.getMonitorMap().entrySet()) {
                        writer.write(entry.getKey().trim() + "=" + entry.getValue().trim());
                        writer.newLine();
                    }
                }
            }

            return true;
        } catch (Exception e) {
            logger.error("Exception happens in dumToFile().data=" + data.toString() + "ip=" + ip + ",port=" + port, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception e) {
                }
            }
        }

        return false;
    }

    @Scheduled(cron = "*/1 * * * * *")
    public void syncServerStatus() {
        try {
            logger.info("begin syncServerStatus...");
            String rootPath = RuntimeUtil.getRootResourcePath() + "/";
            System.out.println("rootPath = " + rootPath);
            // init log4j
            PropertyConfigurator.configure(rootPath + LOG4J_PATH);
            // init c3p0
            C3p0PoolSource.init(rootPath + C3P0_PATH);
            // get serverlist
            Set<String> serverSet = initServerList(rootPath + SERVERS_CONFIG_PATH);
            // do dump
            for (String server : serverSet) {
                String[] values = server.split(":");
                if (values.length == 2) {
                    String ip = values[0].trim();
                    int port = Integer.parseInt(values[1].trim());
                    if (dumpServerMonitor(ip, port)) {
                        logger.info("success to dump monitor for ipPort=" + server);
                    } else {
                        logger.error("fail to dump monitor for ipPort=" + server);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new CrontabTask().syncServerStatus();
    }
}
