package com.sankuai.octo.errorlog.service;

import com.sankuai.octo.errorlog.dao.CellServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (C) 2015 Meituan
 * All rights reserved
 * User: emma
 * Date: 2018/6/6
 */
@Service
public class CellAppkeyService {
    private static final Logger logger= LoggerFactory.getLogger(CellAppkeyService.class);
    @Autowired
    private CellServiceDao cellServiceDao;

    @Cacheable(cacheNames = "LRUCache-1m")
    public List<String> getCellAppkeys() {
        List<String> appkeys = new ArrayList<>();
        try {
            appkeys = cellServiceDao.getCellServices();
            logger.info("Errorlog getCellAppkeys");
        } catch (Exception e){
            logger.error("Errorlog getCellAppkeys failed.", e);
        }
        return appkeys;
    }
}
