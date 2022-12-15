package dev.maxmelnyk.footballnotifications.bot

import cats.syntax.all._
import com.bot4s.telegram.api.declarative.{Callbacks, Commands}
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup}
import com.typesafe.scalalogging.AnyLogging
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient

trait SubscriptionBot[F[_]]
  extends Commands[F]
    with Callbacks[F]
    with AnyLogging {

  protected def apiFootballClient: ApiFootballClient[F]

  onCommand("subscribe") { implicit msg =>
    withArgs {
      case Seq() =>
        reply("Please, provide a team name you want to subscribe to.").void
      case args =>
        val teamName = args.mkString(" ")
        val result = for {
          teams <- apiFootballClient.searchTeams(teamName)
          _ <- teams match {
            case Seq() =>
              reply(s"Team '$teamName' is not found, please, try another one.")
            case teams if teams.length > 20 =>
              reply(s"Too many teams found, please, try to be more specific.")
            case teams =>
              val buttons = teams.map { team =>
                InlineKeyboardButton.callbackData(team.name, prefixTag("subscribe")(team.id.toString))
              }
              val markup = InlineKeyboardMarkup.singleColumn(buttons)
              reply(s"Please, choose a team you want to subscribe to:", replyMarkup = Some(markup))
          }
        } yield ()

        result.recoverWith {
          case e: Exception =>
            logger.error(e.getMessage, e)
            reply("Error occurred, try again later.").void
        }
    }
  }

  onCallbackWithTag("subscribe") { implicit cbq =>
    val result = for {
      data <- monad.fromOption(cbq.data, new Exception("No data in callback"))
      message <- monad.fromOption(cbq.message, new Exception("No message in callback"))

      teamId <- monad.fromOption(data.toIntOption, new Exception("Invalid team id"))
      chatId = message.chat.id

      teamOpt <- apiFootballClient.getTeamById(teamId)
      team <- monad.fromOption(teamOpt, new Exception(s"No team found for id $teamId"))

      _ <- subscribe(teamId, chatId)
      _ <- ackCallback(Some(s"You have subscribed to '${team.name}'."))
    } yield ()

    result.recoverWith {
      case e: Exception =>
        logger.error(e.getMessage, e)
        ackCallback(Some("Error occurred, try again later."), showAlert = Some(true)).void
    }
  }

  private def subscribe(teamId: Int, chatId: Long): F[Unit] = unit // TODO
}
