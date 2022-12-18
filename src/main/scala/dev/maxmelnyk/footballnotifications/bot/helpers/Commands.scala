package dev.maxmelnyk.footballnotifications.bot.helpers

import cats.syntax.all._
import com.bot4s.telegram.api.declarative.{Action, CommandFilterMagnet, Commands => TelegramCommands}
import com.bot4s.telegram.models.Message
import com.typesafe.scalalogging.AnyLogging

private[bot] trait Commands[F[_]]
  extends TelegramCommands[F]
    with AnyLogging {

  override def onCommand(filter: CommandFilterMagnet)(action: Action[F, Message]): Unit = {
    super.onCommand(filter) { implicit msg =>
      action(msg).recoverWith {
        case e: Exception =>
          logger.error("Error occurred during processing command.", e)
          reply("Error occurred, try again later.").void
      }
    }
  }
}
