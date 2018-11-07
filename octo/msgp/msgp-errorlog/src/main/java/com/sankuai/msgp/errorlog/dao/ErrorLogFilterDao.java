package com.sankuai.msgp.errorlog.dao;

import com.sankuai.msgp.errorlog.pojo.ErrorLogFilter;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogFilterDao {

    List<ErrorLogFilter> selectBy(@Param("appkey")String appkey,@Param("status")Integer status,@Param("enabled")Boolean enabled);

}