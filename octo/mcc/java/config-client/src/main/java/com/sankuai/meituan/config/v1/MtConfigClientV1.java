package com.sankuai.meituan.config.v1;

import com.google.common.base.Preconditions;
import com.sankuai.meituan.config.MtConfigClientInvoker;
import com.sankuai.meituan.config.annotation.MtConfig;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.AnnotationConfigChangeListener;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.meituan.config.listener.IGlobalConfigChangeListener;
import com.sankuai.meituan.config.service.HttpStatus;
import com.sankuai.meituan.config.service.RemoteConfigService;
import com.sankuai.meituan.config.service.SnapshotService;
import com.sankuai.meituan.config.util.AnnotationUtil;
import com.sankuai.meituan.config.zookeeper.INodeChangeListener;
import com.sankuai.meituan.config.zookeeper.MtZooKeeperClient;
import com.sankuai.meituan.config.zookeeper.RetryToSuccessExecutor;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author yangguo03
 * @version 1.0
 * @modified oulong
 * @created 14-4-25
 */
public class MtConfigClientV1 implements INodeChangeListener, MtConfigClientInvoker {
    private static final Logger LOG = LoggerFactory.getLogger(MtConfigClientV1.class);

    // nodeName分隔符
    public static final String NODE_NAME_SEPARATOR = ".";
    // zk路径分隔符
    public static final String ZNODE_PATH_SEPARATOR = "/";
    // zk路径前缀
    public static final String ZNODE_PATH_PREFIX = "/config";

    // 以.分隔的路径
    // 与Config Serve一致
    private String nodeName;

    // 空间名，从nodeName获取
    private String spaceName;

    // 缓存数据
    private CacheConfigV1 cacheConfigV1;

    // 最大匹配路径
    private String maxMatchPath;

    // 轮询间隔
    private long pullPeriod = 100; // second
    private int retryConnectZKPeriod = 30; //second

    // 注解扫描的基准包路径，默认全扫描
    private String scanBasePackage = ".";

    /**
     * client实例的id,全局唯一不可重复,用于注解找到对应的client
     * 默认为{@link #nodeName},可以单独设置
     */
    private String id;

    // 配置项监听器
    private volatile IGlobalConfigChangeListener globalConfigChangeListener = new IGlobalConfigChangeListener() {
        @Override
        public void changed(Map<String, String> oldData, Map<String, String> newData) {
            //do nothing
        }
    };

    // 每个配置项可以有多个Listener
    private ConcurrentMap<String/* key */, Set<IConfigChangeListener>> listeners = new ConcurrentHashMap<String, Set<IConfigChangeListener>>();
    // 调用Listener的线程池
    private ExecutorService listenerThreadPoolExecutor = Executors.newFixedThreadPool(1);
    private ExecutorService snapshotExecutor = Executors.newFixedThreadPool(1);


    private ExecutorService globalConfigListenerExecutor = Executors.newFixedThreadPool(1);

    // 需要监听的zk节点
    // 例如：nodeName=fe.crm.dev.yg，则需要监听的节点有四个：
    // /config/fe，/config/fe/crm，/config/fe/crm/dev，/config/fe/crm/dev/yg
    private List<String> zkNode2Watch;

    // zk连接
    private MtZooKeeperClient mtZooKeeperClient;

    // services
    private RemoteConfigService remoteConfigService;
    private SnapshotService snapshotService;

    // 轮询
    private ScheduledExecutorService pullExecutorService = Executors.newScheduledThreadPool(1);

    private volatile Reflections reflections;

    private static final Object lock = new Object();

    public MtConfigClientV1() {

    }

