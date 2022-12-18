package dev.maxmelnyk.footballnotifications.bot.helpers

import cats.syntax.all._
import com.bot4s.telegram.api.declarative.{Action, Callbacks => TelegramCallbacks}
import com.bot4s.telegram.models.CallbackQuery
import com.typesafe.scalalogging.AnyLogging

private[bot] trait Callbacks[F[_]]
  extends TelegramCallbacks[F]
    with AnyLogging {

  override def onCallbackQuery(action: Action[F, CallbackQuery]): Unit = {
    super.onCallbackQuery { implicit cbq =>
      action(cbq).recoverWith {
        case e: Exception =>
          logger.error("Error occurred during processing callback.", e)
          ackCallback(Some("Error occurred, try again later.")).void
      }
    }
  }
}
