package com.sankuai.meituan.config.service;

import com.sankuai.meituan.config.constant.UserStatus;
import com.sankuai.meituan.config.domain.SpaceAdmin;
import com.sankuai.meituan.config.domain.SpaceAdminExample;
import com.sankuai.meituan.config.mapper.SpaceAdminMapper;
import com.sankuai.meituan.filter.util.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author yangguo03
 * @version 1.0
 * @created 14-5-6
 */
@Component
public class SpaceAdminService {
    private static final Logger LOG = LoggerFactory.getLogger(SpaceAdminService.class);

    @Resource
    private SpaceAdminMapper spaceAdminMapper;

    public Set<String> getSpaces(Integer userId) {
        if (userId == null) {
            return new HashSet<>();
        }
        List<SpaceAdmin> spaceAdmins = getSpaceAdmins(userId);
        Set<String> spaces = new HashSet<>();
        for (SpaceAdmin spaceAdmin : spaceAdmins) {
            spaces.add(spaceAdmin.getSpaceName());
        }
        return spaces;
    }

    public Set<Integer> getSpaceAdminIds(String spaceName) {
        if (StringUtils.isBlank(spaceName)) {
            return new HashSet<>();
        }
        List<SpaceAdmin> spaceAdmins = getSpaceAdmins(spaceName);
        Set<Integer> spaceAdminIds = new HashSet<>();
        for (SpaceAdmin spaceAdmin : spaceAdmins) {
            spaceAdminIds.add(spaceAdmin.getUserId());
        }
        return spaceAdminIds;
    }

    public List<SpaceAdmin> getSpaceAdmins(Integer userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        SpaceAdminExample example = new SpaceAdminExample();
        example.or().andStatusEqualTo(UserStatus.ACTIVE.getIndex())
                .andUserIdEqualTo(userId);
        return spaceAdminMapper.selectByExample(example);
    }

    public List<SpaceAdmin> getSpaceAdmins(String spaceName) {
        if (StringUtils.isBlank(spaceName)) {
            return new ArrayList<>();
        }
        SpaceAdminExample example = new SpaceAdminExample();
        example.or().andStatusEqualTo(UserStatus.ACTIVE.getIndex())
                .andSpaceNameEqualTo(spaceName);
        return spaceAdminMapper.selectByExample(example);
    }

    public SpaceAdmin getBySpaceNameAndUserId(String spaceName, Integer userId) {
        if (StringUtils.isBlank(spaceName) || userId == null) {
            return null;
        }
        SpaceAdminExample example = new SpaceAdminExample();
        example.or().andStatusEqualTo(UserStatus.ACTIVE.getIndex())
                .andSpaceNameEqualTo(spaceName)
                .andUserIdEqualTo(userId);
        List<SpaceAdmin> spaceAdmins = spaceAdminMapper.selectByExample(example);
        if (spaceAdmins == null || spaceAdmins.isEmpty()) {
            return null;
        }
        return spaceAdmins.get(0);
    }

    public void insert(String spaceName, Integer userId) {
        if (StringUtils.isBlank(spaceName) || userId == null) {
            return;
        }
        SpaceAdmin spaceAdmin = getBySpaceNameAndUserId(spaceName, userId);
        if (spaceAdmin != null) {
            LOG.warn("user({}) is already admin of space({})", userId, spaceName);
            return;
        }
        spaceAdmin = new SpaceAdmin();
        spaceAdmin.setUserId(userId);
        spaceAdmin.setSpaceName(spaceName);
        spaceAdmin.setOperatorId(UserUtils.getUser().getId());
        spaceAdmin.setStatus(UserStatus.ACTIVE.getIndex());
        Date now = new Date();
        spaceAdmin.setCreateTime(now);
        spaceAdmin.setUpdateTime(now);

        spaceAdminMapper.insert(spaceAdmin);
    }

    public void delete(String spaceName, Integer userId) {
        if (StringUtils.isBlank(spaceName) || userId == null) {
            return;
        }
        SpaceAdmin spaceAdmin = getBySpaceNameAndUserId(spaceName, userId);
        if (spaceAdmin == null) {
            LOG.warn("user({}) is not admin of space({})", userId, spaceAdmin);
            return;
        }
        spaceAdmin.setOperatorId(UserUtils.getUser().getId());
        spaceAdmin.setStatus(UserStatus.DELETE.getIndex());
        spaceAdmin.setUpdateTime(new Date());

        spaceAdminMapper.updateByPrimaryKey(spaceAdmin);
    }

}
