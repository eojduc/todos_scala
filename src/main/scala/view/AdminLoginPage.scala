package view

import cats.effect.IO
import database.{Connection, Db, Users}
import model.{Request, Response, User}
import org.http4s.implicits.uri
import scalatags.Text.all.*
import model.Uri.withError
import model.Request.getForm
import model.Response.*
import model.User.*
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import cats.syntax.all.*
import doobie.implicits.*
object AdminLoginPage:

  def routes(db: Db): HttpRoutes[IO] = HttpRoutes.of[IO]:
    case req @ GET -> Root => get(req).pure[IO]
    case req @ POST -> Root => post(req).flatMap(_.transact(db))
  def get(request: Request): Response = page(request.params.get("error")).toResponse

  def post(request: Request): IO[Connection[Response]] =
    for form <- request.getForm
    yield for users <- Users.findAll
    yield form.getFirst("code") match
      case None => Response.redirect(uri"/admin-login".withError("Invalid form"))
      case Some(code) => users.findAdmin(code) match
        case None =>
          Response.redirect(uri"/admin-login".withError("Invalid code"))
        case Some(user) =>
          Response.redirect(uri"/")
            .withCookies(
              "userType" -> "admin",
              "code" -> user.code
            )
  
  private def page(message: Option[String]): Frag =
    View.layout(
      div(
        `class` := "container flex flex-col items-center",
        title,
        message match
          case Some(msg) => div(`class` := "text-red-500 mb-4", msg)
          case None => (),
        adminLoginForm,
        linksToOtherPages
      )
    )

  def title: Frag = h1(`class` := "text-3xl font-bold mb-8", "Admin Login")
  private def linksToOtherPages: Frag =
    div(
      `class` := "flex",
      a(
        `class` := "text-blue-500",
        href := "/login",
        "Login here"
      ),
      a(
        `class` := "text-blue-500 ml-4",
        href := "/register",
        "Register here"
      )
    )

  private def adminLoginForm: Frag =
    form(
      `class` := "flex",
      method := "post",
      action := "/admin-login",
      input(
        `class` := "p-2 border rounded-lg ml-2",
        `type` := "password",
        name := "code",
        placeholder := "Code"
      ),
      input(
        `type` := "submit",
        `class` := "p-2 bg-blue-500 text-white rounded-lg ml-2",
        value := "Login"
      )
    )
