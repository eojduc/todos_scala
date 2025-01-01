package view

import cats.effect.IO
import cats.syntax.applicative.given
import scalatags.Text.all.*
import org.http4s.implicits.uri
import database.Users
import database.Connection
import model.{Request, Response, User}
import model.Response.{withCookies}
import model.Frag.toResponse
import org.http4s.HttpRoutes
import org.http4s.dsl.io.{->, GET, POST, Root}
import database.Db
import util.*
import model.Request.getForm
import model.Uri



object RegisterPage:
  def routes(db: Db): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req @ GET -> Root => get(req).pure[IO]
    case req @ POST -> Root => post(req).run(db)
    
  private def get(request: Request): Response = page(request.params.get("error")).toResponse


  private def post(request: Request): Connection[Response] =
    for form <- Connection.fromIO(request.getForm)
      users <- Users.findAll
      response <- (form.getFirst("username"), form.getFirst("password")) match
        case (Some(username), Some(password)) =>
          val existingUser = users.collect({ case u: User.Normal => u})
            .find(_.username == username)
          existingUser match
            case Some(_) => Response.redirect(uri"/register" |> Uri.withError("Username already exists")).pure[Connection]
            case None => 
              for _ <- Users.insert(Users.Insert.Normal(username, password))
              yield Response.redirect(uri"/")
                .withCookies(
                  "userType" -> "normal", 
                  "username" -> username, 
                  "password" -> password
                )
        case _ => Response.redirect(uri"/register" |> Uri.withError("Invalid form")).pure[Connection]
    yield response
  private def page(message: Option[String]): Frag =
    View.layout(
      h1(`class` := "text-3xl font-bold mb-8", "Register"),
      message match
        case Some(msg) => h2(`class` := "text-red-500 mb-4", msg)
        case None => (),
      submitForm,
      loginButtons
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
