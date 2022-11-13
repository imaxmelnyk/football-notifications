import sbt._

object Dependencies {
  private val config = Seq("com.typesafe" % "config" % "1.4.2")

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

  val allDeps: Seq[ModuleID] = config ++ cats ++ http4s ++ sttp ++ telegram
}
