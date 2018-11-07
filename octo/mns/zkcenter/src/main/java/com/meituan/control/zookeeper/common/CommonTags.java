package com.meituan.control.zookeeper.common;

import java.util.HashSet;
import java.util.Set;
import com.meituan.control.zookeeper.cluster.ZkCluster;

/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class CommonTags {
//    public static String CONF_DIR = "src/main/resources/conf/";
    public static String CONF_DIR = "";

    public static class ReqTypes {
        public static final String TEST = "test";
        public static final String HEALTH_CHECK = "health";

        public static final String REPORT_ZK_CLIENT_INFO = "report_zk_client_info";
        public static final String GET_CLUSTERS_DEPLOY = "get_clusters_deploy";

        public static final String GET_NODE_INFO = "get_node_info";
        public static final String GET_JMX = "get_jmx";
        public static final String GET_FLWC = "get_flwc";
        public static final String GET_MONITOR = "get_monitor";


    }

    public static class ErrorCode {
        public static final int OK = 200;
        public static final int ERROR_FAILED = 400;
        public static final int ERROR_RESPONSE_FAILED = 401;
    }

    public static class RequestKey {
        public static final String REQTYPE = "reqtype";

        public static final String CLUSTER_NAME = "cluster_name";
        public static final String NODE_NAME = "node_name";

        public static final String SERVER = "server";

        public static final String CMD = "cmd";

        public static final String SCOPE = "scope";
        public static final String HOUR_SCOPE = "hour";
        public static final String DAY_SCOPE = "day";
        public static final String WEEK_SCOPE = "week";
        public static final String MONTH_SCOPE = "month";
    }

    public static class ResponseKey {
        public static final String CLUSTERS = "clusters";

        public static final String CLUSTER_NAME = "cluster_name";
        public static final String VOTERS = "voters";
        public static final String OBSERVERS = "observers";

        public static final String DATA =  "data";
        public static final String STAT = "stat";
        public static final String CHILDREN = "children";

        public static final String REPLICATED_SERVER_OBJECT = "ReplicatedServerObject";
        public static final String REPLICA_OBJECT = "ReplicaObject";
        public static final String LEADER_OR_FOLLOWER_OBJECT = "LeaderOrFollowerObject";
        public static final String INMEMORY_DATATREE_OBJECT = "InMemoryDataTreeObject";

        public static final String CONNECTED_CLIENT_SET_CN = "客户端连接";

        public static final String OP_RESULT = "result";

        public static final String EXPIRED_SESSIONS_CN = "超时Session";
        public static final String EPHEMERALS_CN = "临时节点";

        public static final String STATISTIC_MAP_CN = "状态统计";
    }

    public static class ConfigString {
        public static final String CLUSTER_NAMES = "cluster_names";
        public static final String CLUSTER_PREFIX = "cluster_";
        public static final String VOTER = "voter";
        public static final String OBSERBER = "observer";


    }

    public static class Configs {
        public static Set<ZkCluster> zkClusters = new HashSet<ZkCluster>();
    }
}
