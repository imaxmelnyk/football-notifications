package dev.maxmelnyk.footballnotifications.client.apifootball

import cats.MonadThrow
import cats.syntax.all._
import com.typesafe.scalalogging.LazyLogging
import dev.maxmelnyk.footballnotifications.client.apifootball.models._
import dev.maxmelnyk.footballnotifications.config.Config
import io.circe.Decoder
import io.circe.parser.decode
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

trait ApiFootballClient[F[_]] {
  def searchTeams(query: String): F[Seq[Team]]

  def getTeamById(id: Int): F[Option[Team]]
}

object ApiFootballClient {
  def apply[F[_] : MonadThrow](sttpBackend: SttpBackend[F, Any]): ApiFootballClient[F] = {
    new DefaultApiFootballClient[F](sttpBackend)
  }
}


private class DefaultApiFootballClient[F[_] : MonadThrow](sttpBackend: SttpBackend[F, Any])
  extends ApiFootballClient[F]
    with LazyLogging {

  import DefaultApiFootballClient._

  def searchTeams(query: String): F[Seq[Team]] = {
    request[Seq[TeamWithVenue]](uri"$baseUrl/teams?search=$query").map { teamsWithVenues =>
      teamsWithVenues.map(_.team)
    }
  }

  def getTeamById(id: Int): F[Option[Team]] = {
    request[Seq[TeamWithVenue]](uri"$baseUrl/teams?id=$id").map {
      case Seq() => None
      case Seq(teamsWithVenue) => Some(teamsWithVenue.team)
      case _ => throw new Exception(s"More than one team found for id $id")
    }
  }

  private def request[R: Decoder](uri: Uri): F[R] = {
    basicRequest
      .get(uri)
      .header(authHeader, Config.apiFootballApiKey)
      .mapResponse {
        case Left(responseBodyStr) =>
          decode[ErrorResponse](responseBodyStr) match {
            case Left(error) => throw new Exception(s"Failed to decode error response body: $responseBodyStr", error)
            case Right(errorResponse) => throw new Exception(errorResponse.message)
          }
        case Right(responseBodyStr) =>
          decode[Response[R]](responseBodyStr)(Response.responseDecoder) match {
            case Left(error) => throw new Exception(s"Failed to decode response body: $responseBodyStr", error)
            case Right(response) =>
              response.getErrorsMessage.foreach(logger.info(_: String))
              response.response
          }
      }
      .send(sttpBackend)
      .map(_.body)
  }
}

private object DefaultApiFootballClient {
  private val baseUrl = "https://v3.football.api-sports.io"
  private val authHeader = "x-apisports-key"
}
