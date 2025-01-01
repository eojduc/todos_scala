package model


case class Quote(q: String, a: String)


object Quote:
  def firstOrDefault(quotes: List[Quote]): Quote = quotes
    .headOption.getOrElse(Quote("Repetitio est mater studiorum", "Latin Proverb"))
    
    