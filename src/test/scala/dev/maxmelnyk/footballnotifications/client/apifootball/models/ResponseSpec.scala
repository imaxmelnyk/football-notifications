package dev.maxmelnyk.footballnotifications.client.apifootball.models

import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser.decode
import io.circe.syntax._

class ResponseSpec extends AnyFlatSpec with Matchers {
  "Football API Response" should "be decoded correctly in case of errors" in {
    val json =
      """
        |{
        |  "get": "teams",
        |  "parameters": {
        |    "search": "Non-existent team"
        |  },
        |  "errors": {
        |    "search": "Invalid search parameter"
        |  },
        |  "results": 0,
        |  "paging": {
        |    "current": 1,
        |    "total": 1
        |  },
        |  "response": []
        |}
        |""".stripMargin

    val expected = Response[Seq[String]](
      get = "teams",
      parameters = Map("search" -> "Non-existent team"),
      errors = Map("search" -> "Invalid search parameter"),
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val actual = decode[Response[Seq[String]]](json)(Response.responseDecoder)

    actual shouldEqual Right(expected)
  }

  it should "be decoded correctly in case of no errors" in {
    val json =
      """
        |{
        |  "get": "teams",
        |  "parameters": [],
        |  "errors": [],
        |  "results": 0,
        |  "paging": {
        |    "current": 1,
        |    "total": 1
        |  },
        |  "response": []
        |}
        |""".stripMargin

    val expected = Response[Seq[String]](
      get = "teams",
      parameters = Map.empty,
      errors = Map.empty,
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val actual = decode[Response[Seq[String]]](json)(Response.responseDecoder)

    actual shouldEqual Right(expected)
  }

  it should "be encoded correctly in case of errors" in {
    val response = Response[Seq[String]](
      get = "teams",
      parameters = Map("search" -> "Non-existent team"),
      errors = Map("search" -> "Invalid search parameter"),
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val expected = Json.obj(
      "get" -> Json.fromString("teams"),
      "parameters" -> Json.obj(
        "search" -> Json.fromString("Non-existent team")),
      "errors" -> Json.obj(
        "search" -> Json.fromString("Invalid search parameter")),
      "results" -> Json.fromInt(0),
      "paging" -> Json.obj(
        "current" -> Json.fromInt(1),
        "total" -> Json.fromInt(1)),
      "response" -> Json.arr())

    val actual = response.asJson(Response.responseEncoder)

    actual shouldEqual expected
  }

  it should "be encoded correctly in case of no errors" in {
    val response = Response[Seq[String]](
      get = "teams",
      parameters = Map.empty,
      errors = Map.empty,
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val expected = Json.obj(
      "get" -> Json.fromString("teams"),
      "parameters" -> Json.arr(),
      "errors" -> Json.arr(),
      "results" -> Json.fromInt(0),
      "paging" -> Json.obj(
        "current" -> Json.fromInt(1),
        "total" -> Json.fromInt(1)),
      "response" -> Json.arr())

    val actual = response.asJson(Response.responseEncoder)

    actual shouldEqual expected
  }

  it should "generate correct error message (1 error)" in {
    val response = Response[Seq[String]](
      get = "teams",
      parameters = Map.empty,
      errors = Map("search" -> "Invalid search parameter"),
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val expected = Some("Errors found in the response from API Football:\n" +
      "search -> Invalid search parameter")

    val actual = response.getErrorsMessage

    actual shouldEqual expected
  }

  it should "generate correct error message (no errors)" in {
    val response = Response[Seq[String]](
      get = "teams",
      parameters = Map.empty,
      errors = Map.empty,
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val expected = None

    val actual = response.getErrorsMessage

    actual shouldEqual expected
  }

  it should "generate correct error message (multiple errors)" in {
    val response = Response[Seq[String]](
      get = "teams",
      parameters = Map.empty,
      errors = Map(
        "search" -> "Invalid search parameter",
        "league" -> "Invalid league parameter"),
      results = 0,
      paging = Response.Paging(current = 1, total = 1),
      response = Seq.empty)

    val expected = Some("Errors found in the response from API Football:\n" +
      "search -> Invalid search parameter\n" +
      "league -> Invalid league parameter")

    val actual = response.getErrorsMessage

    actual shouldEqual expected
  }
}
