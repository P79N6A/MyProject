package com.sankuai.octo.msgp.serivce.graph

import com.sankuai.msgp.common.model.{Business, ServiceModels}
import com.sankuai.msgp.common.service.org.BusinessOwtService

/**
 * 业务线与组织对应关系 http://ops.sankuai.com/buget/mapowt/
 */
object ViewDefine {
  private val WAIMAI = "waimai"

  object Graph extends Enumeration {
    type Graph = Value

    // 0-1000 waimai
    val waimai_group = Value(5, "外卖配送事业群服务视图")
    val waimai = Value(100, "外卖服务视图")
    val waimai_m = Value(110, "外卖M端服务视图")
    val waimai_c = Value(120, "外卖C端服务视图")
    val waimai_b = Value(130, "外卖B端服务视图")
    val banma = Value(200, "配送服务视图")

    // 1000-2000 到店
    val daodian_group = Value(1000, "到店事业群服务视图")
    val meishi = Value(1200, "餐饮服务视图")

    // 2000-3000 hotel
    val hotel_group = Value(2000, "酒店旅游事业群服务视图")
    val hotel = Value(2100, "酒店服务视图")

    val hotel_m = Value(2110, "酒店M端服务视图")

    val travel = Value(2200, "旅游服务视图")

    // 3000-4000 movie
    val movie_group = Value(3000, "猫眼电影服务视图")

    // 4000-5000 finance
    val finance_group = Value(4000, "金融发展部服务视图")
    val pay_group = Value(5000, "智能支付服务视图")
    val car_group = Value(6000, "打车服务视图")
    val sjst_m_group = Value(7000, "餐饮生态M端")
    val pay_quickpass = Value(8000,"闪付视图")

    val sjst_erp = Value(7001,"餐饮商家生态")
    val micro_loan = Value(4001,"小微信贷")
    val daocan_c = Value(7002,"到餐c端交易")
    val daocan_message = Value(7003,"到餐信息组")
    val wallet = Value(5100,"钱包")
    val pay_c = Value(5001,"支付c端")
    val pay_risk = Value(5002,"风控")

    val lottery = Value(4002,"彩票")
    val pay_b = Value(5003,"支付b端")

    val ad_business = Value(9000,"广告业务运营")

  }


  // note：偏应用函数
  val conditions = Map(
    Graph.waimai_group.id -> isWaimaiGroup _,
    Graph.waimai.id -> isWaimai _,
    Graph.waimai_m.id -> isWaimaiM _,
    Graph.waimai_c.id -> isWaimaiC _,
    Graph.waimai_b.id -> isWaimaiB _,
    Graph.banma.id -> isBanma _,

    Graph.daodian_group.id -> isDaodianGroup _,
    Graph.meishi.id -> isMeishi _,

    Graph.hotel_group.id -> isHotelGroup _,
    Graph.hotel.id -> isHotel _,
    Graph.hotel_m.id -> isHotelM _,
    Graph.travel.id -> isTravel _,

    Graph.movie_group.id -> isMovieGroup _,

    Graph.finance_group.id -> isFinanceGroup _,
    Graph.pay_group.id -> isPayGroup _,
    Graph.car_group.id -> isCarGroup _,
    Graph.sjst_m_group.id -> isSjstMGroup _,
    Graph.pay_quickpass.id -> isQuickpass _,
    Graph.sjst_erp.id -> isSjstErp _,
    Graph.micro_loan.id -> isMicroLoan _,
    Graph.daocan_c.id -> isDaocanC _,
    Graph.daocan_message.id -> isDaocanMessage _,
    Graph.wallet.id -> isWallet _,
    Graph.pay_c.id -> isPayC _,
    Graph.pay_risk.id -> isPayRisk _,
    Graph.lottery.id -> isLottery _,

    Graph.pay_b.id -> isPayB _,
    Graph.ad_business.id -> isAdBusiness _

  )

  def isInGraph(graphId: Int, desc: ServiceModels.Desc) = {
    val condition = conditions.get(graphId)
    condition.isDefined && condition.get.apply(desc)
  }

  def isWaimaiGroup(desc: ServiceModels.Desc) = {
    BusinessOwtService.getBusiness(desc.owt.getOrElse("")) == Business.waimai.getId
  }

  def isWaimai(desc: ServiceModels.Desc) = {
    desc.appkey.contains("waimai") && isWaimaiGroup(desc)
  }

