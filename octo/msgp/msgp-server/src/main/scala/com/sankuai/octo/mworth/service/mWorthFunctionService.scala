package com.sankuai.octo.mworth.service

import com.sankuai.octo.mworth.dao.{worthFunction, worthConfig}
import com.sankuai.octo.mworth.db.Tables.{WorthFunctionRow, WorthConfigRow}

import scala.collection.concurrent.TrieMap

/**
 * Created by zava on 15/12/2.
 * 服务持有对象
 */
object mWorthFunctionService {
  private val functionMap = TrieMap[String, WorthFunctionRow]()

  def get(key:String) ={
    if(functionMap.size ==0){
      init();
    }
    functionMap.get(key);
  }
  //用完清空
  def clear(): Unit ={
    functionMap.clear()
  }

  private def init(): Unit ={
    //初始化config
    val functions = worthFunction.query(None, None, None)
    functions.foreach {
      x =>
        val key = s"${x.project}|${x.model}|${x.functionDesc}"
        functionMap.put(key, x)
    }
  }

}
