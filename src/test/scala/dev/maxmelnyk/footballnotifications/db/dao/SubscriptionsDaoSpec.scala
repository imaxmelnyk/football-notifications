package dev.maxmelnyk.footballnotifications.db.dao

import cats.effect.IO
import dev.maxmelnyk.footballnotifications.db.DatabaseSpec
import dev.maxmelnyk.footballnotifications.db.models.Subscription
import doobie.implicits._
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers

class SubscriptionsDaoSpec extends AsyncFlatSpec with DatabaseSpec with Matchers {
  private var dao: SubscriptionsDao[IO] = _

  override def beforeEach(): Unit = {
    super.beforeEach()

    dao = SubscriptionsDao(transactor)
  }


  "Subscriptions DAO" should "upsert (create)" in {
    val subscription = Subscription(1, 1)

    for {
      _ <- dao.upsert(subscription)
      subscriptions <- getSubscriptions
    } yield {
      subscriptions shouldEqual List(subscription)
    }
  }

  it should "upsert (update)" in {
    val subscription = Subscription(1, 1)

    for {
      _ <- insertSubscription(subscription)
      _ <- dao.upsert(subscription)
      subscriptions <- getSubscriptions
    } yield {
      subscriptions shouldEqual List(subscription)
    }
  }

  it should "get by chat id" in {
    val subscription1 = Subscription(1, 1)
    val subscription2 = Subscription(1, 2)
    val subscription3 = Subscription(2, 1)

    for {
      _ <- insertSubscription(subscription1)
      _ <- insertSubscription(subscription2)
      _ <- insertSubscription(subscription3)
      subscriptions <- dao.getByChatId(1)
    } yield {
      subscriptions shouldEqual List(subscription1, subscription2)
    }
  }

  it should "delete" in {
    val subscription1 = Subscription(1, 1)
    val subscription2 = Subscription(1, 2)

    for {
      _ <- insertSubscription(subscription1)
      _ <- insertSubscription(subscription2)
      _ <- dao.delete(subscription1)
      subscriptions <- getSubscriptions
    } yield {
      subscriptions shouldEqual List(subscription2)
    }
  }

  private def getSubscriptions: IO[Seq[Subscription]] = {
    val query = sql"select * from subscriptions;"
    query.query[Subscription].to[List].transact(transactor)
  }

  private def insertSubscription(subscription: Subscription): IO[Unit] = {
    val query =
      sql"""
           |insert into subscriptions (chat_id, team_id)
           |values (${subscription.chatId}, ${subscription.teamId});
           |""".stripMargin
    query.update.run.transact(transactor).void
  }
}
