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

lazy val perfTest = (project in file("perf-test"))
  .settings(
    name := "ads-stats-perf-test",
    cancelable := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "requests" % "0.6.5",
      "com.github.pureconfig" %% "pureconfig" % "0.13.0"
    )
  )

lazy val root = (project in file("."))
  .settings(
    name := "ads-stats",
    fork := true,
    cancelable := true,
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
        "ch.qos.logback" % "logback-classic" % "1.2.3",
        "io.jaegertracing" % "jaeger-client" % "1.3.2"
      )
  )
