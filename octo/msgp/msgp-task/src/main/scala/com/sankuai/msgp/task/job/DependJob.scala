package com.sankuai.msgp.task.job


object DependJob {
  /*val SERVER = "server"

  val CLIENT = "client"

  private[job] val logger = LoggerFactory.getLogger(this.getClass)

  private val digitsPattern = Pattern.compile(".*\\d+.*")

  private val sourceMap = Map(SERVER -> false, CLIENT -> true)

  //  获取业务线 -> appkey list
  private val owtDescMap = ReportHelper.getOwtToDescMap

  private val dependThreadPool = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(5))


  def start(timeMillis: Long = System.currentTimeMillis()): Unit = {
    val (start, end) = TaskTimeHelper.getStartEnd(timeMillis)

    logger.info(s"DailyDependJob begin,start:$start,end:$end")

    val calculationBegin = System.currentTimeMillis()
    calculate(start, end, "server")
    calculate(start, end, "client")
    val calculationEnd = System.currentTimeMillis()

    logger.info(s"Calculation begin: $calculationBegin, end: $calculationEnd, last: ${(calculationEnd - calculationBegin) / 1000}")
  }

  def calculate(start: Int, end: Int, source: String) {
    //  计算业务线服务最近7日依赖关系
    owtDescMap.foreach { case (owt, apps) =>
      computeDependency(apps, start, end, source)
    }
  }

  def computeDependency(apps: List[Desc], start: Int, end: Int, source: String) = {
    //  async compute
    val appsPar = apps.par
    appsPar.tasksupport = dependThreadPool
    val depends = appsPar.map { app =>
      val count = getServiceDependencyCount(app.appkey, start, end, source)
      ReportDependDao.ReportDependDomain(app.business.getOrElse(100), app.owt.getOrElse(""), app.appkey,
        sourceMap.getOrElse(source, false), count,
        TaskTimeHelper.getMondayDate(start), System.currentTimeMillis())
    }.toList
    //  batch insert
    batchInsertDependDomains(depends)
  }

  private def batchInsertDependDomains(resList: List[ReportDependDomain]) = {
    if (resList != null && resList.nonEmpty) {
      try {
        ReportDependDao.batchInsert(resList)
      } catch {
        case e: Exception =>
          logger.error("batchInsertErrorDomains fail", e)
      }
    }
  }

  /**
    * 获取服务依赖,或 被依赖的数目
    *
    * @param appKey 服务appkey
    * @param start  起始时间
    * @param end    终止时间
    * @param source client or server
    */
  def getServiceDependencyCount(appKey: String, start: Int, end: Int, source: String) = {
    val env = "prod"
    val metricsTags = DataQuery.tags(appKey, start, end, env, source)
    val count = if (metricsTags.remoteAppKeys.nonEmpty) {
      metricsTags.remoteAppKeys.filter(_ != Constants.ALL).count(hasNoDigit)
    } else {
      0
    }
    count
  }

  private def hasNoDigit(content: String) = {
    val m = digitsPattern.matcher(content)
    !m.matches()

  }

  def getDependency(owt: String, source: String, weekDay: Date, limit: Int) = {
    ReportDependDao.query(owt, sourceMap.getOrElse(source, false), weekDay, limit)
  }*/
}
