package dev.maxmelnyk.footballnotifications.client.apifootball

import dev.maxmelnyk.footballnotifications.client.apifootball.models._
import io.circe.Decoder
import io.circe.syntax._
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.monad.IdMonad
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{Identity, UriContext, Response => SttpResponse}
import sttp.model.{StatusCode, Uri}

class ApiFootballClientSpec extends AnyFlatSpec with Matchers with PrivateMethodTester {
  private val team: Team = Team(
    id = 47,
    name = "Tottenham Hotspur",
    code = Some("TOT"),
    country = Some("England"),
    founded = Some(1882),
    national = false,
    logo = "https://media.api-sports.io/football/teams/47.png")

  private val venue: Venue = Venue(
    id = Some(593),
    name = Some("Tottenham Hotspur Stadium"),
    address = Some("Bill Nicholson Way, 748 High Road"),
    city = Some("London"),
    capacity = Some(62850),
    surface = Some("grass"),
    image = Some("https://media.api-sports.io/football/venues/593.png"))

  private def apiFootballClientRequest[F[_], R: Decoder](client: ApiFootballClient[F], uri: Uri): String = {
    val request = PrivateMethod[String](Symbol("request"))
    client invokePrivate request(uri, implicitly[Decoder[R]])
  }


  "Football API Client" should "search teams (some results)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenRequestMatches { request =>
        request.uri.path == List("teams") &&
          request.uri.params.get("search").contains("tottenham")
      }
      .thenRespond {
        val body = Response(
          get = "teams",
          parameters = Map("search" -> "tottenham"),
          errors = Map.empty,
          results = 1,
          paging = Response.Paging(current = 1, total = 1),
          response = List(TeamWithVenue(team, venue)))

        SttpResponse(body.asJson(Response.responseEncoder).noSpaces, StatusCode.Ok)
      }

    val client = ApiFootballClient(sttpBackend)
    val actual = client.searchTeams("tottenham")

    actual shouldEqual List(team)
    assert(false)
  }

  it should "search teams (no results)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenRequestMatches { request =>
        request.uri.path == List("teams") &&
          request.uri.params.get("search").contains("no such team")
      }
      .thenRespond {
        val body = Response(
          get = "teams",
          parameters = Map("search" -> "no such team"),
          errors = Map.empty,
          results = 1,
          paging = Response.Paging(current = 1, total = 1),
          response = List.empty[TeamWithVenue])

        SttpResponse(body.asJson(Response.responseEncoder).noSpaces, StatusCode.Ok)
      }

    val client = ApiFootballClient(sttpBackend)
    val actual = client.searchTeams("no such team")

    actual shouldEqual List.empty
  }

  it should "get team by id (exists)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenRequestMatches { request =>
        request.uri.path == List("teams") &&
          request.uri.params.get("id").contains("47")
      }
      .thenRespond {
        val body = Response(
          get = "teams",
          parameters = Map("id" -> "47"),
          errors = Map.empty,
          results = 1,
          paging = Response.Paging(current = 1, total = 1),
          response = List(TeamWithVenue(team, venue)))

        SttpResponse(body.asJson(Response.responseEncoder).noSpaces, StatusCode.Ok)
      }

    val client = ApiFootballClient(sttpBackend)
    val actual = client.getTeamById(47)

    actual shouldEqual Some(team)
  }

  it should "get team by id (does not exist)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenRequestMatches { request =>
        request.uri.path == List("teams") &&
          request.uri.params.get("id").contains("999")
      }
      .thenRespond {
        val body = Response(
          get = "teams",
          parameters = Map("id" -> "999"),
          errors = Map.empty,
          results = 1,
          paging = Response.Paging(current = 1, total = 1),
          response = List.empty[TeamWithVenue])

        SttpResponse(body.asJson(Response.responseEncoder).noSpaces, StatusCode.Ok)
      }

    val client = ApiFootballClient(sttpBackend)
    val actual = client.getTeamById(999)

    actual shouldEqual None
  }

  it should "fail to get team by id (multiple results)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenRequestMatches { request =>
        request.uri.path == List("teams") &&
          request.uri.params.get("id").contains("47")
      }
      .thenRespond {
        val body = Response(
          get = "teams",
          parameters = Map("id" -> "47"),
          errors = Map.empty,
          results = 1,
          paging = Response.Paging(current = 1, total = 1),
          response = List(TeamWithVenue(team, venue), TeamWithVenue(team, venue)))

        SttpResponse(body.asJson(Response.responseEncoder).noSpaces, StatusCode.Ok)
      }

    val client = ApiFootballClient(sttpBackend)

    val exception = intercept[Exception](client.getTeamById(47))
    exception.getMessage shouldEqual "More than one team found for id 47"
  }

  it should "fail request when its unsuccessful (error during request)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenAnyRequest
      .thenRespondF(IdMonad.error[SttpResponse[String]](new Exception("some error")))

    val client = ApiFootballClient(sttpBackend)

    val exception = intercept[Exception] {
      apiFootballClientRequest[Identity, String](client, uri"https://test.dev/whatever")
    }
    exception.getMessage shouldEqual "some error"
  }

  it should "fail request when its unsuccessful (parse response)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenAnyRequest
      .thenRespond(SttpResponse("{}", StatusCode.Ok))

    val client = ApiFootballClient(sttpBackend)

    val exception = intercept[Exception] {
      apiFootballClientRequest[Identity, String](client, uri"https://test.dev/whatever")
    }
    exception.getMessage should startWith("Failed to decode response body")
  }

  it should "fail request when its unsuccessful (error status)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenAnyRequest
      .thenRespond(SttpResponse(ErrorResponse("some error").asJson.noSpaces, StatusCode.BadRequest))

    val client = ApiFootballClient(sttpBackend)

    val exception = intercept[Exception] {
      apiFootballClientRequest[Identity, String](client, uri"https://test.dev/whatever")
    }
    exception.getMessage shouldEqual "some error"
  }

  it should "fail request when its unsuccessful (parse error response)" in {
    val sttpBackend = SttpBackendStub.synchronous
      .whenAnyRequest
      .thenRespond(SttpResponse("{}", StatusCode.BadRequest))

    val client = ApiFootballClient(sttpBackend)

    val exception = intercept[Exception] {
      apiFootballClientRequest[Identity, String](client, uri"https://test.dev/whatever")
    }
    exception.getMessage should startWith("Failed to decode error response body")
  }
}
