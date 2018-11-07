import sbt._
import com.github.bigtoast.sbtthrift.ThriftPlugin

seq(ThriftPlugin.thriftSettings: _*)

organization := "com.sankuai.inf.octo"

name := "oswatch"

version := "1.2.0-SNAPSHOT"

scalaVersion := "2.11.7"

assemblyOutputPath in assembly := file("deploy/inf-octo-oswatch-assembly.jar")
mainClass in assembly := Some("com.sankuai.octo.oswatch.server.MTThriftServer")
test in assembly := {}

assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter { x => x.data.getName.matches("sbt.*") || x.data.getName.matches(".*macros.*") }
}

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "unwanted.txt" => MergeStrategy.discard
  case PathList("netty", "bundles", xs@_*) => MergeStrategy.first
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith ".key" => MergeStrategy.first
  case PathList("org", "jboss", "netty", xs@_*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "collections", xs@_*) => MergeStrategy.first
  case PathList("org", "w3c", "dom", "TypeInfo.class") => MergeStrategy.first
  case PathList("com", "sankuai", "inf", xs@_*) => MergeStrategy.first
  case PathList("com", "sankuai", "octo", xs@_*) => MergeStrategy.first
  case "jboss-beans.xml" => MergeStrategy.first
  case "rootdoc.txt" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

resolvers ++= Seq(
  "Meituan ivy" at "http://nexus.sankuai.com:8081/nexus/content/repositories/ivy/",
  "Meituan release" at "http://nexus.sankuai.com:8081/nexus/content/repositories/releases/",
  "Meituan snapshot" at "http://nexus.sankuai.com:8081/nexus/content/repositories/snapshots/",
  "Meituan public release" at "http://nexus.sankuai.com:8081/nexus/content/repositories/public/",
  "Meituan public snapshot" at "http://nexus.sankuai.com:8081/nexus/content/repositories/public-snapshots/"
)

val springVersion = "3.1.2.RELEASE"
libraryDependencies ++= Seq(
  "org.springframework" % "spring-core" % springVersion,
  "org.springframework" % "spring-context" % springVersion,
  "org.springframework" % "spring-context-support" % springVersion,
  "org.springframework" % "spring-tx" % springVersion,
  "org.springframework" % "spring-beans" % springVersion,

  // sankuai
  "com.meituan.service.mobile" % "mtthrift" % "1.6.2-SNAPSHOT",
  "com.sankuai.meituan" % "mtconfig-client" % "1.1.0-SNAPSHOT",
  "com.sankuai.octo" % "idl-oswatch" % "1.1.0-SNAPSHOT",
  "com.sankuai.hulk" % "idl-harbor" % "1.2.8-SNAPSHOT",

  // java
  "mysql" % "mysql-connector-java" % "5.1.36",
  "com.mchange" % "c3p0" % "0.9.5.1",

  // scala
  "org.scalaj" %% "scalaj-http" % "1.1.5",
  "com.typesafe.play" %% "play-json" % "2.4.0-M1",
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.slick" %% "slick-codegen" % "2.1.0",
  "org.scalatest" %% "scalatest" % "2.2.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.13",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
)

libraryDependencies ~= { _.map(_.exclude("org.slf4j", "slf4j-log4j12"))}
