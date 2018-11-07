package com.sankuai.msgp.errorlog.dao;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CellServiceDao {

    int updateCellService(long time);

    List<String> getCellServices();
}
