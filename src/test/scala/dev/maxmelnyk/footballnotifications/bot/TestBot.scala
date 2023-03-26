package dev.maxmelnyk.footballnotifications.bot

import cats.MonadThrow
import cats.instances.all.catsStdInstancesForTry
import com.bot4s.telegram.api.BotBase
import com.typesafe.scalalogging.StrictLogging

import scala.util.Try

trait TestBot extends BotBase[Try] with StrictLogging {
  implicit val monad: MonadThrow[Try] = catsStdInstancesForTry
}
