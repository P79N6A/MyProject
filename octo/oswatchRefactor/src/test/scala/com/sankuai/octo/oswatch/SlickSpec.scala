package com.sankuai.octo.oswatch

import org.scalatest._
/**
 * Created by dreamblossom on 15/9/29.
 */
class SlickSpec extends FlatSpec with Matchers{
  {
      scala.slick.codegen.SourceCodeGenerator.main(
        Array("scala.slick.driver.MySQLDriver",
          "com.mysql.jdbc.Driver",
          "jdbc:mysql://172.27.2.202:3306/inf_oswatch?user=root&password=oswatch&useUnicode=true&characterEncoding=UTF-8",
          "/Users/dreamblossom/project/overloadHotfix/oswatchRefactor/src/main/scala",
          "com.sankuai.octo.oswatch.db")
      )
    }
}
