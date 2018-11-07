package com.sankuai.logparser.service;

import com.sankuai.logparser.util.DefaultThreadFactory;
import com.sankuai.msgp.common.dao.appkey.AppkeyAliasDao;
import com.sankuai.msgp.common.dao.appkey.AppkeyDescDao;
import com.sankuai.msgp.common.utils.client.Messager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.JavaConversions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by emma on 2017/8/18.
 */
public class AppkeyInfoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppkeyInfoService.class);

    private static Set<String> allAppkeys = new HashSet<>();
    private static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    public static Set<String> getAllAppkey() {
        rwLock.readLock().lock();
        Set<String> appkeys = new HashSet<>();
        try {
            appkeys.addAll(allAppkeys);
        } finally {
            rwLock.readLock().unlock();
        }
        return appkeys;
    }

    static {
        ScheduledExecutorService obtainAppkeyInfo = new ScheduledThreadPoolExecutor(1, new DefaultThreadFactory("ObtainAppkeyInfo", true));
        obtainAppkeyInfo.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    updateAppkeyInfo();
                } catch (Exception e) {
                    LOGGER.error("updateAppkeyInfo failed", e);
                }
            }
        }, 0L, 10 * 60, TimeUnit.SECONDS); // 十分钟更新一次
    }

    private static void updateAppkeyInfo() {
        rwLock.writeLock().lock();
        try {
            List<String> appkeyList = JavaConversions.asJavaList(AppkeyDescDao.getAllAppkey());
            List<String> appkeyAliasList = JavaConversions.asJavaList(AppkeyAliasDao.getAllAppkeyAlias());
            if (appkeyList != null && !appkeyList.isEmpty()) {
                allAppkeys.clear();
                allAppkeys.addAll(appkeyList);
                allAppkeys.addAll(appkeyAliasList);
                LOGGER.info("UpdateAppkeyInfo success appkey and alias size={}", allAppkeys.size());
            } else {
                LOGGER.error("UpdateAppkeyInfo appkeyList is empty");
                Messager.sendXMAlarmToErrorLogAdmin("[异常日志]Topology获取Appkey列表失败结果是空, 检查当前服务");
            }
        } catch (Exception e) {
            LOGGER.error("UpdateAppkeyInfo fail", e);
            String errorMsg = e.getClass().getName() + ": " + e.getMessage();
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]Topology获取Appkey列表失败, 请检查Topology消息生成是否正常, 异常信息是" + errorMsg);
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
