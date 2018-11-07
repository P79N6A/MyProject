package com.meituan.control.zookeeper.flwc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-15
 * Desc:
 *      This class formats the 13 four letter words commands of zookeeper--
 *      transfer their outputs to the corresponding structured Data Object.
 *      So note that: the following code maybe seriously depends on the output's format, and
 *      if the output's format changged from zookeeper(maybe in new version), The codes of
 *      this class should be modified.---
 */
public class FlwcFormater {
    /**
     * 处理conf命令返回的结果
     * content like:
     * clientPort=2181
     * tickTime=2000
     * ...
     * ...
     * */
    public static ConfData formatConf(String content) {
        Map<String, String> configMap = new HashMap<String, String>();
        if (content != null && content.length() > 0) {
            String[] contentArray = content.split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (String line : contentArray) {
                    if (line != null && line.length() > 0) {
                        String[] kv = line.trim().split("=");
                        if (kv != null && kv.length == 2) {
                            configMap.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
            }
        }

        return new ConfData(configMap);
    }

    /**
     * 处理cons命令的返回
     * content like:
     * /10.4.36.152:23980[1](queued=0,recved=8698,sent=8698,sid=0x44d9623403daf02,lop=PING,est=1433941555709,to=30000,lcxid=0x4b5b,lzxid=0x3313d2996a,lresp=1434423475740,llat=0,minlat=0,avglat=0,maxlat=4)
     * ....
     * ....
     * */
    public static ConsData formatCons(String content) {
        Set<ConnectedClient> connectedClientSet = new HashSet<ConnectedClient>();
        if (content != null && content.length() > 0) {
            String[] contentArray = content.split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (String line : contentArray) {
                    if (line != null && line.length() > 0) {
                        line = line.trim();
                        int index1 = line.indexOf('[');
                        int index2 = line.indexOf('(');
                        int index3 = line.indexOf(')');
                        // 简要判别格式
                        if (index1 != -1 && index2 != -1 && index3 != -1) {
                            String ipPort = line.substring(1, index1);
                            if (ipPort != null && ipPort.contains(":")) {
                                String detailString = line.substring(index2 + 1, index3);
                                String[] detailsKv = detailString.split(",");
                                // 取detailMap
                                Map<String, String> detailMap = new HashMap<String, String>();
                                String sid = null;
                                for (String kv : detailsKv) {
                                    String[] keyValue = kv.split("=");
                                    if (keyValue.length == 2) {
                                        String key = keyValue[0];
                                        String value = keyValue[1];
                                        detailMap.put(key, value);
                                        if (key.equals("sid")) {
                                            sid = value;
                                        }
                                    }
                                }
                                if (sid != null) {
                                    connectedClientSet.add(new ConnectedClient(sid, ipPort, detailMap));
                                }
                            }
                        }
                    }
                }
            }
        }

        return new ConsData(connectedClientSet);
    }

    /**
     * 处理crst命令的返回
     * content is:
     * “Connection stats reset”
     * */
    public static CrstData formatCrst(String content) {
        return new CrstData(content);
    }

    /**
     * 处理dump命令的返回
     * content like:
     *  SessionTracker dump:
     *  Session Sets (21):
     *  2 expire at Wed Jun 17 17:17:30 CST 2015:
     *       0x34d96233f08793d
     *       0x14d962e64db4a75
     *  50 expire at Wed Jun 17 17:17:50 CST 2015:
     *       0x44d9623403e9c57
     *       0xc4db2dc1b341b52
     *       ...
     *  ...
     *  ...
     *  ephemeral nodes dump:
     *  Sessions with Ephemerals (11823):
     *  0x94d962b5eb02cca:
     *      /mtthrift/DEFAULT/hotel_data_service_recommend_online/clients/10.32.41.138:pid16960
     *      /mtthrift/DEFAULT/service_hbdata_mining_selectlog_production/clients/10.32.41.138:pid16960
     *      ...
     *  0x44d9623403de755:
     *      ...
     *      ...
     * */
    public static DumpData formatDump(String content) {
        String[] contentArray = content.split("\n");
        if (contentArray != null && contentArray.length > 0) {
            // expiredSessionMap
            Map<String, Set<String>> expiredSessionMap = new HashMap<String, Set<String>>();
            Map<String, Set<String>> sessionEphemeralsMap = new HashMap<String, Set<String>>();

            boolean reachExpiredSession = false;
            boolean reachEphemerals = false;
            for (int i = 0; i < contentArray.length; i++) {
                contentArray[i] = contentArray[i].trim();
                if (contentArray[i].contains("SessionTracker dump")) {
                    reachExpiredSession = true;
                }
                if (contentArray[i].contains("ephemeral nodes dump")) {
                    reachEphemerals = true;
                }
                // 寻找一个超时时间下面的所有session
                if (reachExpiredSession && contentArray[i].contains("expire at")) {
                    int index = contentArray[i].indexOf("expire at");
                    String time = contentArray[i].substring(index + "expire at".length()).trim();
                    time = time.substring(0, time.length() - 1);
                    Set<String> sessionIdSet = getExpiredSessionAfter(contentArray, i);
                    expiredSessionMap.put(time, sessionIdSet);
                }
                // 寻找一个session下面的所有临时节点
                if (reachEphemerals && contentArray[i].startsWith("0x") && contentArray[i].endsWith(":")) {
                    String sessionId = contentArray[i].substring(0, contentArray[i].length() - 1);
                    Set<String> ephemeralSet = getEphemeralsAfter(contentArray, i);
                    sessionEphemeralsMap.put(sessionId, ephemeralSet);
                }
            }

            return new DumpData(expiredSessionMap, sessionEphemeralsMap);
        }

        return null;
    }

    private static Set<String> getExpiredSessionAfter(String[] contentArray, int startIndex) {
        Set<String> result = new HashSet<String>();
        for (int i = startIndex + 1; i < contentArray.length; i++) {
            if (contentArray[i].contains("expire at") || contentArray[i].contains("ephemeral nodes dump")) {
                break;
            }
            if (contentArray[i].trim().startsWith("0x")) {
                result.add(contentArray[i].trim());
            }
        }

        return result;
    }

    private static Set<String> getEphemeralsAfter(String[] contentArray, int startIndex) {
        Set<String> result = new HashSet<String>();
        for (int i = startIndex + 1; i < contentArray.length; i++) {
            contentArray[i] = contentArray[i].trim();
            if ((contentArray[i].startsWith("0x") &&  contentArray[i].endsWith(":"))
                    || contentArray[i].contains("SessionTracker dump")) {
                break;
            }
            if (contentArray[i].startsWith("/")) {
                result.add(contentArray[i].trim());
            }
        }

        return result;
    }

    /**
     * 处理envi命令的返回,content like:
     *  Environment:
     *  zookeeper.version=3.4.4-1386507, built on 09/17/2012 08:33 GMT
     *  host.name=mobile-zk02.lf.sankuai.com
     *  java.version=1.6.0_35
     *  ...
     *  ...
     * */
    public static EnviData formatEnvi(String content) {
        Map<String, String> enviMap = new HashMap<String, String>();
        if (content != null && content.length() > 0) {
            String[] contentArray = content.split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (String line : contentArray) {
                    if (line != null && line.length() > 0) {
                        String[] kv = line.trim().split("=");
                        if (kv != null && kv.length == 2) {
                            enviMap.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
            }
        }

        return new EnviData(enviMap);
    }

    /**
     * 处理ruok命令的返回 content is:
     * imok
     * */
    public static RuokData formatRuok(String content) {
        return new RuokData(content);
    }

    /**
     * 处理srst命令的返回 content is:
     * Server stats reset.
     * */
    public static SrstData formatSrst(String content) {
        return new SrstData(content);
    }

    /**
     * 处理srvr命令的返回 content like:
     * Zookeeper version: 3.4.4-1386507, built on 09/17/2012 08:33 GMT
     * Latency min/avg/max: 0/0/15
     * Received: 204036
     * ...
     * ...
     * */
    public static SrvrData formatSrvr(String content) {
        Map<String, String> srvrMap = new HashMap<String, String>();
        if (content != null && content.length() > 0) {
            String[] contentArray = content.split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (String line : contentArray) {
                    if (line != null && line.length() > 0) {
                        String[] kv = line.trim().split(":", 2);
                        if (kv != null && kv.length == 2) {
                            srvrMap.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
            }
        }

        return new SrvrData(srvrMap);
    }

    /**
     * 处理stat命令的返回 content like:
     *  Zookeeper version: 3.4.4-1386507, built on 09/17/2012 08:33 GMT
     *  Clients:
     *  /10.64.43.199:31563[1](queued=0,recved=492,sent=492)
     *  ...
     *  ...
     *  /10.32.26.236:34480[1](queued=0,recved=2232,sent=2232)
     *  /10.32.42.138:22173[1](queued=0,recved=30777,sent=30777)
     *
     *  Latency min/avg/max: 0/0/87
     *  Received: 626532
     *  Sent: 631017
     *  Connections: 1399
     *  Outstanding: 0
     *  Zxid: 0x331430121b
     *  Mode: leader
     *  Node count: 62506
     * */
    public static StatData formatStat(String content) {
        Set<ConnectedClient> clientSet = new HashSet<ConnectedClient>();
        Map<String, String> statisticMap = new HashMap<String, String>();
        if (content != null && content.trim().length() > 0) {
            String[] contentArray = content.trim().split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (int i = 0; i < contentArray.length; i++) {
                    if (contentArray[i].contains("Clients:")) {
                        clientSet = getClientSetAfter(contentArray, i);
                    }
                    if (contentArray[i].contains("Latency min/avg/max")) {
                        statisticMap = getStatisticMapAfter(contentArray, i);
                    }
                }
            }
        }

        return new StatData(clientSet, statisticMap);
    }

    private static Set<ConnectedClient> getClientSetAfter(String[] contentArray, int index) {
        Set<ConnectedClient> result = new HashSet<ConnectedClient>();
        for (int i = index + 1; i < contentArray.length; i++) {
            contentArray[i] = contentArray[i].trim();
            if (contentArray[i].startsWith("/") && contentArray[i].contains("queued")) {
                int index1 = contentArray[i].indexOf('[');
                int index2 = contentArray[i].indexOf('(');
                int index3 = contentArray[i].indexOf(')');
                if (index1 != -1 && index2 != -1 && index3 != -1) {
                    String ipPort = contentArray[i].substring(1, index1);
                    if (ipPort != null && ipPort.contains(":")) {
                        String detailString = contentArray[i].substring(index2 + 1, index3);
                        String[] detailsKv = detailString.split(",");
                        // 取detailMap
                        Map<String, String> detailMap = new HashMap<String, String>();
                        for (String kv : detailsKv) {
                            String[] keyValue = kv.split("=");
                            if (keyValue.length == 2) {
                                String key = keyValue[0].trim();
                                String value = keyValue[1].trim();
                                detailMap.put(key, value);
                            }
                        }
                        // sid is empty
                        result.add(new ConnectedClient("", ipPort, detailMap));
                    }
                }
            }
        }

        return result;
    }

    private static Map<String, String> getStatisticMapAfter(String[] contentArray, int index) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = index; i < contentArray.length; i++) {
            contentArray[i] = contentArray[i].trim();
            if (contentArray[i] != null && contentArray[i].length() > 0) {
                String[] kv = contentArray[i].trim().split(":", 2);
                if (kv != null && kv.length == 2) {
                    result.put(kv[0].trim(), kv[1].trim());
                }
            }
        }

        return result;
    }

    /**
     * 处理wchc命令的返回 content like:
     * 0x44d962340406d84
     *      /redisClusters/mobile-rec-data/conf_cfdnebebfdccdbdgidddgdgggdghggcggh
     *      /redisClusters/dataapp-recsys-cache01/conf_aojehhmldhfccnfcekgjfghdgdgffdeheegeefgf
     *      ...
     * 0x44d9623403a18de
     *      /redisClusters/zhifu_bankgate/conf_jbefdicddbfedgdggdhdfdgd
     *      ...
     * ...
     * ...
     * */
    public static WchcData formatWchc(String content) {
        Map<String, Set<String>> session2WatchedPathMap = new HashMap<String, Set<String>>();
        if (content != null && content.trim().length() > 0) {
            String[] contentArray = content.trim().split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (int i = 0; i < contentArray.length; i++) {
                    contentArray[i] = contentArray[i].trim();
                    if (contentArray[i].startsWith("0x")) {
                        String sid = contentArray[i];
                        Set<String> watchedPathSet = getWatchedPathSetAfter(contentArray, i);
                        session2WatchedPathMap.put(sid, watchedPathSet);
                    }
                }
            }
        }

        return new WchcData(session2WatchedPathMap);
    }

    private static Set<String> getWatchedPathSetAfter(String[] contentArray, int index) {
        Set<String> result = new HashSet<String>();
        for (int i = index + 1; i < contentArray.length; i++) {
            contentArray[i] = contentArray[i].trim();
            if (contentArray[i].startsWith("0x")) {
                break;
            }
            if (contentArray[i].startsWith("/")) {
                result.add(contentArray[i]);
            }
        }
        return result;
    }

    /**
     * 处理wchs命令的返回 content is:
     *  851 connections watching 760 paths
     *  Total watches:25095
     * */
    public static WchsData formatWchs(String content) {
        return new WchsData(content);
    }

    /**
     * 处理wchp命令的返回 content like:
     * /config/mobile/mq/mafka_config/dx_push
     *      0x44d9623403b98d5
     *      0x44d9623403b98e0
     *      ...
     * /com/meituan/mobile/activemq/config/011_hotel_mta_partner_goods
     *      0x44d962340404f28
     *      ...
     *      ...
     * */
    public static WchpData formatWchp(String content) {
        Map<String, Set<String>> watchedPath2SessionsMap = new HashMap<String, Set<String>>();
        if (content != null && content.trim().length() > 0) {
            String[] contentArray = content.trim().split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (int i = 0; i < contentArray.length; i++) {
                    contentArray[i] = contentArray[i].trim();
                    if (contentArray[i].startsWith("/")) {
                        String watchedPath = contentArray[i];
                        Set<String> sessionSet = getSessionSetAfter(contentArray, i);
                        watchedPath2SessionsMap.put(watchedPath, sessionSet);
                    }
                }
            }
        }

        return new WchpData(watchedPath2SessionsMap);
    }

