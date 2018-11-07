package com.sankuai.octo.errorlog.service;

import com.sankuai.msgp.common.utils.StringUtil;
import com.sankuai.msgp.common.utils.UserUtil;
import com.sankuai.octo.errorlog.constant.ErrorLogFilterStatus;
import com.sankuai.octo.errorlog.dao.ErrorLogFilterDao;
import com.sankuai.octo.errorlog.model.ErrorLogFilter;
import com.sankuai.octo.errorlog.model.ErrorLogFilterExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author yangguo03
 * @version 1.0
 * @created 13-11-26
 */
@Service
public class ErrorLogFilterService {
    private static final Logger logger = LoggerFactory.getLogger(ErrorLogFilterService.class);

    @Resource
    private ErrorLogFilterDao errorLogFilterDao;

    public Integer insert(ErrorLogFilter filter) {
        if (filter == null) {
            return null;
        }
        Date date = new Date();
        filter.setSortNum(date.getTime());
        filter.setCreateTime(date);
        filter.setUpdateTime(date);
        filter.setOperatorId(UserUtil.getCurrentUserId());
        filter.setStatus(ErrorLogFilterStatus.ACTIVE.getValue());
        errorLogFilterDao.insertSelective(filter);
        return filter.getId();
    }

    public Integer update(ErrorLogFilter filter, Boolean oldEnabled) {
        if (filter == null || filter.getId() == null) {
            return null;
        }
        Date date = new Date();
        if (!filter.getEnabled().equals(oldEnabled)) {
            filter.setSortNum(date.getTime());
        }
        filter.setUpdateTime(date);
        filter.setOperatorId(UserUtil.getCurrentUserId());
        errorLogFilterDao.updateByPrimaryKeySelective(filter);

        return filter.getId();
    }

    public Integer delete(Integer filterId) {
        if (filterId == null) {
            return null;
        }
        ErrorLogFilter filter = new ErrorLogFilter();
        filter.setId(filterId);
        filter.setUpdateTime(new Date());
        filter.setOperatorId(UserUtil.getCurrentUserId());
        filter.setStatus(ErrorLogFilterStatus.DELETE.getValue());
        errorLogFilterDao.updateByPrimaryKeySelective(filter);
        return filterId;
    }

    public Integer updateEnable(Integer filterId) {
        if (filterId == null) {
            return null;
        }
        Date date = new Date();
        ErrorLogFilter filter = new ErrorLogFilter();
        filter.setId(filterId);
        filter.setSortNum(date.getTime());
        filter.setEnabled(Boolean.TRUE);
        filter.setUpdateTime(date);
        filter.setOperatorId(UserUtil.getCurrentUserId());
        errorLogFilterDao.updateByPrimaryKeySelective(filter);
        return filterId;
    }

    public Integer updateDisable(Integer filterId) {
        if (filterId == null) {
            return null;
        }
        Date date = new Date();
        ErrorLogFilter filter = new ErrorLogFilter();
        filter.setId(filterId);
        filter.setSortNum(date.getTime());
        filter.setEnabled(Boolean.FALSE);
        filter.setUpdateTime(date);
        filter.setOperatorId(UserUtil.getCurrentUserId());
        errorLogFilterDao.updateByPrimaryKeySelective(filter);
        return filterId;
    }

    public List<ErrorLogFilter> selectByAppkey(String appkey) {
        if (StringUtil.isBlank(appkey)) {
            return Collections.emptyList();
        }
        ErrorLogFilterExample example = new ErrorLogFilterExample();
        example.or().andAppkeyEqualTo(appkey)
                .andStatusEqualTo(ErrorLogFilterStatus.ACTIVE.getValue());
        example.setOrderByClause("sort_num asc");
        return errorLogFilterDao.selectByExample(example);
    }

    public ErrorLogFilter selectByFilterId(Integer filterId) {
        if (filterId == null) {
            return null;
        }
        return errorLogFilterDao.selectByPrimaryKey(filterId);
    }

    public Boolean updateSort(List<Integer> filterIds) {
        if (filterIds == null || filterIds.isEmpty()) {
            return Boolean.TRUE;
        }
        Long index = 1L;
        for (Integer id : filterIds) {
            ErrorLogFilter filter = new ErrorLogFilter();
            filter.setId(id);
            filter.setSortNum(index);
            filter.setUpdateTime(new Date());
            filter.setOperatorId(UserUtil.getCurrentUserId());
            errorLogFilterDao.updateByPrimaryKeySelective(filter);

            index++;
        }
        return Boolean.TRUE;
    }
}
