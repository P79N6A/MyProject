package com.sankuai.inf.octo.mns;

import com.sankuai.inf.octo.mns.cache.ConfigCacheManager;
import com.sankuai.inf.octo.mns.cache.MnsCacheManager;
import com.sankuai.inf.octo.mns.listener.IServiceListChangeListener;
import com.sankuai.octo.appkey.model.AppkeyDesc;
import com.sankuai.octo.appkey.model.AppkeyDescResponse;
import com.sankuai.octo.oswatch.thrift.data.DegradeAction;
import com.sankuai.sgagent.thrift.model.*;
import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.List;

public class MnsInvoker {

    private static MnsCacheManager cacheManager;
    private static ConfigCacheManager configCacheManager;
    private static RegistryManager registryManager;
    private static String localIP = com.sankuai.inf.octo.mns.util.ProcessInfoUtil.getLocalIpV4();

    static {
        cacheManager = new MnsCacheManager();
        configCacheManager = new ConfigCacheManager();
        registryManager = new RegistryManager();
    }

    public MnsInvoker() {
    }

    private static SGService getDefaultSGService(final String appkey, final int port, final boolean isThrift) {
        SGService service = new SGService();
        service.setAppkey(appkey)
                .setPort(port).setIp(localIP)
                .setVersion(isThrift ? "original" : "HLB")
                .setLastUpdateTime((int) (System.currentTimeMillis() / 1000))
                .setServerType(isThrift ? 0 : 1)
                .setWeight(10).setFweight(10.d)
                .setProtocol(isThrift ? "thrift" : "http")
                .setExtend("OCTO|slowStartSeconds:180");
        return service;
    }

    public static void registerThriftService(final String appkey, final int port) throws TException {
        registryManager.registerService(getDefaultSGService(appkey, port, true));
    }

    public static void unRegisterThriftService(final String appkey, final int port) throws TException {
        registryManager.unRegisterService(getDefaultSGService(appkey, port, true));
    }

    public static void registerHttpService(final String appkey, final int port) throws TException {
        registryManager.registerService(getDefaultSGService(appkey, port, false));
    }

    public static void unRegisterHttpService(final String appkey, final int port) throws TException {
        registryManager.unRegisterService(getDefaultSGService(appkey, port, false));
    }

    /**
     * @param sgService
     * @throws TException
     */
    public static void registerService(SGService sgService) throws TException {
        registryManager.registerService(sgService);
    }

    public static void registServiceWithCmd(int uptCmd, SGService sgService) throws TException {
        registryManager.registerServiceWithCmd(uptCmd, sgService);
    }

    public static void unRegisterService(SGService sgService) throws TException {
        registryManager.unRegisterService(sgService);
    }

    public static List<SGService> getSGServiceList(final String appkey, final String remoteAppkey) {
        return cacheManager.getServiceList(appkey, remoteAppkey);
    }

    public static List<SimpleServerInfo> getServerList(final String appkey, final String remoteAppkey) {
        List<SGService> sgServiceList = getSGServiceList(appkey, remoteAppkey);
        List<SimpleServerInfo> serverList = new ArrayList<SimpleServerInfo>();
        for (SGService sgService : sgServiceList) {
            if (2 != sgService.getStatus()) {
                continue;
            }
            SimpleServerInfo server = new SimpleServerInfo(sgService.getIp(),
                    sgService.getPort(), sgService.getFweight());
            if (sgService.getFweight() <= 0) {
                server.setWeight(sgService.getWeight());
            }
            serverList.add(server);
        }
        return serverList;
    }

    public static List<SGService> getHttpServiceList(final String appkey, final String remoteAppkey) {
        return cacheManager.getHttpServiceList(appkey, remoteAppkey);
    }

    // Degrade
    public static List<DegradeAction> getDegradeActionListAtClient(
            final String appkey, final String remoteAppkey) {
        return cacheManager.getDegradeActions(appkey, remoteAppkey);
    }

    public static List<DegradeAction> getDegradeActionListAtServer(final String appkey) {
        return cacheManager.getDegradeActions("", appkey);
    }

    // Auth
    public static String getAuthorizedConsumers(String appkey, String remoteAppkey) {
        return cacheManager.getAuthorizedConsumers(appkey, remoteAppkey);
    }

    public static String getAuthorizedProviders(String remoteAppkey) {
        return cacheManager.getAuthorizedProviders(remoteAppkey);
    }

    // MCC
    public static int setConfig(String appkey, String data) throws TException {
        return configCacheManager.setConfig(appkey, data);
    }

    public static String getConfig(proc_conf_param_t param_t) throws TException {
        return configCacheManager.getConfig(param_t);
    }

    public static String getConfig(String appkey) throws TException {
        return configCacheManager.getConfig(appkey);
    }

    public static String getConfig(String appkey, String env, String path) throws TException {
        return configCacheManager.getConfig(appkey, env, path);
    }

    public static byte[] getFileConfig(String appkey, String filename) throws TException {
        return configCacheManager.getFileConfig(appkey, filename);
    }

