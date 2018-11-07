package com.sankuai.octo.msgp.utils.helper

import java.util
import java.util.Collections

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.Status
import com.sankuai.msgp.common.service.org.OpsService
import com.sankuai.octo.msgp.dao.realtime.RealtimeLogDao
import com.sankuai.octo.msgp.domain.HostIp
import com.sankuai.octo.msgp.serivce.service.AppkeyProviderService
import org.joda.time.DateTime
import org.springframework.util.StringUtils

import scala.collection.JavaConverters._


/**
  * Created by wujinwu on 16/5/11.
  */

object RealtimeHelper {

  def getHostIP(appkey: String, env: Int): util.List[HostIp] = {
    if (StringUtils.hasText(appkey)) {
      val providerNodes = AppkeyProviderService.getProviderNodes(appkey, env)
      providerNodes.filter(_.status == Status.ALIVE.id).map(_.ip).distinct.map { ip =>
        val hostname = OpsService.ipToHost(ip)
        new HostIp(hostname, ip)
      }.sortBy(_.getHostname).asJava
    } else {
      Collections.emptyList()
    }
  }

  def getLogPath(appkey: String) = {
    if (StringUtils.hasText(appkey)) {
      //  获取appkey的缓存日志路径
      val rowOption = RealtimeLogDao.get(appkey)
      if (rowOption.nonEmpty) {
        val logPath = rowOption.get.logPath
        //Replace the date as current date
        val dateRegExp = """[0-9]{4}[_-]?(((0[13578]|(10|12))[_-]?(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)[_-]?(0[1-9]|[1-2][0-9]|30)))$""".r
        val dateMatches = dateRegExp.findAllIn(logPath).toList
        if(dateMatches.length == 1){
          val date = dateMatches.head
          val sepRegExp = """\D""".r
          val sepMatches = sepRegExp.findFirstIn(date).toList
          val separator = if (sepMatches.isEmpty){""}else{sepMatches.head}
          val currentDate = getCurrentDate(separator)
          dateRegExp.replaceAllIn(logPath,currentDate)
        }else{
          logPath
        }

      } else {
        ""
      }
    } else {
      ""
    }
  }


  /**
    *
    * @param separator  日期分隔符
    * @return
    */
  def getCurrentDate(separator: String) ={
    val current = DateTime.now().toString("yyyy-MM-dd")
    current.replace("-",separator)
  }

  /**
    *
    * @param appkey 服务key
    *               true 放行,false 禁止
    */

  def isPass(appkey: String) = {
    val rt_appkeys: String = MsgpConfig.get("realtime_appkeys", "inf")
    val pass_appkeys = rt_appkeys.split(",").toSet
    pass_appkeys.exists(bu => appkey.contains(bu))
  }

}
