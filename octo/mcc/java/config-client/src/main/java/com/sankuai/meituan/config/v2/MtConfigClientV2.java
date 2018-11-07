package com.sankuai.meituan.config.v2;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.sankuai.meituan.config.MtConfigClientInvoker;
import com.sankuai.meituan.config.annotation.MtConfig;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.AnnotationConfigChangeListener;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.meituan.config.listener.IGlobalConfigChangeListener;
import com.sankuai.meituan.config.pojo.ConfigData;
import com.sankuai.meituan.config.service.BufferSgAgentService;
import com.sankuai.meituan.config.service.SgAgentService;
import com.sankuai.meituan.config.util.AnnotationUtil;
import com.sankuai.meituan.config.util.MtConfigNameUtil;
import org.apache.commons.lang.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;

public class MtConfigClientV2 implements MtConfigClientInvoker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MtConfigClientV2.class);

    /**
     * client的别名,对应着一个appkey,env和path的client实例,全应用唯一
     */
    private String id;
    /**
     * 应用名称
     */
    private String appkey;
    /**
     * 使用的环境
     */
    private String env;
    /**
     * 自定义的节点路径,用'/'进行分割,开头必须为'/',结尾不能为'/'
     */
    private String path;
    /**
     * 修改鉴权的token
     */
    private String token;
    /**
     * 缓存数据
     */
    private volatile ConfigData configData;
    /**
     * 全局配置监听器
     */
    private volatile IGlobalConfigChangeListener globalConfigChangeListener = new IGlobalConfigChangeListener() {
        @Override
        public void changed(Map<String, String> oldData, Map<String, String> newData) {
            //do nothing
        }
    };
    /**
     * 每个key对应的监听器
     */
    private ConcurrentMap<String/* key */, Set<IConfigChangeListener>> innerListeners = new ConcurrentHashMap<String, Set<IConfigChangeListener>>();
    private ConcurrentMap<String/* key */, Set<IConfigChangeListener>> customListeners = new ConcurrentHashMap<String, Set<IConfigChangeListener>>();
    /**
     * 监听器调度线程池
     */
    private ExecutorService listenerThreadPoolExecutor = Executors.newFixedThreadPool(1);
    private ExecutorService globalConfigListenerExecutor = Executors.newFixedThreadPool(1);
    /**
     * 轮询服务
     */
    private ScheduledExecutorService pullExecutorService = Executors.newScheduledThreadPool(1);
    /**
     * 同步sg_agent的时间间隔
     */
    private long pullPeriod = SgAgentService.getDefaultPullPeriod(); // ms
    /**
     * 默认扫描系统里所有的包
     */
    private String scanBasePackage = ".";

    private SgAgentService sgAgentService;
    private volatile Reflections reflections;
    private static final Object lock = new Object();

    public MtConfigClientV2() {
    }

    @Override
    public void init() throws MtConfigException {
        long startTime = System.currentTimeMillis();
        LOGGER.info("MCC初始化开始");

        sgAgentService = new BufferSgAgentService(appkey, env, path);

        // 初始化缓存
        initCacheConfig();

        // 扫描注解，在初始化缓存后面
        scanAnnotation();

        // 初始化轮询
        pullExecutorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    pull();
                } catch (Exception e) {
                    LOGGER.error(generateExceptionMsg("更新配置失败"), e);
                }
            }
        }, pullPeriod, pullPeriod, TimeUnit.MILLISECONDS);

        LOGGER.info("MCC初始化完成, 耗时 {} ms", (System.currentTimeMillis() - startTime));
    }

    @Override
    public void destroy() {

    }

    private void scanAnnotation() throws MtConfigException {
        initReflections();

        Set<Field> fields = reflections.getFieldsAnnotatedWith(MtConfig.class);
        Set<String> mtConfigKeys = new HashSet<String>();
        Map<String, String> newConfig = Maps.newHashMap();
        for (Field f : fields) {
            if (f.isAnnotationPresent(MtConfig.class)) {
                MtConfig configParam = f.getAnnotation(MtConfig.class);
                String key = configParam.key();
                Preconditions.checkState(StringUtils.isNotEmpty(configParam.key()),
                        String.format("类[%s]中的[%s]字段的MtConfig注解的key不能为空", f.getDeclaringClass().getName(), f.getName()));
                Preconditions.checkState(StringUtils.isNotEmpty(configParam.clientId()) || StringUtils.isNotEmpty(configParam.nodeName()),
                        String.format("类[%s]中的[%s]字段的MtConfig注解的nodeName和clientId属性不能同时为空,请设置[clientId]属性", f.getDeclaringClass().getName(), f.getName()));
                Preconditions.checkState(StringUtils.isEmpty(configParam.clientId()) || StringUtils.isEmpty(configParam.nodeName()),
                        String.format("类[%s]中的[%s]字段的MtConfig注解同时设定了nodeName和clientId属性,请去除[nodeName]的设置", f.getDeclaringClass().getName(), f.getName()));
                if (configParam.clientId().equals(this.id)) {
                    // 校验注解key是否有同名的，相同就抛出异常
                    if (mtConfigKeys.contains(key)) {
                        throw new MtConfigException(String.format("[MT_CONFIG_ERR] MtConfig注解初始化失败,有多个id为[%s],key为[%s]的注解配置", this.id, key));
                    } else {
                        mtConfigKeys.add(key);
                    }

                    // watch field
                    watchField(f, key, newConfig);
                }
            }
        }
        //如果注解配置的节点的key对应的配置在服务器上不存在,则将注解对应的field的默认值设置到服务器上
        if (!newConfig.isEmpty()) {
            try {
                sgAgentService.setConfig(newConfig);
            } catch (Exception e) {
                LOGGER.error(generateExceptionMsg("初始化注解field失败,无法将不存在的配置项对应的field的默认值设置到server端"), e);
            }
        }
    }

    private void initReflections() {
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
    }

    private void initCacheConfig() {
        configData = sgAgentService.getConfig();
    }

    private void pull() {
        ConfigData newConfig = null;
        try {
            long oldVersion = configData.getVersion();
            newConfig = sgAgentService.getConfig();
            long newVersion = newConfig.getVersion();

            if (oldVersion > newVersion) {
                LOGGER.warn("当前配置版本比服务器上配置版本新,请检查: cacheConfig:{}, newConfig:{}", configData, newConfig);
            }
            if (oldVersion != newVersion) {
                LOGGER.debug("MCC(v2) value changed.");
                // 更新缓存版本
                final ConcurrentMap<String, String> oldData = configData.getConfig();
                final ConcurrentMap<String, String> newData = newConfig.getConfig();
                // 更新缓存
                configData = newConfig;
                // 对比data
                diff(oldData, newData);

                globalConfigListenerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        globalConfigChangeListener.changed(oldData, newData);
                    }
                });
            }

        } catch (Exception e) {
            LOGGER.error(generateExceptionMsg("从sg_agent获取配置失败"), e);
        }
    }

    /**
     * 添加对配置项的监控，配置项值变动、新增、删除时会触发
     *
     * @param key
     * @param listener
     */
    private void addInnerListener(String key, IConfigChangeListener listener) {
        if (!innerListeners.containsKey(key)) {
            synchronized (lock) {
                if (!innerListeners.containsKey(key)) {
                    innerListeners.put(key, new HashSet<IConfigChangeListener>());
                }
            }
        }
        innerListeners.get(key).add(listener);
    }

    /**
     * 添加对配置项的自定义监控，配置项值变动、新增、删除时会触发
     */
    @Override
    public void addListener(String key, IConfigChangeListener listener) {
        if (!customListeners.containsKey(key)) {
            synchronized (lock) {
                if (!customListeners.containsKey(key)) {
                    customListeners.put(key, new HashSet<IConfigChangeListener>());
                }
            }
        }
        customListeners.get(key).add(listener);
    }

    @Override
    public void removeListener(String key, IConfigChangeListener listener) {
        if (customListeners.containsKey(key)) {
            synchronized (lock) {
                if (customListeners.containsKey(key)) {
                    Set<IConfigChangeListener> listeners = customListeners.get(key);
                    if (null != listeners && !listeners.isEmpty()) {
                        customListeners.get(key).remove(listener);
                    }
                }
            }
        }
    }

    private void watchField(final Field field, String key, Map<String, String> newConfig) {
        try {
            Class fieldType = field.getDeclaringClass();
            addInnerListener(key, new AnnotationConfigChangeListener(fieldType));

            // 对使用注解的field的默认值和CacheConfig中数据的合并
            // cacheConfig中有的，field更新；没有的，添加field的默认值
            ConcurrentMap<String, String> config = configData.getConfig();
            field.setAccessible(true);
            if (config.keySet().contains(key)) {
                Object value = AnnotationUtil.transferValueType(field, config.get(key));
                field.set(fieldType, value);
            } else {
                Object defaultValue = field.get(fieldType);
                // cache中不存在，且有默认值
                if (null != defaultValue) {
                    String value = defaultValue.toString();
                    configData.getConfig().put(key, value);
                    newConfig.put(key, value);
                }
            }
        } catch (Exception e) {
            LOGGER.error("[MT_CONFIG_WARNING] config field error. field="
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
        return configData.getConfig().get(key);
    }

    /**
     * 获取所有配置
     * key-value格式
     *
     * @return
     */
    @Override
    public Map<String, String> getAllKeyValues() {
        return new HashMap<String, String>(configData.getConfig());
    }

    /**
     * 获取所有的key
     *
     * @return
     */
    @Override
    public Set<String> getAllKeys() {
        return new HashSet<String>(configData.getConfig().keySet());
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
        try {
            sgAgentService.setConfig(key, value, token);
            return true;
        } catch (Exception e) {
            LOGGER.error(generateExceptionMsg("设置配置失败"), e);
            return false;
        }
    }

    @Override
    public Boolean setValue(String key, String value, String nodeName) {
        throw new UnsupportedOperationException("不再支持com.sankuai.meituan.config.v2.MtConfigClientV2.setValue(java.lang.String, java.lang.String, java.lang.String)方法");
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
        MapDifference<String, String> oldAndNewDiff = Maps.difference(oldData, newData);
        notifyConfigDeleted(oldAndNewDiff);
        notifyConfigUpdated(oldAndNewDiff);
        notifyConfigAdded(oldAndNewDiff);
    }

    private void notifyConfigAdded(MapDifference<String, String> oldAndNewDiff) {
        for (Map.Entry<String, String> addedEntry : oldAndNewDiff.entriesOnlyOnRight().entrySet()) {
            configChanged(addedEntry.getKey(), null, addedEntry.getValue());
        }
    }

    private void notifyConfigUpdated(MapDifference<String, String> oldAndNewDiff) {
        for (Map.Entry<String, MapDifference.ValueDifference<String>> changedEntry : oldAndNewDiff.entriesDiffering().entrySet()) {
            MapDifference.ValueDifference<String> oldAndNewValue = changedEntry.getValue();
            configChanged(changedEntry.getKey(), oldAndNewValue.leftValue(), oldAndNewValue.rightValue());
        }
    }

    private void notifyConfigDeleted(MapDifference<String, String> oldAndNewDiff) {
        for (Map.Entry<String, String> deletedEntry : oldAndNewDiff.entriesOnlyOnLeft().entrySet()) {
            configChanged(deletedEntry.getKey(), deletedEntry.getValue(), null);
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
        listenerThreadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (innerListeners.containsKey(key)) {
                    for (final IConfigChangeListener listener : innerListeners.get(key)) {
                        listener.changed(key, oldValue, newValue);
                    }
                }
                if (customListeners.containsKey(key)) {
                    executeCustomListener(key, oldValue, newValue);
                }
            }
        });
    }

    private void executeCustomListener(final String key, final String oldValue, final String newValue) {
        Set<IConfigChangeListener> iConfigChangeListeners = customListeners.get(key);
        if (null == iConfigChangeListeners) {
            return;
        }
        for (final IConfigChangeListener listener : iConfigChangeListeners) {
            listener.changed(key, oldValue, newValue);
        }
    }

    private String generateExceptionMsg(String msg) {
        return MessageFormatter.arrayFormat("{},appkey:[{}],env:[{}],path:[{}]", new Object[]{msg, appkey, env, path}).getMessage();
    }

    /**
     * @see #setId(String)
     */
    @Deprecated
    public void setNodeName(String nodeName) {
        this.id = nodeName;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public void setPath(String path) {
        MtConfigNameUtil.checkPathOfUser(path);
        this.path = StringUtils.isEmpty(path) || StringUtils.equals("/", path)? "/" : "/" + path.replace(".", "/");
    }

    @Deprecated
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

    public void setToken(String token) {
        this.token = token;
    }

}
