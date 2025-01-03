package routes

import database.simple.*
import view.{View}
import scalasql.dialects.PostgresDialect.TableOpsConv
import scalatags.Text.all.*


case class LoginRoutes()(implicit cc: castor.Context, log: cask.Logger) extends cask.Routes{

  @cask.get("/login")
  def get(request: cask.Request, error: Option[String] = None) =
    getUser(request) match
      case Some(_) => cask.Response("Already logged in", statusCode = 302, headers = Seq("Location" -> "/"))
      case None => cask.Response(
        View.layout2(page(error)).render,
        headers = Seq("Content-Type" -> "text/html")
      )
      
      
  @cask.postForm("/login")
  def login(username: cask.FormValue, password: cask.FormValue) =
    val normalUsers = dbClient.transaction(_.run(NormalUser.select))
    val normalUser = normalUsers.find(user => user.username == username.value && user.password == password.value)
    normalUser match
      case Some(_) => cask.Response(
        "Logged in",
        cookies = Seq(cask.Cookie("userType", "normal"), cask.Cookie("username", username.value), cask.Cookie("password", password.value)),
        statusCode = 302,
        headers = Seq("Location" -> "/")
      )
      case None => cask.Redirect("/login?error=Invalid code")

  def page(error: Option[String]): Frag =
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
  initialize()
}


 
