package dev.maxmelnyk.footballnotifications.config

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load().resolve()

  val telegramBotToken: String = config.getString("app.telegram.bot-token")
  val apiFootballApiKey: String = config.getString("app.api-football.api-key")

  val dbUrl: String = config.getString("app.db.url")
  val dbUser: String = config.getString("app.db.user")
  val dbPassword: String = config.getString("app.db.password")
  val dbConnectionPoolSize: Int = config.getInt("app.db.connection-pool-size")
}
