import sbt._
import com.github.bigtoast.sbtthrift.ThriftPlugin

seq(ThriftPlugin.thriftSettings: _*)

organization := "com.sankuai.inf.octo"

name := "sbtTemplate"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

assemblyOutputPath in assembly := file("deploy/inf-octo-sbttemplate-assembly.jar")
mainClass in assembly := Some("com.sankuai.inf.octo.sbttemplate.server.MTThriftServer")
test in assembly := {}

assemblyExcludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter {x => x.data.getName.matches("sbt.*") || x.data.getName.matches(".*macros.*")}
}

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*)                        => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html"                => MergeStrategy.first
  case "application.conf"                                           => MergeStrategy.concat
  case "unwanted.txt"                                               => MergeStrategy.discard
  case PathList("netty", "bundles", xs @ _*)                        => MergeStrategy.first
  case PathList("META-INF", xs @ _*)                                => MergeStrategy.discard
  case PathList(ps @ _*) if ps.last endsWith ".key"                 => MergeStrategy.first
  case PathList("org", "jboss", "netty", xs @ _*)                   => MergeStrategy.first
  case PathList("org", "apache", "commons", "collections", xs @ _*) => MergeStrategy.first
  case PathList("org", "w3c", "dom", "TypeInfo.class")              => MergeStrategy.first
  case PathList("com", "sankuai", "inf", xs @ _*)                   => MergeStrategy.first
  case PathList("com", "sankuai", "octo", xs @ _*)                  => MergeStrategy.first
  case "jboss-beans.xml"                                            => MergeStrategy.first
  case "rootdoc.txt"                                                => MergeStrategy.discard
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
  "com.meituan.service.mobile" % "mtthrift" % "1.6.2-Degrade-SNAPSHOT"
)
