package com.meituan.service.mobile.mtthrift.proxy;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.meituan.dorado.common.RpcRole;
import com.meituan.mtrace.Endpoint;
import com.meituan.mtrace.Tracer;
import com.meituan.service.inf.kms.client.KmsAuthDataSource;
import com.meituan.service.mobile.mtthrift.annotation.MTThriftInvocationHandler;
import com.meituan.service.mobile.mtthrift.annotation.MTThriftInvocationHandler.ThriftClientMetadata;
import com.meituan.service.mobile.mtthrift.annotation.MTThriftInvocationHandler.TypeAndName;
import com.meituan.service.mobile.mtthrift.auth.DefaultSignHandler;
import com.meituan.service.mobile.mtthrift.auth.ISignHandler;
import com.meituan.service.mobile.mtthrift.client.cell.ICellPolicy;
import com.meituan.service.mobile.mtthrift.client.cluster.DirectlyCluster;
import com.meituan.service.mobile.mtthrift.client.cluster.ICluster;
import com.meituan.service.mobile.mtthrift.client.cluster.MtThrfitInvokeInfo;
import com.meituan.service.mobile.mtthrift.client.cluster.OctoAgentCluster;
import com.meituan.service.mobile.mtthrift.client.invoker.DefaultMethodTimeoutRetryPolicy;
import com.meituan.service.mobile.mtthrift.client.invoker.DefaultTimeoutPolicy;
import com.meituan.service.mobile.mtthrift.client.invoker.IMethodTimeoutRetryPolicy;
import com.meituan.service.mobile.mtthrift.client.invoker.IResponseCollector;
import com.meituan.service.mobile.mtthrift.client.invoker.ITimeoutPolicy;
import com.meituan.service.mobile.mtthrift.client.invoker.LocalMockMethodInterceptor;
import com.meituan.service.mobile.mtthrift.client.invoker.MTThriftMethodInterceptor;
import com.meituan.service.mobile.mtthrift.client.model.Server;
import com.meituan.service.mobile.mtthrift.client.pool.MTThriftPoolConfig;
import com.meituan.service.mobile.mtthrift.client.route.ILoadBalancer;
import com.meituan.service.mobile.mtthrift.config.ThriftClientGlobalConfig;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.service.mobile.mtthrift.generic.GenericService;
import com.meituan.service.mobile.mtthrift.monitor.IClientMonitor;
import com.meituan.service.mobile.mtthrift.monitor.NullClientMonitor;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceClientTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.server.http.NettyHttpServer;
import com.meituan.service.mobile.mtthrift.transport.CustomizedTFramedTransport;
import com.meituan.service.mobile.mtthrift.util.*;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.octo.protocol.TraceInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.meituan.service.mobile.mtthrift.util.MtConfigUtil.OCTO_INVOKER_AUTH_ENABLE;
import static com.meituan.service.mobile.mtthrift.util.MtConfigUtil.OCTO_INVOKER_METHODTIMEOUTRETRY;
import static com.meituan.service.mobile.mtthrift.util.MtConfigUtil.OCTO_INVOKER_TIMEOUT;

