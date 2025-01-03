package model

import upickle.default as upickle

case class Quote(q: String, a: String) derives upickle.ReadWriter


def getQuote(): Quote =
  val json = requests.get("https://zenquotes.io/api/random").text()
  val quotes = upickle.read[List[Quote]](json)
  quotes.headOption.getOrElse(Quote("Repetitio est mater studiorum", "Latin Proverb"))

