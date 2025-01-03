package routes

import model.Quote
import scalatags.Text.all.*
import database.simple.*
import cask.{FormValue, Request}
import io.circe.Decoder
import scalasql.dialects.PostgresDialect.*
import scalasql.*
import view.View

import java.time.Instant
import sttp.client3.{SimpleHttpClient, UriContext, basicRequest}
import io.circe.generic.auto._
import sttp.client3.circe._
import io.circe.generic.auto.deriveDecoder

val client = SimpleHttpClient()

val response = basicRequest
  .get(uri"https://api.quotable.io/random")
  .response(asJson[List[Quote]])
case class HomeRoutes()(implicit cc: castor.Context, log: cask.Logger) extends cask.Routes{

  @cask.get("/")
  def home(request: Request) = {
    val quotes = client.send(response).body match
      case Right(value) => value
      case Left(error) => List()
    getUser(request) match
      case None => cask.Redirect("/login")
      case Some(user) =>
        val todos = allTodos().toList
        val quote = Quote.firstOrDefault(List())
        cask.Response(
          page(user, todos, quote).render,
          headers = Seq("Content-Type" -> "text/html"),
        )
  }
  extension (todo: Todo[Sc])
    def toggle = todo.copy(complete = !todo.complete)


  @cask.get("/logout")
  def logout() = cask.Response("", statusCode = 302,
    cookies = Seq(
      cask.Cookie("userType", "", expires = Instant.ofEpochSecond(0)),
      cask.Cookie("username", "", expires = Instant.ofEpochSecond(0)),
      cask.Cookie("password", "", expires = Instant.ofEpochSecond(0))
    ),
    headers = Seq("Location" -> "/")
  )

  @cask.postForm("/todo")
  def addTodo(title: FormValue, request: Request) =
    getUser(request) match
      case Some(user: NormalUser[Sc]) =>
        insertTodo(title.value, false, user.id)
        cask.Response(
          todoList(allTodos().filter(_.ownerId == user.id).toList).render,
          headers = Seq("Content-Type" -> "text/html"),
        )
      case _ => cask.Response("Not found", statusCode = 404)

  @cask.post("/todo/:id/toggle")
  def toggleTodo(id: Int) =
    getTodoById(id) match
      case None => cask.Response("Not found", statusCode = 404)
      case Some(todo) =>
        val toggled = todo.toggle
        updateTodo(toggled)
        cask.Response(
          todoCard(toggled).render,
          headers = Seq("Content-Type" -> "text/html"),
        )
  initialize()


  private def page(user: User[Sc], todos: List[Todo[Sc]], quote: Quote)  =
    View.layout2(
      header(user),
      div(
        `class` := "flex flex-col items-center w-3/5 gap-8 my-4",
        motivationalQuote(quote),
        title,
        user match
          case _: AdminUser[Sc] => todoList(todos)
          case normal: NormalUser[Sc] =>
            val myTodos = todos.filter(_.ownerId == normal.id)
              List(todoList(myTodos), newTodoForm(normal))
      )
    )

  private def motivationalQuote(quote: Quote): Frag =
    div(
      `class` := "card bg-base-200 card-normal w-3/5 my-2",
      div(
        `class` := "card-body",
        h2(`class` := "text-lg", quote.q),
        h3("-" ++ quote.a)
      )
    )

  private def title: Frag = h1(`class` := "text-3xl font-bold", "Todo List")

  private def todoCard(todo: Todo[Sc]): Frag =
    div(
      `class` := "bg-base-200 card card-normal w-full",
      id := s"todo-${todo.id}",
      div(
        `class` := "card-body",
        h5(`class` := "card-title", todo.title),
        input(
          `class` := "checkbox",
          `type` := "checkbox",
          attr("hx-post") := s"/todo/${todo.id}/toggle",
          attr("hx-trigger") := "change",
          attr("hx-target") := s"#todo-${todo.id}",
          attr("hx-swap") := "outerHTML",
          if todo.complete then checked else ()
        ),
        p(if todo.complete then "Completed" else "Not completed")
      )
    )

  private def todoList(todos: List[Todo[Sc]]): Frag =
    div(
      `class` := "contents",
      id := "todos",
      for todo <- todos yield todoCard(todo)
    )

  private def newTodoForm(user: NormalUser[Sc]): Frag =
    div(
      `class` := "card card-normal bg-base-200 w-3/5",
      div(
        `class` := "card-body",
        h2(`class` := "card-title", "Add a new todo"),
        form(
          `class` := "contents",
          attr("hx-post") := s"/todo",
          attr("hx-trigger") := "submit",
          attr("hx-target") := "#todos",
          attr("hx-swap") := "outerHTML",
          input(
            `class` := "input",
            `type` := "text",
            name := "title",
            placeholder := "title"
          ),
          div(
            `class` := "flex justify-end w-full",
            input(
              `type` := "submit",
              `class` := "btn btn-primary",
              value := "Add"
            )
          )
        )
      )

    )

  def title(user: User[Sc]): String = user match
    case _: AdminUser[Sc] => "admin"
    case normal: NormalUser[Sc] => normal.username

  private def header(user: User[Sc]): Frag =
    div(
      `class` := "navbar",
      div(
        `class` := "navbar-start",
        h3(`class` := "text-lg", s"Welcome, ${title(user)}"),
      ),
      div(
        `class` := "navbar-end",
        a(
          href := "/logout",
          `class` := "p-2 btn btn-secondary",
          "Logout"
        )
      )
    )


}


