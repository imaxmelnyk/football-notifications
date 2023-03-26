package dev.maxmelnyk.footballnotifications.bot.helpers

import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.methods.SendMessage
import com.bot4s.telegram.models.Message
import dev.maxmelnyk.footballnotifications.bot.TestUtils.textMessage
import dev.maxmelnyk.footballnotifications.bot.{TestBot, TestRequestHandler}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success, Try}

class CommandsSpec extends AnyFlatSpec with Matchers with MockFactory {
  private val testHandler = mockFunction[Message, Try[Unit]]
  private val testClient = mock[TestRequestHandler]
  private val testBot = new TestBot with Commands[Try] {
    val client: RequestHandler[Try] = testClient
    onCommand("/test")(testHandler)
  }

  "Commands" should "not reply error in case of success" in {
    inSequence {
      testHandler
        .expects(*)
        .returning(Success(()))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects(*)
        .never()
    }

    testBot.receiveExtMessage((textMessage("/test"), None))
  }

  it should "reply error in case of failure" in {
    inSequence {
      testHandler
        .expects(*)
        .returning(Failure(new Exception("testing")))
        .once()

      (testClient.apply[Message](_: SendMessage))
        .expects {
          where { sendMessage: SendMessage =>
            sendMessage.text == "Error occurred, try again later."
          }
        }
        .atLeastOnce()
    }

    testBot.receiveExtMessage((textMessage("/test"), None))
  }
}