    public static int addServiceListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return cacheManager.addServiceListListener(req, listener);
    }

    public static int removeServiceListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return cacheManager.removeServiceListListener(req, listener);
    }

    public static int addServiceListListenerWithZKFweight(ProtocolRequest req, IServiceListChangeListener listener) {
        return cacheManager.addServiceListListenerWithZKFweight(req, listener);
    }

    public static int removeServiceListListenerWithZKFweight(ProtocolRequest req, IServiceListChangeListener listener) {
        return cacheManager.removeServiceListListenerWithZKFweight(req, listener);
    }

    public static int addOriginServiceListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return cacheManager.addOriginServiceListListener(req, listener);
    }

    public static int removeOriginServiceListener(ProtocolRequest req, IServiceListChangeListener listener) {
        return cacheManager.removeOriginServiceListListener(req, listener);
    }

    /**
     * @param appkey
     * @return
     * @deprecated
     */
    @Deprecated
    public static int fileConfigAddApp(String appkey) {
        return configCacheManager.fileConfigAddApp(appkey);
    }

    @Deprecated
    public static int addListener(String appkey, String filename, com.sankuai.inf.octo.mns.IFileChangeListener listener) {
        return configCacheManager.addListener(appkey, filename, listener);
    }

    public static int addListener(String appkey, String filename, com.sankuai.inf.octo.mns.listener.IFileChangeListener listener) {
        return configCacheManager.addListener(appkey, filename, listener);
    }

    /**
     * @deprecated. Use MnsInvoker.Method() directly.
     */
    @Deprecated
    public static MnsInvoker getInstance() {
        return getInstance(strAgentUrl);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String getAuthorizedConsumers(String appkey, String remoteAppkey, boolean useCached) {
        return cacheManager.getAuthorizedConsumers(appkey,
                remoteAppkey);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static String getAuthorizedProviders(String remoteAppkey, boolean useCached) {
        return cacheManager.getAuthorizedProviders(remoteAppkey);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static void registerService(String appkey, int port, String version) throws TException {
        SGService service = getDefaultSGService(appkey, port, true);
        service.setVersion(version);
        registryManager.registerService(service);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static List<SGService> getSGServiceList(final String appkey,
                                                   final String remoteAppkey, boolean useCached) {
        return cacheManager.getServiceList(appkey, remoteAppkey);
    }

    public static List<SGService> getServiceList(ProtocolRequest req) {
        return cacheManager.getServiceList(req);
    }

    public static List<SGService> getServiceListWithZKFweight(ProtocolRequest req) {
        return cacheManager.getServiceListWithZKFweight(req);
    }

    public static List<SGService> getOriginServiceList(ProtocolRequest req) {
        return cacheManager.getOriginServiceList(req);
    }

    public static AppkeyDescResponse getAppkeyDesc(String appkey) {
        return cacheManager.getAppkeyDesc(appkey);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static List<SGService> getHttpServiceList(final String appkey,
                                                     final String remoteAppkey, boolean useCached) {
        return cacheManager.getHttpServiceList(appkey, remoteAppkey);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static List<SimpleServerInfo> getServerList(final String appkey,
                                                       final String remoteAppkey, boolean useCached) {
        return getServerList(appkey, remoteAppkey);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static List<DegradeAction> getDegradeActionListAtClient(
            final String appkey, final String remoteAppkey, boolean useCached) {
        return cacheManager.getDegradeActions(appkey, remoteAppkey);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static List<DegradeAction> getDegradeActionListAtServer(
            final String appkey, boolean useCached) {
        return getDegradeActionListAtServer(appkey);
    }

    /**
     * @deprecated, A bad design.
     */
    @Deprecated
    public static void shutdownAgentConnection() {
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static byte[] getFileConfig(String appkey, String filename, boolean useCached) throws TException {
        return configCacheManager.getFileConfig(appkey, filename);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public static int setConfig(String appkey, String env, String path, String data) throws TException {
        return configCacheManager.setConfig(appkey, env, path, data);
    }

    public static int setConfig(proc_conf_param_t confParam) throws TException {
        return configCacheManager.setConfig(confParam);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int uploadLog(SGLog oLog) {
        return 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    public int uploadModuleInvoke(SGModuleInvokeInfo oInfo) {
        return 0;
    }

    /**
     * @deprecated
     */
    @Deprecated
    private static String strAgentUrl = Consts.localAgent;
    /**
     * @deprecated
     */
    @Deprecated
    private volatile static MnsInvoker mnsInvoker = null;

    /**
     * @deprecated. strAgentUrl is not supported.
     * OCTO env is suggested: http://wiki.sankuai.com/x/pnrnEw
     */
    @Deprecated
    private MnsInvoker(String strAgentUrl) {
        this.strAgentUrl = strAgentUrl;
    }

    /**
     * @deprecated. strAgentUrl is not supported.
     * OCTO env is suggested: http://wiki.sankuai.com/x/pnrnEw
     */
    @Deprecated
    public static synchronized MnsInvoker getInstance(String strAgentUrl) {
        if (null != mnsInvoker) {
            return mnsInvoker;
        }
        mnsInvoker = new MnsInvoker(strAgentUrl);
        return mnsInvoker;
    }
}
