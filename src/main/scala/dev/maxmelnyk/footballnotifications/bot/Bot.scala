package dev.maxmelnyk.footballnotifications.bot

import cats.effect.Async
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient
import dev.maxmelnyk.footballnotifications.config.Config
import sttp.client3.SttpBackend

class Bot[F[_]: Async](sttpBackend: SttpBackend[F, Any],
                       protected val apiFootballClient: ApiFootballClient[F])
  extends TelegramBot[F](Config.telegramBotToken, sttpBackend)
    with Polling[F]
    with SubscriptionBot[F]

object Bot {
  def apply[F[_]: Async](sttpBackend: SttpBackend[F, Any],
                         apiFootballClient: ApiFootballClient[F]): Bot[F] = {
    new Bot(sttpBackend, apiFootballClient)
  }
}
