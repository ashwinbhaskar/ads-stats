import Dependencies._

ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "ads-delivery",
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
