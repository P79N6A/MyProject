package com.sankuai.octo.statistic.helper

import com.sankuai.octo.statistic.model.{StatEnv, StatSource, Tag, TagKey}
import com.sankuai.octo.statistic.util.tair

/**
  * Created by wujinwu on 16/1/8.
  */
object TagHelper {

  def getTagKey(appkey: String, ts: Int, env: String, source: StatSource = StatSource.Server) = {
    TagKey(appkey, ts, StatEnv.getInstance(env), source)
  }

  def putTagToTair(key: TagKey, value: Tag): Unit = {
    val tairKey = getTagTairKey(key)
    tair.put(tairKey, value)
  }

  def getDailyTag(appKey: String, dayStartTs: Int, statEnv: StatEnv, statSource: StatSource): Option[Tag] = {
    val key = getTagKeyByEnum(appKey, dayStartTs, statEnv, statSource)
    TagHelper.getTagBytesByKey(key) match {
      case Some(bytes) => Some(TagHelper.asTag(bytes))
      case None => None
    }
  }

  def getTagKeyByEnum(appkey: String, ts: Int, env: StatEnv, source: StatSource = StatSource.Server) = {
    TagKey(appkey, ts, env, source)
  }

  def getTagBytesByKey(key: TagKey): Option[Array[Byte]] = {
    val tairKey = getTagTairKey(key)
    tair.getValue(tairKey)
  }

  private def getTagTairKey(key: TagKey) = s"${key.env}|daily|tags|${key.appkey}|${key.ts}|${key.source}"

  def asTag(bytes: Array[Byte]): Tag = {
    api.bytesToObject(bytes, classOf[Tag])
  }

}
