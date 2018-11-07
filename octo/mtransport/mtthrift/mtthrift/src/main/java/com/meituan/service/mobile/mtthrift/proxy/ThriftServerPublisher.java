package com.meituan.service.mobile.mtthrift.proxy;

import com.facebook.swift.codec.ThriftCodecManager;
import com.google.common.collect.ImmutableMap;
import com.meituan.dorado.common.RpcRole;
import com.meituan.mtrace.Tracer;
import com.meituan.service.mobile.mtthrift.annotation.ThriftMethodProcessor;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftMethodMetadata;
import com.meituan.service.mobile.mtthrift.annotation.metadata.ThriftServiceMetadata;
import com.meituan.service.mobile.mtthrift.auth.IAuthHandler;
import com.meituan.service.mobile.mtthrift.client.invoker.IMTThriftFilter;
import com.meituan.service.mobile.mtthrift.config.ThriftServerGlobalConfig;
import com.meituan.service.mobile.mtthrift.degrage.ServerDegradHandler;
import com.meituan.dorado.rpc.handler.filter.Filter;
import com.meituan.service.mobile.mtthrift.monitor.IServerMonitor;
import com.meituan.service.mobile.mtthrift.mtrace.LocalPointConf;
import com.meituan.service.mobile.mtthrift.mtrace.MtraceServerTBinaryProtocol;
import com.meituan.service.mobile.mtthrift.server.MTTServer;
import com.meituan.service.mobile.mtthrift.server.MTTThreadedSelectorServer;
import com.meituan.service.mobile.mtthrift.server.http.NettyHttpServer;
import com.meituan.service.mobile.mtthrift.server.netty.NettyServer;
import com.meituan.service.mobile.mtthrift.util.*;
import com.sankuai.sgagent.thrift.model.ConfigStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static com.google.common.collect.Maps.newHashMap;

