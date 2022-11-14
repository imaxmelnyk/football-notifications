package dev.maxmelnyk.footballnotifications

import cats.effect.{IO, IOApp}
import com.typesafe.scalalogging.LazyLogging
import dev.maxmelnyk.footballnotifications.config.Config
import sttp.client3.http4s.Http4sBackend

object Main extends IOApp.Simple with LazyLogging {
  val run: IO[Unit] = {
    Http4sBackend.usingDefaultBlazeClientBuilder[IO]().use { sttpBackend =>
      logger.info("Starting message polling...")
      EchoBot[IO](Config.telegramBotToken, sttpBackend).startPolling()
    }
  }
}
