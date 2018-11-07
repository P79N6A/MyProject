package com.sankuai.octo.statistic.helper

import com.sankuai.octo.statistic.model._
import org.springframework.beans.BeanUtils

object PerfHelper {

  def statDataToPerfData(statData: StatData) = {
    val perfData = new PerfData()
    //  复制属性,单独处理 source
    BeanUtils.copyProperties(statData, perfData, "source")

    val role = statData.getSource match {
      case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => PerfRole.SERVER
      case StatSource.Client | StatSource.ClientDrop | StatSource.ClientSlow | StatSource.ClientFailure => PerfRole.CLIENT
      case _ =>
        //  should not happen
        PerfRole.SERVER
    }
    val dataType = statData.getSource match {
      case StatSource.Server | StatSource.Client => PerfDataType.ALL
      case StatSource.ServerDrop | StatSource.ClientDrop => PerfDataType.DROP
      case StatSource.ServerSlow | StatSource.ClientSlow => PerfDataType.SLOW
      case StatSource.ServerFailure | StatSource.ClientFailure => PerfDataType.FAILURE
      case _ =>
        //  should not happen
        PerfDataType.ALL
    }
    perfData.setRole(role)
    perfData.setDataType(dataType)
    if (perfData.getPerfProtocolType == null) {
      perfData.setPerfProtocolType(PerfProtocolType.THRIFT)
    }
    perfData
  }
}
