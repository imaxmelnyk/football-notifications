package dev.maxmelnyk.footballnotifications.config

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load().resolve()

  val telegramBotToken: String = config.getString("app.telegram.bot-token")
  val apiFootballApiKey: String = config.getString("app.api-football.api-key")
}
