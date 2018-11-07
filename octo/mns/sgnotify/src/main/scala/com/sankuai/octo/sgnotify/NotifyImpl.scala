package com.sankuai.octo.sgnotify

import com.sankuai.octo.config.model.{ConfigFileRequest, ConfigFileResponse, file_param_t}
import com.sankuai.octo.sgnotify.comm.FileConfigCmdType
import com.sankuai.octo.sgnotify.model.Constants
import com.sankuai.sgagent.thrift.model.SGAgent
import org.apache.thrift.TException
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.{TFramedTransport, TSocket, TTransportException}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import com.sankuai.meituan.config.listener.IConfigChangeListener

object NotifyImpl {
  private final val LOG: Logger = LoggerFactory.getLogger(NotifyImpl.getClass)

  val DEFAULT_PROD_AGENTS = config.get("prod.agents", "10.4.241.165,10.4.241.166,10.4.241.125,10.4.246.240")
  val DEFAULT_STAGE_AGENTS = config.get("stage.agents", "10.4.244.156,10.4.245.250")
  val DEFAULT_TEST_AGENTS = config.get("test.agents", "10.4.245.248,10.4.245.249")

  var prodAgents = DEFAULT_PROD_AGENTS.split(",").toList
  var stageAgents = DEFAULT_STAGE_AGENTS.split(",").toList
  var testAgents = DEFAULT_TEST_AGENTS.split(",").toList

  val TIMEOUT = 1500

  config.instance.addListener("prod.agents", new IConfigChangeListener {
    def changed(key: String, oldValue: String, newValue: String) = {
      LOG.info(s"change $key $oldValue $newValue")
      prodAgents = newValue.split(",").toList
    }
  })

  config.instance.addListener("stage.agents", new IConfigChangeListener {
    def changed(key: String, oldValue: String, newValue: String) = {
      LOG.info(s"change $key $oldValue $newValue")
      stageAgents = newValue.split(",").toList
    }
  })

  config.instance.addListener("test.agents", new IConfigChangeListener {
    def changed(key: String, oldValue: String, newValue: String) = {
      LOG.info(s"change $key $oldValue $newValue")
      testAgents = newValue.split(",").toList
    }
  })


  /**
    * 兼容线下的新老云主机问题
    * 如果是线下，需要将使用默认的agent主机替换掉老云主机的IPs
    *
    * @param ips
    * @param envDesc
    */
  private def handleIPs(ips: List[String], envDesc: String): List[String] = {
    if (!common.isOffline) return ips
    //处理线下新老云主机问题
    val newHosts = ips.filter(_.startsWith("10."))
    val ret = if (ips.size > newHosts.size) {
      //包含了老云主机，需要替换
      val oldHosts = envDesc match {
        case "prod" => prodAgents
        case "stage" => stageAgents
        case "test" => testAgents
        case _ => throw new IllegalArgumentException(s"env参数错误:$envDesc")
      }
      newHosts ++ oldHosts
    } else {
      ips
    }
    ret.distinct
  }

  def distributeOrEnableFileConfig(request: ConfigFileRequest, tag: FileConfigCmdType) = {
    try {
      val start = System.currentTimeMillis()
      val hosts = handleIPs(request.hosts.asScala.toList, request.files.env)
      val result = hosts.map(x => (x, notifyAgentFileConfig(x, request.getFiles, tag)))
      var failResult = result.filter(Constants.CODE_SUCCESS != _._2)
      val failList = failResult.map(_._1).toList
      val end = System.currentTimeMillis()
      LOG.info(s"${tag} fileConfig cost ${end - start} result $result")
      val code = if (failList.isEmpty) 200 else 500
      val codes = failResult.map(x => (x._1 , new Integer(x._2))).toMap.asJava
      val ret = new ConfigFileResponse(code)
      ret.setHosts(failList.asJava)
      ret.setCodes(codes)
      ret
    } catch {
      case ex: Exception =>
        LOG.error(ex.getMessage, ex)
        val ret = new ConfigFileResponse(500)
        ret.setHosts(List[String]().asJava)
        ret.setCodes(Map[String, Integer]().asJava)
        ret
    }


  }


  def notifyAgentFileConfig(ip: String, files: file_param_t, tag: FileConfigCmdType): Int = {
    var transport: TFramedTransport = null
    var ret = Constants.CODE_SUCCESS
    try {
      transport = new TFramedTransport(new TSocket(ip, 5266, TIMEOUT), 16384000)
      val protocol = new TBinaryProtocol(transport)
      transport.open()
      val client = new SGAgent.Client(protocol)
      val code = if (FileConfigCmdType.DISTRIBUTE == tag) client.notifyFileConfigIssued(files) else client.notifyFileConfigWork(files)

      code match {
        case Constants.CODE_SUCCESS => ret = Constants.CODE_SUCCESS
        case _: Int =>
          LOG.error(s"${tag} file ${files} to ${ip} failed ${code}")
          ret = code
      }
    } catch {
      case e: TTransportException =>
        LOG.error(s"TTransportException ${tag} connect agent ${ip} failed ${e}")
        ret = Constants.CODE_AGENT_CONNECT_ERROR
      case e: TException =>
        LOG.error(s"TException ${tag} file ${files} to ${ip} failed ${e}")
        ret = Constants.CODE_AGENT_INVOKE_ERROR
      case e: Throwable =>
        LOG.error(s"Throwable ${tag} file ${files} to ${ip} failed ${e}")
        ret = Constants.CODE_SGNOTIFY_INTERNAL_ERROR
    } finally {
      closeConnection(transport, ip)
    }
    ret
  }

  private def closeConnection(conn: TFramedTransport, ip: String) = {
    if (null != conn) {
      try {
        conn.close
      } catch {
        case e: Exception => LOG.error(s"sg_agent connection closed failure : IP(${ip}) \n ${e}")
      }
    }
  }
}