package view

import cats.effect.IO
import cats.syntax.all.*
import scalatags.Text.all.*
import org.http4s.implicits.uri
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import database.*
import model.*



object RegisterPage:
  def routes(db: Db): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req @ GET -> Root => get(req).pure[IO]
    case req @ POST -> Root => post(req).run(db)
    
  private def get(request: Request): Response = page(request.params.get("error")).toResponse


  private def post(request: Request): Connection[Response] =
    for form <- request.getForm.toConnection
      users <- Users.findAll
      response <- (form.getFirst("username"), form.getFirst("password")) match
        case (Some(username), Some(password)) =>
          val existingUser = users.collect({ case u: User.Normal => u})
            .find(_.username == username)
          existingUser match
            case Some(_) => Response.redirect(uri"/register".withError("Username already exists")).pure[Connection]
            case None => 
              for _ <- Users.insert(Users.Insert.Normal(username, password))
              yield Response.redirect(uri"/")
                .withCookies(
                  "userType" -> "normal", 
                  "username" -> username, 
                  "password" -> password
                )
        case _ => Response.redirect(uri"/register".withError("Invalid form")).pure[Connection]
    yield response
  private def page(message: Option[String]): Frag =
    View.layout(
      div(
        `class` := "card card-normal m-16 bg-base-200",
        div(
          `class` := "card-body items-center",
          h1(`class` := "card-title", "Register"),
          message match
            case Some(msg) => h2(`class` := "alert alert-error", msg)
            case None => (),
            submitForm,
          loginButtons
        )
      )
    )

  private def submitForm: Frag =
    form(
      `class` := "contents",
      method := "post",
      action := "/register",
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
          value := "Register"
        )
      )
    )
  private def loginButtons: Frag =
    div(
      `class` := "card-actions",
      a(
        `class` := "link link-primary",
        href := "/login",
        "Login here"
      ),
      a(
        `class` := "link link-primary",
        href := "/admin-login",
        "Admin login"
      )
    )
