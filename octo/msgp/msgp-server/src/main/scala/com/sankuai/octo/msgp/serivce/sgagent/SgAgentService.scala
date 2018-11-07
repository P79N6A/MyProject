package com.sankuai.octo.msgp.serivce.sgagent

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.Page
import com.sankuai.sgagent.thrift.model.{ProtocolRequest, SGAgent, SGService}
import org.apache.commons.lang3.StringUtils
import org.apache.thrift.protocol.{TBinaryProtocol, TProtocol}
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransport}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object SgAgentService {
  private val LOG: Logger = LoggerFactory.getLogger(SgAgentService.getClass)

  /**
   * get the serviceList
   *
   * @param appkey required
   * @param ip     required
   * @param appIP  not required
   * @param port   not required
   * @param page
   * @return subSet of the whole serviceList
   */
  def getServiceListByAppkeyIP(protocolStr: String, appkey: String, ip: String, appIP: String, port: String, page: Page) = {
    var transport: TTransport = null
    var result: List[SGService] = null
    try {
      val timeout = 3000
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open
      //get the whole serviceList
      val reqProtocolStr = if (StringUtils.isEmpty(protocolStr)) {
        "thrift"
      } else {
        protocolStr
      }
      val req = new ProtocolRequest()
      req.setLocalAppkey("com.sankuai.inf.msgp")
        .setRemoteAppkey(appkey)
        .setProtocol(reqProtocolStr)
      val response = agent.getServiceListByProtocol(req)
      if (null != response && response.getErrcode == 0 && null != response.getServicelist) {
        val serviceList = response.getServicelist.asScala
        //filter the serviceList according to appIP
        val result_appIP = if (null != appIP && appIP.length() > 0) serviceList.filter(_.ip.contains(appIP)) else serviceList
        //filter the serviceList according to port
        val result_port = if (null != port && port.length() > 0) result_appIP.filter(x => String.valueOf(x.port).contains(port)) else result_appIP

        page.setTotalCount(result_port.size)
        result = result_port.toList.sortBy(_.ip)
      }
    } catch {
      case e: Exception =>
        LOG.error(s"$ip ${e.getMessage}",e)
        result = null
    } finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"$ip fail ${e.getMessage}")
        }
      }
    }
    if (null == result) null else result.slice(page.getStart, page.getStart + page.getPageSize).asJava
  }

  def registerProvider(ip: String, reg_appkey: String, reg_ip: String, reg_port: Int) = {
    val sleep_time = MsgpConfig.get("check.sentinel.sleep", "3000").toLong
    val timeout = 3000
    var transport: TTransport = null
    try {
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open
      val service: SGService = new SGService
      service.setAppkey(reg_appkey).setPort(reg_port).setIp(reg_ip).setVersion("original").
        setLastUpdateTime((System.currentTimeMillis / 1000).toInt).setServerType(0).setWeight(10).
        setFweight(10.0).setProtocol("thrift").setExtend("OCTO|slowStartSeconds:180")
      agent.registService(service)
      Thread.sleep(sleep_time)
    }
    catch {
      case e: Exception =>
        throw e
    }
    finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"transport close error $ip ", e)
        }
      }
    }
  }

  def getServiceList(ip: String,localAppkey: String, remoteAppkey: String) = {
    val timeout = 3000
    var transport: TTransport = null
    try {
      transport = new TFramedTransport(new TSocket(ip, 5266, timeout), 16384000)
      val protocol: TProtocol = new TBinaryProtocol(transport)
      val agent = new SGAgent.Client(protocol)
      transport.open
      val req = new ProtocolRequest()
      req.setLocalAppkey(localAppkey)
        .setRemoteAppkey(remoteAppkey)
        .setProtocol("thrift")
      val response =  agent.getServiceListByProtocol(req)
      if (null != response && response.getErrcode == 0 && null != response.getServicelist) {
         response.getServicelist.asScala
      }else{
        List()
      }
    }
    catch {
      case e: Exception =>
        throw e
    }
    finally {
      if (null != transport) {
        try {
          transport.close()
        } catch {
          case e: Exception => LOG.error(s"transport close error $ip ", e)
        }
      }
    }
  }
}
