package com.sankuai.octo.msgp.service.coverage;

import com.sankuai.msgp.common.utils.DateTimeUtil;
import com.sankuai.octo.msgp.config.TaskHost;
import com.sankuai.octo.msgp.dao.coverage.ServiceCoverageStatisticDao;
import com.sankuai.octo.msgp.serivce.service.ServiceCommon;
import com.sankuai.octo.msgp.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 服务覆盖率，统计service
 *
 * Created by emma on 2017/6/15.
 */
@Service
public class ComponentCoverageStatisticService {
    private Logger logger = LoggerFactory.getLogger(ComponentCoverageStatisticService.class);

    @Resource
    private ServiceCoverageStatisticDao svcCoverageStatisticDao;
    @Resource
    private ComponentCoverageCollectionService collectionService;

    /**
     * 每天8点30分定时生成服务覆盖率数据
     */
    @Scheduled(cron = "0 30 8 * * ?")
    public void genServiceCoverageData() {
        if (!TaskHost.isTaskHost()) {
            // 不是任务执行主机
            return;
        }
        try {
            // 1. 服务覆盖元数据
            Result collectionResult = collectionService.genAppkeyServiceData();
            if (!collectionResult.getIsSuccess()) {
                String msg = "每日服务覆盖率元数据生成失败: " + collectionResult.getMsg();
                logger.error(msg);
                ServiceCommon.sendInfoToAdmin(msg);
            }
            // 2. 服务覆盖统计结果数据
            int newDataRows = genServiceCoverageStatisticData();
            if (newDataRows <= 0) {
                String msg = "每日服务覆盖率统计结果为0, 请检查是否有元数据";
                logger.error(msg);
                ServiceCommon.sendInfoToAdmin(msg);
            }
        } catch (Exception e) {
            String msg = "每日服务覆盖率数据生成异常: " + e.getMessage();
            logger.error(msg, e);
            ServiceCommon.sendInfoToAdmin(msg);
        }
    }

    /**
     * 每天9点检查服务覆盖率数据是否生成
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkServiceCoverageStatisticData() {
        if (!TaskHost.isTaskHost()) {
            // 不是任务执行主机
            return;
        }
        String date = DateTimeUtil.getYesterday();
        // 1. 检查服务覆盖元数据
        if (!collectionService.hasOneDayAppkeySvcData(date)) {
            String msg = "每日服务覆盖元数据未生成, 手动调用接口:http://" + TaskHost.getTaskHost() + ":8910/svccover/generate";
            ServiceCommon.sendInfoToAdmin(msg);
            return;
        }
        // 2. 服务覆盖统计结果数据
        if (!hasOneDayAppkeySvcData(date)) {
            String msg = "每日服务覆盖统计结果数据未生成, 手动调用接口:http://" + TaskHost.getTaskHost() + ":8910/svccover/generate?onlyStatistics=true";
            ServiceCommon.sendInfoToAdmin(msg);
            return;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int genServiceCoverageStatisticData() {
        String date = DateTimeUtil.getYesterday();
        if (hasOneDayAppkeySvcData(date)) {
            logger.warn("service_coverage_statistic has data of {}", date);
            int deleteCount = svcCoverageStatisticDao.deleteOneDaySvcCoverageData(date);
        }
        int insertRows = svcCoverageStatisticDao.genOneDayStatisticData(date);
        logger.info("每日服务覆盖率统计结果生成成功, {} 新增{}条数据", date, insertRows);
        return insertRows;
    }

    private boolean hasOneDayAppkeySvcData(String yesterday) {
        int yesterdayDataNum = svcCoverageStatisticDao.countOneDaySvcCoverageData(yesterday);
        if (yesterdayDataNum > 0) {
            return true;
        } else {
            return false;
        }
    }
}
