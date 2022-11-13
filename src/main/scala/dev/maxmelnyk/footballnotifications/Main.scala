package dev.maxmelnyk.footballnotifications

import cats.effect.{IO, IOApp}
import dev.maxmelnyk.footballnotifications.config.Config
import sttp.client3.http4s.Http4sBackend

object Main extends IOApp.Simple {
  val run: IO[Unit] = {
    Http4sBackend.usingDefaultBlazeClientBuilder[IO]().use { sttpBackend =>
      EchoBot[IO](Config.telegramBotToken, sttpBackend).startPolling()
    }
  }
}
