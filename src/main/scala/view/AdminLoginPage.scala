package view

import cats.effect.IO
import model.User
import org.http4s.headers.Location
import org.http4s.implicits.{path, uri}
import org.http4s.{Headers, Query, Request, Response, Status, Uri, UrlForm}
import scalatags.Text.all.*


object AdminLoginPage:
  def get(req: Request[IO]): IO[Response[IO]] =
    val message = req.params.get("error")
    def body = View.layout(
      div(
        `class` := "container flex flex-col items-center",
        h1(`class` := "text-3xl font-bold mb-8", "Admin Login"),
        message.map(h2(`class` := "text-red-500 mb-4", _)).getOrElse(()),
        adminLoginForm,
        loginButtons
      )
    )
    IO.pure(body.toResponse)

  def post(req: Request[IO]): IO[Response[IO]] =
    for form <- req.as[UrlForm]
      users <- User.findAll
      codeOption = form.getFirst("code")
      res <- codeOption match
        case None => IO.pure(Response[IO](Status.SeeOther)
          .putHeaders(Location(uri"/admin-login"))
          .addCookie("error", "Invalid form"))
        case Some(code) =>
          val userOption = users.collectFirst({ case u: User.Admin if u.code == code => u })
          userOption match
            case None =>
              val uri = Uri(
                path = path"/admin-login",
                query = Query("error" -> Some("Invalid code"))
              )
              IO.pure(Response[IO](Status.SeeOther)
                .putHeaders(Location(uri)))
            case Some(user) =>
              val response = Response[IO](
                status = Status.SeeOther,
                headers = Headers(Location(uri"/"))
              )
                .addCookie("userType", "admin")
                .addCookie("code", user.code)
              IO.pure(response)
    yield res


  private def loginButtons: Frag =
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
