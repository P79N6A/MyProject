package com.sankuai.octo.msgp.dao;

import com.sankuai.octo.msgp.model.AppkeyDesc;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by emma on 2017/6/8.
 */
@Repository
public interface AppkeyDescDao {

    List<AppkeyDesc> getMtthriftService();

    List<AppkeyDesc> getMnsService();

    List<AppkeyDesc> getAppkeyOwt();

    List<AppkeyDesc> getSvcDegrageAppkeyOwt();
}
