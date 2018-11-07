package com.sankuai.octo.errorlog.dao;


import com.sankuai.octo.errorlog.model.ErrorLogServiceStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface ErrorLogServiceStatusDao {

    ErrorLogServiceStatus getAppkeyStatus(String appkey);

    int updateAppkeyStatus(ErrorLogServiceStatus serviceStatus);

    List<String> getStartAppkeys(String day);

    List<String> getAllStartAppkey();
}
