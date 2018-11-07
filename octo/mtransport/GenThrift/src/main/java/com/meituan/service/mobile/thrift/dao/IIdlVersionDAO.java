package com.meituan.service.mobile.thrift.dao;

import com.meituan.service.mobile.thrift.domain.IdlVersionDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-3-6
 * Time: 下午3:12
 */
public interface IIdlVersionDAO {

    //根据appkey,查询idlversion表有无该appkey记录
    @Select("select count(*) from idlversion where appkey=#{appkey}")
    public int existAppKey(@Param("appkey")String appkey);

    //保存IDL版本更新信息
    @Insert("insert into idlversion(appkey, uid, version, file, remark, content, time) values(#{appkey}, #{uid}, #{version}, #{file}, #{remark}, #{content}, now())")
    public void addIdlVersion(IdlVersionDO idlVersionDO);

    //查询某appkey的最新版本
    @Select("select MAX(version) from idlversion where appkey=#{appkey}")
    public int getVersionByAppKey(String appkey);

    //根据appkey和version查询文件名
    @Select("select file from idlversion where appkey=#{appkey} and version=#{version}")
    public String getFileByAppKeyAndVersion(@Param("appkey")String appkey, @Param("version")int version);

}
