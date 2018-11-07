package com.sankuai.octo.oswatch.model

import com.sankuai.octo.oswatch.db.Tables.{OswatchMonitorPolicyRow}

/**
 * Created by dreamblossom on 15/9/30.
 */
object WatcherMail {
    case class Start()
    case class Stop()
    case class Update(oswatchMonitorPoliy: OswatchMonitorPolicyRow)
}
