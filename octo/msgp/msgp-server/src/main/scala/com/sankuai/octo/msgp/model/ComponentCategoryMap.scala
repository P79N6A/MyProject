package com.sankuai.octo.msgp.model

import com.sankuai.octo.msgp.domain.Dependency

import scala.collection.immutable.List

/**
  * Created by yves on 16/9/5.
  */
object ComponentCategoryMap {

  val category = Map(
    //选出若干能代表该分类的组件
    ComponentCategory.web_framework -> List(
      new Dependency("org.springframework", "spring-webmvc"),
      new Dependency("org.restlet.jee", "org.restlet"),
      new Dependency("com.sun.jersey", "jersey-server")),

    ComponentCategory.web_container -> List(
      new Dependency("org.eclipse.jetty", "jetty-server"),
      new Dependency("org.apache.tomcat", "tomcat-catalina")
    ),
    ComponentCategory.json -> List(
      new Dependency("com.alibaba", "fastjson"),
      new Dependency("org.json", "json"),
      new Dependency("org.codehaus.jackson", "jackson-core-asl"),
      new Dependency("net.sf.json-lib", "json-lib"),
      new Dependency("com.fasterxml.jackson.core", "jackson-core"),
      new Dependency(" com.google.code.gson", "gson")
    ),
    ComponentCategory.kv -> List(
      new Dependency("com.meituan.cache", "redis-cluster-client"),
      new Dependency("com.taobao.tair", "tair3-client"),
      new Dependency("com.dianping.squirrel", "squirrel-client")
    ),

    ComponentCategory.log -> List(
      new Dependency("com.meituan.inf", "xmd-log4j2"),
      new Dependency("com.meituan.scribe", "scribe-log4j"),
      new Dependency("log4j", "log4j"),
      new Dependency("org.apache.logging.log4j", "log4j-core")
    ),

    ComponentCategory.mq -> List(
      new Dependency("com.meituan.mafka", "mafka-client"),
      new Dependency("com.rabbitmq", "rabbitmq-client"),
      new Dependency(" org.zeromq", "jeromq"),
      new Dependency("com.meituan.mobile.activemq", "activemq-client")
    ),
    ComponentCategory.monitor -> List(
      new Dependency("com.dianping.cat", "cat-client"),
      new Dependency("com.meituan.service.mobile", "jmonitor"),
      new Dependency("com.meituan", "jmonitor")
    ),
    ComponentCategory.http -> List(
      new Dependency("org.apache.httpcomponents", "httpclient"),
      new Dependency("com.squareup.okhttp", "okhttp"),
      new Dependency("com.squareup.okhttp3", "okhttp"),
      new Dependency("org.apache.httpcomponents", "httpasyncclient"),
      new Dependency("org.apache.httpcomponents", "httpcore")
    ),
    ComponentCategory.database_relevant -> List(
      new Dependency("c3p0", "c3p0"),
      new Dependency("com.alibaba", "druid"),
      new Dependency("org.apache.commons", "commons-pool2"),
      new Dependency("com.jolbox", "bonecp"),
      new Dependency("commons-dbcp", "commons-dbcp"),
      new Dependency("org.apache.commons", "commons-dbcp2"),
      new Dependency("com.dianping.zebra", "zebra-api")
    ),
    ComponentCategory.orm -> List(
      new Dependency("org.mybatis", "mybatis"),
      new Dependency("org.hibernate", "hibernate-core")
    ),
    ComponentCategory.octo -> List(
      new Dependency("com.meituan.service.mobile", "mtthrift"),
      new Dependency("com.sankuai.meituan", "mtconfig-client")
    ),
    ComponentCategory.bom -> List(
      new Dependency("com.sankuai", "xmd-bom"),
      new Dependency("com.sankuai", "inf-bom")
    ),
    ComponentCategory.others -> List()
  )

  def getDependencyListByCategory(componentCategory: ComponentCategory.Value) = {
    category.getOrElse(componentCategory, List())
  }

  def getCategoryByDependency(dependency: Dependency) = {
    val categoryEnum  = category.find(x=> x._2.contains(dependency)).getOrElse((ComponentCategory.others,List()))._1
    ComponentCategory.getCategoryVariableName(categoryEnum)
  }
}
