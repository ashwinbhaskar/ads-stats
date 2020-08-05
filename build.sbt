import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"

val circeVersion = "0.13.0"
val doobieVersion = "0.8.8"
val http4sVersion = "0.21.6"

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
        "com.typesafe" % "config" % "1.4.0",
        "org.flywaydb" % "flyway-core" % "6.2.1"
      )
  )
