package com.sankuai.msgp.common.utils.helper

import com.sankuai.msgp.common.utils.StringUtil

/**
  * Created by yves on 16/9/27.
  */
object SqlParser {

  case class ValueExpress(value: String, express: String = "=")

  def sqlParser(prefixSQL: String, parameterMap: Map[String, ValueExpress], suffixSQL: String): String = {
    val sqlConditionBuilder = new StringBuilder
    sqlConditionBuilder.append(prefixSQL)
    parameterMap.keys.toList.foreach {
      parameter =>
        val valueExp = parameterMap(parameter)
        if (StringUtil.isBlank(valueExp.value) || "null".equals(valueExp.value) || parameter.equals("null")) {
          sqlConditionBuilder.append("")
        } else {
          sqlConditionBuilder.append(s" AND $parameter ${valueExp.express} '${valueExp.value}'")
        }
    }
    sqlConditionBuilder.append(suffixSQL)
    //TODO do some secure checking before return. eg. SQL Injection
    sqlConditionBuilder.toString()
  }
}
