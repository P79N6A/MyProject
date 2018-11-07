package com.sankuai.meituan.config.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.meituan.service.mobile.mtthrift.callback.OctoThriftCallback;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.function.Consumer;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.meituan.config.util.TaskUtil;
import com.sankuai.meituan.config.util.ZKPathBuilder;
import com.sankuai.octo.config.model.ConfigFileRequest;
import com.sankuai.octo.config.model.ConfigFileResponse;
import com.sankuai.octo.config.model.ConfigNode;
import com.sankuai.octo.config.model.Constants;
import com.sankuai.octo.sgnotify.model.ConfigUpdateEvent;
import com.sankuai.octo.sgnotify.model.ConfigUpdateResult;
import com.sankuai.octo.sgnotify.service.SgNotify;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SgNotifyService {
    private static final Logger LOG = LoggerFactory.getLogger(SgNotifyService.class);
    public static final ThreadLocal<String> currentIp = new ThreadLocal<>();

    @Resource
    private SgNotify.Iface sgNotify;

    @Resource(name = "asyncSgNotify")
    private SgNotify.AsyncIface asyncSgNotify;
    @Resource
    private ZookeeperService zookeeperService;

    private static final String SPLIT_CHAR = "+";
    private ExecutorService asycExecutor = Executors.newFixedThreadPool(20);
    private static ThreadLocal<Integer> deleteIPNum = new ThreadLocal<Integer>();

    private int notifyCount = 0;

    private boolean isNotify() {
        // don't care the syn problem
        if (200 <= notifyCount) {
            notifyCount = 0;
            return true;
        }
        return false;
    }

    //TODO 加BORP
    @Scheduled(cron = "0 25 3 * * *")
    public void flushDeprecatedRelation() {
        LOG.info("fresh config_noitfy list");
        TaskUtil.singletonExecute(new Runnable() {
            @Override
            public void run() {
                final long currentTime = System.currentTimeMillis();
                deleteIPNum.set(0);
                try {
                    for (String app : zookeeperService.getNodes(ParamName.NOTIFY_BASE_PATH)) {
                        final ZKPathBuilder appPathBuilder = ZKPathBuilder.newBuilder(ParamName.NOTIFY_BASE_PATH).appendSpace(app);
                        zookeeperService.iterateAndDeleteEmpty(appPathBuilder.toPath(), new Consumer<String>() {
                            @Override
                            public void accept(String node) {
                                final ZKPathBuilder nodePathBuilder = appPathBuilder.copy().appendSpace(node);
                                zookeeperService.iterateAndDeleteEmpty(nodePathBuilder.toPath(), new Consumer<String>() {
                                    @Override
                                    public void accept(String ip) {
                                        String ipPath = nodePathBuilder.copy().appendSpace(ip).toPath();
                                        Stat stat = zookeeperService.getStat(ipPath);
                                        if (currentTime - stat.getMtime() > getRelationExpireTime()) {
                                            LOG.info("relation [{}] had expired", ipPath);
                                            try {
                                                zookeeperService.delete(ipPath);
                                                deleteIPNum.set(deleteIPNum.get() + 1);
                                            } catch (Exception e) {
                                                LOG.warn("fail to delete node, ipPath = " + ipPath, e);
                                            }
                                            if (deleteIPNum.get() > 800) {
                                                try {
                                                    Thread.sleep(1000 * 60);
                                                } catch (InterruptedException e) {
                                                    // ignore error
                                                }
                                                deleteIPNum.set(0);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                } catch (Exception e) {
                    LOG.warn("fail to execute task.", e);
                }

            }
        });
    }

    public void asycNotifySgAgent(final String spacePath) {
        asycExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!spacePath.contains("com.sankuai.dataapp.recsys.rerank") || isNotify()) {
                        doNotify(spacePath);
                    }
                } catch (Exception e) {
                    LOG.error("fail to execute dynamic notify.", e);
                }
            }
        });
    }

    private void doNotify(final String spacePath) {
        LOG.info("notify {}", spacePath);
        Multimap<String, ConfigNode> changeNodesByIp = isRootChange(spacePath) ? getAllClient(spacePath) : findNeedNotifyClient(spacePath);
        if (changeNodesByIp != null && !changeNodesByIp.isEmpty()) {
            Map<String, Collection<ConfigNode>> maps = changeNodesByIp.asMap();
            Map<String, List<ConfigNode>> eventMap = new HashMap<String, List<ConfigNode>>();
            for (Map.Entry<String, Collection<ConfigNode>> entry : maps.entrySet()) {
                eventMap.put(entry.getKey(), new ArrayList<ConfigNode>(entry.getValue()));
            }
            List<Map<String, List<ConfigNode>>> splitList = splitGigMap(eventMap);
            for (final Map<String, List<ConfigNode>> event : splitList) {
                asycExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ConfigUpdateResult configUpdateResult = sgNotify.notifyConfig(new ConfigUpdateEvent(event));
                            checkResult(configUpdateResult);
                        } catch (Exception e) {
                            LOG.warn(MessageFormat.format("无法将配置更新信息推动给sg_notify,变更消息为:{0}", JSON.toJSONString(event.entrySet())), e);
                        }
                    }
                });
            }
        }
    }

    private List<Map<String, List<ConfigNode>>> splitGigMap(Map<String, List<ConfigNode>> originMaps) {
        List<Map<String, List<ConfigNode>>> ret = new ArrayList<Map<String, List<ConfigNode>>>();
        Map<String, List<ConfigNode>> tempMap = new HashMap<String, List<ConfigNode>>();
        for (Map.Entry<String, List<ConfigNode>> entry : originMaps.entrySet()) {
            tempMap.put(entry.getKey(), entry.getValue());
            if (tempMap.size() >= 50) {
                ret.add(tempMap);
                tempMap = new HashMap<String, List<ConfigNode>>();
            }
        }
        if (tempMap.size() < 50) {
            ret.add(tempMap);
        }
        return ret;
    }

    private void checkResult(ConfigUpdateResult configUpdateResult) {
        Map<String, Integer> failIps = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> statusByIp : configUpdateResult.getCodes().entrySet()) {
            if (com.sankuai.octo.sgnotify.model.Constants.CODE_SUCCESS != statusByIp.getValue()) {
                failIps.put(statusByIp.getKey(), statusByIp.getValue());
            }
        }
        if (!failIps.isEmpty()) {
            LOG.warn("sg_agent配置更新失败, {}", failIps.toString());
        }
    }

    private boolean isRootChange(String spacePath) {
        return StringUtils.split(spacePath, "/").length == 1;
    }

    private Multimap<String, ConfigNode> findNeedNotifyClient(String spacePath) {
        String appNotifyPath = getAppNotifyPath(spacePath);
        List<String> appNodes = zookeeperService.getNodes(appNotifyPath);
        Multimap<String, ConfigNode> changeNodesByIp = HashMultimap.create();
        String appNodePrefix = getNodePath(spacePath);
        if (CollectionUtils.isNotEmpty(appNodes)) {
            for (String appNode : appNodes) {
                if (StringUtils.startsWith(appNode, appNodePrefix)) {
                    for (String ip : zookeeperService.getNodes(appNotifyPath + "/" + appNode)) {
                        ConfigNode configNode = buildNode(spacePath, appNode);
                        if (null != configNode) {
                            changeNodesByIp.put(ip, configNode);
                        }
                    }
                }
            }
        }
        return changeNodesByIp;
    }

    private Multimap<String, ConfigNode> getAllClient(String spacePath) {
        String appNotifyPath = getAppNotifyPath(spacePath);
        List<String> appNodes = zookeeperService.getNodes(appNotifyPath);
        Multimap<String, ConfigNode> changeNodesByIp = HashMultimap.create();
        if (CollectionUtils.isNotEmpty(appNodes)) {
            for (String appNode : appNodes) {
                for (String ip : zookeeperService.getNodes(appNotifyPath + "/" + appNode)) {
                    ConfigNode configNode = buildNode(spacePath, appNode);
                    if (null != configNode) {
                        changeNodesByIp.put(ip, configNode);
                    }
                }
            }
        }
        return changeNodesByIp;
    }

    private ConfigNode buildNode(String spacePath, String appNode) {
        String[] nodes = StringUtils.split(appNode, SPLIT_CHAR);
        Assert.isTrue(nodes != null && nodes.length > 0, "使用更新通知的节点至少为2级节点");//因为client一定要填env,所以通知的节点至少为2级节点
        if (null != nodes) {
            String env = nodes[0];
            String path = "/";
            if (nodes.length > 1) {
                path += StringUtils.replace(appNode.substring(env.length() + 1), SPLIT_CHAR, "/");
            }
            return new ConfigNode(NodeNameUtil.getAppkey(spacePath), env, path);
        } else {
            return null;
        }
    }

    public void registerConnector(String spacePath) {
        String requestIp = getRequestIp();
        final String notifyNodePath = getNotifyNodePath(spacePath, requestIp);
        final long startTime = System.currentTimeMillis();
        zookeeperService.createAsyc(notifyNodePath, new Consumer<Exception>() {
            @Override
            public void accept(Exception e) {
                long useTime = System.currentTimeMillis() - startTime;
                if (e != null) {
                    LOG.error(MessageFormatter.format("写入更新通知节点失败, path:[{}], use time:[{}]", notifyNodePath, useTime).getMessage(), e);
                }
            }
        });

    }

    private String getNotifyNodePath(String spacePath, String requestIp) {
        String appkey = NodeNameUtil.getAppkey(spacePath);
        String[] eachNodes = StringUtils.split(spacePath, "/");
        String notifyNode = StringUtils.join(eachNodes, SPLIT_CHAR, 1, eachNodes.length);
        return ZKPathBuilder.newBuilder(ParamName.NOTIFY_BASE_PATH)
                .appendSpace(appkey).appendSpace(notifyNode).appendSpace(requestIp)
                .toPath();
    }

    public void syncRelation(List<String> spacePaths) {
        String requestIp = getRequestIp();
        LOG.debug("syncRelation, requestIp:{}, configNodes:{}", requestIp, spacePaths);
        for (String spacePath : spacePaths) {
            String notifyNodePath = getNotifyNodePath(spacePath, requestIp);
            try {
                zookeeperService.setData(notifyNodePath, new byte[0], -1);
            } catch (Exception e) {
                LOG.error(MessageFormatter.format("同步agent的节点关系失败, requestIp:[{}], spacePath:[{}]", requestIp, spacePath).getMessage(), e);
            }
        }
    }

    public String getRequestIp() {
        String requestIp = currentIp.get();
        Assert.hasText(requestIp, "无法获取访问的sg_agent的ip");
        return requestIp;
    }

    private String getAppNotifyPath(String spacePath) {
        return ZKPathBuilder.newBuilder(ParamName.NOTIFY_BASE_PATH).appendSpace(NodeNameUtil.getAppkey(spacePath)).toPath();
    }

    private String getNodePath(String spacePath) {
        String[] nodes = StringUtils.split(spacePath, "/");
        return StringUtils.join(nodes, SPLIT_CHAR, 1, nodes.length);
    }

    public static long getRelationExpireTime() {
        return 1000 * 60 * 60 * 24;// one day
    }

    public ConfigFileResponse distributeConfigFile(ConfigFileRequest request) throws TException {
        return doCallSgnotify(request, new AsyncSgnotifyDistributeOrEnable() {
            @Override
            public void callSgnoitfy(ConfigFileRequest req, List<SettableFuture<ConfigFileResponse>> settableFutures) throws TException {
                OctoThriftCallback<SgNotify.AsyncClient.distributeConfigFile_call, ConfigFileResponse> distributeConfigFileCallBack = new OctoThriftCallback<SgNotify.AsyncClient.distributeConfigFile_call, ConfigFileResponse>();
                asyncSgNotify.distributeConfigFile(req, distributeConfigFileCallBack);
                settableFutures.add(distributeConfigFileCallBack.getSettableFuture());
            }
        });

    }

    public ConfigFileResponse enableConfigFile(ConfigFileRequest request) throws TException {
        return doCallSgnotify(request, new AsyncSgnotifyDistributeOrEnable() {
            @Override
            public void callSgnoitfy(ConfigFileRequest req, List<SettableFuture<ConfigFileResponse>> settableFutures) throws TException {
                OctoThriftCallback<SgNotify.AsyncClient.enableConfigFile_call, ConfigFileResponse> distributeConfigFileCallBack = new OctoThriftCallback<SgNotify.AsyncClient.enableConfigFile_call, ConfigFileResponse>();
                asyncSgNotify.enableConfigFile(req, distributeConfigFileCallBack);
                settableFutures.add(distributeConfigFileCallBack.getSettableFuture());
            }
        });
    }

    private ConfigFileResponse doCallSgnotify(ConfigFileRequest request, AsyncSgnotifyDistributeOrEnable sgnotifyCall) throws TException {

        ConfigFileResponse ret = new ConfigFileResponse();
        List<String> ips = request.getHosts();
        if (null != ips) {
            try {
                Iterable<List<String>> subIpsList = Iterables.partition(ips, 10);
                List<SettableFuture<ConfigFileResponse>> settableFutures = new ArrayList<SettableFuture<ConfigFileResponse>>();
               Iterator<List<String>> ipIter = subIpsList.iterator();
                while (ipIter.hasNext()) {
                    List<String> subIps = ipIter.next();
                    ConfigFileRequest req = new ConfigFileRequest(request);
                    req.setHosts(subIps);
                    sgnotifyCall.callSgnoitfy(req, settableFutures);
                }

                ListenableFuture<List<ConfigFileResponse>> futures = Futures.successfulAsList(settableFutures);
                List<ConfigFileResponse> responses = futures.get();

                if (null != responses) {
                    ret.setHosts(new ArrayList<String>());
                    for (ConfigFileResponse item : responses) {
                        if (Constants.SUCCESS != item.getCode() && null != item.getHosts()) {
                            // get the fail enable ips
                            for (String ip : item.getHosts()) {
                                ret.getHosts().add(ip);
                            }
                            ret.setCode(item.getCode());
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("distributeConfigFile exception..." + request, e);
                ret.setCode(Constants.UNKNOW_ERROR);
                return ret;
            }
        }
        return ret;
    }

    private interface AsyncSgnotifyDistributeOrEnable {
        void callSgnoitfy(ConfigFileRequest req, List<SettableFuture<ConfigFileResponse>> settableFutures) throws TException;
    }
}
