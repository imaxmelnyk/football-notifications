package dev.maxmelnyk.footballnotifications.db

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.h2.Driver
import org.scalatest.{AsyncTestSuite, BeforeAndAfterEach}

trait DatabaseSpec extends AsyncIOSpec with BeforeAndAfterEach {
  this: AsyncTestSuite =>

  private val dbUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  private val dbUser = "sa"
  private val dbPassword = ""

  protected lazy val transactor: Transactor[IO] = {
    Transactor.fromDriverManager[IO](
      classOf[Driver].getName,
      dbUrl,
      dbUser,
      dbPassword)
  }

  private lazy val flyway: Flyway =  {
    Flyway.configure()
      .dataSource(dbUrl, dbUser, dbPassword)
      .locations("migrations")
      .cleanDisabled(false)
      .load()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()

    flyway.clean()
    flyway.migrate()
  }
}
