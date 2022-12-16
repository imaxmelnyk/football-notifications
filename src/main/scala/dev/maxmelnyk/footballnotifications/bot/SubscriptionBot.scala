package dev.maxmelnyk.footballnotifications.bot

import cats.syntax.all._
import com.bot4s.telegram.models.{InlineKeyboardButton, InlineKeyboardMarkup}
import com.typesafe.scalalogging.AnyLogging
import dev.maxmelnyk.footballnotifications.bot.helpers.{Callbacks, Commands}
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient
import dev.maxmelnyk.footballnotifications.db.dao.SubscriptionsDao
import dev.maxmelnyk.footballnotifications.db.models.Subscription

trait SubscriptionBot[F[_]]
  extends Commands[F]
    with Callbacks[F]
    with AnyLogging {

  protected def apiFootballClient: ApiFootballClient[F]

  protected def subscriptionsDao: SubscriptionsDao[F]

  onCommand("subscribe") { implicit msg =>
    withArgs {
      case Seq() =>
        reply("Please, provide a team name you want to subscribe to.").void
      case args =>
        val teamName = args.mkString(" ")
        for {
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
    }
  }

  onCallbackWithTag("subscribe") { implicit cbq =>
    for {
      data <- monad.fromOption(cbq.data, new Exception("No data in callback"))
      message <- monad.fromOption(cbq.message, new Exception("No message in callback"))

      teamId <- monad.fromOption(data.toIntOption, new Exception("Invalid team id"))
      chatId = message.chat.id

      teamOpt <- apiFootballClient.getTeamById(teamId)
      team <- monad.fromOption(teamOpt, new Exception(s"No team found for id $teamId"))

      _ <- subscriptionsDao.upsert(Subscription(chatId, teamId))
      _ <- ackCallback(Some(s"You have subscribed to '${team.name}'."))
    } yield ()
  }

  onCommand("unsubscribe") { implicit msg =>
    for {
      subscriptions <- subscriptionsDao.getByChatId(msg.chat.id)
      teamOpts <- subscriptions.traverse { subscription =>
        apiFootballClient.getTeamById(subscription.teamId)
      }
      teams = teamOpts.flatten
      _ <- teams match {
        case Seq() =>
          reply("You are not subscribed to any team yet.")
        case teams =>
          val buttons = teams.map { team =>
            InlineKeyboardButton.callbackData(team.name, prefixTag("unsubscribe")(team.id.toString))
          }
          val markup = InlineKeyboardMarkup.singleColumn(buttons)
          reply(s"Please, choose a team you want to unsubscribe from:", replyMarkup = Some(markup))
      }
    } yield ()
  }

  onCallbackWithTag("unsubscribe") { implicit cbq =>
    for {
      data <- monad.fromOption(cbq.data, new Exception("No data in callback"))
      message <- monad.fromOption(cbq.message, new Exception("No message in callback"))

      teamId <- monad.fromOption(data.toIntOption, new Exception("Invalid team id"))
      chatId = message.chat.id

      teamOpt <- apiFootballClient.getTeamById(teamId)
      team <- monad.fromOption(teamOpt, new Exception(s"No team found for id $teamId"))

      _ <- subscriptionsDao.delete(Subscription(chatId, teamId))
      _ <- ackCallback(Some(s"You have unsubscribed from '${team.name}'."))
    } yield ()
  }
}
