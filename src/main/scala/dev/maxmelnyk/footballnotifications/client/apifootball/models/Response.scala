package dev.maxmelnyk.footballnotifications.client.apifootball.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, HCursor, Json}

case class Response[R](get: String,
                       parameters: Map[String, String] = Map.empty,
                       errors: Map[String, String] = Map.empty,
                       results: Int,
                       paging: Response.Paging,
                       response: R) {
  def getErrorsMessage: Option[String] = {
    if (errors.nonEmpty) {
      val header = "Errors found in the response from API Football:"
      val result = errors.foldLeft(header) { case (currentMessage, (field, error)) =>
        s"$currentMessage\n$field -> $error"
      }

      Some(result)
    } else {
      Option.empty[String]
    }
  }
}

object Response {
  case class Paging(current: Int,
                    total: Int)

  object Paging {
    implicit val pagingDecoder: Decoder[Paging] = deriveDecoder[Paging]
    implicit val pagingEncoder: Encoder[Paging] = deriveEncoder[Paging]
  }

  // Here we have custom decoder and encoder because
  // `parameters` and `errors` fields not always have the same type:
  // in case of the data present in there it's an object, otherwise it's an empty array,
  // thus, we need to handle these cases by ourselves.

  def responseDecoder[R: Decoder]: Decoder[Response[R]] = (cursor: HCursor) => {
    val parametersDecodeResult = cursor
      .downField("parameters")
      .as[Map[String, String]]
      .orElse(Right(Map.empty[String, String]))

    val errorsDecodeResult = cursor
      .downField("errors")
      .as[Map[String, String]]
      .orElse(Right(Map.empty[String, String]))

    for {
      get <- cursor.downField("get").as[String]
      parameters <- parametersDecodeResult
      errors <- errorsDecodeResult
      results <- cursor.downField("results").as[Int]
      paging <- cursor.downField("paging").as[Paging]
      response <- cursor.downField("response").as[R]
    } yield {
      Response(get, parameters, errors, results, paging, response)
    }
  }

  def responseEncoder[R: Encoder]: Encoder[Response[R]] = (response: Response[R]) => {
    val parametersJson = response.parameters match {
      case parameters if parameters.nonEmpty => parameters.asJson
      case _ => Json.arr()
    }

    val errorsJson = response.errors match {
      case errors if errors.nonEmpty => errors.asJson
      case _ => Json.arr()
    }

    Json.obj(
      "get" -> response.get.asJson,
      "parameters" -> parametersJson,
      "errors" -> errorsJson,
      "results" -> response.results.asJson,
      "paging" -> response.paging.asJson,
      "response" -> response.response.asJson)
  }
}
