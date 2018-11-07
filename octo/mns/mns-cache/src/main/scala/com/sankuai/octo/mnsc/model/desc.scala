package com.sankuai.octo.mnsc.model

import play.api.libs.json.Json

/*object desc {
  case class descData(appkey: String, business: Int)
  implicit val descDataReads = Json.reads[descData]
  implicit val descDataWrites = Json.writes[descData]
}*/

object desc {
  case class User(id: Int, login: String, name: String) {
    def equal(user: User): Boolean = {
      user.id == this.id && user.login == this.login && user.name == this.name
    }
  }

  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]

  case class descData(name: String, appkey: String, baseApp: Option[String] = Some(""), owners: List[User], observers: Option[List[User]] = Some(List()), intro: String, category: String,
                              business: Option[Int] = Some(0), group: Option[String] = Some(""), base: Option[Int] = Some(0), owt: Option[String] = Some(""), pdl: Option[String] = Some(""),
                              level: Option[Int] = Some(0), tags: Option[String] = Some(""), regLimit: Option[Int] = Some(0), createTime: Option[Long] = Some(0), cell: Option[String] = Some("")) {
    def ownerId: String = owners.map(_.id).mkString(",")

    def owner: String = owners.map(x => x.name + "(" + x.login + ")").mkString(",")

    def observer: String = {
      if (None != observers) {
        observers.get.map(x => x.name + "(" + x.login + ")").mkString(",")
      } else {
        ""
      }
    }

    def observerId: String = {
      if (None != observers) {
        observers.get.map(_.id).mkString(",")
      } else {
        ""
      }
    }
  }

  implicit val descDataReads = Json.reads[descData]
  implicit val descDataWrites = Json.writes[descData]
}


