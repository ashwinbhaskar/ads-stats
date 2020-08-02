import Dependencies._

ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"

val circeVersion = "0.12.3"

lazy val root = (project in file("."))
  .settings(
    name := "ads-delivery",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-literal"
    ).map(_ % circeVersion) ++
    Seq(scalaTest % Test)
)