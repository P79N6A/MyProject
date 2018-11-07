package com.sankuai.octo.msgp.serivce.hlb

import com.alibaba.fastjson.{JSON, JSONObject}
import com.sankuai.msgp.common.model.Path
import com.sankuai.octo.msgp.utils.client.ZkClient
import com.sankuai.octo.msgp.utils.{Result, ResultData}
import org.apache.commons.lang3.StringUtils

/**
  * Created by emma on 2017/5/25.
  */
object HlbService {

  def getHttpServerPort(env: String, appkey: String): ResultData[java.util.Map[String, String]] = {
    val result = new ResultData[java.util.Map[String, String]]()
    if (StringUtils.isBlank(env) || StringUtils.isBlank(appkey)) {
      result.failure("param is empty")
    } else {
      val srvPortName = "server_port"
      val path = s"/mns/sankuai/$env/$appkey/${Path.sharedHttpConfig}"
      val httpConfigRet = getHttpConfig(env, appkey, path)
      if (!httpConfigRet.isSuccess) {
        result.failure(httpConfigRet.getMsg)
      } else {
        val propertiesJson = httpConfigRet.getData()
        if (!propertiesJson.containsKey(srvPortName)) {
          result.failure(s"zk path=$path no $srvPortName")
        } else {
          val srvPort = propertiesJson.getString(srvPortName)
          val srvPortMap = new java.util.HashMap[String, String]
          srvPortMap.put(srvPortName, srvPort)
          result.success(srvPortMap)
        }
      }
    }
  }

  def setHttpServerPort(env: String, appkey: String, svrPort: String): Result = {
    val setResult = new Result()
    if (StringUtils.isBlank(env) || StringUtils.isBlank(appkey) || StringUtils.isBlank(svrPort)) {
      setResult.failure("param is empty")
    } else {
      val srvPortName = "server_port"
      val path = s"/mns/sankuai/$env/$appkey/${Path.sharedHttpConfig}"
      val httpConfigRet = getHttpConfig(env, appkey, path)
      if (!httpConfigRet.isSuccess) {
        setResult.failure(httpConfigRet.getMsg)
      } else {
        val propertiesJson = httpConfigRet.getData()
        propertiesJson.put(srvPortName, svrPort)

        ZkClient.setData(path, propertiesJson.toJSONString)
        setResult.success()
      }
    }
  }

  def delHttpServerPort(env: String, appkey: String): Result = {
    val delResult = new Result()
    if (StringUtils.isBlank(env) || StringUtils.isBlank(appkey)) {
      delResult.failure("param is empty")
    } else {
      val srvPortName = "server_port"
      val path = s"/mns/sankuai/$env/$appkey/${Path.sharedHttpConfig}"
      val httpConfigRet = getHttpConfig(env, appkey, path)
      if (!httpConfigRet.isSuccess) {
        delResult.failure(httpConfigRet.getMsg)
      } else {
        val propertiesJson = httpConfigRet.getData()
        if (!propertiesJson.containsKey(srvPortName)) {
          delResult.failure(s"zk path=$path no $srvPortName")
        } else {
          propertiesJson.remove(srvPortName)
          ZkClient.setData(path, propertiesJson.toJSONString)
          delResult.success()
        }
      }
    }
  }

  private def getHttpConfig(env: String, appkey: String, path: String): ResultData[JSONObject] = {
    val result = new ResultData[JSONObject]()
    if (!ZkClient.exist(path)) {
      result.failure(s"zk path=$path does not existed")
    } else {
      val propertiesStr = ZkClient.getData(path)
      if (StringUtils.isBlank(propertiesStr)) {
        result.failure(s"zk path=$path no data")
      } else {
        result.success(JSON.parseObject(propertiesStr))
      }
    }
  }
}
