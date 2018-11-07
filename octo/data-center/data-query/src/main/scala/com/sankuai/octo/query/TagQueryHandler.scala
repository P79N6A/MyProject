package com.sankuai.octo.query

import com.sankuai.octo.statistic.helper.{TagHelper, TimeProcessor}
import com.sankuai.octo.statistic.model.{QueryTag, StatEnv, StatSource}
import org.slf4j.LoggerFactory

object TagQueryHandler {
  private val logger = LoggerFactory.getLogger(TagQueryHandler.getClass)

  def tags(appkey: String, env: String, role: String, start: Int, end: Int) = {
    val envTransfer: StatEnv = StatEnv.getInstance(env)
    val sourceTransfer: StatSource = TagQueryHandler.sourceToStatSource(role)

    //    val tag = new QueryTag()
    //    tag.setAppkey(appkey)
    val oneDay = 24 * 60 * 60
    val startDay = TimeProcessor.getDayStart(start)
    val endDay = TimeProcessor.getDayStart(end)
    val count = (endDay - startDay) / oneDay
    /** 在查询tags将source归类 */
    val statSource = getSource(sourceTransfer)
    val tagList = (0 to count).par.flatMap { x =>
      val dayStart = startDay + x * oneDay
      TagHelper.getDailyTag(appkey, dayStart, envTransfer, statSource)
    }.seq.toSet
    val spannames = tagList.flatMap(tag => tag.spannames)
    val localHosts = tagList.flatMap(tag => tag.localHosts)
    val remoteAppKeys = tagList.flatMap(tag => tag.remoteAppKeys)
    val remoteHosts = tagList.flatMap(tag => tag.remoteHosts)
    QueryTag(appkey, spannames, localHosts, remoteAppKeys, remoteHosts)
  }

  def sourceToStatSource(source: String) = {
    source.toLowerCase match {
      case "server" => StatSource.Server
      case "client" => StatSource.Client
      case _ => StatSource.Server
    }
  }


  private def getSource(statSource: StatSource) = {
    statSource match {
      case StatSource.Client | StatSource.ClientDrop | StatSource.ClientSlow | StatSource.ClientFailure => StatSource.Client
      case StatSource.Server | StatSource.ServerDrop | StatSource.ServerSlow | StatSource.ServerFailure => StatSource.Server
      case StatSource.RemoteClient | StatSource.RemoteClientDrop | StatSource.RemoteClientSlow | StatSource.RemoteClientFailure => StatSource.RemoteClient
    }
  }


}
