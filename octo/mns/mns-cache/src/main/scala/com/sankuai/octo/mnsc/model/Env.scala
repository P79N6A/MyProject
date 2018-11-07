package com.sankuai.octo.mnsc.model

import com.sankuai.inf.octo.mns.util.ProcessInfoUtil
import org.apache.commons.lang.StringUtils

object Env extends Enumeration {
  type Env = Value
  val test = Value(1)
  val stage = Value(2)
  val prod = Value(3)

  def isValid(env: String) = {
    if (StringUtils.isEmpty(env)) {
      false
    } else {
      if (ProcessInfoUtil.isLocalHostOnline) {
        onlineEnv.contains(env)
      } else {
        offlineEnv.contains(env)
      }
    }
  }

  def strConvertEnum(env:String)={
   val envEnum= if (ProcessInfoUtil.isLocalHostOnline){
      onlineEnv.get(env)
    }else{
      offlineEnv.get(env)
    }
    envEnum.get
  }

  private val offlineEnv = Map(
    "ppe" -> stage,
    "dev" -> prod,
    "test" -> test,
    "prod" -> prod,
    "stage" -> stage,
    "beta" -> stage,
    "1" -> test,
    "2" -> stage,
    "3" -> prod
  )
  private val onlineEnv = Map(
    "prod" -> prod,
    "staging" -> stage,
    "stage" -> stage,
    "test" -> test,
    "1" -> test,
    "2" -> stage,
    "3" -> prod
  )

  def isValid(env: Int) = values.map(_.id).contains(env)
}
