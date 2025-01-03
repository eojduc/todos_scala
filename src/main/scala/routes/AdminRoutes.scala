package routes

import database.simple.*
import view.{View}
import scalasql.dialects.PostgresDialect.TableOpsConv
import scalatags.Text.all.*


case class AdminLoginRoutes()(implicit cc: castor.Context, log: cask.Logger) extends cask.Routes{

  @cask.get("/admin-login")
  def get(request: cask.Request, error: Option[String] = None) =
    getUser(request) match
      case Some(_) => cask.Response("Already logged in", statusCode = 302, headers = Seq("Location" -> "/"))
      case None => cask.Response(
        View.layout2(page(error)).render,
        headers = Seq("Content-Type" -> "text/html")
      )
  @cask.postForm("/admin-login")
  def login(code: cask.FormValue) =
    val adminUsers = dbClient.transaction(_.run(AdminUser.select))
    val adminUser = adminUsers.find(_.code == code.value)
    adminUser match
      case Some(_) => cask.Response(
        "Logged in",
        cookies = Seq(cask.Cookie("userType", "admin"), cask.Cookie("code", code.value)),
        statusCode = 302,
        headers = Seq("Location" -> "/")
      )
      case None => cask.Redirect("/admin-login?error=Invalid code")
  
  initialize()
  
  
  @cask.get("/cookies")
  def cookies(request: cask.Request) = request.cookies.toString()

  def page(message: Option[String]): Frag =
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
        linksToOtherPages
      )
    )

  private def linksToOtherPages: Frag =
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

  private def adminLoginForm: Frag =
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
}
 
