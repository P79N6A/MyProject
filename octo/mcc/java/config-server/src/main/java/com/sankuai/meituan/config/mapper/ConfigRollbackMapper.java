package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.ConfigRollback;
import com.sankuai.meituan.config.domain.ConfigRollbackExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRollbackMapper {
    int countByExample(ConfigRollbackExample example);

    int deleteByExample(ConfigRollbackExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ConfigRollback record);

    int insertSelective(ConfigRollback record);

    List<ConfigRollback> selectByExampleWithBLOBsWithRowbounds(ConfigRollbackExample example, RowBounds rowBounds);

    List<ConfigRollback> selectByExampleWithBLOBs(ConfigRollbackExample example);

    List<ConfigRollback> selectByExampleWithRowbounds(ConfigRollbackExample example, RowBounds rowBounds);

    List<ConfigRollback> selectByExample(ConfigRollbackExample example);

    ConfigRollback selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ConfigRollback record, @Param("example") ConfigRollbackExample example);

    int updateByExampleWithBLOBs(@Param("record") ConfigRollback record, @Param("example") ConfigRollbackExample example);

    int updateByExample(@Param("record") ConfigRollback record, @Param("example") ConfigRollbackExample example);

    int updateByPrimaryKeySelective(ConfigRollback record);

    int updateByPrimaryKeyWithBLOBs(ConfigRollback record);

    int updateByPrimaryKey(ConfigRollback record);
}