    @Override
    public void init() throws MtConfigException {
        long startTime = System.currentTimeMillis();
        LOG.info("mtconfig client init");
        if (StringUtils.isBlank(nodeName)) {
            LOG.error("nodeName is blank");
            return;
        }
        String[] nodeNameArr = nodeName.split("\\.");
        spaceName = nodeNameArr[0];

        // 初始化缓存
        initCacheConfig();

        // 扫描注解，在初始化缓存后面
        scanAnnotation();

        // 初始化轮询
        pullExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    pull();
                } catch (Exception e) {
                    LOG.error("pull failed", e);
                }
            }
        }, pullPeriod, pullPeriod, TimeUnit.SECONDS);

        RetryToSuccessExecutor executor = new RetryToSuccessExecutor(new Runnable() {
            @Override
            public void run() {
                initWatcher();
            }

            @Override
            public String toString() {
                return "连接ZK";
            }
        }, retryConnectZKPeriod);

        LOG.info("mtconfig client init finish, cost {} ms", (System.currentTimeMillis() - startTime));
    }

    @Override
    public void destroy() {
        if (mtZooKeeperClient != null) {
            mtZooKeeperClient.destroy();
        }
        if (listenerThreadPoolExecutor != null) {
            listenerThreadPoolExecutor.shutdown();
        }
        if (pullExecutorService != null) {
            pullExecutorService.shutdown();
        }
    }

    private void scanAnnotation() throws MtConfigException {
        if (null == reflections) {
            synchronized (lock) {
                // double check
                if (null == reflections) {
                    reflections = new Reflections(new ConfigurationBuilder()
                            .setUrls(ClasspathHelper.forPackage(scanBasePackage))
                            .setScanners(new FieldAnnotationsScanner()));
                }
            }
        }

        Set<Field> fields = reflections.getFieldsAnnotatedWith(MtConfig.class);
        Set<String> mtConfigKeys = new HashSet<String>();
        HashMap<String, String> newConfig = new HashMap<String, String>();
        for (Field f : fields) {
            if (f.isAnnotationPresent(MtConfig.class)) {
                MtConfig configParam = f.getAnnotation(MtConfig.class);
                String key = configParam.key();
                Preconditions.checkState(StringUtils.isNotEmpty(configParam.key()),
                        String.format("类[%s]中的[%s]字段的MtConfig注解的key不能为空", f.getDeclaringClass().getName(), f.getName()));
                boolean isClientIdEmpty=StringUtils.isEmpty(configParam.clientId());
                boolean isNodeNameEmpty=StringUtils.isEmpty(configParam.nodeName());
                Preconditions.checkState(isClientIdEmpty^isNodeNameEmpty,
                        String.format("类[%s]中的[%s]字段的MtConfig注解的nodeName和clientId属性需要且仅需要设置其中一项", f.getDeclaringClass().getName(), f.getName()));

                if (StringUtils.isNotEmpty(configParam.clientId())) {
                    if (configParam.clientId().equals(this.id)) {
                        if (mtConfigKeys.contains(key)) {
                            throw new MtConfigException(String.format("[MT_CONFIG_ERR] MtConfig注解初始化失败,有多个id为[%s]或nodeName为[%s],key为[%s]的注解配置", this.id, this.nodeName, key));
                        } else {
                            mtConfigKeys.add(key);
                        }

                        Class clazz = f.getDeclaringClass();
                        watchField(clazz, f, key, new AnnotationConfigChangeListener(clazz), newConfig);
                    }
                } else {
                    if (configParam.nodeName().equals(this.nodeName)) {
                        if (mtConfigKeys.contains(key)) {
                            throw new MtConfigException(String.format("[MT_CONFIG_ERR] MtConfig注解初始化失败,有多个id为[%s]或nodeName为[%s],key为[%s]的注解配置", this.id, this.nodeName, key));
                        } else {
                            mtConfigKeys.add(key);
                        }

                        Class clazz = f.getDeclaringClass();
                        watchField(clazz, f, key, new AnnotationConfigChangeListener(clazz), newConfig);
                    }
                }
            }
        }
        if (!newConfig.isEmpty()) {
            try {
                remoteConfigService.setValue(spaceName, nodeName, newConfig, Boolean.FALSE);
            } catch (Exception e) {
                LOG.error("setValue failed: nodeName={}", nodeName, e);
            }
        }
    }

    private void initCacheConfig() {
        if (remoteConfigService == null) {
            remoteConfigService = new RemoteConfigService();
        }
        if (snapshotService == null) {
            snapshotService = new SnapshotService();
        }
        // 从Config Server获取配置
        try {
            LOG.info("mtconfig client init config from server");
            RemoteConfigService.MergedData mergedData = remoteConfigService.getMergedData(nodeName, null);
            cacheConfigV1 = new CacheConfigV1();
            cacheConfigV1.setNodeName(nodeName);
            cacheConfigV1.setVersion(mergedData.getVersion());
            cacheConfigV1.setConfig(new ConcurrentHashMap<String, String>(mergedData.getData()));
        } catch (Exception e) {
            LOG.error("get config from server failed", e);
        }
        // Config Server不可用时，从本地snapshot获取配置
        if (cacheConfigV1 == null) {
            LOG.info("mtconfig client init config from snapshot");
            cacheConfigV1 = snapshotService.getSnapshot(nodeName);
        } else {  // Config Server可用，保存snapshot
            snapshotService.saveSnapshot(cacheConfigV1);
        }
        // Config Server不可用，且没有snapshot
        if (cacheConfigV1 == null) {
            LOG.error("can not get config from server or snapshot");
            cacheConfigV1 = new CacheConfigV1();
            cacheConfigV1.setVersion(Long.MIN_VALUE);
            cacheConfigV1.setNodeName(nodeName);
            cacheConfigV1.setConfig(new ConcurrentHashMap<String, String>());
        }
    }

    private void pull() {
        RemoteConfigService.MergedData mergedData = null;
        try {
            mergedData = remoteConfigService.getMergedData(nodeName, cacheConfigV1.getVersion());
        } catch (Exception e) {
            LOG.error("get config from server failed", e);
        }
        if (mergedData == null) {
            LOG.error("get config from server failed: response is null");
            return;
        }
        if (HttpStatus.SC_OK == mergedData.getStatusCode()) {

            long oldVersion = cacheConfigV1.getVersion();
            long newVersion = mergedData.getVersion();

            if (oldVersion > newVersion) {
                LOG.warn("version not newer: {} {} {}",
                        new Object[]{nodeName, cacheConfigV1.getVersion(), mergedData.getVersion()});
            }

            if (oldVersion != newVersion) {
                LOG.debug("MCC(v1) value changed.");
                // 更新缓存版本
                cacheConfigV1.setVersion(mergedData.getVersion());
                final Map<String, String> oldData = cacheConfigV1.getConfig();
                final Map<String, String> newData = mergedData.getData();
                // 更新缓存
                cacheConfigV1.setConfig(new ConcurrentHashMap<String, String>(mergedData.getData()));
                // 对比data
                diff(oldData, newData);
                // 更新snapshot
                snapshotExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        snapshotService.saveSnapshot(cacheConfigV1);
                    }
                });
                globalConfigListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        globalConfigChangeListener.changed(oldData, newData);
                    }
                });
            }
        }
    }

    private void initWatcher() {
        mtZooKeeperClient = MtZooKeeperClient.getInstance(remoteConfigService.getZkServerList());

        zkNode2Watch = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        sb.append(ZNODE_PATH_PREFIX);
        for (String path : nodeName.split("\\.")) {
            sb.append(ZNODE_PATH_SEPARATOR).append(path);
            zkNode2Watch.add(sb.toString());
        }

        for (String zkNode : zkNode2Watch) {
            mtZooKeeperClient.existAndListen(zkNode, this);
        }
    }

    /**
     * 添加对配置项的监控，配置项值变动、新增、删除时会触发
     *
     * @param key
     * @param listener
     */
    @Override
    public void addListener(String key, IConfigChangeListener listener) {
        if (!listeners.containsKey(key)) {
            synchronized (lock) {
                if (!listeners.containsKey(key)) {
                    listeners.put(key, new HashSet<IConfigChangeListener>());
                }
            }
        }
        listeners.get(key).add(listener);
    }

    @Override
    public void removeListener(String key, IConfigChangeListener listener) {
        LOG.warn("The method 'removeListener' is not supported in MCC V1");
    }

    /**
     * 监听一个属性
     *
     * @param field
     * @param listener
     */
    private void watchField(final Class clazz, final Field field,
                            String key,
                            IConfigChangeListener listener, final Map<String, String> newConfig) {
        try {
            if (StringUtils.isNotEmpty(this.nodeName)) {
                addListener(key, listener);

                // 对使用注解的field的默认值和CacheConfig中数据的合并
                // cacheConfig中有的，field更新；没有的，添加field的默认值
                ConcurrentMap<String, String> kvMap = cacheConfigV1.getConfig();
                field.setAccessible(true);
                if (kvMap.keySet().contains(key)) {
                    Object value = AnnotationUtil.transferValueType(field, kvMap.get(key));
                    field.set(clazz, value);
                } else {
                    Object oValue = field.get(clazz);
                    // cache中不存在，且有默认值
                    if (null != oValue) {
                        String value = oValue.toString();
                        cacheConfigV1.getConfig().put(key, value);
                        newConfig.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("[MT_CONFIG_WARNING] config field error. field="
                    + field.getName(), e);
        }
    }

    /**
     * 读取配置项的值
     *
     * @param key
     * @return
     */
    @Override
    public String getValue(String key) {
        return cacheConfigV1.getConfig().get(key);
    }

    /**
     * 获取所有配置
     * key-value格式
     *
     * @return
     */
    @Override
    public Map<String, String> getAllKeyValues() {
        return new HashMap<String, String>(cacheConfigV1.getConfig());
    }

    /**
     * 获取所有的key
     *
     * @return
     */
    @Override
    public Set<String> getAllKeys() {
        return new HashSet<String>(cacheConfigV1.getConfig().keySet());
    }

    /**
     * 设置配置项的值
     * 使用MtConfigClient初始化时的nodeName。
     *
     * @param key
     * @param value
     * @return
     */
    @Override
    public Boolean setValue(String key, String value) {
        return setValue(key, value, nodeName);
    }

    /**
     * 设置配置项的值
     * 不建议使用,以后会下线掉
     *
     * @param key
     * @param value
     * @param nodeName
     * @return
     */
    @Override
    @Deprecated
    public Boolean setValue(String key, String value, String nodeName) {
        try {
            return remoteConfigService.setValue(spaceName, nodeName, key, value, Boolean.TRUE);
        } catch (Exception e) {
            LOG.error("setValue failed: key=" + key + ", value=" + value + ", nodeName=" + nodeName, e);
            return Boolean.FALSE;
        }
    }

    /**
     * Watcher监测到变更时，从Config Server获取最新配置
     *
     * @param path
     */
    @Override
    public void updateExist(String path) {
        pull();
    }

    /**
     * 对比data，找出变更的配置项
     *
     * @param oldData
     * @param newData
     */
    private void diff(final Map<String, String> oldData, final Map<String, String> newData) {
        if (oldData == null || oldData.isEmpty() || newData == null || newData.isEmpty()) {
            return;
        }
        for (Map.Entry<String, String> oldEntry : oldData.entrySet()) {
            String key = oldEntry.getKey();
            String oldValue = oldEntry.getValue();
            String newValue = newData.get(key);

            if (newValue == null) {  // 删除
                configChanged(key, oldValue, null);
            } else if (!newValue.equals(oldValue)) {  // 修改
                configChanged(key, oldValue, newValue);
            }
        }
        for (Map.Entry<String, String> newEntry : newData.entrySet()) {
            String key = newEntry.getKey();
            String oldValue = oldData.get(key);
            String newValue = newEntry.getValue();

            if (oldValue == null) {   // 新增
                configChanged(key, null, newValue);
            }
        }
    }

    /**
     * 有变更的配置项
     *
     * @param key      配置项key
     * @param oldValue 配置项旧值，为null表示新增配置项
     * @param newValue 配置项新值，为null表示删除配置项
     */
    private void configChanged(final String key, final String oldValue, final String newValue) {
        if (!listeners.containsKey(key)) {
            return;
        }
        // TODO 使用线程池调用Listener
        for (final IConfigChangeListener listener : listeners.get(key)) {
            listenerThreadPoolExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    listener.changed(key, oldValue, newValue);
                }
            });
        }
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Deprecated
    public String getConfigServerHost() {
        LOG.warn("The parameter 'configServerHost' has expired, please delete the getting code.");
        return "";
    }

    @Deprecated
    public void setConfigServerHost(String configServerHost) {
        LOG.warn("The parameter 'configServerHost' has expired, please delete the setting code.");
    }

    public long getPullPeriod() {
        return pullPeriod;
    }

    @Override
    public void setPullPeriod(long pullPeriod) {
        this.pullPeriod = pullPeriod;
    }

    @Override
    public void setScanBasePackage(String scanBasePackage) {
        this.scanBasePackage = scanBasePackage;
    }

    @Override
    public void setGlobalConfigChangeListener(IGlobalConfigChangeListener globalConfigChangeListener) {
        Preconditions.checkNotNull(globalConfigChangeListener, "全局配置监听器(globalConfigChangeListener)不可设置为null");
        this.globalConfigChangeListener = globalConfigChangeListener;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
