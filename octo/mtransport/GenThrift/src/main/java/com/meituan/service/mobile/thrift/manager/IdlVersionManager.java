package com.meituan.service.mobile.thrift.manager;

import com.meituan.service.mobile.thrift.dao.IIdlVersionDAO;
import com.meituan.service.mobile.thrift.domain.IdlVersionDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: gaosheng
 * Date: 15-3-6
 * Time: 下午4:05
 */
@Service
public class IdlVersionManager {

    @Autowired
    private IIdlVersionDAO iIdlVersionDAO;

    public int existAppKey(String appkey){
        return iIdlVersionDAO.existAppKey(appkey);
    }

    public void addIdlVersion(IdlVersionDO idlVersionDO){
        iIdlVersionDAO.addIdlVersion(idlVersionDO);
    }

    public int getVersionByAppkey(String appkey){
        return iIdlVersionDAO.getVersionByAppKey(appkey);
    }

    public String getFileByAppKeyAndVersion(String appkey, int version){
        return iIdlVersionDAO.getFileByAppKeyAndVersion(appkey, version);
    }
}
