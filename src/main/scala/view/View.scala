package view

import scalatags.Text.all.*


object View:
  def layout(content: Frag*): Frag =
    html(
      head(
        script(src := "https://cdn.tailwindcss.com"),
        script(src := "https://unpkg.com/htmx.org@2.0.4"),
      ),
      body(
        `class` := "bg-gray-50",
        div(
          `class` := "container flex flex-col items-center",
          content
        )
      )
    )