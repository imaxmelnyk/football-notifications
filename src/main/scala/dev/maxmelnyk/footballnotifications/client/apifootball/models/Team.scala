package dev.maxmelnyk.footballnotifications.client.apifootball.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Team(id: Int,
                name: String,
                code: Option[String],
                country: Option[String],
                founded: Option[Int],
                national: Boolean,
                logo: String)

object Team {
  implicit val teamDecoder: Decoder[Team] = deriveDecoder[Team]
  implicit val teamEncoder: Encoder[Team] = deriveEncoder[Team]
}
