package dev.maxmelnyk.footballnotifications

import cats.effect.Async
import cats.syntax.functor._
import com.bot4s.telegram.cats.{Polling, TelegramBot}
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import sttp.client3.SttpBackend

class EchoBot[F[_]: Async](token: String,
                           sttpBackend: SttpBackend[F, Any])
  extends TelegramBot[F](token, sttpBackend)
    with Polling[F] {

  override def receiveMessage(msg: Message): F[Unit] = {
    msg.text match {
      case Some(msgText) => request(SendMessage(msg.source, msgText)).void
      case _ => unit
    }
  }
}

object EchoBot {
  def apply[F[_]: Async](token: String,
                         sttpBackend: SttpBackend[F, Any]): EchoBot[F] = new EchoBot(token, sttpBackend)
}
