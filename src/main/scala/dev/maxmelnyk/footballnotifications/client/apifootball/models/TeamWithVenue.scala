package dev.maxmelnyk.footballnotifications.client.apifootball.models

import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

case class TeamWithVenue(team: Team,
                         venue: Venue)

object TeamWithVenue {
  implicit val teamWithVenueDecoder: Decoder[TeamWithVenue] = deriveDecoder[TeamWithVenue]
  implicit val teamWithVenueEncoder: Encoder[TeamWithVenue] = deriveEncoder[TeamWithVenue]
}
