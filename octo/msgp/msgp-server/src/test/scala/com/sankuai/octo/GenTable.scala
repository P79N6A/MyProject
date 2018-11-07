package com.sankuai.octo

object GenTable {

  def wyzgen() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.4.239.149:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/wangyanzhao/project/project-web/octo/msgp/msgp-server/src/main/scala",
        "com.sankuai.octo.msgp.db")
    )
  }

  def wyzGenErrorLog() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.4.239.54:3306/mtsg?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/wangyanzhao/project/project-web/octo/msgp/msgp-server/src/main/scala",
        "com.sankuai.octo.errorlog.db")
    )
  }

  def zavaworthgen() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.4.239.149:3306/mworth?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/zava/inf/octo/mworth/mworth-server/src/main/scala",
        "com.sankuai.octo.mworth.db")
    )
  }

  def zavamsgpgen() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.4.239.149:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/zava/inf/octo/mworth/mworth-server/src/main/scala",
        "com.sankuai.octo.msgp.db")
    )
  }

  def yvesgenmworth() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.4.239.149:3306/mworth?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/yves/inf/octo/mworth/mworth-server/src/main/scala",
        "com.sankuai.octo.mworth.db")
    )
  }

  def yvesgenmsgp() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.4.239.149:3306/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/yves/downloads/msgp/msgp-server",
        "com.sankuai.octo.msgp.db")
    )
  }

  def yrGenErrorLog() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver",
        "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.24.32.30:5002/errorlog_offline?user=errorlog_offline&password=IL2ruJ2VpxRTCx&useUnicode=true&characterEncoding=UTF-8",
        "/Users/emma/Documents/workspace/octo/msgp/msgp-server/src/main/scala",
        "com.sankuai.octo.errorlog.db")
    )
  }

  def zyGenMsgp() = {
    slick.codegen.SourceCodeGenerator.main(
      Array("scala.slick.driver.MySQLDriver", "com.mysql.jdbc.Driver",
        "jdbc:mysql://10.20.116.73:5002/msgp?user=dbadmin&password=dbpasswd&useUnicode=true&characterEncoding=UTF-8",
        "/Users/nero/Desktop",
        "com.sankuai.octo.msgp.db")
    )
  }

  def main(args: Array[String]) {
    yrGenErrorLog()
  }
}
