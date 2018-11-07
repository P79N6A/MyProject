package com.sankuai.octo.msgp.serivce.sgagent

import com.sankuai.octo.msgp.dao.appkey.AppkeyProviderDao
import com.sankuai.octo.msgp.dao.appkey.AppkeyProviderDao.IpPortName
import com.sankuai.octo.msgp.model.Appkeys


/**
 * Created by lhmily on 07/15/2016.
 */
object SgAgentCommon {


  def getSGAgentIPAndPort(envId: Int, keyword: String) = {
    val nodes = AppkeyProviderDao.provdiers(Appkeys.sgagent.toString, envId)
    nodes.filter(searchFilter(_, keyword)).sortBy(_.name)
  }

  private def searchFilter(x: IpPortName, keyword: String) = {
    keyword.split(" ").filter(_ != "").forall(self => x.ip.contains(self) || x.name.contains(self))
  }
}
