package view

import cats.effect.IO
import org.http4s.implicits.uri
import org.http4s.HttpRoutes
import scalatags.Text.all.*
import model.*
import database.*
import org.http4s.dsl.io.*

object LoginPage:

  def routes(db: Db): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req @ GET -> Root => get(req).run(db)
    case req @ POST -> Root => post(req).run(db)
  private def get(request: Request): Connection[Response] =
    for users <- Users.findAll
    yield request.getUser(users) match
      case Some(_) => Response.redirect(uri"/")
      case None => page(request.params.get("error")).toResponse
  private def post(request: Request): Connection[Response] =
    for form <- request.getForm.toConnection
      users <- Users.findAll
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
        `class` := "card card-normal bg-base-200 m-16",
        div(
          `class` := "card-body items-center",
          h1(`class` := "card-title", "Login"),
          error match
            case Some(err) => h2(`class` := "alert alert-error", err)
            case None => (),
            formToLogIn,
          linksToOtherPages,
        )
      )
    )
  private def formToLogIn: Frag =
    form(
      `class` := "contents",
      method := "post",
      action := "/login",
      input(
        `class` := "input",
        `type` := "text",
        name := "username",
        placeholder := "Username"
      ),
      input(
        `class` := "input",
        `type` := "password",
        name := "password",
        placeholder := "Password"
      ),
      div(
        `class` := "flex flex-row justify-end w-full",
        input(
          `type` := "submit",
          `class` := "btn btn-primary",
          value := "Login"
        )
      )
    )
  private def linksToOtherPages: Frag =
    div(
      `class` := "card-actions",
      a(
        `class` := "link link-primary",
        href := "/register",
        "Register"
      ),
      a(
        `class` := "link link-primary",
        href := "/admin-login",
        "Admin Login"
      )
    )
  