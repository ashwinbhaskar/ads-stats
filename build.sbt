import Dependencies._

ThisBuild / scalaVersion := "2.13.2"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"
assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val circeVersion = "0.13.0"
val doobieVersion = "0.8.8"
val http4sVersion = "1.0.0-M16"
val natchezVersion = "0.1.0-M4"
val catsEffectVersion = "3.0.0-RC2"
val compilerOptions = Seq(
      "-Ywarn-dead-code",
      "-Ywarn-unused:imports", 
      "-Ywarn-unused:locals", 
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-deprecation"
      // "-Xfatal-warnings"
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
         "org.tpolecat" %% "natchez-jaeger"
      ).map(_ % natchezVersion) ++ 
      Seq(
        "com.typesafe" % "config" % "1.4.0",
        "org.flywaydb" % "flyway-core" % "6.2.1",
        "io.jaegertracing" % "jaeger-client" % "1.3.2"
      ) ++
      Seq(
        "org.typelevel" %% "cats-effect"
      ).map(_ % catsEffectVersion)
  ).dependsOn(shared)
