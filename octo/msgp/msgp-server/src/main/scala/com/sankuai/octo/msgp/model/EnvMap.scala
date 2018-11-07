package com.sankuai.octo.msgp.model

import com.sankuai.msgp.common.utils.helper.CommonHelper
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory


object EnvMap extends Enumeration {
  private val LOG = LoggerFactory.getLogger(EnvMap.getClass)

  type EnvMap = Value
  val PROD = Value("prod")
  val STAGING = Value("staging")
  val DEV = Value("dev")
  val PPE = Value("ppe")
  val TEST = Value("test")


  val onlineEnvMap = Map(
    "prod" -> "prod",
    "stage" -> "staging",
    "test" -> "test"
  )


  val offlineEnvMap = Map(
    "prod" -> "dev",
    "stage" -> "ppe",
    "test" -> "test"
  )

  /**
    * check the env is valid.
    * online only supports prod/staging (ignore case)
    * offline only supports dev/ppe/test (ignore case)
    *
    * @param envStr prod/staging/dev/ppe/test (ignore case)
    * @return true or false
    */
  def isValid(envStr: String): Boolean = {

    // check the env is prod/staging/dev/ppe/test or not (ignore case)
    val isEnvStrValid = values.foldLeft(false) { (result, item) => result || StringUtils.equalsIgnoreCase(envStr, item.toString) }
    if (isEnvStrValid) {
      val envType = withName(envStr.toLowerCase())
      // check online or offline
      if (CommonHelper.isOffline) {
        PROD != envType && STAGING != envType
      } else {
        DEV != envType && PPE != envType && TEST != envType
      }

    } else {
      false
    }

  }

  def envConvertoZkEnvStr(env: EnvMap.Value) = env match {
    case PROD => "prod"
    case STAGING => "stage"
    case DEV => "prod"
    case PPE => "stage"
    case TEST => "test"
    case _ =>
      LOG.warn("invalid EnvMap.Value, use default env prod")
      "prod"
  }

  def envConvertoEnvInt(env: EnvMap.Value) = env match {
    case PROD => 3
    case STAGING => 2
    case DEV => 3
    case PPE => 2
    case TEST => 1
    case _ =>
      LOG.warn("invalid EnvMap.Value, use default env 3")
      3
  }


  def getAliasEnv(env: String) = {
    if (CommonHelper.isOffline) {
      offlineEnvMap.get(env).getOrElse("dev ")
    } else {
      onlineEnvMap.get(env).getOrElse("prod")
    }
  }


  def getAliasEnv(envInt: Int) =
    if (CommonHelper.isOffline) {
      envInt match {
        case 1 => "test"
        case 2 => "ppe"
        case 3 => "dev"
      }
    } else {
      envInt match {
        case 1 => "test"
        case 2 => "staging"
        case 3 => "prod"
      }

    }

}
