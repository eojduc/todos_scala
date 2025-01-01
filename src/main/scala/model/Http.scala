package model

import cats.effect.IO
import org.http4s.{MediaType, Status, UrlForm}
import fs2.Stream
import org.http4s.headers.`Content-Type`
import scalatags.Text.all.Frag
import util.*

type Request = org.http4s.Request[IO]
type Response = org.http4s.Response[IO]
type Client = org.http4s.client.Client[IO]
type Uri = org.http4s.Uri

object Request:
  extension (request: Request)
    def getCookie(name: String): Option[String] = request.cookies.find(_.name == name).map(_.content)
    def getForm: IO[UrlForm] = request.as[UrlForm]
    def getUser(users: List[User]): Option[User] =
      request.getCookie("userType") match
        case Some("normal") =>
          for 
            username <- request.getCookie("username")
            password <- request.getCookie("password")
            user <- users.findUser(username, password)
          yield user
        case Some("admin") =>
          for 
            code <- request.getCookie("code")
            user <- users.findAdmin(code)
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



object Frag:
  extension (frag: Frag)
    def toResponse: Response =
      val body = fs2.Stream(frag.render).through(fs2.text.utf8.encode)
      Response.ok(body).withContentType (`Content-Type` (MediaType.text.html) )

object Uri:
  def withError(error: String)(uri: Uri): Uri =
    uri.withQueryParam("error", error)

