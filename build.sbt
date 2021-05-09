import Dependencies._

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.1.0"
ThisBuild / organization := "com.ashwinbhaskar"
ThisBuild / organizationName := "example"

val conflictingFiles = Set("Generated", "ManagedBean", "PostConstruct", "PreDestroy", "Priority", "Resource$AuthenticationType", "Resource", "DeclareRoles", "DenyAll", "RolesAllowed", "RunAs", "DataSourceDefinition", "DataSourceDefinitions", "Resources", "PermitAll").map(_ + ".class")
assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.discard
  case x if conflictingFiles.exists(x.endsWith) => MergeStrategy.last
  case x => 
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

val circeVersion = "0.14.0-M6"
val doobieVersion = "1.0.0-M2"
val http4sVersion = "1.0.0-M21"
val natchezVersion = "0.1.2"
val catsEffectVersion = "3.1.0"
val testContainerVersion = "0.39.3"
val compilerOptions = Seq(
      "-Ywarn-dead-code",
      "-Ywarn-unused:imports", 
      "-Ywarn-unused:locals", 
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-deprecation"
      // "-Xfatal-warnings"
    )


lazy val testContainers = Seq(
  "com.dimafeng" %% "testcontainers-scala-postgresql" % testContainerVersion,
  "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainerVersion
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
      "co.fs2" %% "fs2-core" % "3.0.2"
    ),
    scalacOptions ++= compilerOptions
  ).dependsOn(shared % "compile->compile;test->test")

lazy val shared = (project in file("shared"))
  .settings(
    name := "shared",
    libraryDependencies ++=Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
    ) ++ testContainers.map(_ % Test),
    scalacOptions ++= compilerOptions
  )

lazy val root = (project in file("."))
  .settings(
    name := "ads-stats",
    fork := true,
    cancelable := true,
    scalacOptions ++= compilerOptions,
    semanticdbEnabled := true, // enable SemanticDB
    semanticdbVersion := scalafixSemanticdb.revision, // use Scalafix compatible version
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
         "org.tpolecat" %% "natchez-jaeger",
         "org.tpolecat" %% "natchez-noop"
      ).map(_ % natchezVersion) ++ 
      Seq(
        "com.typesafe" % "config" % "1.4.0",
        "org.flywaydb" % "flyway-core" % "6.2.1",
        "io.jaegertracing" % "jaeger-client" % "1.3.2"
      ) ++
      Seq(
        "org.typelevel" %% "cats-effect"
      ).map(_ % catsEffectVersion)
  ).dependsOn(shared % "compile->compile;test->test")
