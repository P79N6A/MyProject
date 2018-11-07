package com.sankuai.octo.oswatch.model

/**
 * Created by dreamblossom on 15/9/30.
 */
object OctoEnv extends Enumeration {
  val test = "test"
  val stage = "stage"
  val prod = "prod"

  def getEnv(v: Int) = {
    v match {
      case 1 => test
      case 2 => stage
      case _ => prod
    }
  }
}