  def isWaimaiM(desc: ServiceModels.Desc) = {
    desc.owt.getOrElse("") == WAIMAI && desc.pdl.getOrElse("") == "m"
  }

  def isWaimaiC(desc: ServiceModels.Desc) = {
    desc.owt.getOrElse("") == WAIMAI && desc.pdl.getOrElse("") == "c"
  }

  def isWaimaiB(desc: ServiceModels.Desc) = {
    desc.owt.getOrElse("") == WAIMAI && desc.pdl.getOrElse("") == "e"
  }

  def isBanma(desc: ServiceModels.Desc) = {
    desc.appkey.contains(".banma.")
  }

  def isSc(desc: ServiceModels.Desc) = {
    desc.appkey.contains(".sc.") && isWaimaiGroup(desc)
  }

  def isXg(desc: ServiceModels.Desc) = {
    val groupMatched = desc.group.getOrElse("").contains("供应链")
    (groupMatched || desc.appkey.contains(".xg.")) && isWaimaiGroup(desc)
  }

  def isDaodianGroup(desc: ServiceModels.Desc) = {
    desc.business.exists(_ == Business.platform.getId)
  }

  def isMeishi(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    owt.contains("meishi") || owt.contains("wpt") || owt.contains("web")
  }

  def isHotelGroup(desc: ServiceModels.Desc) = {
    BusinessOwtService.getBusiness(desc.owt.getOrElse("")) == Business.hotel.getId
  }

  def isHotel(desc: ServiceModels.Desc) = {
    desc.appkey.contains(".hotel.") && isHotelGroup(desc)
  }

  def isHotelM(desc: ServiceModels.Desc) = {
    val groupMatched = desc.group.getOrElse("").contains("M")
    groupMatched && desc.appkey.contains(".hotel.") && isHotelGroup(desc)
  }

  def isTravel(desc: ServiceModels.Desc) = {
    (desc.appkey.contains("meilv") || desc.appkey.contains("lvyou") || desc.appkey.contains("travel")) && isHotelGroup(desc)
  }

  def isMovieGroup(desc: ServiceModels.Desc) = {
    BusinessOwtService.getBusiness(desc.owt.getOrElse("")) == Business.movie.getId
  }

  def isFinanceGroup(desc: ServiceModels.Desc) = {
    BusinessOwtService.getBusiness(desc.owt.getOrElse("")) == Business.finance.getId
  }

  def isPayGroup(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    owt.equals("zc") || owt.equals("qdb")
  }

  def isCarGroup(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    owt.equals("qcs")
  }
  def isSjstMGroup(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    owt.equals("sjst") && pdl.equals("m")
  }
  def isQuickpass(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    owt.equals("cx") && pdl.equals("quickpass")
  }
  def isSjstErp(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val pdls = List("erp","data","app","com","openplatform","lot")
    owt.equals("sjst") && pdls.contains(pdl)
  }
  def isMicroLoan(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val pdls = List("account","acs","credit","crm","ct","dws","ecif","fe","id","lam","market","mart","optlog","product","rc","trade","user","web")
    owt.equals("fd") && pdls.contains(pdl)
  }

  def isDaocanC(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val pdls = List("order","receipt","refund")
    owt.equals("web") && pdls.contains(pdl)
  }

  def isDaocanMessage(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val pdls = List("poi","meishifilter","deal")
    owt.equals("web") && pdls.contains(pdl)
  }
  def isWallet(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val pdls = List("wallet","userproduct","promotion")
    owt.equals("qianbao") && pdls.contains(pdl)
  }
  def isPayC(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val payPdls = List("paycashier","qdbcashier","userproduct","paytrade")
    val conchPdls = List("refund","tradecore","trade")
    (owt.equals("pay") && payPdls.contains(pdl)) || (owt.equals("conch") && conchPdls.contains(pdl))
  }

  def isPayRisk(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    owt.equals("pay") && pdl.equals("rc")
  }

  def isLottery(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    owt.equals("fsp") && pdl.equals("lot")
  }

  def isPayB(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    val payPdls = List("account","funds","fundstransfer","merchantproduct","bankgw")
    val conchPdls = List("account","balance","accounting","bill","certify","fee","funds","settle")
    (owt.equals("pay") && payPdls.contains(pdl)) || (owt.equals("conch") && conchPdls.contains(pdl))
  }


  def isAdBusiness(desc: ServiceModels.Desc) = {
    val owt = desc.owt.getOrElse("")
    val pdl = desc.pdl.getOrElse("")
    owt.equals("adp") && pdl.equals("ado")
  }


}
