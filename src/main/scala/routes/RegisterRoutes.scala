package routes

import scalatags.Text.all.*
import view.View
import database.simple.*
import scalasql.dialects.PostgresDialect.*
import scalasql.*


case class RegisterRoutes()(implicit cc: castor.Context, log: cask.Logger) extends cask.Routes{

  @cask.get("/register")
  def getRegister(error: Option[String] = None, request: cask.Request) = {
    getUser(request) match
      case Some(user) => cask.Redirect("/")
      case None => cask.Response(
        page(error).render,
        headers = Seq("Content-Type" -> "text/html")
      )
  }

  @cask.postForm("/register")
  def postRegister(username: cask.FormValue, password: cask.FormValue) =
    if username.value.isBlank || password.value.isBlank then
      cask.Response(
        page(Some("Username and password cannot be empty")).render,
        headers = Seq("Content-Type" -> "text/html")
      )
    else
      val existingUser = dbClient.transaction(_.run(NormalUser.select.filter(_.username === username.value)).toList)
      if !existingUser.isEmpty then
        cask.Response(
          page(Some("User already exists")).render,
          headers = Seq("Content-Type" -> "text/html")
        )
      else
        insertUser(username.value, password.value)
        cask.Response(
          "Logged in",
          statusCode = 302,
          headers = Seq("Location" -> "/login"),
          cookies = Seq(cask.Cookie("username", username.value), cask.Cookie("password", password.value), cask.Cookie("userType", "normal"))
        )


  initialize()

  private def page(message: Option[String]) =
    View.layout2(
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
}


