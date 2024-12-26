package view

import cats.effect.IO
import model.{User, Response, Request}
import model.Uri.withError
import org.http4s.implicits.{path, uri}
import org.http4s.{Headers, Query, Status, Uri, UrlForm}
import scalatags.Text.all.*
import model.Request.*
import model.Response.*
import database.Connection
import database.Users


    
    
object LoginPage:
  def get(req: Request): Connection[Response] =
    for users <- Users.findAll
    yield req.getUser(users) match
      case Some(_) => Response.redirect(uri"/")
      case None => page(req.params.get("error")).toResponse
  def post(request: Request): IO[Connection[Response]] =
    for form <- request.getForm
    yield for users <- Users.findAll
    yield (form.getFirst("username"), form.getFirst("password")) match
      case (Some(username), Some(password)) =>
        users.findUser(username, password) match
          case None => Response.redirect(uri"/login".withError("Invalid username or password"))
          case Some(user) =>
            Response.redirect(uri"/")
              .withCookies(
                "userType" -> "normal",
                "username" -> user.username,
                "password" -> user.password
              )
      case _ => Response.badRequest("Invalid form data")
  
  private def page(error: Option[String]): Frag =
    View.layout(
      div(
        `class` := "container flex flex-col items-center",
        h1(`class` := "text-3xl font-bold mb-8", "Login"),
        error match
          case Some(err) => h2(`class` := "text-red-500 mb-4", err)
          case None => (),
          formToLogIn,
        linksToOtherPages,
      )
    )
  private def formToLogIn: Frag =
    form(
      `class` := "flex",
      method := "post",
      action := "/login",
      input(
        `class` := "p-2 border rounded-lg",
        `type` := "text",
        name := "username",
        placeholder := "Username"
      ),
      input(
        `class` := "p-2 border rounded-lg ml-2",
        `type` := "password",
        name := "password",
        placeholder := "Password"
      ),
      input(
        `type` := "submit",
        `class` := "p-2 bg-blue-500 text-white rounded-lg ml-2",
        value := "Login"
      )
    )
  private def linksToOtherPages: Frag =
    div(
      `class` := "mt-4",
      a(
        `class` := "text-blue-500",
        href := "/register",
        "Register here"
      ),
      a(
        `class` := "text-blue-500 ml-4",
        href := "/admin-login",
        "Admin login"
      )
    )
  