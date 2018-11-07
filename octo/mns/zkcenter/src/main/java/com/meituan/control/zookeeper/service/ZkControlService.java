package com.meituan.control.zookeeper.service;

import java.util.List;
import java.util.Set;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import com.meituan.control.zookeeper.flwc.*;
import com.meituan.control.zookeeper.jmx.*;
import com.meituan.control.zookeeper.util.StringUtil;
import com.meituan.control.zookeeper.cluster.ZkCluster;
import com.meituan.control.zookeeper.cluster.ZkServer;
import com.meituan.control.zookeeper.common.CommonTags;
import com.meituan.control.zookeeper.monitor.ZkMonitorUtil;


/**
 * User: jinmengzhe
 * Date: 2015-05-20
 */
public class ZkControlService {
	private final static Logger logger = Logger.getLogger(ZkControlService.class);
	
	public ZkControlService() {
		
	}
	
	public ResonseContext testMethod(RequestContext context) {
		ResonseContext result = new ResonseContext();
		try {
			try {
				Thread.sleep(100);
			} catch (Exception e) {

			}
			JSONObject data = new JSONObject();
			data.put("jinmengzhe","jinmengzhe");
			result.data = data;
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in testMethod", e);
		}

		return result;
	}

	public ResonseContext reportZkClientInfo(RequestContext context) {
		ResonseContext result = new ResonseContext();
		try {
			// just record the client info
			if (context.data != null) {
				logger.info("report zk-client-info:\n" + context.data.toString(1));
			}
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in reportZkClientInfo", e);
		}

		return result;
	}

	public ResonseContext getClustersDeploy(RequestContext context) {
		ResonseContext result = new ResonseContext();
		try {
			// just record the client info
			JSONObject data = new JSONObject();
			JSONArray clustersArray = new JSONArray();
			for (ZkCluster cluster : CommonTags.Configs.zkClusters) {
				String clusterName = cluster.getClusterName();
				Set<ZkServer> voterSet = cluster.getVoterSet();
				Set<ZkServer> observerSet = cluster.getObserverSet();
				// json construct
				JSONObject clusterJsonObject = new JSONObject();
				JSONArray voterArray = new JSONArray();
				JSONArray observerArray = new JSONArray();
				for (ZkServer voter : voterSet) {
					voterArray.add(voter.getIp() + ":" + voter.getPort());
				}
				for (ZkServer observer : observerSet) {
					observerArray.add(observer.getIp() + ":" + observer.getPort());
				}
				clusterJsonObject.put(CommonTags.ResponseKey.CLUSTER_NAME, clusterName);
				clusterJsonObject.put(CommonTags.ResponseKey.VOTERS, voterArray);
				clusterJsonObject.put(CommonTags.ResponseKey.OBSERVERS, observerArray);

				clustersArray.add(clusterJsonObject);
			}
			data.put(CommonTags.ResponseKey.CLUSTERS, clustersArray);

			result.data = data;
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in getClustersDeploy", e);
		}

		logger.info(result.data.toString(1));
		return result;
	}

