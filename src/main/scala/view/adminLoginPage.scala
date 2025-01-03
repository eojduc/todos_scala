package view

import scalatags.Text.all.*

def adminLoginPage(message: Option[String]): doctype =
  layout(
    div(
      `class` := "card card-normal bg-base-200 m-16",
      div(
        `class` := "card-body items-center",
        h1(`class` := "card-title", "Admin Login"),
        message match
          case Some(msg) => h2(`class` := "alert alert-error", msg)
          case None => ()
        ,
        adminLoginForm,
        loginAndRegisterLinks
      )
    )
  )


private def loginAndRegisterLinks =
  div(
    `class` := "card-actions",
    a(
      `class` := "link link-primary",
      href := "/login",
      "Login"
    ),
    a(
      `class` := "link link-primary",
      href := "/register",
      "Register"
    )
  )

private def adminLoginForm =
  form(
    `class` := "contents",
    method := "post",
    action := "/admin-login",
    input(
      `class` := "input",
      `type` := "password",
      name := "code",
      placeholder := "Code"
    ),
    div(
      `class` := "flex flex-row justify-end w-full",
      input(
        `type` := "submit",
        `class` := "btn btn-primary",
        value := "Login"
      )
    )
  )