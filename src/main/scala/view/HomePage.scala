package view

import cats.effect.IO
import database.Users
import database.Todos
import database.Connection
import model.{Request, Response, Todo, User}
import org.http4s.implicits.uri
import org.http4s.{EntityDecoder, HttpRoutes, Status, Uri, UrlForm}
import scalatags.Text.all.*
import model.Uri.*
import model.User.*
import model.Request.*
import model.Response.*
import cats.syntax.applicative.*
import model.Client
import model.Quote
import model.Quote.*

object HomePage:
  def logout: Response =
    Response.redirect(uri"/login")
      .removeCookies("userType", "username", "password", "code")


  def get(request: Request, client: Client): IO[Connection[Response]] =
    for quote <- Quote.getOne(client)
    yield for users <- Users.findAll
      todos <- Todos.findAll
    yield request.getUser(users) match
      case None => Response.redirect(uri"/login")
        .removeCookies("userType", "username", "password", "code")
      case Some(user) => page(user, todos, quote).toResponse


  def toggle(id: Int): Connection[Response] =
    for todoOption <- Todos.find(id)
      res <- todoOption match
        case Some(todo) =>
          for _ <- Todos.update(todo.toggle)
          yield todoCard(todo.toggle).toResponse
        case None => Response(Status.NotFound).pure[Connection]
    yield res


  def post(req: Request): IO[Connection[Response]] =
    for form <- req.as[UrlForm]
    yield for
      users <- Users.findAll
      res <- req.getUser(users) match
        case None => Response
          .redirect(uri"/login".withError("Log in to create a todo."))
          .removeCookies("userType", "username", "password")
          .pure[Connection]
        case Some(user) => form.getFirst("title") match
          case None => Response(Status.BadRequest).pure[Connection]
          case Some(title) =>
            for _ <- Todos.create(title, false, user.id)
              todos <- Todos.findAll
            yield
              val myTodos = todos.filter(_.ownerId == user.id)
              todoList(myTodos).toResponse
    yield res


  private def page(user: User, todos: List[Todo], quote: Quote): Frag =
    View.layout(
      div(
        `class` := "container flex flex-col items-center",
        header(user),
        motivationalQuote(quote),
        title,
        user match
          case _: User.Admin => todoList(todos)
          case normal: User.Normal =>
            val myTodos = todos.filter(_.ownerId == normal.id)
            List(todoList(myTodos), newTodoForm(normal))
      )
    )

  private def motivationalQuote(quote: Quote): Frag =
    div(
      `class` := "bg-white shadow-md rounded-lg p-4 my-4 w-3/5",
      p(`class` := "text-lg font-semibold my-2", quote.q),
      p(`class` := "text-gray-600", "-" ++ quote.a)
    )
  private def title: Frag = h1(`class` := "text-3xl font-bold mb-8", "Todo List")

  private def todoCard(todo: Todo): Frag =
    div(
      `class` := "bg-white shadow-md rounded-lg p-4 my-4 w-3/5",
      id := s"todo-${todo.id}",
      h1(`class` := "text-lg font-semibold my-2", todo.title),
      input(
        `class` := "mr-2",
        `type` := "checkbox",
        attr("hx-post") := s"/todo/${todo.id}/toggle",
        attr("hx-trigger") := "change",
        attr("hx-target") := s"#todo-${todo.id}",
        attr("hx-swap") := "outerHTML",
        if todo.complete then checked else ()
      ),
      p(`class` := "text-gray-600", if todo.complete then "Completed" else "Not completed")
    )
  private def todoList(todos: List[Todo]): Frag =
    div(
      `class` := "flex flex-col items-center w-full",
      id := "todos",
      for todo <- todos yield todoCard(todo)
    )

  private def newTodoForm(user: User.Normal): Frag =
    div(
      `class` := "bg-white shadow-md rounded-lg p-4 my-4 w-3/5",
      form(
        `class` := "flex",
        attr("hx-post") := s"/todo",
        attr("hx-trigger") := "submit",
        attr("hx-target") := "#todos",
        attr("hx-swap") := "outerHTML",
        input(
          `class` := "p-2 border rounded-lg",
          `type` := "text",
          name := "title",
          placeholder := "New todo"
        ),
        input(
          `type` := "submit",
          `class` := "p-2 bg-blue-500 text-white rounded-lg ml-2",
          value := "Add"
        )
      )
    )

  private def header(user: User): Frag =
    div(
      `class` := "flex items-center justify-between w-3/5 mt-4",
      p(`class` := "text-lg", s"Welcome, ${user.title}"),
      a(
        href := "/logout",
        `class` := "p-2 bg-red-500 text-white rounded-lg",
        "Logout"
      )
    )
