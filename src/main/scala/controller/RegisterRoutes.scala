package controller

import model.*

case object RegisterRoutes extends cask.Routes:
  @cask.get("/register")
  def getRegister(error: Option[String] = None, request: cask.Request) =
    request.getUser match
      case Some(user) => cask.Response.seeOther("/")
      case None => view.registerPage(error).toResponse

  @cask.postForm("/register")
  def postRegister(username: cask.FormValue, password: cask.FormValue) =
    if username.value.isBlank || password.value.isBlank then
      view.registerPage(Some("Username and password cannot be empty")).toResponse
    else
      val existingUser = Users.findUserByUsername(username.value)
      existingUser match
        case None =>
          Users.insertUser(username.value, password.value)
          cask.Response.seeOther("/")
            .withCookies(
              "userType" -> "normal",
              "username" -> username.value,
              "password" -> password.value
            )
        case Some(_) => view.registerPage(Some("User already exists")).toResponse
  initialize()
