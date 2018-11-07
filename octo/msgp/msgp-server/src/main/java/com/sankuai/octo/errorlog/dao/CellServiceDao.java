package com.sankuai.octo.errorlog.dao;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public interface CellServiceDao {

    List<String> getCellServices();
}
