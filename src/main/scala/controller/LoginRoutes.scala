package controller

import model.*

case object LoginRoutes extends cask.Routes:
  @cask.get("/login")
  def get(request: cask.Request, error: Option[String] = None) =
    request.getUser match
      case Some(_) => cask.Response.seeOther("/")
      case None => view.loginPage(error).toResponse
  
  @cask.postForm("/login")
  def login(username: cask.FormValue, password: cask.FormValue) =
    val normalUser = Users.findUser(username.value, password.value)
    normalUser match
      case Some(_) =>
        cask.Response.seeOther("/")
          .withCookies(
            "userType" -> "normal",
            "username" -> username.value,
            "password" -> password.value
          )
      case None => cask.Response.seeOther("/login?error=Invalid username or password")
  initialize()

