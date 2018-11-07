package com.sankuai.msgp.common.service.org

import com.sankuai.msgp.common.model.{Base, Business, Pdl}
import com.sankuai.msgp.common.service.org.OpsService.TreeData
import com.sankuai.msgp.common.utils.{ExecutionContextFactory, StringUtil}

import scala.collection.JavaConverters._
import scala.concurrent.Future


/**
  * Created by zava on 16/2/19.
  * bp ,giant,fe,平台事业群
  * adp 广告平台
  * dataapp :平台事业群
  *
  * sre api http://ops.sankuai.com/api/dashboard/owts
  * ,new Pdl("mobile") ,new Pdl("gct"), new Pdl("mpt"),
  */
object BusinessOwtService {
  val businessMap = Map(
    0 -> List(new Pdl("meishi"), new Pdl("web"), new Pdl("srq")),
    1 -> List(new Pdl("cloud"), new Pdl("cloudoffice"), new Pdl("cloudprivate"), new Pdl("cloudpublic"), new Pdl("tair"), new Pdl("sre"), new Pdl("dba"), new Pdl("inf"), new Pdl("ee"), new Pdl("rc"), new Pdl("sec"), new Pdl("mx"), new Pdl("ep"), new Pdl("data"), new Pdl("databus")),
    2 -> List(new Pdl("movie")),
    3 -> List(), //创新业务部
    4 -> List(new Pdl("icb"), new Pdl("hotel"), new Pdl("trip"), new Pdl("travel"), new Pdl("tower"),
      new Pdl("tp"), new Pdl("pt"), new Pdl("flight"), new Pdl("hbdata"), new Pdl("ia"), new Pdl("oversea"), new Pdl("train"), new Pdl("gty")),
    5 -> List(new Pdl("waimai"), new Pdl("banma"), new Pdl("shangchao"), new Pdl("xianguo"), new Pdl("wmarch"), new Pdl("retail")),
    6 -> List(), //云计算部
    7 -> List(new Pdl("pay"), new Pdl("zc"), new Pdl("fd"), new Pdl("promo")),
    8 -> List(), //支付平台部
    9 -> List(), //智能餐厅部
    10 -> List(new Pdl("xm"), new Pdl("it"), new Pdl("mit")),
    11 -> List(new Pdl("adp")),
    12 -> List(new Pdl("fe"), new Pdl("dataapp"), new Pdl("giant"), new Pdl("bp"), new Pdl("wpt"), new Pdl("recsys"), new Pdl("nlpml")),
    13 -> List(new Pdl("gct")),
    14 -> List(new Pdl("sjst"), new Pdl("canyinrc"), new Pdl("bb")),
    100 -> List()
  )
  val owtMap = scala.collection.mutable.Map[String, Int]()
  implicit val ec = ExecutionContextFactory.build(2)

  /**
   * 被废弃，只提供数字
   * @param owt
   * @return
   */
  @Deprecated
  def getBusiness(owt: String) = {
    if (owtMap.isEmpty) {
      businessMap.toList.foreach {
        business =>
          val buss = business._1
          business._2.foreach {
            pdl =>
              owtMap += (pdl.getOwt -> buss)
          }
      }
    }
    owtMap.getOrElse(owt, 100)
  }

  def getAllBusiness = {
    OpsService.businessGroup
  }

  /**
    * 从ops获取数据,自己不单独维护
    * @param base beijing or shanghai
    * @param owt 业务线
    * @return business
    */
  def getBusiness(base: String = "meituan", owt: String) = {
    val key = s"$base.$owt"
    val businessOpt  = if(StringUtil.isBlank(base)) {
      val meituanOpt = OpsService.owtBusiness.get(s"${Base.meituan.toString}.$owt")
      val dianpingOpt = OpsService.owtBusiness.get(s"${Base.dianping.toString}.$owt")
      if(meituanOpt.nonEmpty){
        meituanOpt.get.business_group
      }else if(dianpingOpt.nonEmpty){
        dianpingOpt.get.business_group
      }else{
        Some(Business.other.toString)
      }
    }else{
      val data = OpsService.owtBusiness.getOrElse(key, TreeData(business_group = Some(Business.other.toString)))
      data.business_group
    }
    businessOpt.getOrElse("")
  }

  /**
    * 从ops获得business下的owt
    * 比上面维护的更为准确
    *
    * @param business 事业群
    * @param base     归属地
    * @return
    */
  def getOwtList(base: String, business: String) = {
    val allOwts = OpsService.owtBusiness.filter {
      x =>
        x._2.business_group.getOrElse("").equalsIgnoreCase(business)
    }.keys.map {
      x =>
        val baseOwt = x.split("\\.")
        if (baseOwt.length == 2) {
          (baseOwt.apply(0), baseOwt.apply(1))
        } else {
          ("", "")
        }
    }.filter(x => StringUtil.isNotBlank(x._1))

    /** waring: toString is a function and functions cannot be used for pattern matching. */
    val meituan = Base.meituan.toString
    val dianping = Base.dianping.toString
    base match {
      case `meituan` => allOwts.filter(x => x._1.equalsIgnoreCase(Base.meituan.toString)).map(_._2).toList
      case `dianping` => allOwts.filter(x => x._1.equalsIgnoreCase(Base.dianping.toString)).map(_._2).toList
      case _ => allOwts.map(_._2).toList
    }
  }

  def getOwtList(business: Int) = {
    if (businessMap.contains(business)) {
      businessMap.apply(business).map(_.getOwt)
    } else {
      List[String]()
    }
  }

  def getPdlList(owt: String) = {
    OpsService.pdlList(owt).asScala.toList.map(_.getPdl).filter(_.nonEmpty)
  }

  def refresh = {
    Future {
      OpsService.refreshOwt
      OpsService.refreshPdls()
    }
  }
}
