package model


extension (request: cask.Request)
  def getUser: Option[User] =
    request.getCookie("userType") match
      case Some("normal") =>
        for
          username <- request.getCookie("username")
          password <- request.getCookie("password")
          user <- Users.findUser(username, password)
        yield user
      case Some("admin") =>
        for
          code <- request.getCookie("code")
          user <- Users.getAdminByCode(code)
        yield user
      case _ => None
      
    
  def getCookie(name: String): Option[String] =
    request.cookies.get(name).map(_.value)