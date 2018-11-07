package com.sankuai.octo.mnsc.bootstrap

import com.sankuai.octo.mnsc.dataCache._
import com.sankuai.octo.mnsc.model.Path
import com.sankuai.octo.mnsc.zkWatcher._
import org.joda.time.DateTime



class bootstrap {
  def init() = {
    val start = new DateTime().getMillis
    println(s"start to init.")
    appProviderWatcher.initPathCache()

    appDescDataCache.renewAllDescForce(true)
    appProviderDataCache.renewAllProviderForce(Path.provider.toString, true)
    appProviderHttpDataCache.renewAllProviderForce(Path.providerHttp.toString, true)
    httpPropertiesDataCache.renewAllHttpPropertiesForce(true)
    httpGroupDataCache.renewAllGroups(true)

    appProviderWatcher.initWatcher()
    httpPropertiesWatcher.initWatcher()
    httpGroupWatcher.initWatcher()

    appDescDataCache.doDescRenew()
    appProviderHttpDataCache.doRenew()
    appProviderDataCache.doRenew()
    httpPropertiesDataCache.doRenew()
    httpGroupDataCache.doRenew()
    val end = new DateTime().getMillis
    println(s"finish to init -->  cost ${end - start}")
  }
}
