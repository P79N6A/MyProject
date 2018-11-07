package com.sankuai.octo

import com.sankuai.octo.msgp.serivce.overload.OverloadDegrade
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by dreamblossom on 15/10/9.
 */
class DelOldDegradeActionInZKSpec extends FlatSpec with Matchers{
  print(OverloadDegrade.removeDegradeNode(3, "com.sankuai.chenxi.test_provider_a", "Class.X.method2"))
}
