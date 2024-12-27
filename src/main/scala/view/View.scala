package view

import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import scalatags.Text.all.*
import model.Response



extension (fragment: Frag)
  def  toResponse: Response =
    Response.ok(
      fs2.Stream(fragment.render).through(fs2.text.utf8.encode)
    ).withContentType(`Content-Type`(MediaType.text.html))

object View:
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