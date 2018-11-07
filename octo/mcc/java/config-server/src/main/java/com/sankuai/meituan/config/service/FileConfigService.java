package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.model.EmbededNode;
import com.sankuai.meituan.config.model.PathStat;
import com.sankuai.meituan.config.model.PropertyValue;
import com.sankuai.meituan.config.util.CronScheduler;
import com.sankuai.meituan.config.util.NodeNameUtil;
import com.sankuai.octo.config.model.ConfigGroup;
import com.sankuai.octo.config.model.ConfigGroups;
import org.apache.zookeeper.data.Stat;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.taobao.tair3.client.util.SerializableUtil;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by lhmily on 03/24/2016.
 */
@Component
public class FileConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(FileConfigService.class);
    @Resource
    ConfigNodeService nodeService;

    @Resource
    ConfigTairClient tairClient;

    private List<String> envs = new ArrayList<>();

    public FileConfigService() {
        envs.add("prod");
        envs.add("stage");
        envs.add("test");
    }

    private void handleFileStatistcs(String appkey, Map<String, String> result) {
        long createTime = 0;
        Boolean isHasFile = false;
        for (String env : envs) {
            ConfigGroups groups = tairClient.getGroups(env, appkey);
            if (null == groups) {
                continue;
            }
            List<ConfigGroup> groupList = groups.getGroups();

            for (ConfigGroup group : groupList) {
                if (group.getId().equals("0")) {
                    createTime = group.createTime;
                }
                List<String> fileNames = tairClient.getFilenameList(env, appkey, group.getId());
                if (!fileNames.isEmpty()) {
                    isHasFile = true;
                    break;
                }
            }
            if (isHasFile) {
                break;
            }
        }
        if (isHasFile) {
            result.put(appkey, Long.toString(createTime));
        }
    }

    private boolean isHashEnvs(List<EmbededNode> childNodes) {
        int envCount = 0;
        for (EmbededNode cnode : childNodes) {
            if (envs.contains(cnode.getName())) {
                ++envCount;
            }
        }
        return 3 == envCount;
    }

    private String getMCCcheckKey(boolean isDynamic) {
        return isDynamic ? "MCCStatisticDataDynamic" : "MCCStatisticDataFile";
    }

    public Map<String, String> getStatistcsCfg(boolean isDynamic) {
        byte[] dataByte = tairClient.get(getMCCcheckKey(isDynamic));
        if (null == dataByte || dataByte.length <= 0) {
            generateMCCStatisticData(isDynamic);
            dataByte = tairClient.get(getMCCcheckKey(isDynamic));
        }
        try {
            return SerializableUtil.deserializeMap(dataByte, HashMap.class, String.class, String.class);
        } catch (Exception e) {
            LOG.error("Failed to deserialize data from tair.", e);
            return Collections.emptyMap();
        }
    }

    private Map<String, String> genStatistcsCfg(boolean isDynamic) {
        Map<String, String> result = new HashMap<>();
        List<EmbededNode> appkeys = nodeService.getChildNodes("");
        for (EmbededNode node : appkeys) {
            String appkey = node.getName();
            if (!isHashEnvs(nodeService.getChildNodes(appkey))) {
                continue;
            }
            if (isDynamic) {
                handleDynamicStatistcs(appkey, result);
            } else {
                handleFileStatistcs(appkey, result);
            }
        }
        return result;
    }

    private void handleDynamicStatistcs(String appkey, Map<String, String> result) {
        boolean hashCfg = false;
        Stat statusTemp = new Stat();
        for (String env : envs) {
            String spacePath = NodeNameUtil.getSpacePath(appkey, appkey + "/" + env);
            List<PropertyValue> data = nodeService.getData(spacePath, statusTemp);
            if (!data.isEmpty()) {
                hashCfg = true;
                break;
            }
        }
        if (hashCfg) {
            PathStat status = nodeService.getPathStat(NodeNameUtil.getSpacePath(appkey, appkey));
            result.put(appkey, Long.toString(status.getNodeCreateTime()));
        }

    }

    public static void initMCCStatistics() {
        JobDetail job = JobBuilder.newJob(DailyStatisticJob.class).build();
        //每天凌晨三点执行一次
        Trigger trigger = TriggerBuilder.newTrigger().startNow()
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0)).build();
        CronScheduler.scheduleJob(job, trigger);
    }

    @DisallowConcurrentExecution
    class DailyStatisticJob implements Job {
        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            generateMCCStatisticData(true);
            generateMCCStatisticData(false);
        }

    }

    private void generateMCCStatisticData(boolean isDynamic) {
        String key = getMCCcheckKey(isDynamic);
        Map<String, String> data = genStatistcsCfg(isDynamic);
        try {
            tairClient.put(key, SerializableUtil.serializeMap(data));
        } catch (Exception e) {
            LOG.error("Failed to save statisticData into tair.", e);
        }
    }

}
