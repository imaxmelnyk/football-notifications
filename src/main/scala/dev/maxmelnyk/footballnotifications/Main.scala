package dev.maxmelnyk.footballnotifications

import cats.effect.{IO, IOApp}
import com.typesafe.scalalogging.LazyLogging
import dev.maxmelnyk.footballnotifications.bot.Bot
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient
import sttp.client3.http4s.Http4sBackend

object Main extends IOApp.Simple with LazyLogging {
  val run: IO[Unit] = {
    Http4sBackend.usingDefaultBlazeClientBuilder[IO]().use { sttpBackend =>
      val apiFootballClient: ApiFootballClient[IO] = ApiFootballClient[IO](sttpBackend)
      val bot: Bot[IO] = Bot[IO](sttpBackend, apiFootballClient)

      logger.info("Starting message polling...")
      bot.startPolling()
    }
  }
}
