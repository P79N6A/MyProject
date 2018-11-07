package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.ConfigAdmin;
import com.sankuai.meituan.config.domain.ConfigAdminExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ConfigAdminMapper {
    int countByExample(ConfigAdminExample example);

    int deleteByExample(ConfigAdminExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ConfigAdmin record);

    int insertSelective(ConfigAdmin record);

    List<ConfigAdmin> selectByExampleWithRowbounds(ConfigAdminExample example, RowBounds rowBounds);

    List<ConfigAdmin> selectByExample(ConfigAdminExample example);

    ConfigAdmin selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ConfigAdmin record, @Param("example") ConfigAdminExample example);

    int updateByExample(@Param("record") ConfigAdmin record, @Param("example") ConfigAdminExample example);

    int updateByPrimaryKeySelective(ConfigAdmin record);

    int updateByPrimaryKey(ConfigAdmin record);
}