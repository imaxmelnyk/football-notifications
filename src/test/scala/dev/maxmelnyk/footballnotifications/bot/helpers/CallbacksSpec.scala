package dev.maxmelnyk.footballnotifications.bot.helpers

import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.methods.AnswerCallbackQuery
import com.bot4s.telegram.models.CallbackQuery
import dev.maxmelnyk.footballnotifications.bot.{TestBot, TestRequestHandler, TestUtils}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class CallbacksSpec extends AnyFlatSpec with Matchers with MockFactory {
  private val testHandler = mockFunction[CallbackQuery, Try[Unit]]
  private val testClient = mock[TestRequestHandler]
  private val testBot = new TestBot with Callbacks[Try] {
    val client: RequestHandler[Try] = testClient
    onCallbackQuery(testHandler)
  }

  "Callbacks" should "not reply error in case of success" in {
    inSequence {
      testHandler
        .expects(*)
        .returning(Success(()))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects(*)
        .never()
    }

    testBot.receiveCallbackQuery(TestUtils.callbackQuery())
  }

  it should "reply error in case of failure" in {
    inSequence {
      testHandler
        .expects(*)
        .returning(Failure(new Exception("testing")))
        .once()

      (testClient.apply[Boolean](_: AnswerCallbackQuery))
        .expects {
          where { answerCallbackQuery: AnswerCallbackQuery =>
            answerCallbackQuery.text.contains("Error occurred, try again later.")
          }
        }
        .atLeastOnce()
    }

    testBot.receiveCallbackQuery(TestUtils.callbackQuery())
  }
}
