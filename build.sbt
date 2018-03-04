organization in ThisBuild := "io.magickeeper"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test

lazy val `magickeeper` = (project in file("."))
  .aggregate(`cardlists-api`, `cardlists-impl`)

lazy val `cardlists-api` = (project in file("magickeeper-cardlists-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `cardlists-impl` = (project in file("magickeeper-cardlists-impl"))
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
  .dependsOn(`cardlists-api`)

lazy val `web-gateway` = (project in file("gateway"))
  .enablePlugins(PlayScala && LagomPlay)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslServer,
      macwire,
      scalaTest
    ))
  .dependsOn(`cardlists-api`)