public class ThriftServerPublisher implements FactoryBean<Object>, ApplicationContextAware, InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(ThriftServerPublisher.class);
    private static final Map<Integer, Class<?>> port2serviceInterface = new ConcurrentHashMap<Integer, Class<?>>();
    public static final Map<String, String> serviceInterfaceThriftTypeMap = new ConcurrentHashMap<String, String>();
    private static boolean catInitialized;
    private static boolean serverCatReport = true;
    private volatile boolean started;
    private volatile boolean destroyed;

    private ApplicationContext applicationContext;
    private int minWorkerThreads = 10;
    private int maxWorkerThreads = 256;
    private int workQueueSize = 0;
    private int selectorThreads = 4;
    private int port;
    private IServerMonitor serverMonitor;
    private Class<?> serviceInterface;
    private String serviceSimpleName;
    private Object serviceImpl;
    private boolean daemon = true;
    private IMTThriftFilter reuqestFilter;// rpc拦截过滤器
    private long maxRequestMessageBytes;//请求最大字节数

    private MTTServer _server;
    private NettyHttpServer httpServer;
    private boolean _interruptPublish = false;
    private String zkServers;// zookeeper地址
    private String zkPath;// 方式1：zk管理的动态集群
    private String clusterManager = "OCTO";
    private String strAgentUrl;
    private int slowStartSeconds = 180;

    private String appKey;
    private boolean serializeNullStringAsBlank = false;
    private int shutdownWaitTime = 6;
    private boolean printLog = false;
    private boolean annotatedThrift = false;
    private ServerDegradHandler serverDegradHandler = null;
    private ConfigStatus configStatus = ConfigStatusUtil.newDefaultConfigStatus();
    private String serverType = ServerType.Netty.name();
    private Map<Class<?>, ThriftServiceBean> serviceProcessorMap = new HashMap<Class<?>, ThriftServiceBean>();
    private int maxServerConn = -1;
    private int limitCount = -1;
    private int limitSecondsTime = -1;
    private IAuthHandler authHandler;
    private List<ThriftInterceptor> interceptors = Collections.emptyList();
    private List<Filter> filters;
    private BlockingQueue<Runnable> threadPoolQueue;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object getObject() throws Exception {
        return this;
    }


    @Override
    public Class<?> getObjectType() {
        return this.getClass();
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public ServerDegradHandler getServerDegradHandler() {
        return serverDegradHandler;
    }

    public static boolean isServerCatReport() {
        return serverCatReport;
    }

    public void setServerCatReport(boolean serverCatReport) {
        this.serverCatReport = serverCatReport;
    }

    public ConfigStatus getConfigStatus() {
        return configStatus;
    }

    public void setConfigStatus(ConfigStatus configStatus) {
        this.configStatus = configStatus;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (started) {
            logger.warn("ignore publish " + serviceImpl + ",you have published " + serviceInterface);
            return;
        }
        ContextInitializer.init();
        ConfigStatusUtil.checkConfigStatus(this.configStatus);

        if (port2serviceInterface.containsKey(port)) {
            _interruptPublish = true;
            if (port2serviceInterface.get(port).equals(serviceInterface)) {
                logger.error("ignore publish " + serviceImpl + ",you have published " + serviceInterface + " on " + port);
            } else {
                throw new IllegalArgumentException("you have published " + port2serviceInterface.get(port) + " on " + port + ", can't republish " + serviceInterface + " on "
                        + port);
            }
            return;
        } else if (serviceInterface != null) {
            port2serviceInterface.put(port, serviceInterface);
        }

        if (serverType.equalsIgnoreCase(ServerType.Netty.name())) {
            _server = new NettyServer(port);
            if (threadPoolQueue != null) {
                ((NettyServer) _server).setThreadPoolQueue(threadPoolQueue);
            }
        } else {
            _server = new MTTThreadedSelectorServer(port);
        }

        _server.setSelectorThreads(selectorThreads);
        _server.setWorkerThreads(minWorkerThreads);
        _server.setMaxWorkerThreads(maxWorkerThreads);
        _server.setWorkQueueSize(workQueueSize);
        _server.setAcceptQueueSizePerThread(50);
        _server.setReuqestFilter(reuqestFilter);
        _server.setMaxReadBufferBytes(maxRequestMessageBytes);
        _server.setStrAgentUrl(strAgentUrl);
        _server.setClusterManager(clusterManager);
        _server.setSlowStartSeconds(getSlowStartSeconds());
        _server.setServiceInterface(serviceInterface);
        _server.setServiceImpl(serviceImpl);
        _server.setAppKey(getAppKey());
        _server.initLocalEndPoint();
        _server.setSerializeNullStringAsBlank(serializeNullStringAsBlank);
        _server.setShutdownWaitTime(this.shutdownWaitTime);
        _server.setConfigStatus(configStatus);
        _server.setMaxServerConn(maxServerConn);
        _server.setLimitCount(limitCount);
        _server.setLimitSecondsTime(limitSecondsTime);
        _server.setAuthHandler(authHandler);

        serverDegradHandler = new ServerDegradHandler(strAgentUrl, appKey);
        serverDegradHandler.getDegradeActionsByAgent(appKey);

        if (!clusterManager.equalsIgnoreCase("OCTO"))
            logger.warn("clusterManager should changed to OCTO!");

        if ((serverType.equalsIgnoreCase(ServerType.Netty.name())) && serviceProcessorMap.size() > 0) {
            _server.getServiceBeanMap().putAll(serviceProcessorMap);
            for (Map.Entry<Class<?>, ThriftServiceBean> entry : serviceProcessorMap.entrySet()) {
                Class<?> serviceInterface = entry.getKey();
                ThriftServiceBean serviceBean = entry.getValue();
                Object serviceImpl = serviceBean.getServiceImpl();
                if (!JdkUtil.isJdk6() && AnnotationUtil.detectThriftAnnotation(serviceInterface)) {
                    TProcessor processor = new ThriftServiceProcessor(DynamicProxyUtil.createJdkDynamicProxy(serviceInterface, serviceImpl, serverMonitor, this));
                    _server.registerTProcessor(serviceInterface.getName(), processor);
                    serviceInterfaceThriftTypeMap.put(serviceInterface.getSimpleName(), "annotation");
                } else {
                    TProcessor processor = new MtTProcessor(getProcessorConstructorIface(serviceInterface).
                            newInstance(DynamicProxyUtil.createJdkDynamicProxy(getSynIfaceInterface(serviceInterface), serviceImpl, serverMonitor, this)));
                    _server.registerTProcessor(serviceInterface.getName(), processor);
                    serviceInterfaceThriftTypeMap.put(serviceInterface.getSimpleName(), "idl");
                }
                String serviceName = serviceInterface.getName();
                if (serviceBean.getServiceExecutor() != null) {
                    _server.registerExecutor(serviceName, serviceBean.getServiceExecutor());
                }
                for (Map.Entry<String, Executor> executorEntry : serviceBean.getMethodExecutor().entrySet()) {
                    String name = serviceName + "#" + executorEntry.getKey();
                    _server.registerExecutor(name, executorEntry.getValue());
                }
            }
        } else {
            ThriftServiceBean serviceBean = new ThriftServiceBean();
            serviceBean.setServiceImpl(serviceImpl);
            _server.getServiceBeanMap().put(serviceInterface, serviceBean);

            if (!JdkUtil.isJdk6() && AnnotationUtil.detectThriftAnnotation(serviceInterface)) {
                TProcessor processor = new ThriftServiceProcessor(DynamicProxyUtil.createJdkDynamicProxy(serviceInterface, serviceImpl, serverMonitor, this));
                _server.registerTProcessor(processor);
                serviceInterfaceThriftTypeMap.put(serviceInterface.getSimpleName(), "annotation");
            } else {
                TProcessor processor = new MtTProcessor(getProcessorConstructorIface(serviceInterface).
                        newInstance(DynamicProxyUtil.createJdkDynamicProxy(getSynIfaceInterface(serviceInterface), serviceImpl, serverMonitor, this)));
                _server.registerTProcessor(processor);
                serviceInterfaceThriftTypeMap.put(serviceInterface.getSimpleName(), "idl");
            }
        }

        if (_interruptPublish) {
            return;
        }
        _server.addShutDownHook();
        _server.run(daemon);


        if (!catInitialized && ThriftServerGlobalConfig.isEnableCat()) {
            TraceInfoUtil.catInitAtServer(this);
            catInitialized = true;
        }

        if (serviceProcessorMap != null && !serviceProcessorMap.isEmpty()) {
            TraceInfoUtil.useServiceProcessorMap = true;
        }

        if (serviceProcessorMap != null && serviceProcessorMap.size() > 0) {
            logger.info("mtthrift service published: {}", serviceProcessorMap.keySet().toString());
        } else {
            logger.info("mtthrift service published: {}", serviceInterface);
        }

        // 启动HttpServer
        httpServer = NettyHttpServer.buildHttpServer(RpcRole.PROVIDER);
        httpServer.addShutDownHook();

        // 记录发布服务
        if (!serviceProcessorMap.isEmpty()) {
            ThriftServerRepository.addServiceInfo(appKey, serviceProcessorMap, _server, authHandler);
        } else {
            ThriftServerRepository.addServiceInfo(appKey, _server, authHandler);
        }
        MtConfigUtil.initServerConfigListener();
        started = true;
    }

    public void publish() throws Exception {
        if (_server == null) {
            afterPropertiesSet();
        }
    }

    public void destroy() {
        if (_server != null) {
            _server.shutdown();
        }
        port2serviceInterface.remove(port);
        started = false;
        destroyed = true;
    }

    private Class<?> getSynIfaceInterface(Class<?> serviceInterface) {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && c.isInterface() && c.getSimpleName().equals("Iface")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Interface of Iface");
    }

    private Class<TProcessor> getProcessorClass(Class<?> serviceInterface) {
        Class<?>[] classes = serviceInterface.getClasses();
        for (Class c : classes)
            if (c.isMemberClass() && !c.isInterface() && c.getSimpleName().equals("Processor")) {
                return c;
            }
        throw new IllegalArgumentException("serviceInterface must contain Sub Interface of Processor");
    }

    private Constructor<TProcessor> getProcessorConstructorIface(Class<?> serviceInterface) {
        try {
            return getProcessorClass(serviceInterface).getConstructor(getSynIfaceInterface(serviceInterface));
        } catch (Exception e) {
            throw new IllegalArgumentException("serviceInterface must contain Sub Class of Processor with Constructor(Iface.class):" + e.getMessage());
        }
    }

    public String getStrAgentUrl() {
        return strAgentUrl;
    }

    public void setStrAgentUrl(String strAgentUrl) {
        this.strAgentUrl = strAgentUrl;
    }

    public String getAppKey() {
        if (StringUtils.isBlank(appKey)) {
            appKey = System.getProperty("app.key", "");
        }
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getClusterManager() {
        return clusterManager;
    }

    public void setClusterManager(String clusterManager) {
        this.clusterManager = clusterManager;
    }

    public int getSlowStartSeconds() {
        return slowStartSeconds;
    }

    public void setSlowStartSeconds(int slowStartSeconds) {
        if (slowStartSeconds < 30) {
            slowStartSeconds = 30;
            logger.warn("slowStartSeconds < 10, changed to 10 !");
        }
        if (slowStartSeconds > 600) {
            logger.warn("slowStartSeconds > 600, changed to 600 !");
            slowStartSeconds = 600;
        }
        this.slowStartSeconds = slowStartSeconds;
    }

    public boolean isSerializeNullStringAsBlank() {
        return serializeNullStringAsBlank;
    }

    public void setSerializeNullStringAsBlank(
            boolean serializeNullStringAsBlank) {
        this.serializeNullStringAsBlank = serializeNullStringAsBlank;
    }

    public int getMaxWorkerThreads() {
        return maxWorkerThreads;
    }

    public void setMaxWorkerThreads(int maxWorkerThreads) {
        this.maxWorkerThreads = maxWorkerThreads;
    }

    public int getWorkQueueSize() {
        return workQueueSize;
    }

    public void setWorkQueueSize(int workQueueSize) {
        this.workQueueSize = workQueueSize;
    }

    public void setShutdownWaitTime(int shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
    }

    public void setAnnotatedThrift(boolean annotatedThrift) {
        this.annotatedThrift = annotatedThrift;
    }

    public boolean isAnnotatedThrift() {
        return this.annotatedThrift;
    }

    public boolean isPrintLog() {
        return printLog;
    }

    private String getExceptionMessage(Throwable e) {
        StackTraceElement[] stacks = e.getStackTrace();
        if (stacks != null && stacks.length > 0) {
            StackTraceElement stackTraceElement = stacks[0];
            return e.getClass().getName() + (e.getMessage() == null ? "" : ":" + e.getMessage()) + "(" + stackTraceElement.getFileName() + "," + stackTraceElement.getMethodName() + "() line " + stackTraceElement.getLineNumber() + ")";
        } else {
            return e.getClass().getName() + (e.getMessage() == null ? "" : ":" + e.getMessage());
        }
    }

    @Deprecated
    public void setZkServers(String zkServers) {
        this.zkServers = zkServers;
    }

    @Deprecated
    public void setZkPath(String zkPath) {
        this.zkPath = zkPath;
    }

    public class MtTProcessor implements TProcessor {
        private TProcessor _processor;

        private MtTProcessor(TProcessor _processor) {
            this._processor = _processor;
        }

        public TProcessor get_processor() {
            return _processor;
        }

        @Override
        public boolean process(TProtocol in, TProtocol out) throws TException {
            try {
                return _processor.process(in, out);
            } catch (Throwable e) {
                if (e instanceof TException) {
                    throw (TException) e;
                }
                if (e instanceof UndeclaredThrowableException && null != e.getCause()) {
                    e = e.getCause();
                }
                throw new TException(e);
            }
        }
    }

    public class ThriftServiceProcessor implements TProcessor {

        private final ThriftCodecManager codecManager = new ThriftCodecManager();
        private Map<String, ThriftMethodProcessor> methods;

        public ThriftServiceProcessor(Object serviceImpl) {
            Map<String, ThriftMethodProcessor> processorMap = newHashMap();

            ThriftServiceMetadata serviceMetadata = new ThriftServiceMetadata(serviceImpl.getClass(), codecManager.getCatalog());
            for (ThriftMethodMetadata methodMetadata : serviceMetadata.getMethods().values()) {
                String methodName = methodMetadata.getName();
                ThriftMethodProcessor methodProcessor = new ThriftMethodProcessor(serviceImpl, serviceMetadata.getName(), methodMetadata, codecManager);
                if (processorMap.containsKey(methodName)) {
                    throw new IllegalArgumentException("Multiple @ThriftMethod-annotated methods named '" + methodName + "' found in the given services");
                }
                processorMap.put(methodName, methodProcessor);
            }

            methods = ImmutableMap.copyOf(processorMap);
            if (null == methods)
                logger.error("find no thrift method!");

        }

        @Override
        public boolean process(TProtocol in, TProtocol out)
                throws TException {

            TMessage message = in.readMessageBegin();
            int sequenceId = message.seqid;// lookup method
            String methodName = message.name;

            ThriftMethodProcessor method = methods.get(methodName);
            if (method == null) {
                TProtocolUtil.skip(in, TType.STRUCT);
                writeApplicationException(out, methodName, sequenceId, TApplicationException.UNKNOWN_METHOD, "Invalid method name: '" + methodName + "'", null);
                return false;
            }
            try {
                boolean result = method.process(in, out, sequenceId);
                return result;
            } catch (Throwable e) {
                if (e instanceof TException) {
                    throw (TException) e;
                }
                if (e instanceof UndeclaredThrowableException && null != e.getCause()) {
                    e = e.getCause();
                }
                throw new TException(e);
            }
        }

        public TApplicationException writeApplicationException(
                TProtocol outputProtocol,
                String methodName,
                int sequenceId,
                int errorCode,
                String errorMessage,
                Throwable cause)
                throws TException {
            //加入traceId
            errorMessage += " traceId:" + Tracer.id() + " ";

            // unexpected exception
            TApplicationException applicationException = new TApplicationException(errorCode, errorMessage);
            if (cause != null) {
                applicationException.initCause(cause);
            }

            logger.error(applicationException.getMessage(), errorMessage);

            // Application exceptions are sent to client, and the connection can be reused
            outputProtocol.writeMessageBegin(new TMessage(methodName, TMessageType.EXCEPTION, sequenceId));
            applicationException.write(outputProtocol);
            outputProtocol.writeMessageEnd();
            outputProtocol.getTransport().flush();

            return applicationException;
        }

    }

    public ThriftServerPublisher() {
    }

    public void setPrintLog(boolean printLog) {
        this.printLog = printLog;
    }

    public void setMinWorkerThreads(int minWorkerThreads) {
        this.minWorkerThreads = minWorkerThreads;
    }

    public void setSelectorThreads(int selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    public void setPort(int port) {
        this.port = port;
        System.setProperty("app.port", String.valueOf(port));
        // 存在ThriftClientProxy的情况下，其可能先初始化LocalPointConf里的port，这里再覆盖下
        LocalPointConf.setPort(port);
    }

    public void setServerMonitor(IServerMonitor serverMonitor) {
        this.serverMonitor = serverMonitor;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
        this.serviceSimpleName = serviceInterface.getSimpleName();
    }

    public Class<?> getServiceInterface() {
        return this.serviceInterface;
    }

    public String getServiceSimpleName() {
        return serviceSimpleName;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public void setReuqestFilter(IMTThriftFilter reuqestFilter) {
        this.reuqestFilter = reuqestFilter;
    }

    public void setMaxRequestMessageBytes(long maxRequestMessageBytes) {
        this.maxRequestMessageBytes = maxRequestMessageBytes;
    }

    public Map<Class<?>, ThriftServiceBean> getServiceProcessorMap() {
        return serviceProcessorMap;
    }

    public void setServiceProcessorMap(Map<Class<?>, ThriftServiceBean> serviceProcessorMap) {
        this.serviceProcessorMap = serviceProcessorMap;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    enum ServerType {
        Netty, Thrift
    }

    public int getMaxServerConn() {
        return maxServerConn;
    }

    public void setMaxServerConn(int maxServerConn) {
        this.maxServerConn = maxServerConn;
    }

    public int getLimitCount() {
        return limitCount;
    }

    public void setLimitCount(int limitCount) {
        this.limitCount = limitCount;
    }

    public int getLimitSecondsTime() {
        return limitSecondsTime;
    }

    public void setLimitSecondsTime(int limitSecondsTime) {
        this.limitSecondsTime = limitSecondsTime;
    }

    public IAuthHandler getAuthHandler() {
        return authHandler;
    }

    public void setAuthHandler(IAuthHandler authHandler) {
        this.authHandler = authHandler;
    }

    @Deprecated
    public static class Builder {
        //默认
        private int minWorkerThreads = 10;
        private int maxWorkerThreads = 256;
        private int selectorThreads = 4;
        private boolean daemon = true;
        private boolean annotatedThrift = false;
        private boolean printLog = false;
        private int slowStartSeconds = 180;
        private long maxRequestMessageBytes;//请求最大字节数，默认10 * 1024 * 1024
        private boolean serializeNullStringAsBlank = false;
        private int shutdownWaitTime = 3;
        private String clusterManager = "OCTO";

        //必填
        private int port;
        private String appKey;
        private Class<?> serviceInterface;
        private Object serviceImpl;

        //选填
        private String strAgentUrl;
        private IMTThriftFilter reuqestFilter;// rpc拦截过滤器

        public Builder(String appKey, String serviceInterface, Object serviceImpl, int port) {
            this.appKey = appKey;
            try {
                this.serviceInterface = Class.forName(serviceInterface);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            }
            this.serviceImpl = serviceImpl;
            this.port = port;
        }

        public Builder minWorkerThreads(int minWorkerThreads) {
            this.minWorkerThreads = minWorkerThreads;
            return this;
        }

        public Builder maxWorkerThreads(int maxWorkerThreads) {
            this.maxWorkerThreads = maxWorkerThreads;
            return this;
        }

        public Builder selectorThreads(int selectorThreads) {
            this.selectorThreads = selectorThreads;
            return this;
        }

        public Builder daemon(boolean daemon) {
            this.daemon = daemon;
            return this;
        }

        public Builder annotatedThrift(boolean annotatedThrift) {
            this.annotatedThrift = annotatedThrift;
            return this;
        }

        public Builder printLog(boolean printLog) {
            this.printLog = printLog;
            return this;
        }

        public Builder slowStartSeconds(int slowStartSeconds) {
            this.slowStartSeconds = slowStartSeconds;
            return this;
        }

        public Builder maxRequestMessageBytes(int maxRequestMessageBytes) {
            this.maxRequestMessageBytes = maxRequestMessageBytes;
            return this;
        }

        public Builder serializeNullStringAsBlank(boolean serializeNullStringAsBlank) {
            this.serializeNullStringAsBlank = serializeNullStringAsBlank;
            return this;
        }

        public Builder shutdownWaitTime(int shutdownWaitTime) {
            this.shutdownWaitTime = shutdownWaitTime;
            return this;
        }

        public Builder clusterManager(String clusterManager) {
            this.clusterManager = clusterManager;
            return this;
        }

        public Builder strAgentUrl(String strAgentUrl) {
            this.strAgentUrl = strAgentUrl;
            return this;
        }

        public Builder reuqestFilter(IMTThriftFilter reuqestFilter) {
            this.reuqestFilter = reuqestFilter;
            return this;
        }

        public ThriftServerPublisher build() {
            return new ThriftServerPublisher(this);
        }
    }

    private ThriftServerPublisher(Builder builder) {
        this.appKey = builder.appKey;
        this.serviceInterface = builder.serviceInterface;
        this.serviceImpl = builder.serviceImpl;
        this.port = builder.port;

        this.minWorkerThreads = builder.minWorkerThreads;
        this.maxWorkerThreads = builder.maxWorkerThreads;
        this.selectorThreads = builder.selectorThreads;
        this.daemon = builder.daemon;
        this.annotatedThrift = builder.annotatedThrift;
        this.printLog = builder.printLog;
        this.slowStartSeconds = builder.slowStartSeconds;
        this.maxRequestMessageBytes = builder.maxRequestMessageBytes;
        this.serializeNullStringAsBlank = builder.serializeNullStringAsBlank;
        this.shutdownWaitTime = builder.shutdownWaitTime;
        this.clusterManager = builder.clusterManager;

        this.strAgentUrl = builder.strAgentUrl;
        this.reuqestFilter = builder.reuqestFilter;
        this.serviceSimpleName = this.serviceInterface.getSimpleName();
        try {
            publish();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<ThriftInterceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<ThriftInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public BlockingQueue<Runnable> getThreadPoolQueue() {
        return threadPoolQueue;
    }

    public void setThreadPoolQueue(BlockingQueue<Runnable> threadPoolQueue) {
        this.threadPoolQueue = threadPoolQueue;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
