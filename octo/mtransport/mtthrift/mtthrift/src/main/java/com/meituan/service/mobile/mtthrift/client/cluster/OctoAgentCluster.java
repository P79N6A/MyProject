package com.meituan.service.mobile.mtthrift.client.cluster;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
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
import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGService;
import com.sankuai.sgagent.thrift.model.ServiceDetail;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;


public class OctoAgentCluster extends BaseCluster {

    private static final Logger LOG = LoggerFactory.getLogger(OctoAgentCluster.class);
    private static final int ALIVE = 2;
    private static final String EMPTY = "";
    private static final String DEFAULT_CELL = "default_cell";
    private static final String DEFAULT_SWIMLANE = "default_swimlane";
    private static final String DEFAULT_SAME_IDC = "default_same_idc";
    private static final String DEFAULT_SAME_REGION = "default_same_region";
    private static final String DEFAULT_DIFF_REGION = "default_diff_region";
    private static final String GRAY_RELEASE_PREFIX = "gray-release-";
    private static final List<ServerConn> EMPTY_LIST = Collections.emptyList();
    private static String localCell = (StringUtils.isBlank(ProcessInfoUtil.getCell())) ?
            DEFAULT_CELL : ProcessInfoUtil.getCell();

    private static String localSwimlane = (StringUtils.isBlank(ProcessInfoUtil.getSwimlane())) ?
            DEFAULT_SWIMLANE : ProcessInfoUtil.getSwimlane();

    private static ScheduledExecutorService scheduExec = Executors.newScheduledThreadPool(3, new MTDefaultThreadFactory("OctoAgentCluster"));

    private volatile Map<Server, ServerConn> serversMap = new ConcurrentHashMap<Server, ServerConn>();
    private volatile List<ServerConn> serverConns = new ArrayList<ServerConn>();
    //第一个String标识cell,第二个String标识swimlane
    private volatile Table<String, String, Set<String>> deadServerSetIndexTable = HashBasedTable.create();
    //第一个String标识cell,第二个String标识swimlane,Map里面的String标识region
    private volatile Table<String, String, Map<String, List<ServerConn>>> serverConnListIndexTable = HashBasedTable.create();

    //用于标识远程服务是否是cell服务
    private volatile boolean remoteAppIsCell = false;
    //用于标识远程服务是否包含cell服务节点
    private volatile boolean isRemoteAppContainsCellProvider = false;


    private int timeOut;
    private int initMinIdle;
    private String appKey;
    private String remoteAppkey;
    private int remoteServerPort;
    private boolean filterByServiceName = false;
    private boolean isNettyIO;
    private boolean getServersWithoutRegion = false;
    private MTThriftPoolConfig poolConfig;
    private ScheduledFuture<?> serverListPollingTask;
    private ProtocolRequest protocolRequest;
    private IServiceListChangeListener serviceListChangeListener;
    private ICellPolicy userDefinedCellPolicy = null;


    public OctoAgentCluster(boolean isImplFacebookService, boolean async) {
        super(isImplFacebookService, async);
    }

