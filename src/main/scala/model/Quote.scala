package model

import cats.effect.IO
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import org.http4s.implicits.uri
import model.Client
import io.circe.generic.auto.{deriveDecoder}


case class Quote(q: String, a: String)
given EntityDecoder[IO, List[Quote]] = jsonOf[IO, List[Quote]]



object Quote:
  def getOne(client: Client): IO[Quote] =
    for quotes <- client.expect[List[Quote]](uri"https://zenquotes.io/api/random")
    yield quotes.headOption.getOrElse(Quote("No quote found", "No author found"))
    
    