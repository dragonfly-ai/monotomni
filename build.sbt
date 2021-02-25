ThisBuild / scalaVersion := "2.13.3"

lazy val akkaHttpVersion = "10.2.3"
lazy val akkaVersion    = "2.6.12"

lazy val root = project.in(file(".")).aggregate(monotomni.js, monotomni.jvm).dependsOn(monotomni.jvm).settings(
  name := "ExampleTimeServer",
  version := "0.01",
  organization := "ai.dragonfly.code",
  mainClass in (Compile, run) := Some("ExampleTimeServer"),
  scalacOptions ++= Seq("-feature", "-deprecation"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion, // or whatever the latest version is
  )
)

lazy val monotomni = crossProject(JSPlatform, JVMPlatform).settings(
  publishTo := Some( Resolver.file("file",  new File( "/var/www/maven" ) ) ),
  name := "monotomni",
  version := "0.3",
  organization := "ai.dragonfly.code",
  libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % "1.3.0",
  scalacOptions in ThisBuild ++= Seq("-feature", "-deprecation"),
  mainClass in (Compile, run) := Some("ai.dragonfly.distributed.monotomni.Test")
).jvmSettings(
  libraryDependencies += "org.scala-js" %% "scalajs-stubs" % "1.0.0"
).jsSettings(
  libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.1.0",
  scalaJSUseMainModuleInitializer := true
)
