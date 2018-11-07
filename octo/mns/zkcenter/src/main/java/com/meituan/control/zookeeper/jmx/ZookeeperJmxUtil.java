package com.meituan.control.zookeeper.jmx;

import org.apache.log4j.Logger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * User: jinmengzhe
 * Date: 2015-06-11
 * Desc:
 *       The objectName in zk jmx is like this:
 *       1,InMemoryDataTree:
 *          org.apache.ZooKeeperService:name0=ReplicatedServer_id1,name1=replica.1,name2=Follower,name3=InMemoryDataTree
 *       2,Follower/Leader
 *          org.apache.ZooKeeperService:name0=ReplicatedServer_id1,name1=replica.1,name2=Follower
 *       3, replica.1/2/3/....
 *          org.apache.ZooKeeperService:name0=ReplicatedServer_id1,name1=replica.1
 *       4, ReplicatedServer_id1/2/3/...
 *          org.apache.ZooKeeperService:name0=ReplicatedServer_id1
 *
 *       @See jconsole to be easy looked
 *       注意这个类是与zk的jmx暴漏项的形式密切相关的、如果zk版本发生变化导致暴漏项形式变了、代码要做相应改动
 *
 *       Note：发现有时候name3会有出现Connections的情况、要注意objectName的获取方式
 *
 */
public class ZookeeperJmxUtil {
    private final static Logger logger = Logger.getLogger(ZookeeperJmxUtil.class);
    private final static String zkDomain = "org.apache.ZooKeeperService";
    private final static String inMemoryDataTreeObjectSuffix = "name3=InMemoryDataTree";

    public static ZkJmxDetail getZkJmxDetail(String jmxIp, String jmxPort) throws Exception {
        String jmxAddress = buildJmxConnectString(jmxIp, jmxPort);
        JMXServiceURL url = new JMXServiceURL(jmxAddress);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

        ObjectName inMemoryDataTreeObject = getInMemoryDataTreeObjectName(mbsc);
        Map<String, String> inMemoryDataTreeMap = getAllAttributesMap(mbsc, inMemoryDataTreeObject);

        ObjectName leaderOrFollowerObject = getLeaderOrFollowerObjectName(mbsc);
        Map<String, String> leaderOrFollowerMap = getAllAttributesMap(mbsc, leaderOrFollowerObject);

        ObjectName replicaObject = getReplicaObjectName(mbsc);
        Map<String, String> replicaMap = getAllAttributesMap(mbsc, replicaObject);

        ObjectName replicatedServerObject = getReplicatedServerObjectName(mbsc);
        Map<String, String> replicatedServerMap = getAllAttributesMap(mbsc, replicatedServerObject);

        jmxConnector.close();
        ZkJmxDetail result = new ZkJmxDetail(inMemoryDataTreeMap, leaderOrFollowerMap, replicaMap, replicatedServerMap);
        return result;
    }

    private static Map<String, String> getAllAttributesMap(MBeanServerConnection mbsc, ObjectName objectName) throws Exception {
        Map<String, String> result = new HashMap<String, String>();
        MBeanInfo mbInfo = mbsc.getMBeanInfo(objectName);
        MBeanAttributeInfo[] attrInfo = mbInfo.getAttributes();
        for (MBeanAttributeInfo mbai : attrInfo) {
            result.put(mbai.getName(), mbsc.getAttribute(objectName, mbai.getName()).toString());
        }

        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    // 获取InMemoryDataTreeObjectName
    private static ObjectName getInMemoryDataTreeObjectName(MBeanServerConnection mbsc) throws Exception {
        ObjectName result = null;
        Set<ObjectName> objectNameSet = mbsc.queryNames(null, null);
        for (ObjectName objectName : objectNameSet) {
            if (objectName.getDomain().equals(zkDomain) && objectName.toString().endsWith(inMemoryDataTreeObjectSuffix)) {
                result = objectName;
                break;
            }
        }

        return result;
    }

    // 获取LeaderOrFollowerObjectName
    private static ObjectName getLeaderOrFollowerObjectName(MBeanServerConnection mbsc) throws Exception {
        ObjectName inMemoryDataTreeObjectName = getInMemoryDataTreeObjectName(mbsc);
        int index = inMemoryDataTreeObjectName.toString().lastIndexOf(',');
        String resultName = inMemoryDataTreeObjectName.toString().substring(0, index);

        return new ObjectName(resultName);
    }

    // 获取ReplicaObjectName
    private static ObjectName getReplicaObjectName(MBeanServerConnection mbsc) throws Exception {
        ObjectName leaderOrFollowerObjectName = getLeaderOrFollowerObjectName(mbsc);
        int index = leaderOrFollowerObjectName.toString().lastIndexOf(',');
        String resultName = leaderOrFollowerObjectName.toString().substring(0, index);

        return new ObjectName(resultName);
    }

    // 获取ReplicatedServerObjectName
    private static ObjectName getReplicatedServerObjectName(MBeanServerConnection mbsc) throws Exception {
        ObjectName replicaObjectName = getReplicaObjectName(mbsc);
        int index = replicaObjectName.toString().lastIndexOf(',');
        String resultName = replicaObjectName.toString().substring(0, index);

        return new ObjectName(resultName);
    }

    public static void printlnAllObjectName(String jmxIp, String jmxPort) throws Exception {
        String jmxAddress = buildJmxConnectString(jmxIp, jmxPort);
        System.out.println("jmxAddress=" + jmxAddress);

        JMXServiceURL url = new JMXServiceURL(jmxAddress);
        JMXConnector jmxConnector = JMXConnectorFactory.connect(url, null);
        MBeanServerConnection mbsc = jmxConnector.getMBeanServerConnection();

        Set<ObjectName> objectNameSet = mbsc.queryNames(null, null);
        for (ObjectName objectName : objectNameSet) {
            System.out.println(objectName.toString());
        }

        jmxConnector.close();
    }

    private static String buildJmxConnectString(String jmxIp, String jmxPort) {
        return "service:jmx:rmi:///jndi/rmi://"
                + jmxIp + ":" + jmxPort
                + "/jmxrmi";
    }

    public static void main(String[] args) {
        try {
            String jmxIp = "192.168.2.225";
            //String jmxIp = "192.168.60.199";
            String jmxPort = "5001";

            ZkJmxDetail result = ZookeeperJmxUtil.getZkJmxDetail(jmxIp, jmxPort);
            System.out.println(result);

            //ZookeeperJmxUtil.printlnAllObjectName(jmxIp, jmxPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
