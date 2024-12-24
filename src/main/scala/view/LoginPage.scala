package view

import cats.effect.IO
import model.User
import org.http4s.headers.Location
import org.http4s.implicits.{path, uri}
import org.http4s.{Headers, Query, Request, Response, Status, Uri, UrlForm}
import scalatags.Text.all.*
    
    
object LoginPage:
  def get(req: Request[IO]): IO[Response[IO]] =
    val message = req.params.get("error")
    IO.pure(LoginPage.html(message).toResponse)
  def post(req: Request[IO]): IO[Response[IO]] =
    for form <- req.as[UrlForm]
      loginRequestOption = for
          username <- form.getFirst("username")
          password <- form.getFirst("password")
        yield (username, password)
      res <- loginRequestOption match
        case None => IO.pure(Response(Status.BadRequest).withEntity("Invalid form data"))
        case Some((username, password)) =>
          for users <- User.findAll
            res <- users.collectFirst({ case u: User.Normal if u.username == username && u.password == password => u }) match
              case None =>
                val uri = Uri(
                  path = path"/login",
                  query = Query("error" -> Some("Invalid login"))
                )
                IO.pure(
                  Response[IO](
                    status = Status.SeeOther,
                    headers = Headers(Location(uri))
                  )
                )
              case Some(user) =>
                val response = Response[IO](
                  status = Status.SeeOther,
                  headers = Headers(Location(uri"/"))
                )
                  .addCookie("userType", "normal")
                  .addCookie("username", user.username)
                  .addCookie("password", user.password)
                IO.pure(response)
          yield res
    yield res

  def html(message: Option[String]) =
    View.layout(
      div(
        `class` := "container flex flex-col items-center",
        h1(`class` := "text-3xl font-bold mb-8", "Login"),
        message
          .map(h2(`class` := "text-red-500 mb-4", _))
          .getOrElse(()),
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
        ),
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
      )
    )
  