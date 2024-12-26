package view

import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import scalatags.Text.all.Frag
import model.Response

extension (html: Frag)
  def toResponse: Response =
    Response.ok(
      fs2.Stream(html.render).through(fs2.text.utf8.encode)
    ).withContentType(`Content-Type`(MediaType.text.html))

object View:

  import scalatags.Text.all.*
  def layout(content: Frag): Frag =
    html(
      head(
        script(src := "https://cdn.tailwindcss.com"),
        script(src := "https://unpkg.com/htmx.org@2.0.4"),
      ),
      body(
        `class` := "bg-gray-50",
        content
      )
    )