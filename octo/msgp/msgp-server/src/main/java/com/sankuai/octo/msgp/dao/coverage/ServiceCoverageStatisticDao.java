package com.sankuai.octo.msgp.dao.coverage;

import com.sankuai.octo.msgp.domain.coverage.ServiceCoverageOutlineDay;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

@Repository
public interface ServiceCoverageStatisticDao {

    int genOneDayStatisticData(String date);

    int deleteOneDaySvcCoverageData(String date);

    int countOneDaySvcCoverageData(String date);

    List<ServiceCoverageOutlineDay> getOutlineDatesListBgIsAll(@Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("cmptVal") String cmptVal, @Param("baseInt") Integer baseInt);

    List<ServiceCoverageOutlineDay> getOutlineDatesListOwtIsAll(HashMap<String, Object> params);

    List<ServiceCoverageOutlineDay> getOutlineDatesList(@Param("owt") String owt, @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("cmptVal") String cmptVal, @Param("baseInt") int baseInt);

}
