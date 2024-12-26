package view

import cats.effect.IO
import cats.syntax.applicative.*
import org.http4s.implicits.{uri}
import scalatags.Text.all.*
import database.Users
import database.Connection
import model.Uri.withError
import model.Request.getForm
import model.Response.*
import model.{User, Request, Response}



object RegisterPage:
  def get(req: Request): Response =
    val message = req.params.get("error")
    page(message).toResponse


  def post(req: Request): IO[Connection[Response]] =
    for form <- req.getForm
    yield for
      users <- Users.findAll
      res <- (form.getFirst("username"), form.getFirst("password")) match
        case (Some(username), Some(password)) =>
          users.collect({ case u: User.Normal => u})
            .find(_.username == username) match
            case Some(_) => Response.redirect(
                uri"/register".withError("Username already exists")
              ).pure[Connection]
            case None => for _ <- Users.insert(Users.Insert.Normal(username, password))
              yield Response.redirect(uri"/")
                .withCookies(
                  "userType" -> "normal",
                  "username" -> username,
                  "password" -> password
                )
        case _ => Response.redirect(uri"/register".withError("Invalid form")).pure[Connection]
    yield res
  def page(message: Option[String]): Frag =
    View.layout(
      div(
        `class` := "container flex flex-col items-center",
        h1(`class` := "text-3xl font-bold mb-8", "Register"),
        message match
          case Some(msg) => h2(`class` := "text-red-500 mb-4", msg)
          case None => (),
          submitForm,
        loginButtons
      )
    )

  private def submitForm: Frag =
    form(
      `class` := "flex",
      method := "post",
      action := "/register",
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
        value := "Register"
      )
    )
  private def loginButtons: Frag =
    div(
      `class` := "mt-4",
      a(
        `class` := "text-blue-500",
        href := "/login",
        "Login here"
      ),
      a(
        `class` := "text-blue-500 ml-4",
        href := "/admin-login",
        "Admin login"
      )
    )
