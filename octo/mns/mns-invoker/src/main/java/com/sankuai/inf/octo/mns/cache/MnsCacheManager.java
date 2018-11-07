package com.sankuai.inf.octo.mns.cache;

import com.sankuai.inf.octo.mns.Consts;
import com.sankuai.inf.octo.mns.InvokeProxy;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.inf.octo.mns.util.CommonUtil;
import com.sankuai.inf.octo.mns.falcon.FalconCollect;
import com.sankuai.inf.octo.mns.model.SGAgentClient;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.inf.octo.mns.util.ScheduleTaskFactory;
import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.sgagent.thrift.model.ProtocolRequest;
import com.sankuai.sgagent.thrift.model.SGAgent;
import com.sankuai.sgagent.thrift.model.SGService;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MnsCacheManager {
    private static final Logger LOG = LoggerFactory.getLogger(MnsCacheManager.class);
    private SGAgent.Iface cacheClient = new InvokeProxy(SGAgentClient.ClientType.mns).getProxy();
    private SGAgent.Iface tempClient = new InvokeProxy(SGAgentClient.ClientType.temp).getProxy();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new ScheduleTaskFactory("MnsCacheManager-Schedule"));
    private MnsCache<String, String, List<SGService>> serviceCache = MnsCache.create(
            new ServiceListLoader());
    private MnsCache<String, String, List<SGService>> httpServiceCache = MnsCache.create(
            new HttpServiceListLoader());
    private MnsCache<String, String, List<DegradeAction>> degradeCache = MnsCache.create(
            new DegradeActionLoader());
    private MnsCache<String, String, String> authorizedConsumerCache = MnsCache.create(
            new AuthorizedConsumerLoader());
    private MnsCache<String, String, String> authorizedProviderCache = MnsCache.create();

    private ServiceCache protocolServiceCache = new ServiceCache();

    private ServiceCache serviceCacheWithZKFweight = new ServiceCacheWithZKFweight();

    private ServiceCache originServiceCache = new OriginServiceCache();

    private AppkeyDescCache appkeyDescCache = new AppkeyDescCache();

    private enum ServiceCacheType {
        PROTOCOL, WITHZKFWEIGHT, ORIGIN
    }

    private static final String METRIC_UPDATECACHE = "MnsInvoker.updateCache.meantime";

    public MnsCacheManager() {
        LOG.info("init MnsCacheManager, start update scheduler");
        scheduler.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    Long start = System.currentTimeMillis();
                    serviceCache.updateAll();
                    httpServiceCache.updateAll();
                    degradeCache.updateAll();
                    authorizedConsumerCache.updateAll();
                    protocolServiceCache.updateAll();
                    serviceCacheWithZKFweight.updateAll();
                    originServiceCache.updateAll();
                    appkeyDescCache.updateAll();
                    Long end = System.currentTimeMillis();
                    //上报更新缓存耗时
                    FalconCollect.addItem(METRIC_UPDATECACHE, "", end - start);
                } catch (Exception e) {
                    LOG.debug("update mns cache exception, " + e.getMessage(), e);
                }
            }
        }, 1, Consts.defaultUpdateTime, TimeUnit.SECONDS);
    }

    private boolean isValid(ProtocolRequest req) {
        boolean ret = (null != req)
                && !(CommonUtil.isBlankString(req.getRemoteAppkey())
                && CommonUtil.isBlankString(req.getServiceName()))
                && !CommonUtil.isBlankString(req.getProtocol());
        if (!ret) {
            LOG.error("Invalid ProtocolRequest, protocol and remoteAppkey/serviceName cannot be empty.");
        }
        return ret;
    }

    private String handleLocalAppkey(String localAppkey) {
        if (null == localAppkey) {
            LOG.debug("localAppkey is null, probably not set, please check the settings!");
            localAppkey = "";
        }
        return localAppkey;
    }

    private void pretreatServiceListRequest(ProtocolRequest req) {
        String curLocalAppkey = handleLocalAppkey(req.getLocalAppkey());

        req.setLocalAppkey(StringUtils.trim(curLocalAppkey))
                .setRemoteAppkey(StringUtils.trim(req.getRemoteAppkey()))
                .setServiceName(StringUtils.trim(req.getServiceName()))
                .setProtocol(StringUtils.trim(req.getProtocol()))
                .setSwimlane(ProcessInfoUtil.getSwimlane());
    }

    public AppkeyDescResponse getAppkeyDesc(String appkey) {
        return appkeyDescCache.get(appkey);
    }

    public List<SGService> getServiceList(ProtocolRequest req) {
        return getServiceListByType(req, ServiceCacheType.PROTOCOL);
    }

    public List<SGService> getServiceListWithZKFweight(ProtocolRequest req) {

        return getServiceListByType(req, ServiceCacheType.WITHZKFWEIGHT);
    }

    public List<SGService> getOriginServiceList(ProtocolRequest req) {
        return getServiceListByType(req, ServiceCacheType.ORIGIN);
    }

    private List<SGService> getServiceListByType(ProtocolRequest req, ServiceCacheType type) {
        if (!isValid(req)) {
            return Collections.emptyList();
        }
        pretreatServiceListRequest(req);
        switch (type) {
            case PROTOCOL:
                return protocolServiceCache.get(req);
            case WITHZKFWEIGHT:
                return serviceCacheWithZKFweight.get(req);
            case ORIGIN:
                return originServiceCache.get(req);
            default:
                LOG.error("unknown ServiceCacheType = {}", type);
                return Collections.emptyList();
        }
    }

    public List<SGService> getServiceList(String appkey,
                                          final String remoteAppKey) {
        appkey = handleLocalAppkey(appkey);
        List<SGService> list = serviceCache.get(appkey, remoteAppKey);
        if (null == list) {
            list = getServiceFromAgent(tempClient, appkey, remoteAppKey);
            if (null == list) {
                list = Collections.emptyList();
            }
            serviceCache.put(appkey, remoteAppKey, list);
        }
        if (list.isEmpty()) {
            LOG.debug("server list is empty, remoteAppKey:{} |refer to:{}", remoteAppKey, Consts.serverListEmptyWiki);
        }
        return list;
    }

    public List<SGService> getHttpServiceList(String appkey,
                                              final String remoteAppKey) {
        String localAppkey = handleLocalAppkey(appkey);
        List<SGService> list = httpServiceCache.get(localAppkey, remoteAppKey);
        if (null == list) {
            list = getHttpServiceFromAgent(tempClient, localAppkey, remoteAppKey);
            if (null == list) {
                list = Collections.emptyList();
            }
            httpServiceCache.put(localAppkey, remoteAppKey, list);

        }
        if (list.isEmpty()) {
            LOG.debug("server list is empty, remoteAppKey:{} |refer to:{}", remoteAppKey, Consts.serverListEmptyWiki);
        }
        return list;
    }

    public List<DegradeAction> getDegradeActions(final String appkey,
                                                 final String remoteAppKey) {
        List<DegradeAction> list = degradeCache.get(appkey, remoteAppKey);
        if (list == null) {
            // don't exist, put record for update, need to fetch but only once
            list = Collections.emptyList();
            degradeCache.put(appkey, remoteAppKey, list);
            list = getDegradeActionFromAgent(tempClient, appkey, remoteAppKey);
            if (null != list) {
                degradeCache.put(appkey, remoteAppKey, list);
            }
        }
        return list;
    }

    public String getAuthorizedConsumers(String consumer, String provider) {
        String jsonStr = authorizedConsumerCache.get(consumer, provider);
        if (jsonStr == null) {
            // don't exist, put record for update, need to fetch but only once
            jsonStr = "";
            // TODO 暂时直接返回，不启用，待后续和mtthrift一起梳理这里的逻辑
            authorizedConsumerCache.put(consumer, provider, jsonStr);
        }
        return jsonStr;
    }

    /**
     * 注册限制，需要在registerService之前拿到
     *
     * @param provider
     * @return
     */
    public String getAuthorizedProviders(String provider) {
        String nothing = "";
        String jsonStr = authorizedProviderCache.get(nothing, provider);
        if (jsonStr == null) {
            // don't exist, put record for update, need to fetch but only once
            jsonStr = "";
            // TODO 暂时直接返回，不启用，待后续和mtthrift一起梳理这里的逻辑
            authorizedProviderCache.put(nothing, provider, jsonStr);
        }
        return jsonStr;
    }

    private List<SGService> getServiceFromAgent(SGAgent.Iface client,
                                                String appkey, String remoteAppKey) {
        List<SGService> list = null;
        try {
            list = client.getServiceList(appkey, remoteAppKey);
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        if (null != list && list.isEmpty()) {
            FalconCollect.addItem("MnsInvoker.getServiceList.empty", "");
        }

        FalconCollect.setRate("MnsInvoker.getServiceList.success.percent", "", InvokeProxy.isSuccess.get());
        return list;
    }

    private List<SGService> getHttpServiceFromAgent(SGAgent.Iface client,
                                                    String appkey, String remoteAppKey) {
        List<SGService> list = null;
        try {
            list = client.getHttpServiceList(appkey, remoteAppKey);
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        if (null != list && list.isEmpty()) {
            FalconCollect.addItem("MnsInvoker.getHttpServiceList.empty", "");
        }
        FalconCollect.setRate("MnsInvoker.getHttpServiceList.success.percent", "", InvokeProxy.isSuccess.get());
        return list;
    }

    private List<DegradeAction> getDegradeActionFromAgent(SGAgent.Iface client,
                                                          String appkey, String remoteAppKey) {
        List<DegradeAction> list = null;
        try {
            list = client.getDegradeActions(appkey, remoteAppKey);
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        FalconCollect.setRate("MnsInvoker.getDegradeActions.success.percent", "", InvokeProxy.isSuccess.get());

        return list;
    }

    private String getAuthorizedConsumersFromAgent(String appkey) {
        String jsonStr = null;
        try {
            jsonStr = cacheClient.getAuthorizedConsumers(appkey);
        } catch (Exception e) {
            LOG.debug(e.getMessage(), e);
        }
        FalconCollect.setRate("MnsInvoker.getAuthorizedConsumers.success.percent", "", InvokeProxy.isSuccess.get());
        return jsonStr;
    }

    private String getAuthorizedProvidersFromAgent(String appkey) {
        String jsonStr = null;
        try {
            jsonStr = cacheClient.getAuthorizedProviders(appkey);
        } catch (TException e) {
            LOG.debug(e.getMessage(), e);
        }
        FalconCollect.setRate("MnsInvoker.getAuthorizedProviders.success.percent", "", InvokeProxy.isSuccess.get());
        return jsonStr;
    }

    public int addServiceListListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return handleListener(protocolServiceCache, req, listener, true);
    }


    public int addServiceListListenerWithZKFweight(ProtocolRequest req, IServiceListChangeListener listener) {
        return handleListener(serviceCacheWithZKFweight, req, listener, true);
    }

    public int addOriginServiceListListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return handleListener(originServiceCache, req, listener, true);
    }

    public int removeServiceListListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return handleListener(protocolServiceCache, req, listener, false);
    }

    public int removeServiceListListenerWithZKFweight(ProtocolRequest req, IServiceListChangeListener listener) {
        return handleListener(serviceCacheWithZKFweight, req, listener, false);
    }

    public int removeOriginServiceListListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return handleListener(originServiceCache, req, listener, false);

    }

    void clearCache() {
        protocolServiceCache.clearCache();
        serviceCacheWithZKFweight.clearCache();
        originServiceCache.clearCache();
    }


    private int handleListener(ServiceCache cache, ProtocolRequest req, IServiceListChangeListener listener, boolean isAdd) {
        if (!isValid(req) || null == listener) {
            return -1;
        }
        pretreatServiceListRequest(req);

        if (isAdd) {
            cache.addListener(req, listener);
        } else {
            return cache.removeListener(req, listener);
        }
        return 0;
    }


    class ServiceListLoader implements
            CacheLoader<String, String, List<SGService>> {
        @Override
        public List<SGService> reload(String row, String column) {
            return getServiceFromAgent(cacheClient, row, column);
        }
    }

    class HttpServiceListLoader
            implements CacheLoader<String, String, List<SGService>> {
        @Override
        public List<SGService> reload(String row, String column) {
            return getHttpServiceFromAgent(cacheClient, row, column);
        }
    }

    class DegradeActionLoader
            implements CacheLoader<String, String, List<DegradeAction>> {
        @Override
        public List<DegradeAction> reload(String row, String column) {
            return getDegradeActionFromAgent(cacheClient, row, column);
        }
    }

    class AuthorizedConsumerLoader
            implements CacheLoader<String, String, String> {
        @Override
        public String reload(String consumer, String provider) {
            return getAuthorizedConsumersFromAgent(provider);
        }
    }
}