	public ResonseContext zkWebGetNodeInfo(RequestContext context) {
		ResonseContext result = new ResonseContext();
		ZkCluster zkCluster = null;
		try {
			String clusterName = context.data.getString(CommonTags.RequestKey.CLUSTER_NAME);
			String nodeName = context.data.getString(CommonTags.RequestKey.NODE_NAME);
			if (StringUtil.isEmpty(clusterName) || StringUtil.isEmpty(nodeName)) {
				throw new Exception("cluster_name or node_name not find");
			}
			for (ZkCluster cluster : CommonTags.Configs.zkClusters) {
				if (clusterName.equals(cluster.getClusterName())) {
					zkCluster = cluster;
				}
			}
			if (zkCluster == null) {
				throw new Exception("cluster_name not exist");
			}
			// get from zookeeper
			ZooKeeper zooKeeper = zkCluster.getConnection();
			Stat stat = new Stat();
			byte[] data = zooKeeper.getData(nodeName, null, stat);
			List<String> children = zooKeeper.getChildren(nodeName, null, null);

			// construct result
			JSONObject resultJsonObject = new JSONObject();
			String dataString = "";
			if (data != null && data.length > 0) {
				dataString = new String(data, "utf-8");
			}
			// 1) data
			resultJsonObject.put(CommonTags.ResponseKey.DATA, dataString);
			// 2) stat 不关心具体key value 利用反射取出来即可
			JSONObject statJsonObject = new JSONObject();
			for (Field field : Stat.class.getDeclaredFields()) {
				String fieldName = field.getName();
				String methodName = "get" + (char) (fieldName.charAt(0) - 32) + fieldName.substring(1);
				Method method = Stat.class.getMethod(methodName);
				String fieldValue = method.invoke(stat).toString();
				statJsonObject.put(fieldName, fieldValue);
			}
			resultJsonObject.put(CommonTags.ResponseKey.STAT, statJsonObject);
			// 3) children
			JSONArray childrenJsonArray = new JSONArray();
			for (String child : children) {
				childrenJsonArray.add(child);
			}
			resultJsonObject.put(CommonTags.ResponseKey.CHILDREN, childrenJsonArray);

			result.data = resultJsonObject;
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in zkWebGetNodeInfo", e);
			checkZkClusterState(zkCluster);
		}

		logger.info(result.data.toString(1));
		return result;
	}

	public ResonseContext getJmxInfo(RequestContext context) {
		ResonseContext result = new ResonseContext();
		try {
			String ipPortString = context.data.getString(CommonTags.RequestKey.SERVER);
			ZkServer zkServer = queryZkServer(ipPortString);
			if (zkServer == null) {
				throw new Exception("server=" + ipPortString + " is not exist");
			}
			ZkJmxDetail jmxDetail = ZookeeperJmxUtil.getZkJmxDetail(zkServer.getIp(), zkServer.getJmxPort());
			// do jmx
			JSONObject data = new JSONObject();
			data.put(CommonTags.ResponseKey.INMEMORY_DATATREE_OBJECT, JSONObject.fromObject(jmxDetail.getInMemoryDataTreeMap()));
			data.put(CommonTags.ResponseKey.LEADER_OR_FOLLOWER_OBJECT, JSONObject.fromObject(jmxDetail.getLeaderOrFollowerMap()));
			data.put(CommonTags.ResponseKey.REPLICA_OBJECT, JSONObject.fromObject(jmxDetail.getReplicaMap()));
			data.put(CommonTags.ResponseKey.REPLICATED_SERVER_OBJECT, JSONObject.fromObject(jmxDetail.getReplicatedServerMap()));

			result.data = data;
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in getJmxInfo", e);
		}

		logger.info(result.data.toString(1));
		return result;
	}

