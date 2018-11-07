package com.sankuai.octo.errorlog.dao;

import com.sankuai.octo.errorlog.model.ErrorLogFilter;
import com.sankuai.octo.errorlog.model.ErrorLogFilterExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ErrorLogFilterDao {

    int insert(ErrorLogFilter record);

    int insertSelective(ErrorLogFilter record);

    List<ErrorLogFilter> selectByExample(ErrorLogFilterExample example);

    ErrorLogFilter selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ErrorLogFilter record);
}