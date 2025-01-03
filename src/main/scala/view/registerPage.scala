package view

import scalatags.Text.all.*
def registerPage(message: Option[String]) =
  layout(
    div(
      `class` := "card card-normal m-16 bg-base-200",
      div(
        `class` := "card-body items-center",
        h1(`class` := "card-title", "Register"),
        message match
          case Some(msg) => h2(`class` := "alert alert-error", msg)
          case None => ()
        ,
        submitForm,
        loginButtons
      )
    )
  )

private def submitForm: Frag =
  form(
    `class` := "contents",
    method := "post",
    action := "/register",
    input(
      `class` := "input",
      `type` := "text",
      name := "username",
      placeholder := "Username"
    ),
    input(
      `class` := "input",
      `type` := "password",
      name := "password",
      placeholder := "Password"
    ),
    div(
      `class` := "flex flex-row justify-end w-full",
      input(
        `type` := "submit",
        `class` := "btn btn-primary",
        value := "Register"
      )
    )
  )

private def loginButtons: Frag =
  div(
    `class` := "card-actions",
    a(
      `class` := "link link-primary",
      href := "/login",
      "Login here"
    ),
    a(
      `class` := "link link-primary",
      href := "/admin-login",
      "Admin login"
    )
  )