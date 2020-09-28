import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"
ThisBuild / resolvers += Resolver.bintrayRepo("colisweb", "maven")
assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val circeVersion = "0.13.0"
val doobieVersion = "0.8.8"
val http4sVersion = "0.21.6"
val scalaTracingVersion = "2.4.1"
val compilerOptions = Seq(
      "-Ywarn-dead-code",
      "-Ywarn-unused:imports", 
      "-Ywarn-unused:locals", 
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-deprecation",
      "-Xfatal-warnings"
    )

lazy val perfTest = (project in file("perf-test"))
  .settings(
    name := "ads-stats-perf-test",
    cancelable := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "requests" % "0.6.5",
      "com.lihaoyi" %% "upickle" % "0.9.5",
      "com.github.pureconfig" %% "pureconfig" % "0.13.0",
      "org.scalacheck" %% "scalacheck" % "1.14.1",
      "org.typelevel" %% "cats-effect" % "2.1.4",
      "co.fs2" %% "fs2-core" % "2.4.0"
    ),
    scalacOptions ++= compilerOptions
  ).dependsOn(shared)

lazy val shared = (project in file("shared"))
  .settings(
    name := "shared",
    libraryDependencies ++=Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    ),
    scalacOptions ++= compilerOptions
  )

lazy val root = (project in file("."))
  .settings(
    name := "ads-stats",
    fork := true,
    cancelable := true,
    scalacOptions ++= compilerOptions,
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-literal"
    ).map(_ % circeVersion) ++
      Seq(scalaTest % Test) ++
      Seq(
        "org.tpolecat" %% "doobie-core",
        "org.tpolecat" %% "doobie-hikari",
        "org.tpolecat" %% "doobie-postgres"
      ).map(_ % doobieVersion) ++
      Seq(
        "org.http4s" %% "http4s-blaze-server",
        "org.http4s" %% "http4s-dsl",
        "org.http4s" %% "http4s-circe"
      ).map(_ % http4sVersion) ++
      Seq(
         "com.colisweb" %% "scala-opentracing-context",
         "com.colisweb" %% "scala-opentracing-http4s-server-tapir"
      ).map(_ % scalaTracingVersion) ++ 
      Seq(
        "com.typesafe" % "config" % "1.4.0",
        "org.flywaydb" % "flyway-core" % "6.2.1",
        "io.jaegertracing" % "jaeger-client" % "1.3.2"
      )
  ).dependsOn(shared)
