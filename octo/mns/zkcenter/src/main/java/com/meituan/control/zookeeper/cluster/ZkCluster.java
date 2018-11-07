package com.meituan.control.zookeeper.cluster;

import java.util.Set;
import java.util.TreeSet;

import org.apache.zookeeper.ZooKeeper;


/**
 * User: jinmengzhe
 * Date: 2015-05-21
 * Desc:
 *      zk集群的描述：集群名、voter、observer
 */
public class ZkCluster {
    private String clusterName = "";
    private Set<ZkServer> voterSet = new TreeSet<ZkServer>();
    private Set<ZkServer> observerSet = new TreeSet<ZkServer>();

    private String connectString = "";
    private ZooKeeperConn connection = null;


    public ZkCluster(String clusterName, Set<ZkServer> voterSet, Set<ZkServer> observerSet) {
        this.clusterName = clusterName;
        this.voterSet = voterSet;
        this.observerSet = observerSet;

        buildConnectString();
        buildConnection();
    }

    public ZkServer getZkServer(String ipPortString) {
        String[] values = ipPortString.split(":");
        if (values != null && values.length == 2) {
            for (ZkServer server : voterSet) {
                if (server.getIp().equals(values[0]) && server.getPort().equals(values[1])) {
                    return server;
                }
            }
            for (ZkServer server : observerSet) {
                if (server.getIp().equals(values[0]) && server.getPort().equals(values[1])) {
                    return server;
                }
            }
        }

        return null;
    }

    public String getConnectString() {
        return connectString;
    }

    // Zookeeper is thread-safe
    public ZooKeeper getConnection() {
        return connection.getConnection();
    }

    public void triggerReconnect() {
        connection.triggerReconnect();
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Set<ZkServer> getVoterSet() {
        return voterSet;
    }

    public void setVoterSet(Set<ZkServer> voterSet) {
        this.voterSet = voterSet;
    }

    public Set<ZkServer> getObserverSet() {
        return observerSet;
    }

    public void setObserverSet(Set<ZkServer> observerSet) {
        this.observerSet = observerSet;
    }

    private void buildConnectString() {
        StringBuilder sb = new StringBuilder();
        boolean isFirstServer = true;
        for (ZkServer voter : voterSet) {
            if (isFirstServer) {
                isFirstServer = false;
            } else {
                sb.append(",");
            }
            sb.append(voter.getIp() + ":" + voter.getPort());
        }
        for (ZkServer observer : observerSet) {
            sb.append(",");
            sb.append(observer.getIp() + ":" + observer.getPort());
        }

        connectString = sb.toString();
    }

    private void buildConnection() {
        connection = new ZooKeeperConn(connectString, 3000);
    }
}
