import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"
ThisBuild / resolvers += Resolver.bintrayRepo("colisweb", "maven")

val circeVersion = "0.13.0"
val doobieVersion = "0.8.8"
val http4sVersion = "0.21.6"
val scalaTracingVersion = "2.4.1"

lazy val root = (project in file("."))
  .settings(
    name := "ads-delivery",
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
        "ch.qos.logback" % "logback-classic" % "1.2.3"
      )
  )
