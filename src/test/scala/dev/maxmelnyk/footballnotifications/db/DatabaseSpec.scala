package dev.maxmelnyk.footballnotifications.db

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import doobie.util.transactor.Transactor
import org.flywaydb.core.Flyway
import org.h2.Driver
import org.scalatest.{AsyncTestSuite, BeforeAndAfterEach}

trait DatabaseSpec extends AsyncIOSpec with BeforeAndAfterEach {
  this: AsyncTestSuite =>

  protected var transactor: Transactor[IO] = _
  private var flyway: Flyway = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    val dbUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    val dbUser = "sa"
    val dbPassword = ""

    transactor = Transactor.fromDriverManager[IO](classOf[Driver].getName, dbUrl, dbUser, dbPassword)

    flyway = Flyway.configure()
      .dataSource(dbUrl, dbUser, dbPassword)
      .locations("migrations")
      .cleanDisabled(false)
      .load()

    flyway.migrate()
  }

  override def afterEach(): Unit = {
    flyway.clean()

    super.afterEach()
  }
}
