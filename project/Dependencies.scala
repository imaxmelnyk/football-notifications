import sbt._

object Dependencies {
  private val config = Seq("com.typesafe" % "config" % "1.4.2")

  private val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.4.4",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5")

  private val circeVersion = "0.14.3"
  private val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion)

  private val cats = Seq("org.typelevel" %% "cats-effect" % "3.3.14")

  private val http4sVersion = "0.23.12"
  private val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion)

  private val sttpVersion = "3.8.3"
  private val sttp = Seq(
    "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
    "com.softwaremill.sttp.client3" %% "http4s-backend" % sttpVersion)

  private val telegram = Seq("com.bot4s" %% "telegram-core" % "5.6.1")

  private val test = Seq("org.scalatest" %% "scalatest" % "3.2.14" % "test")

  val allDeps: Seq[ModuleID] = logging ++ config ++ circe ++ cats ++ http4s ++ sttp ++ telegram ++ test
}
