package view

import scalatags.Text.all.*


object View:
  def layout(content: Frag*): Frag =
    html(
      head(
        script(src := "https://cdn.tailwindcss.com"),
        script(src := "https://unpkg.com/htmx.org@2.0.4"),
        link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/daisyui@4.12.23/dist/full.min.css"),
      ),
      body(
        `class` := "bg-base-100",
        div(
          `class` := "container flex flex-col items-center",
          content
        )
      )
    )