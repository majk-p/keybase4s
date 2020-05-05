import Dependencies._

ThisBuild / scalaVersion     := "2.12.11"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "net.michalp"
ThisBuild / organizationName := "michalp"
ThisBuild / organizationHomepage := Some(url("http://michalp.net/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/majk-p/keybase4s"),
    "scm:git@github.com:majk-p/keybase4s.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "majk-p",
    name  = "Michał Pawlik",
    email = "admin@michalp.net",
    url   = url("http://michalp.net")
  )
)

ThisBuild / description := "Keybase API client implemtentation for Scala with ZIO."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/majk-p/keybase4s"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true


val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.0-RC18-2",
  "dev.zio" %% "zio-streams" % "1.0.0-RC18-2",
  "com.lihaoyi" %% "os-lib" % "0.2.7",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % "0.13.0"
)
lazy val root = (project in file("."))
  .settings(
    name := "Keybase API",
    libraryDependencies += scalaTest % Test
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
