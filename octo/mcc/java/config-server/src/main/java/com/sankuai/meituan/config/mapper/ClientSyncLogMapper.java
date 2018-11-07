package com.sankuai.meituan.config.mapper;

import com.sankuai.meituan.config.domain.ClientSyncLog;
import com.sankuai.meituan.config.domain.ClientSyncLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ClientSyncLogMapper {
    int countByExample(ClientSyncLogExample example);

    int deleteByExample(ClientSyncLogExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ClientSyncLog record);

    int insertSelective(ClientSyncLog record);

    List<ClientSyncLog> selectByExampleWithRowbounds(ClientSyncLogExample example, RowBounds rowBounds);

    List<ClientSyncLog> selectByExample(ClientSyncLogExample example);

    ClientSyncLog selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ClientSyncLog record, @Param("example") ClientSyncLogExample example);

    int updateByExample(@Param("record") ClientSyncLog record, @Param("example") ClientSyncLogExample example);

    int updateByPrimaryKeySelective(ClientSyncLog record);

    int updateByPrimaryKey(ClientSyncLog record);
}