public class ThriftClientProxy implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(ThriftClientProxy.class);
    private static final int DEFAULT_TIMEOUT = 2000;
    private static boolean catInitialized = false;
    private volatile boolean started;
    private volatile boolean destroyed;

    private Class<?> serviceInterface;
    private String genericServiceName;
    private MTThriftPoolConfig mtThriftPoolConfig;// 网络连接池配置，可不配
    private boolean async = false;// 同步还是异步，目前仅支持异步
    private static int cores = Runtime.getRuntime().availableProcessors();
    private int asyncSelectorThreadCount = cores * 2;
    private static List<TAsyncClientManager> asyncClientManagerList = null;
    private int timeout;
    private int connTimeout = Consts.connectTimeout;
    private String zkServers;// zookeeper地址
    private String zkPath;// 方式1：zk管理的动态集群
    private String serverIpPorts;// 方式2：指定的server列表
    private int localServerPort = -1; // 若设置, 则直连到本地测试服务
    private IClientMonitor clientMonitor;
    @Deprecated
    private boolean isImplFacebookService = false;
    @Deprecated
    private boolean isHabse = false;
    private int maxResponseMessageBytes = 16384000;
    private boolean serverDynamicWeight = false;
    private boolean enableRemoteDCServer = true;
    private String serviceName;

    private ICluster cluster;
    private Object serviceProxy;
    private Constructor synConstructor;
    private Constructor asynConstructor;
    private TAsyncClientManager asyncClientManager;
    private TProtocolFactory asyncProtocol;
    @Deprecated
    private String clusterManager = "OCTO";
    @Deprecated
    private String strAgentUrl;
    private boolean retryRequest = true;
    private int retryTimes = 3;
    private String appKey;
    private String remoteAppkey;
    private int remoteServerPort;
    private Endpoint localEndpoint;
    private int slowStartSeconds = 180;
    private boolean bUpdateLocalConfig = false;
    private ILoadBalancer userDefinedBalancer = null;
    private volatile ITimeoutPolicy timeoutPolicy = null;
    private volatile IMethodTimeoutRetryPolicy methodTimeoutPolicy = null;
    private IResponseCollector responseCollector = null;
    private boolean annotatedThrift = false;
    private String mockServiceImpl;
    private LoadingCache<TypeAndName, ThriftClientMetadata> clientMetadataCache;
    private boolean gzip = false;
    private boolean snappy = false;
    private boolean chenkSum = false;
    private byte[] protocol = Consts.protocol;
    private boolean disableTimeoutStackTrace = false;
    private ISignHandler signHandler;
    private boolean filterByServiceName = false;
    private boolean remoteUniProto = false;//直连时服务端是否为统一协议
    private boolean getServersWithoutRegion = false;
    private boolean isNettyIO = false;
    private ClassLoader classLoader = null;
    private String generic;
    private ICellPolicy userDefinedCellPolicy = null;
    private List<Filter> filters;


    public ICellPolicy getUserDefinedCellPolicy() {
        return userDefinedCellPolicy;
    }

    public void setUserDefinedCellPolicy(ICellPolicy userDefinedCellPolicy) {
        this.userDefinedCellPolicy = userDefinedCellPolicy;
    }

    private NettyHttpServer httpServer;

    public boolean isDisableTimeoutStackTrace() {
        return disableTimeoutStackTrace;
    }

    public void setDisableTimeoutStackTrace(boolean disableTimeoutStackTrace) {
        this.disableTimeoutStackTrace = disableTimeoutStackTrace;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setUserDefinedBalancer(Class<?> userDefinedBalancerClass) {
        try {
            userDefinedBalancer = (ILoadBalancer) (userDefinedBalancerClass.newInstance());
        } catch (InstantiationException e) {
            logger.error("InstantiationException " + e);
            userDefinedBalancer = null;
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException " + e);
            userDefinedBalancer = null;
        }
    }

    public void setTimeoutPolicy(ITimeoutPolicy timeoutPolicy) {
        this.timeoutPolicy = timeoutPolicy;
    }

    public void setMtThriftPoolConfig(MTThriftPoolConfig mtThriftPoolConfig) {
        this.mtThriftPoolConfig = mtThriftPoolConfig;
    }

    public MTThriftPoolConfig getMtThriftPoolConfig() {
        if (mtThriftPoolConfig == null) {
            mtThriftPoolConfig = new MTThriftPoolConfig();
            mtThriftPoolConfig.setMaxActive(cores * 50);
            mtThriftPoolConfig.setMaxIdle(cores * 5);
            mtThriftPoolConfig.setMinIdle(5);
            mtThriftPoolConfig.setMaxWait(500);
            mtThriftPoolConfig.setTestOnBorrow(false);
        }
        return mtThriftPoolConfig;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTimeout() {
        return timeout > 0 ? timeout : DEFAULT_TIMEOUT;
    }


    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    @Deprecated
    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public void setSnappy(boolean snappy) {
        this.snappy = snappy;
    }

    public void setChenkSum(boolean chenkSum) {
        this.chenkSum = chenkSum;
    }

    private String getZkServers() {
        return zkServers;
    }

    @Deprecated
    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    public void setServerIpPorts(String serverIpPorts) {
        this.serverIpPorts = serverIpPorts;
    }

    protected IClientMonitor getClientMonitor() {
        if (clientMonitor == null) {
            clientMonitor = new NullClientMonitor();
        }
        return clientMonitor;
    }

    public void setClientMonitor(IClientMonitor clientMonitor) {
        this.clientMonitor = clientMonitor;
    }

    public void setImplFacebookService(boolean implFacebookService) {
        // do nothing
    }

    public void setIsImplFacebookService(boolean implFacebookService) {
        // do nothing
    }

    public void setHabse(boolean habse) {
        isHabse = habse;
        if (isHabse) {
            setIsImplFacebookService(false);
        }
    }

    public int getMaxResponseMessageBytes() {
        return maxResponseMessageBytes;
    }

    public void setMaxResponseMessageBytes(int maxResponseMessageBytes) {
        if (maxResponseMessageBytes > 1000) {
            this.maxResponseMessageBytes = maxResponseMessageBytes;
        }
    }

    public void setServerDynamicWeight(boolean serverDynamicWeight) {
        this.serverDynamicWeight = serverDynamicWeight;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        if (StringUtils.isBlank(serviceName)) {
            if (generic != null) {
                this.serviceName = genericServiceName;
            } else {
                this.serviceName = serviceInterface.getName();
            }
        }

        return serviceName;
    }

    public ThriftClientProxy() {
    }

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() throws Exception {
        return serviceProxy;
    }

    public int getServersSize() {
        if (cluster != null && cluster.getServerConnList() != null) {
            return cluster.getServerConnList().size();
        }
        return 0;
    }

    @Override
    public Class<?> getObjectType() {
        if (isGeneric()) {
            return GenericService.class;
        }
        if (serviceInterface == null)
            return null;

        annotatedThrift = AnnotationUtil.detectThriftAnnotation(serviceInterface);
        return getIfaceInterface();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getMockServiceImpl() {
        return mockServiceImpl;
    }

    public void setMockServiceImpl(String mockServiceImpl) {
        this.mockServiceImpl = mockServiceImpl;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (started) {
            logger.warn("the client bean with iface={} have been built, will not do again", serviceInterface);
            return;
        }
        ContextInitializer.init();
        if (StringUtils.isBlank(appKey)) {
            appKey = MtConfigUtil.getAppName();

            if (StringUtils.isBlank(appKey)) {
                logger.warn("appKey is empty(remoteAppkey is {}, serviceInterface is {}), " +
                        "may cause problem, please use the right appKey !!!", remoteAppkey, serviceInterface);
                appKey = "";
            } else {
                logger.warn("appKey is empty(remoteAppkey is {}, serviceInterface is {}), " +
                    "replaced with app.name in /META-INF/app.properties, which is {}",
                        remoteAppkey, serviceInterface, appKey);
            }
        }

        if (serviceInterface == null && genericServiceName != null) {
            serviceInterface = GenericService.class;
        }

        // 存在多个ThriftServerPublisher的情况下，这里暂时没法取到准确的port，使用LocalPointConf.getAppPort代替
        localEndpoint = new Endpoint(appKey, LocalPointConf.getAppIp(), 0);

        if (async) {
            if (null == asyncClientManagerList) {
                synchronized (ThriftClientProxy.class) {
                    if (null == asyncClientManagerList) {
                        asyncClientManagerList = new ArrayList<TAsyncClientManager>();
                        for (int i = 0; i < asyncSelectorThreadCount; i++) {
                            asyncClientManagerList.add(new TAsyncClientManager());
                        }
                    }
                }
            }
            asyncProtocol = new MtraceClientTBinaryProtocol.Factory(localEndpoint);
        }

        if (localServerPort > 0) {
            serverIpPorts = LocalPointConf.getAppIp() + Consts.colon + localServerPort;
            logger.info("connect 2 local server: {}", serverIpPorts);
        }

        if (!JdkUtil.isJdk6()) {
            annotatedThrift = AnnotationUtil.detectThriftAnnotation(serviceInterface);
            final ThriftCodecManager codecManager = checkNotNull(new ThriftCodecManager(), "codecManager is null");
            clientMetadataCache = CacheBuilder
                    .newBuilder()
                    .build(new CacheLoader<TypeAndName, ThriftClientMetadata>() {

                        @Override
                        public ThriftClientMetadata load(TypeAndName typeAndName)
                                throws Exception {
                            return new ThriftClientMetadata(typeAndName.getType(), typeAndName.getName(), codecManager);
                        }
                    });
        }

        if (annotatedThrift && async && !isNettyIO) {
            throw new IllegalArgumentException("async call with annotated thrift is only supported in nettyIO!");
        }

        if (mockServiceImpl != null && !mockServiceImpl.trim().equals("")) {
            LocalMockMethodInterceptor clientInterceptor = new LocalMockMethodInterceptor(mockServiceImpl);
            Class<?> _interface = null;

            if (isGeneric()) {
                _interface = GenericService.class;
            } else if (annotatedThrift) {
                _interface = serviceInterface;
            } else {
                _interface = getIfaceInterface();
            }
            ProxyFactory pf = new ProxyFactory(_interface, clientInterceptor);
            serviceProxy = pf.getProxy();
            return;
        }

        if (serverIpPorts != null && serverIpPorts.trim().length() > 0 && !clusterManager.equalsIgnoreCase("DIRECT")) {
            clusterManager = "DIRECT";
            logger.warn("Parameter serverIpPorts(remoteAppkey is {}, serviceInterface is {}, serverIpPorts is {}) not empty, " +
                    "clusterManager is modified to DIRECT!", remoteAppkey, serviceInterface, serverIpPorts);
        }

        if (clusterManager.equalsIgnoreCase("OCTO")) {
            cluster = new OctoAgentCluster(getMtThriftPoolConfig(), getTimeout(), isImplFacebookService, async,
                    appKey, remoteAppkey, getRemoteServerPort(), getServiceName(), this.connTimeout
                    , filterByServiceName, isNettyIO, getServersWithoutRegion, userDefinedCellPolicy);

        } else if (clusterManager.equalsIgnoreCase("ZK") || clusterManager.equalsIgnoreCase("MIX")) {
            throw new IllegalArgumentException("mtthrift don't support zk module since 1.6.2");
        } else if (clusterManager.equalsIgnoreCase("DIRECT")) {
            slowStartSeconds = 0;
            if (serverIpPorts == null || serverIpPorts.trim().length() <= 0)
                throw new IllegalArgumentException("DIRECT clusterManager, serverIpPorts not valid");
            String[] ipPortArr = serverIpPorts.trim().split("[^0-9a-zA-Z_\\-\\.:]+");
            Set<Server> servers = new HashSet<Server>();
            for (String ipPort : ipPortArr) {
                String[] items = ipPort.split(":");
                if (items.length == 2) {
                    Server server = new Server(items[0], Integer.parseInt(items[1]), remoteUniProto);
                    server.setNettyIOSupported(isNettyIO);
                    if (isNettyIO) {
                        server.setUnifiedProto(true);
                    }
                    server.setServerAppKey(remoteAppkey);
                    servers.add(server);
                } else if (items.length == 3) {
                    Server server = new Server(items[0], Integer.parseInt(items[1]), "", Integer.parseInt(items[2]));
                    server.setUnifiedProto(remoteUniProto);
                    server.setNettyIOSupported(isNettyIO);
                    if (isNettyIO) {
                        server.setUnifiedProto(true);
                    }
                    server.setServerAppKey(remoteAppkey);
                    servers.add(server);
                } else {
                    logger.error("ignore thrift server " + ipPort);
                    continue;
                }
            }
            cluster = new DirectlyCluster(servers, getMtThriftPoolConfig(), getTimeout(),
                    isImplFacebookService, async, getServiceName(), connTimeout, isNettyIO);

        } else {
            throw new IllegalArgumentException(" property clusterManger not valid ! ");
        }

        if (this.chenkSum)
            this.protocol[0] = (byte) (this.protocol[0] | 0x80);

        if (this.gzip)
            this.protocol[0] = (byte) (this.protocol[0] | 0x40);

        if (this.snappy)
            this.protocol[0] = (byte) (this.protocol[0] | 0x20);


        if (MtConfigUtil.isMtConfigClientInitiated()) {
            this.addTimeoutConfigListener();
            this.addMethodTimeoutRetryConfigListener();
        }

        MTThriftMethodInterceptor clientInterceptor = new MTThriftMethodInterceptor(this, cluster,
                slowStartSeconds, userDefinedBalancer, responseCollector);

        clientInterceptor.setRetryTimes(this.retryTimes);
        clientInterceptor.setRetryRequest(retryRequest);
        clientInterceptor.afterPropertiesSet();// 做校验或初始化连接
        Class<?> _interface;
        if (isGeneric()) {
            _interface = GenericService.class;
            MTThriftInvocationHandler.ThriftClientMetadata clientMetadata = clientMetadataCache
                    .getUnchecked(new TypeAndName(
                            GenericService.class, GenericService.class.getName()));
            clientInterceptor.setClientMetadata(clientMetadata);
        } else if (annotatedThrift) {
            _interface = serviceInterface;
            MTThriftInvocationHandler.ThriftClientMetadata clientMetadata = clientMetadataCache
                    .getUnchecked(new TypeAndName(
                            serviceInterface, serviceInterface.getName()));
            clientInterceptor.setClientMetadata(clientMetadata);
        } else {
            _interface = getIfaceInterface();
        }

        ProxyFactory pf = new ProxyFactory(_interface, clientInterceptor);
        if (classLoader != null)
            serviceProxy = pf.getProxy(classLoader);
        else
            serviceProxy = pf.getProxy();

        //避免cat和mtrace多次初始化
        if (!catInitialized && ThriftClientGlobalConfig.isEnableCat()) {
            TraceInfoUtil.catInitAtClient(this);
            TraceInfoUtil.mtraceInitAtClient(appKey);
            catInitialized = true;
        }

        configureAuth();

        if ("DIRECT".equalsIgnoreCase(clusterManager)) {
            TraceInfoUtil.useDirect = true;
        }

        httpServer = NettyHttpServer.buildHttpServer(RpcRole.INVOKER);
        httpServer.addShutDownHook();

        ThriftClientRepository.addClientInfo(appKey, remoteAppkey, remoteServerPort, serviceInterface, cluster, signHandler);
        started = true;
    }

    /**
     * appkey不为空且没有自定义SignHandler时初始化默认的DefaultSignHandler
     * @throws Exception
     */
    private void configureAuth() throws Exception {
        ThriftClientGlobalConfig clientGlobalConfig = new ThriftClientGlobalConfig();
        if (signHandler != null) {
            Boolean enableAuthInMcc = getEnableAuthFromMcc();
            if (enableAuthInMcc == null) {
                clientGlobalConfig.setEnableAuthByMcc(true);
            } else {
                clientGlobalConfig.setEnableAuthByMcc(enableAuthInMcc);
            }
        }

        if (signHandler == null && StringUtils.isNotBlank(appKey)) {
            logger.info("initiating signHandler with appkey:{}", appKey);
            KmsAuthDataSource kmsAuthDataSource = new KmsAuthDataSource();
            kmsAuthDataSource.setAppkey(appKey);

            DefaultSignHandler defaultSignHandler = new DefaultSignHandler();
            //如果不设置，默认appkey会从app.name读取，可能会没有或者和设置的appkey不一致
            //所以这里显式设置成用户指定的appkey

            if (!appKey.equals(MtConfigUtil.getAppName())) {
                defaultSignHandler.setNamespace(appKey);
                logger.warn("app.name conflicts with appkey, appKey is {} while app.name is {}",
                        appKey, MtConfigUtil.getAppName());
                Transaction transaction = Cat.newTransaction("AppNameConflict", appKey);
                Cat.logEvent("AppNameConflict.appkey", appKey);
                Cat.logEvent("AppNameConflict.appName", MtConfigUtil.getAppName());
                transaction.complete();
            }

            defaultSignHandler.setAuthDataSource(kmsAuthDataSource);
            defaultSignHandler.afterPropertiesSet();

            this.signHandler = defaultSignHandler;
        }
    }

    /**
     * 如果有mcc配置，则返回mcc的配置
     * 如果没有mcc配置则返回null
     * 如果mcc初始失败则返回null
     * @return
     */
    private Boolean getEnableAuthFromMcc() {
        if (MtConfigUtil.isMtConfigClientInitiated()) {
            String value = MtConfigUtil.getMtConfigValue(OCTO_INVOKER_AUTH_ENABLE);
            if (StringUtils.isNotEmpty(value)) {
                return Boolean.valueOf(value);
            }
        }
        return null;
    }

    private final IConfigChangeListener timeoutPolicyListener = new IConfigChangeListener() {
        @Override
        public void changed(String key, String oldValue, String newValue) {
            if (timeoutPolicy == null
                    || ThriftClientProxy.this.timeoutPolicy instanceof DefaultTimeoutPolicy) {
                logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                timeoutPolicy = new DefaultTimeoutPolicy(newValue, getServiceSimpleName());
            }
        }
    };
    private final IConfigChangeListener methodRetryListener = new IConfigChangeListener() {
        @Override
        public void changed(String key, String oldValue, String newValue) {
            if (ThriftClientProxy.this.methodTimeoutPolicy == null
                    || ThriftClientProxy.this.methodTimeoutPolicy instanceof DefaultMethodTimeoutRetryPolicy) {
                logger.info("config[key:{}] changed from {} to {}", key, oldValue, newValue);
                ThriftClientProxy.this.methodTimeoutPolicy = new DefaultMethodTimeoutRetryPolicy(newValue, getServiceSimpleName());
            }
        }
    };

    private void addTimeoutConfigListener() {
        if (this.timeoutPolicy == null) {
            String clientTimeoutPolicy = MtConfigUtil.getMtConfigValue(OCTO_INVOKER_TIMEOUT);
            if (clientTimeoutPolicy != null) {
                logger.info("read config[key:{}, value:{}]", OCTO_INVOKER_TIMEOUT, clientTimeoutPolicy);
                logger.info("config[{}] is not null, will be used as default timeout policy", OCTO_INVOKER_TIMEOUT);
                this.timeoutPolicy = new DefaultTimeoutPolicy(clientTimeoutPolicy, getServiceSimpleName());
            }

        }

        MtConfigUtil.addMtConfigListener(OCTO_INVOKER_TIMEOUT, timeoutPolicyListener);
    }

    private void addMethodTimeoutRetryConfigListener() {
        if (this.methodTimeoutPolicy == null) {
            String clientMethodTimeoutPolicy = MtConfigUtil.getMtConfigValue(MtConfigUtil.OCTO_INVOKER_METHODTIMEOUTRETRY);
            if (clientMethodTimeoutPolicy != null) {
                logger.info("read config[key:{}, value:{}]", MtConfigUtil.OCTO_INVOKER_METHODTIMEOUTRETRY, clientMethodTimeoutPolicy);
                logger.info("config[{}] is not null, will be used as default timeout policy", MtConfigUtil.OCTO_INVOKER_METHODTIMEOUTRETRY);
                this.methodTimeoutPolicy = new DefaultMethodTimeoutRetryPolicy(clientMethodTimeoutPolicy, getServiceSimpleName());
            }
        }

        MtConfigUtil.addMtConfigListener(MtConfigUtil.OCTO_INVOKER_METHODTIMEOUTRETRY, methodRetryListener);
    }

    private String getZkServersFromProperty() {
        Properties props = new Properties();
        InputStream is = null;
        try {
            String propName = Consts.ZK_PROPERTIES_FILE;
            is = this.getClass().getClassLoader().getResourceAsStream(propName);
            props.load(is);
            return props.getProperty("zookeeper.server", "");
        } catch (Exception e) {
            logger.error("Default zk config error " + Consts.ZK_PROPERTIES_FILE);
            throw new RuntimeException("ZK properties file error", e);
        } finally {
            if (is != null)
                try {
                    is.close();
                } catch (IOException e) {
                    logger.info("close exception...", e.getMessage());
                }
        }
    }

    private String getClusterName() {
        String clusterName = null;
        if (serviceName != null && serviceName.trim().length() > 0) {
            clusterName = serviceName;
        } else if (zkPath != null && zkPath.trim().length() > 0) {
            clusterName = zkPath.replaceFirst("^/", "").replaceAll("\\|/", "_");
        } else {
            clusterName = serviceInterface.getName();
        }
        return clusterName;
    }

    public void destroy() {
        if (cluster != null) {
            cluster.destroy();
        }

        MtConfigUtil.removeMtConfigListener(OCTO_INVOKER_TIMEOUT, timeoutPolicyListener);
        MtConfigUtil.removeMtConfigListener(OCTO_INVOKER_METHODTIMEOUTRETRY, methodRetryListener);

        started = false;
        destroyed = true;
    }

    public void noticeInvoke(String methodName, String serverIpPort, long takesMills) {
        getClientMonitor().noticeInvoke(getServiceSimpleName(), methodName, serverIpPort, takesMills);
    }

    public void noticeGetConnect(long takesMills) {
        getClientMonitor().noticeGetConnect(getServiceSimpleName(), takesMills);
    }

    @Deprecated
    public void noticeException(String methodName, String serverIpPort, String exceptionMessage, Throwable e) {
    }

    public boolean isAsync() {
        return async;
    }

    private Constructor<?> getClientConstructorWithTProtocol() {
        if (async && !isNettyIO) {
            if (asynConstructor == null) {
                try {
                    asynConstructor = getAsyncClientClass().getConstructor(TProtocolFactory.class, TAsyncClientManager.class, TNonblockingTransport.class);
                } catch (Exception e) {
                    throw new IllegalArgumentException("serviceInterface must contain Sub Class of AsyncClient with Constructor(TProtocol.class)");
                }
            }
            return asynConstructor;
        } else {
            if (synConstructor == null) {
                try {
                    synConstructor = getSynClientClass().getConstructor(TProtocol.class);
                } catch (Exception e) {
                    throw new IllegalArgumentException("serviceInterface must contain Sub Class of Client with Constructor(TProtocol.class)");
                }
            }
            return synConstructor;
        }
    }

    public Object getClientInstance(TTransport socket, MtThrfitInvokeInfo mtThrfitInvokeInfo) throws IllegalAccessException, InvocationTargetException,
            InstantiationException {
        getClientConstructorWithTProtocol();
        if (async && socket instanceof TNonblockingTransport) {
            Object o = asynConstructor.newInstance(asyncProtocol, asyncClientManagerList.get(socket.hashCode() % asyncSelectorThreadCount), socket);
            ((TAsyncClient) o).setTimeout(timeout);
            return o;
        } else {
            if (isHabse) {// habse不支持TFramedTransport
                TProtocol protocol = new TBinaryProtocol(socket);
                Object o = synConstructor.newInstance(protocol);
                return o;
            } else {

                CustomizedTFramedTransport transport = (CustomizedTFramedTransport) socket;
                transport.setUnifiedProto(mtThrfitInvokeInfo.isUniProto());
                transport.setServiceName(getServiceName());
                transport.setProtocol(this.protocol);
                if (mtThrfitInvokeInfo.isUniProto()) {
                    TraceInfo traceInfo = TraceInfoUtil.getTraceInfo(localEndpoint.getAppkey(), localEndpoint.getHost());
                    transport.setTraceInfo(traceInfo);
                }

                MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
                protocol.setLocalEndpoint(localEndpoint);
                protocol.setClusterManager(clusterManager);
                return synConstructor.newInstance(protocol);
            }
        }
    }

    public TProtocol getProtocol(TTransport socket, MtThrfitInvokeInfo mtThrfitInvokeInfo) {
        CustomizedTFramedTransport transport = (CustomizedTFramedTransport) socket;
        transport.setUnifiedProto(mtThrfitInvokeInfo.isUniProto());
        transport.setServiceName(getServiceName());
        transport.setProtocol(this.protocol);
        if (mtThrfitInvokeInfo.isUniProto()) {
            TraceInfo traceInfo = TraceInfoUtil.getTraceInfo(localEndpoint.getAppkey(), localEndpoint.getHost());
            if (isGeneric()) {
                Tracer.getClientTracer().putRemoteOneStepContext(Consts.GENERIC_TAG, generic);
            }
            transport.setTraceInfo(traceInfo);
        }
        MtraceClientTBinaryProtocol protocol = new MtraceClientTBinaryProtocol(transport, mtThrfitInvokeInfo);
        protocol.setLocalEndpoint(localEndpoint);
        protocol.setClusterManager(clusterManager);
        return protocol;
    }


    private Class<?> getIfaceInterface() {
        if (async)
            return getAsyncIfaceInterface();
        else
            return getSynIfaceInterface();
    }

    private String serviceInterfaceSimpleName;

    public String getServiceSimpleName() {
        if (serviceInterfaceSimpleName != null)
            return serviceInterfaceSimpleName;
        serviceInterfaceSimpleName = serviceInterface.getSimpleName();
        return serviceInterfaceSimpleName;
    }

    private Class<?> getSynClientClass() {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Client")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Class of Client");
    }

    private Class<?> getAsyncClientClass() {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("AsyncClient")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Class of AsyncClient");
    }

    private Class<?> getSynIfaceInterface() {
        if (annotatedThrift) {
            return serviceInterface;
        } else {
            Class<?>[] classes = serviceInterface.getClasses();
            for (Class c : classes)
                if (c.isMemberClass() && c.isInterface() && c.getSimpleName()
                        .equals("Iface")) {
                    return c;
                }
            throw new IllegalArgumentException(
                    "serviceInterface must contain Sub Interface of Iface");
        }
    }

    private Class<?> getAsyncIfaceInterface() {
        if (annotatedThrift) {
            return serviceInterface;
        } else {
            Class<?>[] classes = serviceInterface.getClasses();
            for (Class c : classes)
                if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("AsyncIface")) {
                    return c;
                }
            throw new IllegalArgumentException("serviceInterface must contain Sub Interface of AsyncIface");
        }
    }

    @Deprecated
    public void setStrAgentUrl(String strAgentUrl) {
        this.strAgentUrl = strAgentUrl;
    }

    public boolean isRetryRequest() {
        return retryRequest;
    }

    public void setRetryRequest(boolean retryRequest) {
        this.retryRequest = retryRequest;
    }

    public String getRemoteAppkey() {
        return remoteAppkey;
    }

    public void setRemoteAppkey(String remoteAppkey) {
        this.remoteAppkey = remoteAppkey;
    }

    public String getClusterManager() {
        return clusterManager;
    }

    @Deprecated
    public void setClusterManager(String clusterManager) {
        this.clusterManager = clusterManager;
    }

    public int getRemoteServerPort() {
        return remoteServerPort;
    }

    public void setRemoteServerPort(int remoteServerPort) {
        this.remoteServerPort = remoteServerPort;
    }

    public boolean isEnableRemoteDCServer() {
        return enableRemoteDCServer;
    }

    public void setEnableRemoteDCServer(boolean enableRemoteDCServer) {
        this.enableRemoteDCServer = enableRemoteDCServer;
    }

    public int getSlowStartSeconds() {
        return slowStartSeconds;
    }

    public void setSlowStartSeconds(int slowStartSeconds) {
        this.slowStartSeconds = slowStartSeconds;
    }

    public void setbUpdateLocalConfig(boolean bUpdateLocalConfig) {
        this.bUpdateLocalConfig = bUpdateLocalConfig;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Deprecated
    public void setAnnotatedThrift(boolean annotatedThrift) {
        this.annotatedThrift = annotatedThrift;
    }

    public boolean getAnnotatedThrift() {
        return this.annotatedThrift;
    }

    public void setLocalServerPort(int localServerPort) {
        this.localServerPort = localServerPort;
    }

    public void setResponseCollector(Class<?> responseCollectorClass) {
        try {
            responseCollector = (IResponseCollector) (responseCollectorClass.newInstance());
        } catch (InstantiationException e) {
            logger.error("InstantiationException " + e);
            responseCollector = null;
        } catch (IllegalAccessException e) {
            logger.error("IllegalAccessException " + e);
            responseCollector = null;
        }
    }

    public ISignHandler getSignHandler() {
        return signHandler;
    }

    public void setSignHandler(ISignHandler signHandler) {
        this.signHandler = signHandler;
    }

    public boolean isFilterByServiceName() {
        return filterByServiceName;
    }

    public void setFilterByServiceName(boolean filterByServiceName) {
        this.filterByServiceName = filterByServiceName;
    }

    /**
     * 直连时服务端是否为统一协议
     */
    public boolean isRemoteUniProto() {
        return remoteUniProto;
    }

    /**
     * 直连时服务端是否为统一协议
     *
     * @param remoteUniProto
     */
    public void setRemoteUniProto(boolean remoteUniProto) {
        this.remoteUniProto = remoteUniProto;
    }

    public Endpoint getLocalEndpoint() {
        return localEndpoint;
    }

    public boolean isNettyIO() {
        return isNettyIO;
    }

    public void setNettyIO(boolean nettyIO) {
        isNettyIO = nettyIO;
    }

    public boolean getCheckSum() {
        return this.chenkSum;
    }

    public boolean getGzip() {
        return this.gzip;
    }

    public boolean getSnappy() {
        return this.snappy;
    }

    public ITimeoutPolicy getTimeoutPolicy() {
        return timeoutPolicy;
    }

    public boolean isGetServersWithoutRegion() {
        return getServersWithoutRegion;
    }

    public void setGetServersWithoutRegion(boolean getServersWithoutRegion) {
        this.getServersWithoutRegion = getServersWithoutRegion;
    }

    public IMethodTimeoutRetryPolicy getMethodTimeoutPolicy() {
        return methodTimeoutPolicy;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getGeneric() {
        return generic;
    }

    public boolean isGeneric() {
        return generic != null;
    }

    public void setGeneric(String generic) {
        this.generic = generic;
    }

    public String getGenericServiceName() {
        return genericServiceName;
    }

    public void setGenericServiceName(String genericServiceName) {
        this.genericServiceName = genericServiceName;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}