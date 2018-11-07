package com.sankuai.octo.mworth.utils

import scala.collection.JavaConverters._
/**
 * Created by zava on 16/2/22.
 * 报表名字 head区域
 */
object report {
    val reportHeaderMap = Map(
        "org_owt"-> List("部门","业务线"),
        "org_user"-> List("部门","用户"),
        "org_model"-> List("部门","模块"),

        "owt"-> List("业务线","appkey"),
        "owt_model"-> List("业务线","功能"),

        "model"-> List("功能模块","子功能")
    )
    def getHeader(name:String)={
        reportHeaderMap.getOrElse(name,List("部门")).asJava
    }

}
