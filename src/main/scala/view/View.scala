package view

import cats.effect.IO
import org.http4s.headers.`Content-Type`
import org.http4s.{Headers, MediaType, Request, Response, Status}
import scalatags.Text.all.Frag

extension (request: Request[IO])
  def getCookie(name: String): Option[String] = request.cookies.find(_.name == name).map(_.content)

extension (html: Frag)
  def toResponse: Response[IO] =
    Response[IO](
      status = Status.Ok,
      headers = Headers(`Content-Type`(MediaType.text.html)),
      body = fs2.Stream(html.render).through(fs2.text.utf8.encode)
    )

object View:
      
  import scalatags.Text.all.*
  def layout(content: Frag): Frag =
    html(
      head(
        script(src := "https://cdn.tailwindcss.com"),
        script(src := "https://unpkg.com/htmx.org@2.0.4"),
      ),
      body(
        `class` := "bg-gray-50 text-gray-800 font-sans",
        content
      )
    )




