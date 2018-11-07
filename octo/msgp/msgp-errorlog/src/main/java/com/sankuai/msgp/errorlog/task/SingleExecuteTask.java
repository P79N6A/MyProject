package com.sankuai.msgp.errorlog.task;

import com.sankuai.meituan.config.MtConfigClient;
import com.sankuai.msgp.common.utils.DateTimeUtil;
import com.sankuai.msgp.common.utils.client.Messager;
import com.sankuai.msgp.errorlog.dao.CellServiceDao;
import com.sankuai.msgp.errorlog.dao.ErrorLogDayReportDao;
import com.sankuai.msgp.errorlog.service.ErrorLogRouteAdjustService;
import com.sankuai.msgp.errorlog.service.ErrorLogRouteCfgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.TimeZone;

/**
 * Created by emma on 2017/8/22.
 */
@Scope("singleton")
@Component
@Transactional
public class SingleExecuteTask {
    private final Logger LOGGER = LoggerFactory.getLogger(SingleExecuteTask.class);

    private static final String APPKEY_BLACKLIST = "appkey.blacklist.dynamic";
    @Autowired
    private ErrorLogTaskHost taskHost;
    @Autowired
    private MtConfigClient topologyConfigClient;
    @Autowired
    private ErrorLogDayReportDao errorLogDayReportDao;
    @Autowired
    private CellServiceDao cellServiceDao;
    @Autowired
    private ErrorLogRouteAdjustService errorLogRouteService;
    @Autowired
    private ErrorLogRouteCfgService routeCfgService;

    @Scheduled(cron = "0 5 0 * * ?")
    public void genErrorLogDailyReport() {
        if (!taskHost.isTaskHost()) {
            // 不是任务执行主机
            return;
        }
        String today = DateTimeUtil.getLastOneDayDate(0);
        String yesterday = DateTimeUtil.getYesterday();
        Long endSeconds = DateTimeUtil.getTimeInSecond(DateTimeUtil.DATE_DAY_FORMAT, today);
        Long startSeconds = DateTimeUtil.getTimeInSecond(DateTimeUtil.DATE_DAY_FORMAT, yesterday);
        if (errorLogDayReportDao.countOneDayData(yesterday) > 0) {
            LOGGER.warn("ErrorLog daily report has data of {}", yesterday);
            errorLogDayReportDao.deleteOneDayData(yesterday);
        }
        errorLogDayReportDao.genDailyReportData(yesterday, startSeconds, endSeconds);
        LOGGER.info("Gen new errorLog daily report of {}", yesterday);
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void checkErrorLogDailyReport() {
        if (!taskHost.isTaskHost()) {
            // 不是任务执行主机
            return;
        }
        String yesterday = DateTimeUtil.getYesterday();
        try {
            if (errorLogDayReportDao.countOneDayData(yesterday) == 0) {
                genErrorLogDailyReport();
                if (errorLogDayReportDao.countOneDayData(yesterday) == 0) {
                    Messager.sendXMAlarmToErrorLogAdmin("[异常日志]昨日统计数据生成结果为0, 请检查问题原因");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Errorlog daily report generate fail", e);
            String errorMsg = e.getClass().getName() + ": " + e.getMessage();
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]昨日统计数据生成异常，" + errorMsg);
        }
    }

    /**
     * 10分钟清理一次动态黑名单appkey
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    public void clearBlackList() {
        if (!taskHost.isTaskHost()) {
            // 不是任务执行主机
            return;
        }
        topologyConfigClient.setValue(APPKEY_BLACKLIST, "");
        LOGGER.info("Clear errorlog dynamic blacklist.");
    }

    @Scheduled(cron = "0 0 5 1/3 * ?")
    public void dynamicAdjustAppkeyRoute() {
        try {
            if (!taskHost.isTaskHost()) {
                // 不是任务执行主机
                return;
            }
            boolean isAdjusted = errorLogRouteService.dynamicAdjustAppkeyRoute(routeCfgService.getNodeAppkeyRouteMap(), false);
            if (isAdjusted) {
                Messager.sendXMAlarmToErrorLogAdmin("[异常日志]Appkey路由重新生成");
            }
            LOGGER.info("Appkey route dynamicAdjustAppkeyRoute execute, isAdjusted={}", isAdjusted);
        } catch (Exception e) {
            LOGGER.error("Appkey route adjust fail.", e);
            Messager.sendXMAlarmToErrorLogAdmin("[异常日志]Appkey路由生成失败: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0/3 * * * ?")
    public void updateCellAppkey() {
        try {
            if (!taskHost.isTaskHost()) {
                // 不是任务执行主机
                return;
            }
            long currentTimestamp = System.currentTimeMillis();
            long todayZero = currentTimestamp / (1000 * 3600 * 24) * (3600 * 24) - TimeZone.getDefault().getRawOffset() / 1000;
            int newDataCount = cellServiceDao.updateCellService(todayZero);
            LOGGER.info("Cell appkey updated, add {}", newDataCount);
        } catch (Exception e) {
            LOGGER.error("Cell appkey updated failed.", e);
        }
    }
}
