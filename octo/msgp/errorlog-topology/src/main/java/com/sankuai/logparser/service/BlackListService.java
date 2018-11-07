package com.sankuai.logparser.service;

import com.google.common.util.concurrent.RateLimiter;
import com.sankuai.inf.octo.mns.util.ProcessInfoUtil;
import com.sankuai.logparser.util.ConcurrentHashSet;
import com.sankuai.logparser.util.DefaultThreadFactory;
import com.sankuai.logparser.util.MtConfig;
import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.meituan.config.listener.IConfigChangeListener;
import com.sankuai.msgp.common.utils.client.Messager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by emma on 2017/8/1.
 */
public class BlackListService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlackListService.class);
    private MtConfigClient topologyConfigClient = MtConfig.getTopologyCfgClient();

    private static final String APPKEY_DYNAMIC_BLACKLIST_CFG = "appkey.blacklist.dynamic";
    private static final String APPKEY_FIXED_BLACKLIST_CFG = "appkey.blacklist.fixed";
    private static final String MINUTE_LIMITED_THRESHOLD_CFG = "appkey.limited.per.minute";
    private static final String SECONDS_LIMITED_THRESHOLD_CFG = "appkey.limited.per.seconds";
    private static final String DEFAULT_LIMITED_KEY = "defaults";
    private static final Integer CHECK_INTERVAL = 60;
    private static final boolean IS_ONLINE = ProcessInfoUtil.isLocalHostOnline();
    private static final String ENV = IS_ONLINE ? "" : "【线下】";
    private static final String MSGP_LINK = IS_ONLINE ? "http://octo.sankuai.com" : "http://octo.test.sankuai.info";

    /**
     * 分钟粒度统计相关变量
     */
    private volatile static Map<String, Integer> perMinlimitedThreshodMap;
    private volatile static Set<String> appkeyFixedBlackSet;
    private volatile static Set<String> appkeyDynamicBlackSet;
    private volatile ConcurrentHashMap<String, AtomicInteger> perMinLogCountMap = new ConcurrentHashMap<>();
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * 秒拦截相关变量
     * 每个appkey默认每秒可以有2000个log
     */
    private static Double DEFAULT_PERMITS_PER_SECOND = 2000.0;
    private static final int INIT_LIMITER_COUNT = 50;
    private static long limiterInitTime = System.currentTimeMillis();
    private static List<RateLimiter> limiterList = new Vector<>();
    private static volatile boolean hasLimiterInList = false;
    private ConcurrentHashMap<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();
    // 每秒日志过多appkey
    private volatile ConcurrentHashMap<String, AtomicInteger> secLogNumOverMap = new ConcurrentHashMap<>();

    static {
        for (int i = 0; i < INIT_LIMITER_COUNT; i++) {
            RateLimiter limiter = RateLimiter.create(DEFAULT_PERMITS_PER_SECOND);
            limiterList.add(limiter);
        }
        // RateLimiter初始化后1s内没有1w个令牌, 需等待1s, 避免新加入的服务日志量太大, 导致误判
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOGGER.error("Init RateLimiter list, sleep fail.", e);
        }
        hasLimiterInList = true;
    }

    public static BlackListService getInstance() {
        return BackListServiceHolder.instance;
    }

    private static class BackListServiceHolder {
        private static final BlackListService instance = new BlackListService();
    }

    private BlackListService() {
        initConfig();
        checkErrorLogCountTask();
    }

    /**
     * 获取某个Appkey的许可, 每秒限制2000
     *
     * @param appkey
     * @return
     */
    public boolean getPermitPerSecond(String appkey) {
        RateLimiter limiter = null;
        if (rateLimiterMap.containsKey(appkey)) {
            limiter = rateLimiterMap.get(appkey);
        } else {
            if (hasLimiterInList) {
                synchronized (BlackListService.class) {
                    if (limiterList.isEmpty()) {
                        hasLimiterInList = false;
                    } else {
                        if (rateLimiterMap.containsKey(appkey)) {
                            limiter = rateLimiterMap.get(appkey);
                        } else {
                            limiter = limiterList.remove(0);
                            rateLimiterMap.putIfAbsent(appkey, limiter);
                        }
                    }
                }
            }
            if (limiter == null) {
                synchronized (BlackListService.class) {
                    if (rateLimiterMap.containsKey(appkey)) {
                        limiter = rateLimiterMap.get(appkey);
                    } else {
                        limiter = RateLimiter.create(DEFAULT_PERMITS_PER_SECOND);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            LOGGER.error("Init RateLimiter, sleep fail.", e);
                        }
                        rateLimiterMap.putIfAbsent(appkey, limiter);
                    }
                }
            }
        }
        return limiter.tryAcquire();
    }

    public void secondsLogOver(String appkey) {
        if (!secLogNumOverMap.containsKey(appkey)) {
            secLogNumOverMap.putIfAbsent(appkey, new AtomicInteger(10));
            sendLogDiscardMessage(appkey, DEFAULT_PERMITS_PER_SECOND.intValue());
        }
    }

    /**
     * 判断appkey是否在黑名单
     *
     * @param appkey
     * @return
     */
    public boolean isInFixedBlackList(String appkey) {
        return appkeyFixedBlackSet.contains(appkey);
    }

    public boolean isInDynamicBlackList(String appkey) {
        return appkeyDynamicBlackSet.contains(appkey);
    }

    public void countAppkeyLogPerMin(String appkey) {
        try {
            rwLock.readLock().lock();
            AtomicInteger logCount = perMinLogCountMap.putIfAbsent(appkey, new AtomicInteger(1));
            if (logCount != null) {
                perMinLogCountMap.get(appkey).incrementAndGet();
            }
        } catch (Exception e) {
            LOGGER.error("countAppkeyLogPerMin fail", e);
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void checkErrorLogCountTask() {
        ScheduledExecutorService checkLogCount = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("CheckLogCount", true));
        checkLogCount.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    checkMinErrorLogCount();
                    scanSecErrorLogCount();
                    if (hasLimiterInList && System.currentTimeMillis() - limiterInitTime > 86400 * 1000) {
                        // 一天后清理掉多初始化的RateLimiter
                        synchronized (BlackListService.class) {
                            // TODO 测试log, 验证后可删除
                            LOGGER.info("Clear limiterList item, current size={}", limiterList.size());
                            limiterList.clear();
                            hasLimiterInList = false;
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("CheckErrorLogCount fail", e);
                }
            }
        }, 0L, CHECK_INTERVAL, TimeUnit.SECONDS);
    }

    private void checkMinErrorLogCount() {
        ConcurrentHashMap<String, AtomicInteger> perMinLogCountTmpMap = new ConcurrentHashMap<>();
        try {
            rwLock.writeLock().lock();
            perMinLogCountTmpMap.putAll(perMinLogCountMap);
            perMinLogCountMap.clear();
        } finally {
            rwLock.writeLock().unlock();
        }
        Iterator<Map.Entry<String, AtomicInteger>> iterator = perMinLogCountTmpMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AtomicInteger> entry = iterator.next();
            String appkey = entry.getKey();
            int perMinLogNum = entry.getValue().get(); //获取 appkey 一分钟的log数
            int limitThreshold = getBlackListThreshold(appkey);
            if (perMinLogNum > limitThreshold) {
                // 超过阈值放入动态黑名单
                if (addDynamicBlacklist(appkey)) {
                    LOGGER.info(appkey + " log num = {} > {}, limited", perMinLogNum, limitThreshold);
                    // 发送通知 TODO 存在的别名的appkey需要重新映射
//                    String oriAppkey = AppkeyAliasDao.octoAppkey(appkey);
                    sendLimitMessage(appkey, perMinLogNum, limitThreshold);
                }
            } else if (isInDynamicBlackList(appkey)) {
                // 动态黑名单中appkey的日志已经低于阈值
                if (deleteDynamicBlacklist(appkey)) {
                    LOGGER.info(appkey + " log num = {} < {}, delete from blacklist", perMinLogNum, limitThreshold);
                    sendRecoverMessage(appkey, perMinLogNum, limitThreshold);
                }
            }
        }
    }

    /**
     * 每秒日志超过1w, 将appkey放入map中, value=10, 每分钟-1, 10分钟后删除
     * 避免日志量过大频繁通知业务，未恢复10分钟通知一次
     */
    private void scanSecErrorLogCount() {
        Iterator<Map.Entry<String, AtomicInteger>> iterator = secLogNumOverMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, AtomicInteger> secLogNumOverItem = iterator.next();
            if (secLogNumOverItem.getValue().intValue() <= 0) {
                iterator.remove();
            } else {
                secLogNumOverItem.getValue().decrementAndGet();
            }
        }
    }

    private Integer getBlackListThreshold(String appkey) {
        int defaultCount = 12000;
        Integer threshold;
        if (perMinlimitedThreshodMap.containsKey(appkey)) {
            threshold = perMinlimitedThreshodMap.get(appkey);
        } else {
            threshold = perMinlimitedThreshodMap.get(DEFAULT_LIMITED_KEY);
        }
        return threshold == null ? defaultCount : threshold;
    }

    private void initConfig() {
        initPerMinLimitedThreshold();
        topologyConfigClient.addListener(MINUTE_LIMITED_THRESHOLD_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed, new value={}", key, newValue);
                initPerMinLimitedThreshold();
            }
        });

        initPerSecLimitedThreshold(false);
        topologyConfigClient.addListener(SECONDS_LIMITED_THRESHOLD_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed, new value={}", key, newValue);
                initPerSecLimitedThreshold(true);
            }
        });

        initFixedAppkeyBlackList();
        topologyConfigClient.addListener(APPKEY_FIXED_BLACKLIST_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed, new value={}", key, newValue);
                initFixedAppkeyBlackList();
            }
        });

        initDynamicAppkeyBlackList();
        topologyConfigClient.addListener(APPKEY_DYNAMIC_BLACKLIST_CFG, new IConfigChangeListener() {
            @Override
            public void changed(String key, String oldValue, String newValue) {
                LOGGER.info("{} value changed, new value={}", key, newValue);
                initDynamicAppkeyBlackList();
            }
        });
    }

    private void initPerMinLimitedThreshold() {
        String limitThresholdStr = topologyConfigClient.getValue(MINUTE_LIMITED_THRESHOLD_CFG);
        perMinlimitedThreshodMap = new ConcurrentHashMap<>();
        if (StringUtils.isBlank(limitThresholdStr)) {
            return;
        }
        genMinLimitThresholdMap(limitThresholdStr.replaceAll("\\s+", ""));
    }

    private void initFixedAppkeyBlackList() {
        String blackListStr = topologyConfigClient.getValue(APPKEY_FIXED_BLACKLIST_CFG);
        if (StringUtils.isBlank(blackListStr)) {
            appkeyFixedBlackSet = Collections.EMPTY_SET;
            return;
        }
        appkeyFixedBlackSet = new ConcurrentHashSet<>(Arrays.asList(blackListStr.replaceAll("\\s+", "").split(",")));
    }

    private void initDynamicAppkeyBlackList() {
        String blackListStr = topologyConfigClient.getValue(APPKEY_DYNAMIC_BLACKLIST_CFG);
        if (StringUtils.isBlank(blackListStr)) {
            appkeyDynamicBlackSet = Collections.EMPTY_SET;
            return;
        }
        appkeyDynamicBlackSet = new ConcurrentHashSet<>(Arrays.asList(blackListStr.replaceAll("\\s+", "").split(",")));
    }

    private void initPerSecLimitedThreshold(boolean isValueChangeTrigger) {
        String limitThresholdStr = topologyConfigClient.getValue(SECONDS_LIMITED_THRESHOLD_CFG);
        if (StringUtils.isBlank(limitThresholdStr)) {
            return;
        }
        try {
            Double perSecondsLimit = Double.valueOf(limitThresholdStr);
            boolean isChanged = false;
            if (!DEFAULT_PERMITS_PER_SECOND.equals(perSecondsLimit) && perSecondsLimit >= 200) {
                DEFAULT_PERMITS_PER_SECOND = perSecondsLimit;
                isChanged = true;
            }
            synchronized (BlackListService.class) {
                if (isValueChangeTrigger && isChanged) {
                    int oldLimiterListSize = limiterList.size();
                    limiterList.clear();
                    int rateLimiterCount = INIT_LIMITER_COUNT > rateLimiterMap.size() ? INIT_LIMITER_COUNT : rateLimiterMap.size();
                    for (int i = 0; i < rateLimiterCount; i++) {
                        RateLimiter limiter = RateLimiter.create(DEFAULT_PERMITS_PER_SECOND);
                        limiterList.add(limiter);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        LOGGER.error("Init RateLimiter list, sleep fail.", e);
                    }
                    LOGGER.info("Seconds threshold change. Clear limiterList item, size={}, newListSize={}", oldLimiterListSize, rateLimiterCount);

                    Iterator<Map.Entry<String, RateLimiter>> iterator = rateLimiterMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, RateLimiter> entry = iterator.next();
                        if (limiterList.isEmpty()) {
                            iterator.remove();
                        } else {
                            entry.setValue(limiterList.remove(0));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("{} config is not right, check if is num type", SECONDS_LIMITED_THRESHOLD_CFG);
        }
    }

    private void genMinLimitThresholdMap(String limitThresholdStr) {
        String[] limitCfgs = limitThresholdStr.split(";");

        for (String limitCfg : limitCfgs) {
            String[] limitCfgPair = limitCfg.split(":");
            if (limitCfgPair.length < 2) {
                LOGGER.error("{} config is not right", MINUTE_LIMITED_THRESHOLD_CFG);
                continue;
            }
            String appkey = limitCfgPair[0];
            try {
                Integer threshold = Integer.valueOf(limitCfgPair[1]);
                perMinlimitedThreshodMap.put(appkey, threshold);
            } catch (Exception e) {
                LOGGER.warn("{} config is not right, check if is num type", MINUTE_LIMITED_THRESHOLD_CFG);
                continue;
            }
        }
    }

    public boolean addDynamicBlacklist(String appkey) {
        String appkeyLimitedConfig = topologyConfigClient.getValue(APPKEY_DYNAMIC_BLACKLIST_CFG);
        appkeyLimitedConfig = appkeyLimitedConfig == null ? "" : appkeyLimitedConfig;
        if (!appkeyLimitedConfig.contains(appkey)) {
            String newValue;
            if (StringUtils.isBlank(appkeyLimitedConfig)) {
                newValue = appkey;
            } else {
                newValue = appkeyLimitedConfig + "," + appkey;
            }
            topologyConfigClient.setValue(APPKEY_DYNAMIC_BLACKLIST_CFG, newValue);
            return true;
        }
        return false;
    }

    public boolean deleteDynamicBlacklist(String appkey) {
        String appkeyBlacklistConfig = topologyConfigClient.getValue(APPKEY_DYNAMIC_BLACKLIST_CFG);
        if (appkeyBlacklistConfig != null && appkeyBlacklistConfig.contains(appkey)) {
            Set appkeyBlackSet = new ConcurrentHashSet<>(Arrays.asList(appkeyBlacklistConfig.replaceAll("\\s+", "").split(",")));
            if (appkeyBlackSet.remove(appkey)) {
                topologyConfigClient.setValue(APPKEY_DYNAMIC_BLACKLIST_CFG, setToString(appkeyBlackSet));
                return true;
            }
        }
        return false;
    }

    private String setToString(Set appkeyBlackSet) {
        if (appkeyBlackSet == null || appkeyBlackSet.isEmpty()) {
            return "";
        } else {
            String setString = appkeyBlackSet.toString();
            return setString.substring(1, setString.length() - 1);
        }
    }

    private static void sendLimitMessage(String appkey, int count, int thresholdCount) {
        String subject = "OCTO异常日志监控提醒";
        String message = "服务标识:" + ENV + appkey +
                "\n提醒内容:每分钟异常日志量(" + count + ")超出阈值(" + thresholdCount + "), 异常统计功能使用已被限制，低于阈值或10分钟后自动恢复。请及时检查服务状态，降低异常数量" +
                "[查看异常详情|" + MSGP_LINK + "/log/report?appkey=" + appkey + " ]，[处理方法|https://123.sankuai.com/km/page/28363495]";
        Messager.sendAlarmByAppkey(appkey, subject, message);
    }

    /**
     * 恢复通知
     *
     * @param appkey
     * @param count
     * @param thresholdCount
     */
    private void sendRecoverMessage(String appkey, int count, int thresholdCount) {
        String message = "服务标识:" + ENV  + appkey +
                "\n提醒内容:每分钟异常日志量(" + count + ")已低于阈值(" + thresholdCount + "), 异常统计功能恢复。";
        Messager.sendXMAlarmByAppkey(appkey, message);
    }

    public void sendLogDiscardMessage(String appkey, int secondsThreshold) {
        String message = "服务标识:" + ENV + appkey +
                "\n提醒内容:异常日志量过大, 每秒钟日志量超过" + secondsThreshold + ", 异常统计服务将忽略部分日志, 日志数少于" +
                secondsThreshold + "/s后将自动恢复。请及时检查服务状态，降低异常数量" +
                "[查看异常详情|" + MSGP_LINK + "/log/report?appkey=" + appkey +
                " ]，[处理方法|https://123.sankuai.com/km/page/28363495]";
        Messager.sendXMAlarmByAppkey(appkey, message);
    }
}
