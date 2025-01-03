package view

import scalatags.Text.all.*

def loginPage(error: Option[String]): doctype =
  layout(
    div(
      `class` := "card card-normal bg-base-200 m-16",
      div(
        `class` := "card-body items-center",
        h1(`class` := "card-title", "Login"),
        error match
          case Some(err) => h2(`class` := "alert alert-error", err)
          case None => (),
        formToLogIn,
        linksToOtherPages,
      )
    )
  )
private def formToLogIn: Frag =
  form(
    `class` := "contents",
    method := "post",
    action := "/login",
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
        value := "Login"
      )
    )
  )
private def linksToOtherPages: Frag =
  div(
    `class` := "card-actions",
    a(
      `class` := "link link-primary",
      href := "/register",
      "Register"
    ),
    a(
      `class` := "link link-primary",
      href := "/admin-login",
      "Admin Login"
    )
  )