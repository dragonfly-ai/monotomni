ThisBuild / scalaVersion := "2.13.3"
ThisBuild / publishTo := Some( Resolver.file("file",  new File( "/var/www/maven" ) ) )
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation")
ThisBuild / organization := "ai.dragonfly.code"
ThisBuild / version := "0.01"

lazy val akkaHttpVersion = "10.2.3"
lazy val akkaVersion    = "2.6.12"

// root Project contains the example TimeServer.
lazy val root = project.in(file(".")).aggregate(
  monotomni.js,
  monotomni.jvm
).dependsOn(monotomni.projects(JVMPlatform)).settings(
  name := "ExampleTimeServer",
  mainClass in (Compile, run) := Some("ai.dragonfly.monotomni.ExampleTimeServer"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion, // or whatever the latest version is
  )
)

lazy val monotomni = crossProject(JSPlatform, JVMPlatform).settings(
  name := "monotomni",
  libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "1.3.0",
  mainClass in (Compile, run) := Some("ai.dragonfly.monotomni.TestMonotomni")
).jvmSettings(
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0"
).jsSettings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0",
  scalaJSUseMainModuleInitializer := true
)