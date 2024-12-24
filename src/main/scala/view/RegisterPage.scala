package view

import cats.effect.IO
import model.User
import org.http4s.headers.Location
import org.http4s.implicits.{path, uri}
import org.http4s.{Headers, Query, Request, Response, Status, Uri, UrlForm}
import scalatags.Text.all.*



object RegisterPage:
  def get(req: Request[IO]): IO[Response[IO]] =
    val message = req.params.get("error")
    val body = View.layout(
      div(
        `class` := "container flex flex-col items-center",
        h1(`class` := "text-3xl font-bold mb-8", "Register"),
        message.map(msg => h2(`class` := "text-red-500 mb-4", msg)).getOrElse(()),
        submitForm,
        loginButtons
      )
    )
    IO.pure(body.toResponse)

  def post(req: Request[IO]): IO[Response[IO]] =
    for form <- req.as[UrlForm]
      users <- User.findAll
      insertOption: Option[User.Insert.Normal] =
        for username <- form.getFirst("username")
            password <- form.getFirst("password")
        yield User.Insert.Normal(username, password)
      res <- insertOption match
        case None =>
          IO.pure(Response[IO](
            status = Status.SeeOther,
            headers = Headers(Location(Uri(
              path = path"/register",
              query = Query("error" -> Some("Invalid form"))
            )))
          ))
        case Some(insert) =>
          if users.collectFirst({ case u: User.Normal if u.username == insert.username => u }).isDefined then
            val uri = Uri(
              path = path"/register",
              query = Query("error" -> Some("Username already exists"))
            )
            IO.pure(Response[IO](Status.SeeOther).putHeaders(Location(uri)))
          else
            for _ <- User.insert(insert)
            yield Response[IO](Status.SeeOther)
              .putHeaders(Location(uri"/login"))
              .addCookie("userType", "normal")
              .addCookie("username", insert.username)
              .addCookie("password", insert.password)
    yield res

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
