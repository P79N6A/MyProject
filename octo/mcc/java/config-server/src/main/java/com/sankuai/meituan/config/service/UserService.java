package com.sankuai.meituan.config.service;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sankuai.meituan.config.constant.ParamName;
import com.sankuai.meituan.config.model.ConfigSpace;
import com.sankuai.meituan.config.model.UserBean;
import com.sankuai.meituan.filter.util.User;
import com.sankuai.meituan.org.remote.service.RemoteEmployeeService;
import com.sankuai.meituan.org.remote.service.RemoteOrgTreeService;
import com.sankuai.meituan.org.remote.vo.EmployeeInfo;
import com.sankuai.meituan.org.remote.vo.OrgTreeNodeVo;
import com.sankuai.meituan.org.remote.vo.builder.SearchBuilder;
import com.sankuai.meituan.org.remote.vo.search.SearchCondition;
import com.sankuai.meituan.org.remote.vo.search.SearchType;
import org.apache.commons.collections.CollectionUtils;
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
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    @Resource
    private ConfigAdminService configAdminService;
    @Resource
    private SpaceAdminService spaceAdminService;
    @Resource
    private ZookeeperService zookeeperService;
    @Resource
    private RemoteEmployeeService remoteEmployeeService;
    @Resource
    private RemoteOrgTreeService remoteOrgTreeService;

    public static final User defaultAdminUser = new User();

    static {
        defaultAdminUser.setId(1);
        defaultAdminUser.setLogin("mcc");
        defaultAdminUser.setName("mccadmin");
        List<String> roles = new ArrayList<String>();
        roles.add("user");
        defaultAdminUser.setRoles(roles);
    }

    public Boolean isConfigAdmin(Integer userId) {
        if (userId == null) {
            return Boolean.FALSE;
        }
        Set<Integer> configAdminIds = configAdminService.getConfigAdminIds();
        configAdminIds.add(1);
        if (configAdminIds.contains(userId)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }


    public Boolean isSpaceAdmin(String spaceName, Integer userId) {
        if (StringUtils.isBlank(spaceName) || userId == null) {
            return Boolean.FALSE;
        }
        Set<Integer> spaceAdminIds = spaceAdminService.getSpaceAdminIds(spaceName);

        if (spaceAdminIds.contains(userId)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public List<ConfigSpace> getConfigSpaces(Integer userId) {
        Set<String> spaces = getSpaces(userId);
        spaces.add("sample");       // 所有人都有sample空间权限
        List<ConfigSpace> configSpaces = new ArrayList<>(spaces.size());
        for (String space : spaces) {
            configSpaces.add(new ConfigSpace(space));
        }
        return configSpaces;
    }

    public Set<String> getSpaces(Integer userId) {
        if (userId == null) {
            return new HashSet<>();
        }
        if (isConfigAdmin(userId)) {
            return new HashSet<>(zookeeperService.getNodes(ParamName.CONFIG_BASE_PATH));
        }
        Set<String> spaces = spaceAdminService.getSpaces(userId);
        List<String> allSpaces = zookeeperService.getNodes(ParamName.CONFIG_BASE_PATH);
        Set<String> authSpaces = new HashSet<>();
        // 过滤，保证从数据库中取得space都是有效的
        for (String space : allSpaces) {
            if (spaces.contains(space)) {
                authSpaces.add(space);
            }
        }
        return authSpaces;
    }

    public List<UserBean> getConfigAdmins() {
        final Set<Integer> userIds = configAdminService.getConfigAdminIds();
        return getUsersByIds(userIds);
    }

    public String addConfigAdmin(Integer userId) {
        if (userId == null) {
            return "userId is null";
        }
        configAdminService.insert(userId);
        return null;
    }

    public String deleteConfigAdmin(Integer userId) {
        if (userId == null) {
            return "userId is null";
        }
        configAdminService.delete(userId);
        return null;
    }

    public List<UserBean> getSpaceAdmins(String spaceName) {
        Set<Integer> userIds = spaceAdminService.getSpaceAdminIds(spaceName);
        return getUsersByIds(userIds);
    }

    private List<UserBean> getUsersByIds(final Collection<Integer> ids) {
        Collection<EmployeeInfo> employeeInfos = Lists.newArrayList();
        try{
            Collection<EmployeeInfo> employeeInfosRemote = remoteEmployeeService.getEmployeeList(Lists.newArrayList(ids));
            if(null != employeeInfosRemote){
                employeeInfos.addAll(employeeInfosRemote);
            }
        }catch (Exception e){
            LOG.error("cannot get mtorg info.", e);
        }

        List<UserBean> userBeans = Lists.newArrayList(Iterables.transform(employeeInfos, new Function<EmployeeInfo, UserBean>() {
            @Override
            public UserBean apply(EmployeeInfo input) {
                ids.remove(input.getId());
                return new UserBean(input);
            }
        }));
        if (CollectionUtils.isNotEmpty(ids)) {
            LOG.warn("({}) are not user,please check admin and space admin", ids);
        }
        return userBeans;
    }

    public String addSpaceAdmin(String spaceName, Integer userId) {
        if (StringUtils.isBlank(spaceName)) {
            return "spaceName is blank";
        }
        if (userId == null) {
            return "userId is null";
        }
        spaceAdminService.insert(spaceName, userId);
        return null;
    }

    public String deleteSpaceAdmin(String spaceName, Integer userId) {
        if (StringUtils.isBlank(spaceName)) {
            return "spaceName is blank";
        }
        if (userId == null) {
            return "userId is null";
        }
        spaceAdminService.delete(spaceName, userId);
        return null;
    }

    public List<OrgTreeNodeVo> empListSearch(String keyWord) {
//        List<OrgTreeNodeVo> vos = remoteOrgTreeService.searchOnlineEmpListNodes(keyWord);  // 不包含虚拟账号，可以直接使用RemoteOrgTreeService封装好的方法
        SearchCondition searchCondition = SearchBuilder.create().searchName(keyWord)        // 包含虚拟账号，要自己写SearchCondition
                .searchType(SearchType.EMP)
                .includeVirtual()  // 包含虚拟账号
                .list().build();
        List<OrgTreeNodeVo> vos = new ArrayList<OrgTreeNodeVo>();
        try{
            List<OrgTreeNodeVo> remoteVos = remoteOrgTreeService.search(searchCondition);
            if(null != remoteVos){
                vos.addAll(remoteVos) ;
            }
        }catch (Exception e){
            LOG.error("cannot get mtorg info.", e);
        }
        withLogin(vos);
        return vos;
    }

    /**
     * 人员上加Login
     *
     * @param fromList
     * @return
     */
    private void withLogin(List<OrgTreeNodeVo> fromList) {
        for (OrgTreeNodeVo orgTreeNodeVo : fromList) {
            orgTreeNodeVo.setName(orgTreeNodeVo.getName() + "(" + orgTreeNodeVo.getEnName() + ")");
        }
    }
}
