package view

import cats.effect.IO
import cats.syntax.all.*
import database.*
import model.*
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import scalatags.Text.all.*

object AdminLoginPage:

  def routes(db: Db): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req @ GET -> Root => get(req).pure[IO]
    case req @ POST -> Root => post(req).run(db)

  private def get(request: Request): Response = page(request.params.get("error")).toResponse

  private def page(message: Option[String]): Frag =
    View.layout(
      div(
        `class` := "card card-normal bg-base-200 m-16",
        div(
          `class` := "card-body items-center",
          h1(`class` := "card-title", "Admin Login"),
          message match
            case Some(msg) => h2(`class` := "alert alert-error", msg)
            case None => ()
              ,
            adminLoginForm,
          linksToOtherPages
        )
      )
    )

  private def linksToOtherPages: Frag =
    div(
      `class` := "card-actions",
      a(
        `class` := "link link-primary",
        href := "/login",
        "Login"
      ),
      a(
        `class` := "link link-primary",
        href := "/register",
        "Register"
      )
    )

  private def post(request: Request): Connection[Response] =
    for form <- request.getForm.toConnection
        users <- Users.findAll
    yield form.getFirst("code") match
      case None => Response.redirect(uri"/admin-login".withError("Code is required"))
      case Some(code) => users.findAdmin(code) match
        case None =>
          Response.redirect(uri"/admin-login".withError("Invalid code"))
        case Some(user) =>
          Response.redirect(uri"/")
            .withCookies(
              "userType" -> "admin",
              "code" -> user.code
            )

  private def adminLoginForm: Frag =
    form(
      `class` := "contents",
      method := "post",
      action := "/admin-login",
      input(
        `class` := "input",
        `type` := "password",
        name := "code",
        placeholder := "Code"
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
