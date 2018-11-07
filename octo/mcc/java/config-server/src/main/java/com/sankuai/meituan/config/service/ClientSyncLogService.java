package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.domain.ClientSyncLog;
import com.sankuai.meituan.config.domain.ClientSyncLogExample;
import com.sankuai.meituan.config.mapper.ClientSyncLogMapper;
import com.sankuai.meituan.config.util.HttpUtil;
import com.sankuai.meituan.config.util.TaskUtil;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-9
 */
@Deprecated
@Component
public class ClientSyncLogService {
    private static final Logger LOG = LoggerFactory.getLogger(ClientSyncLogService.class);

    @Resource
    private ClientSyncLogMapper clientSyncLogMapper;

    @Scheduled(cron = "0 0 3 * * *")
    private void flushOldLog() {
        TaskUtil.singletonExecute(new Runnable() {
            @Override
            public void run() {
                try {
                    Date aWeekBefore = DateUtils.addDays(new Date(), -7);
                    ClientSyncLogExample example = new ClientSyncLogExample();
                    example.createCriteria().andSyncTimeLessThan(aWeekBefore);
                    clientSyncLogMapper.deleteByExample(example);
                } catch (Exception e) {
                    LOG.warn("fail to execute task", e);
                }

            }
        });
    }

    public void insertOrUpdate(String node, String ip, Integer pid, Long version) {
        if (isSyncLogUsable()) {
            try {
                ClientSyncLog log = get(node, ip, pid);
                if (log == null) {
                    log = new ClientSyncLog();
                    log.setNode(node);
                    log.setIp(ip);
                    log.setPid(pid);
                    log.setVersion(version);
                    log.setSyncTime(new Date());
                    log.setHost(HttpUtil.ipToHost(ip));
                    clientSyncLogMapper.insert(log);
                } else {
                    log.setVersion(version);
                    log.setSyncTime(new Date());
                    clientSyncLogMapper.updateByPrimaryKey(log);
                }
            } catch (Exception e) {
                // 防止数据库不可用影响接口可用性
                LOG.error(MessageFormatter.format("insertOrUpdate ClientSyncLog failed: {} {} {} {}", new Object[]{node, ip, pid, version}).getMessage(), e);
            }
        }
    }

    private boolean isSyncLogUsable() {
        return true;
    }

    public ClientSyncLog get(String node, String ip, Integer pid) {
        if (node == null || ip == null || pid == null) {
            return null;
        }
        ClientSyncLogExample example = new ClientSyncLogExample();
        example.or().andNodeEqualTo(node)
                .andIpEqualTo(ip)
                .andPidEqualTo(pid);
        List<ClientSyncLog> logs = clientSyncLogMapper.selectByExample(example);
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        return logs.get(0);
    }

    public List<ClientSyncLog> getLikeNode(String node) {
        if (node == null) {
            return new ArrayList<>();
        }
        ClientSyncLogExample example = new ClientSyncLogExample();
        example.or().andNodeEqualTo(node);
        example.or().andNodeLike(node + ".%");
        example.setOrderByClause("sync_time desc");
        List<ClientSyncLog> logs = clientSyncLogMapper.selectByExample(example);
        // 根据ip+pid去重
        return logs;
    }
}
