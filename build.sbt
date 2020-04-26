import Dependencies._

ThisBuild / scalaVersion     := "2.12.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "net.michalp"
ThisBuild / organizationName := "michalp"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.0-RC18-2",
  "com.lihaoyi" %% "os-lib" % "0.2.7"
)
lazy val root = (project in file("."))
  .settings(
    name := "Keybase API",
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
