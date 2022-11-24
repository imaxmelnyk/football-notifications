package dev.maxmelnyk.footballnotifications.client.apifootball.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

case class Venue(id: Option[Int],
                 name: Option[String],
                 address: Option[String],
                 city: Option[String],
                 capacity: Option[Int],
                 surface: Option[String],
                 image: Option[String])

object Venue {
  implicit val venueDecoder: Decoder[Venue] = deriveDecoder[Venue]
  implicit val venueEncoder: Encoder[Venue] = deriveEncoder[Venue]
}
