package com.sankuai.octo.mworth.service


import com.sankuai.msgp.common.model.Page
import com.sankuai.octo.mworth.dao.{worthConfig, worthEvent, worthFunction, worthValue}
import com.sankuai.octo.mworth.db.Tables.WorthConfigRow
import com.sankuai.octo.mworth.model.MWorthConfig
import com.sankuai.octo.mworth.utils.AsyncProcessor

import scala.collection.concurrent.TrieMap

/**
 * Created by zava on 15/12/2.
 * 配置持有对象
 */
object mWorthConfigService {
  private val configMap = TrieMap[String, WorthConfigRow]()

  private val asyncCounttProcessor = AsyncProcessor(1, processCount)

  def get(key: String) = {
    if (configMap.size == 0) {
      init();
    }
    configMap.get(key);
  }

  def save(config: MWorthConfig) = {
    val value = worthConfig.save(config);
    if (config.isCoverd) {
      asyncCounttProcessor.put(config)
    }
    value
  }

  //配置是对指定时间的任务生效
  private def processCount(config: MWorthConfig) {
    val page = new Page(1, 1000);
    val wFunctions = worthFunction.get(config.getFunctionId)
    //TODO check null
    val wFunction = wFunctions(0)
    worthValue.delete(Some(wFunction.project), Some(wFunction.model), Some(wFunction.functionName), Some(config.getTargetAppkey), config.getFromTime, config.getToTime)

    val list = worthEvent.search(Some(wFunction.project), Some(wFunction.model), Some(wFunction.functionName), Some(config.getTargetAppkey), config.getFromTime, config.getToTime, page)
    //计算
    worthCountService.count(list, config)
    (2 to page.getTotalPageCount).foreach {
      i =>
        page.setPageNo(i)
        worthEvent.search(Some(wFunction.project), Some(wFunction.model), Some(wFunction.functionName), Some(config.getTargetAppkey), config.getFromTime, config.getToTime, page)
        worthCountService.count(list, config)
    }

  }

  def clear(): Unit = {
    configMap.clear()
  }

  private def init(): Unit = {
    //初始化config
    val configs = worthConfig.query(None, None, Some(true))
    configs.foreach {
      x =>
        val key = x.targetAppkey match {
          case Some(value) =>
            s"${x.functionId}|$value"
          case None =>
            s"${x.functionId}"
        }
        configMap.put(key, x)
    }
  }

}