    public OctoAgentCluster(MTThriftPoolConfig poolConfig, int timeOut, boolean isImplFacebookService, boolean async,
                            String appKey, final String remoteAppkey, int remoteServerPort, String serviceName, int connTimeout,
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
        this.userDefinedCellPolicy = userDefinedCellPolicy;

        protocolRequest = new ProtocolRequest(appKey, remoteAppkey, "thrift", serviceName);
        protocolRequest.setEnableSwimlane2(true);
        protocolRequest.setEnableCell(true); //sg_agent返回全量服务列表,默认false,即只返回非cell节点
        serviceListChangeListener = new ServerListChangeListener(protocolRequest);
        MnsInvoker.addServiceListener(protocolRequest, serviceListChangeListener);
        getServerListByAgent(protocolRequest);
        getRemoteAppkeyDescByAgent(remoteAppkey);

        serverListPollingTask = scheduExec.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    getServerListByAgent(protocolRequest);
                    getRemoteAppkeyDescByAgent(remoteAppkey);
                } catch (Throwable e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }, 1, 3, TimeUnit.SECONDS);
    }


    public boolean isRemoteAppIsCell() {
        return remoteAppIsCell;
    }

    public void setRemoteAppIsCell(boolean remoteAppIsCell) {
        this.remoteAppIsCell = remoteAppIsCell;
    }

    private List<ServerConn> getServerConnListByCellRoute(RouterMetaData routerMetaData) {

        List<ServerConn> result;
        String cell = "";

        if (!remoteAppIsCell) {
            //远端服务不是cell服务,则只访问default_cell节点(中心节点)
            cell = DEFAULT_CELL;
            result = getServerConnListBySwimlaneRoute(cell);
        } else {
            //取流量里面的cell(取不到则默认取本机cell);主要是为了实现类似泳道的特性
            cell = Tracer.getClientTracer().getContext(Tracer.CELL);
            if (cell != null && cell.startsWith(GRAY_RELEASE_PREFIX)) {
                result = getServerConnListBySwimlaneRoute(cell);
                if (result == null || result.size() <= 0) {
                    //灰度链路支持流量回调到中心节点
                    cell = DEFAULT_CELL;
                    result = getServerConnListBySwimlaneRoute(cell);
                }
            } else {
                cell = ""; //置空cell,否则可能会依赖于上面Tracer.getClientTracer().getContext(Tracer.CELL)的结果
                if (routerMetaData != null && userDefinedCellPolicy != null) {
                    cell = userDefinedCellPolicy.getCell(routerMetaData);
                }

                if (StringUtils.isBlank(cell)) {
                    cell = localCell;
                }
                result = getServerConnListBySwimlaneRoute(cell);
            }
        }

        return result;
    }

    private List<ServerConn> getServerConnListBySwimlaneRoute(String cell) {

        List<ServerConn> result;
        Set<String> deadServerSet;
        Map<String, List<ServerConn>> regionServerConnsMap;

        String swimlane = Tracer.getClientTracer().getContext(Tracer.SWIMLANE);
        if (StringUtils.isBlank(swimlane)) {
            swimlane = localSwimlane;
        }

        regionServerConnsMap = serverConnListIndexTable.get(cell, swimlane);
        result = getServerConnListByRegionRoute(regionServerConnsMap);

        if (result == null || result.size() <= 0) {
            //判断泳道里面是否有未启动或者禁用节点
            deadServerSet = deadServerSetIndexTable.get(cell, swimlane);
            if (deadServerSet != null && deadServerSet.size() > 0) {
                if (!cell.startsWith(GRAY_RELEASE_PREFIX)) {
                    LOG.error("getFromServerConnListIndexTable: No ServerConnList for remoteAppkey({}), cell({}), localCell({}), swimlane({}), deadServerSet({})",
                            remoteAppkey, cell, localCell, swimlane, deadServerSet.toString());
                }
                result = EMPTY_LIST;
            } else {
                //回调到主干
                regionServerConnsMap = serverConnListIndexTable.get(cell, DEFAULT_SWIMLANE);
                result = getServerConnListByRegionRoute(regionServerConnsMap);
                if (result == null || result.size() <= 0) {
                    if (!cell.startsWith(GRAY_RELEASE_PREFIX)) {
                        LOG.error("getFromServerConnListIndexTable: No ServerConnList for remoteAppkey({}), cell({}), localCell({}), swimlane({})",
                                remoteAppkey, cell, localCell, swimlane);
                    }
                    result = EMPTY_LIST;
                }
            }
        }

        return result;
    }

    private List<ServerConn> getServerConnListByRegionRoute(Map<String, List<ServerConn>> regionServerConnsMap) {
        List<ServerConn> result = null;
        if (regionServerConnsMap != null) {
            result = regionServerConnsMap.get(DEFAULT_SAME_IDC);
            if (result == null || result.size() <= 0) {
                result = regionServerConnsMap.get(DEFAULT_SAME_REGION);
                if (result == null || result.size() <= 0) {
                    result = regionServerConnsMap.get(DEFAULT_DIFF_REGION);
                }
            }
        }
        return result;
    }


    private void getServerListByAgent(ProtocolRequest protocolRequest) {
        boolean valid;
        List<SGService> sgServiceList = null;
        try {
            sgServiceList = MnsInvoker.getServiceList(protocolRequest);
            valid = validateServiceList(sgServiceList);
        } catch (Exception ex) {
            LOG.warn("getServerList by Agent Exception", ex);
            valid = false;
        }

        if (valid) {
            updateSeverList(sgServiceList);
        }

    }

    private void getRemoteAppkeyDescByAgent(String remoteAppkey) {
        AppkeyDescResponse response = MnsInvoker.getAppkeyDesc(remoteAppkey);
        if (response != null && response.getErrCode() == 0) {
            if (response.getDesc() != null) {
                String result = response.getDesc().getCell();
                if (result != null && "true".equals(result.trim().toLowerCase())) {
                    remoteAppIsCell = true;
                } else {
                    remoteAppIsCell = false;
                }
            }
        }
    }

    private boolean validateServiceList(List<SGService> sgServices) {
        boolean valid;
        if (null == sgServices) {
            valid = false;
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * 根据传入的sgServiceList更新deadServerSetIndexTable,目的是记录各泳道对应的未启动或者禁用节点
     *
     * @param sgServiceList
     */
    private void updateDeadServerSetIndexTable(List<SGService> sgServiceList) {

        Set<String> deadServerSet;
        Table<String, String, Set<String>> _deadServerSetIndexTable = HashBasedTable.create();

        for (SGService sgService : sgServiceList) {
            if (remoteServerPort > 0 && sgService.getPort() != remoteServerPort) {
                continue;
            }

            if (ALIVE != sgService.getStatus()) {
                String cell = sgService.getCell();
                if (cell == null || EMPTY.equals(cell)) {
                    cell = DEFAULT_CELL;
                }
                String swimlane = sgService.getSwimlane();
                if (swimlane != null && !EMPTY.equals(swimlane)) {
                    deadServerSet = _deadServerSetIndexTable.get(cell, swimlane);
                    if (deadServerSet == null) {
                        deadServerSet = new HashSet<String>();
                        _deadServerSetIndexTable.put(cell, swimlane, deadServerSet);
                    }

                    String ipHost = sgService.getIp() + ":" + sgService.getPort();
                    deadServerSet.add(ipHost);
                }
            }
        }

        deadServerSetIndexTable = _deadServerSetIndexTable;
    }

    private void updateSeverList(List<SGService> sgServiceList) {

        //更新各泳道对应的未启动或者禁用节点
        updateDeadServerSetIndexTable(sgServiceList);

        Map<String, SGService> ipHostSGServiceMap = new HashMap<String, SGService>();
        for (SGService sgService : sgServiceList) {
            if (remoteServerPort > 0 && sgService.getPort() != remoteServerPort) {
                continue;
            }

            String ipHost = sgService.getIp() + ":" + sgService.getPort();
            if (ALIVE == sgService.getStatus()) {
                ipHostSGServiceMap.put(ipHost, sgService);
            }
        }

        //真实变更标记
        boolean changed = false;

        //下线节点
        Set<String> ipHostSet = ipHostSGServiceMap.keySet();
        for (Iterator<Server> it = serversMap.keySet().iterator(); it.hasNext(); ) {
            Server server = it.next();
            if (!ipHostSet.contains(server.getIp() + ":" + server.getPort())) {
                ServerConn serverConn = serversMap.get(server);
                it.remove();
                changed = true;
                if (serverConn != null) {
                    LOG.info("removeServer:{}", server);
                    destroyPool(serverConn);
                    serverConn.setObjectPool(null);
                }
            }
        }

        //新增节点
        for (String ipHost : ipHostSGServiceMap.keySet()) {

            SGService sgService = ipHostSGServiceMap.get(ipHost);
            Map<String, ServiceDetail> serviceDetailMap = sgService.getServiceInfo();

            Server server = sgService2Server(sgService, serviceName);
            if (!serversMap.containsKey(server) && ALIVE == server.getStatus()) {
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

        //weight变化和uniProto变化
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
                }
            } else if (server.getServerWeight() != weight) {
                server.setWeight(weight);
                changed = true;
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
            updateValidConns();
            updateServerConnListIndexTable();
        }

    }

    /**
     * 更新可用server列表（比如新增server，或是server的Weight由0变为非0）
     */
    private void updateValidConns() {

        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        boolean _isRemoteAppContainsCellProvider = false;

        for (ServerConn conn : serversMap.values()) {
            if (ALIVE == conn.getServer().getStatus()) {
                _serverConns.add(conn);
                //判断远程服务是否是cell服务
                if (conn.getCell() != null && !EMPTY.equals(conn.getCell().trim())) {
                    _isRemoteAppContainsCellProvider = true;
                }
            }
        }

        isRemoteAppContainsCellProvider = _isRemoteAppContainsCellProvider;
        serverConns = _serverConns;

    }

    /**
     * 从serverConns建立索引到serverConnListIndexTable
     */
    private void updateServerConnListIndexTable() {

        Table<String, String, Map<String, List<ServerConn>>> _serverConnListIndexTable = HashBasedTable.create();
        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        _serverConns.addAll(serverConns);


        for (ServerConn conn : _serverConns) {

            //获取cell
            String cell = conn.getCell();
            if (cell == null || EMPTY.equals(cell.trim())) {
                cell = DEFAULT_CELL;
            }

            //获取swimlane
            String swimlane = conn.getSwimlane();
            if (swimlane == null || EMPTY.equals(swimlane)) {
                swimlane = DEFAULT_SWIMLANE;
            }

            //获取region
            String region = DEFAULT_SAME_IDC;
            Double weight = conn.getServer().getWeight();
            if (Double.compare(weight, 1.0) >= 0 && Double.compare(weight, 100.0) <= 0) {
                region = DEFAULT_SAME_IDC;
            } else if (Double.compare(weight, 0.001) >= 0 && Double.compare(weight, 0.1) <= 0) {
                region = DEFAULT_SAME_REGION;
            } else if (Double.compare(weight, 0.000001) >= 0 && Double.compare(weight, 0.0001) <= 0) {
                region = DEFAULT_DIFF_REGION;
            }

            Map<String, List<ServerConn>> regionServerConnsMap = _serverConnListIndexTable.get(cell, swimlane);
            if (regionServerConnsMap == null) {
                regionServerConnsMap = new ConcurrentHashMap<String, List<ServerConn>>();
                _serverConnListIndexTable.put(cell, swimlane, regionServerConnsMap);
            }
            List<ServerConn> serverConns = regionServerConnsMap.get(region);
            if (serverConns == null) {
                serverConns = new ArrayList<ServerConn>();
                regionServerConnsMap.put(region, serverConns);
            }
            serverConns.add(conn);

        }

        serverConnListIndexTable = _serverConnListIndexTable;
    }

    private boolean addServer(Server server) {
        if (serversMap.containsKey(server)) {
            return false;
        }

        LOG.info("addServer:{}", server);
        ServerConn serverConn = new ServerConn();
        serverConn.setServer(server);
        serverConn.setSwimlane(server.getSwimlane());
        serverConn.setCell(server.getCell());
        //若权重为0,则连接池minIdle置为0
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
        //恢复初始设置
        poolConfig.setMinIdle(this.initMinIdle);
        return true;
    }


    public static Server sgService2Server(SGService sgService, String serviceName) {
        Server server = new Server(sgService.getIp(), sgService.getPort());
        server.setWeight(sgService.getWeight());
        // 若fweight不为0,则已fWeight代替weight
        if (Double.compare(sgService.getFweight(), 0.d) > 0.d) {
            server.setWeight(sgService.getFweight());
        }
        server.setStatus(sgService.getStatus());
        server.setServerAppKey(sgService.getAppkey());
        server.setVersion(sgService.getVersion());
        server.setHeartbeatSupport(sgService.getHeartbeatSupport());
        server.setSwimlane(sgService.getSwimlane());
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

    @Override
    public List<ServerConn> getServerConnList() {
        if (getServersWithoutRegion) {
            List<ServerConn> serversWithoutRegion = new ArrayList<ServerConn>();
            serversWithoutRegion.addAll(serverConns);
            return serversWithoutRegion;
        }

        return getServerConnListByCellRoute(null);
    }

    @Override
    public List<ServerConn> getServerConnList(RouterMetaData routerMetaData) {
        if (getServersWithoutRegion) {
            List<ServerConn> serversWithoutRegion = new ArrayList<ServerConn>();
            serversWithoutRegion.addAll(serverConns);
            return serversWithoutRegion;
        }
        return getServerConnListByCellRoute(routerMetaData);
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
        serverConnListIndexTable.clear();
        deadServerSetIndexTable.clear();
    }

    @Override
    public void updateServerConn(ServerConn serverConn) {
        List<ServerConn> _serverConns = new ArrayList<ServerConn>();
        for (ServerConn conn : serversMap.values()) {
            if (conn.getServer().getIp().equalsIgnoreCase(serverConn.getServer().getIp()) &&
                    conn.getServer().getPort() == serverConn.getServer().getPort()) {
                _serverConns.add(serverConn);
                conn.getServer().setWeight(serverConn.getServer().getWeight());
            } else {
                _serverConns.add(conn);
            }
        }
        serverConns = _serverConns;
        LOG.info("update mtthrift serverList:{}", serverConns);
        updateServerConnListIndexTable();
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

            if (this.protocolRequest.equals(req) && validateServiceList(newList)) {
                updateSeverList(newList);
            }

        }
    }

}

