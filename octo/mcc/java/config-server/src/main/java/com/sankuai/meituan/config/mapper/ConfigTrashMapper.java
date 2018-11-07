package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.ConfigTrash;
import com.sankuai.meituan.config.domain.ConfigTrashExample;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigTrashMapper {
    int countByExample(ConfigTrashExample example);

    int deleteByExample(ConfigTrashExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ConfigTrash record);

    int insertSelective(ConfigTrash record);

    List<ConfigTrash> selectByExampleWithBLOBsWithRowbounds(ConfigTrashExample example, RowBounds rowBounds);

    List<ConfigTrash> selectByExampleWithBLOBs(ConfigTrashExample example);

    List<ConfigTrash> selectByExampleWithRowbounds(ConfigTrashExample example, RowBounds rowBounds);

    List<ConfigTrash> selectByExample(ConfigTrashExample example);

    ConfigTrash selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ConfigTrash record, @Param("example") ConfigTrashExample example);

    int updateByExampleWithBLOBs(@Param("record") ConfigTrash record, @Param("example") ConfigTrashExample example);

    int updateByExample(@Param("record") ConfigTrash record, @Param("example") ConfigTrashExample example);

    int updateByPrimaryKeySelective(ConfigTrash record);

    int updateByPrimaryKeyWithBLOBs(ConfigTrash record);

    int updateByPrimaryKey(ConfigTrash record);
}