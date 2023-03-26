import sbt._

object Dependencies {
  private val config = Seq("com.typesafe" % "config" % "1.4.2")

  private val logging = Seq(
    "ch.qos.logback" % "logback-classic" % "1.4.6",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5")

  private val circeVersion = "0.14.5"
  private val circe = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion)

  private val cats = Seq("org.typelevel" %% "cats-effect" % "3.4.8")

  private val doobieVersion = "1.0.0-RC1"
  private val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % doobieVersion,
    "org.tpolecat" %% "doobie-postgres" % doobieVersion,
    "org.tpolecat" %% "doobie-hikari" % doobieVersion)

  private val http4sVersion = "0.23.14"
  private val http4s = Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion)

  private val sttpVersion = "3.8.13"
  private val sttp = Seq(
    "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
    "com.softwaremill.sttp.client3" %% "http4s-backend" % sttpVersion)

  private val telegram = Seq("com.bot4s" %% "telegram-core" % "5.6.3")

  private val test = Seq(
    "org.scalatest" %% "scalatest" % "3.2.15" % Test,
    "org.scalamock" %% "scalamock" % "5.2.0" % Test,
    "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0" % Test,
    "com.h2database" % "h2" % "2.1.214" % Test,
    "org.flywaydb" % "flyway-core" % "9.16.0" % Test)

  val allDeps: Seq[ModuleID] = logging ++ config ++ circe ++ cats ++ doobie ++ http4s ++ sttp ++ telegram ++ test
}
