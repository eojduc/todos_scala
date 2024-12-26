package model

import cats.effect.IO
import org.http4s.{Status, Uri, UrlForm}
import fs2.Stream

type Request = org.http4s.Request[IO]
type Response = org.http4s.Response[IO]
type Client = org.http4s.client.Client[IO]


object Request:
  extension (request: Request)
    def getForm: IO[UrlForm] = request.as[UrlForm]
    def getCookie(name: String): Option[String] = request.cookies.find(_.name == name).map(_.content)
    def getUser(users: List[User]): Option[User] =
      request.getCookie("userType") match
        case Some("normal") =>
          for // option
            username <- request.getCookie("username")
            password <- request.getCookie("password")
            user <- users.findUser(username, password)
          yield user
        case Some("admin") =>
          for // option
            code <- request.getCookie("code")
            user <- users
              .collect({ case u: User.Admin => u })
              .find(u => u.code == code)
          yield user
        case _ => None



object Response:
  def apply(status: org.http4s.Status): Response = org.http4s.Response[IO](status = status)
  def ok(stream: Stream[IO, Byte]): Response = org.http4s.Response[IO](
    status = Status.Ok,
    body = stream
  )

  def redirect(uri: org.http4s.Uri): Response = Response(status = org.http4s.Status.SeeOther)
    .putHeaders(org.http4s.headers.Location(uri))

  def badRequest(message: String): Response = Response(status = org.http4s.Status.BadRequest)
    .withEntity(message)

  extension (response: Response)
    def withCookies(cookies: (String, String)*): Response =
      cookies.foldLeft(response):
        case (res, (name, content)) => res.addCookie(name, content)
    def removeCookies(names: String*): Response =
      names.foldLeft(response):
        case (res, name) => res.removeCookie(name)


object Uri:
  extension (uri: Uri)
    def withError(error: String): Uri =
      uri.withQueryParam("error", error)

