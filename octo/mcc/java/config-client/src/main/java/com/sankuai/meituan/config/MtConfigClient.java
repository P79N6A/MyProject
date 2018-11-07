package com.sankuai.meituan.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sankuai.meituan.config.exception.MtConfigException;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.meituan.config.listener.IGlobalConfigChangeListener;
import com.sankuai.meituan.config.v1.MtConfigClientV1;
import com.sankuai.meituan.config.v2.MtConfigClientV2;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MtConfigClient implements MtConfigClientInvoker, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MtConfigClient.class);

    private static final Set<String> existClientIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private String nodeName;
    private long pullPeriod = 100; // second
    private String scanBasePackage = ".";
    private String model = "v1";
    private String id;
    private String appkey;
    private String token;
    @Deprecated
    private String env = "";
    private String path = "/";

    private Multimap<String, IConfigChangeListener> listeners = HashMultimap.create();
    private MtConfigClientInvoker invoker = null;

    // 配置项监听器
    private volatile IGlobalConfigChangeListener globalConfigChangeListener = new IGlobalConfigChangeListener() {
        @Override
        public void changed(Map<String, String> oldData, Map<String, String> newData) {
            //do nothing
        }
    };

    @Override
    public void init() throws MtConfigException {
        if ("v1".equals(this.model)) {
            //只针对v1模式
            if (StringUtils.isEmpty(this.nodeName)) {
                throw new IllegalArgumentException("nodeName不能为空");
            }
            if(StringUtils.isEmpty(this.id)){
                if(!existClientIds.contains(this.nodeName)){
                    existClientIds.add(this.nodeName);
                }
                this.id = this.nodeName;
            }else{
                initId();
            }
        } else {
            initId();
        }
        try{
            generateInvoker();
            initInvoker();
        }catch (Exception e){
            existClientIds.remove(this.id);
            throw new MtConfigException("MCC 初始化失败，导致可能无法获取部分配置内容",e);
        }

    }

    private void initId() {
        if (StringUtils.isEmpty(id)) {
            this.id = this.nodeName;
        }
        if (StringUtils.isEmpty(id) || existClientIds.contains(id)) {
            throw new IllegalArgumentException("clientId(nodeName)不可为空,且系统内不允许有重复,clientId:" + id);
        } else {
            existClientIds.add(id);
        }
    }

    private void generateInvoker() {
        if ("v1".equals(model)) {
            MtConfigClientV1 mtConfigClientV1 = new MtConfigClientV1();
            mtConfigClientV1.setNodeName(nodeName);
            mtConfigClientV1.setPullPeriod(pullPeriod);
            this.invoker = mtConfigClientV1;
        } else if ("v2".equals(model)) {
            MtConfigClientV2 mtConfigClientV2 = new MtConfigClientV2();
            mtConfigClientV2.setAppkey(appkey);
            mtConfigClientV2.setEnv(env);
            mtConfigClientV2.setPath(path);
            mtConfigClientV2.setToken(token);
            this.invoker = mtConfigClientV2;
        } else {
            throw new MtConfigException("model must be v1 or v2.");
        }
    }

    private void initInvoker() {
        this.invoker.setId(this.id);
        this.invoker.setScanBasePackage(scanBasePackage);
        this.invoker.setGlobalConfigChangeListener(new IGlobalConfigChangeListener() {
            @Override
            public void changed(Map<String, String> oldData, Map<String, String> newData) {
                MtConfigClient.this.globalConfigChangeListener.changed(oldData, newData);
            }
        });
        this.invoker.init();
        //init后才设置监听器是为了避免监听器漏了同步到invoker,看一下addListener就知道了
        for (Map.Entry<String, IConfigChangeListener> listenerByKey : listeners.entries()) {
            this.invoker.addListener(listenerByKey.getKey(), listenerByKey.getValue());
        }
    }

    @Override
    public void destroy() {
        existClientIds.remove(this.id);
        if (null != invoker) {
            invoker.destroy();
        }
    }

    @Override
    public void addListener(String key, IConfigChangeListener listener) {
        if (this.invoker == null) {
            this.listeners.put(key, listener);
        } else {
            this.invoker.addListener(key, listener);
        }
    }

    @Override
    public void removeListener(String key, IConfigChangeListener listener) {
        if (StringUtils.isEmpty(key) || null == listener) {
            LOGGER.warn("Illegal parameter：key = {} , listener = {}.", key, listener);
            return;
        }
        if (null != this.invoker) {
            this.invoker.removeListener(key, listener);
        }
    }

    @Override
    public String getValue(String key) {
        return invoker.getValue(key);
    }

    @Override
    public Map<String, String> getAllKeyValues() {
        return invoker.getAllKeyValues();
    }

    @Override
    public Set<String> getAllKeys() {
        return invoker.getAllKeys();
    }

    @Override
    @Deprecated
    public Boolean setValue(String key, String value) {
        return invoker.setValue(key, value);
    }

    @Override
    public Boolean setValue(String key, String value, String nodeName) {
        return invoker.setValue(key, value, nodeName);
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
        Preconditions.checkNotNull(globalConfigChangeListener, "全局配置监听器不可为null");
        this.globalConfigChangeListener = globalConfigChangeListener;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * @deprecated (2016-02-24, Don't get configServerHost)
     */
    @Deprecated
    public String getConfigServerHost() {
        LOGGER.warn("The parameter 'configServerHost' has expired, please delete the getting code.");
        return "";
    }

    /**
     * @deprecated (2016-02-24, MCC itself can automatically identify configServerHost)
     */
    @Deprecated
    public void setConfigServerHost(String configServerHost) {
        LOGGER.warn("The parameter 'configServerHost' has expired, please delete the setting code.");
    }

    /**
     * @deprecated (2016-02-24, Don't get ZK address)
     */
    @Deprecated
    public String getZkServerList() {
        LOGGER.warn("The parameter 'zkServerList' has expired, please delete the getting code.");
        return "";
    }

    /**
     * @deprecated (2016-02-24, MCC itself can automatically identify zkServerList)
     */
    @Deprecated
    public void setZkServerList(String zkServerList) {
        LOGGER.warn("The parameter 'zkServerList' has expired, please delete the setting code.");
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Deprecated
    public void setEnv(String env) {
        LOGGER.warn("MCC itself can automatically identify env");
        this.env = env;
    }

    @Override
    public void close() {
        this.destroy();
    }
}
