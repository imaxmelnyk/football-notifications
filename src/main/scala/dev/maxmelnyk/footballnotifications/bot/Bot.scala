package dev.maxmelnyk.footballnotifications.bot

import cats.MonadThrow
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient
import dev.maxmelnyk.footballnotifications.config.Config
import dev.maxmelnyk.footballnotifications.db.dao.SubscriptionsDao
import sttp.client3.SttpBackend

class Bot[F[_]](sttpBackend: SttpBackend[F, Any],
                protected val apiFootballClient: ApiFootballClient[F],
                protected val subscriptionsDao: SubscriptionsDao[F])
               (implicit monad: MonadThrow[F])
  extends TelegramBot[F](Config.telegramBotToken, sttpBackend)
    with Polling[F]
    with SubscriptionBot[F]

object Bot {
  def apply[F[_]](sttpBackend: SttpBackend[F, Any],
                  apiFootballClient: ApiFootballClient[F],
                  subscriptionsDao: SubscriptionsDao[F])
                 (implicit monad: MonadThrow[F]): Bot[F] = {
    new Bot(sttpBackend, apiFootballClient, subscriptionsDao)
  }
}
