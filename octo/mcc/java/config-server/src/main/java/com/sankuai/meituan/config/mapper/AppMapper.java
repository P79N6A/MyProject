package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.App;
import com.sankuai.meituan.config.domain.AppExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface AppMapper {
    int countByExample(AppExample example);

    int deleteByExample(AppExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(App record);

    int insertSelective(App record);

    List<App> selectByExampleWithRowbounds(AppExample example, RowBounds rowBounds);

    List<App> selectByExample(AppExample example);

    App selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") App record, @Param("example") AppExample example);

    int updateByExample(@Param("record") App record, @Param("example") AppExample example);

    int updateByPrimaryKeySelective(App record);

    int updateByPrimaryKey(App record);
}