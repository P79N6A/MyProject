package com.sankuai.msgp.common.utils.client

import java.net.{InetAddress, UnknownHostException}

import com.sankuai.msgp.common.config.MsgpConfig
import com.sankuai.msgp.common.model.Page
import com.sankuai.msgp.common.utils.helper.JsonHelper
import org.apache.commons.lang3.StringUtils
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.{BoolQueryBuilder, QueryBuilders}
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.DateTime
import org.slf4j.LoggerFactory


object EsClient {
  private val LOGGER = LoggerFactory.getLogger(this.getClass)

  private val ESIndex = "log.octo_errorlog_logservice_all"
  private val ESType = "logs"

  private val cluster = "es.cluster.name"
  private val address = "es.address.name"
  private val port = 9300
  private val ES_MAX_ITEM_COUNT = 10000

  val client = try {
    val settings = Settings.settingsBuilder()
      .put("cluster.name", MsgpConfig.get(cluster, "data_test_cluster")).build()
    TransportClient.builder().settings(settings).build()
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(MsgpConfig.get(address, "test.es.data.sankuai.com")), port));
  } catch {
    case e: UnknownHostException =>
      LOGGER.error("init ES client failed", e)
      throw e
  }

  def getLogByUniqueKey(uniqueKey: String) = {
    val query = new BoolQueryBuilder()
    query.must(QueryBuilders.termQuery("error_unique_key", uniqueKey))
    val request = client.prepareSearch(ESIndex)
      .setTypes(ESType)
      .setQuery(query)

    LOGGER.debug(request.toString)

    val response = request.get()

    val ret = response.getHits.getHits.map { x =>
      x.getSource
    }
    if (ret.length == 0) {
      null
    } else {
      ret.head
    }
  }

  def scanner(appkey: String, hostSet: String, host: String, filterId: Long, exceptionName: String, stime: Long, etime: Long, message: String, page: Page) = {
    val startDay = new DateTime(stime).toString("yyyyMMdd")

    val query = new BoolQueryBuilder()
    query.must(QueryBuilders.termQuery("error_appkey", appkey))
    if (!hostSet.equalsIgnoreCase("All")) {
      query.must(QueryBuilders.termQuery("error_host_set", hostSet))
    }
    if (!host.equalsIgnoreCase("All")) {
      query.must(QueryBuilders.termQuery("error_host", host))
    }
    if (filterId != -1) {
      query.must(QueryBuilders.termQuery("error_filter_id", filterId))
    }
    if (filterId == 0) {
      query.must(QueryBuilders.termQuery("error_exception_name", exceptionName))
    }

    if (!StringUtils.isEmpty(message)) {
      query.must(QueryBuilders.matchQuery("error_message", message))
    }

    query.must(QueryBuilders.rangeQuery("error_time").from(stime).to(etime))

    val request = client.prepareSearch(ESIndex)
      .setTypes(ESType)
      .setQuery(query)
      .setFrom(page.getStart)
      .setSize(page.getPageSize)
      .addSort("error_time", SortOrder.DESC)

    LOGGER.debug(request.toString)

    val response = request.execute().actionGet()

    val ret = response.getHits.getHits.map { x =>
      x.getSource
    }
    // ES 查询范围最大支持1w条
    page.setTotalCount(response.getHits.getTotalHits.toInt, ES_MAX_ITEM_COUNT / page.getPageSize)
    ret
  }

  def getAppkeyLogCount(appkey: String, stime: Long, etime: Long) = {

    val query = new BoolQueryBuilder()
    query.must(QueryBuilders.termQuery("error_appkey", appkey))

    query.must(QueryBuilders.rangeQuery("error_time").from(stime).to(etime))

    val aggregation = AggregationBuilders.cardinality("log_count").field("error_unique_key").precisionThreshold(10000000)
    val request = client.prepareSearch(ESIndex)
      .setTypes(ESType)
      .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
      .setQuery(query)
      .addAggregation(aggregation)
      .setSize(0);
    val response = request.get()
    val ret = response.getAggregations.asMap().get("log_count").getProperty("value")
    ret
  }

  def main(args: Array[String]) {
    println(JsonHelper.jsonStr(scanner("com.sankuai.meishi.poiapi", "All", "All", 0, "", 1473325148000L, 1473325150000L, "", new Page(1, 20))))
  }
}
