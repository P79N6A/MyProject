package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.constant.UserStatus;
import com.sankuai.meituan.config.domain.ConfigAdmin;
import com.sankuai.meituan.config.domain.ConfigAdminExample;
import com.sankuai.meituan.config.mapper.ConfigAdminMapper;
import com.sankuai.meituan.filter.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-6
 */
@Component
public class ConfigAdminService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigAdminService.class);

    @Resource
    private ConfigAdminMapper configAdminMapper;


    public Set<Integer> getConfigAdminIds() {
        List<ConfigAdmin> configAdmins = getConfigAdmins();
        Set<Integer> configAdminIds = new HashSet<>();
        for (ConfigAdmin configAdmin : configAdmins) {
            configAdminIds.add(configAdmin.getUserId());
        }
        return configAdminIds;
    }

    public List<ConfigAdmin> getConfigAdmins() {
        ConfigAdminExample example = new ConfigAdminExample();
        example.or().andStatusEqualTo(UserStatus.ACTIVE.getIndex());
        return configAdminMapper.selectByExample(example);
    }

    public ConfigAdmin getByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }
        ConfigAdminExample example = new ConfigAdminExample();
        example.or().andStatusEqualTo(UserStatus.ACTIVE.getIndex())
                .andUserIdEqualTo(userId);
        List<ConfigAdmin> configAdmins = configAdminMapper.selectByExample(example);
        if (configAdmins == null || configAdmins.isEmpty()) {
            return null;
        }
        return configAdmins.get(0);
    }

    public void insert(Integer userId) {
        if (userId == null) {
            return;
        }
        ConfigAdmin configAdmin = getByUserId(userId);
        if (configAdmin != null) {
            LOG.warn("user({}) is already admin", userId);
            return;
        }
        configAdmin = new ConfigAdmin();
        configAdmin.setUserId(userId);
        configAdmin.setOperatorId(UserUtils.getUser().getId());
        configAdmin.setStatus(UserStatus.ACTIVE.getIndex());
        Date now = new Date();
        configAdmin.setCreateTime(now);
        configAdmin.setUpdateTime(now);

        configAdminMapper.insert(configAdmin);
    }

    public void delete(Integer userId) {
        if (userId == null) {
            return;
        }
        ConfigAdmin configAdmin = getByUserId(userId);
        if (configAdmin == null) {
            LOG.warn("user({}) is not admin", userId);
            return;
        }
        configAdmin.setOperatorId(UserUtils.getUser().getId());
        configAdmin.setStatus(UserStatus.DELETE.getIndex());
        configAdmin.setUpdateTime(new Date());

        configAdminMapper.updateByPrimaryKey(configAdmin);
    }
}
