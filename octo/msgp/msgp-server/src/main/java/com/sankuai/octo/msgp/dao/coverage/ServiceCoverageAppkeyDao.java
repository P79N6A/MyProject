package com.sankuai.octo.msgp.dao.coverage;

import com.sankuai.octo.msgp.model.coverage.ServiceCoverageAppkey;
import com.sankuai.octo.msgp.domain.coverage.AppkeyComponentInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.HashMap;

@Repository
public interface ServiceCoverageAppkeyDao {

    Integer batchAddDayAppkeySvcCoverageData(List<ServiceCoverageAppkey> svcCoverageList);

    Integer addAppkeySvcCoverageData(ServiceCoverageAppkey svcCoverage);

    Integer deleteOneDayAppkeyServiceData(String date);

    Integer countOndDayAppkeyServiceData(String date);

    List<AppkeyComponentInfo> getDetailsListOwtIsAll(HashMap<String, Object> params);

    List<AppkeyComponentInfo> getDetailsList(@Param("owt") String owt, @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("cmptVal") String cmptVal, @Param("baseInt") int baseInt);

    List<AppkeyComponentInfo> getDetailsListBgIsAll(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("cmptVal") String cmptVal, @Param("baseInt") int baseInt);

}
