import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.sbt.SbtAspectj._
import com.typesafe.sbt.SbtNativePackager.NativePackagerKeys._
import com.typesafe.sbt.SbtNativePackager._
import org.rbayer.GruntSbtPlugin._
import GruntKeys._
import sbt.Keys._
import sbtfilter.Plugin.FilterKeys._

val versionsConfiguration = settingKey[Config]("Dependency versions configuration")
val thisVersion = settingKey[String]("Base Framework Version")
val coreVersion = settingKey[String]("Core Framework Version")
val accountsVersion = settingKey[String]("Accounts Framework Version")
val qmsVersion = settingKey[String]("QMS Version")
val pieVersion = settingKey[String]("Insights Engine Version")
val geocodeVersion = settingKey[String]("Geocode Version")

versionsConfiguration := {
  ConfigFactory.parseFile(new File("versions.conf")).resolve()
}

thisVersion in ThisBuild := versionsConfiguration.value.getString("base.version")

coreVersion in ThisBuild := versionsConfiguration.value.getString("core.version")

accountsVersion in ThisBuild := versionsConfiguration.value.getString("accounts.version")

qmsVersion in ThisBuild := versionsConfiguration.value.getString("qms.version")

pieVersion in ThisBuild := versionsConfiguration.value.getString("pie.version")

geocodeVersion in ThisBuild := versionsConfiguration.value.getString("geocode.version")

aspectjSettings

organization := "com.poshly"

name := "Products UI"

version := thisVersion.value.replaceAll("-SNAPSHOT", System.currentTimeMillis().toString)

organizationName := "Poshly Inc."

startYear := Some(2014)

description := "Poshly Products UI"

scalaVersion := "2.11.8"

val akkaVersion = "2.4.17"
val kamonVersion = "0.3.5"
val sprayVersion = "1.3.3"
val json4sVersion = "3.2.11"

libraryDependencies ++= Seq(
  "com.poshly" %% "core-metrics" % coreVersion.value excludeAll ExclusionRule(organization = "com.twitter"),
  "com.poshly" %% "core-server" % coreVersion.value excludeAll ExclusionRule(organization = "com.twitter"),
  "com.poshly" %% "core-redis" % coreVersion.value,
  "com.poshly" %% "core-mailer" % coreVersion.value,
  "com.poshly" %% "core-webkit" % coreVersion.value,
  "com.poshly" %% "core-zk" % coreVersion.value,
  "com.poshly.qms" %% "qms-client" % qmsVersion.value withSources() excludeAll(
    ExclusionRule(organization = "org.slf4j"), ExclusionRule(organization = "log4j")),
  "com.poshly.geocode" %% "geocode-client" % geocodeVersion.value withSources(),
  "com.poshly.pie" %% "pie-client" % pieVersion.value withSources(),
  "com.poshly.sparkservice" %% "sparkservice-client" % pieVersion.value withSources(),
  "com.poshly.accounts" %% "accounts-client" % accountsVersion.value withSources(),
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "org.json4s" %% "json4s-native" % json4sVersion,
  "org.json4s" %% "json4s-ext" % json4sVersion withSources(),
  "io.spray" %% "spray-caching" % sprayVersion,
  "io.spray" %% "spray-can" % sprayVersion,
  "io.spray" %% "spray-httpx" % sprayVersion,
  "io.spray" %% "spray-routing" % sprayVersion,
  "io.spray" %% "spray-json" % "1.3.2",
  "com.newrelic.agent.java" % "newrelic-agent" % "3.14.0",
  "io.kamon" %% "kamon-core" % kamonVersion,
  "io.kamon" %% "kamon-spray" % kamonVersion,
  "io.kamon" %% "kamon-newrelic" % kamonVersion,
  "org.aspectj" % "aspectjweaver" % "1.8.5"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "io.spray" %% "spray-testkit" % sprayVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test",
  "org.specs2" %% "specs2" % "2.3.13"
)

resolvers := Seq(
  "plugins-releases" at "http://nexus.poshly.com/content/repositories/plugins-releases/",
  "poshly-plugins" at "http://nexus.poshly.com/content/groups/poshly-plugins/",
  "poshly-nexus" at "http://nexus.poshly.com/content/groups/public",
  "poshly-snapshots" at "http://nexus.poshly.com/content/repositories/snapshots/",
  "poshly-releases" at "http://nexus.poshly.com/content/repositories/releases/",
  "central" at "http://repo1.maven.org/maven2/",
  "Websudos" at "http://repo1.maven.org/maven2/com/websudos/",
  "Websudos bintray releases" at "https://dl.bintray.com/websudos/oss-releases/"
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle

javaOptions <++= AspectjKeys.weaverOptions in Aspectj

initialize ~= { _ =>
  System.setProperty("run.mode", "dev")
}

publishMavenStyle := true

compileOrder := CompileOrder.ScalaThenJava

// For Bamboo to see the test output
logBuffered in Test := false

// Include only src/main/java in the compile configuration
unmanagedSourceDirectories in Compile += (scalaSource in Compile).value

// Include only src/test/java in the test configuration
unmanagedSourceDirectories in Test += (scalaSource in Test).value

credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

fork in run := true

javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

resourceGenerators in Compile <+= (resourceManaged, baseDirectory) map { (managedBase, base) =>
  val webappBase = base / "dist"
  for {
    (from, to) <- webappBase ** "*" x rebase(webappBase, managedBase / "main" / "webapp")
  } yield {
    Sync.copy(from, to)
    to
  }
}

seq(filterSettings: _*)

includeFilter in(Compile, filterResources) ~= { f => f || ("*.props") }

extraProps in filterResources ++= Seq("version" -> thisVersion.value)

// watch app files
watchSources <++= baseDirectory map { path => ((path / "app") ** "*").get }

gruntSettings

gruntTasks in Compile := Seq("build --force")

gruntResourcesDirectory in Compile := Option(baseDirectory.value / "dist")

gruntResourcesClasspath in Compile := file("webapp")

packageArchetype.java_server

daemonGroup in Linux := "services"

rpmGroup := Some("Poshly/Services")

rpmBrpJavaRepackJars := false

maintainer in Linux := "Poshly Inc."

packageSummary in Linux := "Products UI"

packageDescription := "Products UI"

rpmRelease := "2"

rpmLicense := Some("Proprietary")

packageArchitecture in Rpm := "x86_64"

rpmVendor := "Poshly Inc."

mappings in Universal <+= (packageBin in Compile, sourceDirectory) map { (_, src) =>
  val conf = src / "main" / "conf" / "newrelic.yml"
  conf -> "conf/newrelic.yml"
}

linuxScriptReplacements += "run_mode" -> Option(System.getProperty("run.mode")).getOrElse("dev")

linuxScriptReplacements += "newrelic_env" -> Option(System.getProperty("newrelic.environment")).getOrElse("dev")

linuxScriptReplacements += "newrelic_jar" -> libraryDependencies.value.find(_.name == "newrelic-agent").map(artifact => s"${artifact.organization}.${artifact.name}-${artifact.revision}.jar").get

linuxScriptReplacements += "aspectj_jar" -> libraryDependencies.value.find(_.name == "aspectjweaver").map(artifact => s"${artifact.organization}.${artifact.name}-1.8.5.jar").get

defaultLinuxInstallLocation := "/services"