    private static Set<String> getSessionSetAfter(String[] contentArray, int index) {
        Set<String> result = new HashSet<String>();
        for (int i = index + 1; i < contentArray.length; i++) {
            contentArray[i] = contentArray[i].trim();
            if (contentArray[i].startsWith("/")) {
                break;
            }
            if (contentArray[i].startsWith("0x")) {
                result.add(contentArray[i]);
            }
        }

        return result;
    }

    /**
     * 处理mntr命令的返回 content like:
     *  zk_version	3.4.4-1386507, built on 09/17/2012 08:33 GMT
     *  zk_avg_latency	0
     *  zk_max_latency	93
     *  zk_min_latency	0
     *  zk_packets_received	3633675
     *  ...
     *  ...
     * */
    public static MntrData formatMntr(String content) {
        Map<String, String> monitorMap = new HashMap<String, String>();
        if (content != null && content.trim().length() > 0) {
            String[] contentArray = content.trim().split("\n");
            if (contentArray != null && contentArray.length > 0) {
                for (String line : contentArray) {
                    if (line != null && line.trim().length() > 0) {
                        String[] kv = line.trim().split("\t", 2);
                        if (kv != null && kv.length == 2) {
                            monitorMap.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
            }
        }

        return new MntrData(monitorMap);
    }
}
