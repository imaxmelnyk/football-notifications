package dev.maxmelnyk.footballnotifications.db.dao

import cats.effect.MonadCancelThrow
import cats.syntax.all._
import dev.maxmelnyk.footballnotifications.db.models.Subscription
import doobie.implicits._
import doobie.util.transactor.Transactor

trait SubscriptionsDao[F[_]] {
  def upsert(subscription: Subscription): F[Subscription]

  def getByChatId(chatId: Long): F[Seq[Subscription]]

  def delete(subscription: Subscription): F[Unit]
}

object SubscriptionsDao {
  def apply[F[_]](transactor: Transactor[F])
                 (implicit monad: MonadCancelThrow[F]): SubscriptionsDao[F] = {
    new DefaultSubscriptionsDao(transactor)
  }
}


private class DefaultSubscriptionsDao[F[_]](transactor: Transactor[F])
                                           (implicit monad: MonadCancelThrow[F])
  extends SubscriptionsDao[F] {

  def upsert(subscription: Subscription): F[Subscription] = {
    val existsQuery =
      sql"""
           |select exists (
           |  select 1
           |  from subscriptions
           |  where chat_id = ${subscription.chatId} and team_id = ${subscription.teamId}
           |);
           |""".stripMargin

    val insertQuery =
      sql"""
           |insert into subscriptions (chat_id, team_id)
           |values (${subscription.chatId}, ${subscription.teamId});
           |""".stripMargin

    val result = for {
      exists <- existsQuery.query[Boolean].unique
      _ <- insertQuery.update.run.unlessA(exists)
    } yield subscription

    result.transact(transactor)
  }

  def getByChatId(chatId: Long): F[Seq[Subscription]] = {
    val getQuery =
      sql"""
           |select chat_id, team_id
           |from subscriptions
           |where chat_id = $chatId;
           |""".stripMargin

    getQuery.query[Subscription].to[Seq].transact(transactor)
  }

  def delete(subscription: Subscription): F[Unit] = {
    val deleteQuery =
      sql"""
           |delete from subscriptions
           |where chat_id = ${subscription.chatId} and team_id = ${subscription.teamId};
           |""".stripMargin

    deleteQuery.update.run.transact(transactor).void
  }
}
