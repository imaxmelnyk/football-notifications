package dev.maxmelnyk.footballnotifications.bot

import com.bot4s.telegram.models.{CallbackQuery, Chat, ChatType, Message, User}

object TestUtils {
  def textMessage(text: String): Message = {
    Message(
      messageId = 0,
      chat = Chat(0, ChatType.Private),
      date = 0,
      text = Some(text))
  }

  def callbackQuery(message: Option[Message] = None,
                    data: Option[String] = None): CallbackQuery = {
    CallbackQuery(
      id = "test",
      from = User(0, isBot = false, "test"),
      message = message,
      chatInstance = "test",
      data = data)
  }
}
