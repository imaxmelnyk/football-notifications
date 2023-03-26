package dev.maxmelnyk.footballnotifications

import cats.effect.{IO, IOApp}
import com.typesafe.scalalogging.LazyLogging
import dev.maxmelnyk.footballnotifications.bot.Bot
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient
import dev.maxmelnyk.footballnotifications.config.Config
import dev.maxmelnyk.footballnotifications.db.dao.SubscriptionsDao
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.postgresql.Driver
import sttp.client3.http4s.Http4sBackend

object Main extends IOApp.Simple with LazyLogging {
  val run: IO[Unit] = {
    val resources = for {
      // for all the http requests
      sttpBackend <- Http4sBackend.usingDefaultBlazeClientBuilder[IO]()

      // database connections
      dbExecutionContext <- ExecutionContexts.fixedThreadPool[IO](Config.dbConnectionPoolSize)
      transactor <- HikariTransactor.newHikariTransactor[IO](
        classOf[Driver].getName,
        Config.dbUrl,
        Config.dbUser,
        Config.dbPassword,
        dbExecutionContext)
    } yield (sttpBackend, transactor)

    resources.use { case (sttpBackend, transactor) =>
      val apiFootballClient: ApiFootballClient[IO] = ApiFootballClient[IO](sttpBackend)
      val subscriptionsDao: SubscriptionsDao[IO] = SubscriptionsDao[IO](transactor)
      val bot: Bot[IO] = Bot[IO](sttpBackend, apiFootballClient, subscriptionsDao)

      logger.info("Starting message polling...")
      bot.startPolling()
    }
  }
}
