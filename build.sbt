organization in ThisBuild := "io.magickeeper"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `magickeeper` = (project in file("."))
  .aggregate(`magickeeper-api`, `magickeeper-impl`)

lazy val `magickeeper-api` = (project in file("magickeeper-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `magickeeper-impl` = (project in file("magickeeper-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`magickeeper-api`)

lazy val `web-gateway` = (project in file("gateway"))
  .enablePlugins(PlayScala && LagomPlay)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      macwire,
      scalaTest
    ))
  .dependsOn(`magickeeper-api`)


