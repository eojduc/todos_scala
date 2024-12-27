package model


case class Quote(q: String, a: String)



object Quote:
  def firstOrDefault(quotes: List[Quote]): Quote = quotes.headOption.getOrElse(Quote("No quote found", "No author found"))
    
    