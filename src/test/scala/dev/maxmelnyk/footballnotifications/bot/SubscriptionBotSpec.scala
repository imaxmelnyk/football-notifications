package dev.maxmelnyk.footballnotifications.bot

import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.methods.{AnswerCallbackQuery, SendMessage}
import com.bot4s.telegram.models.{InlineKeyboardMarkup, Message}
import dev.maxmelnyk.footballnotifications.client.apifootball.ApiFootballClient
import dev.maxmelnyk.footballnotifications.client.apifootball.models.Team
import dev.maxmelnyk.footballnotifications.db.dao.SubscriptionsDao
import dev.maxmelnyk.footballnotifications.db.models.Subscription
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class SubscriptionBotSpec extends AnyFlatSpec with Matchers with MockFactory {
  private val subscriptionDaoMock = mock[SubscriptionsDao[Try]]
  private val apiFootballClientMock = mock[ApiFootballClient[Try]]
  private val testClient = mock[TestRequestHandler]
  private val testBot = new TestBot with SubscriptionBot[Try] {
    val subscriptionsDao: SubscriptionsDao[Try] = subscriptionDaoMock
    val apiFootballClient: ApiFootballClient[Try] = apiFootballClientMock
    val client: RequestHandler[Try] = testClient
  }

  private val team: Team = Team(
    id = 47,
    name = "Tottenham Hotspur",
    code = Some("TOT"),
    country = Some("England"),
    founded = Some(1882),
    national = false,
    logo = "https://media.api-sports.io/football/teams/47.png")

  behavior of "Subscription request"

  it should "send team selection (happy path)" in {
    inSequence {
      (apiFootballClientMock.searchTeams(_: String))
        .expects("tottenham")
        .returning(Success(List(team)))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "Please, choose a team you want to subscribe to:" &&
              sendMessage.replyMarkup.exists {
                case InlineKeyboardMarkup(Seq(Seq(button))) =>
                  button.text == team.name &&
                    button.callbackData.contains(s"subscribe${team.id}")
                case _ =>
                  false
              }
          }
        }
        .returning(Success(TestUtils.textMessage()))
        .once()
    }

    testBot.receiveExtMessage((TestUtils.textMessage(Some("/subscribe tottenham")), None))
  }

  it should "send an error if no team specified" in {
    (testClient.apply[Message](_: SendMessage))
      .expects {
        where { sendMessage: SendMessage =>
          sendMessage.text == "Please, provide a team name you want to subscribe to."
        }
      }
      .returning(Success(TestUtils.textMessage()))
      .once()

    testBot.receiveExtMessage((TestUtils.textMessage(Some("/subscribe")), None))
  }

  it should "send an error if team is not found" in {
    inSequence {
      (apiFootballClientMock.searchTeams(_: String))
        .expects("no such team")
        .returning(Success(List()))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "Team 'no such team' is not found, please, try another one."
          }
        }
        .returning(Success(TestUtils.textMessage()))
        .once()
    }

    testBot.receiveExtMessage((TestUtils.textMessage(Some("/subscribe no such team")), None))
  }

  it should "send an error if more than 20 teams found" in {
    inSequence {
      (apiFootballClientMock.searchTeams(_: String))
        .expects("manchester")
        .returning(Success(List.fill(21)(team)))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "Too many teams found, please, try to be more specific."
          }
        }
        .returning(Success(TestUtils.textMessage()))
        .once()
    }

    testBot.receiveExtMessage((TestUtils.textMessage(Some("/subscribe manchester")), None))
  }

  behavior of "Subscription request callback"

  it should "successfully subscribe to the selected team (happy path)" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some(s"subscribe${team.id}"))
    val subscription = Subscription(callbackQuery.message.get.chat.id, team.id)

    inSequence {
      (apiFootballClientMock.getTeamById(_: Int))
        .expects(team.id)
        .returning(Success(Some(team)))
        .once()

      (subscriptionDaoMock.upsert(_: Subscription))
        .expects(subscription)
        .returning(Success(subscription))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains(s"You have subscribed to '${team.name}'.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if there is no source message" in {
    val callbackQuery = TestUtils.callbackQuery(data = Some("subscribe0"))

    inSequence {
      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if data is malformed" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some("subscribebla"))

    inSequence {
      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if such team is not found" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some("subscribe1"))

    inSequence {
      (apiFootballClientMock.getTeamById(_: Int))
        .expects(1)
        .returning(Success(None))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if saving failed" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some(s"subscribe${team.id}"))
    val subscription = Subscription(callbackQuery.message.get.chat.id, team.id)

    inSequence {
      (apiFootballClientMock.getTeamById(_: Int))
        .expects(team.id)
        .returning(Success(Some(team)))
        .once()

      (subscriptionDaoMock.upsert(_: Subscription))
        .expects(subscription)
        .returning(Failure(new Exception("some error")))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  behavior of "Un-subscription request"

  it should "send team selection (happy path)" in {
    val message = TestUtils.textMessage(Some("/unsubscribe"))
    val subscription = Subscription(message.chat.id, team.id)

    inSequence {
      (subscriptionDaoMock.getByChatId(_: Long))
        .expects(message.chat.id)
        .returning(Success(List(subscription)))
        .once()

      (apiFootballClientMock.getTeamById(_: Int))
        .expects(team.id)
        .returning(Success(Some(team)))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "Please, choose a team you want to unsubscribe from:" &&
              sendMessage.replyMarkup.exists {
                case InlineKeyboardMarkup(Seq(Seq(button))) =>
                  button.text == team.name &&
                    button.callbackData.contains(s"unsubscribe${team.id}")
                case _ =>
                  false
              }
          }
        }
        .returning(Success(TestUtils.textMessage()))
        .once()
    }

    testBot.receiveExtMessage((message, None))
  }

  it should "send an error if there are no subscriptions" in {
    val message = TestUtils.textMessage(Some("/unsubscribe"))

    inSequence {
      (subscriptionDaoMock.getByChatId(_: Long))
        .expects(message.chat.id)
        .returning(Success(List()))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "You are not subscribed to any team yet."
          }
        }
        .returning(Success(message))
        .once()
    }

    testBot.receiveExtMessage((message, None))
  }

  it should "send an error if there is a subscription, but team not found" in {
    val message = TestUtils.textMessage(Some("/unsubscribe"))
    val subscription = Subscription(message.chat.id, team.id)

    inSequence {
      (subscriptionDaoMock.getByChatId(_: Long))
        .expects(message.chat.id)
        .returning(Success(List(subscription)))
        .once()

      (apiFootballClientMock.getTeamById(_: Int))
        .expects(team.id)
        .returning(Success(None))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "You are not subscribed to any team yet."
          }
        }
        .returning(Success(message))
        .once()
    }

    testBot.receiveExtMessage((message, None))
  }

  behavior of "Un-subscription request callback"

  it should "successfully un-subscribe from the selected team (happy path)" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some(s"unsubscribe${team.id}"))
    val subscription = Subscription(callbackQuery.message.get.chat.id, team.id)

    inSequence {
      (apiFootballClientMock.getTeamById(_: Int))
        .expects(team.id)
        .returning(Success(Some(team)))
        .once()

      (subscriptionDaoMock.delete(_: Subscription))
        .expects(subscription)
        .returning(Success(()))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains(s"You have unsubscribed from '${team.name}'.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if there is no source message" in {
    val callbackQuery = TestUtils.callbackQuery(data = Some("unsubscribe0"))

    inSequence {
      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if data is malformed" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some("unsubscribebla"))

    inSequence {
      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if such team is not found" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some("unsubscribe1"))

    inSequence {
      (apiFootballClientMock.getTeamById(_: Int))
        .expects(1)
        .returning(Success(None))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }

  it should "send an error if saving failed" in {
    val callbackQuery = TestUtils.callbackQuery(Some(TestUtils.textMessage()), Some(s"unsubscribe${team.id}"))
    val subscription = Subscription(callbackQuery.message.get.chat.id, team.id)

    inSequence {
      (apiFootballClientMock.getTeamById(_: Int))
        .expects(team.id)
        .returning(Success(Some(team)))
        .once()

      (subscriptionDaoMock.delete(_: Subscription))
        .expects(subscription)
        .returning(Failure(new Exception("some error")))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .returning(Success(true))
        .once()
    }

    testBot.receiveCallbackQuery(callbackQuery)
  }
}
