package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.SpaceAdmin;
import com.sankuai.meituan.config.domain.SpaceAdminExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface SpaceAdminMapper {
    int countByExample(SpaceAdminExample example);

    int deleteByExample(SpaceAdminExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SpaceAdmin record);

    int insertSelective(SpaceAdmin record);

    List<SpaceAdmin> selectByExampleWithRowbounds(SpaceAdminExample example, RowBounds rowBounds);

    List<SpaceAdmin> selectByExample(SpaceAdminExample example);

    SpaceAdmin selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SpaceAdmin record, @Param("example") SpaceAdminExample example);

    int updateByExample(@Param("record") SpaceAdmin record, @Param("example") SpaceAdminExample example);

    int updateByPrimaryKeySelective(SpaceAdmin record);

    int updateByPrimaryKey(SpaceAdmin record);
}