package dev.maxmelnyk.footballnotifications.client.apifootball

import dev.maxmelnyk.footballnotifications.client.apifootball.models._
import dev.maxmelnyk.footballnotifications.config.Config
import cats.effect.Async
import cats.syntax.all._
import com.typesafe.scalalogging.LazyLogging
import io.circe.Decoder
import io.circe.parser.decode
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import sttp.model.Uri

trait ApiFootballClient[F[_]] {
  def searchTeams(query: String): F[Either[Exception, Seq[Team]]]
}

object ApiFootballClient {
  def apply[F[_]: Async](sttpBackend: SttpBackend[F, Any]): ApiFootballClient[F] = {
    new DefaultApiFootballClient[F](sttpBackend)
  }
}


private class DefaultApiFootballClient[F[_]: Async](sttpBackend: SttpBackend[F, Any])
  extends ApiFootballClient[F]
    with LazyLogging {

  import DefaultApiFootballClient._

  def searchTeams(query: String): F[Either[Exception, Seq[Team]]] = {
    request[Seq[TeamWithVenue]](uri"$baseUrl/teams?search=$query").map {
      case Right(teamsWithVenues) => Right(teamsWithVenues.map(_.team))
      case Left(error) => Left(error)
    }
  }

  private def request[R: Decoder](uri: Uri): F[Either[Exception, R]] = {
    basicRequest
      .get(uri)
      .header(authHeader, Config.apiFootballApiKey)
      .mapResponse {
        case Right(responseBodyStr) =>
          decode[Response[R]](responseBodyStr)(Response.responseDecoder) match {
            case Right(response) =>
              response.getErrorsMessage.foreach(logger.info(_))
              Right(response.response)
            case Left(error) =>
              logger.error(s"Failed to parse response from API Football: $error")
              Left(new Exception(s"Failed to decode response body: $responseBodyStr", error))
          }
        case Left(responseBodyStr) =>
          decode[ErrorResponse](responseBodyStr) match {
            case Right(errorResponse) => Left(new Exception(errorResponse.message))
            case Left(error) => Left(new Exception(s"Failed to decode error response body: $responseBodyStr", error))
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
