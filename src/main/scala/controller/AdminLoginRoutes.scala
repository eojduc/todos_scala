package controller

import model.*

case object AdminLoginRoutes extends cask.Routes:

  @cask.get("/admin-login")
  def get(request: cask.Request, error: Option[String] = None) =
    request.getUser match
      case Some(_) => cask.Response.seeOther("/")
      case None => view.adminLoginPage(error).toResponse

  @cask.postForm("/admin-login")
  def login(code: cask.FormValue) =
    Users.getAdminByCode(code.value) match
      case Some(_) =>
        cask.Response.seeOther("/")
          .withCookies("userType" -> "admin", "code" -> code.value)
      case None => cask.Response.seeOther("/admin-login?error=Invalid code")

  initialize()

