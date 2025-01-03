package model

extension [T](response: cask.Response[T])
  def withCookies(cookies: (String, String)*) = response.copy(
    cookies = response.cookies ++ cookies.map((c, v) => cask.Cookie(c, v))
  )
  def removeCookies(cookies: String*) =
    response.copy(
      cookies = response.cookies.filter(c => !cookies.contains(c.name)) ++
        cookies.map(c => cask.Cookie(c, "", expires = java.time.Instant.ofEpochSecond(0)))
    )


extension (res: cask.Response.type)
  def seeOther(location: String) = cask.Response("", statusCode = 303, headers = Seq("Location" -> location))
