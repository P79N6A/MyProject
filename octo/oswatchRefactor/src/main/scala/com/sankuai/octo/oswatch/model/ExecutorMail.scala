package com.sankuai.octo.oswatch.model

import com.sankuai.octo.oswatch.db.Tables.OswatchMonitorPolicyRow

/**
 * Created by dreamblossom on 15/10/3.
 */
object ExecutorMail {
   case class TellRegister(oswatchMonitorPolicyRow: OswatchMonitorPolicyRow, monitorTypeValue:Double)
}
