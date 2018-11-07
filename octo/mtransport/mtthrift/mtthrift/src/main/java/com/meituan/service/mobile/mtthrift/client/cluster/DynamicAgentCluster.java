package com.meituan.service.mobile.mtthrift.client.cluster;

import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.client.cell.DefaultCellPolicy;
import com.meituan.service.mobile.mtthrift.client.cell.ICellPolicy;
import com.meituan.service.mobile.mtthrift.client.cell.RouterMetaData;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.model.ServerConn;
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.netty.channel.NettyChannelPool;
import com.meituan.service.mobile.mtthrift.server.MTDefaultThreadFactory;
import com.sankuai.inf.octo.mns.MnsInvoker;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by jiguang on 14-9-22.
 */
public class DynamicAgentCluster extends BaseCluster {
    private static final Logger LOG = LoggerFactory.getLogger(DynamicAgentCluster.class);
    private static final int ALIVE = 2;
    private static final String EMPTY = "";
    private static final String DEFAULT_CELL = "default_cell";
    private static final List<ServerConn> EMPTY_LIST = Collections.emptyList();

    private volatile ConcurrentMap<Server, ServerConn> serversMap = new ConcurrentHashMap<Server, ServerConn>();
    private int timeOut;
    private MTThriftPoolConfig poolConfig;
    private int initMinIdle;
    private volatile List<ServerConn> serverConns = new ArrayList<ServerConn>();
    private volatile List<ServerConn> serverConnsSameIDC = new ArrayList<ServerConn>();
    private volatile List<ServerConn> serverConnsSameRegion = new ArrayList<ServerConn>();
    private volatile List<ServerConn> serverConnsDiffRegion = new ArrayList<ServerConn>();

    private volatile Map<String, List<ServerConn>> serverConnsInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
    private volatile Map<String, List<ServerConn>> serverConnsSameIDCInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
    private volatile Map<String, List<ServerConn>> serverConnsSameRegionInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
    private volatile Map<String, List<ServerConn>> serverConnsDiffRegionInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();

    private volatile Map<String, Set<String>> deadServerMapInSwimlane = new ConcurrentHashMap<String, Set<String>>();

    private volatile Map<String, List<ServerConn>> serverConnsInCell = new ConcurrentHashMap<String, List<ServerConn>>();
    private volatile Map<String, List<ServerConn>> serverConnsSameIDCInCell = new ConcurrentHashMap<String, List<ServerConn>>();
    private volatile Map<String, List<ServerConn>> serverConnsSameRegionInCell = new ConcurrentHashMap<String, List<ServerConn>>();
    private volatile Map<String, List<ServerConn>> serverConnsDiffRegionInCell = new ConcurrentHashMap<String, List<ServerConn>>();

    private volatile List<SGService> sgServiceList;
    private volatile List<DegradeAction> degradeActions = new ArrayList<DegradeAction>();
    private volatile Map<String, SGService> ipHostSGServiceMap = new HashMap<String, SGService>();
    private String appKey;
    private String remoteAppkey;
    private int remoteServerPort;
    private boolean filterByServiceName = false;
    private boolean isNettyIO;
    private boolean getServersWithoutRegion = false;
    private ScheduledFuture<?> serverListPollingTask;
    private ProtocolRequest protocolRequest;
    private IServiceListChangeListener serviceListChangeListener;
    private ICellPolicy userDefinedCellPolicy = null;
    private volatile boolean remoteAppIsCell = false;
    private static String localCell = (ProcessInfoUtil.getCell() != null &&
            !EMPTY.equals(ProcessInfoUtil.getCell())) ? ProcessInfoUtil.getCell() : DEFAULT_CELL;

    private static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool
            (3, new MTDefaultThreadFactory("DynamicAgentCluster"));