	public ResonseContext getFlwcInfo(RequestContext context) {
		ResonseContext result = new ResonseContext();
		try {
			String ipPortString = context.data.getString(CommonTags.RequestKey.SERVER);
			ZkServer zkServer = queryZkServer(ipPortString);
			if (zkServer == null) {
				throw new Exception("server=" + ipPortString + " is not exist");
			}
			String ip = zkServer.getIp();
			int port = Integer.parseInt(zkServer.getPort());
			String cmd = context.data.getString(CommonTags.RequestKey.CMD);

			JSONObject data = new JSONObject();
			switch (cmd) {
				case FlwcCmd.CONF:
					ConfData confData = FlwcCmdUtil.exeConf(ip, port);
					data = FlwcJsonUtil.buidConfJsonObject(confData);
					break;
				case FlwcCmd.CONS:
					ConsData consData = FlwcCmdUtil.exeCons(ip, port);
					data = FlwcJsonUtil.buidConsJsonObject(consData);
					break;
				case FlwcCmd.CRST:
					CrstData crstData = FlwcCmdUtil.exeCrst(ip, port);
					data = FlwcJsonUtil.buidCrstJsonObject(crstData);
					break;
				case FlwcCmd.DUMP:
					DumpData dumpData = FlwcCmdUtil.exeDump(ip, port);
					data = FlwcJsonUtil.buidDumpJsonObject(dumpData);
					break;
				case FlwcCmd.ENVI:
					EnviData enviData = FlwcCmdUtil.exeEnvi(ip, port);
					data = FlwcJsonUtil.buidEnviJsonObject(enviData);
					break;
				case FlwcCmd.MNTR:
					MntrData mntrData = FlwcCmdUtil.exeMntr(ip, port);
					data = FlwcJsonUtil.buidMntrJsonObject(mntrData);
					break;
				case FlwcCmd.RUOK:
					RuokData ruokData = FlwcCmdUtil.exeRuok(ip, port);
					data = FlwcJsonUtil.buidRuokJsonObject(ruokData);
					break;
				case FlwcCmd.SRST:
					SrstData srstData = FlwcCmdUtil.exeSrst(ip, port);
					data = FlwcJsonUtil.buidSrstJsonObject(srstData);
					break;
				case FlwcCmd.SRVR:
					SrvrData srvrData = FlwcCmdUtil.exeSrvr(ip, port);
					data = FlwcJsonUtil.buidSrvrJsonObject(srvrData);
					break;
				case FlwcCmd.STAT:
					StatData statData = FlwcCmdUtil.exeStat(ip, port);
					data = FlwcJsonUtil.buidStatJsonObject(statData);
					break;
				case FlwcCmd.WCHC:
					WchcData wchcData = FlwcCmdUtil.exeWchc(ip, port);
					data = FlwcJsonUtil.buidWchcJsonObject(wchcData);
					break;
				case FlwcCmd.WCHP:
					WchpData wchpData = FlwcCmdUtil.exeWchp(ip, port);
					data = FlwcJsonUtil.buidWchpJsonObject(wchpData);
					break;
				case FlwcCmd.WCHS:
					WchsData wchsData = FlwcCmdUtil.exeWchs(ip, port);
					data = FlwcJsonUtil.buidWchsJsonObject(wchsData);
					break;
				default:
					throw new Exception("cmd=" + cmd + " is not support!");
			}

			result.data = data;
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in getFlwcInfo", e);
		}

		logger.info(result.data.toString(1));
		return result;
	}

	public ResonseContext getMonitor(RequestContext context) {
		ResonseContext result = new ResonseContext();
		try {
			String ipPortString = context.data.getString(CommonTags.RequestKey.SERVER);
			ZkServer zkServer = queryZkServer(ipPortString);
			if (zkServer == null) {
				throw new Exception("server=" + ipPortString + " is not exist");
			}
			JSONObject data;
			String scope = context.data.getString(CommonTags.RequestKey.SCOPE);
			switch (scope) {
				case CommonTags.RequestKey.HOUR_SCOPE:
					data = ZkMonitorUtil.buildHourMonitorResponse(ipPortString);
					break;
				case CommonTags.RequestKey.DAY_SCOPE:
					data = ZkMonitorUtil.buildDayMonitorResponse(ipPortString);
					break;
				case CommonTags.RequestKey.WEEK_SCOPE:
					data = ZkMonitorUtil.buildWeekMonitorResponse(ipPortString);
					break;
				case CommonTags.RequestKey.MONTH_SCOPE:
					data = ZkMonitorUtil.buildMonthMonitorResponse(ipPortString);
					break;
				default:
					throw new Exception("scope=" + scope + " not support!");
			}
			result.data = data;
			result.resultcode = 200;
		} catch (Exception e) {
			result.resultcode = CommonTags.ErrorCode.ERROR_FAILED;
			logger.error("failed in getFlwcInfo", e);
		}

		logger.info(result.data.toString(1));
		return result;
	}

	private void checkZkClusterState(ZkCluster cluster) {
		if (cluster != null && cluster.getConnection().getState() != ZooKeeper.States.CONNECTED) {
			cluster.triggerReconnect();
		}
	}

	private ZkServer queryZkServer(String ipPortString) {
		if (StringUtil.isEmpty(ipPortString)) {
			return null;
		}
		ZkServer zkServer = null;
		for (ZkCluster cluster : CommonTags.Configs.zkClusters) {
			ZkServer zkServerTmp = cluster.getZkServer(ipPortString);
			if (zkServerTmp != null) {
				zkServer = zkServerTmp;
				break;
			}
		}

		return zkServer;
	}
}
