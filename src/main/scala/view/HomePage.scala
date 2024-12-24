package view

import cats.effect.IO
import model.{User, *}
import org.http4s.headers.Location
import org.http4s.implicits.{path, uri}
import org.http4s.{Query, Request, Response, Status, Uri, UrlForm}
import scalatags.Text.all.{id, *}
object HomePage:

  def logout: IO[Response[IO]] =
    IO.pure(Response(Status.SeeOther)
    .putHeaders(Location(uri"/login"))
    .removeCookie("userType")
    .removeCookie("username")
    .removeCookie("password")
    .removeCookie("code"))

  def get(req: Request[IO]): IO[Response[IO]] =
    for
      users <- User.findAll
      todos <- Todo.findAll
    yield req.getCookie("userType") match
      case Some("admin") => getAllTodos(req, todos, users)
      case Some("normal") => getUserTodos(req, todos, users)
      case _ => Response(Status.SeeOther)
        .putHeaders(Location(uri"/login"))
        .removeCookie("userType")


  def toggle(id: Int): IO[Response[IO]] =
    for todoOption <- Todo.find(id)
      res <- todoOption match
        case Some(todo) => for _ <- Todo.update(todo.toggle) yield todo.toggle.toHtml.toResponse
        case None => IO.pure(Response(Status.NotFound))
    yield res

  def post(req: Request[IO]): IO[Response[IO]] =
    for form <- req.as[UrlForm]
      users <- User.findAll
      normalUsers = users.collect({ case u: User.Normal => u })
      userOption =
        for username <- req.getCookie("username")
            password <- req.getCookie("password")
            user <- normalUsers.collectFirst({ case u if u.username == username && u.password == password => u })
        yield user
      res <- userOption match
        case None =>
          val uri = Uri(
            path = path"/login",
            query = Query("error" -> Some("Invalid user"))
          )
          IO.pure(Response[IO](Status.SeeOther)
            .putHeaders(Location(uri)))
        case Some(user) =>
          val createOption = for
            title <- form.getFirst("title")
            ownerId <- form.getFirst("ownerId").flatMap(_.toIntOption)
            complete <- form.getFirst("complete").flatMap(_.toBooleanOption)
          yield (title, complete, ownerId)
          createOption match
            case None => IO.pure(Response(Status.BadRequest))
            case Some((title, complete, ownerId)) =>
              for _ <- Todo.create(title, complete, ownerId)
                  todos <- Todo.findAll
              yield todos.filter(_.ownerId == user.id).toHtml.toResponse
    yield res


  extension (todo: Todo)
    private def toHtml: Frag =
      div(
        `class` := "bg-white shadow-md rounded-lg p-4 my-4 w-3/5",
        h1(`class` := "text-lg font-semibold my-2", todo.title),
        input(
          `class` := "mr-2",
          `type` := "checkbox",
          attr("hx-post") := s"/todo/${todo.id}/toggle",
          attr("hx-trigger") := "change",
          attr("hx-target") := s"#todo-${todo.id}",
          if todo.complete then checked else ()
        ),
        p(`class` := "text-gray-600", if todo.complete then "Completed" else "Not completed")
      )
  extension (todos: List[Todo])
    private def toHtml: Frag =
      div(
        `class` := "flex flex-col items-center w-full",
        for todo <- todos yield div(`class` := "contents", id := s"todo-${todo.id}", todo.toHtml)
      )


  private def getAllTodos(req: Request[IO], todos: List[Todo], users: List[User]): Response[IO] =
    val userOption =
      for code <- req.getCookie("code")
          user <- users.collectFirst({ case u: User.Admin if u.code == code => u })
      yield user
    userOption match
      case None => Response(Status.SeeOther)
        .putHeaders(Location(uri"/login"))
        .removeCookie("userType")
        .removeCookie("code")
      case Some(user) =>
        View.layout(
          div(
            `class` := "container flex flex-col items-center",
            header(user),
            h1(`class` := "text-3xl font-bold mb-8", "Todo List"),
            div(
              id := "todos",
              `class` := "contents",
              todos.toHtml,
            )
          )
        ).toResponse


  private def getUserTodos(req: Request[IO], todos: List[Todo], users: List[User]): Response[IO] =
    val userOption =
      for username <- req.getCookie("username")
          password <- req.getCookie("password")
          user <- users.collectFirst({ case u: User.Normal if u.username == username && u.password == password => u })
      yield user
    userOption match
      case None => Response(Status.SeeOther)
        .putHeaders(Location(uri"/login"))
        .removeCookie("userType")
        .removeCookie("username")
        .removeCookie("password")
      case Some(user) =>
        val myTodos = todos.filter(_.ownerId == user.id)
        View.layout(
          div(
            `class` := "container flex flex-col items-center",
            header(user),
            h1(`class` := "text-3xl font-bold mb-8", "Todo List"),
            div(
              id := "todos",
              `class` := "contents",
              myTodos.toHtml,
            ),
            newTodoForm(user.id.toString)
          )
        ).toResponse


  private def newTodoForm(id: String): Frag =
    div(
      `class` := "bg-white shadow-md rounded-lg p-4 my-4 w-3/5",
      form(
        `class` := "flex",
        attr("hx-post") := "/todo",
        attr("hx-trigger") := "submit",
        attr("hx-target") := "#todos",
        input(
          `class` := "p-2 border rounded-lg",
          `type` := "text",
          name := "title",
          placeholder := "New todo"
        ),
        input(
          `type` := "hidden",
          name := "ownerId",
          value := id
        ),
        input(
          `type` := "hidden",
          name := "complete",
          value := "false"
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