    public DynamicAgentCluster(MTThriftPoolConfig poolConfig, int timeOut, boolean isImplFacebookService
            , boolean async, boolean serverDynamicWeight, String strAgentUrl, String appKey, String remoteAppkey
            , int remoteServerPort, boolean bUpdateLocalConfig, String serviceName, int connTimeout,
                               boolean filterByServiceName, boolean isNettyIO, boolean getServersWithoutRegion, ICellPolicy userDefinedCellPolicy) throws Exception {
        super(isImplFacebookService, async);
        this.poolConfig = poolConfig;
        this.initMinIdle = poolConfig.getMinIdle();
        this.timeOut = timeOut;
        this.appKey = appKey;
        this.remoteAppkey = remoteAppkey;
        this.remoteServerPort = remoteServerPort;
        this.serviceName = serviceName;
        this.connTimeout = connTimeout;
        this.filterByServiceName = filterByServiceName;
        this.isNettyIO = isNettyIO;
        this.getServersWithoutRegion = getServersWithoutRegion;
        this.userDefinedCellPolicy = (userDefinedCellPolicy != null) ? userDefinedCellPolicy : new DefaultCellPolicy();

        protocolRequest = new ProtocolRequest(appKey, remoteAppkey, "thrift", serviceName);
        protocolRequest.setEnableSwimlane2(true);
        serviceListChangeListener = new ServerListChangeListener(protocolRequest);
        MnsInvoker.addServiceListener(protocolRequest, serviceListChangeListener);
        getServerListByAgent(protocolRequest);

        serverListPollingTask = scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getServerListByAgent(protocolRequest);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }, 1, 3, TimeUnit.SECONDS);
    }

    public DynamicAgentCluster(boolean isImplFacebookService, boolean async) {
        super(isImplFacebookService, async);
    }

    public static Server SGService2Server(SGService sgService, String serviceName) {
        Server server = new Server(sgService.getIp(), sgService.getPort());
        server.setWeight(sgService.getWeight());
        // 若 fweight 不为 0 , 则已 fWeight 代替 weight
        if (Double.compare(sgService.getFweight(), 0.d) > 0.d) {
            server.setWeight(sgService.getFweight());
        }
        server.setStatus(sgService.getStatus());
        server.setServerAppKey(sgService.getAppkey());
        server.setVersion(sgService.getVersion());
        server.setHeartbeatSupport(sgService.getHeartbeatSupport());
        server.setSwimlane(sgService.swimlane);
        server.setCell(sgService.getCell());

        Map<String, ServiceDetail> serviceDetailMap = sgService.getServiceInfo();
        if (serviceDetailMap != null) {
            for (String serviceName_ : serviceDetailMap.keySet()) {
                if (serviceName_.equals(serviceName)) {
                    server.setUnifiedProto(serviceDetailMap.get(serviceName_).isUnifiedProto());
                }
            }
        }
        return server;
    }

    public List<ServerConn> getServerConnListBySwimlaneRoute() {

        String swimlane = Tracer.getSwimlane();

        if (swimlane != null && !EMPTY.equals(swimlane)) {
            if (serverConnsSameIDCInSwimlane.get(swimlane) != null && !serverConnsSameIDCInSwimlane.get(swimlane).isEmpty()) {
                return serverConnsSameIDCInSwimlane.get(swimlane);
            } else if (serverConnsSameRegionInSwimlane.get(swimlane) != null && !serverConnsSameRegionInSwimlane.get(swimlane).isEmpty()) {
                return serverConnsSameRegionInSwimlane.get(swimlane);
            } else if (serverConnsDiffRegionInSwimlane.get(swimlane) != null && !serverConnsDiffRegionInSwimlane.get(swimlane).isEmpty()) {
                return serverConnsDiffRegionInSwimlane.get(swimlane);
            }

            Set<String> deadServerSet = deadServerMapInSwimlane.get(swimlane);
            if (deadServerSet != null && !deadServerSet.isEmpty()) {
                LOG.error("getServerConnListBySwimlaneRoute: No ServerConnList for remoteAppkey({}), swimlane({}), deadServerSet({})",
                        remoteAppkey, swimlane, deadServerSet.toString());
                return EMPTY_LIST;
            }
        }

        if (serverConnsSameIDC.size() > 0) {
            return serverConnsSameIDC;
        } else if (serverConnsSameRegion.size() > 0) {
            return serverConnsSameRegion;
        } else {
            return serverConnsDiffRegion;
        }
    }

    public List<ServerConn> getServerConnListByCellRoute(RouterMetaData routerMetaData) {

        String cell = userDefinedCellPolicy.getCell(routerMetaData);

        if (cell == null || EMPTY.equals(cell.trim())) {
            cell = localCell;
        }

        String swimlane = Tracer.getSwimlane();

        if (swimlane != null && !EMPTY.equals(swimlane)) {

            if (serverConnsInCell.get(cell) != null && !serverConnsInCell.get(cell).isEmpty()) {

                List<ServerConn> serverConnsWithSameCell = serverConnsInCell.get(cell);
                List<ServerConn> serverConnsWithSameSwimlane = new ArrayList<ServerConn>();
                List<ServerConn> serverConnsWithOutSwimlane = new ArrayList<ServerConn>();

                Set<String> deadServerSet = deadServerMapInSwimlane.get(swimlane);
                Set<String> realDeadServerSet = new HashSet<String>();
                boolean deadServerInSwimlane = false;

                for (ServerConn serverConn : serverConnsWithSameCell) {
                    if (swimlane.equals(serverConn.getSwimlane())) {
                        serverConnsWithSameSwimlane.add(serverConn);
                    }

                    if (serverConn.getSwimlane() == null && EMPTY.equals(serverConn.getSwimlane().trim())) {
                        serverConnsWithOutSwimlane.add(serverConn);
                    }

                    String ipHost = serverConn.getServer().getIp() + ":" + serverConn.getServer().getPort();
                    if (deadServerSet != null && deadServerSet.contains(ipHost)) {
                        deadServerInSwimlane = true;
                        realDeadServerSet.add(ipHost);
                    }
                }

                if (serverConnsWithSameSwimlane.size() > 0) {
                    return getServerConnListByRegionRoute(serverConnsWithSameSwimlane);
                } else if (serverConnsWithSameSwimlane.size() == 0 && deadServerInSwimlane) {
                    LOG.error("getServerConnListByCellRoute: No ServerConnList for remoteAppkey({}), userDefinedCell({}), localCell({}), swimlane({}), deadServerSet({})",
                            remoteAppkey, cell, localCell, swimlane, realDeadServerSet.toString());
                    return EMPTY_LIST;
                } else {
                    return getServerConnListByRegionRoute(serverConnsWithOutSwimlane);
                }

            } else {
                LOG.error("getServerConnListByCellRoute: No ServerConnList for remoteAppkey({}), userDefinedCell({}), localCell({}), swimlane({})",
                        remoteAppkey, cell, localCell, swimlane);
                return EMPTY_LIST;
            }

        } else {
            if (serverConnsSameIDCInCell.get(cell) != null && !serverConnsSameIDCInCell.get(cell).isEmpty()) {
                return serverConnsSameIDCInCell.get(cell);
            } else if (serverConnsSameRegionInCell.get(cell) != null && !serverConnsSameRegionInCell.get(cell).isEmpty()) {
                return serverConnsSameRegionInCell.get(cell);
            } else if (serverConnsDiffRegionInCell.get(cell) != null && !serverConnsDiffRegionInCell.get(cell).isEmpty()) {
                return serverConnsDiffRegionInCell.get(cell);
            } else {
                LOG.error("getServerConnListByCellRoute: No ServerConnList for remoteAppkey({}), userDefinedCell({}), localCell({})",
                        remoteAppkey, cell, localCell);
                return EMPTY_LIST;
            }
        }
    }

    @Override
    public List<ServerConn> getServerConnList() {
        if (getServersWithoutRegion) {
            List<ServerConn> serversWithoutRegion = new ArrayList<ServerConn>();
            serversWithoutRegion.addAll(serverConns);
            return serversWithoutRegion;
        }

        if (remoteAppIsCell) {
            return getServerConnListByCellRoute(new RouterMetaData());
        } else {
            return getServerConnListBySwimlaneRoute();
        }
    }

    @Override
    public List<ServerConn> getServerConnList(RouterMetaData routerMetaData) {
        if (getServersWithoutRegion) {
            List<ServerConn> serversWithoutRegion = new ArrayList<ServerConn>();
            serversWithoutRegion.addAll(serverConns);
            return serversWithoutRegion;
        }

        if (remoteAppIsCell) {
            return getServerConnListByCellRoute(routerMetaData);
        } else {
            return getServerConnListBySwimlaneRoute();
        }
    }

    public List<ServerConn> getServerConnListByRegionRoute(List<ServerConn> serverConns) {

        List<ServerConn> serverConnsSameIDC = new ArrayList<ServerConn>();
        List<ServerConn> serverConnsSameRegion = new ArrayList<ServerConn>();
        List<ServerConn> serverConnsDiffRegion = new ArrayList<ServerConn>();

        for (ServerConn conn : serverConns) {
            Double weight = conn.getServer().getWeight();

            if (Double.compare(weight, 1.0) >= 0 && Double.compare(weight, 100.0) <= 0) {
                //同IDC
                serverConnsSameIDC.add(conn);
            } else if (Double.compare(weight, 0.001) >= 0 && Double.compare(weight, 0.1) <= 0) {
                //同Region不同IDC
                serverConnsSameRegion.add(conn);
            } else if (Double.compare(weight, 0.000001) >= 0 && Double.compare(weight, 0.0001) <= 0) {
                //不同Region
                serverConnsDiffRegion.add(conn);
            }
        }

        if (serverConnsSameIDC.size() > 0) {
            return serverConnsSameIDC;
        } else if (serverConnsSameRegion.size() > 0) {
            return serverConnsSameRegion;
        } else {
            return serverConnsDiffRegion;
        }

    }

    @Override
    public void destroy() {
        MnsInvoker.removeServiceListener(protocolRequest, serviceListChangeListener);

        if (serverListPollingTask != null) {
            try {
                if (serverListPollingTask.cancel(true)) {
                    LOG.warn("cancelled serverListPollingTask, appkey:{}, remoteAppkey:{}, serviceName:{}",
                            appKey, remoteAppkey, serviceName);
                } else {
                    LOG.warn("fail to cancel serverListPollingTask, serverListPollingTask.cancel(true) return false"
                            + "appkey:{}, remoteAppkey:{}, serviceName:{}", appKey, remoteAppkey, serviceName);
                }
            } catch (Exception e) {
                LOG.warn("error during cancelling serverListPollingTask", e);
            }
        }

        for (ServerConn serverConn : serversMap.values()) {
            destroyPool(serverConn);
        }
        serversMap.clear();
        serverConns.clear();
    }

    public void getServerListByAgent(ProtocolRequest protocolRequest) {
        boolean valid;
        try {
            sgServiceList = MnsInvoker.getServiceList(protocolRequest);
            valid = validateServiceList(sgServiceList);
        } catch (Exception ex) {
            LOG.warn("getServerList by Agent Exception", ex);
            valid = false;
        }

        if (valid) {
            updateSeverList();
        }

    }

    private void updateSeverList() {
        ipHostSGServiceMap = new HashMap<String, SGService>();
        for (SGService sgService : sgServiceList) {
            // 根据远程 ServerPort 筛选
            if (remoteServerPort > 0 && sgService.getPort() != remoteServerPort) {
                continue;
            }

            String ipHost = sgService.getIp() + ":" + sgService.getPort();
            // 异常状态节点, 忽略
            if (2 != sgService.getStatus()) {
                String swimlane = sgService.getSwimlane();
                if (swimlane != null && !EMPTY.equals(swimlane)) {
                    Set<String> deadServersInSwimlane = deadServerMapInSwimlane.get(swimlane);
                    if (deadServersInSwimlane == null) {
                        deadServersInSwimlane = new HashSet<String>();
                        deadServerMapInSwimlane.put(swimlane, deadServersInSwimlane);
                    }
                    deadServersInSwimlane.add(ipHost);
                }
                continue;
            } else {
                for (Set<String> deadServerSet : deadServerMapInSwimlane.values()) {
                    deadServerSet.remove(ipHost);
                }
            }

            ipHostSGServiceMap.put(ipHost, sgService);
        }

        boolean changed = false;// 真实变更标记
        // 下线的节点:
        Set<String> ipHostSet = ipHostSGServiceMap.keySet();

        for (Iterator<Server> it = serversMap.keySet().iterator(); it.hasNext(); ) {// 删除已下线server
            Server server = it.next();
            if (!ipHostSet.contains(server.getIp() + ":" + server.getPort())) {
                ServerConn serverConn = serversMap.get(server);
                it.remove();
                changed = true;
                if (serverConn != null) {
                    LOG.info("removeServer:" + server);
                    destroyPool(serverConn);
                    serverConn.setObjectPool(null);
                }
            }
        }

        // 新出现的节点:
        for (String ipHost : ipHostSGServiceMap.keySet()) {

            SGService sgService = ipHostSGServiceMap.get(ipHost);
            Map<String, ServiceDetail> serviceDetailMap = sgService.getServiceInfo();

            Server server = SGService2Server(sgService, serviceName);
            if (!serversMap.containsKey(server) && 2 == server.getStatus()) {
                if (filterByServiceName && serviceDetailMap != null && serviceDetailMap.containsKey(serviceName)) {
                    if (addServer(server)) {
                        changed = true;
                    }
                } else if (!filterByServiceName) {
                    if (addServer(server)) {
                        changed = true;
                    }
                }
            }
        }

        // weight 变化 和 uniProto 字段的变化
        for (Iterator<Server> it = serversMap.keySet().iterator(); it.hasNext(); ) {
            Server server = it.next();
            String ipHostServerKey = server.getIp() + ":" + server.getPort();
            SGService sgService = ipHostSGServiceMap.get(ipHostServerKey);
            int weight = sgService.getWeight();
            double fWeight = sgService.getFweight();

            if (Double.compare(fWeight, 0.d) > 0.d) {
                if (Double.compare(Math.abs(server.getDoubleServerWeight() - fWeight) / fWeight, 0.001d) > 0.d) {
                    server.setWeight(fWeight);
                    changed = true;
                    LOG.debug("update server fWeight:{}", server);
                }
            } else if (server.getServerWeight() != weight) {
                server.setWeight(weight);
                changed = true;
                LOG.debug("update server weight:{}", server);
            }

            Map<String, ServiceDetail> serviceDetailMap = sgService.getServiceInfo();
            boolean isUnifiedProto = false;
            if (serviceDetailMap != null && serviceDetailMap.containsKey(serviceName)) {
                ServiceDetail serviceDetailInMap = serviceDetailMap.get(serviceName);
                if (serviceDetailInMap != null) {
                    isUnifiedProto = serviceDetailInMap.isUnifiedProto();
                }
            }
            if (server.isUnifiedProto() != isUnifiedProto) {
                changed = true;
                server.setUnifiedProto(isUnifiedProto);
            }
        }

        if (changed) {
            updateVaildConns();
            filterServerConns();
        }

    }

    public void indexServerConnsByRegion() {
        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        List<ServerConn> _serverConnsSameIDC = new ArrayList<ServerConn>();
        List<ServerConn> _serverConnsSameRegion = new ArrayList<ServerConn>();
        List<ServerConn> _serverConnsDiffRegion = new ArrayList<ServerConn>();

        _serverConns.addAll(serverConns);

        for (int i = 0; i < _serverConns.size(); i++) {
            ServerConn conn = _serverConns.get(i);
            Double weight = conn.getServer().getWeight();
            if (Double.compare(weight, 1.0) >= 0 && Double.compare(weight, 100.0) <= 0) {
                //同IDC
                _serverConnsSameIDC.add(conn);
            } else if (Double.compare(weight, 0.001) >= 0 && Double.compare(weight, 0.1) <= 0) {
                //同Region不同IDC
                _serverConnsSameRegion.add(conn);
            } else if (Double.compare(weight, 0.000001) >= 0 && Double.compare(weight, 0.0001) <= 0) {
                //不同Region
                _serverConnsDiffRegion.add(conn);
            }
        }

        serverConnsSameIDC = _serverConnsSameIDC;
        serverConnsSameRegion = _serverConnsSameRegion;
        serverConnsDiffRegion = _serverConnsDiffRegion;
    }


    public void indexServerConnsBySwimlane() {
        Map<String, List<ServerConn>> _serverConnsInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsSameIDCInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsSameRegionInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsDiffRegionInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();

        _serverConnsInSwimlane.putAll(serverConnsInSwimlane);

        for (Map.Entry<String, List<ServerConn>> entry : _serverConnsInSwimlane.entrySet()) {
            String swimlane = entry.getKey();
            List<ServerConn> swimlaneServerConns = entry.getValue();
            for (int i = 0; i < swimlaneServerConns.size(); i++) {
                ServerConn conn = swimlaneServerConns.get(i);
                Double weight = conn.getServer().getWeight();
                if (Double.compare(weight, 1.0) >= 0 && Double.compare(weight, 100.0) <= 0) {
                    //同IDC
                    List<ServerConn> _swimlaneServerConnsSameIDC = _serverConnsSameIDCInSwimlane.get(swimlane);

                    if (_swimlaneServerConnsSameIDC == null) {
                        _swimlaneServerConnsSameIDC = new ArrayList<ServerConn>();
                        _serverConnsSameIDCInSwimlane.put(swimlane, _swimlaneServerConnsSameIDC);
                    }

                    _swimlaneServerConnsSameIDC.add(conn);
                } else if (Double.compare(weight, 0.001) >= 0 && Double.compare(weight, 0.1) <= 0) {
                    //同Region不同IDC
                    List<ServerConn> _swimlaneServerConnsSameRegion = _serverConnsSameRegionInSwimlane.get(swimlane);

                    if (_swimlaneServerConnsSameRegion == null) {
                        _swimlaneServerConnsSameRegion = new ArrayList<ServerConn>();
                        _serverConnsSameRegionInSwimlane.put(swimlane, _swimlaneServerConnsSameRegion);
                    }

                    _swimlaneServerConnsSameRegion.add(conn);
                } else if (Double.compare(weight, 0.000001) >= 0 && Double.compare(weight, 0.0001) <= 0) {
                    //不同Region
                    List<ServerConn> _swimlaneServerConnsDiffRegion = _serverConnsDiffRegionInSwimlane.get(swimlane);

                    if (_swimlaneServerConnsDiffRegion == null) {
                        _swimlaneServerConnsDiffRegion = new ArrayList<ServerConn>();
                        _serverConnsDiffRegionInSwimlane.put(swimlane, _swimlaneServerConnsDiffRegion);
                    }

                    _swimlaneServerConnsDiffRegion.add(conn);
                }
            }
        }

        serverConnsSameIDCInSwimlane = _serverConnsSameIDCInSwimlane;
        serverConnsSameRegionInSwimlane = _serverConnsSameRegionInSwimlane;
        serverConnsDiffRegionInSwimlane = _serverConnsDiffRegionInSwimlane;
    }

    public void indexServerConnsByCell() {
        Map<String, List<ServerConn>> _serverConnsInCell = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsSameIDCInCell = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsSameRegionInCell = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsDiffRegionInCell = new ConcurrentHashMap<String, List<ServerConn>>();

        _serverConnsInCell.putAll(serverConnsInCell);

        for (Map.Entry<String, List<ServerConn>> entry : _serverConnsInCell.entrySet()) {
            String cell = entry.getKey();
            List<ServerConn> cellServerConns = entry.getValue();
            for (int i = 0; i < cellServerConns.size(); i++) {
                ServerConn conn = cellServerConns.get(i);
                Double weight = conn.getServer().getWeight();
                if (Double.compare(weight, 1.0) >= 0 && Double.compare(weight, 100.0) <= 0) {
                    //同IDC
                    List<ServerConn> _cellServerConnsSameIDC = _serverConnsSameIDCInCell.get(cell);

                    if (_cellServerConnsSameIDC == null) {
                        _cellServerConnsSameIDC = new ArrayList<ServerConn>();
                        _serverConnsSameIDCInCell.put(cell, _cellServerConnsSameIDC);
                    }

                    _cellServerConnsSameIDC.add(conn);
                } else if (Double.compare(weight, 0.001) >= 0 && Double.compare(weight, 0.1) <= 0) {
                    //同Region不同IDC
                    List<ServerConn> _cellServerConnsSameRegion = _serverConnsSameRegionInCell.get(cell);

                    if (_cellServerConnsSameRegion == null) {
                        _cellServerConnsSameRegion = new ArrayList<ServerConn>();
                        _serverConnsSameRegionInCell.put(cell, _cellServerConnsSameRegion);
                    }

                    _cellServerConnsSameRegion.add(conn);
                } else if (Double.compare(weight, 0.000001) >= 0 && Double.compare(weight, 0.0001) <= 0) {
                    //不同Region
                    List<ServerConn> _cellServerConnsDiffRegion = _serverConnsDiffRegionInCell.get(cell);

                    if (_cellServerConnsDiffRegion == null) {
                        _cellServerConnsDiffRegion = new ArrayList<ServerConn>();
                        _serverConnsDiffRegionInCell.put(cell, _cellServerConnsDiffRegion);
                    }

                    _cellServerConnsDiffRegion.add(conn);
                }
            }
        }

        serverConnsSameIDCInCell = _serverConnsSameIDCInCell;
        serverConnsSameRegionInCell = _serverConnsSameRegionInCell;
        serverConnsDiffRegionInCell = _serverConnsDiffRegionInCell;

    }

    public void filterServerConns() {
        indexServerConnsByRegion();
        indexServerConnsBySwimlane();
        indexServerConnsByCell();
    }

    /**
     * 更新可用server列表（比如新增server，或是server的Weight由0变为非0）
     */
    private void updateVaildConns() {

        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        Map<String, List<ServerConn>> _serverConnsInSwimlane = new ConcurrentHashMap<String, List<ServerConn>>();
        Map<String, List<ServerConn>> _serverConnsInCell = new ConcurrentHashMap<String, List<ServerConn>>();
        boolean _remoteAppIsCell = false;

        for (ServerConn conn : serversMap.values()) {
            if (ALIVE == conn.getServer().getStatus()) {

                //处理泳道
                String swimlane = conn.getSwimlane();
                if (swimlane == null || EMPTY.equals(swimlane)) {
                    _serverConns.add(conn);
                } else {
                    List<ServerConn> serverConnList = _serverConnsInSwimlane.get(swimlane);

                    if (serverConnList == null) {
                        serverConnList = new ArrayList<ServerConn>();
                        _serverConnsInSwimlane.put(swimlane, serverConnList);
                    }

                    serverConnList.add(conn);
                }

                //处理CELL
                String cell = conn.getCell();
                if (cell == null || EMPTY.equals(cell.trim())) {
                    cell = DEFAULT_CELL;
                }
                List<ServerConn> serverConnList = _serverConnsInCell.get(cell);
                if (serverConnList == null) {
                    serverConnList = new ArrayList<ServerConn>();
                    _serverConnsInCell.put(cell, serverConnList);
                }
                serverConnList.add(conn);

                if (conn.getCell() != null && !EMPTY.equals(conn.getCell().trim())) {
                    _remoteAppIsCell = true;
                }

            }
        }

        remoteAppIsCell = _remoteAppIsCell;
        serverConns = _serverConns;
        serverConnsInSwimlane = _serverConnsInSwimlane;
        serverConnsInCell = _serverConnsInCell;

        LOG.debug("update mtthrift serverList:{}", serverConns);
    }

    private boolean addServer(Server server) {
        if (serversMap.containsKey(server)) {
            return false;
        }

        LOG.info("addServer:" + server);
        ServerConn serverConn = new ServerConn();
        serverConn.setServer(server);
        serverConn.setSwimlane(server.getSwimlane());
        serverConn.setCell(server.getCell());
        // 若权重为0 , 则连接池 min idle 置为 0
        if (server.getWeight() < 1) {
            poolConfig.setMinIdle(0);
        }

        serverConn.setConnPoolConf(poolConfig);

        if (isNettyIO && !server.isNettyIOSupported()) {
            LOG.warn("server({}:{}) does not support nettyIO, fall back to thriftIO", server.getIp(), server.getPort());
        }

        if (isNettyIO && server.isNettyIOSupported()) {
            serverConn.setChannelPool(new NettyChannelPool(poolConfig, this, serverConn, connTimeout));
        } else {
            serverConn.setObjectPool(createPool(server.getIp(), server.getPort(), timeOut, poolConfig, connTimeout));
        }
        ServerConn oldServerConn = serversMap.put(server, serverConn);
        if (oldServerConn != null) {
            destroyPool(oldServerConn);
        }
        // 恢复初始设置
        poolConfig.setMinIdle(this.initMinIdle);
        return true;
    }

    public String getRemoteAppkey() {
        return remoteAppkey;
    }

    public void setRemoteAppkey(String remoteAppkey) {
        this.remoteAppkey = remoteAppkey;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    @Override
    public void updateServerConn(ServerConn serverConn) {
        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        for (ServerConn conn : serversMap.values()) {
            if (conn.getServer().getIp().equalsIgnoreCase(serverConn.getServer().getIp()) &&
                    conn.getServer().getPort() == serverConn.getServer().getPort()) {
                _serverConns.add(serverConn);
            } else {
                _serverConns.add(conn);
            }
        }
        serverConns = _serverConns;
        LOG.info("update mtthrift serverList:{}", serverConns);
        filterServerConns();
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    public List<DegradeAction> getDegradeActions() {
        return degradeActions;
    }

    public List<ServerConn> getServerConnsSameIDC() {
        return serverConnsSameIDC;
    }

    public List<ServerConn> getServerConnsSameRegion() {
        return serverConnsSameRegion;
    }

    public List<ServerConn> getServerConnsDiffRegion() {
        return serverConnsDiffRegion;
    }

    public void setServerList(List<ServerConn> serverList) {
        serverConns = serverList;
    }

    public class ServerListChangeListener implements IServiceListChangeListener {
        private ProtocolRequest protocolRequest;

        public ServerListChangeListener(ProtocolRequest protocolRequest) {
            this.protocolRequest = protocolRequest;
        }

        @Override
        public void changed(ProtocolRequest req,
                            List<SGService> oldList,
                            List<SGService> newList,
                            List<SGService> addList,
                            List<SGService> deletedList,
                            List<SGService> modifiedList) {


            LOG.debug("get serverListChangeListener event");

            if (this.protocolRequest.equals(req) && validateServiceList(newList)) {
                sgServiceList = newList;
                updateSeverList();
            }

        }
    }

    private boolean validateServiceList(List<SGService> sgServices) {
        boolean valid = false;

        if (null == sgServices) {
            valid = false;
        } else {
            valid = true;
        }

        return valid;
    }

}
