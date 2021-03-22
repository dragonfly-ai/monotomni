ThisBuild / scalaVersion := "2.13.3"
ThisBuild / publishTo := Some( Resolver.file("file",  new File( "/var/www/maven" ) ) )
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")
ThisBuild / organization := "ai.dragonfly.code"
ThisBuild / version := "0.02"
ThisBuild / resolvers += "dragonfly.ai" at "https://code.dragonfly.ai/"
ThisBuild / fork in run := true

lazy val akkaHttpVersion = "10.2.3"
lazy val akkaVersion    = "2.6.12"

// root Project contains the example TimeServer.
lazy val root = project.in(file(".")).aggregate(
  monotomni.js,
  monotomni.jvm
).dependsOn(monotomni.projects(JVMPlatform)).settings(
  name := "ExampleTimeServer",
  mainClass in (Compile, run) := Some("ai.dragonfly.monotomni.server.ExampleTimeServer"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion, // or whatever the latest version is
  )
)

lazy val monotomni = crossProject(JSPlatform, JVMPlatform).settings(
  name := "monotomni",
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %%% "scala-xml" % "1.3.0",
    "ai.dragonfly.code" %%% "vector" % "0.302",
    "biz.enef" %%% "slogging" % "0.6.2"
  ),
  mainClass in (Compile, run) := Some("ai.dragonfly.monotomni.Demo")
).jvmSettings(
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0",
).jsSettings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0",
  scalaJSUseMainModuleInitializer := true
)